package com.humanresource.impl;

import com.humanresource.dtos.ApprovedLeaveDTO;
import com.humanresource.dtos.LeaveApplicationDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.repositories.LeaveApplicationRepository;
import com.humanresource.services.DateConflictChecker;
import com.humanresource.services.LeaveApplicationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeaveApplicationImpl implements LeaveApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LeaveApplicationImpl.class);
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final EmployeeRepository employeeRepository;
    private final DateConflictChecker conflictChecker;

    public LeaveApplicationImpl(LeaveApplicationRepository leaveApplicationRepository,
                                EmployeeRepository employeeRepository,
                                DateConflictChecker conflictChecker) {
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.employeeRepository = employeeRepository;
        this.conflictChecker = conflictChecker;
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
        dto.setWithPay(!Boolean.FALSE.equals(entity.getWithPay()));
        return dto;
    }

    @Transactional
    @Override
    public LeaveApplicationDTO createLeaveApplication(LeaveApplicationDTO dto) throws Exception {
        // Validate BEFORE try-catch so IllegalArgumentException propagates to GlobalExceptionHandler
        validateStatutoryLeaveDuration(dto, null);
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
            entity.setWithPay(!Boolean.FALSE.equals(dto.getWithPay()));

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
        LeaveApplication entity = findLeaveApplication(leaveApplicationId);
        validateStatutoryLeaveDuration(dto, leaveApplicationId);

        entity.setEmployeeId(dto.getEmployeeId());
        entity.setDateFiled(dto.getDateFiled());
        entity.setLeaveType(dto.getLeaveType());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setNoOfDays(dto.getNoOfDays());
        entity.setCommutation(dto.getCommutation());
        entity.setDetails(dto.getDetails());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            entity.setStatus(dto.getStatus());
        }
        entity.setRecommendingApprovalById(dto.getRecommendingApprovalById());
        entity.setAuthorizedOfficialId(dto.getAuthorizedOfficialId());
        entity.setApprovedById(dto.getApprovedById());
        entity.setRecommendationStatus(dto.getRecommendationStatus());
        entity.setRecommendationMessage(dto.getRecommendationMessage());
        entity.setApprovedStatus(dto.getApprovedStatus());
        entity.setApprovalMessage(dto.getApprovalMessage());
        entity.setDueExigencyService(dto.getDueExigencyService());
        entity.setWithPay(!Boolean.FALSE.equals(dto.getWithPay()));

        entity = leaveApplicationRepository.save(entity);
        return toDTO(entity);
    }

    @Transactional
    @Override
    public LeaveApplicationDTO recommendLeaveApplication(
            Long leaveApplicationId,
            Long recommendedById,
            String remarks) throws Exception {
        if (recommendedById == null) {
            throw new IllegalArgumentException("The recommending officer is required.");
        }

        LeaveApplication entity = findPendingLeaveApplication(
                leaveApplicationId,
                "recommended"
        );
        entity.setRecommendationStatus("Recommended");
        entity.setRecommendingApprovalById(recommendedById);
        entity.setRecommendationMessage(normalizeRemarks(remarks));
        return toDTO(leaveApplicationRepository.save(entity));
    }

    @Transactional
    @Override
    public LeaveApplicationDTO approveLeaveApplication(
            Long leaveApplicationId,
            Long approvedById,
            String remarks) throws Exception {
        return finalizeLeaveApplication(
                leaveApplicationId,
                approvedById,
                remarks,
                "Approved"
        );
    }

    @Transactional
    @Override
    public LeaveApplicationDTO disapproveLeaveApplication(
            Long leaveApplicationId,
            Long approvedById,
            String remarks) throws Exception {
        return finalizeLeaveApplication(
                leaveApplicationId,
                approvedById,
                remarks,
                "Disapproved"
        );
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
        List<ApprovedLeaveDTO> result = new ArrayList<>();
        try {
            List<LeaveApplication> approvedLeaves = leaveApplicationRepository
                    .findByStatusAndApprovedStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            "Approved", "Approved", to, from);

            Set<Long> employeeIds = approvedLeaves.stream()
                    .map(LeaveApplication::getEmployeeId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, String> employeeNoById = employeeRepository.findAllById(employeeIds).stream()
                    .collect(Collectors.toMap(Employee::getEmployeeId, Employee::getEmployeeNo));

            for (LeaveApplication leave : approvedLeaves) {
                if (leave.getStartDate() == null || leave.getEndDate() == null) {
                    continue;
                }

                LocalDate effectiveStart = leave.getStartDate().isBefore(from) ? from : leave.getStartDate();
                LocalDate effectiveEnd = leave.getEndDate().isAfter(to) ? to : leave.getEndDate();

                for (LocalDate day = effectiveStart; !day.isAfter(effectiveEnd); day = day.plusDays(1)) {
                    ApprovedLeaveDTO dto = new ApprovedLeaveDTO();
                    dto.setEmployeeNo(employeeNoById.get(leave.getEmployeeId()));
                    dto.setLeaveDate(day);
                    dto.setLeaveType(leave.getLeaveType());
                    dto.setWithPay(!Boolean.FALSE.equals(leave.getWithPay()));
                    dto.setWorkDayType("WHOLEDAY");
                    // The endpoint returns one row per date. Sending the total
                    // application duration on every row would multiply leave
                    // usage during payroll aggregation.
                    dto.setNoOfDaysApplied(
                            leave.getNoOfDays() != null && leave.getNoOfDays() < 1.0
                                    ? leave.getNoOfDays()
                                    : 1.0
                    );
                    result.add(dto);
                }
            }

            result.sort(
                    Comparator.comparing(ApprovedLeaveDTO::getEmployeeNo,
                                    Comparator.nullsLast(String::compareTo))
                            .thenComparing(ApprovedLeaveDTO::getLeaveDate,
                                    Comparator.nullsLast(LocalDate::compareTo))
            );
            
            log.info("Fetched {} approved leave records from {} to {}", result.size(), from, to);
            return result;
        } catch (Exception e) {
            log.error("Error fetching bulk approved leaves: ", e);
            throw e;
        }
    }

    private LeaveApplicationDTO finalizeLeaveApplication(
            Long leaveApplicationId,
            Long approvedById,
            String remarks,
            String finalStatus) {
        if (approvedById == null) {
            throw new IllegalArgumentException("The approving officer is required.");
        }

        LeaveApplication entity = findPendingLeaveApplication(
                leaveApplicationId,
                finalStatus.toLowerCase()
        );
        entity.setStatus(finalStatus);
        entity.setApprovedStatus(finalStatus);
        entity.setApprovedById(approvedById);
        entity.setApprovalMessage(normalizeRemarks(remarks));
        return toDTO(leaveApplicationRepository.save(entity));
    }

    private LeaveApplication findPendingLeaveApplication(
            Long leaveApplicationId,
            String action) {
        LeaveApplication entity = findLeaveApplication(leaveApplicationId);
        if (!"Pending".equalsIgnoreCase(entity.getStatus())) {
            throw new IllegalArgumentException(
                    "Only pending leave applications may be " + action + "."
            );
        }
        return entity;
    }

    private LeaveApplication findLeaveApplication(Long leaveApplicationId) {
        return leaveApplicationRepository.findById(leaveApplicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Leave Application not found with id: " + leaveApplicationId
                ));
    }

    private String normalizeRemarks(String remarks) {
        return remarks == null ? "" : remarks;
    }

    private void validateStatutoryLeaveDuration(LeaveApplicationDTO dto, Long excludedApplicationId) {
        if (dto == null || isInactiveStatus(dto.getStatus(), dto.getApprovedStatus())) {
            return;
        }

        String leaveType = dto.getLeaveType() == null ? "" : dto.getLeaveType().trim();
        boolean guardedType = "Paternity Leave".equalsIgnoreCase(leaveType)
                || "Maternity Leave".equalsIgnoreCase(leaveType)
                || "Solo Parent Leave".equalsIgnoreCase(leaveType);
        if (!guardedType) {
            return;
        }

        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();
        if (start == null || end == null) {
            throw new IllegalArgumentException("Inclusive From and To dates are required for " + leaveType + ".");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("The inclusive To date cannot be earlier than the From date.");
        }

        if ("Paternity Leave".equalsIgnoreCase(leaveType)) {
            int requestedDays = countWeekdays(start, end);
            if (requestedDays > 7) {
                throw new IllegalArgumentException(
                        "Paternity Leave cannot exceed 7 working days per filing. It may be availed continuously or intermittently."
                );
            }
            return;
        }

        if ("Maternity Leave".equalsIgnoreCase(leaveType)) {
            long requestedCalendarDays = ChronoUnit.DAYS.between(start, end) + 1;
            if (requestedCalendarDays > 105) {
                throw new IllegalArgumentException(
                        "Maternity Leave for live childbirth cannot exceed 105 calendar days and must be continuous and uninterrupted."
                );
            }
            return;
        }

        validateSoloParentAnnualLimit(dto.getEmployeeId(), start, end, excludedApplicationId);
    }

    private void validateSoloParentAnnualLimit(Long employeeId, LocalDate start, LocalDate end,
                                               Long excludedApplicationId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee is required for Solo Parent Leave.");
        }

        List<LeaveApplication> existingApplications = leaveApplicationRepository.findByEmployeeId(employeeId);
        for (int year = start.getYear(); year <= end.getYear(); year++) {
            final int leaveYear = year;
            LocalDate yearStart = LocalDate.of(year, 1, 1);
            LocalDate yearEnd = LocalDate.of(year, 12, 31);
            int requestedDays = countWeekdays(
                    start.isAfter(yearStart) ? start : yearStart,
                    end.isBefore(yearEnd) ? end : yearEnd
            );

            int alreadyFiledDays = existingApplications.stream()
                    .filter(application -> excludedApplicationId == null
                            || !excludedApplicationId.equals(application.getLeaveApplicationId()))
                    .filter(application -> "Solo Parent Leave".equalsIgnoreCase(application.getLeaveType()))
                    .filter(application -> !isInactiveStatus(application.getStatus(), application.getApprovedStatus()))
                    .mapToInt(application -> countWeekdaysWithinYear(
                            application.getStartDate(), application.getEndDate(), yearStart, yearEnd))
                    .sum();

            if (alreadyFiledDays + requestedDays > 7) {
                int remainingDays = Math.max(0, 7 - alreadyFiledDays);
                throw new IllegalArgumentException(
                        "Solo Parent Leave is limited to 7 working days in " + leaveYear
                                + ". Only " + remainingDays + " working day(s) remain."
                );
            }
        }
    }

    private int countWeekdaysWithinYear(LocalDate start, LocalDate end,
                                        LocalDate yearStart, LocalDate yearEnd) {
        if (start == null || end == null || end.isBefore(yearStart) || start.isAfter(yearEnd)) {
            return 0;
        }
        return countWeekdays(start.isAfter(yearStart) ? start : yearStart,
                end.isBefore(yearEnd) ? end : yearEnd);
    }

    private int countWeekdays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        int days = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days++;
            }
        }
        return days;
    }

    private boolean isInactiveStatus(String status, String approvedStatus) {
        return isInactiveStatus(status) || isInactiveStatus(approvedStatus);
    }

    private boolean isInactiveStatus(String status) {
        return status != null && ("Disapproved".equalsIgnoreCase(status)
                || "Rejected".equalsIgnoreCase(status)
                || "Cancelled".equalsIgnoreCase(status)
                || "Canceled".equalsIgnoreCase(status));
    }
}

