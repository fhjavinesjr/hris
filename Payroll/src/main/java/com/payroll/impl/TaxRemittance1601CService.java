package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.TaxRemittance1601CMonthDTO;
import com.payroll.dtos.TaxRemittance1601CRequest;
import com.payroll.dtos.TaxRemittance1601CReviewDTO;
import com.payroll.dtos.TaxRemittance1601CVoidRequest;
import com.payroll.entitymodels.PayrollStatus;
import com.payroll.entitymodels.TaxRemittance1601C;
import com.payroll.entitymodels.TaxRemittance1601CAudit;
import com.payroll.repositories.PayrollAdjustmentLineRepository;
import com.payroll.repositories.PayrollDetailDeductionRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.repositories.TaxRemittance1601CAuditRepository;
import com.payroll.repositories.TaxRemittance1601CRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TaxRemittance1601CService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final TaxRemittance1601CRepository repository;
    private final TaxRemittance1601CAuditRepository auditRepository;
    private final PayrollDetailRepository detailRepository;
    private final PayrollDetailDeductionRepository deductionRepository;
    private final PayrollAdjustmentLineRepository adjustmentRepository;
    private final RestTemplate restTemplate;
    private final SystemConfigRuntimeResolver configResolver;

    @Value("${hris.services.administrative.url:http://localhost:8082}")
    private String administrativeUrl;

    public TaxRemittance1601CService(
            TaxRemittance1601CRepository repository,
            TaxRemittance1601CAuditRepository auditRepository,
            PayrollDetailRepository detailRepository,
            PayrollDetailDeductionRepository deductionRepository,
            PayrollAdjustmentLineRepository adjustmentRepository,
            @Qualifier("payrollRestTemplate") RestTemplate restTemplate,
            SystemConfigRuntimeResolver configResolver) {
        this.repository = repository;
        this.auditRepository = auditRepository;
        this.detailRepository = detailRepository;
        this.deductionRepository = deductionRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.restTemplate = restTemplate;
        this.configResolver = configResolver;
    }

    @Transactional(readOnly = true)
    public TaxRemittance1601CReviewDTO review(int year, String authorizationHeader) {
        validateYear(year);
        authorize(authorizationHeader, false);
        Map<Integer, BigDecimal> expected = expectedWithholding(year);
        Map<Integer, TaxRemittance1601C> recorded = new HashMap<>();
        repository.findByTaxYearOrderByTaxMonthAsc(year)
                .forEach(value -> recorded.put(value.getTaxMonth(), value));
        List<TaxRemittance1601CMonthDTO> months = new ArrayList<>();
        List<String> issues = new ArrayList<>();
        BigDecimal expectedTotal = ZERO;
        BigDecimal withheldTotal = ZERO;
        BigDecimal remittedTotal = ZERO;
        for (int month = 1; month <= 12; month++) {
            BigDecimal payroll = expected.getOrDefault(month, ZERO);
            TaxRemittance1601C value = recorded.get(month);
            expectedTotal = expectedTotal.add(payroll);
            boolean confirmed = value != null && "CONFIRMED".equals(value.getStatus());
            BigDecimal actual = value == null ? ZERO : value.getTaxesWithheld();
            boolean matches = confirmed && payroll.compareTo(actual) == 0;
            if (payroll.signum() != 0 && !confirmed) {
                issues.add("Month " + month + " has payroll withholding but no confirmed 1601-C record.");
            } else if (value != null && "DRAFT".equals(value.getStatus())) {
                issues.add("Month " + month + " has an unconfirmed 1601-C draft.");
            } else if (confirmed && !matches) {
                issues.add("Month " + month + " confirmed 1601-C withholding differs from locked payroll.");
            }
            if (confirmed) {
                withheldTotal = withheldTotal.add(actual);
                remittedTotal = remittedTotal.add(value.getAmountRemitted());
                BigDecimal reportedTotal = actual.add(value.getAdjustment()).add(value.getPenalties());
                if (reportedTotal.compareTo(value.getAmountRemitted()) != 0) {
                    issues.add("Month " + month + " remitted amount does not reconcile to withholding, adjustment, and penalties.");
                }
            }
            months.add(new TaxRemittance1601CMonthDTO(
                    year, month, payroll, value == null ? null : actual,
                    value == null ? null : value.getAdjustment(),
                    value == null ? null : value.getPenalties(),
                    value == null ? null : value.getAmountRemitted(),
                    value == null ? null : value.getFilingDate(),
                    value == null ? null : value.getRemittanceDate(),
                    value == null ? null : value.getRemittanceChannel(),
                    value == null ? null : value.getReferenceNo(),
                    value == null ? null : value.getRemarks(),
                    value == null ? null : value.getEvidenceReference(),
                    value == null ? "MISSING" : value.getStatus(),
                    value == null ? null : value.getUpdatedBy(),
                    value == null ? null : value.getUpdatedAt(), matches));
        }
        return new TaxRemittance1601CReviewDTO(year, money(expectedTotal), money(withheldTotal),
                money(remittedTotal), issues.isEmpty(), List.copyOf(issues), List.copyOf(months));
    }

    @Transactional
    public TaxRemittance1601CMonthDTO save(
            int year, int month, TaxRemittance1601CRequest request,
            String actor, String authorizationHeader) {
        validateYear(year);
        if (month < 1 || month > 12) throw new IllegalArgumentException("Tax month must be 1 through 12.");
        authorize(authorizationHeader, true);
        if (request == null) throw new IllegalArgumentException("1601-C remittance details are required.");
        String status = trim(request.status(), 20);
        if (!"DRAFT".equals(status) && !"CONFIRMED".equals(status)) {
            throw new IllegalArgumentException("1601-C status must be DRAFT or CONFIRMED.");
        }
        if (request.taxesWithheld() == null || request.amountRemitted() == null) {
            throw new IllegalArgumentException("Taxes withheld and amount remitted are required.");
        }
        BigDecimal taxesWithheld = nonNegative(request.taxesWithheld(), "Taxes withheld");
        BigDecimal adjustment = money(request.adjustment());
        BigDecimal penalties = nonNegative(request.penalties(), "Penalties");
        BigDecimal remitted = nonNegative(request.amountRemitted(), "Amount remitted");
        String channel = trim(request.remittanceChannel(), 100);
        String reference = trim(request.referenceNo(), 100);
        String remarks = trim(request.remarks(), 500);
        String evidence = trim(request.evidenceReference(), 255);
        if ("CONFIRMED".equals(status)
                && (request.filingDate() == null || request.remittanceDate() == null
                || channel == null || channel.isBlank()
                || reference == null || reference.isBlank())) {
            throw new IllegalArgumentException(
                    "Confirmed 1601-C records require filing date, remittance date, remittance channel, and reference number.");
        }
        if ("CONFIRMED".equals(status)
                && remitted.compareTo(taxesWithheld.add(adjustment).add(penalties)) != 0) {
            throw new IllegalArgumentException(
                    "Amount remitted must equal taxes withheld plus adjustment and penalties.");
        }
        String officer = trim(actor, 100);
        if (officer == null || officer.isBlank()) throw new AccessDeniedException("Officer identity is required.");
        TaxRemittance1601C value = repository.findByTaxYearAndTaxMonth(year, month)
                .orElseGet(TaxRemittance1601C::new);
        String previous = value.getId() == null ? null : auditValues(value);
        String previousStatus = value.getStatus();
        if (("CONFIRMED".equals(previousStatus) || "VOIDED".equals(previousStatus))
                && (remarks == null || remarks.isBlank())) {
            throw new IllegalArgumentException(
                    "Correcting a confirmed or voided 1601-C record requires a reason in remarks.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (value.getId() == null) {
            value.setTaxYear(year);
            value.setTaxMonth(month);
            value.setRecordedBy(officer);
            value.setRecordedAt(now);
        }
        value.setTaxesWithheld(taxesWithheld);
        value.setAdjustment(adjustment);
        value.setPenalties(penalties);
        value.setAmountRemitted(remitted);
        value.setFilingDate(request.filingDate());
        value.setRemittanceDate(request.remittanceDate());
        value.setRemittanceChannel(channel);
        value.setReferenceNo(reference);
        value.setRemarks(remarks);
        value.setEvidenceReference(evidence);
        value.setStatus(status);
        value.setUpdatedBy(officer);
        value.setUpdatedAt(now);
        TaxRemittance1601C saved = repository.save(value);
        TaxRemittance1601CAudit audit = new TaxRemittance1601CAudit();
        audit.setTaxYear(year);
        audit.setTaxMonth(month);
        audit.setAction(previousStatus == null ? "CREATED"
                : "VOIDED".equals(previousStatus) ? "REACTIVATED" : "UPDATED");
        audit.setPreviousValues(previous);
        audit.setNewValues(auditValues(saved));
        audit.setPerformedBy(officer);
        audit.setPerformedAt(now);
        auditRepository.save(audit);
        BigDecimal expected = expectedWithholding(year).getOrDefault(month, ZERO);
        return new TaxRemittance1601CMonthDTO(year, month, expected, taxesWithheld, adjustment,
                penalties, remitted, saved.getFilingDate(), saved.getRemittanceDate(),
                channel, reference, remarks, evidence, status, officer, now,
                "CONFIRMED".equals(status) && expected.compareTo(taxesWithheld) == 0);
    }

    @Transactional
    public TaxRemittance1601CMonthDTO voidRecord(
            int year, int month, TaxRemittance1601CVoidRequest request,
            String actor, String authorizationHeader) {
        validateYear(year);
        if (month < 1 || month > 12) throw new IllegalArgumentException("Tax month must be 1 through 12.");
        authorize(authorizationHeader, true);
        if (request == null || !Boolean.TRUE.equals(request.confirmedNotFiledOrRemitted())) {
            throw new IllegalArgumentException(
                    "Confirm that this record does not represent a filed 1601-C or remitted tax.");
        }
        String reason = trim(request.reason(), 500);
        if (reason == null) throw new IllegalArgumentException("A void reason is required.");
        String officer = trim(actor, 100);
        if (officer == null) throw new AccessDeniedException("Officer identity is required.");
        TaxRemittance1601C value = repository.findByTaxYearAndTaxMonth(year, month)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No 1601-C record exists for this month."));
        if ("VOIDED".equals(value.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This 1601-C record is already voided.");
        }
        String previous = auditValues(value);
        LocalDateTime now = LocalDateTime.now();
        value.setStatus("VOIDED");
        value.setRemarks(reason);
        value.setUpdatedBy(officer);
        value.setUpdatedAt(now);
        TaxRemittance1601C saved = repository.save(value);
        TaxRemittance1601CAudit audit = new TaxRemittance1601CAudit();
        audit.setTaxYear(year);
        audit.setTaxMonth(month);
        audit.setAction("VOIDED");
        audit.setPreviousValues(previous);
        audit.setNewValues(auditValues(saved));
        audit.setPerformedBy(officer);
        audit.setPerformedAt(now);
        auditRepository.save(audit);
        BigDecimal expected = expectedWithholding(year).getOrDefault(month, ZERO);
        return new TaxRemittance1601CMonthDTO(year, month, expected, saved.getTaxesWithheld(),
                saved.getAdjustment(), saved.getPenalties(), saved.getAmountRemitted(),
                saved.getFilingDate(), saved.getRemittanceDate(), saved.getRemittanceChannel(),
                saved.getReferenceNo(), saved.getRemarks(), saved.getEvidenceReference(),
                saved.getStatus(), officer, now, false);
    }

    private Map<Integer, BigDecimal> expectedWithholding(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        Map<Integer, BigDecimal> totals = new HashMap<>();
        Map<String, Integer> monthByPeriod = new HashMap<>();
        detailRepository.findFinalizedForAnnualization(from, to, PayrollStatus.CANCELLED)
                .forEach(detail -> monthByPeriod.put(detail.getSalaryPeriodKey(),
                        detail.getSalaryDate().getMonthValue()));
        deductionRepository.findFinalizedForAnnualization(from, to, PayrollStatus.CANCELLED)
                .stream().filter(line -> isWithholdingTax(line.getDeductionCode()))
                .forEach(line -> totals.merge(line.getPayrollDetail().getSalaryDate().getMonthValue(),
                        money(line.getAmount()), BigDecimal::add));
        adjustmentRepository.findPostedForAnnualization(from, to, PayrollStatus.CANCELLED)
                .stream().filter(line -> "DEDUCTION".equalsIgnoreCase(line.getType()))
                .filter(line -> isWithholdingTax(line.getCode()))
                .forEach(line -> {
                    Integer month = monthByPeriod.get(line.getHeader().getSalaryPeriodKey());
                    if (month != null) totals.merge(month, money(line.getAmount()), BigDecimal::add);
                });
        return totals;
    }

    private boolean isWithholdingTax(String code) {
        if (code == null) return false;
        String normalized = code.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return normalized.startsWith("WTX") || "WITHHOLDINGTAX".equals(normalized);
    }

    private String auditValues(TaxRemittance1601C value) {
        return "status=" + value.getStatus() + ", withheld=" + value.getTaxesWithheld()
                + ", adjustment=" + value.getAdjustment() + ", penalties=" + value.getPenalties()
                + ", remitted=" + value.getAmountRemitted() + ", filingDate=" + value.getFilingDate()
                + ", remittanceDate=" + value.getRemittanceDate() + ", reference=" + value.getReferenceNo()
                + ", channel=" + value.getRemittanceChannel()
                + ", evidence=" + value.getEvidenceReference() + ", remarks=" + value.getRemarks();
    }

    private BigDecimal nonNegative(BigDecimal value, String label) {
        BigDecimal amount = money(value);
        if (amount.signum() < 0) throw new IllegalArgumentException(label + " cannot be negative.");
        return amount;
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(Double value) {
        return value == null ? ZERO : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private String trim(String value, int maxLength) {
        if (value == null) return null;
        String result = value.trim();
        if (result.length() > maxLength) throw new IllegalArgumentException(
                "A 1601-C text field exceeds its " + maxLength + "-character limit.");
        return result.isEmpty() ? null : result;
    }

    private void validateYear(int year) {
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Invalid tax year.");
    }

    void authorize(String authorizationHeader, boolean edit) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AccessDeniedException("1601-C remittance access is not permitted.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        String base = configResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_ADMINISTRATIVE, administrativeUrl);
        try {
            ResponseEntity<Permission> response = restTemplate.exchange(
                    base + "/api/authorization/effective?featureKey=payroll.annualization",
                    HttpMethod.GET, new HttpEntity<>(headers), Permission.class);
            Permission permission = response.getBody();
            if (permission != null && (permission.administrator()
                    || (permission.canAccess() && (!edit || permission.canEdit())))) return;
        } catch (AccessDeniedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AccessDeniedException("1601-C remittance permission could not be verified.");
        }
        throw new AccessDeniedException("1601-C remittance access is not permitted.");
    }

    public record Permission(boolean administrator, boolean canAccess, boolean canAdd,
                             boolean canEdit, boolean canDelete, boolean canFinalize) {}
}
