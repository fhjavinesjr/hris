package com.humanresource.services;

import com.humanresource.dtos.CompensatoryTimeOffDTO;

import java.util.List;

public interface CompensatoryTimeOffService {

    CompensatoryTimeOffDTO create(CompensatoryTimeOffDTO dto) throws Exception;

    List<CompensatoryTimeOffDTO> getAll() throws Exception;

    List<CompensatoryTimeOffDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    List<CompensatoryTimeOffDTO> getPendingAll() throws Exception;

    CompensatoryTimeOffDTO approve(Long ctoId, Long approvedById, String remarks) throws Exception;

    CompensatoryTimeOffDTO disapprove(Long ctoId, Long approvedById, String remarks) throws Exception;

    CompensatoryTimeOffDTO update(Long ctoId, CompensatoryTimeOffDTO dto) throws Exception;

    Boolean delete(Long ctoId) throws Exception;
}
