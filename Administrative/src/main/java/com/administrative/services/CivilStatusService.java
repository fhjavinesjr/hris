package com.administrative.services;

import com.administrative.dtos.CivilStatusDTO;

import java.util.List;

public interface CivilStatusService {

    CivilStatusDTO createCivilStatus(CivilStatusDTO civilStatusDTO) throws Exception;

    List<CivilStatusDTO> getAllCivilStatus() throws Exception;

    CivilStatusDTO getCivilStatusById(Long civilStatusId) throws Exception;

    CivilStatusDTO updateCivilStatus(Long civilStatusId, CivilStatusDTO civilStatusDTO) throws Exception;

    Boolean deleteCivilStatus(Long civilStatusId) throws Exception;

}
