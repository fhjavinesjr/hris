package com.primehr.performancemanagement.report.application;

import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.ApplicationConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.primehr.performancemanagement.report.application.PerformanceReportingData.*;

@Service
@Transactional(readOnly = true)
public class PerformanceReportingServiceImpl implements PerformanceReportingService {
    static final String SCHEMA_VERSION = "1.0";
    static final String TIMEZONE = "Asia/Manila";
    private static final int MAX_RANGE_DAYS = 366;
    private static final int MAX_SCAN = 10_000;

    private final PerformanceFinalRatingRepository finalRatings;
    private final PerformanceRatingCaseRepository ratingCases;
    private final PerformanceCommitmentVersionRepository commitments;
    private final PerformanceRatingAcknowledgmentRepository acknowledgments;
    private final PerformanceRatingAppealRepository appeals;
    private final PerformanceInterventionRepository interventions;
    private final PrimeHrAuditService audit;
    private final int minimumAggregateCohort;

    public PerformanceReportingServiceImpl(PerformanceFinalRatingRepository finalRatings,
            PerformanceRatingCaseRepository ratingCases,
            PerformanceCommitmentVersionRepository commitments,
            PerformanceRatingAcknowledgmentRepository acknowledgments,
            PerformanceRatingAppealRepository appeals,
            PerformanceInterventionRepository interventions,
            PrimeHrAuditService audit,
            @Value("${primehr.reporting.minimum-aggregate-cohort:5}") int minimumAggregateCohort) {
        this.finalRatings = finalRatings;
        this.ratingCases = ratingCases;
        this.commitments = commitments;
        this.acknowledgments = acknowledgments;
        this.appeals = appeals;
        this.interventions = interventions;
        this.audit = audit;
        if (minimumAggregateCohort < 2 || minimumAggregateCohort > 100) {
            throw new IllegalArgumentException("minimum aggregate cohort must be between 2 and 100");
        }
        this.minimumAggregateCohort = minimumAggregateCohort;
    }

    @Override
    public RegisterPage register(String agencyId, RegisterQuery query, String actor, String correlationId) {
        validate(query);
        List<Snapshot> rows = filtered(agencyId, query);
        List<Snapshot> page = page(rows, query.page(), query.size());
        RegisterPage result = new RegisterPage(query.from(), query.to(), query.includeHistory(), query.page(),
                query.size(), rows.size(), pages(rows.size(), query.size()), page.stream().map(this::row).toList());
        audit(agencyId, "VIEW_SPMS_PERFORMANCE_REGISTER", "SPMS_PERFORMANCE_REGISTER", actor,
                registerAudit(query, result.content().size()), correlationId);
        return result;
    }

    @Override
    public Analytics analytics(String agencyId, RegisterQuery query, String actor, String correlationId) {
        validate(query);
        List<Snapshot> rows = filtered(agencyId, query);
        int cohort = (int) rows.stream().map(value -> value.ratingCase().getOwnerEmployeeNo().toUpperCase(Locale.ROOT))
                .distinct().count();
        if (cohort < minimumAggregateCohort) {
            Analytics result = new Analytics(query.from(), query.to(), minimumAggregateCohort, 0, true,
                    "Results are suppressed because the distinct employee cohort is below the configured minimum.",
                    List.of(), List.of());
            audit(agencyId, "VIEW_SPMS_PERFORMANCE_ANALYTICS", "SPMS_PERFORMANCE_ANALYTICS", actor,
                    analyticsAudit(query, cohort, true, 0), correlationId);
            return result;
        }

        long acknowledged = rows.stream().filter(value -> value.acknowledgment() != null
                && value.acknowledgment().getStatus() == PerformanceRatingAcknowledgment.Status.ACKNOWLEDGED).count();
        long appealed = rows.stream().filter(value -> value.appeal() != null).count();
        long intervened = rows.stream().filter(value -> value.intervention() != null
                && value.intervention().getAction() != PerformanceInterventionRule.Action.NONE).count();
        List<AggregateMetric> metrics = List.of(
                count("FINAL_OUTCOMES", "Final rating outcomes", rows.size(),
                        "Eligible final-rating revisions in the selected cohort."),
                count("DISTINCT_EMPLOYEES", "Distinct employees", cohort,
                        "Distinct rated employees in the selected cohort."),
                average("AVERAGE_FINAL_SCORE", "Average final score", rows,
                        "Arithmetic mean of immutable final scores."),
                ratio("ACKNOWLEDGED_RATE", "Acknowledged final ratings", acknowledged, rows.size(),
                        "Acknowledged final ratings divided by eligible outcomes."),
                ratio("APPEAL_RATE", "Outcomes with an appeal", appealed, rows.size(),
                        "Outcomes with an appeal record divided by eligible outcomes."),
                ratio("INTERVENTION_RATE", "Outcomes with an intervention", intervened, rows.size(),
                        "Outcomes with a non-NONE intervention divided by eligible outcomes."));
        List<AggregateBreakdown> breakdowns = new ArrayList<>();
        addBreakdowns(breakdowns, "FORM_TYPE", rows, value -> value.ratingCase().getFormType(),
                value -> value.ratingCase().getFormType());
        addBreakdowns(breakdowns, "RATING_LABEL", rows, value -> value.rating().getRatingLabel(),
                value -> value.rating().getRatingLabel());
        addBreakdowns(breakdowns, "BUSINESS_UNIT", rows,
                value -> String.valueOf(value.ratingCase().getBusinessUnitId()),
                value -> value.commitment().getBusinessUnitName());
        Analytics result = new Analytics(query.from(), query.to(), minimumAggregateCohort, cohort, false, null,
                metrics, List.copyOf(breakdowns));
        audit(agencyId, "VIEW_SPMS_PERFORMANCE_ANALYTICS", "SPMS_PERFORMANCE_ANALYTICS", actor,
                analyticsAudit(query, cohort, false, breakdowns.size()), correlationId);
        return result;
    }

    @Override
    public OutcomePage outcomes(String agencyId, OutcomeQuery query, String actor, String correlationId) {
        validate(query);
        List<Snapshot> rows = snapshots(agencyId, false).stream()
                .filter(value -> value.changedAt().isAfter(query.changedAfter())
                        && !value.changedAt().isAfter(query.changedThrough()))
                .sorted(Comparator.comparing(Snapshot::changedAt).thenComparing(value -> value.rating().getId()))
                .toList();
        List<Snapshot> page = page(rows, query.page(), query.size());
        Instant generatedAt = Instant.now();
        OutcomePage result = new OutcomePage(SCHEMA_VERSION, query.changedAfter(), query.changedThrough(),
                generatedAt, query.page(), query.size(), rows.size(), pages(rows.size(), query.size()),
                page.stream().map(this::outcome).toList());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("requestedBy", actor);
        metadata.put("schemaVersion", SCHEMA_VERSION);
        metadata.put("changedAfter", query.changedAfter().toString());
        metadata.put("changedThrough", query.changedThrough().toString());
        metadata.put("page", query.page());
        metadata.put("size", query.size());
        metadata.put("resultCount", result.content().size());
        audit(agencyId, "PULL_SPMS_PERFORMANCE_OUTCOMES", "SPMS_PERFORMANCE_OUTCOME_FEED", actor,
                metadata, correlationId);
        return result;
    }

    private List<Snapshot> filtered(String agencyId, RegisterQuery query) {
        return snapshots(agencyId, query.includeHistory()).stream()
                .filter(value -> {
                    LocalDate finalized = value.rating().getFinalizedAt().atZone(ZoneId.of(TIMEZONE)).toLocalDate();
                    return !finalized.isBefore(query.from()) && !finalized.isAfter(query.to());
                })
                .filter(value -> blank(query.cycleId()) || same(query.cycleId(), value.ratingCase().getCycleId()))
                .filter(value -> blank(query.formType()) || same(query.formType(), value.ratingCase().getFormType()))
                .filter(value -> query.businessUnitId() == null
                        || query.businessUnitId().equals(value.ratingCase().getBusinessUnitId()))
                .filter(value -> blank(query.employeeNo())
                        || contains(value.ratingCase().getOwnerEmployeeNo(), query.employeeNo()))
                .filter(value -> blank(query.ratingLabel())
                        || same(query.ratingLabel(), value.rating().getRatingLabel()))
                .toList();
    }

    private List<Snapshot> snapshots(String agencyId, boolean includeHistory) {
        List<PerformanceFinalRating> ratings = finalRatings.findByAgencyIdOrderByFinalizedAtDesc(agencyId);
        if (ratings.size() > MAX_SCAN) {
            throw new IllegalArgumentException("Performance outcome cohort exceeds 10000 rows; narrow the filters");
        }
        Map<String, PerformanceFinalRating> latest = ratings.stream().collect(Collectors.toMap(
                PerformanceFinalRating::getRatingCaseId, Function.identity(),
                (left, right) -> left.getRevisionNo() >= right.getRevisionNo() ? left : right));
        Map<String, PerformanceRatingCase> cases = ratingCases.findByAgencyIdOrderByOpenedAtDesc(agencyId).stream()
                .collect(Collectors.toMap(PerformanceRatingCase::getId, Function.identity()));
        Map<String, PerformanceCommitmentVersion> versions = commitments.findByAgencyIdOrderByCreatedAtDesc(agencyId)
                .stream().collect(Collectors.toMap(PerformanceCommitmentVersion::getId, Function.identity()));
        Map<String, PerformanceRatingAcknowledgment> ackByRating = acknowledgments.findByAgencyId(agencyId).stream()
                .collect(Collectors.toMap(PerformanceRatingAcknowledgment::getFinalRatingId, Function.identity()));
        Map<String, PerformanceRatingAppeal> appealByRating = appeals.findByAgencyId(agencyId).stream()
                .collect(Collectors.toMap(PerformanceRatingAppeal::getFinalRatingId, Function.identity()));
        Map<String, PerformanceIntervention> interventionByRating = interventions.findByAgencyId(agencyId).stream()
                .collect(Collectors.toMap(PerformanceIntervention::getFinalRatingId, Function.identity()));
        List<Snapshot> result = new ArrayList<>();
        for (PerformanceFinalRating rating : ratings) {
            boolean current = latest.get(rating.getRatingCaseId()).getId().equals(rating.getId());
            if (!includeHistory && !current) continue;
            PerformanceRatingCase ratingCase = required(cases.get(rating.getRatingCaseId()), "rating case", rating.getId());
            PerformanceCommitmentVersion commitment = required(versions.get(ratingCase.getCommitmentVersionId()),
                    "commitment version", rating.getId());
            PerformanceRatingAcknowledgment acknowledgment = ackByRating.get(rating.getId());
            PerformanceRatingAppeal appeal = appealByRating.get(rating.getId());
            PerformanceIntervention intervention = interventionByRating.get(rating.getId());
            Instant changedAt = latest(rating.getFinalizedAt(), updated(acknowledgment), updated(appeal),
                    updated(intervention));
            result.add(new Snapshot(rating, ratingCase, commitment, acknowledgment, appeal, intervention,
                    current, changedAt));
        }
        return result.stream().sorted(Comparator.comparing((Snapshot value) -> value.rating().getFinalizedAt())
                .reversed().thenComparing(value -> value.rating().getId())).toList();
    }

    private RegisterRow row(Snapshot value) {
        return new RegisterRow(value.rating().getId(), value.ratingCase().getId(), value.rating().getRevisionNo(),
                value.rating().getSupersedesId(), value.current(), value.ratingCase().getOwnerEmployeeNo(),
                value.ratingCase().getOwnerName(), value.ratingCase().getFormType(), value.ratingCase().getCycleId(),
                value.ratingCase().getBusinessUnitId(), value.commitment().getBusinessUnitName(),
                value.commitment().getPeriodStart(), value.commitment().getPeriodEnd(),
                value.rating().getRoundedScore(), value.rating().getRatingLabel(), value.rating().getFinalizedAt(),
                status(value.acknowledgment()), status(value.appeal()), action(value.intervention()),
                status(value.intervention()), value.rating().getCalculationFingerprint());
    }

    private Outcome outcome(Snapshot value) {
        String acknowledgment = status(value.acknowledgment());
        String appeal = status(value.appeal());
        String interventionAction = action(value.intervention());
        String interventionStatus = status(value.intervention());
        String successor = value.appeal() == null ? null : value.appeal().getSuccessorFinalRatingId();
        String fingerprint = hash(value.rating().getId(), value.ratingCase().getId(),
                String.valueOf(value.rating().getRevisionNo()), Objects.toString(value.rating().getSupersedesId(), ""),
                value.ratingCase().getOwnerEmployeeNo(), value.ratingCase().getCycleId(),
                value.ratingCase().getFormType(), String.valueOf(value.ratingCase().getBusinessUnitId()),
                value.rating().getRoundedScore().toPlainString(), value.rating().getRatingLabel(),
                value.rating().getFinalizedAt().toString(), acknowledgment, appeal,
                Objects.toString(successor, ""), interventionAction, interventionStatus,
                value.rating().getCalculationFingerprint(), String.valueOf(value.rating().getVersion()),
                value.changedAt().toString());
        return new Outcome(value.rating().getId(), value.ratingCase().getId(), value.rating().getRevisionNo(),
                value.rating().getSupersedesId(), value.ratingCase().getOwnerEmployeeNo(),
                value.ratingCase().getCycleId(), value.ratingCase().getFormType(),
                value.ratingCase().getBusinessUnitId(), value.commitment().getPeriodStart(),
                value.commitment().getPeriodEnd(), value.rating().getRoundedScore(),
                value.rating().getRatingLabel(), value.rating().getFinalizedAt(), acknowledgment, appeal,
                successor, interventionAction, interventionStatus, value.rating().getCalculationFingerprint(),
                value.rating().getVersion(), value.changedAt(), fingerprint);
    }

    private void addBreakdowns(List<AggregateBreakdown> target, String dimension, List<Snapshot> rows,
            Function<Snapshot, String> key, Function<Snapshot, String> label) {
        Map<String, List<Snapshot>> grouped = rows.stream().collect(Collectors.groupingBy(key, TreeMap::new,
                Collectors.toList()));
        grouped.forEach((groupKey, values) -> {
            long employees = values.stream().map(value -> value.ratingCase().getOwnerEmployeeNo().toUpperCase(Locale.ROOT))
                    .distinct().count();
            if (employees >= minimumAggregateCohort) {
                target.add(new AggregateBreakdown(dimension, groupKey, label.apply(values.get(0)), values.size(),
                        mean(values)));
            }
        });
    }

    private static AggregateMetric count(String code, String label, long value, String definition) {
        return new AggregateMetric(code, label, value, null, BigDecimal.valueOf(value), "COUNT", definition);
    }

    private static AggregateMetric ratio(String code, String label, long numerator, long denominator,
            String definition) {
        BigDecimal value = denominator == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        return new AggregateMetric(code, label, numerator, denominator, value, "PERCENT", definition);
    }

    private static AggregateMetric average(String code, String label, List<Snapshot> rows, String definition) {
        return new AggregateMetric(code, label, rows.size(), null, mean(rows), "SCORE", definition);
    }

    private static BigDecimal mean(List<Snapshot> rows) {
        return rows.stream().map(value -> value.rating().getRoundedScore()).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rows.size()), 4, RoundingMode.HALF_UP);
    }

    private static void validate(RegisterQuery query) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(query.from(), "from");
        Objects.requireNonNull(query.to(), "to");
        if (query.to().isBefore(query.from())) throw new IllegalArgumentException("to must not be before from");
        if (query.from().plusDays(MAX_RANGE_DAYS).isBefore(query.to())) {
            throw new IllegalArgumentException("Date range must not exceed 366 days");
        }
        validatePage(query.page(), query.size());
        if (query.businessUnitId() != null && query.businessUnitId() < 1) {
            throw new IllegalArgumentException("businessUnitId must be positive");
        }
        if (!blank(query.formType()) && !Set.of("OPCR", "DPCR", "IPCR").contains(query.formType().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("formType must be OPCR, DPCR, or IPCR");
        }
    }

    private static void validate(OutcomeQuery query) {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(query.changedAfter(), "changedAfter");
        Objects.requireNonNull(query.changedThrough(), "changedThrough");
        if (!query.changedThrough().isAfter(query.changedAfter())) {
            throw new IllegalArgumentException("changedThrough must be after changedAfter");
        }
        validatePage(query.page(), query.size());
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || page > 100_000) throw new IllegalArgumentException("page must be between 0 and 100000");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
    }

    private static <T> List<T> page(List<T> values, int page, int size) {
        long offset = (long) page * size;
        if (offset >= values.size()) return List.of();
        int start = (int) offset;
        return values.subList(start, Math.min(start + size, values.size()));
    }

    private static int pages(int count, int size) {
        return (int) Math.ceil(count / (double) size);
    }

    private static Map<String, Object> registerAudit(RegisterQuery query, int resultCount) {
        Map<String, Object> value = baseAudit(query);
        value.put("page", query.page());
        value.put("size", query.size());
        value.put("resultCount", resultCount);
        return value;
    }

    private static Map<String, Object> analyticsAudit(RegisterQuery query, int cohort, boolean suppressed,
            int breakdownCount) {
        Map<String, Object> value = baseAudit(query);
        value.put("cohortSize", cohort);
        value.put("suppressed", suppressed);
        value.put("breakdownCount", breakdownCount);
        return value;
    }

    private static Map<String, Object> baseAudit(RegisterQuery query) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("from", query.from().toString());
        value.put("to", query.to().toString());
        value.put("includeHistory", query.includeHistory());
        value.put("cycleId", Objects.toString(query.cycleId(), ""));
        value.put("formType", Objects.toString(query.formType(), ""));
        value.put("businessUnitId", Objects.toString(query.businessUnitId(), ""));
        value.put("employeeFilterApplied", !blank(query.employeeNo()));
        value.put("ratingLabel", Objects.toString(query.ratingLabel(), ""));
        return value;
    }

    private void audit(String agency, String action, String aggregate, String actor,
            Map<String, Object> metadata, String correlation) {
        audit.record(agency, action, aggregate, agency, null, null, null, metadata, null, correlation);
    }

    private static String status(PerformanceRatingAcknowledgment value) {
        return value == null ? "NOT_RECORDED" : value.getStatus().name();
    }

    private static String status(PerformanceRatingAppeal value) {
        return value == null ? "NOT_FILED" : value.getStatus().name();
    }

    private static String status(PerformanceIntervention value) {
        return value == null ? "NOT_APPLICABLE" : value.getStatus().name();
    }

    private static String action(PerformanceIntervention value) {
        return value == null ? PerformanceInterventionRule.Action.NONE.name() : value.getAction().name();
    }

    private static Instant updated(com.primehr.rsp.domain.RspAuditedEntity value) {
        return value == null ? null : value.getUpdatedAt();
    }

    private static Instant latest(Instant... values) {
        return Arrays.stream(values).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElseThrow();
    }

    private static <T> T required(T value, String type, String ratingId) {
        if (value == null) throw new ApplicationConflictException("Final rating " + ratingId + " has no " + type);
        return value;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean same(String left, String right) {
        return right != null && right.equalsIgnoreCase(left.trim());
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT));
    }

    private static String hash(String... values) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\u001f", values).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record Snapshot(PerformanceFinalRating rating, PerformanceRatingCase ratingCase,
                            PerformanceCommitmentVersion commitment,
                            PerformanceRatingAcknowledgment acknowledgment,
                            PerformanceRatingAppeal appeal, PerformanceIntervention intervention,
                            boolean current, Instant changedAt) { }
}
