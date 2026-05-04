package com.humanresource.impl;

import com.humanresource.dtos.LeaveMonetizationDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.LeaveInformation;
import com.humanresource.entitymodels.LeaveMonetization;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveInformationRepository;
import com.humanresource.repositories.LeaveMonetizationRepository;
import com.humanresource.services.LeaveMonetizationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveMonetizationImpl implements LeaveMonetizationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveMonetizationImpl.class);

    private final LeaveMonetizationRepository leaveMonetizationRepository;
    private final LeaveInformationRepository leaveInformationRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveMonetizationImpl(LeaveMonetizationRepository leaveMonetizationRepository,
                                 LeaveInformationRepository leaveInformationRepository,
                                 EmployeeRepository employeeRepository) {
        this.leaveMonetizationRepository = leaveMonetizationRepository;
        this.leaveInformationRepository = leaveInformationRepository;
        this.employeeRepository = employeeRepository;
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    private LeaveMonetizationDTO toDTO(LeaveMonetization entity) {
        LeaveMonetizationDTO dto = new LeaveMonetizationDTO();
        dto.setLeaveMonetizationId(entity.getLeaveMonetizationId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setDateFiled(entity.getDateFiled());
        dto.setNoOfDaysSL(entity.getNoOfDaysSL());
        dto.setNoOfDaysVL(entity.getNoOfDaysVL());
        dto.setTotalDays(entity.getTotalDays());
        dto.setSlBalanceBefore(entity.getSlBalanceBefore());
        dto.setVlBalanceBefore(entity.getVlBalanceBefore());
        dto.setSlBalanceAfter(entity.getSlBalanceAfter());
        dto.setVlBalanceAfter(entity.getVlBalanceAfter());
        dto.setReason(entity.getReason());
        dto.setRecommendationStatus(entity.getRecommendationStatus());
        dto.setRecommendedById(entity.getRecommendedById());
        dto.setRecommendedAt(entity.getRecommendedAt());
        dto.setRecommendationRemarks(entity.getRecommendationRemarks());
        dto.setApprovalStatus(entity.getApprovalStatus());
        dto.setApprovedById(entity.getApprovedById());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setApprovalRemarks(entity.getApprovalRemarks());
        dto.setPayrollIncluded(entity.getPayrollIncluded());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    /** Enrich DTO with employee name and number for display. */
    private void enrichEmployeeInfo(LeaveMonetizationDTO dto) {
        employeeRepository.findById(dto.getEmployeeId()).ifPresent(emp -> {
            dto.setEmployeeName(emp.getLastname() + ", " + emp.getFirstname());
            dto.setEmployeeNo(emp.getEmployeeNo());
        });
    }

    // ─── Current balance snapshot ─────────────────────────────────────────────

    /**
     * Returns the employee's SL balance from the latest processed LeaveInformation record.
     * Returns 0.0 if no record exists yet.
     */
    private double currentSLBalance(Long employeeId) {
        Optional<LeaveInformation> latest = leaveInformationRepository
                .findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(
                        employeeId, LocalDate.now().plusYears(100));
        return latest.map(li -> li.getSickLeaveBalance() != null ? li.getSickLeaveBalance() : 0.0).orElse(0.0);
    }

    /**
     * Returns the employee's VL balance from the latest processed LeaveInformation record.
     * Returns 0.0 if no record exists yet.
     */
    private double currentVLBalance(Long employeeId) {
        Optional<LeaveInformation> latest = leaveInformationRepository
                .findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(
                        employeeId, LocalDate.now().plusYears(100));
        return latest.map(li -> li.getVacationLeaveBalance() != null ? li.getVacationLeaveBalance() : 0.0).orElse(0.0);
    }

    // ─── Service methods ─────────────────────────────────────────────────────

    @Transactional
    @Override
    public LeaveMonetizationDTO create(LeaveMonetizationDTO dto) throws Exception {
        if (dto.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId is required");
        }

        double noOfDaysSL = dto.getNoOfDaysSL() != null ? dto.getNoOfDaysSL() : 0.0;
        double noOfDaysVL = dto.getNoOfDaysVL() != null ? dto.getNoOfDaysVL() : 0.0;
        double totalDays = noOfDaysSL + noOfDaysVL;

        if (totalDays <= 0) {
            throw new IllegalArgumentException("Total days to monetize must be greater than zero");
        }

        try {
            // Snapshot the current leave balance at filing time
            double slBefore = currentSLBalance(dto.getEmployeeId());
            double vlBefore = currentVLBalance(dto.getEmployeeId());

            LeaveMonetization entity = new LeaveMonetization();
            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled() != null ? dto.getDateFiled() : LocalDate.now());
            entity.setNoOfDaysSL(noOfDaysSL);
            entity.setNoOfDaysVL(noOfDaysVL);
            entity.setTotalDays(totalDays);
            entity.setSlBalanceBefore(slBefore);
            entity.setVlBalanceBefore(vlBefore);
            entity.setReason(dto.getReason());
            entity.setRecommendationStatus(dto.getRecommendationStatus() != null ? dto.getRecommendationStatus() : "Pending");
            entity.setRecommendedById(dto.getRecommendedById());
            entity.setRecommendationRemarks(dto.getRecommendationRemarks());
            entity.setApprovalStatus(dto.getApprovalStatus() != null ? dto.getApprovalStatus() : "Pending");
            entity.setApprovedById(dto.getApprovedById());
            entity.setApprovalRemarks(dto.getApprovalRemarks());
            entity.setPayrollIncluded(false);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            entity = leaveMonetizationRepository.save(entity);
            LeaveMonetizationDTO result = toDTO(entity);
            enrichEmployeeInfo(result);
            return result;
        } catch (Exception ex) {
            log.error("Error creating LeaveMonetization for employeeId {}: ", dto.getEmployeeId(), ex);
            throw ex;
        }
    }

    @Override
    public List<LeaveMonetizationDTO> getAll() throws Exception {
        return leaveMonetizationRepository.findAll()
                .stream()
                .map(e -> {
                    LeaveMonetizationDTO d = toDTO(e);
                    enrichEmployeeInfo(d);
                    return d;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveMonetizationDTO> getAllByEmployeeId(Long employeeId) throws Exception {
        return leaveMonetizationRepository.findByEmployeeIdOrderByDateFiledDesc(employeeId)
                .stream()
                .map(e -> {
                    LeaveMonetizationDTO d = toDTO(e);
                    enrichEmployeeInfo(d);
                    return d;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveMonetizationDTO> getPending() throws Exception {
        return leaveMonetizationRepository.findByApprovalStatusOrderByDateFiledDesc("Pending")
                .stream()
                .map(e -> {
                    LeaveMonetizationDTO d = toDTO(e);
                    enrichEmployeeInfo(d);
                    return d;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public LeaveMonetizationDTO recommend(Long leaveMonetizationId, Long recommendedById, String remarks) throws Exception {
        LeaveMonetization entity = leaveMonetizationRepository.findById(leaveMonetizationId)
                .orElseThrow(() -> new RuntimeException("LeaveMonetization not found with id: " + leaveMonetizationId));

        if (!"Pending".equals(entity.getRecommendationStatus())) {
            throw new IllegalStateException("Recommendation has already been acted upon (status: " + entity.getRecommendationStatus() + ")");
        }

        entity.setRecommendationStatus("Approved");
        entity.setRecommendedById(recommendedById);
        entity.setRecommendedAt(LocalDateTime.now());
        entity.setRecommendationRemarks(remarks);
        entity.setUpdatedAt(LocalDateTime.now());

        entity = leaveMonetizationRepository.save(entity);
        LeaveMonetizationDTO result = toDTO(entity);
        enrichEmployeeInfo(result);
        return result;
    }

    @Transactional
    @Override
    public LeaveMonetizationDTO disapproveRecommendation(Long leaveMonetizationId, String remarks) throws Exception {
        LeaveMonetization entity = leaveMonetizationRepository.findById(leaveMonetizationId)
                .orElseThrow(() -> new RuntimeException("LeaveMonetization not found with id: " + leaveMonetizationId));

        if (!"Pending".equals(entity.getRecommendationStatus())) {
            throw new IllegalStateException("Recommendation has already been acted upon (status: " + entity.getRecommendationStatus() + ")");
        }

        entity.setRecommendationStatus("Disapproved");
        entity.setRecommendedAt(LocalDateTime.now());
        entity.setRecommendationRemarks(remarks);
        entity.setUpdatedAt(LocalDateTime.now());

        entity = leaveMonetizationRepository.save(entity);
        LeaveMonetizationDTO result = toDTO(entity);
        enrichEmployeeInfo(result);
        return result;
    }

    /**
     * Final approval — enforces CSC MC No. 41 s. 1998 rules:
     *   1. Total days must be >= 10
     *   2. VL reserve: vlBalanceBefore - noOfDaysVL >= 5
     *   3. SL reserve (when SL is deducted): slBalanceBefore - noOfDaysSL >= 5
     */
    @Transactional
    @Override
    public LeaveMonetizationDTO approve(Long leaveMonetizationId, Long approvedById, String remarks) throws Exception {
        LeaveMonetization entity = leaveMonetizationRepository.findById(leaveMonetizationId)
                .orElseThrow(() -> new RuntimeException("LeaveMonetization not found with id: " + leaveMonetizationId));

        if ("Approved".equals(entity.getApprovalStatus())) {
            throw new IllegalStateException("This leave monetization is already approved");
        }
        if ("Disapproved".equals(entity.getApprovalStatus())) {
            throw new IllegalStateException("This leave monetization has been disapproved and cannot be approved");
        }

        double totalDays = entity.getTotalDays() != null ? entity.getTotalDays() : 0.0;
        double noOfDaysSL = entity.getNoOfDaysSL() != null ? entity.getNoOfDaysSL() : 0.0;
        double noOfDaysVL = entity.getNoOfDaysVL() != null ? entity.getNoOfDaysVL() : 0.0;
        double slBefore = entity.getSlBalanceBefore() != null ? entity.getSlBalanceBefore() : 0.0;
        double vlBefore = entity.getVlBalanceBefore() != null ? entity.getVlBalanceBefore() : 0.0;

        // CSC Rule 1: Minimum 10 days (CSC MC No. 41 s. 1998)
        if (totalDays < 10.0) {
            throw new IllegalArgumentException(
                    "Leave monetization requires a minimum of 10 days. Total days filed: " + totalDays);
        }

        // CSC Rule 2: VL mandatory reserve — at least 5 days VL must remain after deduction
        double vlAfter = vlBefore - noOfDaysVL;
        if (vlAfter < 5.0) {
            throw new IllegalArgumentException(
                    "VL balance after monetization (" + vlAfter + " days) must be at least 5 days " +
                    "(CSC mandatory reserve). Current VL balance: " + vlBefore + " days.");
        }

        // CSC Rule 3: SL mandatory reserve — at least 5 days SL must remain after deduction (when SL is deducted)
        double slAfter = slBefore - noOfDaysSL;
        if (noOfDaysSL > 0 && slAfter < 5.0) {
            throw new IllegalArgumentException(
                    "SL balance after monetization (" + slAfter + " days) must be at least 5 days " +
                    "(CSC mandatory reserve). Current SL balance: " + slBefore + " days.");
        }

        // All CSC rules passed — record the deduction results and approval
        entity.setSlBalanceAfter(round3(slAfter));
        entity.setVlBalanceAfter(round3(vlAfter));
        entity.setApprovalStatus("Approved");
        entity.setApprovedById(approvedById);
        entity.setApprovedAt(LocalDate.now());
        entity.setApprovalRemarks(remarks);
        entity.setUpdatedAt(LocalDateTime.now());

        entity = leaveMonetizationRepository.save(entity);
        LeaveMonetizationDTO result = toDTO(entity);
        enrichEmployeeInfo(result);
        return result;
    }

    @Transactional
    @Override
    public LeaveMonetizationDTO disapprove(Long leaveMonetizationId, String remarks) throws Exception {
        LeaveMonetization entity = leaveMonetizationRepository.findById(leaveMonetizationId)
                .orElseThrow(() -> new RuntimeException("LeaveMonetization not found with id: " + leaveMonetizationId));

        if ("Approved".equals(entity.getApprovalStatus())) {
            throw new IllegalStateException("Cannot disapprove an already approved leave monetization");
        }

        entity.setApprovalStatus("Disapproved");
        entity.setApprovedAt(LocalDate.now());
        entity.setApprovalRemarks(remarks);
        entity.setUpdatedAt(LocalDateTime.now());

        entity = leaveMonetizationRepository.save(entity);
        LeaveMonetizationDTO result = toDTO(entity);
        enrichEmployeeInfo(result);
        return result;
    }

    @Transactional
    @Override
    public Boolean delete(Long leaveMonetizationId) throws Exception {
        LeaveMonetization entity = leaveMonetizationRepository.findById(leaveMonetizationId)
                .orElseThrow(() -> new RuntimeException("LeaveMonetization not found with id: " + leaveMonetizationId));

        if ("Approved".equals(entity.getApprovalStatus())) {
            throw new IllegalStateException("Cannot delete an approved leave monetization");
        }

        try {
            leaveMonetizationRepository.deleteById(leaveMonetizationId);
            return true;
        } catch (Exception ex) {
            log.error("Error deleting LeaveMonetization {}: ", leaveMonetizationId, ex);
            return false;
        }
    }

    @Transactional
    @Override
    public LeaveMonetizationDTO update(Long leaveMonetizationId, LeaveMonetizationDTO dto) throws Exception {
        LeaveMonetization entity = leaveMonetizationRepository.findById(leaveMonetizationId)
                .orElseThrow(() -> new RuntimeException("LeaveMonetization not found with id: " + leaveMonetizationId));
        if (dto.getDateFiled() != null) entity.setDateFiled(dto.getDateFiled());
        if (dto.getNoOfDaysSL() != null) entity.setNoOfDaysSL(dto.getNoOfDaysSL());
        if (dto.getNoOfDaysVL() != null) entity.setNoOfDaysVL(dto.getNoOfDaysVL());
        double total = (dto.getNoOfDaysVL() != null ? dto.getNoOfDaysVL() : entity.getNoOfDaysVL())
                     + (dto.getNoOfDaysSL() != null ? dto.getNoOfDaysSL() : entity.getNoOfDaysSL());
        entity.setTotalDays(round3(total));
        if (dto.getReason() != null) entity.setReason(dto.getReason());
        if (dto.getRecommendationStatus() != null) entity.setRecommendationStatus(dto.getRecommendationStatus());
        if (dto.getApprovalStatus() != null) entity.setApprovalStatus(dto.getApprovalStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        entity = leaveMonetizationRepository.save(entity);
        LeaveMonetizationDTO result = toDTO(entity);
        enrichEmployeeInfo(result);
        return result;
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    private static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
