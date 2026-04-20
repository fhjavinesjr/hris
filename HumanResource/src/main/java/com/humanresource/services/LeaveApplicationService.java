package com.humanresource.services;

import com.humanresource.dtos.LeaveApplicationDTO;

import java.util.List;

public interface LeaveApplicationService {

    LeaveApplicationDTO createLeaveApplication(LeaveApplicationDTO leaveApplicationDTO) throws Exception;

    List<LeaveApplicationDTO> getAllLeaveApplications() throws Exception;

    List<LeaveApplicationDTO> getAllLeaveApplicationsByEmployeeId(Long employeeId) throws Exception;

    List<LeaveApplicationDTO> getAllLeaveApplicationsByEmployeeIdAndLeaveType(Long employeeId, String leaveType) throws Exception;

    LeaveApplicationDTO getLeaveApplicationById(Long leaveApplicationId) throws Exception;

    LeaveApplicationDTO updateLeaveApplication(Long leaveApplicationId, LeaveApplicationDTO leaveApplicationDTO) throws Exception;

    Boolean deleteLeaveApplication(Long leaveApplicationId) throws Exception;

}
