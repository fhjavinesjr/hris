package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.AnnualizationEmployeeDTO;
import com.payroll.dtos.AnnualizationPeriodDTO;
import com.payroll.dtos.AnnualizationYearDTO;
import com.payroll.dtos.AnnualizationAdjustmentPeriodDTO;
import com.payroll.dtos.PayrollAdjustmentDTO;
import com.payroll.dtos.PreviousEmployerTaxDataDTO;
import com.payroll.dtos.AnnualizationReadinessDTO;
import com.payroll.dtos.AnnualizationTaxIdentityDTO;
import com.payroll.dtos.Bir2316CompensationItemsDTO;
import com.payroll.dtos.PeraTaxCreditDTO;
import com.payroll.dtos.AnnualizationAlphalistEmployeeDTO;
import com.payroll.dtos.AnnualizationAlphalistPreviewDTO;
import com.payroll.dtos.AnnualizationDatExportDTO;
import com.payroll.dtos.AnnualBenefitReviewLineDTO;
import com.payroll.dtos.AnnualBenefitReviewRequest;
import com.payroll.dtos.AnnualizationRefundVoucherDTO;
import com.payroll.dtos.AnnualizationRefundVoucherRequest;
import com.payroll.dtos.SeparationFinalPayReviewDTO;
import com.payroll.entitymodels.AnnualPeraTaxCredit;
import com.payroll.entitymodels.PayrollAdjustmentLine;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.entitymodels.PayrollDetailDeduction;
import com.payroll.entitymodels.PayrollDetailEarning;
import com.payroll.entitymodels.PayrollStatus;
import com.payroll.entitymodels.PreviousEmployerTaxData;
import com.payroll.entitymodels.AnnualizationAuditLog;
import com.payroll.entitymodels.AnnualizationSnapshot;
import com.payroll.entitymodels.AnnualizationSnapshotPeriod;
import com.payroll.entitymodels.AnnualizationSnapshotPreviousEmployer;
import com.payroll.entitymodels.AnnualizationSnapshotItem;
import com.payroll.entitymodels.AnnualBenefitReview;
import com.payroll.entitymodels.AnnualizationRefundVoucher;
import com.payroll.repositories.PayrollAdjustmentLineRepository;
import com.payroll.repositories.PayrollDetailDeductionRepository;
import com.payroll.repositories.PayrollDetailEarningRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.repositories.PreviousEmployerTaxDataRepository;
import com.payroll.repositories.AnnualizationAuditLogRepository;
import com.payroll.repositories.AnnualizationSnapshotPeriodRepository;
import com.payroll.repositories.AnnualizationSnapshotPreviousEmployerRepository;
import com.payroll.repositories.AnnualizationSnapshotRepository;
import com.payroll.repositories.AnnualizationSnapshotItemRepository;
import com.payroll.repositories.PayrollPeriodLockRepository;
import com.payroll.repositories.AnnualPeraTaxCreditRepository;
import com.payroll.repositories.AnnualBenefitReviewRepository;
import com.payroll.repositories.AnnualizationRefundVoucherRepository;
import com.payroll.services.AnnualizationService;
import com.payroll.services.PayrollAdjustmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AnnualizationServiceImpl implements AnnualizationService {
    private static final Set<String> BIR_EMPLOYMENT_STATUS_CODES =
            Set.of("R", "C", "CP", "S", "P", "AL");
    private static final Set<String> BIR_SEPARATION_REASON_CODES = Set.of("T", "TR", "R", "D");
    private static final Set<String> BIR_REGION_CODES = Set.of(
            "I", "II", "III", "IV-A", "IV-B", "V", "VI", "VII", "VIII",
            "IX", "X", "XI", "XII", "XIII", "NCR", "ARMM", "CAR",
            "1", "2", "3", "4-A", "4-B", "5", "6", "7", "8", "9",
            "10", "11", "12", "13");

    private static final Logger log = LoggerFactory.getLogger(AnnualizationServiceImpl.class);
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal PERA_TAX_CREDIT_RATE = new BigDecimal("0.05");

    private final PayrollDetailRepository detailRepository;
    private final PayrollDetailEarningRepository earningRepository;
    private final PayrollDetailDeductionRepository deductionRepository;
    private final PayrollAdjustmentLineRepository adjustmentLineRepository;
    private final PreviousEmployerTaxDataRepository previousEmployerRepository;
    private final AnnualizationSnapshotRepository snapshotRepository;
    private final AnnualizationSnapshotPeriodRepository snapshotPeriodRepository;
    private final AnnualizationSnapshotPreviousEmployerRepository snapshotPreviousRepository;
    private final AnnualizationSnapshotItemRepository snapshotItemRepository;
    private final AnnualizationAuditLogRepository auditLogRepository;
    private final AnnualPeraTaxCreditRepository peraTaxCreditRepository;
    private final PayrollPeriodLockRepository periodLockRepository;
    private final PayrollAdjustmentService payrollAdjustmentService;
    private final RestTemplate restTemplate;
    private final SystemConfigRuntimeResolver systemConfigResolver;
    private final Bir1604CDatWriter bir1604CDatWriter;
    private final AnnualBenefitReviewRepository benefitReviewRepository;
    private final AnnualizationRefundVoucherRepository refundVoucherRepository;

    @Value("${hris.services.administrative.url:http://localhost:8082}")
    private String administrativeUrl;

    @Value("${hris.services.hrmanagement.url:http://localhost:8085}")
    private String humanResourceUrl;

    public AnnualizationServiceImpl(
            PayrollDetailRepository detailRepository,
            PayrollDetailEarningRepository earningRepository,
            PayrollDetailDeductionRepository deductionRepository,
            PayrollAdjustmentLineRepository adjustmentLineRepository,
            PreviousEmployerTaxDataRepository previousEmployerRepository,
            AnnualizationSnapshotRepository snapshotRepository,
            AnnualizationSnapshotPeriodRepository snapshotPeriodRepository,
            AnnualizationSnapshotPreviousEmployerRepository snapshotPreviousRepository,
            AnnualizationSnapshotItemRepository snapshotItemRepository,
            AnnualizationAuditLogRepository auditLogRepository,
            AnnualPeraTaxCreditRepository peraTaxCreditRepository,
            PayrollPeriodLockRepository periodLockRepository,
            PayrollAdjustmentService payrollAdjustmentService,
            @Qualifier("payrollRestTemplate") RestTemplate restTemplate,
            SystemConfigRuntimeResolver systemConfigResolver,
            Bir1604CDatWriter bir1604CDatWriter,
            AnnualBenefitReviewRepository benefitReviewRepository,
            AnnualizationRefundVoucherRepository refundVoucherRepository) {
        this.detailRepository = detailRepository;
        this.earningRepository = earningRepository;
        this.deductionRepository = deductionRepository;
        this.adjustmentLineRepository = adjustmentLineRepository;
        this.previousEmployerRepository = previousEmployerRepository;
        this.snapshotRepository = snapshotRepository;
        this.snapshotPeriodRepository = snapshotPeriodRepository;
        this.snapshotPreviousRepository = snapshotPreviousRepository;
        this.snapshotItemRepository = snapshotItemRepository;
        this.auditLogRepository = auditLogRepository;
        this.peraTaxCreditRepository = peraTaxCreditRepository;
        this.periodLockRepository = periodLockRepository;
        this.payrollAdjustmentService = payrollAdjustmentService;
        this.restTemplate = restTemplate;
        this.systemConfigResolver = systemConfigResolver;
        this.bir1604CDatWriter = bir1604CDatWriter;
        this.benefitReviewRepository = benefitReviewRepository;
        this.refundVoucherRepository = refundVoucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AnnualizationYearDTO getAnnualization(int taxYear, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        Computation computation = compute(taxYear, authorizationHeader);
        Map<String, AnnualizationEmployeeDTO> summariesByEmployee = new LinkedHashMap<>();
        computation.records().forEach(record -> summariesByEmployee.put(
                record.employeeNo().toUpperCase(Locale.ROOT), withoutPeriods(record)));
        for (AnnualizationSnapshot snapshot
                : snapshotRepository.findByTaxYearAndStatusOrderByEmployeeNameAsc(taxYear, "FINALIZED")) {
            summariesByEmployee.put(snapshot.getEmployeeNo().toUpperCase(Locale.ROOT),
                    fromSnapshot(snapshot, false));
        }
        List<AnnualizationEmployeeDTO> summaries = new ArrayList<>(summariesByEmployee.values());
        summaries.sort(Comparator.comparing(AnnualizationEmployeeDTO::employeeName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        int computed = (int) summaries.stream().filter(r -> "COMPUTED".equals(r.status())).count();
        int finalized = (int) summaries.stream().filter(r -> "FINALIZED".equals(r.status())).count();
        int forReview = (int) summaries.stream().filter(r -> "FOR_REVIEW".equals(r.status())).count();
        return new AnnualizationYearDTO(
                taxYear,
                computation.taxTableConfigured(),
                summaries.size(),
                computed,
                finalized,
                forReview,
                summaries);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnualBenefitReviewLineDTO> getBenefitReviewLines(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        String requested = requireEmployeeNo(employeeNo);
        BenefitContext context = loadBenefitContext(taxYear, authorizationHeader);
        Map<String, AnnualBenefitReview> reviews = new LinkedHashMap<>();
        benefitReviewRepository.findByTaxYear(taxYear).forEach(value ->
                reviews.put(reviewKey(value.getSourceType(), value.getSourceId()), value));
        return benefitSources(taxYear, requested).stream()
                .map(source -> reviewLine(source, reviews.get(reviewKey(source.type(), source.id())), context))
                .toList();
    }

    @Override
    @Transactional
    public AnnualBenefitReviewLineDTO saveBenefitReview(
            int taxYear, String employeeNo, String sourceType, Long sourceId,
            AnnualBenefitReviewRequest request, String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.EDIT);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        if (request == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Benefit review details are required");
        BenefitSource source = benefitSources(taxYear, requested).stream()
                .filter(value -> value.type().equals(sourceType) && value.id().equals(sourceId))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Finalized earning line was not found for this employee and tax year"));
        BenefitContext context = loadBenefitContext(taxYear, authorizationHeader);
        String profileCode = request.profileCode() == null ? "" : request.profileCode().trim().toUpperCase(Locale.ROOT);
        BenefitTaxProfileSource profile = context.profiles().get(profileCode);
        if (profile == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Select a configured benefit tax profile for this tax year");
        String suggested = context.profileFor(source.code());
        if (suggested != null && !suggested.equalsIgnoreCase(profileCode))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This earning is assigned to a different benefit tax profile in Administrative");
        BigDecimal exempt = request.exemptAmount();
        BigDecimal otherExempt = request.otherExemptAmount() == null ? ZERO : request.otherExemptAmount();
        BigDecimal taxable = request.taxableAmount();
        String exemptItem = normalizeBirItemNo(request.exemptBirItem());
        String otherExemptItem = normalizeBirItemNo(request.otherExemptBirItem());
        String taxableItem = normalizeBirItemNo(request.taxableBirItem());
        if (!validReviewAmounts(exempt, otherExempt, taxable, source.amount())
                || !validReviewItems(exempt, exemptItem, otherExempt, otherExemptItem,
                        taxable, taxableItem, profile))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Benefit amounts must reconcile to the finalized earning and use valid BIR items");
        String reason = cleanRemarks(request.reason(), "Benefit review reason");
        if (reason == null || reason.length() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A review reason of at most 500 characters is required");
        String actor = requireActor(performedBy);
        AnnualBenefitReview review = benefitReviewRepository
                .findBySourceTypeAndSourceId(sourceType, sourceId).orElseGet(AnnualBenefitReview::new);
        if (review.getId() != null && (!requested.equalsIgnoreCase(review.getEmployeeNo())
                || review.getTaxYear() != taxYear))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Existing review belongs to another employee or tax year");
        String old = review.getId() == null ? "none" : review.getExemptAmount() + "/" + review.getTaxableAmount();
        review.setSourceType(sourceType);
        review.setSourceId(sourceId);
        review.setEmployeeNo(requested);
        review.setTaxYear(taxYear);
        review.setSourceAmount(source.amount());
        review.setExemptAmount(exempt);
        review.setOtherExemptAmount(otherExempt);
        review.setTaxableAmount(taxable);
        review.setExemptBirItem(exempt.signum() == 0 ? null : exemptItem);
        review.setOtherExemptBirItem(otherExempt.signum() == 0 ? null : otherExemptItem);
        review.setTaxableBirItem(taxable.signum() == 0 ? null : taxableItem);
        review.setProfileCode(profileCode);
        review.setProfileLegalBasis(profile.legalBasis());
        review.setProfileGuidanceCeiling(profile.guidanceCeiling());
        review.setProfileCeilingPeriod(profile.ceilingPeriod());
        review.setReason(reason);
        review.setReviewedBy(actor);
        review.setReviewedAt(LocalDateTime.now());
        benefitReviewRepository.save(review);
        String auditDetails = sourceType + ":" + sourceId + " " + old + " -> " + exempt + "/" + otherExempt
                + "/" + taxable + " BIR " + exemptItem + "/" + otherExemptItem + "/" + taxableItem + "; " + reason;
        saveAudit(requested, taxYear, "BENEFIT_CLASSIFICATION_REVIEWED", "COMPUTED", "COMPUTED",
                auditDetails.substring(0, Math.min(auditDetails.length(), 500)), actor);
        return reviewLine(source, review, context);
    }

    private List<BenefitSource> benefitSources(int year, String employeeNo) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<BenefitSource> result = new ArrayList<>();
        for (PayrollDetailEarning earning : earningRepository.findFinalizedForAnnualization(
                start, end, PayrollStatus.CANCELLED)) {
            PayrollDetail detail = earning.getPayrollDetail();
            if (!employeeNo.equalsIgnoreCase(detail.getEmployeeNo()) || "BASIC".equalsIgnoreCase(earning.getEarningCode())) continue;
            result.add(new BenefitSource("PAYROLL_EARNING", earning.getId(), earning.getEarningCode(),
                    earning.getEarningName(), detail.getSalaryPeriodKey(), detail.getSalaryDate(),
                    money(earning.getAmount()), earning.getBir2316ItemNo(), earning.getIsTaxable()));
        }
        for (PayrollAdjustmentLine line : adjustmentLineRepository.findPostedForAnnualization(
                start, end, PayrollStatus.CANCELLED)) {
            if (!"EARNING".equalsIgnoreCase(line.getType())
                    || !employeeNo.equalsIgnoreCase(line.getHeader().getEmployeeNo())) continue;
            String periodKey = line.getHeader().getSalaryPeriodKey();
            LocalDate date = detailRepository.findByEmployeeNoAndSalaryPeriodKey(employeeNo, periodKey)
                    .map(PayrollDetail::getSalaryDate).orElse(null);
            result.add(new BenefitSource("POSTED_ADJUSTMENT", line.getId(), line.getCode(),
                    line.getName(), periodKey, date, money(line.getAmount()),
                    line.getBir2316ItemNo(), line.getIsTaxable()));
        }
        result.sort(Comparator.comparing(BenefitSource::date, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(BenefitSource::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return result;
    }

    private AnnualBenefitReviewLineDTO reviewLine(BenefitSource source, AnnualBenefitReview review,
            BenefitContext context) {
        String suggested = context.profileFor(source.code());
        boolean stale = review != null && (review.getSourceAmount().compareTo(source.amount()) != 0
                || !context.profiles().containsKey(review.getProfileCode())
                || context.profiles().containsKey(review.getProfileCode())
                && !sameProfile(review, context.profiles().get(review.getProfileCode()))
                || suggested != null && !suggested.equalsIgnoreCase(review.getProfileCode()));
        return new AnnualBenefitReviewLineDTO(source.type(), source.id(), source.code(), source.name(),
                source.periodKey(), source.date(), source.amount(), source.birItem(), source.taxable(),
                suggested, review == null ? null : review.getProfileCode(),
                review == null ? null : review.getExemptAmount(),
                review == null ? null : review.getExemptBirItem(),
                review == null ? null : review.getOtherExemptAmount(),
                review == null ? null : review.getOtherExemptBirItem(),
                review == null ? null : review.getTaxableAmount(),
                review == null ? null : review.getTaxableBirItem(),
                review == null ? null : review.getReason(),
                review == null ? null : review.getReviewedBy(),
                review == null ? null : review.getReviewedAt(), stale);
    }

    private record BenefitSource(String type, Long id, String code, String name, String periodKey,
            LocalDate date, BigDecimal amount, String birItem, Boolean taxable) {}

    @Override
    @Transactional(readOnly = true)
    public AnnualizationAlphalistPreviewDTO getAlphalistPreview(
            int taxYear, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        List<AnnualizationSnapshot> snapshots = snapshotRepository
                .findByTaxYearAndStatusOrderByEmployeeNameAsc(taxYear, "FINALIZED");
        List<Long> snapshotIds = snapshots.stream().map(AnnualizationSnapshot::getId).toList();
        Map<Long, List<AnnualizationSnapshotPeriod>> periodsBySnapshot = new LinkedHashMap<>();
        if (!snapshotIds.isEmpty()) {
            for (AnnualizationSnapshotPeriod period : snapshotPeriodRepository
                    .findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(snapshotIds)) {
                periodsBySnapshot.computeIfAbsent(period.getSnapshotId(), ignored -> new ArrayList<>())
                        .add(period);
            }
        }

        List<String> datasetIssues = alphalistDatasetIssues(snapshots);
        List<AnnualizationAlphalistEmployeeDTO> records = new ArrayList<>();
        int sequence = 1;
        for (AnnualizationSnapshot snapshot : snapshots) {
            records.add(toAlphalistEmployee(
                    sequence++, snapshot,
                    periodsBySnapshot.getOrDefault(snapshot.getId(), List.of())));
        }
        AnnualizationSnapshot identitySource = snapshots.isEmpty() ? null : snapshots.get(0);
        int validEmployees = (int) records.stream().filter(AnnualizationAlphalistEmployeeDTO::valid).count();
        int qualified = (int) records.stream()
                .filter(record -> Boolean.TRUE.equals(record.substitutedFilingQualified())).count();
        int notQualified = (int) records.stream()
                .filter(record -> Boolean.FALSE.equals(record.substitutedFilingQualified())).count();
        int notAssessed = records.size() - qualified - notQualified;
        return new AnnualizationAlphalistPreviewDTO(
                taxYear,
                "BIR Alphalist Data Entry and Validation Module v7.4 / Form 1604-C review preview",
                datasetIssues.isEmpty() && validEmployees == records.size() && !records.isEmpty(),
                identitySource == null ? null : identitySource.getEmployerName(),
                identitySource == null ? null : identitySource.getEmployerTin(),
                identitySource == null ? null : identitySource.getEmployerBirBranchCode(),
                identitySource == null ? null : identitySource.getEmployerRdoCode(),
                records.size(), validEmployees, records.size() - validEmployees,
                qualified, notQualified, notAssessed,
                sumAlphalist(records, AnnualizationAlphalistEmployeeDTO::grossCompensation),
                sumAlphalist(records, AnnualizationAlphalistEmployeeDTO::taxableCompensation),
                sumAlphalist(records, AnnualizationAlphalistEmployeeDTO::annualTaxDue),
                sumAlphalist(records, AnnualizationAlphalistEmployeeDTO::totalTaxesWithheld),
                List.copyOf(datasetIssues),
                List.of(
                        "Export is enabled only when every finalized employee passes the BIR v7.4 preflight checks.",
                        "This DAT is the employee alphalist attachment, not the employer's BIR Form 1604-C. Review the separate 1601-C remittance ledger for monthly filing and payment evidence.",
                        "A calculated year-end adjustment is reported only after a matching posted adjustment in a locked payroll period, or a paid refund voucher is recorded.",
                        "Employees with previous-employer compensation remain blocked until their BIR compensation breakdown is recorded.",
                        "For separation before December, complete the last-pay annualization and map the HR separation nature to a BIR reason before export."),
                List.copyOf(records));
    }

    @Override
    @Transactional
    public AnnualizationDatExportDTO exportAlphalist(
            int taxYear, String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        AnnualizationAlphalistPreviewDTO preview = getAlphalistPreview(taxYear, authorizationHeader);
        if (!preview.exportEnabled()) {
            List<String> issues = new ArrayList<>(preview.datasetIssues());
            preview.records().stream().filter(record -> !record.valid()).forEach(record ->
                    issues.add(record.employeeNo() + ": " + String.join(" ", record.issues())));
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "BIR export is blocked until all preview issues are resolved. "
                            + String.join("; ", issues));
        }

        List<AnnualizationSnapshot> snapshots = snapshotRepository
                .findByTaxYearAndStatusOrderByEmployeeNameAsc(taxYear, "FINALIZED");
        Map<String, Map<String, BigDecimal>> itemsByEmployee = new LinkedHashMap<>();
        Map<String, List<AnnualizationSnapshotPeriod>> periodsByEmployee = new LinkedHashMap<>();
        for (AnnualizationSnapshot snapshot : snapshots) {
            Map<String, BigDecimal> items = new LinkedHashMap<>();
            for (AnnualizationSnapshotItem item
                    : snapshotItemRepository.findBySnapshotIdOrderByItemNoAsc(snapshot.getId())) {
                items.put(item.getItemNo(), item.getAmount());
            }
            if (items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "BIR compensation itemization is missing for " + snapshot.getEmployeeNo() + ".");
            }
            itemsByEmployee.put(snapshot.getEmployeeNo(), items);
            periodsByEmployee.put(snapshot.getEmployeeNo(), snapshotPeriodRepository
                    .findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(snapshot.getId()));
        }

        Bir1604CDatWriter.ExportFile file = bir1604CDatWriter.write(
                taxYear, preview.employerTin(), preview.employerBirBranchCode(),
                preview.records(), itemsByEmployee, periodsByEmployee);
        String actor = performedBy == null || performedBy.isBlank() ? "SYSTEM" : performedBy;
        for (AnnualizationSnapshot snapshot : snapshots) {
            saveAudit(snapshot.getEmployeeNo(), taxYear, "ALPHALIST_EXPORTED",
                    "FINALIZED", "FINALIZED", file.fileName(), actor);
        }
        return new AnnualizationDatExportDTO(file.fileName(), file.content());
    }

    private List<String> alphalistDatasetIssues(List<AnnualizationSnapshot> snapshots) {
        List<String> issues = new ArrayList<>();
        if (snapshots.isEmpty()) {
            issues.add("No finalized annualization snapshots were found for the selected tax year.");
            return issues;
        }
        AnnualizationSnapshot first = snapshots.get(0);
        if (isBlank(first.getEmployerName())) issues.add("Employer registered name is missing.");
        if (tinDigits(first.getEmployerTin()).length() != 9) issues.add("Employer TIN must contain exactly nine digits.");
        if (!isFourDigits(first.getEmployerBirBranchCode())) issues.add("Employer BIR branch code must contain four digits.");
        if (isBlank(first.getEmployerRdoCode())) issues.add("Employer RDO code is missing.");
        boolean inconsistentIdentity = snapshots.stream().anyMatch(snapshot ->
                !sameText(first.getEmployerName(), snapshot.getEmployerName())
                        || !sameText(first.getEmployerTin(), snapshot.getEmployerTin())
                        || !sameText(first.getEmployerBirBranchCode(), snapshot.getEmployerBirBranchCode())
                        || !sameText(first.getEmployerRdoCode(), snapshot.getEmployerRdoCode()));
        if (inconsistentIdentity) {
            issues.add("Finalized snapshots contain inconsistent employer identity values.");
        }
        return issues;
    }

    private AnnualizationAlphalistEmployeeDTO toAlphalistEmployee(
            int sequence, AnnualizationSnapshot snapshot,
            List<AnnualizationSnapshotPeriod> periods) {
        List<String> issues = new ArrayList<>();
        if (isBlank(snapshot.getEmployeeName())) issues.add("Employee name is missing.");
        if (isBlank(snapshot.getEmployeeLastName())) issues.add("Employee legal last name is missing.");
        if (isBlank(snapshot.getEmployeeFirstName())) issues.add("Employee legal first name is missing.");
        if (isBlank(snapshot.getEmployeeNationality())) issues.add("Employee nationality is missing.");
        validateDatText(snapshot.getEmployeeLastName(), 30, "Employee legal last name", issues);
        validateDatText(snapshot.getEmployeeFirstName(), 30, "Employee legal first name", issues);
        validateDatText(snapshot.getEmployeeMiddleName(), 30, "Employee legal middle name", issues);
        validateDatText(snapshot.getEmployeeNationality(), 30, "Employee nationality", issues);
        if (!BIR_EMPLOYMENT_STATUS_CODES.contains(snapshot.getEmployeeEmploymentStatusCode())) {
            issues.add("BIR employment status is missing or invalid.");
        }
        if (snapshot.getEmployeeEmploymentFrom() == null
                || snapshot.getEmployeeEmploymentTo() == null) {
            issues.add("Legal employment coverage is missing.");
        } else if (snapshot.getEmployeeEmploymentFrom()
                .isAfter(snapshot.getEmployeeEmploymentTo())) {
            issues.add("Legal employment coverage is invalid.");
        } else if (snapshot.getEmployeeEmploymentFrom().getYear() != snapshot.getTaxYear()
                || snapshot.getEmployeeEmploymentTo().getYear() != snapshot.getTaxYear()) {
            issues.add("Legal employment coverage must be within the selected tax year.");
        }
        if (tinDigits(snapshot.getEmployeeTin()).length() != 9) {
            issues.add("Employee TIN must contain exactly nine digits.");
        }
        if (!isFourDigits(snapshot.getEmployeeBirBranchCode())) {
            issues.add("Employee BIR branch code must contain four digits.");
        }
        if (!isBirRegionCode(snapshot.getEmployeeBirRegionCode())) {
            issues.add("Employee BIR region must be a valid BIR region code.");
        }
        LocalDate periodFrom = periods.stream().map(AnnualizationSnapshotPeriod::getSalaryDate)
                .filter(java.util.Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        LocalDate periodTo = periods.stream().map(AnnualizationSnapshotPeriod::getSalaryDate)
                .filter(java.util.Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        if (periodFrom == null || periodTo == null) {
            issues.add("Finalized payroll-period coverage is missing.");
        }
        boolean hasUnknownMweStatus = periods.stream()
                .anyMatch(period -> period.getMinimumWageEarner() == null);
        boolean hasMwePeriod = periods.stream()
                .anyMatch(period -> Boolean.TRUE.equals(period.getMinimumWageEarner()));
        boolean hasNonMwePeriod = periods.stream()
                .anyMatch(period -> Boolean.FALSE.equals(period.getMinimumWageEarner()));
        Integer birScheduleNo = hasMwePeriod && !hasNonMwePeriod ? Integer.valueOf(2)
                : !hasMwePeriod && hasNonMwePeriod ? Integer.valueOf(1) : null;
        if (hasUnknownMweStatus) {
            issues.add("BIR schedule cannot be resolved because an MWE status is missing.");
        }
        if (hasMwePeriod && hasNonMwePeriod) {
            issues.add("BIR schedule requires review because MWE status changes within the tax year.");
        }
        if (Integer.valueOf(2).equals(birScheduleNo) && !hasSingleCompleteMweRateBasis(periods)) {
            issues.add("Schedule 2 requires one complete and consistent minimum-wage rate basis for the tax year.");
        }
        String birRegionCode = snapshot.getEmployeeBirRegionCode();
        if (snapshot.getSubstitutedFilingQualified() == null) {
            issues.add("Substituted-filing assessment is missing.");
        }
        if (amountOrZero(snapshot.getPreviousGrossCompensation()).signum() > 0) {
            issues.add("Previous-employer BIR compensation breakdown is not yet recorded.");
        }
        boolean separated = snapshot.getEmployeeEmploymentTo() != null
                && snapshot.getEmployeeEmploymentTo().isBefore(LocalDate.of(snapshot.getTaxYear(), 12, 31));
        if (separated && (snapshot.getEmployeeBirSeparationReasonCode() == null
                || !BIR_SEPARATION_REASON_CODES.contains(
                        snapshot.getEmployeeBirSeparationReasonCode()))) {
            issues.add("Map the employee's HR separation nature to a BIR reason (T, TR, R, or D) and refinalize.");
        }
        BigDecimal taxDue = amountOrZero(snapshot.getAnnualTaxDue());
        BigDecimal previousWithheld = amountOrZero(snapshot.getPreviousTaxWithheld());
        BigDecimal presentWithheld = amountOrZero(snapshot.getPresentTaxWithheld());
        BigDecimal taxWithheld = scale(previousWithheld.add(presentWithheld));
        BigDecimal adjustment = settledAnnualizationAdjustment(snapshot);
        if (amountOrZero(snapshot.getAdjustmentAmount()).signum() != 0
                && adjustment.signum() == 0) {
            issues.add("Year-end adjustment is not posted in a locked payroll period or supported by a recorded paid refund voucher.");
        }
        BigDecimal decemberWithheld = adjustment.signum() > 0 ? adjustment : ZERO;
        BigDecimal refunded = adjustment.signum() < 0 ? adjustment.abs() : ZERO;
        BigDecimal actualWithheldAdjusted = scale(previousWithheld.add(presentWithheld)
                .add(amountOrZero(snapshot.getPeraTaxCredit()))
                .add(decemberWithheld).subtract(refunded));
        if (Boolean.TRUE.equals(snapshot.getSubstitutedFilingQualified())) {
            if (amountOrZero(snapshot.getPreviousGrossCompensation()).signum() > 0) {
                issues.add("Qualified substituted filing conflicts with previous-employer compensation.");
            }
            if (taxDue.compareTo(actualWithheldAdjusted) != 0) {
                issues.add("Qualified substituted filing conflicts with tax due and adjusted withholding.");
            }
        }
        if (actualWithheldAdjusted.compareTo(taxDue) != 0) {
            issues.add("Year-end withholding adjustment does not reconcile with annual tax due.");
        }
        return new AnnualizationAlphalistEmployeeDTO(
                sequence, snapshot.getEmployeeNo(), snapshot.getEmployeeName(), snapshot.getEmployeeTin(),
                snapshot.getEmployeeBirBranchCode(),
                snapshot.getEmployeeLastName(), snapshot.getEmployeeFirstName(),
                snapshot.getEmployeeMiddleName(), snapshot.getEmployeeExtensionName(),
                snapshot.getEmployeeNationality(), snapshot.getEmployeeEmploymentStatusCode(),
                snapshot.getEmployeeEmploymentStatusName(), birScheduleNo, birRegionCode,
                snapshot.getEmployeeEmploymentFrom(),
                snapshot.getEmployeeEmploymentTo(),
                snapshot.getEmployeeBirSeparationReasonCode(),
                periodFrom, periodTo, amountOrZero(snapshot.getGrossCompensation()),
                amountOrZero(snapshot.getNonTaxableCompensation()),
                amountOrZero(snapshot.getTaxableCompensation()), taxDue,
                taxWithheld, previousWithheld, presentWithheld,
                amountOrZero(snapshot.getPeraTaxCredit()), decemberWithheld, refunded,
                actualWithheldAdjusted, actualWithheldAdjusted,
                snapshot.getSubstitutedFilingQualified(),
                snapshot.getSubstitutedFilingReason(), issues.isEmpty(), List.copyOf(issues));
    }

    private BigDecimal settledAnnualizationAdjustment(AnnualizationSnapshot snapshot) {
        if (snapshot.getAdjustmentHeaderId() == null
                && amountOrZero(snapshot.getAdjustmentAmount()).signum() < 0) {
            AnnualizationRefundVoucher voucher = refundVoucherRepository
                    .findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(snapshot.getId())
                    .orElse(null);
            if (voucher != null && voucher.getAmount() != null
                    && voucher.getAmount().compareTo(snapshot.getAdjustmentAmount().abs()) == 0
                    && snapshotPayrollPeriodsLocked(snapshot.getId())) {
                return amountOrZero(snapshot.getAdjustmentAmount());
            }
        }
        if (snapshot.getAdjustmentHeaderId() == null
                || isBlank(snapshot.getAdjustmentSalaryPeriodKey())
                || !periodLockRepository.existsBySalaryPeriodKey(
                        snapshot.getAdjustmentSalaryPeriodKey())) return ZERO;
        return payrollAdjustmentService.findAdjustment(
                        snapshot.getEmployeeNo(), snapshot.getAdjustmentSalaryPeriodKey())
                .filter(header -> snapshot.getAdjustmentHeaderId().equals(header.getId()))
                .filter(header -> AnnualizationAdjustmentEvidence.matches(
                        snapshot.getAdjustmentHeaderId(), snapshot.getAdjustmentAmount(), header))
                .map(header -> amountOrZero(snapshot.getAdjustmentAmount()))
                .orElse(ZERO);
    }

    private boolean snapshotPayrollPeriodsLocked(Long snapshotId) {
        List<AnnualizationSnapshotPeriod> periods = snapshotPeriodRepository
                .findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(snapshotId);
        return !periods.isEmpty() && periods.stream().allMatch(period ->
                !isBlank(period.getSalaryPeriodKey())
                        && periodLockRepository.existsBySalaryPeriodKey(period.getSalaryPeriodKey()));
    }

    private BigDecimal sumAlphalist(
            List<AnnualizationAlphalistEmployeeDTO> records,
            java.util.function.Function<AnnualizationAlphalistEmployeeDTO, BigDecimal> mapper) {
        return scale(records.stream().map(mapper).reduce(ZERO, BigDecimal::add));
    }

    private BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? ZERO : scale(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String tinDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private boolean isFourDigits(String value) {
        return value != null && value.matches("\\d{4}");
    }

    private boolean isBirRegionCode(String value) {
        return value != null
                && BIR_REGION_CODES.contains(value.trim().toUpperCase(Locale.ROOT));
    }

    private void validateDatText(String value, int maxLength, String label, List<String> issues) {
        if (value == null || value.isBlank()) return;
        String clean = value.trim().replaceAll("[\\r\\n]", " ");
        if (clean.length() > maxLength) {
            issues.add(label + " exceeds the BIR " + maxLength + "-character limit.");
        }
        if (!StandardCharsets.US_ASCII.newEncoder().canEncode(clean)) {
            issues.add(label + " contains characters unsupported by the BIR DAT format.");
        }
    }

    private boolean hasSingleCompleteMweRateBasis(List<AnnualizationSnapshotPeriod> periods) {
        Set<String> bases = new LinkedHashSet<>();
        for (AnnualizationSnapshotPeriod period : periods) {
            if (!Boolean.TRUE.equals(period.getMinimumWageEarner())
                    || period.getMinimumWageDailyRate() == null
                    || period.getMinimumWageMonthlyRate() == null
                    || period.getMinimumWageDailyRate().signum() <= 0
                    || period.getMinimumWageMonthlyRate().signum() <= 0) {
                return false;
            }
            BigDecimal daily = scale(period.getMinimumWageDailyRate());
            BigDecimal monthly = scale(period.getMinimumWageMonthlyRate());
            BigDecimal factor = monthly.multiply(BigDecimal.valueOf(12))
                    .divide(daily, 0, RoundingMode.HALF_UP);
            bases.add(daily.toPlainString() + ":" + monthly.toPlainString()
                    + ":" + factor.toPlainString());
        }
        return !periods.isEmpty() && bases.size() == 1;
    }

    private String birEmploymentStatusName(String code) {
        if (code == null) return null;
        return switch (code) {
            case "R" -> "Regular";
            case "C" -> "Casual";
            case "CP" -> "Contractual / Project-Based";
            case "S" -> "Seasonal";
            case "P" -> "Probationary";
            case "AL" -> "Apprentice / Learner";
            default -> null;
        };
    }

    private boolean sameText(String left, String right) {
        String cleanLeft = left == null ? "" : left.trim();
        String cleanRight = right == null ? "" : right.trim();
        return cleanLeft.equalsIgnoreCase(cleanRight);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnualizationEmployeeDTO getEmployeeAnnualization(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        if (employeeNo == null || employeeNo.isBlank()) {
            throw new IllegalArgumentException("Employee number is required.");
        }
        String requested = employeeNo.trim();
        var finalized = snapshotRepository
                .findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                        requested, taxYear, "FINALIZED");
        if (finalized.isPresent()) return fromSnapshot(finalized.get(), true);
        return compute(taxYear, authorizationHeader).records().stream()
                .filter(record -> requested.equalsIgnoreCase(record.employeeNo()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No finalized payroll was found for employee " + requested
                                + " in tax year " + taxYear + "."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreviousEmployerTaxDataDTO> getPreviousEmployers(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        String requested = requireEmployeeNo(employeeNo);
        return previousEmployerRepository
                .findByEmployeeNoIgnoreCaseAndTaxYearOrderByEmploymentFromAsc(requested, taxYear)
                .stream().map(this::toPreviousEmployerDto).toList();
    }

    @Override
    @Transactional
    public PreviousEmployerTaxDataDTO createPreviousEmployer(
            int taxYear, String employeeNo, PreviousEmployerTaxDataDTO request,
            String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ADD);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        validatePreviousEmployer(request, taxYear);
        LocalDateTime now = LocalDateTime.now();
        String actor = requireActor(performedBy);
        PreviousEmployerTaxData entity = new PreviousEmployerTaxData();
        applyPreviousEmployer(entity, request, requested, taxYear);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor);
        entity.setUpdatedBy(actor);
        PreviousEmployerTaxData saved = previousEmployerRepository.save(entity);
        saveAudit(requested, taxYear, "PREVIOUS_EMPLOYER_ADDED", null,
                "COMPUTED", saved.getEmployerName(), actor);
        return toPreviousEmployerDto(saved);
    }

    @Override
    @Transactional
    public PreviousEmployerTaxDataDTO updatePreviousEmployer(
            int taxYear, String employeeNo, Long id, PreviousEmployerTaxDataDTO request,
            String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.EDIT);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        validatePreviousEmployer(request, taxYear);
        PreviousEmployerTaxData entity = previousEmployerRepository
                .findByIdAndEmployeeNoIgnoreCase(id, requested)
                .filter(value -> value.getTaxYear().equals(taxYear))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Previous-employer record was not found."));
        applyPreviousEmployer(entity, request, requested, taxYear);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(requireActor(performedBy));
        PreviousEmployerTaxData saved = previousEmployerRepository.save(entity);
        saveAudit(requested, taxYear, "PREVIOUS_EMPLOYER_UPDATED", "COMPUTED",
                "COMPUTED", saved.getEmployerName(), requireActor(performedBy));
        return toPreviousEmployerDto(saved);
    }

    @Override
    @Transactional
    public void deletePreviousEmployer(
            int taxYear, String employeeNo, Long id, String performedBy,
            String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.DELETE);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        PreviousEmployerTaxData entity = previousEmployerRepository
                .findByIdAndEmployeeNoIgnoreCase(id, requested)
                .filter(value -> value.getTaxYear().equals(taxYear))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Previous-employer record was not found."));
        previousEmployerRepository.delete(entity);
        saveAudit(requested, taxYear, "PREVIOUS_EMPLOYER_DELETED", "COMPUTED",
                "COMPUTED", entity.getEmployerName(), requireActor(performedBy));
    }

    @Override
    @Transactional(readOnly = true)
    public PeraTaxCreditDTO getPeraTaxCredit(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        return peraTaxCreditRepository.findByEmployeeNoIgnoreCaseAndTaxYear(
                requireEmployeeNo(employeeNo), taxYear).map(this::toPeraTaxCreditDto).orElse(null);
    }

    @Override
    @Transactional
    public PeraTaxCreditDTO savePeraTaxCredit(
            int taxYear, String employeeNo, PeraTaxCreditDTO request,
            String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        String requested = requireEmployeeNo(employeeNo);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        AnnualPeraTaxCredit existing = peraTaxCreditRepository
                .findByEmployeeNoIgnoreCaseAndTaxYear(requested, taxYear).orElse(null);
        requireAnnualizationPermission(authorizationHeader,
                existing == null ? PermissionAction.ADD : PermissionAction.EDIT);
        requireNotFinalized(requested, taxYear);
        validatePeraTaxCredit(request, taxYear);
        String actor = requireActor(performedBy);
        LocalDateTime now = LocalDateTime.now();
        AnnualPeraTaxCredit target = existing == null ? new AnnualPeraTaxCredit() : existing;
        target.setEmployeeNo(requested);
        target.setTaxYear(taxYear);
        target.setContributionAmount(scale(request.contributionAmount()));
        target.setCertifiedTaxCredit(scale(
                request.contributionAmount().multiply(PERA_TAX_CREDIT_RATE)));
        target.setCertificateNo(request.certificateNo().trim());
        target.setAdministratorName(request.administratorName().trim());
        target.setCertificateIssuedDate(request.certificateIssuedDate());
        if (existing == null) {
            target.setCreatedAt(now);
            target.setCreatedBy(actor);
        }
        target.setUpdatedAt(now);
        target.setUpdatedBy(actor);
        AnnualPeraTaxCredit saved = peraTaxCreditRepository.save(target);
        saveAudit(requested, taxYear,
                existing == null ? "PERA_TAX_CREDIT_ADDED" : "PERA_TAX_CREDIT_UPDATED",
                "COMPUTED", "COMPUTED", saved.getCertificateNo(), actor);
        return toPeraTaxCreditDto(saved);
    }

    @Override
    @Transactional
    public void deletePeraTaxCredit(
            int taxYear, String employeeNo, String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.DELETE);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        AnnualPeraTaxCredit target = peraTaxCreditRepository
                .findByEmployeeNoIgnoreCaseAndTaxYear(requested, taxYear)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "PERA tax-credit evidence was not found."));
        peraTaxCreditRepository.delete(target);
        saveAudit(requested, taxYear, "PERA_TAX_CREDIT_DELETED", "COMPUTED",
                "COMPUTED", target.getCertificateNo(), requireActor(performedBy));
    }

    @Override
    @Transactional
    public AnnualizationEmployeeDTO finalizeAnnualization(
            int taxYear, String employeeNo, String remarks,
            Boolean substitutedFilingQualified, String substitutedFilingReason,
            String performedBy,
            String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        AnnualizationEmployeeDTO computed = compute(taxYear, authorizationHeader).records().stream()
                .filter(record -> requested.equalsIgnoreCase(record.employeeNo()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No finalized payroll was found for employee " + requested
                                + " in tax year " + taxYear + "."));
        if (isSeparatedInYear(computed, taxYear)
                && detailRepository.findByEmployeeNoIgnoreCaseAndSalaryDateBetweenOrderBySalaryDateDesc(
                        requested, LocalDate.of(taxYear, 1, 1), LocalDate.of(taxYear, 12, 31))
                    .stream().anyMatch(detail -> detail.getStatus() != PayrollStatus.CANCELLED
                            && !periodLockRepository.existsBySalaryPeriodKey(detail.getSalaryPeriodKey()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Lock every computed salary period through the employee's last payment before finalizing separation annualization.");
        }
        if (isSeparatedInYear(computed, taxYear)
                && computed.adjustmentAmount() != null
                && computed.adjustmentAmount().signum() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The separation final payment has additional tax due. "
                            + "Post the final-pay adjustment before locking, then reload annualization. "
                            + "If final pay cannot cover additional tax, resolve the employer-advance exception first.");
        }
        if (!computed.readiness().ready()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Annualization is not ready: " + String.join("; ", computed.readiness().issues()));
        }
        String assessmentReason = validateSubstitutedFilingAssessment(
                computed, substitutedFilingQualified, substitutedFilingReason);
        int version = snapshotRepository
                .findFirstByEmployeeNoIgnoreCaseAndTaxYearOrderByVersionDesc(requested, taxYear)
                .map(value -> value.getVersion() + 1).orElse(1);
        String actor = requireActor(performedBy);
        String finalizationRemarks = cleanRemarks(remarks, "Finalization remarks");
        AnnualizationSnapshot snapshot = snapshotRepository.save(
                toSnapshot(computed, version, actor, finalizationRemarks,
                        substitutedFilingQualified, assessmentReason));
        snapshotPeriodRepository.saveAll(computed.periods().stream()
                .map(period -> toSnapshotPeriod(snapshot.getId(), period)).toList());
        snapshotPreviousRepository.saveAll(computed.previousEmployers().stream()
                .map(previous -> toSnapshotPrevious(snapshot.getId(), previous)).toList());
        snapshotItemRepository.saveAll(toSnapshotItems(snapshot.getId(), computed.bir2316Items()));
        saveAudit(requested, taxYear, "SUBSTITUTED_FILING_ASSESSED", computed.status(),
                "FINALIZED", (substitutedFilingQualified ? "QUALIFIED: " : "NOT_QUALIFIED: ")
                        + assessmentReason, actor);
        saveAudit(requested, taxYear, "ANNUALIZATION_FINALIZED", computed.status(),
                "FINALIZED", finalizationRemarks, actor);
        return fromSnapshot(snapshot, true);
    }

    @Override
    @Transactional
    public AnnualizationEmployeeDTO reopenAnnualization(
            int taxYear, String employeeNo, String reason, String performedBy,
            String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        String requested = requireEmployeeNo(employeeNo);
        String reopenReason = cleanRemarks(reason, "Reopen reason");
        if (reopenReason == null) {
            throw new IllegalArgumentException("A reason is required to reopen annualization.");
        }
        AnnualizationSnapshot snapshot = snapshotRepository
                .findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                        requested, taxYear, "FINALIZED")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "Annualization is not finalized."));
        if (snapshot.getAdjustmentHeaderId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This annualization already created payroll adjustment " +
                            snapshot.getAdjustmentHeaderId() + " and can no longer be reopened.");
        }
        if (refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(
                snapshot.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Void the recorded refund voucher before reopening annualization.");
        }
        String actor = requireActor(performedBy);
        snapshot.setStatus("VOIDED");
        snapshot.setReopenedAt(LocalDateTime.now());
        snapshot.setReopenedBy(actor);
        snapshot.setReopenReason(reopenReason);
        snapshotRepository.save(snapshot);
        saveAudit(requested, taxYear, "ANNUALIZATION_REOPENED", "FINALIZED",
                "COMPUTED", reopenReason, actor);
        return compute(taxYear, authorizationHeader).records().stream()
                .filter(record -> requested.equalsIgnoreCase(record.employeeNo()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No finalized payroll was found after reopening annualization."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnualizationAdjustmentPeriodDTO> getEligibleAdjustmentPeriods(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        String requested = requireEmployeeNo(employeeNo);
        AnnualizationSnapshot snapshot = snapshotRepository.findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                        requested, taxYear, "FINALIZED")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "Annualization must be finalized first."));
        requireAssessedSeparationFinalPay(snapshot);
        return detailRepository
                .findByEmployeeNoIgnoreCaseAndSalaryDateBetweenOrderBySalaryDateDesc(
                        requested, LocalDate.of(taxYear, 1, 1), LocalDate.of(taxYear, 12, 31))
                .stream()
                .filter(detail -> detail.getStatus() != PayrollStatus.CANCELLED)
                .filter(detail -> !periodLockRepository.existsBySalaryPeriodKey(
                        detail.getSalaryPeriodKey()))
                .map(detail -> new AnnualizationAdjustmentPeriodDTO(
                        detail.getSalaryPeriodKey(), detail.getSalaryDate()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public AnnualizationEmployeeDTO applyAdjustment(
            int taxYear, String employeeNo, String salaryPeriodKey, String performedBy,
            String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        String requested = requireEmployeeNo(employeeNo);
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("A target salary period is required.");
        }
        String targetPeriod = salaryPeriodKey.trim();
        AnnualizationSnapshot snapshot = snapshotRepository
                .findFinalizedForUpdate(requested, taxYear)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "Annualization must be finalized first."));
        requireAssessedSeparationFinalPay(snapshot);
        if (snapshot.getAdjustmentHeaderId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This annualization was already applied to salary period " +
                            snapshot.getAdjustmentSalaryPeriodKey() + ".");
        }
        if (refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(
                snapshot.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A paid refund voucher is already recorded for this annualization.");
        }
        if (snapshot.getAdjustmentAmount() == null || snapshot.getAdjustmentAmount().signum() == 0
                || "NO_ADJUSTMENT".equals(snapshot.getAdjustmentType())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This annualization has no withholding adjustment to apply.");
        }
        PayrollDetail target = detailRepository
                .findByEmployeeNoAndSalaryPeriodKey(snapshot.getEmployeeNo(), targetPeriod)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "The selected salary period has no computed payroll for this employee."));
        if (target.getSalaryDate() == null || target.getSalaryDate().getYear() != taxYear) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The target salary period must belong to tax year " + taxYear + ".");
        }
        if (target.getStatus() == PayrollStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A cancelled payroll period cannot receive an adjustment.");
        }
        String actor = requireActor(performedBy);
        PayrollAdjustmentDTO adjustment = payrollAdjustmentService.createAnnualizationAdjustment(
                snapshot.getEmployeeNo(), snapshot.getEmployeeName(), targetPeriod,
                snapshot.getAdjustmentAmount(), taxYear, snapshot.getVersion(), actor,
                authorizationHeader);
        snapshot.setAdjustmentHeaderId(adjustment.getId());
        snapshot.setAdjustmentSalaryPeriodKey(targetPeriod);
        snapshot.setAdjustmentAppliedAt(LocalDateTime.now());
        snapshot.setAdjustmentAppliedBy(actor);
        snapshotRepository.save(snapshot);
        saveAudit(requested, taxYear, "ANNUALIZATION_ADJUSTMENT_CREATED", "FINALIZED",
                "FINALIZED", "Pending payroll adjustment " + adjustment.getId() +
                        " created for " + targetPeriod, actor);
        return fromSnapshot(snapshot, true);
    }

    private void requireAssessedSeparationFinalPay(AnnualizationSnapshot snapshot) {
        if ("SEPARATION".equals(snapshot.getAnnualizationType())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Do not apply a separation tax adjustment to an open payroll period: "
                            + "the finalized annualization excludes that period's compensation. "
                            + "The final salary must be included in the tax calculation before its withholding is posted.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnualizationAdjustmentPeriodDTO> getSeparationFinalPayPeriods(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        String requested = requireEmployeeNo(employeeNo);
        requireNotFinalized(requested, taxYear);
        return detailRepository.findByEmployeeNoIgnoreCaseAndSalaryDateBetweenOrderBySalaryDateDesc(
                        requested, LocalDate.of(taxYear, 1, 1), LocalDate.of(taxYear, 12, 31))
                .stream().filter(detail -> detail.getStatus() == PayrollStatus.COMPUTED)
                .filter(detail -> !periodLockRepository.existsBySalaryPeriodKey(detail.getSalaryPeriodKey()))
                .map(detail -> new AnnualizationAdjustmentPeriodDTO(
                        detail.getSalaryPeriodKey(), detail.getSalaryDate())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SeparationFinalPayReviewDTO reviewSeparationFinalPay(
            int taxYear, String employeeNo, String salaryPeriodKey, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        return computeSeparationFinalPayReview(taxYear, requireEmployeeNo(employeeNo),
                salaryPeriodKey, authorizationHeader);
    }

    @Override
    @Transactional
    public SeparationFinalPayReviewDTO prepareSeparationFinalPayAdjustment(
            int taxYear, String employeeNo, String salaryPeriodKey, String performedBy,
            String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        String requested = requireEmployeeNo(employeeNo);
        SeparationFinalPayReviewDTO review = computeSeparationFinalPayReview(
                taxYear, requested, salaryPeriodKey, authorizationHeader);
        if (!review.canApply()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, review.actionRequired());
        }
        AnnualizationEmployeeDTO annualization = review.provisionalAnnualization();
        PayrollAdjustmentDTO adjustment = payrollAdjustmentService.createAnnualizationAdjustment(
                requested, annualization.employeeName(), review.salaryPeriodKey(),
                annualization.adjustmentAmount(), taxYear, 0, requireActor(performedBy),
                authorizationHeader);
        saveAudit(requested, taxYear, "SEPARATION_FINAL_PAY_ADJUSTMENT_CREATED",
                annualization.status(), annualization.status(),
                "Pending final-pay adjustment " + adjustment.getId() + " for "
                        + review.salaryPeriodKey(), requireActor(performedBy));
        return new SeparationFinalPayReviewDTO(review.salaryPeriodKey(), review.salaryDate(),
                review.netPayBeforeAdjustment(), annualization, false,
                "Pending adjustment created. Post it, recompute and lock the final salary, "
                        + "then reload and finalize annualization.");
    }

    private SeparationFinalPayReviewDTO computeSeparationFinalPayReview(
            int taxYear, String employeeNo, String salaryPeriodKey, String authorizationHeader) {
        requireNotFinalized(employeeNo, taxYear);
        if (salaryPeriodKey == null || salaryPeriodKey.isBlank()) {
            throw new IllegalArgumentException("A final salary period is required.");
        }
        PayrollDetail target = detailRepository.findByEmployeeNoAndSalaryPeriodKey(
                        employeeNo, salaryPeriodKey.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No computed final salary was found for this employee and period."));
        if (target.getStatus() != PayrollStatus.COMPUTED || target.getSalaryDate() == null
                || target.getSalaryDate().getYear() != taxYear
                || periodLockRepository.existsBySalaryPeriodKey(target.getSalaryPeriodKey())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Select an open, computed final salary in the requested tax year.");
        }
        List<PayrollDetail> yearDetails = detailRepository
                .findByEmployeeNoIgnoreCaseAndSalaryDateBetweenOrderBySalaryDateDesc(
                        employeeNo, LocalDate.of(taxYear, 1, 1), LocalDate.of(taxYear, 12, 31));
        if (yearDetails.stream().filter(detail -> detail.getStatus() != PayrollStatus.CANCELLED)
                .anyMatch(detail -> !detail.getSalaryPeriodKey().equals(target.getSalaryPeriodKey())
                        && (!periodLockRepository.existsBySalaryPeriodKey(detail.getSalaryPeriodKey())
                        || detail.getSalaryDate().isAfter(target.getSalaryDate())))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The selected final salary must be the last period; all earlier computed periods must be locked.");
        }
        AnnualizationEmployeeDTO annualization = compute(taxYear, authorizationHeader, target)
                .records().stream().filter(record -> employeeNo.equalsIgnoreCase(record.employeeNo()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "The final salary could not be annualized."));
        if (!isSeparatedInYear(annualization, taxYear)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This workflow requires a recorded separation before year-end.");
        }
        List<PayrollAdjustmentLine> posted = adjustmentLineRepository.findPostedForEmployeePeriod(
                employeeNo, target.getSalaryPeriodKey());
        BigDecimal net = money(target.getNetAmount());
        for (PayrollAdjustmentLine line : posted) {
            BigDecimal amount = money(line.getAmount());
            net = "EARNING".equalsIgnoreCase(line.getType()) ? net.add(amount) : net.subtract(amount);
        }
        BigDecimal adjustment = annualization.adjustmentAmount();
        boolean alreadyPosted = payrollAdjustmentService.findAdjustment(
                employeeNo, target.getSalaryPeriodKey())
                .map(value -> "POSTED".equalsIgnoreCase(value.getStatus())).orElse(false);
        String issue = !annualization.readiness().ready()
                ? "Resolve annualization readiness: " + String.join("; ", annualization.readiness().issues())
                : net.signum() < 0
                    ? "Posted adjustments exceed the final salary. Correct payroll before annualizing."
                : adjustment == null || adjustment.signum() == 0
                    ? "No final-pay withholding adjustment is needed. Lock the salary, reload, and finalize."
                : alreadyPosted
                    ? "This salary already has a posted adjustment. Resolve it before creating a final-pay tax adjustment."
                : adjustment.signum() > 0 && adjustment.compareTo(net) > 0
                    ? "The additional tax exceeds the available final pay. Employer-advance evidence is required; do not deduct beyond net pay."
                : null;
        return new SeparationFinalPayReviewDTO(target.getSalaryPeriodKey(), target.getSalaryDate(),
                scale(net), annualization, issue == null, issue);
    }

    private boolean isSeparatedInYear(AnnualizationEmployeeDTO record, int taxYear) {
        LocalDate separation = record.taxIdentity() == null
                ? null : record.taxIdentity().employeeEmploymentTo();
        return separation != null && separation.getYear() == taxYear
                && separation.isBefore(LocalDate.of(taxYear, 12, 31));
    }

    @Override
    @Transactional(readOnly = true)
    public AnnualizationRefundVoucherDTO getRefundVoucher(
            int taxYear, String employeeNo, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.ACCESS);
        AnnualizationSnapshot snapshot = snapshotRepository
                .findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                        requireEmployeeNo(employeeNo), taxYear, "FINALIZED")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Annualization must be finalized first."));
        return refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(
                snapshot.getId()).map(this::toRefundVoucherDto).orElse(null);
    }

    @Override
    @Transactional
    public AnnualizationRefundVoucherDTO recordRefundVoucher(
            int taxYear, String employeeNo, AnnualizationRefundVoucherRequest request,
            String performedBy, String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        AnnualizationSnapshot snapshot = snapshotRepository
                .findFinalizedForUpdate(requireEmployeeNo(employeeNo), taxYear)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Annualization must be finalized first."));
        if (!"REFUND".equals(snapshot.getAdjustmentType())
                || snapshot.getAdjustmentAmount() == null
                || snapshot.getAdjustmentAmount().signum() >= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This annualization has no refund to record.");
        }
        if (snapshot.getAdjustmentHeaderId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A payroll adjustment is already linked to this annualization. Do not record the refund twice.");
        }
        if (!snapshotPayrollPeriodsLocked(snapshot.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "All payroll periods in the finalized annualization must still be locked.");
        }
        if (refundVoucherRepository.findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(
                snapshot.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A paid refund voucher is already recorded for this annualization.");
        }
        if (request == null || !Boolean.TRUE.equals(request.confirmedPaid())
                || request.paidDate() == null || request.voucherReference() == null
                || request.voucherReference().isBlank()) {
            throw new IllegalArgumentException(
                    "Confirm the refund was paid and provide its payment date and voucher reference.");
        }
        if ("SEPARATION".equals(snapshot.getAnnualizationType())) {
            LocalDate lastSalaryDate = snapshotPeriodRepository
                    .findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(snapshot.getId()).stream()
                    .map(AnnualizationSnapshotPeriod::getSalaryDate)
                    .filter(Objects::nonNull).max(LocalDate::compareTo).orElse(null);
            if (lastSalaryDate == null || !request.paidDate().equals(lastSalaryDate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "For separation, the refund must be paid with the last compensation on "
                                + lastSalaryDate + ". Verify the payroll salary date and voucher evidence.");
            }
        }
        String reference = request.voucherReference().trim();
        if (reference.length() > 100) {
            throw new IllegalArgumentException("Voucher reference must be 100 characters or fewer.");
        }
        AnnualizationRefundVoucher voucher = new AnnualizationRefundVoucher();
        voucher.setSnapshotId(snapshot.getId());
        voucher.setAmount(scale(snapshot.getAdjustmentAmount().abs()));
        voucher.setVoucherReference(reference);
        voucher.setPaidDate(request.paidDate());
        voucher.setRecordedBy(requireActor(performedBy));
        voucher.setRecordedAt(LocalDateTime.now());
        AnnualizationRefundVoucher saved = refundVoucherRepository.save(voucher);
        saveAudit(snapshot.getEmployeeNo(), taxYear, "REFUND_VOUCHER_RECORDED",
                "FINALIZED", "FINALIZED",
                "Paid voucher " + reference + " for refund " + saved.getAmount()
                        + " on " + saved.getPaidDate(), saved.getRecordedBy());
        return toRefundVoucherDto(saved);
    }

    @Override
    @Transactional
    public void voidRefundVoucher(
            int taxYear, String employeeNo, String reason, String performedBy,
            String authorizationHeader) {
        validateYear(taxYear);
        requireAnnualizationPermission(authorizationHeader, PermissionAction.FINALIZE);
        AnnualizationSnapshot snapshot = snapshotRepository
                .findFinalizedForUpdate(requireEmployeeNo(employeeNo), taxYear)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Annualization must be finalized first."));
        AnnualizationRefundVoucher voucher = refundVoucherRepository
                .findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(snapshot.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No active refund voucher is recorded."));
        if (reason == null || reason.isBlank() || reason.trim().length() > 300) {
            throw new IllegalArgumentException("A void reason of 1 to 300 characters is required.");
        }
        String actor = requireActor(performedBy);
        voucher.setVoidedAt(LocalDateTime.now());
        voucher.setVoidedBy(actor);
        voucher.setVoidReason(reason.trim());
        refundVoucherRepository.save(voucher);
        saveAudit(snapshot.getEmployeeNo(), taxYear, "REFUND_VOUCHER_VOIDED",
                "FINALIZED", "FINALIZED",
                "Voucher " + voucher.getVoucherReference() + " voided: " + reason.trim(), actor);
    }

    private AnnualizationRefundVoucherDTO toRefundVoucherDto(AnnualizationRefundVoucher voucher) {
        return new AnnualizationRefundVoucherDTO(
                voucher.getId(), voucher.getAmount(), voucher.getPaidDate(),
                voucher.getVoucherReference(), voucher.getRecordedBy(), voucher.getRecordedAt());
    }

    private Computation compute(int taxYear, String authorizationHeader) {
        return compute(taxYear, authorizationHeader, null);
    }

    private Computation compute(int taxYear, String authorizationHeader, PayrollDetail openFinalPay) {
        LocalDate fromDate = LocalDate.of(taxYear, 1, 1);
        LocalDate toDate = LocalDate.of(taxYear, 12, 31);
        BenefitContext benefitContext = loadBenefitContext(taxYear, authorizationHeader);
        Map<String, AnnualBenefitReview> benefitReviews = new LinkedHashMap<>();
        benefitReviewRepository.findByTaxYear(taxYear).forEach(value ->
                benefitReviews.put(reviewKey(value.getSourceType(), value.getSourceId()), value));
        Map<String, PeraTaxCreditDTO> peraCredits = new LinkedHashMap<>();
        peraTaxCreditRepository.findByTaxYear(taxYear).forEach(source -> peraCredits.put(
                source.getEmployeeNo().toUpperCase(Locale.ROOT), toPeraTaxCreditDto(source)));

        List<PayrollDetail> details = new ArrayList<>(detailRepository.findFinalizedForAnnualization(
                fromDate, toDate, PayrollStatus.CANCELLED));
        if (openFinalPay != null) details.add(openFinalPay);
        Map<PeriodKey, PeriodAccumulator> periods = new LinkedHashMap<>();
        for (PayrollDetail detail : details) {
            PeriodKey key = new PeriodKey(detail.getEmployeeNo(), detail.getSalaryPeriodKey());
            periods.put(key, new PeriodAccumulator(detail));
        }
        if (!benefitContext.available()) periods.values().forEach(period -> period.mweIssues.add(
                "Benefit tax profiles could not be loaded from Administrative; retry before finalization"));

        List<PayrollDetailEarning> earnings = new ArrayList<>(earningRepository.findFinalizedForAnnualization(
                fromDate, toDate, PayrollStatus.CANCELLED));
        if (openFinalPay != null) earnings.addAll(openFinalPay.getEarnings());
        for (PayrollDetailEarning earning : earnings) {
            PayrollDetail detail = earning.getPayrollDetail();
            PeriodAccumulator period = periods.get(new PeriodKey(
                    detail.getEmployeeNo(), detail.getSalaryPeriodKey()));
            if (period == null) continue;
            AnnualBenefitReview benefitReview = benefitReviews.get(reviewKey("PAYROLL_EARNING", earning.getId()));
            String suggestedProfile = benefitContext.profileFor(earning.getEarningCode());
            if (benefitReview != null || suggestedProfile != null) {
                if (applyBenefitReview(period, benefitReview, suggestedProfile, benefitContext,
                        money(earning.getAmount()), earning.getEarningName())) continue;
            }
            String birItem = earning.getBir2316ItemNo();
            Boolean taxable = earning.getIsTaxable();
            if ((birItem == null || birItem.isBlank())
                    && ("BASIC".equalsIgnoreCase(earning.getEarningCode())
                    || "HAZARD".equalsIgnoreCase(earning.getEarningCode()))) {
                EarningTaxSetting setting = benefitContext.generatedTaxSetting(earning.getEarningCode());
                if (setting != null) {
                    birItem = setting.bir2316ItemNo();
                    taxable = setting.taxable();
                }
            }
            if (birItem == null || birItem.isBlank()) {
                period.unclassifiedEarnings.add(earning.getEarningName() == null
                        ? earning.getEarningCode() : earning.getEarningName());
            } else {
                period.addBirItem(
                        birItem, earning.getEarningName(),
                        money(earning.getAmount()));
            }
            if (Boolean.TRUE.equals(taxable)) {
                period.taxableEarnings = period.taxableEarnings.add(money(earning.getAmount()));
                period.addTaxableBirItem(birItem, money(earning.getAmount()));
            }
        }

        List<PayrollDetailDeduction> deductions = new ArrayList<>(deductionRepository.findFinalizedForAnnualization(
                fromDate, toDate, PayrollStatus.CANCELLED));
        if (openFinalPay != null) deductions.addAll(openFinalPay.getDeductions());
        for (PayrollDetailDeduction deduction : deductions) {
            PayrollDetail detail = deduction.getPayrollDetail();
            PeriodAccumulator period = periods.get(new PeriodKey(
                    detail.getEmployeeNo(), detail.getSalaryPeriodKey()));
            if (period != null) {
                addClassifiedDeduction(period, deduction.getDeductionCode(), deduction.getAmount());
            }
        }

        List<PayrollAdjustmentLine> postedLines = new ArrayList<>(adjustmentLineRepository.findPostedForAnnualization(
                fromDate, toDate, PayrollStatus.CANCELLED));
        if (openFinalPay != null) postedLines.addAll(adjustmentLineRepository.findPostedForEmployeePeriod(
                openFinalPay.getEmployeeNo(), openFinalPay.getSalaryPeriodKey()));
        for (PayrollAdjustmentLine line : postedLines) {
            PeriodKey key = new PeriodKey(
                    line.getHeader().getEmployeeNo(), line.getHeader().getSalaryPeriodKey());
            PeriodAccumulator period = periods.get(key);
            if (period == null) continue;
            BigDecimal amount = money(line.getAmount());
            if ("EARNING".equalsIgnoreCase(line.getType())) {
                period.gross = period.gross.add(amount);
                AnnualBenefitReview benefitReview = benefitReviews.get(reviewKey("POSTED_ADJUSTMENT", line.getId()));
                String suggestedProfile = benefitContext.profileFor(line.getCode());
                if ((benefitReview != null || suggestedProfile != null)
                        && applyBenefitReview(period, benefitReview, suggestedProfile, benefitContext,
                                amount, line.getName())) continue;
                if (line.getBir2316ItemNo() == null || line.getBir2316ItemNo().isBlank()) {
                    period.unclassifiedEarnings.add(line.getName() == null
                            ? line.getCode() : line.getName());
                } else {
                    period.addBirItem(line.getBir2316ItemNo(), line.getName(), amount);
                }
                if (Boolean.TRUE.equals(line.getIsTaxable())) {
                    period.taxableEarnings = period.taxableEarnings.add(amount);
                    period.addTaxableBirItem(line.getBir2316ItemNo(), amount);
                }
            } else if ("DEDUCTION".equalsIgnoreCase(line.getType())) {
                addClassifiedDeduction(period, line.getCode(), line.getAmount());
            }
        }

        List<AnnualTaxBracket> brackets = loadAnnualTaxBrackets(taxYear, authorizationHeader);
        boolean taxTableConfigured = !brackets.isEmpty();
        AnnualizationEmployerIdentity employerIdentity = loadEmployerIdentity(authorizationHeader);
        Map<String, AnnualizationEmployeeIdentity> employeeIdentities =
                loadEmployeeIdentities(taxYear, authorizationHeader);
        Map<Long, String> jobPositions = loadJobPositions(authorizationHeader);
        Map<Long, NatureOfAppointmentIdentity> appointmentNatures =
                loadAppointmentNatures(authorizationHeader);
        Map<Long, NatureOfSeparationIdentity> separationNatures =
                loadSeparationNatures(authorizationHeader);
        Map<String, List<EmployeeMweStatusSource>> mweStatuses =
                loadEmployeeMweStatuses(authorizationHeader);
        List<MinimumWageRateSource> minimumWageRates =
                loadMinimumWageRates(authorizationHeader);

        Map<String, List<PreviousEmployerTaxDataDTO>> previousEmployers = new LinkedHashMap<>();
        for (PreviousEmployerTaxData value
                : previousEmployerRepository.findByTaxYearOrderByEmployeeNoAscEmploymentFromAsc(taxYear)) {
            previousEmployers
                    .computeIfAbsent(value.getEmployeeNo().toUpperCase(Locale.ROOT), ignored -> new ArrayList<>())
                    .add(toPreviousEmployerDto(value));
        }

        Map<String, EmployeeAccumulator> employees = new LinkedHashMap<>();
        for (PeriodAccumulator period : periods.values()) {
            period.resolveMinimumWage(
                    mweStatuses.getOrDefault(period.employeeNo.toUpperCase(Locale.ROOT), List.of()),
                    minimumWageRates);
            EmployeeAccumulator employee = employees.computeIfAbsent(
                    period.employeeNo,
                    ignored -> new EmployeeAccumulator(
                            period.employeeNo, period.employeeName, period.department));
            employee.add(
                    period.toDto(), period.unclassifiedEarnings,
                    period.birItemsForAnnualization(),
                    period.birItemDescriptions, period.mweIssues);
        }

        List<AnnualizationEmployeeDTO> records = new ArrayList<>();
        for (EmployeeAccumulator employee : employees.values()) {
            records.add(employee.toDto(
                    taxYear,
                    brackets,
                    taxTableConfigured,
                    employerIdentity,
                    employeeIdentities.get(employee.employeeNo.toUpperCase(Locale.ROOT)),
                    jobPositions,
                    appointmentNatures,
                    separationNatures,
                    previousEmployers.getOrDefault(
                            employee.employeeNo.toUpperCase(Locale.ROOT), List.of()),
                    peraCredits.get(employee.employeeNo.toUpperCase(Locale.ROOT))));
        }
        records.sort(Comparator.comparing(AnnualizationEmployeeDTO::employeeName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return new Computation(taxTableConfigured, records);
    }

    private String reviewKey(String type, Long id) {
        return type + ":" + id;
    }

    private boolean applyBenefitReview(PeriodAccumulator period, AnnualBenefitReview review,
            String suggestedProfile, BenefitContext context, BigDecimal sourceAmount, String name) {
        if (review == null) {
            period.mweIssues.add("Benefit classification review is required for " + name
                    + " in salary period " + period.salaryPeriodKey);
            return false;
        }
        BenefitTaxProfileSource profile = context.profiles().get(review.getProfileCode());
        if (profile == null || !sameProfile(review, profile)
                || review.getSourceAmount().compareTo(sourceAmount) != 0
                || suggestedProfile != null && !suggestedProfile.equalsIgnoreCase(review.getProfileCode())
                || !validReviewAmounts(review.getExemptAmount(), review.getOtherExemptAmount(),
                        review.getTaxableAmount(), sourceAmount)
                || !validReviewItems(review.getExemptAmount(), review.getExemptBirItem(),
                        review.getOtherExemptAmount(), review.getOtherExemptBirItem(),
                        review.getTaxableAmount(), review.getTaxableBirItem(), profile)) {
            period.mweIssues.add("Benefit classification review is stale or invalid for " + name
                    + " in salary period " + period.salaryPeriodKey);
            return false;
        }
        if (review.getExemptAmount().signum() != 0) {
            period.addBirItem(review.getExemptBirItem(), name, review.getExemptAmount());
        }
        if (review.getOtherExemptAmount().signum() != 0) {
            period.addBirItem(review.getOtherExemptBirItem(), name, review.getOtherExemptAmount());
        }
        if (review.getTaxableAmount().signum() != 0) {
            period.addBirItem(review.getTaxableBirItem(), name, review.getTaxableAmount());
            period.addTaxableBirItem(review.getTaxableBirItem(), review.getTaxableAmount());
            period.taxableEarnings = period.taxableEarnings.add(review.getTaxableAmount());
        }
        return true;
    }

    private boolean validReviewAmounts(BigDecimal exempt, BigDecimal otherExempt, BigDecimal taxable, BigDecimal source) {
        if (exempt == null || otherExempt == null || taxable == null || source == null
                || exempt.scale() > 2 || otherExempt.scale() > 2 || taxable.scale() > 2) return false;
        if (source.signum() >= 0 && (exempt.signum() < 0 || otherExempt.signum() < 0 || taxable.signum() < 0)) return false;
        if (source.signum() < 0 && (exempt.signum() > 0 || otherExempt.signum() > 0 || taxable.signum() > 0)) return false;
        return exempt.add(otherExempt).add(taxable).compareTo(source) == 0;
    }

    private boolean validReviewItems(BigDecimal exempt, String exemptItem,
            BigDecimal otherExempt, String otherExemptItem,
            BigDecimal taxable, String taxableItem, BenefitTaxProfileSource profile) {
        String nonTaxable = normalizeBirItemNo(exemptItem);
        String otherNonTaxable = normalizeBirItemNo(otherExemptItem);
        String taxableNo = normalizeBirItemNo(taxableItem);
        if (exempt.signum() != 0 && (nonTaxable == null || !NON_TAXABLE_BENEFIT_ITEMS.contains(nonTaxable)
                || profile.exemptBirItem() != null
                && !profile.exemptBirItem().equalsIgnoreCase(nonTaxable))) return false;
        if (otherExempt.signum() != 0 && !"34".equals(otherNonTaxable)) return false;
        if (taxable.signum() != 0 && (taxableNo == null || !TAXABLE_BENEFIT_ITEMS.contains(taxableNo)
                || profile.taxableBirItem() != null
                && !profile.taxableBirItem().equalsIgnoreCase(taxableNo))) return false;
        return true;
    }

    private static final Set<String> NON_TAXABLE_BENEFIT_ITEMS = Set.of("29", "30", "31", "32", "33", "34", "35", "37");
    private static final Set<String> TAXABLE_BENEFIT_ITEMS = Set.of("39", "40", "41", "42", "43", "44A", "44B", "45", "46", "47", "48", "49", "50", "51A", "51B");

    private boolean sameProfile(AnnualBenefitReview review, BenefitTaxProfileSource profile) {
        return Objects.equals(review.getProfileLegalBasis(), profile.legalBasis())
                && Objects.equals(review.getProfileCeilingPeriod(), profile.ceilingPeriod())
                && ((review.getProfileGuidanceCeiling() == null && profile.guidanceCeiling() == null)
                    || review.getProfileGuidanceCeiling() != null && profile.guidanceCeiling() != null
                    && review.getProfileGuidanceCeiling().compareTo(profile.guidanceCeiling()) == 0);
    }

    private BenefitContext loadBenefitContext(int year, String authorizationHeader) {
        String base = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        HttpEntity<Void> request = new HttpEntity<>(forwardedHeaders(authorizationHeader));
        try {
            ResponseEntity<List<BenefitTaxProfileSource>> profilesResponse = restTemplate.exchange(
                    base + "/api/benefit-tax-profiles?year=" + year,
                    HttpMethod.GET, request,
                    new ParameterizedTypeReference<List<BenefitTaxProfileSource>>() {});
            ResponseEntity<List<EarningTypeBenefitSource>> earningsResponse = restTemplate.exchange(
                    base + "/api/earningType/get-all", HttpMethod.GET, request,
                    new ParameterizedTypeReference<List<EarningTypeBenefitSource>>() {});
            Map<String, BenefitTaxProfileSource> profiles = new LinkedHashMap<>();
            if (profilesResponse.getBody() != null) profilesResponse.getBody().forEach(value ->
                    profiles.put(value.profileCode(), value));
            Map<String, String> byEarning = new LinkedHashMap<>();
            Map<String, EarningTaxSetting> generatedTaxSettings = new LinkedHashMap<>();
            List<EarningTypeBenefitSource> sources = earningsResponse.getBody() == null
                    ? List.of() : earningsResponse.getBody();
            for (String code : List.of("BASIC", "HAZARD")) {
                List<EarningTypeBenefitSource> exact = sources.stream()
                        .filter(source -> code.equalsIgnoreCase(source.accountingCode()))
                        .toList();
                List<EarningTypeBenefitSource> flagged = sources.stream()
                        .filter(source -> "BASIC".equals(code)
                                ? Boolean.TRUE.equals(source.basic())
                                : Boolean.TRUE.equals(source.hazardPay()))
                        .toList();
                List<EarningTypeBenefitSource> matches = exact.size() == 1 ? exact : flagged;
                if (matches.size() == 1) {
                    EarningTypeBenefitSource source = matches.get(0);
                    generatedTaxSettings.put(code,
                            new EarningTaxSetting(source.taxable(), source.bir2316ItemNo()));
                }
            }
            if (earningsResponse.getBody() != null) for (EarningTypeBenefitSource earning : earningsResponse.getBody()) {
                if (earning.benefitTaxProfileCode() == null || earning.benefitTaxProfileCode().isBlank()) continue;
                if (earning.accountingCode() != null) byEarning.put(earning.accountingCode().trim().toUpperCase(Locale.ROOT), earning.benefitTaxProfileCode());
                if (earning.earningTypeId() != null) byEarning.put(earning.earningTypeId().toString(), earning.benefitTaxProfileCode());
            }
            return new BenefitContext(profiles, byEarning, generatedTaxSettings, true);
        } catch (Exception exception) {
            log.warn("Annualization could not load benefit tax profiles: {}", exception.getMessage());
            return new BenefitContext(Map.of(), Map.of(), Map.of(), false);
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record BenefitTaxProfileSource(Long id, String profileCode, Integer taxYear,
            String label, BigDecimal guidanceCeiling, String ceilingPeriod, String sharedPoolCode,
            String exemptBirItem, String taxableBirItem, String legalBasis, String sourceUrl) {}
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record EarningTypeBenefitSource(Long earningTypeId, String accountingCode, String name,
            String benefitTaxProfileCode, Boolean basic, Boolean hazardPay,
            Boolean taxable, String bir2316ItemNo) {
        public EarningTypeBenefitSource(Long earningTypeId, String accountingCode, String name,
                String benefitTaxProfileCode) {
            this(earningTypeId, accountingCode, name, benefitTaxProfileCode, null, null, null, null);
        }
    }
    private record EarningTaxSetting(Boolean taxable, String bir2316ItemNo) {}
    private record BenefitContext(Map<String, BenefitTaxProfileSource> profiles,
            Map<String, String> earningProfiles, Map<String, EarningTaxSetting> generatedTaxSettings,
            boolean available) {
        private String profileFor(String code) {
            return code == null ? null : earningProfiles.get(code.trim().toUpperCase(Locale.ROOT));
        }
        private EarningTaxSetting generatedTaxSetting(String code) {
            return code == null ? null : generatedTaxSettings.get(code.trim().toUpperCase(Locale.ROOT));
        }
    }

    private void addClassifiedDeduction(PeriodAccumulator period, String rawCode, Double rawAmount) {
        String code = normalizeCode(rawCode);
        BigDecimal amount = money(rawAmount);
        if ("GSIS".equals(code) || code.startsWith("GSISCASCADE")) {
            period.gsis = period.gsis.add(amount);
            period.addBirItem("36", "GSIS contribution", amount);
        } else if ("SSS".equals(code) || code.startsWith("SSS")) {
            period.addBirItem("36", "SSS contribution", amount);
        } else if ("PHIC".equals(code) || "PHILHEALTH".equals(code)
                || code.startsWith("PHICCASCADE")) {
            period.philHealth = period.philHealth.add(amount);
            period.addBirItem("36", "PhilHealth contribution", amount);
        } else if ("HDMF".equals(code) || "PAGIBIG".equals(code)) {
            period.pagIbig = period.pagIbig.add(amount);
            period.addBirItem("36", "Pag-IBIG contribution", amount);
        } else if (code.startsWith("UNION")) {
            period.addBirItem("36", "Union dues", amount);
        } else if (code.startsWith("WTX") || "WITHHOLDINGTAX".equals(code)) {
            period.taxWithheld = period.taxWithheld.add(amount);
        }
    }

    private List<AnnualTaxBracket> loadAnnualTaxBrackets(int taxYear, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        String url = baseUrl + "/api/wh-tax/annual-brackets?year=" + taxYear;
        try {
            ResponseEntity<List<AnnualTaxBracket>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<AnnualTaxBracket>>() {});
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (Exception exception) {
            log.warn("Annualization could not load annual tax brackets for {}: {}",
                    taxYear, exception.getMessage());
            return List.of();
        }
    }

    private AnnualizationEmployerIdentity loadEmployerIdentity(String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            return restTemplate.exchange(
                    baseUrl + "/api/settings/annualization-identity",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    AnnualizationEmployerIdentity.class).getBody();
        } catch (Exception exception) {
            log.warn("Annualization could not load employer tax identity: {}", exception.getMessage());
            return null;
        }
    }

    private Map<String, AnnualizationEmployeeIdentity> loadEmployeeIdentities(
            int taxYear, String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, humanResourceUrl);
        try {
            ResponseEntity<List<AnnualizationEmployeeIdentity>> response = restTemplate.exchange(
                    baseUrl + "/api/employees/annualization-identities?year=" + taxYear,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<AnnualizationEmployeeIdentity>>() {});
            Map<String, AnnualizationEmployeeIdentity> result = new LinkedHashMap<>();
            if (response.getBody() != null) {
                response.getBody().forEach(identity -> {
                    if (identity.employeeNo() != null && !identity.employeeNo().isBlank()) {
                        result.put(identity.employeeNo().toUpperCase(Locale.ROOT), identity);
                    }
                });
            }
            return result;
        } catch (Exception exception) {
            log.warn("Annualization could not load employee tax identities: {}", exception.getMessage());
            return Map.of();
        }
    }

    private Map<Long, NatureOfAppointmentIdentity> loadAppointmentNatures(
            String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            ResponseEntity<List<NatureOfAppointmentIdentity>> response = restTemplate.exchange(
                    baseUrl + "/api/natureOfAppointment/get-all",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<NatureOfAppointmentIdentity>>() {});
            Map<Long, NatureOfAppointmentIdentity> result = new LinkedHashMap<>();
            if (response.getBody() != null) {
                response.getBody().forEach(nature -> {
                    if (nature.natureOfAppointmentId() != null) {
                        result.put(nature.natureOfAppointmentId(), nature);
                    }
                });
            }
            return result;
        } catch (Exception exception) {
            log.warn("Annualization could not load appointment natures: {}",
                    exception.getMessage());
            return Map.of();
        }
    }

    private Map<Long, NatureOfSeparationIdentity> loadSeparationNatures(
            String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            ResponseEntity<List<NatureOfSeparationIdentity>> response = restTemplate.exchange(
                    baseUrl + "/api/natureOfSeparation/get-all", HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<NatureOfSeparationIdentity>>() {});
            Map<Long, NatureOfSeparationIdentity> result = new LinkedHashMap<>();
            if (response.getBody() != null) {
                response.getBody().forEach(nature -> {
                    if (nature.natureOfSeparationId() != null) {
                        result.put(nature.natureOfSeparationId(), nature);
                    }
                });
            }
            return result;
        } catch (Exception exception) {
            log.warn("Annualization could not load separation natures: {}", exception.getMessage());
            return Map.of();
        }
    }

    private Map<Long, String> loadJobPositions(String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            ResponseEntity<List<JobPositionIdentity>> response = restTemplate.exchange(
                    baseUrl + "/api/job-position/get-all",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<JobPositionIdentity>>() {});
            Map<Long, String> result = new LinkedHashMap<>();
            if (response.getBody() != null) {
                response.getBody().forEach(position -> {
                    if (position.jobPositionId() != null
                            && position.jobPositionName() != null
                            && !position.jobPositionName().isBlank()) {
                        result.put(position.jobPositionId(), position.jobPositionName().trim());
                    }
                });
            }
            return result;
        } catch (Exception exception) {
            log.warn("Annualization could not load authoritative job positions: {}",
                    exception.getMessage());
            return Map.of();
        }
    }

    private Map<String, List<EmployeeMweStatusSource>> loadEmployeeMweStatuses(
            String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, humanResourceUrl);
        try {
            ResponseEntity<List<EmployeeMweStatusSource>> response = restTemplate.exchange(
                    baseUrl + "/api/employee-mwe-statuses",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<EmployeeMweStatusSource>>() {});
            Map<String, List<EmployeeMweStatusSource>> result = new LinkedHashMap<>();
            if (response.getBody() != null) {
                for (EmployeeMweStatusSource status : response.getBody()) {
                    if (status.employeeNo() != null && !status.employeeNo().isBlank()) {
                        result.computeIfAbsent(status.employeeNo().toUpperCase(Locale.ROOT),
                                ignored -> new ArrayList<>()).add(status);
                    }
                }
            }
            return result;
        } catch (Exception exception) {
            log.warn("Annualization could not load employee MWE statuses: {}", exception.getMessage());
            return Map.of();
        }
    }

    private List<MinimumWageRateSource> loadMinimumWageRates(String authorizationHeader) {
        HttpHeaders headers = forwardedHeaders(authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            ResponseEntity<List<MinimumWageRateSource>> response = restTemplate.exchange(
                    baseUrl + "/api/minimum-wage-rates",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<MinimumWageRateSource>>() {});
            return response.getBody() == null ? List.of() : response.getBody();
        } catch (Exception exception) {
            log.warn("Annualization could not load statutory minimum-wage rates: {}",
                    exception.getMessage());
            return List.of();
        }
    }

    private HttpHeaders forwardedHeaders(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }
        return headers;
    }

    private void requireAnnualizationPermission(
            String authorizationHeader, PermissionAction action) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AccessDeniedException("Annualization access is not permitted.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            ResponseEntity<EffectivePermission> response = restTemplate.exchange(
                    baseUrl + "/api/authorization/effective?featureKey=payroll.annualization",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    EffectivePermission.class);
            EffectivePermission permission = response.getBody();
            if (permission != null && (permission.administrator() || permission.allows(action))) {
                return;
            }
        } catch (AccessDeniedException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Annualization permission check failed: {}", exception.getMessage());
        }
        throw new AccessDeniedException("Annualization access is not permitted.");
    }

    private String requireEmployeeNo(String employeeNo) {
        if (employeeNo == null || employeeNo.isBlank()) {
            throw new IllegalArgumentException("Employee number is required.");
        }
        return employeeNo.trim();
    }

    private String requireActor(String performedBy) {
        return performedBy == null || performedBy.isBlank() ? "SYSTEM" : performedBy.trim();
    }

    private void validatePreviousEmployer(PreviousEmployerTaxDataDTO request, int taxYear) {
        if (request == null) throw new IllegalArgumentException("Previous-employer data is required.");
        if (request.employerName() == null || request.employerName().isBlank()) {
            throw new IllegalArgumentException("Previous employer name is required.");
        }
        if (request.employerTin() == null || request.employerTin().isBlank()) {
            throw new IllegalArgumentException("Previous employer TIN is required.");
        }
        if (request.employerAddress() == null || request.employerAddress().isBlank()) {
            throw new IllegalArgumentException("Previous employer address is required.");
        }
        if (request.employerZipCode() == null || request.employerZipCode().isBlank()) {
            throw new IllegalArgumentException("Previous employer ZIP code is required.");
        }
        if (request.employmentFrom() == null || request.employmentTo() == null) {
            throw new IllegalArgumentException("Previous employment dates are required.");
        }
        if (request.employmentFrom().isAfter(request.employmentTo())) {
            throw new IllegalArgumentException("Employment From must not be later than Employment To.");
        }
        if (request.employmentFrom().getYear() != taxYear || request.employmentTo().getYear() != taxYear) {
            throw new IllegalArgumentException("Previous employment dates must fall within tax year " + taxYear + ".");
        }
        requireNonNegative(request.grossCompensation(), "Gross compensation");
        requireNonNegative(request.nonTaxableCompensation(), "Non-taxable compensation");
        requireNonNegative(request.taxableCompensation(), "Taxable compensation");
        requireNonNegative(request.taxWithheld(), "Tax withheld");
        if (request.taxableCompensation().add(request.nonTaxableCompensation())
                .compareTo(request.grossCompensation()) > 0) {
            throw new IllegalArgumentException(
                    "Taxable and non-taxable compensation cannot exceed gross compensation.");
        }
    }

    private void requireNonNegative(BigDecimal value, String label) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(label + " must be zero or greater.");
        }
    }

    private void applyPreviousEmployer(
            PreviousEmployerTaxData target, PreviousEmployerTaxDataDTO source,
            String employeeNo, int taxYear) {
        target.setEmployeeNo(employeeNo);
        target.setTaxYear(taxYear);
        target.setEmployerName(source.employerName().trim());
        target.setEmployerTin(source.employerTin().trim());
        target.setEmployerAddress(source.employerAddress().trim());
        target.setEmployerZipCode(source.employerZipCode().trim());
        target.setEmploymentFrom(source.employmentFrom());
        target.setEmploymentTo(source.employmentTo());
        target.setGrossCompensation(scale(source.grossCompensation()));
        target.setNonTaxableCompensation(scale(source.nonTaxableCompensation()));
        target.setTaxableCompensation(scale(source.taxableCompensation()));
        target.setTaxWithheld(scale(source.taxWithheld()));
    }

    private PreviousEmployerTaxDataDTO toPreviousEmployerDto(PreviousEmployerTaxData source) {
        return new PreviousEmployerTaxDataDTO(
                source.getId(), source.getEmployeeNo(), source.getTaxYear(), source.getEmployerName(),
                source.getEmployerTin(), source.getEmployerAddress(), source.getEmployerZipCode(),
                source.getEmploymentFrom(), source.getEmploymentTo(),
                source.getGrossCompensation(), source.getNonTaxableCompensation(),
                source.getTaxableCompensation(), source.getTaxWithheld(), source.getCreatedAt(),
                source.getUpdatedAt(), source.getCreatedBy(), source.getUpdatedBy());
    }

    private void validatePeraTaxCredit(PeraTaxCreditDTO request, int taxYear) {
        if (request == null) {
            throw new IllegalArgumentException("PERA tax-credit evidence is required.");
        }
        if (request.contributionAmount() == null || request.contributionAmount().signum() <= 0) {
            throw new IllegalArgumentException("PERA contribution amount must be greater than zero.");
        }
        if (request.certificateNo() == null || request.certificateNo().isBlank()) {
            throw new IllegalArgumentException("PERA tax credit certificate number is required.");
        }
        if (request.administratorName() == null || request.administratorName().isBlank()) {
            throw new IllegalArgumentException("PERA administrator name is required.");
        }
        if (request.certificateIssuedDate() == null) {
            throw new IllegalArgumentException("PERA certificate issue date is required.");
        }
        int issuedYear = request.certificateIssuedDate().getYear();
        if (issuedYear != taxYear && issuedYear != taxYear + 1) {
            throw new IllegalArgumentException(
                    "PERA certificate issue date must be within the tax year or the following year.");
        }
    }

    private PeraTaxCreditDTO toPeraTaxCreditDto(AnnualPeraTaxCredit source) {
        return new PeraTaxCreditDTO(
                source.getId(), source.getEmployeeNo(), source.getTaxYear(),
                source.getContributionAmount(), source.getCertifiedTaxCredit(),
                source.getCertificateNo(), source.getAdministratorName(),
                source.getCertificateIssuedDate(), source.getCreatedAt(), source.getUpdatedAt(),
                source.getCreatedBy(), source.getUpdatedBy());
    }

    private void requireNotFinalized(String employeeNo, int taxYear) {
        if (snapshotRepository
                .findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
                        employeeNo, taxYear, "FINALIZED").isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Annualization is finalized. Reopen it before making changes.");
        }
    }

    private String cleanRemarks(String remarks, String fieldName) {
        if (remarks == null || remarks.isBlank()) return null;
        String cleaned = remarks.trim();
        if (cleaned.length() > 500) {
            throw new IllegalArgumentException(fieldName + " must be 500 characters or fewer.");
        }
        return cleaned;
    }

    private String validateSubstitutedFilingAssessment(
            AnnualizationEmployeeDTO computed, Boolean qualified, String reason) {
        if (qualified == null) {
            throw new IllegalArgumentException(
                    "Complete the substituted-filing assessment before finalization.");
        }
        String cleanedReason = cleanRemarks(reason, "Substituted-filing assessment reason");
        if (cleanedReason == null) {
            throw new IllegalArgumentException("A substituted-filing assessment reason is required.");
        }
        if (qualified && !computed.previousEmployers().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The employee is not qualified for substituted filing because previous-employer compensation is recorded for the tax year.");
        }
        if (qualified && (computed.adjustmentAmount() == null
                || computed.adjustmentAmount().compareTo(ZERO) != 0)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The employee is not qualified for substituted filing because annual tax due does not equal total tax withheld after applicable tax credits.");
        }
        return cleanedReason;
    }

    private AnnualizationSnapshot toSnapshot(
            AnnualizationEmployeeDTO source, int version, String actor, String remarks,
            Boolean substitutedFilingQualified, String substitutedFilingReason) {
        AnnualizationSnapshot target = new AnnualizationSnapshot();
        target.setEmployeeNo(source.employeeNo()); target.setEmployeeName(source.employeeName());
        target.setDepartment(source.department()); target.setTaxYear(source.taxYear());
        AnnualizationTaxIdentityDTO identity = source.taxIdentity();
        target.setEmployerName(identity.employerName()); target.setEmployerTin(identity.employerTin());
        target.setEmployerBirBranchCode(identity.employerBirBranchCode());
        target.setEmployerRdoCode(identity.employerRdoCode()); target.setEmployerAddress(identity.employerAddress());
        target.setEmployerZipCode(identity.employerZipCode()); target.setEmployeeTin(identity.employeeTin());
        target.setEmployeeBirBranchCode(identity.employeeBirBranchCode());
        target.setEmployeeBirRegionCode(identity.employeeBirRegionCode());
        target.setEmployeeRdoCode(identity.employeeRdoCode()); target.setEmployeeAddress(identity.employeeAddress());
        target.setEmployeeZipCode(identity.employeeZipCode());
        target.setEmployeeLocalAddress(identity.employeeLocalAddress());
        target.setEmployeeLocalZipCode(identity.employeeLocalZipCode());
        target.setEmployeeBirthDate(identity.employeeBirthDate());
        target.setEmployeeContactNo(identity.employeeContactNo());
        target.setEmployeePosition(identity.employeePosition());
        target.setEmployeeLastName(identity.employeeLastName());
        target.setEmployeeFirstName(identity.employeeFirstName());
        target.setEmployeeMiddleName(identity.employeeMiddleName());
        target.setEmployeeExtensionName(identity.employeeExtensionName());
        target.setEmployeeNationality(identity.employeeNationality());
        target.setEmployeeEmploymentStatusCode(identity.employeeEmploymentStatusCode());
        target.setEmployeeEmploymentStatusName(identity.employeeEmploymentStatusName());
        target.setEmployeeEmploymentFrom(identity.employeeEmploymentFrom());
        target.setEmployeeEmploymentTo(identity.employeeEmploymentTo());
        target.setEmployeeBirSeparationReasonCode(identity.employeeBirSeparationReasonCode());
        target.setAnnualizationType(identity.employeeEmploymentTo() != null
                && identity.employeeEmploymentTo().isBefore(LocalDate.of(source.taxYear(), 12, 31))
                ? "SEPARATION" : "YEAR_END");
        target.setVersion(version); target.setStatus("FINALIZED");
        target.setPresentGrossCompensation(source.presentGrossCompensation());
        target.setPresentNonTaxableCompensation(source.presentNonTaxableCompensation());
        target.setPresentTaxableCompensation(source.presentTaxableCompensation());
        target.setPresentTaxWithheld(source.presentTaxWithheld());
        target.setPreviousGrossCompensation(source.previousGrossCompensation());
        target.setPreviousNonTaxableCompensation(source.previousNonTaxableCompensation());
        target.setPreviousTaxableCompensation(source.previousTaxableCompensation());
        target.setPreviousTaxWithheld(source.previousTaxWithheld());
        target.setGrossCompensation(source.grossCompensation());
        target.setNonTaxableCompensation(source.nonTaxableCompensation());
        target.setTaxableCompensation(source.taxableCompensation());
        target.setGsisContribution(source.gsisContribution());
        target.setPhilHealthContribution(source.philHealthContribution());
        target.setPagIbigContribution(source.pagIbigContribution());
        target.setTaxWithheld(source.taxWithheld()); target.setAnnualTaxDue(source.annualTaxDue());
        target.setPeraTaxCredit(source.peraTaxCredit());
        PeraTaxCreditDTO peraSource = source.peraTaxCreditSource();
        if (peraSource != null) {
            target.setPeraContributionAmount(peraSource.contributionAmount());
            target.setPeraCertifiedTaxCredit(peraSource.certifiedTaxCredit());
            target.setPeraCertificateNo(peraSource.certificateNo());
            target.setPeraAdministratorName(peraSource.administratorName());
            target.setPeraCertificateIssuedDate(peraSource.certificateIssuedDate());
        }
        target.setAdjustmentAmount(source.adjustmentAmount()); target.setAdjustmentType(source.adjustmentType());
        target.setFinalizedAt(LocalDateTime.now()); target.setFinalizedBy(actor);
        target.setFinalizationRemarks(remarks);
        target.setSubstitutedFilingQualified(substitutedFilingQualified);
        target.setSubstitutedFilingReason(substitutedFilingReason);
        target.setSubstitutedFilingReviewedAt(LocalDateTime.now());
        target.setSubstitutedFilingReviewedBy(actor);
        return target;
    }

    private AnnualizationSnapshotPeriod toSnapshotPeriod(Long snapshotId, AnnualizationPeriodDTO source) {
        AnnualizationSnapshotPeriod target = new AnnualizationSnapshotPeriod();
        target.setSnapshotId(snapshotId); target.setSalaryPeriodKey(source.salaryPeriodKey());
        target.setSalaryDate(source.salaryDate()); target.setGrossCompensation(source.grossCompensation());
        target.setNonTaxableCompensation(source.nonTaxableCompensation());
        target.setTaxableCompensation(source.taxableCompensation());
        target.setGsisContribution(source.gsisContribution());
        target.setPhilHealthContribution(source.philHealthContribution());
        target.setPagIbigContribution(source.pagIbigContribution());
        target.setTaxWithheld(source.taxWithheld());
        target.setMinimumWageEarner(source.minimumWageEarner());
        target.setMinimumWageRegionCode(source.minimumWageRegionCode());
        target.setMinimumWageDailyRate(source.minimumWageDailyRate());
        target.setMinimumWageMonthlyRate(source.minimumWageMonthlyRate());
        target.setMinimumWageLegalBasis(source.minimumWageLegalBasis());
        return target;
    }

    private AnnualizationSnapshotPreviousEmployer toSnapshotPrevious(
            Long snapshotId, PreviousEmployerTaxDataDTO source) {
        AnnualizationSnapshotPreviousEmployer target = new AnnualizationSnapshotPreviousEmployer();
        target.setSnapshotId(snapshotId); target.setEmployerName(source.employerName());
        target.setEmployerTin(source.employerTin()); target.setEmployerAddress(source.employerAddress());
        target.setEmployerZipCode(source.employerZipCode()); target.setEmploymentFrom(source.employmentFrom());
        target.setEmploymentTo(source.employmentTo()); target.setGrossCompensation(source.grossCompensation());
        target.setNonTaxableCompensation(source.nonTaxableCompensation());
        target.setTaxableCompensation(source.taxableCompensation());
        target.setTaxWithheld(source.taxWithheld());
        return target;
    }

    private List<AnnualizationSnapshotItem> toSnapshotItems(
            Long snapshotId, Bir2316CompensationItemsDTO source) {
        if (source == null) return List.of();
        List<AnnualizationSnapshotItem> items = new ArrayList<>();
        addSnapshotItem(items, snapshotId, "29", null, source.item29BasicSalaryMwe());
        addSnapshotItem(items, snapshotId, "30", null, source.item30HolidayPayMwe());
        addSnapshotItem(items, snapshotId, "31", null, source.item31OvertimePayMwe());
        addSnapshotItem(items, snapshotId, "32", null, source.item32NightShiftDifferentialMwe());
        addSnapshotItem(items, snapshotId, "33", null, source.item33HazardPayMwe());
        addSnapshotItem(items, snapshotId, "34", null, source.item34ThirteenthMonthAndOtherBenefits());
        addSnapshotItem(items, snapshotId, "35", null, source.item35DeMinimisBenefits());
        addSnapshotItem(items, snapshotId, "36", null, source.item36MandatoryContributionsAndUnionDues());
        addSnapshotItem(items, snapshotId, "37", source.item37OtherNonTaxableDescription(), source.item37OtherNonTaxable());
        addSnapshotItem(items, snapshotId, "38", null, source.item38TotalNonTaxable());
        addSnapshotItem(items, snapshotId, "39", null, source.item39BasicSalary());
        addSnapshotItem(items, snapshotId, "40", null, source.item40Representation());
        addSnapshotItem(items, snapshotId, "41", null, source.item41Transportation());
        addSnapshotItem(items, snapshotId, "42", null, source.item42CostOfLivingAllowance());
        addSnapshotItem(items, snapshotId, "43", null, source.item43FixedHousingAllowance());
        addSnapshotItem(items, snapshotId, "44A", source.item44AOtherRegularDescription(), source.item44AOtherRegular());
        addSnapshotItem(items, snapshotId, "44B", source.item44BOtherRegularDescription(), source.item44BOtherRegular());
        addSnapshotItem(items, snapshotId, "45", null, source.item45Commission());
        addSnapshotItem(items, snapshotId, "46", null, source.item46ProfitSharing());
        addSnapshotItem(items, snapshotId, "47", null, source.item47FeesIncludingDirectorsFees());
        addSnapshotItem(items, snapshotId, "48", null, source.item48TaxableThirteenthMonthBenefits());
        addSnapshotItem(items, snapshotId, "49", null, source.item49HazardPay());
        addSnapshotItem(items, snapshotId, "50", null, source.item50OvertimePay());
        addSnapshotItem(items, snapshotId, "51A", source.item51AOtherSupplementaryDescription(), source.item51AOtherSupplementary());
        addSnapshotItem(items, snapshotId, "51B", source.item51BOtherSupplementaryDescription(), source.item51BOtherSupplementary());
        addSnapshotItem(items, snapshotId, "52", null, source.item52TotalTaxable());
        return items;
    }

    private void addSnapshotItem(
            List<AnnualizationSnapshotItem> target, Long snapshotId,
            String itemNo, String description, BigDecimal amount) {
        AnnualizationSnapshotItem item = new AnnualizationSnapshotItem();
        item.setSnapshotId(snapshotId);
        item.setItemNo(itemNo);
        item.setDescription(description);
        item.setAmount(scale(amount));
        target.add(item);
    }

    private Bir2316CompensationItemsDTO fromSnapshotItems(Long snapshotId) {
        List<AnnualizationSnapshotItem> stored =
                snapshotItemRepository.findBySnapshotIdOrderByItemNoAsc(snapshotId);
        if (stored.isEmpty()) return null;
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        Map<String, Set<String>> descriptions = new LinkedHashMap<>();
        for (AnnualizationSnapshotItem item : stored) {
            amounts.put(item.getItemNo(), item.getAmount());
            if (item.getDescription() != null && !item.getDescription().isBlank()) {
                descriptions.computeIfAbsent(item.getItemNo(), ignored -> new LinkedHashSet<>())
                        .add(item.getDescription());
            }
        }
        return buildBir2316Items(amounts, descriptions);
    }

    private AnnualizationEmployeeDTO fromSnapshot(AnnualizationSnapshot source, boolean includeDetails) {
        List<AnnualizationPeriodDTO> periods = includeDetails
                ? snapshotPeriodRepository.findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(source.getId())
                    .stream().map(value -> new AnnualizationPeriodDTO(
                            value.getSalaryPeriodKey(), value.getSalaryDate(), value.getGrossCompensation(),
                            value.getNonTaxableCompensation(), value.getTaxableCompensation(),
                            value.getGsisContribution(), value.getPhilHealthContribution(),
                            value.getPagIbigContribution(), value.getTaxWithheld(),
                            value.getMinimumWageEarner(), value.getMinimumWageRegionCode(),
                            value.getMinimumWageDailyRate(), value.getMinimumWageMonthlyRate(),
                            value.getMinimumWageLegalBasis())).toList()
                : List.of();
        List<PreviousEmployerTaxDataDTO> previous = includeDetails
                ? snapshotPreviousRepository.findBySnapshotIdOrderByEmploymentFromAsc(source.getId())
                    .stream().map(value -> new PreviousEmployerTaxDataDTO(
                            value.getId(), source.getEmployeeNo(), source.getTaxYear(), value.getEmployerName(),
                            value.getEmployerTin(), value.getEmployerAddress(), value.getEmployerZipCode(),
                            value.getEmploymentFrom(), value.getEmploymentTo(),
                            value.getGrossCompensation(), value.getNonTaxableCompensation(),
                            value.getTaxableCompensation(), value.getTaxWithheld(),
                            null, null, null, null)).toList()
                : List.of();
        Bir2316CompensationItemsDTO compensationItems = includeDetails
                ? fromSnapshotItems(source.getId()) : null;
        return new AnnualizationEmployeeDTO(
                source.getEmployeeNo(), source.getEmployeeName(), source.getDepartment(), source.getTaxYear(),
                source.getPresentGrossCompensation(), source.getPresentNonTaxableCompensation(),
                source.getPresentTaxableCompensation(), source.getPresentTaxWithheld(),
                source.getPreviousGrossCompensation(), source.getPreviousNonTaxableCompensation(),
                source.getPreviousTaxableCompensation(), source.getPreviousTaxWithheld(),
                source.getGrossCompensation(), source.getNonTaxableCompensation(),
                source.getTaxableCompensation(), source.getGsisContribution(),
                source.getPhilHealthContribution(), source.getPagIbigContribution(), source.getTaxWithheld(),
                source.getAnnualTaxDue(), source.getPeraTaxCredit(), snapshotPeraSource(source),
                source.getAdjustmentAmount(), source.getAdjustmentType(),
                "FINALIZED", null, source.getVersion(), source.getFinalizedAt(), source.getFinalizedBy(),
                source.getSubstitutedFilingQualified(), source.getSubstitutedFilingReason(),
                source.getSubstitutedFilingReviewedAt(), source.getSubstitutedFilingReviewedBy(),
                source.getAdjustmentHeaderId(), source.getAdjustmentSalaryPeriodKey(),
                source.getAdjustmentAppliedAt(), source.getAdjustmentAppliedBy(),
                previous, periods, compensationItems,
                snapshotIdentity(source), readiness(
                        snapshotIdentity(source), source.getAnnualTaxDue(), previous,
                        List.of(), List.of()));
    }

    private void saveAudit(
            String employeeNo, int taxYear, String action, String oldStatus,
            String newStatus, String remarks, String actor) {
        AnnualizationAuditLog audit = new AnnualizationAuditLog();
        audit.setEmployeeNo(employeeNo); audit.setTaxYear(taxYear); audit.setAction(action);
        audit.setOldStatus(oldStatus); audit.setNewStatus(newStatus); audit.setRemarks(remarks);
        audit.setPerformedBy(actor); audit.setPerformedAt(LocalDateTime.now());
        auditLogRepository.save(audit);
    }

    private BigDecimal calculateAnnualTax(BigDecimal taxableCompensation,
                                          List<AnnualTaxBracket> brackets) {
        for (AnnualTaxBracket bracket : brackets) {
            boolean inRange = taxableCompensation.compareTo(bracket.incomeFrom()) >= 0
                    && (bracket.incomeTo() == null
                    || taxableCompensation.compareTo(bracket.incomeTo()) <= 0);
            if (inRange) {
                return scale(bracket.baseTax().add(
                        taxableCompensation.subtract(bracket.incomeFrom())
                                .multiply(bracket.excessRate())));
            }
        }
        return null;
    }

    private BigDecimal appliedPeraTaxCredit(
            BigDecimal annualTaxDue, BigDecimal taxWithheld, PeraTaxCreditDTO source) {
        if (annualTaxDue == null || source == null || source.certifiedTaxCredit() == null) {
            return ZERO;
        }
        BigDecimal remainingTax = annualTaxDue.subtract(taxWithheld);
        if (remainingTax.signum() <= 0) return ZERO;
        return scale(source.certifiedTaxCredit().min(remainingTax));
    }

    private AnnualizationEmployeeDTO withoutPeriods(AnnualizationEmployeeDTO source) {
        return new AnnualizationEmployeeDTO(
                source.employeeNo(), source.employeeName(), source.department(), source.taxYear(),
                source.presentGrossCompensation(), source.presentNonTaxableCompensation(),
                source.presentTaxableCompensation(), source.presentTaxWithheld(),
                source.previousGrossCompensation(), source.previousNonTaxableCompensation(),
                source.previousTaxableCompensation(), source.previousTaxWithheld(),
                source.grossCompensation(), source.nonTaxableCompensation(),
                source.taxableCompensation(), source.gsisContribution(),
                source.philHealthContribution(), source.pagIbigContribution(),
                source.taxWithheld(), source.annualTaxDue(), source.peraTaxCredit(),
                source.peraTaxCreditSource(), source.adjustmentAmount(),
                source.adjustmentType(), source.status(), source.reviewMessage(),
                source.snapshotVersion(), source.finalizedAt(), source.finalizedBy(),
                source.substitutedFilingQualified(), source.substitutedFilingReason(),
                source.substitutedFilingReviewedAt(), source.substitutedFilingReviewedBy(),
                source.adjustmentHeaderId(), source.adjustmentSalaryPeriodKey(),
                source.adjustmentAppliedAt(), source.adjustmentAppliedBy(),
                List.of(), List.of(), source.bir2316Items(),
                source.taxIdentity(), source.readiness());
    }

    private PeraTaxCreditDTO snapshotPeraSource(AnnualizationSnapshot source) {
        if (source.getPeraCertificateNo() == null || source.getPeraCertificateNo().isBlank()) {
            return null;
        }
        return new PeraTaxCreditDTO(
                null, source.getEmployeeNo(), source.getTaxYear(),
                source.getPeraContributionAmount(), source.getPeraCertifiedTaxCredit(),
                source.getPeraCertificateNo(), source.getPeraAdministratorName(),
                source.getPeraCertificateIssuedDate(), null, null, null, null);
    }

    private void validateYear(int year) {
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("A valid four-digit tax year is required.");
        }
    }

    private BigDecimal money(Double value) {
        return value == null ? ZERO : BigDecimal.valueOf(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private AnnualizationTaxIdentityDTO snapshotIdentity(AnnualizationSnapshot source) {
        return new AnnualizationTaxIdentityDTO(
                source.getEmployerName(), source.getEmployerTin(), source.getEmployerBirBranchCode(), source.getEmployerRdoCode(),
                source.getEmployerAddress(), source.getEmployerZipCode(), source.getEmployeeTin(),
                source.getEmployeeBirBranchCode(), source.getEmployeeBirRegionCode(),
                source.getEmployeeRdoCode(), source.getEmployeeAddress(), source.getEmployeeZipCode(),
                source.getEmployeeLocalAddress(), source.getEmployeeLocalZipCode(),
                source.getEmployeeBirthDate(), source.getEmployeeContactNo(),
                source.getEmployeePosition(), source.getEmployeeLastName(),
                source.getEmployeeFirstName(), source.getEmployeeMiddleName(),
                source.getEmployeeExtensionName(), source.getEmployeeNationality(),
                source.getEmployeeEmploymentStatusCode(), source.getEmployeeEmploymentStatusName(),
                source.getEmployeeEmploymentFrom(), source.getEmployeeEmploymentTo(),
                source.getEmployeeBirSeparationReasonCode());
    }

    private AnnualizationReadinessDTO readiness(
            AnnualizationTaxIdentityDTO identity, BigDecimal annualTaxDue,
            List<PreviousEmployerTaxDataDTO> previousEmployers,
            List<String> unclassifiedEarnings,
            List<String> itemizationIssues) {
        List<String> issues = new ArrayList<>();
        requireIdentity(identity == null ? null : identity.employerName(), "Employer name", issues);
        requireIdentity(identity == null ? null : identity.employerTin(), "Employer TIN", issues);
        requireIdentity(identity == null ? null : identity.employerBirBranchCode(), "Employer BIR branch code", issues);
        requireIdentity(identity == null ? null : identity.employerRdoCode(), "Employer RDO code", issues);
        requireIdentity(identity == null ? null : identity.employerAddress(), "Employer address", issues);
        requireIdentity(identity == null ? null : identity.employerZipCode(), "Employer ZIP code", issues);
        if (identity != null && identity.employerZipCode() != null
                && !identity.employerZipCode().trim().matches("[0-9]{4}")) {
            issues.add("Employer ZIP code must be four digits in Administrative System Setup");
        }
        requireIdentity(identity == null ? null : identity.employeeTin(), "Employee TIN", issues);
        requireIdentity(identity == null ? null : identity.employeeBirBranchCode(), "Employee BIR branch code", issues);
        requireIdentity(identity == null ? null : identity.employeeBirRegionCode(), "Employee BIR region number", issues);
        requireIdentity(identity == null ? null : identity.employeeRdoCode(), "Employee RDO code", issues);
        requireIdentity(identity == null ? null : identity.employeeAddress(), "Employee address", issues);
        requireIdentity(identity == null ? null : identity.employeeZipCode(), "Employee ZIP code", issues);
        if (identity != null && identity.employeeZipCode() != null
                && !identity.employeeZipCode().trim().matches("[0-9]{4}")) {
            issues.add("Employee ZIP code must be four digits in HR Management Personal Data");
        }
        if (identity == null || identity.employeeBirthDate() == null) {
            issues.add("Employee birth date is missing");
        }
        requireIdentity(identity == null ? null : identity.employeeContactNo(),
                "Employee contact number", issues);
        requireIdentity(identity == null ? null : identity.employeePosition(),
                "Employee position", issues);
        if (identity == null || identity.employeeEmploymentFrom() == null
                || identity.employeeEmploymentTo() == null) {
            issues.add("Employee employment dates are missing for BIR Form 2316");
        } else if (identity.employeeEmploymentFrom().isAfter(identity.employeeEmploymentTo())) {
            issues.add("Employee employment dates are invalid for BIR Form 2316");
        } else if (identity.employeeEmploymentFrom().getYear() != identity.employeeEmploymentTo().getYear()) {
            issues.add("Employee employment dates must be within one tax year for BIR Form 2316");
        }
        if (identity != null && identity.employeeEmploymentTo() != null
                && identity.employeeEmploymentTo().isBefore(LocalDate.of(
                        identity.employeeEmploymentTo().getYear(), 12, 31))
                && (identity.employeeBirSeparationReasonCode() == null
                        || !BIR_SEPARATION_REASON_CODES.contains(identity.employeeBirSeparationReasonCode()))) {
            issues.add("Map the employee's HR separation nature to BIR reason T, TR, R, or D in Administrative");
        }
        for (PreviousEmployerTaxDataDTO previous : previousEmployers) {
            String employer = previous.employerName() == null || previous.employerName().isBlank()
                    ? "Previous employer" : previous.employerName();
            requireIdentity(previous.employerAddress(), employer + " address", issues);
            requireIdentity(previous.employerZipCode(), employer + " ZIP code", issues);
        }
        unclassifiedEarnings.forEach(value -> issues.add(
                "BIR 2316 classification is missing for " + value));
        issues.addAll(itemizationIssues);
        if (annualTaxDue == null) issues.add("Annual tax calculation");
        return new AnnualizationReadinessDTO(issues.isEmpty(), issues);
    }

    private Bir2316CompensationItemsDTO buildBir2316Items(
            Map<String, BigDecimal> source,
            Map<String, Set<String>> descriptions) {
        BigDecimal item29 = birAmount(source, "29");
        BigDecimal item30 = birAmount(source, "30");
        BigDecimal item31 = birAmount(source, "31");
        BigDecimal item32 = birAmount(source, "32");
        BigDecimal item33 = birAmount(source, "33");
        BigDecimal item34 = birAmount(source, "34");
        BigDecimal item35 = birAmount(source, "35");
        BigDecimal item36 = birAmount(source, "36");
        BigDecimal item37 = birAmount(source, "37");
        BigDecimal item38 = scale(item29.add(item30).add(item31).add(item32).add(item33)
                .add(item34).add(item35).add(item36).add(item37));

        // Basic salary buckets are already net of item 36 at the payroll-period level.
        // Period-level allocation is required for years that change MWE status mid-year.
        BigDecimal item39 = birAmount(source, "39");
        BigDecimal item40 = birAmount(source, "40");
        BigDecimal item41 = birAmount(source, "41");
        BigDecimal item42 = birAmount(source, "42");
        BigDecimal item43 = birAmount(source, "43");
        BigDecimal item44A = birAmount(source, "44A");
        BigDecimal item44B = birAmount(source, "44B");
        BigDecimal item45 = birAmount(source, "45");
        BigDecimal item46 = birAmount(source, "46");
        BigDecimal item47 = birAmount(source, "47");
        BigDecimal item48 = birAmount(source, "48");
        BigDecimal item49 = birAmount(source, "49");
        BigDecimal item50 = birAmount(source, "50");
        BigDecimal item51A = birAmount(source, "51A");
        BigDecimal item51B = birAmount(source, "51B");
        BigDecimal item52 = scale(item39.add(item40).add(item41).add(item42).add(item43)
                .add(item44A).add(item44B).add(item45).add(item46).add(item47)
                .add(item48).add(item49).add(item50).add(item51A).add(item51B));

        return new Bir2316CompensationItemsDTO(
                item29, item30, item31, item32, item33, item34, item35, item36,
                birDescription(descriptions, "37"), item37, item38,
                item39, item40, item41, item42, item43,
                birDescription(descriptions, "44A"), item44A,
                birDescription(descriptions, "44B"), item44B,
                item45, item46, item47, item48, item49, item50,
                birDescription(descriptions, "51A"), item51A,
                birDescription(descriptions, "51B"), item51B, item52);
    }

    private BigDecimal birAmount(Map<String, BigDecimal> source, String itemNo) {
        return scale(source.getOrDefault(itemNo, ZERO));
    }

    private String birDescription(Map<String, Set<String>> descriptions, String itemNo) {
        Set<String> values = descriptions.get(itemNo);
        if (values == null || values.isEmpty()) return null;
        String joined = String.join(", ", values);
        return joined.length() <= 500 ? joined : joined.substring(0, 500);
    }

    private String normalizeBirItemNo(String value) {
        return value == null || value.isBlank()
                ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private void requireIdentity(String value, String label, List<String> issues) {
        if (value == null || value.isBlank()) issues.add(label + " is missing");
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }

    private record PeriodKey(String employeeNo, String salaryPeriodKey) {}
    private record Computation(boolean taxTableConfigured, List<AnnualizationEmployeeDTO> records) {}
    record AnnualTaxBracket(
            BigDecimal incomeFrom,
            BigDecimal incomeTo,
            BigDecimal baseTax,
            BigDecimal excessRate) {}
    record AnnualizationEmployerIdentity(
            String employerName, String tin, String birBranchCode, String rdoCode,
            String address, String zipCode) {}
    record AnnualizationEmployeeIdentity(
            String employeeNo, String tin, String rdoCode, String birBranchCode,
            String birRegionCode, String address, String zipCode,
            String localAddress, String localZipCode, LocalDate birthDate,
            String contactNo, Long jobPositionId,
            String lastName, String firstName, String middleName, String extensionName,
            String nationality, Long natureOfAppointmentId, Integer natureOfSeparationId,
            LocalDate employmentFrom, LocalDate employmentTo) {}
    record JobPositionIdentity(Long jobPositionId, String jobPositionName) {}
    record NatureOfAppointmentIdentity(
            Long natureOfAppointmentId, String code, String nature, Boolean isContractual,
            String birEmploymentStatusCode) {}
    record NatureOfSeparationIdentity(
            Long natureOfSeparationId, String code, String nature,
            String birSeparationReasonCode) {}
    record EmployeeMweStatusSource(
            Long id, String employeeNo, LocalDate effectiveFrom, LocalDate effectiveTo,
            Boolean minimumWageEarner, String regionCode, String remarks,
            String createdBy, LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {}
    record MinimumWageRateSource(
            Long id, String regionCode, String regionName,
            LocalDate effectiveFrom, LocalDate effectiveTo,
            BigDecimal dailyRate, BigDecimal monthlyRate,
            String legalBasis, String remarks) {}
    private enum PermissionAction { ACCESS, ADD, EDIT, DELETE, FINALIZE }

    record EffectivePermission(
            boolean administrator, boolean canAccess, boolean canAdd, boolean canEdit,
            boolean canDelete, boolean canFinalize) {
        boolean allows(PermissionAction action) {
            if (!canAccess) return false;
            return switch (action) {
                case ACCESS -> true;
                case ADD -> canAdd;
                case EDIT -> canEdit;
                case DELETE -> canDelete;
                case FINALIZE -> canFinalize;
            };
        }
    }

    private final class PeriodAccumulator {
        private final String employeeNo;
        private final String employeeName;
        private final String department;
        private final String salaryPeriodKey;
        private final LocalDate salaryDate;
        private BigDecimal gross;
        private BigDecimal taxableEarnings = ZERO;
        private BigDecimal gsis = ZERO;
        private BigDecimal philHealth = ZERO;
        private BigDecimal pagIbig = ZERO;
        private BigDecimal taxWithheld;
        private final Set<String> unclassifiedEarnings = new LinkedHashSet<>();
        private final Map<String, BigDecimal> birItems = new LinkedHashMap<>();
        private final Map<String, Set<String>> birItemDescriptions = new LinkedHashMap<>();
        private final Map<String, BigDecimal> taxableBirItems = new LinkedHashMap<>();
        private final Set<String> mweIssues = new LinkedHashSet<>();
        private Boolean minimumWageEarner;
        private String minimumWageRegionCode;
        private BigDecimal minimumWageDailyRate;
        private BigDecimal minimumWageMonthlyRate;
        private String minimumWageLegalBasis;

        private PeriodAccumulator(PayrollDetail detail) {
            employeeNo = detail.getEmployeeNo();
            employeeName = detail.getEmployeeName();
            department = detail.getDepartment();
            salaryPeriodKey = detail.getSalaryPeriodKey();
            salaryDate = detail.getSalaryDate();
            gross = money(detail.getGrossAmount());
            // Withholding is aggregated from the persisted deduction line so the
            // same source also covers posted WTX adjustment lines without duplication.
            taxWithheld = ZERO;
        }

        private AnnualizationPeriodDTO toDto() {
            BigDecimal taxable = taxableEarnings;
            if (Boolean.TRUE.equals(minimumWageEarner)) {
                for (String item : List.of("29", "30", "31", "32", "33")) {
                    taxable = taxable.subtract(taxableBirItems.getOrDefault(item, ZERO));
                }
            }
            taxable = taxable.subtract(birItems.getOrDefault("36", ZERO));
            if (taxable.signum() < 0) taxable = ZERO;
            taxable = scale(taxable);
            BigDecimal nonTaxable = gross.subtract(taxable);
            if (nonTaxable.signum() < 0) nonTaxable = ZERO;
            return new AnnualizationPeriodDTO(
                    salaryPeriodKey,
                    salaryDate,
                    scale(gross),
                    scale(nonTaxable),
                    taxable,
                    scale(gsis),
                     scale(philHealth),
                     scale(pagIbig),
                    scale(taxWithheld),
                    minimumWageEarner,
                    minimumWageRegionCode,
                    minimumWageDailyRate,
                    minimumWageMonthlyRate,
                    minimumWageLegalBasis);
        }

        private void resolveMinimumWage(
                List<EmployeeMweStatusSource> statuses,
                List<MinimumWageRateSource> rates) {
            if (salaryDate == null) {
                mweIssues.add("Payroll date is missing for salary period " + salaryPeriodKey);
                return;
            }
            List<EmployeeMweStatusSource> matchingStatuses = statuses.stream()
                    .filter(status -> covers(status.effectiveFrom(), status.effectiveTo(), salaryDate))
                    .toList();
            if (matchingStatuses.isEmpty()) {
                mweIssues.add("MWE status is missing for salary period " + salaryPeriodKey
                        + " (" + salaryDate + ")");
                return;
            }
            if (matchingStatuses.size() > 1) {
                mweIssues.add("MWE status overlaps for salary period " + salaryPeriodKey
                        + " (" + salaryDate + ")");
                return;
            }
            EmployeeMweStatusSource status = matchingStatuses.get(0);
            minimumWageEarner = status.minimumWageEarner();
            if (minimumWageEarner == null) {
                mweIssues.add("MWE status is not selected for salary period " + salaryPeriodKey);
                return;
            }
            BigDecimal mweCompensation = birItemsForMinimumWage();
            if (!minimumWageEarner) {
                if (mweCompensation.signum() != 0) {
                    mweIssues.add("BIR items 29-33 contain compensation but the employee is not MWE for salary period "
                            + salaryPeriodKey);
                }
                return;
            }
            if (status.regionCode() == null || status.regionCode().isBlank()) {
                mweIssues.add("MWE region is missing for salary period " + salaryPeriodKey);
                return;
            }
            minimumWageRegionCode = status.regionCode().trim().toUpperCase(Locale.ROOT);
            List<MinimumWageRateSource> matchingRates = rates.stream()
                    .filter(rate -> rate.regionCode() != null
                            && minimumWageRegionCode.equalsIgnoreCase(rate.regionCode().trim()))
                    .filter(rate -> covers(rate.effectiveFrom(), rate.effectiveTo(), salaryDate))
                    .toList();
            if (matchingRates.isEmpty()) {
                mweIssues.add("No statutory minimum-wage rate covers " + minimumWageRegionCode
                        + " for salary period " + salaryPeriodKey + " (" + salaryDate + ")");
                return;
            }
            if (matchingRates.size() > 1) {
                mweIssues.add("Statutory minimum-wage rates overlap for " + minimumWageRegionCode
                        + " on " + salaryDate);
                return;
            }
            MinimumWageRateSource rate = matchingRates.get(0);
            minimumWageDailyRate = rate.dailyRate() == null ? null : scale(rate.dailyRate());
            minimumWageMonthlyRate = rate.monthlyRate() == null ? null : scale(rate.monthlyRate());
            minimumWageLegalBasis = rate.legalBasis();
            if (minimumWageDailyRate == null || minimumWageDailyRate.signum() <= 0
                    || minimumWageMonthlyRate == null || minimumWageMonthlyRate.signum() <= 0
                    || minimumWageLegalBasis == null || minimumWageLegalBasis.isBlank()) {
                mweIssues.add("Statutory minimum-wage rate is incomplete for "
                        + minimumWageRegionCode + " on " + salaryDate);
            }
        }

        private BigDecimal birItemsForMinimumWage() {
            BigDecimal total = ZERO;
            for (String item : List.of("29", "30", "31", "32", "33")) {
                total = total.add(birItems.getOrDefault(item, ZERO));
            }
            return total;
        }

        private Map<String, BigDecimal> birItemsForAnnualization() {
            Map<String, BigDecimal> adjusted = new LinkedHashMap<>(birItems);
            BigDecimal remainingContribution = adjusted.getOrDefault("36", ZERO);
            String primaryBasicItem = Boolean.TRUE.equals(minimumWageEarner) ? "29" : "39";
            remainingContribution = reduceBasicItem(
                    adjusted, primaryBasicItem, remainingContribution);
            String secondaryBasicItem = "29".equals(primaryBasicItem) ? "39" : "29";
            remainingContribution = reduceBasicItem(
                    adjusted, secondaryBasicItem, remainingContribution);
            if (remainingContribution.signum() > 0) {
                mweIssues.add("Basic salary is lower than mandatory contributions and union dues for salary period "
                        + salaryPeriodKey);
            }
            return adjusted;
        }

        private BigDecimal reduceBasicItem(
                Map<String, BigDecimal> adjusted, String itemNo, BigDecimal deduction) {
            if (deduction.signum() <= 0) return ZERO;
            BigDecimal basic = adjusted.getOrDefault(itemNo, ZERO);
            BigDecimal applied = basic.min(deduction);
            adjusted.put(itemNo, basic.subtract(applied));
            return deduction.subtract(applied);
        }

        private boolean covers(LocalDate effectiveFrom, LocalDate effectiveTo, LocalDate date) {
            return effectiveFrom != null && !date.isBefore(effectiveFrom)
                    && (effectiveTo == null || !date.isAfter(effectiveTo));
        }

        private void addTaxableBirItem(String itemNo, BigDecimal amount) {
            String item = normalizeBirItemNo(itemNo);
            if (item != null) taxableBirItems.merge(item, amount, BigDecimal::add);
        }

        private void addBirItem(String itemNo, String description, BigDecimal amount) {
            String item = normalizeBirItemNo(itemNo);
            if (item == null) return;
            birItems.merge(item, amount, BigDecimal::add);
            if (description != null && !description.isBlank()) {
                birItemDescriptions.computeIfAbsent(item, ignored -> new LinkedHashSet<>())
                        .add(description.trim());
            }
        }
    }

    private final class EmployeeAccumulator {
        private final String employeeNo;
        private final String employeeName;
        private final String department;
        private final List<AnnualizationPeriodDTO> periods = new ArrayList<>();
        private final Set<String> unclassifiedEarnings = new LinkedHashSet<>();
        private final Set<String> mweIssues = new LinkedHashSet<>();
        private final Map<String, BigDecimal> birItems = new LinkedHashMap<>();
        private final Map<String, Set<String>> birItemDescriptions = new LinkedHashMap<>();

        private EmployeeAccumulator(String employeeNo, String employeeName, String department) {
            this.employeeNo = employeeNo;
            this.employeeName = employeeName;
            this.department = department;
        }

        private void add(
                AnnualizationPeriodDTO period,
                Set<String> missingClassifications,
                Map<String, BigDecimal> periodItems,
                Map<String, Set<String>> periodDescriptions,
                Set<String> periodMweIssues) {
            periods.add(period);
            unclassifiedEarnings.addAll(missingClassifications);
            periodItems.forEach((item, amount) -> birItems.merge(item, amount, BigDecimal::add));
            periodDescriptions.forEach((item, values) ->
                    birItemDescriptions.computeIfAbsent(item, ignored -> new LinkedHashSet<>())
                            .addAll(values));
            mweIssues.addAll(periodMweIssues);
        }

        private AnnualizationEmployeeDTO toDto(
                int taxYear, List<AnnualTaxBracket> brackets, boolean taxTableConfigured,
                AnnualizationEmployerIdentity employerIdentity,
                AnnualizationEmployeeIdentity employeeIdentity,
                Map<Long, String> jobPositions,
                Map<Long, NatureOfAppointmentIdentity> appointmentNatures,
                Map<Long, NatureOfSeparationIdentity> separationNatures,
                List<PreviousEmployerTaxDataDTO> previousEmployers,
                PeraTaxCreditDTO peraTaxCreditSource) {
            periods.sort(Comparator.comparing(AnnualizationPeriodDTO::salaryDate)
                    .thenComparing(AnnualizationPeriodDTO::salaryPeriodKey));
            BigDecimal presentGross = sum(periods, AnnualizationPeriodDTO::grossCompensation);
            BigDecimal presentNonTaxable = sum(periods, AnnualizationPeriodDTO::nonTaxableCompensation);
            BigDecimal presentTaxable = sum(periods, AnnualizationPeriodDTO::taxableCompensation);
            BigDecimal gsis = sum(periods, AnnualizationPeriodDTO::gsisContribution);
            BigDecimal philHealth = sum(periods, AnnualizationPeriodDTO::philHealthContribution);
            BigDecimal pagIbig = sum(periods, AnnualizationPeriodDTO::pagIbigContribution);
            BigDecimal presentWithheld = sum(periods, AnnualizationPeriodDTO::taxWithheld);
            BigDecimal previousGross = sumPrevious(
                    previousEmployers, PreviousEmployerTaxDataDTO::grossCompensation);
            BigDecimal previousNonTaxable = sumPrevious(
                    previousEmployers, PreviousEmployerTaxDataDTO::nonTaxableCompensation);
            BigDecimal previousTaxable = sumPrevious(
                    previousEmployers, PreviousEmployerTaxDataDTO::taxableCompensation);
            BigDecimal previousWithheld = sumPrevious(
                    previousEmployers, PreviousEmployerTaxDataDTO::taxWithheld);
            BigDecimal gross = scale(presentGross.add(previousGross));
            BigDecimal nonTaxable = scale(presentNonTaxable.add(previousNonTaxable));
            BigDecimal taxable = scale(presentTaxable.add(previousTaxable));
            BigDecimal withheld = scale(presentWithheld.add(previousWithheld));
            Bir2316CompensationItemsDTO compensationItems = buildBir2316Items(
                    birItems, birItemDescriptions);

            List<String> itemizationIssues = new ArrayList<>();
            itemizationIssues.addAll(mweIssues);
            if (compensationItems.item39BasicSalary().signum() < 0) {
                itemizationIssues.add(
                        "BIR item 39 basic salary is lower than item 36 mandatory contributions and union dues");
            }
            if (unclassifiedEarnings.isEmpty()) {
                if (compensationItems.item38TotalNonTaxable().compareTo(presentNonTaxable) != 0) {
                    itemizationIssues.add("BIR item 38 does not reconcile with present non-taxable compensation");
                }
                if (compensationItems.item52TotalTaxable().compareTo(presentTaxable) != 0) {
                    itemizationIssues.add("BIR item 52 does not reconcile with present taxable compensation");
                }
            }

            BigDecimal annualTaxDue = taxTableConfigured ? calculateAnnualTax(taxable, brackets) : null;
            NatureOfAppointmentIdentity appointmentNature = employeeIdentity == null
                    || employeeIdentity.natureOfAppointmentId() == null ? null
                    : appointmentNatures.get(employeeIdentity.natureOfAppointmentId());
            NatureOfSeparationIdentity separationNature = employeeIdentity == null
                    || employeeIdentity.natureOfSeparationId() == null ? null
                    : separationNatures.get(employeeIdentity.natureOfSeparationId().longValue());
            String birEmploymentStatusCode = appointmentNature == null
                    || appointmentNature.birEmploymentStatusCode() == null ? null
                    : appointmentNature.birEmploymentStatusCode().trim().toUpperCase(Locale.ROOT);
            AnnualizationTaxIdentityDTO taxIdentity = new AnnualizationTaxIdentityDTO(
                    employerIdentity == null ? null : employerIdentity.employerName(),
                    employerIdentity == null ? null : employerIdentity.tin(),
                    employerIdentity == null ? null : employerIdentity.birBranchCode(),
                    employerIdentity == null ? null : employerIdentity.rdoCode(),
                    employerIdentity == null ? null : employerIdentity.address(),
                    employerIdentity == null ? null : employerIdentity.zipCode(),
                    employeeIdentity == null ? null : employeeIdentity.tin(),
                    employeeIdentity == null ? null : employeeIdentity.birBranchCode(),
                    employeeIdentity == null ? null : employeeIdentity.birRegionCode(),
                    employeeIdentity == null ? null : employeeIdentity.rdoCode(),
                    employeeIdentity == null ? null : employeeIdentity.address(),
                    employeeIdentity == null ? null : employeeIdentity.zipCode(),
                    employeeIdentity == null ? null : employeeIdentity.localAddress(),
                    employeeIdentity == null ? null : employeeIdentity.localZipCode(),
                    employeeIdentity == null ? null : employeeIdentity.birthDate(),
                    employeeIdentity == null ? null : employeeIdentity.contactNo(),
                    employeeIdentity == null || employeeIdentity.jobPositionId() == null
                            ? null : jobPositions.get(employeeIdentity.jobPositionId()),
                    employeeIdentity == null ? null : employeeIdentity.lastName(),
                    employeeIdentity == null ? null : employeeIdentity.firstName(),
                    employeeIdentity == null ? null : employeeIdentity.middleName(),
                    employeeIdentity == null ? null : employeeIdentity.extensionName(),
                    employeeIdentity == null ? null : employeeIdentity.nationality(),
                    birEmploymentStatusCode,
                    birEmploymentStatusName(birEmploymentStatusCode),
                    employeeIdentity == null ? null : employeeIdentity.employmentFrom(),
                    employeeIdentity == null ? null : employeeIdentity.employmentTo(),
                    separationNature == null ? null : separationNature.birSeparationReasonCode());
            AnnualizationReadinessDTO readiness = readiness(
                    taxIdentity, annualTaxDue, previousEmployers,
                    List.copyOf(unclassifiedEarnings), itemizationIssues);
            String status = readiness.ready() ? "COMPUTED" : "FOR_REVIEW";
            String reviewMessage = readiness.ready() ? null : String.join("; ", readiness.issues());
            BigDecimal peraTaxCredit = appliedPeraTaxCredit(
                    annualTaxDue, withheld, peraTaxCreditSource);
            BigDecimal adjustment = annualTaxDue == null ? null
                    : scale(annualTaxDue.subtract(withheld).subtract(peraTaxCredit));
            String adjustmentType = adjustment == null ? null
                    : adjustment.signum() > 0 ? "ADDITIONAL_WITHHOLDING"
                    : adjustment.signum() < 0 ? "REFUND" : "NO_ADJUSTMENT";

            return new AnnualizationEmployeeDTO(
                    employeeNo, employeeName, department, taxYear,
                    presentGross, presentNonTaxable, presentTaxable, presentWithheld,
                    previousGross, previousNonTaxable, previousTaxable, previousWithheld,
                    scale(gross), scale(nonTaxable), scale(taxable),
                    scale(gsis), scale(philHealth), scale(pagIbig), scale(withheld),
                    annualTaxDue, peraTaxCredit, peraTaxCreditSource,
                    adjustment, adjustmentType, status, reviewMessage,
                    null, null, null,
                    null, null, null, null,
                    null, null, null, null,
                    List.copyOf(previousEmployers),
                    List.copyOf(periods), compensationItems, taxIdentity, readiness);
        }

        private BigDecimal sum(List<AnnualizationPeriodDTO> values,
                               java.util.function.Function<AnnualizationPeriodDTO, BigDecimal> mapper) {
            return scale(values.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        private BigDecimal sumPrevious(
                List<PreviousEmployerTaxDataDTO> values,
                java.util.function.Function<PreviousEmployerTaxDataDTO, BigDecimal> mapper) {
            return scale(values.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
    }
}
