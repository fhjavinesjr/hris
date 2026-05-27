package com.humanresource.impl;

import com.humanresource.dtos.ApprovedLeaveDTO;
import com.humanresource.dtos.LeaveApplicationDTO;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.services.DateConflictChecker;
import com.humanresource.services.LeaveApplicationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveApplicationImpl implements LeaveApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationImpl.class);
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final DateConflictChecker conflictChecker;
    private final JdbcTemplate jdbcTemplate;

    public LeaveApplicationImpl(LeaveApplicationRepository leaveApplicationRepository,
                                DateConflictChecker conflictChecker,
                                JdbcTemplate jdbcTemplate) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.conflictChecker = conflictChecker;
        this.jdbcTemplate = jdbcTemplate;
    }

    private LeaveApplicationDTO toDTO(LeaveApplication entity) {
        LeaveApplicationDTO dto = new LeaveApplicationDTO();
        dto.setLeaveApplicationId(entity.getLeaveApplicationId());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setDateFiled(entity.getDateFiled());
        dto.setLeaveType(entity.getLeaveType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setNoOfDays(entity.getNoOfDays());
        dto.setCommutation(entity.getCommutation());
        dto.setDetails(entity.getDetails());
        dto.setStatus(entity.getStatus());
        dto.setRecommendingApprovalById(entity.getRecommendingApprovalById());
        dto.setAuthorizedOfficialId(entity.getAuthorizedOfficialId());
        dto.setApprovedById(entity.getApprovedById());
        dto.setRecommendationStatus(entity.getRecommendationStatus());
        dto.setRecommendationMessage(entity.getRecommendationMessage());
        dto.setApprovedStatus(entity.getApprovedStatus());
        dto.setApprovalMessage(entity.getApprovalMessage());
        dto.setDueExigencyService(entity.getDueExigencyService());
        return dto;
    }

    @Transactional
    @Override
    public LeaveApplicationDTO createLeaveApplication(LeaveApplicationDTO dto) throws Exception {
        // Validate BEFORE try-catch so IllegalArgumentException propagates to GlobalExceptionHandler
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            conflictChecker.checkDateRange(dto.getEmployeeId(), dto.getStartDate(), dto.getEndDate());
        }
        try {
            LeaveApplication entity = new LeaveApplication(
                    null,
                    dto.getEmployeeId(),
                    dto.getDateFiled(),
                    dto.getLeaveType(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    dto.getNoOfDays(),
                    dto.getCommutation(),
                    dto.getDetails(),
                    dto.getStatus() != null ? dto.getStatus() : "Pending",
                    dto.getRecommendingApprovalById(),
                    dto.getAuthorizedOfficialId(),
                    dto.getApprovedById(),
                    dto.getRecommendationStatus(),
                    dto.getRecommendationMessage(),
                    dto.getApprovedStatus(),
                    dto.getApprovalMessage(),
                    dto.getDueExigencyService()
            );

            entity = leaveApplicationRepository.save(entity);
            return toDTO(entity);
        } catch (Exception e) {
            log.error("Error creating LeaveApplication: ", e);
            return null;
        }
    }

    @Override
    public List<LeaveApplicationDTO> getAllLeaveApplications() throws Exception {
        List<LeaveApplication> list = leaveApplicationRepository.findAll();
        List<LeaveApplicationDTO> dtoList = new ArrayList<>();
        for (LeaveApplication entity : list) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    @Override
    public List<LeaveApplicationDTO> getAllLeaveApplicationsByEmployeeId(Long employeeId) throws Exception {
        List<LeaveApplication> list = leaveApplicationRepository.findByEmployeeId(employeeId);
        List<LeaveApplicationDTO> dtoList = new ArrayList<>();
        for (LeaveApplication entity : list) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    @Override
    public List<LeaveApplicationDTO> getAllLeaveApplicationsByEmployeeIdAndLeaveType(Long employeeId, String leaveType) throws Exception {
        List<LeaveApplication> list = leaveApplicationRepository.findByEmployeeIdAndLeaveType(employeeId, leaveType);
        List<LeaveApplicationDTO> dtoList = new ArrayList<>();
        for (LeaveApplication entity : list) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    @Override
    public LeaveApplicationDTO getLeaveApplicationById(Long leaveApplicationId) throws Exception {
        Optional<LeaveApplication> optional = leaveApplicationRepository.findById(leaveApplicationId);
        return optional.map(this::toDTO).orElse(null);
    }

    @Transactional
    @Override
    public LeaveApplicationDTO updateLeaveApplication(Long leaveApplicationId, LeaveApplicationDTO dto) throws Exception {
        try {
            LeaveApplication entity = leaveApplicationRepository.findById(leaveApplicationId)
                    .orElseThrow(() -> new RuntimeException("LeaveApplication not found with id: " + leaveApplicationId));

            entity.setEmployeeId(dto.getEmployeeId());
            entity.setDateFiled(dto.getDateFiled());
            entity.setLeaveType(dto.getLeaveType());
            entity.setStartDate(dto.getStartDate());
            entity.setEndDate(dto.getEndDate());
            entity.setNoOfDays(dto.getNoOfDays());
            entity.setCommutation(dto.getCommutation());
            entity.setDetails(dto.getDetails());
            entity.setStatus(dto.getStatus());
            entity.setRecommendingApprovalById(dto.getRecommendingApprovalById());
            entity.setAuthorizedOfficialId(dto.getAuthorizedOfficialId());
            entity.setApprovedById(dto.getApprovedById());
            entity.setRecommendationStatus(dto.getRecommendationStatus());
            entity.setRecommendationMessage(dto.getRecommendationMessage());
            entity.setApprovedStatus(dto.getApprovedStatus());
            entity.setApprovalMessage(dto.getApprovalMessage());
            entity.setDueExigencyService(dto.getDueExigencyService());

            entity = leaveApplicationRepository.save(entity);
            return toDTO(entity);
        } catch (Exception e) {
            log.error("Error updating LeaveApplication: ", e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deleteLeaveApplication(Long leaveApplicationId) throws Exception {
        try {
            leaveApplicationRepository.deleteById(leaveApplicationId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting LeaveApplication: ", e);
            return false;
        }
    }

    @Override
    public List<ApprovedLeaveDTO> getBulkApprovedLeaves(LocalDate from, LocalDate to) throws Exception {
        // SQL query to get approved leaves and expand date ranges into individual dates
        String sql = """
            WITH DateSequence AS (
                SELECT 
                    la.leaveApplicationId,
                    la.employeeId,
                    la.leaveType,
                    la.startDate,
                    la.endDate,
                    la.noOfDays,
                    la.startDate AS leaveDate
                FROM leave_application la
                WHERE la.status = 'Approved'
                  AND la.approvedStatus = 'Approved'
                  AND la.startDate <= ?
                  AND la.endDate >= ?
                
                UNION ALL
                
                SELECT 
                    ds.leaveApplicationId,
                    ds.employeeId,
                    ds.leaveType,
                    ds.startDate,
                    ds.endDate,
                    ds.noOfDays,
                    DATEADD(day, 1, ds.leaveDate)
                FROM DateSequence ds
                WHERE ds.leaveDate < ds.endDate
            )
            SELECT 
                e.employeeNo,
                ds.leaveDate,
                ds.leaveType,
                CAST(1 AS BIT) AS withPay,
                'WHOLEDAY' AS workDayType,
                ds.noOfDays
            FROM DateSequence ds
            INNER JOIN employee e ON e.employeeId = ds.employeeId
            WHERE ds.leaveDate BETWEEN ? AND ?
            ORDER BY e.employeeNo, ds.leaveDate
            OPTION (MAXRECURSION 365)
            """;

        List<ApprovedLeaveDTO> result = new ArrayList<>();
        try {
            jdbcTemplate.query(sql, rs -> {
                ApprovedLeaveDTO dto = new ApprovedLeaveDTO();
                dto.setEmployeeNo(rs.getString("employeeNo"));
                dto.setLeaveDate(rs.getDate("leaveDate").toLocalDate());
                dto.setLeaveType(rs.getString("leaveType"));
                dto.setWithPay(rs.getBoolean("withPay"));
                dto.setWorkDayType(rs.getString("workDayType"));
                dto.setNoOfDaysApplied(rs.getDouble("noOfDays"));
                result.add(dto);
            }, to, from, from, to);
            
            log.info("Fetched {} approved leave records from {} to {}", result.size(), from, to);
            return result;
        } catch (Exception e) {
            log.error("Error fetching bulk approved leaves: ", e);
            throw e;
        }
    }
}

