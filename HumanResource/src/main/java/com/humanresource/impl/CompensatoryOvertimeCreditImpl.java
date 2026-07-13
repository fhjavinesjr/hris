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
            entity.setDateFiled(dto.getDateFiled());
            entity.setDateWorked(dto.getDateWorked());
            entity.setHoursWorked(dto.getHoursWorked());
            entity.setReason(dto.getReason());
            entity.setWorkType(dto.getWorkType());
            entity.setStatus(dto.getStatus() != null ? dto.getStatus() : entity.getStatus());
            entity.setApprovedById(dto.getApprovedById());
            entity.setApprovalRemarks(dto.getApprovalRemarks());
            entity.setRecommendationStatus(dto.getRecommendationStatus());
            entity.setRecommendedById(dto.getRecommendedById());
            entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setUpdatedAt(LocalDateTime.now());
            entity = cocRepository.save(entity);
            return toDTO(entity);
        } catch (Exception ex) {
            log.error("Error updating COC id {}: ", cocId, ex);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean delete(Long cocId) throws Exception {
        try {
            if (!cocRepository.existsById(cocId)) return false;
            cocRepository.deleteById(cocId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting COC id {}: ", cocId, ex);
            return false;
        }
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
