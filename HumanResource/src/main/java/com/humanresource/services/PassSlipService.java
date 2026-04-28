package com.humanresource.services;

import com.humanresource.dtos.PassSlipDTO;

import java.util.List;

public interface PassSlipService {

    PassSlipDTO create(PassSlipDTO dto) throws Exception;

    List<PassSlipDTO> getAll() throws Exception;

    List<PassSlipDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    List<PassSlipDTO> getPendingAll() throws Exception;

    PassSlipDTO approve(Long passSlipId, Long approvedById, String remarks) throws Exception;

    PassSlipDTO disapprove(Long passSlipId, Long approvedById, String remarks) throws Exception;

    PassSlipDTO update(Long passSlipId, PassSlipDTO dto) throws Exception;

    Boolean delete(Long passSlipId) throws Exception;
}
