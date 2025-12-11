package com.administrative.services;

import com.administrative.dtos.NatureOfSeparationDTO;

import java.util.List;

public interface NatureOfSeparationService {

    NatureOfSeparationDTO createNatureOfSeparation(NatureOfSeparationDTO natureOfSeparationDTO) throws Exception;

    List<NatureOfSeparationDTO> getAllNatureOfSeparation() throws Exception;

    NatureOfSeparationDTO getNatureOfSeparationById(Long natureOfSeparationId) throws Exception;

    NatureOfSeparationDTO updateNatureOfSeparation(Long natureOfSeparationId, NatureOfSeparationDTO natureOfSeparationDTO) throws Exception;

    Boolean deleteNatureOfSeparation(Long natureOfSeparationId) throws Exception;

}
