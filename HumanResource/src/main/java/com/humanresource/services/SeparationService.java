package com.humanresource.services;

import com.humanresource.dtos.SeparationDTO;

import java.util.List;

public interface SeparationService {

    SeparationDTO createSeparation(SeparationDTO SeparationDTO) throws Exception;

    List<SeparationDTO> getAllSeparation() throws Exception;

    List<SeparationDTO> getAllSeparationByEmployeeId(Long employeeId) throws Exception;

    SeparationDTO getLatestSeparationByEmployeeId(Long employeeId) throws Exception;

    SeparationDTO getSeparationById(Long SeparationId) throws Exception;

    SeparationDTO updateSeparation(Long SeparationId, SeparationDTO SeparationDTO) throws Exception;

    Boolean deleteSeparation(Long SeparationId) throws Exception;

}
