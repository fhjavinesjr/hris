package com.payroll.impl;

import com.payroll.dtos.*;
import com.payroll.entitymodels.PayrollAdjustmentHeader;
import com.payroll.entitymodels.PayrollAdjustmentLine;
import com.payroll.entitymodels.PayrollDetail;
import com.payroll.repositories.PayrollAdjustmentHeaderRepository;
import com.payroll.repositories.PayrollDetailRepository;
import com.payroll.services.PayrollAdjustmentService;
import com.payroll.services.PayrollPeriodLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements payroll adjustment logic.
 *
 * Key design principle: original PayrollDetail records are NEVER modified.
 * All deltas live exclusively in PayrollAdjustmentHeader / PayrollAdjustmentLine.
 *
 * Cascade computation for a taxable earning adjustment of amount X:
 *   GSIS_adj  = round(X × gsisRate)               // employee share (e.g. 9%)
 *   PHIC_adj  = round(X × phicRate / 2)            // employee half of 5%
 *   HDMF_adj  = 0                                   // assume already at PAG-IBIG cap
 *   newTaxable = origTaxable + X − GSIS_adj − PHIC_adj
 *   newWTX    = recomputed from brackets on newTaxable
 *   WTX_adj   = newWTX − origWTX
 *   netImpact = X − GSIS_adj − PHIC_adj − WTX_adj
 */
@Service
public class PayrollAdjustmentServiceImpl implements PayrollAdjustmentService {

    private static final Logger log = LoggerFactory.getLogger(PayrollAdjustmentServiceImpl.class);

    private final PayrollAdjustmentHeaderRepository headerRepo;
    private final PayrollDetailRepository detailRepo;
    private final RestTemplate restTemplate;
    private final PayrollPeriodLockService lockService;

    @Value("${hris.services.administrative.url:http://localhost:8082}")
    private String adminServiceUrl;

    public PayrollAdjustmentServiceImpl(PayrollAdjustmentHeaderRepository headerRepo,
                                         PayrollDetailRepository detailRepo,
                                         RestTemplate restTemplate,
                                         PayrollPeriodLockService lockService) {
        this.headerRepo = headerRepo;
        this.detailRepo = detailRepo;
        this.restTemplate = restTemplate;
        this.lockService = lockService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public API
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Optional<PayrollAdjustmentDTO> findAdjustment(String employeeNo, String salaryPeriodKey) {
        return headerRepo.findActiveWithLinesByEmployeeNoAndSalaryPeriodKey(employeeNo, salaryPeriodKey)
                .map(this::toDTO);
    }

    @Override
    public AdjustmentPreviewResponse previewCascade(AdjustmentPreviewRequest req,
                                                     String salaryType,
                                                     String authHeader) {
        PayrollDetail original = detailRepo
                .findByEmployeeNoAndSalaryPeriodKey(req.getEmployeeNo(), req.getSalaryPeriodKey())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payroll record found for employee " + req.getEmployeeNo()
                        + " in period " + req.getSalaryPeriodKey()));

        HttpHeaders httpHeaders = buildAuthHeaders(authHeader);

        PayrollAdjustmentLineDTO manualLine = new PayrollAdjustmentLineDTO(
                req.getType(), req.getCode(), req.getName(),
                req.getAmount(), req.getIsTaxable(), false);

        List<PayrollAdjustmentLineDTO> cascadeLines = Collections.emptyList();
        double netImpact = computeNetImpactOfLine(manualLine);

        if ("EARNING".equals(req.getType()) && Boolean.TRUE.equals(req.getIsTaxable())) {
            cascadeLines = computeCascadeLines(req.getAmount(), original, salaryType, httpHeaders);
            // net impact = earning − sum of DEDUCTION cascades
            double cascadeDeductions = cascadeLines.stream()
                    .filter(l -> "DEDUCTION".equals(l.getType()))
                    .mapToDouble(l -> l.getAmount() != null ? l.getAmount() : 0)
                    .sum();
            netImpact = roundOff(req.getAmount() - cascadeDeductions);
        }

        AdjustmentPreviewResponse resp = new AdjustmentPreviewResponse();
        resp.setManualLine(manualLine);
        resp.setCascadeLines(cascadeLines);
        resp.setOriginalNetPay(original.getNetAmount());
        resp.setTotalAdjustmentImpact(roundOff(netImpact));
        resp.setProjectedNetPay(roundOff(original.getNetAmount() + netImpact));
        return resp;
    }

    @Override
    @Transactional
    public PayrollAdjustmentDTO saveAdjustment(PayrollAdjustmentDTO dto, String authHeader) {
        // ── Period lock guard ────────────────────────────────────────────────
        if (lockService.isPeriodLocked(dto.getSalaryPeriodKey())) {
            throw new IllegalStateException(
                    "Salary period " + dto.getSalaryPeriodKey() + " is locked and cannot be modified.");
        }

        // Validate that a computed payroll exists for this employee + period
        PayrollDetail original = detailRepo
                .findByEmployeeNoAndSalaryPeriodKey(dto.getEmployeeNo(), dto.getSalaryPeriodKey())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No payroll record found for employee " + dto.getEmployeeNo()
                        + " in period " + dto.getSalaryPeriodKey()));

        HttpHeaders httpHeaders = buildAuthHeaders(authHeader);

        // ── Versioning: if an active header already exists, mark it SUPERSEDED ──────
        Optional<PayrollAdjustmentHeader> existingOpt = headerRepo
                .findByEmployeeNoAndSalaryPeriodKeyAndStatusNot(
                        dto.getEmployeeNo(), dto.getSalaryPeriodKey(), "SUPERSEDED");

        int nextVersion = 1;
        if (existingOpt.isPresent()) {
            PayrollAdjustmentHeader old = existingOpt.get();
            nextVersion = old.getVersion() + 1;
            old.setStatus("SUPERSEDED");
            old.setUpdatedAt(LocalDateTime.now());
            headerRepo.save(old);
        }

        // Always create a brand-new header for this version
        PayrollAdjustmentHeader header = new PayrollAdjustmentHeader();
        header.setEmployeeNo(dto.getEmployeeNo());
        header.setEmployeeName(dto.getEmployeeName() != null
                ? dto.getEmployeeName() : original.getEmployeeName());
        header.setSalaryPeriodKey(dto.getSalaryPeriodKey());
        header.setVersion(nextVersion);
        header.setCreatedAt(LocalDateTime.now());
        header.setUpdatedAt(LocalDateTime.now());
        header.setCreatedBy(dto.getCreatedBy());
        header.setAuthorityNo(dto.getAuthorityNo());
        header.setReason(dto.getReason());
        header.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
        header.setPostedAt(null);
        header.setPostedBy(null);

        // Determine salary type from the period key's nthOrder (we need it for WTX cascade).
        // The UI passes manual lines; we re-expand taxable earnings with fresh cascades.
        String salaryType = inferSalaryType(dto.getSalaryPeriodKey());

        int idx = 0;
        List<PayrollAdjustmentLineDTO> inputLines = dto.getLines() != null ? dto.getLines() : Collections.emptyList();

        // Only process non-auto-computed lines (auto lines are recomputed fresh each save)
        List<PayrollAdjustmentLineDTO> manualLines = inputLines.stream()
                .filter(l -> !Boolean.TRUE.equals(l.getIsAutoComputed()))
                .collect(Collectors.toList());

        for (PayrollAdjustmentLineDTO lineDTO : manualLines) {
            PayrollAdjustmentLine line = toEntity(lineDTO, header, idx++);
            header.getLines().add(line);

            // For taxable earnings, compute cascade lines and append them
            if ("EARNING".equals(lineDTO.getType()) && Boolean.TRUE.equals(lineDTO.getIsTaxable())) {
                List<PayrollAdjustmentLineDTO> cascades =
                        computeCascadeLines(lineDTO.getAmount(), original, salaryType, httpHeaders);
                for (PayrollAdjustmentLineDTO cascade : cascades) {
                    header.getLines().add(toEntity(cascade, header, idx++));
                }
                // Rebase the original taxable so subsequent taxable earnings layer correctly
                original = rebaseOriginal(original, lineDTO.getAmount(), cascades);
            }
        }

        PayrollAdjustmentHeader saved = headerRepo.save(header);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public PayrollAdjustmentDTO postAdjustment(Long headerId, String postedBy) {
        PayrollAdjustmentHeader header = headerRepo.findById(headerId)
                .orElseThrow(() -> new IllegalArgumentException("Adjustment header not found: " + headerId));

        // ── Period lock guard ────────────────────────────────────────────────
        if (lockService.isPeriodLocked(header.getSalaryPeriodKey())) {
            throw new IllegalStateException(
                    "Salary period " + header.getSalaryPeriodKey() + " is locked and cannot be modified.");
        }

        if ("SUPERSEDED".equalsIgnoreCase(header.getStatus())) {
            throw new IllegalStateException("Cannot post a superseded adjustment.");
        }
        if ("POSTED".equalsIgnoreCase(header.getStatus())) {
            return toDTO(header);
        }
        if (!"PENDING".equalsIgnoreCase(header.getStatus())) {
            throw new IllegalStateException("Only PENDING adjustments can be posted.");
        }

        header.setStatus("POSTED");
        header.setPostedAt(LocalDateTime.now());
        header.setPostedBy(
                postedBy != null && !postedBy.isBlank()
                        ? postedBy.trim()
                        : "SYSTEM");
        header.setUpdatedAt(LocalDateTime.now());

        PayrollAdjustmentHeader saved = headerRepo.save(header);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteLine(Long lineId) {
        // Load the header via a query that finds the header containing this line
        headerRepo.findAll().stream()
                .filter(h -> h.getLines().stream().anyMatch(l -> lineId.equals(l.getId())))
                .findFirst()
                .ifPresent(header -> {
                    // Remove the target line and any auto-computed lines associated with it
                    header.getLines().removeIf(l -> lineId.equals(l.getId()));
                    if (header.getLines().isEmpty()) {
                        headerRepo.delete(header);
                    } else {
                        headerRepo.save(header);
                    }
                });
    }

    @Override
    public List<AdjustmentSummaryDTO> getSummaryForPeriod(String salaryPeriodKey) {
        List<PayrollAdjustmentHeader> headers =
                headerRepo.findActiveWithLinesBySalaryPeriodKey(salaryPeriodKey);

        List<AdjustmentSummaryDTO> summaries = new ArrayList<>();
        for (PayrollAdjustmentHeader h : headers) {
            Optional<PayrollDetail> detailOpt =
                    detailRepo.findByEmployeeNoAndSalaryPeriodKey(h.getEmployeeNo(), salaryPeriodKey);
            if (detailOpt.isEmpty()) continue;

            double originalNet = detailOpt.get().getNetAmount();
            double adjImpact   = computeTotalImpact(h.getLines());

            AdjustmentSummaryDTO s = new AdjustmentSummaryDTO();
            s.setEmployeeNo(h.getEmployeeNo());
            s.setSalaryPeriodKey(salaryPeriodKey);
            s.setAdjustmentHeaderId(h.getId());
            s.setStatus(h.getStatus());
            s.setOriginalNetPay(originalNet);
            s.setTotalAdjustmentImpact(roundOff(adjImpact));
            s.setAdjustedNetPay(roundOff(originalNet + adjImpact));
            summaries.add(s);
        }
        return summaries;
    }

    @Override
    public long getPendingCountForPeriod(String salaryPeriodKey) {
        return headerRepo.countBySalaryPeriodKeyAndStatus(salaryPeriodKey, "PENDING");
    }

    @Override
    public List<PayrollAdjustmentDTO> getHistoryForEmployee(String employeeNo, String salaryPeriodKey) {
        return headerRepo.findAllVersionsByEmployeeNoAndSalaryPeriodKey(employeeNo, salaryPeriodKey)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Cascade computation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Computes the GSIS, PHIC, and WTX cascade lines for a taxable earning delta.
     *
     * @param earningDelta  positive = additional taxable income, negative = clawback
     * @param original      the base PayrollDetail record (used for original WTX and taxable income)
     * @param salaryType    e.g. "SEMI_MONTHLY" — determines which WTX bracket table to use
     * @param headers       HTTP headers with JWT for admin-service calls
     * @return list of auto-computed DEDUCTION lines (positive = extra deduction, negative = deduction reversal)
     */
    private List<PayrollAdjustmentLineDTO> computeCascadeLines(double earningDelta,
                                                                PayrollDetail original,
                                                                String salaryType,
                                                                HttpHeaders headers) {
        // ── Fetch live rates from Admin service ──────────────────────────────
        double gsisRate = fetchGsisRate(headers);
        double phicRate = fetchPhicRate(headers); // total rate; employee share = /2
        Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>> taxBrackets =
                fetchTaxBrackets(headers);

        // ── Compute contribution deltas ──────────────────────────────────────
        double gsisAdj = roundOff(earningDelta * gsisRate);
        double phicAdj = roundOff(earningDelta * (phicRate / 2.0));

        // ── Recompute WTX on the new taxable income ──────────────────────────
        double origTaxable = original.getTaxableIncome() != null ? original.getTaxableIncome() : 0;
        double origWtx     = original.getTaxAmount()     != null ? original.getTaxAmount()     : 0;

        double newTaxable = origTaxable + earningDelta - gsisAdj - phicAdj;
        double newWtx     = computeWtx(newTaxable, salaryType, taxBrackets);
        double wtxAdj     = roundOff(newWtx - origWtx);

        List<PayrollAdjustmentLineDTO> cascade = new ArrayList<>();

        // GSIS cascade (positive = extra deduction from net)
        if (Math.abs(gsisAdj) > 0.001) {
            cascade.add(new PayrollAdjustmentLineDTO(
                    "DEDUCTION", "GSIS_CASCADE",
                    "GSIS – Additional EE Share",
                    gsisAdj, null, true));
        }

        // PHIC cascade
        if (Math.abs(phicAdj) > 0.001) {
            cascade.add(new PayrollAdjustmentLineDTO(
                    "DEDUCTION", "PHIC_CASCADE",
                    "PhilHealth – Additional EE Share",
                    phicAdj, null, true));
        }

        // WTX cascade (can be negative if a correction reduces taxable income and thus WTX)
        if (Math.abs(wtxAdj) > 0.001) {
            cascade.add(new PayrollAdjustmentLineDTO(
                    "DEDUCTION", "WTX_CASCADE",
                    "Withholding Tax – Additional",
                    wtxAdj, null, true));
        }

        return cascade;
    }

    /** Returns the net pay impact of a single line. */
    private double computeNetImpactOfLine(PayrollAdjustmentLineDTO line) {
        if (line.getAmount() == null) return 0;
        if ("EARNING".equals(line.getType()))    return line.getAmount();
        if ("DEDUCTION".equals(line.getType()))  return -line.getAmount();
        return 0;
    }

    /** Compute total net-pay impact of all lines in a header. */
    private double computeTotalImpact(List<PayrollAdjustmentLine> lines) {
        double total = 0;
        for (PayrollAdjustmentLine l : lines) {
            if (l.getAmount() == null) continue;
            if ("EARNING".equals(l.getType()))   total += l.getAmount();
            if ("DEDUCTION".equals(l.getType())) total -= l.getAmount();
        }
        return total;
    }

    /**
     * Returns a shallow copy of the PayrollDetail with taxable income and WTX rebased
     * after applying one taxable earning delta and its cascades.
     * Used so that subsequent taxable-earning lines in the same save call layer correctly.
     */
    private PayrollDetail rebaseOriginal(PayrollDetail original,
                                          double earningDelta,
                                          List<PayrollAdjustmentLineDTO> cascades) {
        double gsisAdj = cascades.stream()
                .filter(c -> "GSIS_CASCADE".equals(c.getCode())).mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0).sum();
        double phicAdj = cascades.stream()
                .filter(c -> "PHIC_CASCADE".equals(c.getCode())).mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0).sum();
        double wtxAdj  = cascades.stream()
                .filter(c -> "WTX_CASCADE".equals(c.getCode())).mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0).sum();

        PayrollDetail rebased = new PayrollDetail();
        rebased.setTaxableIncome((original.getTaxableIncome() != null ? original.getTaxableIncome() : 0)
                + earningDelta - gsisAdj - phicAdj);
        rebased.setTaxAmount((original.getTaxAmount() != null ? original.getTaxAmount() : 0) + wtxAdj);
        rebased.setNetAmount(original.getNetAmount()); // not used after rebase
        return rebased;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  WTX formula (mirrors PayrollComputationEngine)
    // ─────────────────────────────────────────────────────────────────────────

    private double computeWtx(double taxableIncome,
                               String salaryType,
                               Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>> taxBracketsMap) {
        if (taxableIncome <= 0) return 0;
        String matchKey = taxBracketsMap.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(salaryType)
                        || k.replace("-", "_").equalsIgnoreCase(salaryType)
                        || k.replace(" ", "_").equalsIgnoreCase(salaryType))
                .findFirst().orElse(salaryType);
        List<PayrollDataSnapshot.WHoldingTaxBracketDTO> brackets =
                taxBracketsMap.getOrDefault(matchKey, Collections.emptyList());
        for (PayrollDataSnapshot.WHoldingTaxBracketDTO b : brackets) {
            boolean inRange = taxableIncome >= b.getIncomeFrom()
                    && (b.getIncomeTo() == null || taxableIncome <= b.getIncomeTo());
            if (inRange) {
                double tax = (b.getBaseTax() != null ? b.getBaseTax() : 0)
                        + ((taxableIncome - b.getIncomeFrom()) * (b.getExcessRate() != null ? b.getExcessRate() : 0));
                return roundOff(tax);
            }
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Admin-service data fetchers
    // ─────────────────────────────────────────────────────────────────────────

    private double fetchGsisRate(HttpHeaders h) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    adminServiceUrl + "/api/gsisContribution/get-all",
                    HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> body = resp.getBody();
            if (body != null && !body.isEmpty()) {
                Map<String, Object> latest = body.get(body.size() - 1);
                return parseRate(latest.get("employeeSharePercentage"));
            }
        } catch (Exception ex) {
            log.warn("AdjustmentService: could not fetch GSIS rate, defaulting to 9%: {}", ex.getMessage());
        }
        return 0.09;
    }

    private double fetchPhicRate(HttpHeaders h) {
        try {
            ResponseEntity<List<Map<String, Object>>> resp = restTemplate.exchange(
                    adminServiceUrl + "/api/philhealth/brackets",
                    HttpMethod.GET, new HttpEntity<>(h),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            List<Map<String, Object>> body = resp.getBody();
            if (body != null) {
                // All active brackets share the same rate; grab the first non-zero
                for (Map<String, Object> bracket : body) {
                    Object rateObj = bracket.get("rate");
                    if (rateObj != null) {
                        double r = parseRate(rateObj);
                        if (r > 0) return r;
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("AdjustmentService: could not fetch PhilHealth rate, defaulting to 5%: {}", ex.getMessage());
        }
        return 0.05;
    }

    private Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>> fetchTaxBrackets(HttpHeaders h) {
        try {
            ResponseEntity<Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>>> resp =
                    restTemplate.exchange(
                            adminServiceUrl + "/api/wh-tax/brackets",
                            HttpMethod.GET, new HttpEntity<>(h),
                            new ParameterizedTypeReference<Map<String, List<PayrollDataSnapshot.WHoldingTaxBracketDTO>>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception ex) {
            log.warn("AdjustmentService: could not fetch WH-tax brackets: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Infers a salary type string from the period key's nth-order segment.
     * The WTX bracket map typically uses "Monthly" or "Semi-Monthly" (or similar) as keys.
     * We return "SEMI_MONTHLY" by default since ZCMC uses semi-monthly payroll.
     * The UI can override this via a request parameter on the preview endpoint.
     */
    private String inferSalaryType(String periodKey) {
        // e.g. "2026-6-1" — we don't have enough info from the key alone
        // Default to SEMI_MONTHLY which matches ZCMC standard
        return "SEMI_MONTHLY";
    }

    private double parseRate(Object raw) {
        if (raw == null) return 0.0;
        try {
            double v = Double.parseDouble(raw.toString().trim());
            return v > 1.0 ? v / 100.0 : v;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double roundOff(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private HttpHeaders buildAuthHeaders(String authHeader) {
        HttpHeaders h = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            h.set("Authorization", authHeader);
        }
        return h;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Entity ↔ DTO mappers
    // ─────────────────────────────────────────────────────────────────────────

    private PayrollAdjustmentDTO toDTO(PayrollAdjustmentHeader h) {
        PayrollAdjustmentDTO dto = new PayrollAdjustmentDTO();
        dto.setId(h.getId());
        dto.setEmployeeNo(h.getEmployeeNo());
        dto.setEmployeeName(h.getEmployeeName());
        dto.setSalaryPeriodKey(h.getSalaryPeriodKey());
        dto.setVersion(h.getVersion());
        dto.setAuthorityNo(h.getAuthorityNo());
        dto.setReason(h.getReason());
        dto.setStatus(h.getStatus());
        dto.setCreatedAt(h.getCreatedAt());
        dto.setUpdatedAt(h.getUpdatedAt());
        dto.setCreatedBy(h.getCreatedBy());
        dto.setPostedAt(h.getPostedAt());
        dto.setPostedBy(h.getPostedBy());

        List<PayrollAdjustmentLineDTO> lines = h.getLines().stream()
                .map(this::toLineDTO)
                .collect(Collectors.toList());
        dto.setLines(lines);

        // Compute net adjustment impact
        double impact = computeTotalImpact(h.getLines());
        dto.setNetAdjustmentAmount(roundOff(impact));
        return dto;
    }

    private PayrollAdjustmentLineDTO toLineDTO(PayrollAdjustmentLine l) {
        PayrollAdjustmentLineDTO dto = new PayrollAdjustmentLineDTO();
        dto.setId(l.getId());
        dto.setType(l.getType());
        dto.setCode(l.getCode());
        dto.setName(l.getName());
        dto.setAmount(l.getAmount());
        dto.setIsTaxable(l.getIsTaxable());
        dto.setIsAutoComputed(l.getIsAutoComputed());
        return dto;
    }

    private PayrollAdjustmentLine toEntity(PayrollAdjustmentLineDTO dto,
                                            PayrollAdjustmentHeader header,
                                            int indexNo) {
        PayrollAdjustmentLine entity = new PayrollAdjustmentLine();
        entity.setHeader(header);
        entity.setType(dto.getType());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setAmount(dto.getAmount() != null ? dto.getAmount() : 0.0);
        entity.setIsTaxable(dto.getIsTaxable());
        entity.setIsAutoComputed(Boolean.TRUE.equals(dto.getIsAutoComputed()));
        entity.setIndexNo(indexNo);
        return entity;
    }
}
