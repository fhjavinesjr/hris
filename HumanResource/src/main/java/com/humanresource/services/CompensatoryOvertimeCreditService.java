package com.humanresource.services;

import com.humanresource.dtos.CompensatoryOvertimeCreditDTO;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface CompensatoryOvertimeCreditService {

    CompensatoryOvertimeCreditDTO create(CompensatoryOvertimeCreditDTO dto) throws Exception;

    List<CompensatoryOvertimeCreditDTO> getAll() throws Exception;

    List<CompensatoryOvertimeCreditDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    List<CompensatoryOvertimeCreditDTO> getPendingAll() throws Exception;

    CompensatoryOvertimeCreditDTO approve(Long cocId, Long approvedById, String remarks) throws Exception;

    CompensatoryOvertimeCreditDTO disapprove(Long cocId, Long approvedById, String remarks) throws Exception;

    CompensatoryOvertimeCreditDTO recommend(Long cocId, Long recommendedById, String remarks) throws Exception;

    CompensatoryOvertimeCreditDTO update(Long cocId, CompensatoryOvertimeCreditDTO dto) throws Exception;

    Boolean delete(Long cocId) throws Exception;

    /**
     * Returns the total available COC balance for an employee:
     * CocBeginningBalance.accumulatedHours + approved COC hours earned - CTO hours used.
     */
    Double getAvailableBalance(Long employeeId) throws Exception;

    Map<String, Object> previewFromOvertimeRequest(Long overtimeRequestId, Long employeeId) throws Exception;

    void generateCertificateCoc(Long cocId, OutputStream out) throws Exception;
}
