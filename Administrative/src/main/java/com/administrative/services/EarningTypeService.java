package com.administrative.services;

import com.administrative.dtos.EarningTypeDTO;

import java.util.List;

public interface EarningTypeService {

    EarningTypeDTO createEarningType(EarningTypeDTO earningTypeDTO) throws Exception;

    List<EarningTypeDTO> getAllEarningTypes() throws Exception;

    EarningTypeDTO updateEarningType(Long earningTypeId, EarningTypeDTO earningTypeDTO) throws Exception;

    Boolean deleteEarningType(Long earningTypeId) throws Exception;
}
