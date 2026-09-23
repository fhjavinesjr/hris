package com.humanresource.services;

import com.humanresource.dtos.OvertimeRequestDTO;
import com.humanresource.dtos.StaffOvertimeRequestDTO;

import java.io.OutputStream;
import java.util.List;

public interface OvertimeRequestService {

    OvertimeRequestDTO create(OvertimeRequestDTO dto) throws Exception;

    OvertimeRequestDTO createEmergencyOverride(OvertimeRequestDTO dto) throws Exception;

    OvertimeRequestDTO createStaffRequest(StaffOvertimeRequestDTO dto, Long filedByEmployeeId,
                                          String bearerToken) throws Exception;

    OvertimeRequestDTO updateStaffRequest(String groupRequestId, StaffOvertimeRequestDTO dto,
                                          Long filedByEmployeeId, String bearerToken) throws Exception;

    Boolean deleteStaffRequest(String groupRequestId, Long filedByEmployeeId,
                               String bearerToken) throws Exception;

    OvertimeRequestDTO administrativeUpdateStaffRequest(String groupRequestId,
                                                         OvertimeRequestDTO dto) throws Exception;

    Boolean administrativeDeleteStaffRequest(String groupRequestId) throws Exception;

    List<OvertimeRequestDTO> getAll() throws Exception;

    List<OvertimeRequestDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    List<OvertimeRequestDTO> getStaffRequestsFiledBy(Long employeeId) throws Exception;

    OvertimeRequestDTO reportDiscrepancy(Long overtimeRequestId, Long employeeId, String remarks) throws Exception;

    List<OvertimeRequestDTO> getPendingAll() throws Exception;

    /** Returns only Approved OT requests for an employee — used by COC filing to populate dropdown. */
    List<OvertimeRequestDTO> getApprovedByEmployeeId(Long employeeId) throws Exception;

    OvertimeRequestDTO approve(Long overtimeRequestId, Long approvedById, String remarks,
                               String bearerToken) throws Exception;

    OvertimeRequestDTO disapprove(Long overtimeRequestId, Long approvedById, String remarks,
                                  String bearerToken) throws Exception;

    OvertimeRequestDTO recommend(Long overtimeRequestId, Long recommendedById, String remarks,
                                 String dutyShiftCode, Integer breakMinutes,
                                 String bearerToken) throws Exception;

    OvertimeRequestDTO update(Long overtimeRequestId, OvertimeRequestDTO dto) throws Exception;

    OvertimeRequestDTO administrativeUpdate(Long overtimeRequestId, OvertimeRequestDTO dto) throws Exception;

    Boolean delete(Long overtimeRequestId) throws Exception;

    Boolean administrativeDelete(Long overtimeRequestId) throws Exception;

    void generateOvertimeAuthorization(Long overtimeRequestId, OutputStream out) throws Exception;
}
