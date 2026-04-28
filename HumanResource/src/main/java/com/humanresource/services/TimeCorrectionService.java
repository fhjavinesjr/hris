package com.humanresource.services;

import com.humanresource.dtos.TimeCorrectionDTO;

import java.util.List;

public interface TimeCorrectionService {

    TimeCorrectionDTO create(TimeCorrectionDTO dto) throws Exception;

    List<TimeCorrectionDTO> getAll() throws Exception;

    List<TimeCorrectionDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    List<TimeCorrectionDTO> getPendingAll() throws Exception;

    TimeCorrectionDTO approve(Long id, Long approvedById, String remarks) throws Exception;

    TimeCorrectionDTO disapprove(Long id, Long approvedById, String remarks) throws Exception;

    TimeCorrectionDTO update(Long id, TimeCorrectionDTO dto) throws Exception;

    Boolean delete(Long id) throws Exception;
}
