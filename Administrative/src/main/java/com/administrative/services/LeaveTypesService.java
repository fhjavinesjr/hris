package com.administrative.services;

import com.administrative.dtos.LeaveTypesDTO;

import java.util.List;

public interface LeaveTypesService {

    LeaveTypesDTO createLeaveTypes(LeaveTypesDTO leaveTypesDTO) throws Exception;

    List<LeaveTypesDTO> getAllLeaveTypes() throws Exception;

    LeaveTypesDTO getLeaveTypesById(Long leaveTypesId) throws Exception;

    LeaveTypesDTO updateLeaveTypes(Long leaveTypesId, LeaveTypesDTO leaveTypesDTO) throws Exception;

    Boolean deleteLeaveTypes(Long leaveTypesId) throws Exception;

}
