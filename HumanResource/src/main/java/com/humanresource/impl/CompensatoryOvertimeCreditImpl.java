package com.humanresource.impl;

import com.humanresource.dtos.CompensatoryOvertimeCreditDTO;
import com.humanresource.entitymodels.CompensatoryOvertimeCredit;
import com.humanresource.entitymodels.CocBeginningBalance;
import com.humanresource.entitymodels.OvertimeRequest;
import com.humanresource.repositories.OvertimeRequestRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import com.humanresource.repositories.CompensatoryOvertimeCreditRepository;
import com.humanresource.repositories.CompensatoryTimeOffRepository;
import com.humanresource.repositories.CocBeginningBalanceRepository;
import com.humanresource.services.CompensatoryOvertimeCreditService;
import jakarta.transaction.Transactional;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompensatoryOvertimeCreditImpl implements CompensatoryOvertimeCreditService {

    private static final Logger log = LoggerFactory.getLogger(CompensatoryOvertimeCreditImpl.class);

    private final CompensatoryOvertimeCreditRepository cocRepository;
    private final CompensatoryTimeOffRepository ctoRepository;
    private final CocBeginningBalanceRepository cocBegBalRepository;
    private final DataSource dataSource;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final JdbcTemplate jdbc;

    public CompensatoryOvertimeCreditImpl(CompensatoryOvertimeCreditRepository cocRepository,
                                          CompensatoryTimeOffRepository ctoRepository,
                                          CocBeginningBalanceRepository cocBegBalRepository,
                                          OvertimeRequestRepository overtimeRequestRepository,
                                          DataSource dataSource) {
        this.cocRepository = cocRepository;
        this.ctoRepository = ctoRepository;
        this.cocBegBalRepository = cocBegBalRepository;
        this.dataSource = dataSource;
        this.overtimeRequestRepository = overtimeRequestRepository;
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private CompensatoryOvertimeCreditDTO toDTO(CompensatoryOvertimeCredit e) {
        CompensatoryOvertimeCreditDTO dto = new CompensatoryOvertimeCreditDTO();
        dto.setCocId(e.getCocId());
        dto.setEmployeeId(e.getEmployeeId());
        dto.setDateFiled(e.getDateFiled());
        dto.setDateWorked(e.getDateWorked());
        dto.setHoursWorked(e.getHoursWorked());
        dto.setOvertimeRequestId(e.getOvertimeRequestId());
        dto.setActualHoursWorked(e.getActualHoursWorked());
        dto.setCocMultiplier(e.getCocMultiplier());
        dto.setReason(e.getReason());
        dto.setWorkType(e.getWorkType());
        dto.setStatus(e.getStatus());
        dto.setApprovedById(e.getApprovedById());
        dto.setApprovedAt(e.getApprovedAt());
        dto.setApprovalRemarks(e.getApprovalRemarks());
        dto.setRecommendationStatus(e.getRecommendationStatus());
        dto.setRecommendedById(e.getRecommendedById());
        dto.setRecommendationRemarks(e.getRecommendationRemarks());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setUpdatedAt(e.getUpdatedAt());
        return dto;
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO create(CompensatoryOvertimeCreditDTO dto) throws Exception {
        if (dto.getEmployeeId() == null || dto.getOvertimeRequestId() == null) {
            throw new IllegalArgumentException("An approved Overtime/Holiday Duty authority reference is required.");
        }
        Map<String, Object> preview = previewFromOvertimeRequest(dto.getOvertimeRequestId(), dto.getEmployeeId());
        LocalDate dateWorked = LocalDate.parse(preview.get("dateWorked").toString());
        double actualHours = ((Number) preview.get("actualHoursWorked")).doubleValue();
        double multiplier = ((Number) preview.get("cocMultiplier")).doubleValue();
        double creditedHours = ((Number) preview.get("creditedHours")).doubleValue();
        String workType = preview.get("workType").toString();

        if (cocRepository.existsByOvertimeRequestIdAndStatusNot(dto.getOvertimeRequestId(), "Disapproved")) {
            throw new IllegalArgumentException("This approved authority has already been used for a COC filing.");
        }
        if (creditedHours <= 0) throw new IllegalArgumentException("No eligible rendered hours were found in DTR.");
        LocalDate monthStart = dateWorked.withDayOfMonth(1);
        LocalDate monthEnd = dateWorked.withDayOfMonth(dateWorked.lengthOfMonth());
        double monthEarned = Optional.ofNullable(cocRepository.sumApprovedHoursByEmployeeIdAndDateWorkedBetween(dto.getEmployeeId(), monthStart, monthEnd)).orElse(0.0);
        if (monthEarned + creditedHours > 40.0) {
            throw new IllegalArgumentException("COC monthly earning limit exceeded. Approved credits for this month: " + monthEarned + " hours.");
        }

        CompensatoryOvertimeCredit entity = new CompensatoryOvertimeCredit();
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : LocalDate.now());
        entity.setDateWorked(dateWorked);
        entity.setOvertimeRequestId(dto.getOvertimeRequestId());
        entity.setActualHoursWorked(actualHours);
        entity.setCocMultiplier(multiplier);
        entity.setHoursWorked(creditedHours);
        entity.setReason(dto.getReason());
        entity.setWorkType(workType);

        String requestedStatus = dto.getStatus() != null ? dto.getStatus() : "Pending";
        entity.setStatus(requestedStatus);
        entity.setApprovedById(dto.getApprovedById());
        entity.setApprovalRemarks(dto.getApprovalRemarks());

        if ("Approved".equalsIgnoreCase(requestedStatus)
                || "Disapproved".equalsIgnoreCase(requestedStatus)) {
            entity.setApprovedAt(LocalDateTime.now());
        }

        entity.setRecommendationStatus(
                dto.getRecommendationStatus() != null
                        ? dto.getRecommendationStatus()
                        : "Pending"
        );
        entity.setRecommendedById(dto.getRecommendedById());
        entity.setRecommendationRemarks(dto.getRecommendationRemarks());

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return toDTO(cocRepository.save(entity));
    }

    @Override
    public Map<String, Object> previewFromOvertimeRequest(Long overtimeRequestId, Long employeeId) throws Exception {
        OvertimeRequest ot = overtimeRequestRepository.findById(overtimeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Overtime/Holiday Duty authority not found."));
        if (!employeeId.equals(ot.getEmployeeId())) throw new IllegalArgumentException("Authority does not belong to the selected employee.");
        if (!"Approved".equalsIgnoreCase(ot.getStatus())) throw new IllegalArgumentException("Authority must be approved before COC validation.");
        LocalDate workDate = ot.getDateTimeFrom().toLocalDate();
        Map<String, Object> dtr;
        try {
            dtr = jdbc.queryForMap("SELECT total_work_minutes, total_overtime_minutes FROM dtr_daily WHERE employee_id = ? AND work_date = ?", String.valueOf(employeeId), workDate);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            throw new IllegalArgumentException("No processed DTR record was found for " + workDate + ". Process or validate the employee DTR first.");
        }
        int workMinutes = ((Number)dtr.get("total_work_minutes")).intValue();
        int overtimeMinutes = ((Number)dtr.get("total_overtime_minutes")).intValue();
        String workType = ot.getWorkType() != null ? ot.getWorkType() : "REGULAR_OVERTIME";
        int eligibleMinutes = "REGULAR_OVERTIME".equalsIgnoreCase(workType) ? overtimeMinutes : workMinutes;
        double authorizedHours = ot.getNetAuthorizedHours() != null ? ot.getNetAuthorizedHours() : ot.getTotalHours();
        double actualHours = Math.round(Math.min(eligibleMinutes / 60.0, authorizedHours) * 100.0) / 100.0;
        double multiplier = (workType.contains("HOLIDAY") || workType.contains("DAY_OFF") || workType.contains("REST_DAY")) ? 1.5 : 1.0;
        double credited = Math.round(actualHours * multiplier * 100.0) / 100.0;
        Map<String, Object> result = new HashMap<>();
        result.put("overtimeRequestId", overtimeRequestId); result.put("dateWorked", workDate.toString());
        result.put("workType", workType); result.put("authorizedHours", authorizedHours);
        result.put("actualHoursWorked", actualHours); result.put("cocMultiplier", multiplier); result.put("creditedHours", credited);
        result.put("authorityReference", ot.getAuthorityReference() != null ? ot.getAuthorityReference() : "");
        return result;
    }

    @Override
    public List<CompensatoryOvertimeCreditDTO> getAll() throws Exception {
        return cocRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CompensatoryOvertimeCreditDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        List<CompensatoryOvertimeCreditDTO> list = cocRepository
                .findByEmployeeIdOrderByDateFiledDesc(employeeId)
                .stream().map(this::toDTO).collect(Collectors.toList());

        // Attach current balance to each record
        Double balance = getAvailableBalance(employeeId);
        list.forEach(dto -> dto.setCurrentBalance(balance));
        return list;
    }

    @Override
    public List<CompensatoryOvertimeCreditDTO> getPendingAll() throws Exception {
        return cocRepository.findByStatusOrderByDateFiledDesc("Pending")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO approve(Long cocId, Long approvedById, String remarks) throws Exception {
        CompensatoryOvertimeCredit entity = cocRepository.findById(cocId).orElseThrow(() -> new IllegalArgumentException("COC not found."));
        double projected = getAvailableBalance(entity.getEmployeeId()) + (entity.getHoursWorked() != null ? entity.getHoursWorked() : 0.0);
        if (projected > 120.0) throw new IllegalArgumentException("Approval would exceed the 120-hour maximum COC balance.");
        return updateStatus(cocId, "Approved", approvedById, remarks);
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO disapprove(Long cocId, Long approvedById, String remarks) throws Exception {
        return updateStatus(cocId, "Disapproved", approvedById, remarks);
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO recommend(Long cocId, Long recommendedById, String remarks) throws Exception {
        try {
            Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
            if (optional.isEmpty()) return null;
            CompensatoryOvertimeCredit entity = optional.get();
            entity.setRecommendationStatus("Recommended");
            entity.setRecommendedById(recommendedById);
            entity.setRecommendationRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error recommending COC for id {}: ", cocId, ex);
            return null;
        }
    }

    private CompensatoryOvertimeCreditDTO updateStatus(Long cocId, String newStatus, Long approvedById, String remarks) {
        try {
            Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
            if (optional.isEmpty()) return null;
            CompensatoryOvertimeCredit entity = optional.get();
            entity.setStatus(newStatus);
            entity.setApprovedById(approvedById);
            entity.setApprovedAt(LocalDateTime.now());
            entity.setApprovalRemarks(remarks);
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating COC status for id {}: ", cocId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO update(Long cocId, CompensatoryOvertimeCreditDTO dto) throws Exception {
        try {
            Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
            if (optional.isEmpty()) return null;
            CompensatoryOvertimeCredit entity = optional.get();

            if (!"Pending".equalsIgnoreCase(entity.getStatus())
                    || "Recommended".equalsIgnoreCase(entity.getRecommendationStatus())) {
                throw new IllegalStateException(
                        "Only a pending COC record that has not entered approval may be edited through the employee endpoint."
                );
            }

            if (dto.getDateFiled() != null) entity.setDateFiled(dto.getDateFiled());
            if (dto.getDateWorked() != null) entity.setDateWorked(dto.getDateWorked());
            if (dto.getHoursWorked() != null) {
                validatePositiveHours(dto.getHoursWorked());
                entity.setHoursWorked(dto.getHoursWorked());
            }
            if (dto.getReason() != null) entity.setReason(dto.getReason());
            if (dto.getWorkType() != null) entity.setWorkType(requireWorkType(dto.getWorkType()));

            // Workflow state and audit fields are changed only through the
            // recommend/approve/disapprove endpoints.
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error updating COC id {}: ", cocId, ex);
            return null;
        }
    }

    /**
     * HRM maintenance edit. Unlike the employee edit operation, this does not
     * depend on Pending/Recommended/Approved/Disapproved/Cancelled state and
     * applies the workflow values selected by the administrator.
     */
    @Transactional
    @Override
    public CompensatoryOvertimeCreditDTO administrativeUpdate(
            Long cocId,
            CompensatoryOvertimeCreditDTO dto) throws Exception {
        Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
        if (optional.isEmpty()) return null;

        CompensatoryOvertimeCredit entity = optional.get();
        if (dto.getEmployeeId() != null && !dto.getEmployeeId().equals(entity.getEmployeeId())) {
            throw new IllegalArgumentException("The COC record cannot be moved to another employee.");
        }

        LocalDate newDateFiled = dto.getDateFiled() != null
                ? dto.getDateFiled()
                : entity.getDateFiled();
        LocalDate newDateWorked = dto.getDateWorked() != null
                ? dto.getDateWorked()
                : entity.getDateWorked();
        Double newHoursWorked = dto.getHoursWorked() != null
                ? dto.getHoursWorked()
                : entity.getHoursWorked();
        String newWorkType = dto.getWorkType() != null
                ? requireWorkType(dto.getWorkType())
                : entity.getWorkType();
        String newStatus = normalizeFinalStatus(dto.getStatus(), entity.getStatus());

        if (newDateFiled == null) throw new IllegalArgumentException("Date filed is required.");
        if (newDateWorked == null) throw new IllegalArgumentException("Date worked is required.");
        validatePositiveHours(newHoursWorked);

        Long newOvertimeRequestId = dto.getOvertimeRequestId() != null
                ? dto.getOvertimeRequestId()
                : entity.getOvertimeRequestId();
        if (newOvertimeRequestId != null
                && !newOvertimeRequestId.equals(entity.getOvertimeRequestId())) {
            OvertimeRequest authority = overtimeRequestRepository.findById(newOvertimeRequestId)
                    .orElseThrow(() -> new IllegalArgumentException("Overtime/Duty Order authority not found."));
            if (!entity.getEmployeeId().equals(authority.getEmployeeId())) {
                throw new IllegalArgumentException("Authority does not belong to the COC employee.");
            }
            if (!"Approved".equalsIgnoreCase(authority.getStatus())) {
                throw new IllegalArgumentException("The selected Overtime/Duty Order authority is not approved.");
            }
            if (cocRepository.existsByOvertimeRequestIdAndStatusNotAndCocIdNot(
                    newOvertimeRequestId, "Disapproved", cocId)) {
                throw new IllegalArgumentException("The selected authority is already used by another active COC record.");
            }
        }

        validateAdministrativeCaps(entity, newDateWorked, newHoursWorked, newStatus);

        entity.setDateFiled(newDateFiled);
        entity.setDateWorked(newDateWorked);
        entity.setHoursWorked(newHoursWorked);
        entity.setReason(dto.getReason() != null ? dto.getReason() : entity.getReason());
        entity.setWorkType(newWorkType);
        entity.setOvertimeRequestId(newOvertimeRequestId);

        if (dto.getActualHoursWorked() != null) {
            if (!Double.isFinite(dto.getActualHoursWorked()) || dto.getActualHoursWorked() < 0) {
                throw new IllegalArgumentException("Actual eligible DTR hours cannot be negative.");
            }
            entity.setActualHoursWorked(dto.getActualHoursWorked());
        }
        if (dto.getCocMultiplier() != null) {
            if (!Double.isFinite(dto.getCocMultiplier()) || dto.getCocMultiplier() <= 0) {
                throw new IllegalArgumentException("COC multiplier must be greater than zero.");
            }
            entity.setCocMultiplier(dto.getCocMultiplier());
        }

        applyAdministrativeWorkflow(entity, dto, newStatus);
        entity.setUpdatedAt(LocalDateTime.now());
        return toDTO(cocRepository.save(entity));
    }

    @Transactional
    @Override
    public Boolean delete(Long cocId) throws Exception {
        try {
            Optional<CompensatoryOvertimeCredit> optional = cocRepository.findById(cocId);
            if (optional.isEmpty()) return false;
            CompensatoryOvertimeCredit entity = optional.get();
            if (!"Pending".equalsIgnoreCase(entity.getStatus())
                    || "Recommended".equalsIgnoreCase(entity.getRecommendationStatus())) {
                throw new IllegalStateException(
                        "Only a pending COC record that has not entered approval may be deleted through the employee endpoint."
                );
            }
            cocRepository.deleteById(cocId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting COC id {}: ", cocId, ex);
            return false;
        }
    }

    @Transactional
    @Override
    public Boolean administrativeDelete(Long cocId) throws Exception {
        if (!cocRepository.existsById(cocId)) return false;
        cocRepository.deleteById(cocId);
        return true;
    }

    private void validatePositiveHours(Double hours) {
        if (hours == null || !Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException("COC hours to credit must be greater than zero.");
        }
    }

    private String requireWorkType(String workType) {
        String normalized = workType == null ? "" : workType.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException("Work type is required.");
        return normalized;
    }

    private void validateAdministrativeCaps(CompensatoryOvertimeCredit entity,
                                            LocalDate newDateWorked,
                                            double newHoursWorked,
                                            String newStatus) throws Exception {
        if (!"Approved".equalsIgnoreCase(newStatus)) return;

        boolean existingCreditIsApproved = "Approved".equalsIgnoreCase(entity.getStatus());

        LocalDate monthStart = newDateWorked.withDayOfMonth(1);
        LocalDate monthEnd = newDateWorked.withDayOfMonth(newDateWorked.lengthOfMonth());
        double approvedInTargetMonth = Optional.ofNullable(
                cocRepository.sumApprovedHoursByEmployeeIdAndDateWorkedBetween(
                        entity.getEmployeeId(), monthStart, monthEnd)
        ).orElse(0.0);

        LocalDate oldDateWorked = entity.getDateWorked();
        boolean oldCreditIsInTargetMonth = oldDateWorked != null
                && !oldDateWorked.isBefore(monthStart)
                && !oldDateWorked.isAfter(monthEnd);
        if (existingCreditIsApproved
                && oldCreditIsInTargetMonth
                && entity.getHoursWorked() != null) {
            approvedInTargetMonth -= entity.getHoursWorked();
        }
        if (approvedInTargetMonth + newHoursWorked > 40.0) {
            throw new IllegalArgumentException(
                    "COC monthly earning limit would be exceeded by this administrative edit."
            );
        }

        double projectedBalance = getAvailableBalance(entity.getEmployeeId())
                - (existingCreditIsApproved && entity.getHoursWorked() != null
                    ? entity.getHoursWorked()
                    : 0.0)
                + newHoursWorked;
        if (projectedBalance > 120.0) {
            throw new IllegalArgumentException(
                    "The administrative edit would exceed the 120-hour maximum COC balance."
            );
        }
    }

    private void applyAdministrativeWorkflow(CompensatoryOvertimeCredit entity,
                                             CompensatoryOvertimeCreditDTO dto,
                                             String finalStatus) {
        String previousStatus = entity.getStatus();
        String recommendationStatus = normalizeRecommendationStatus(
                dto.getRecommendationStatus(), entity.getRecommendationStatus());

        entity.setStatus(finalStatus);
        entity.setRecommendationStatus(recommendationStatus);
        entity.setRecommendationRemarks(dto.getRecommendationRemarks());
        entity.setApprovalRemarks(dto.getApprovalRemarks());

        if ("Pending".equalsIgnoreCase(recommendationStatus)) {
            entity.setRecommendedById(null);
        } else {
            entity.setRecommendedById(dto.getRecommendedById());
        }

        if ("Pending".equalsIgnoreCase(finalStatus)) {
            entity.setApprovedById(null);
            entity.setApprovedAt(null);
        } else {
            entity.setApprovedById(dto.getApprovedById());
            if (entity.getApprovedAt() == null
                    || previousStatus == null
                    || !finalStatus.equalsIgnoreCase(previousStatus)) {
                entity.setApprovedAt(LocalDateTime.now());
            }
        }
    }

    private String normalizeFinalStatus(String requested, String current) {
        if (requested == null || requested.isBlank()) return current != null ? current : "Pending";
        return switch (requested.trim().toLowerCase()) {
            case "pending" -> "Pending";
            case "approved" -> "Approved";
            case "disapproved" -> "Disapproved";
            case "cancel", "canceled", "cancelled" -> "Cancelled";
            default -> throw new IllegalArgumentException("Unsupported final status: " + requested);
        };
    }

    private String normalizeRecommendationStatus(String requested, String current) {
        if (requested == null || requested.isBlank()) return current != null ? current : "Pending";
        return switch (requested.trim().toLowerCase()) {
            case "pending" -> "Pending";
            case "approved" -> "Approved";
            case "recommended" -> "Recommended";
            case "disapproved" -> "Disapproved";
            case "cancel", "canceled", "cancelled" -> "Cancelled";
            default -> throw new IllegalArgumentException("Unsupported recommendation status: " + requested);
        };
    }

    /**
     * Available COC Balance =
     *   CocBeginningBalance.accumulatedHours (initial seed balance, if any)
     *   + SUM of approved COC hoursWorked
     *   - SUM of approved CTO hoursUsed
     */
    @Override
    public Double getAvailableBalance(Long employeeId) throws Exception {
        double beginningBalance = 0.0;
        Optional<CocBeginningBalance> begBal = cocBegBalRepository.findByEmployeeId(employeeId);
        if (begBal.isPresent() && begBal.get().getAccumulatedHours() != null) {
            beginningBalance = begBal.get().getAccumulatedHours();
        }
        Double earnedHours = cocRepository.sumApprovedHoursByEmployeeId(employeeId);
        Double usedHours = ctoRepository.sumApprovedHoursUsedByEmployeeId(employeeId);
        return beginningBalance + (earnedHours != null ? earnedHours : 0.0)
                - (usedHours != null ? usedHours : 0.0);
    }

    @Override
    public void generateCertificateCoc(Long cocId, OutputStream out) throws Exception {
        JasperReport report = compile("reports/CertificateCOC.jrxml");

        Map<String, Object> params = new HashMap<>();
        params.put("COC_ID", cocId);

        try (Connection conn = dataSource.getConnection()) {
            JasperPrint print = JasperFillManager.fillReport(report, params, conn);
            JasperExportManager.exportReportToPdfStream(print, out);
        }
    }

    private JasperReport compile(String classpathPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathPath);
        try (InputStream is = resource.getInputStream()) {
            return JasperCompileManager.compileReport(is);
        }
    }
}
