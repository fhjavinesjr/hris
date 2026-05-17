package com.administrative.services;

import com.administrative.dtos.DeductionTypeDTO;

import java.util.List;

public interface DeductionTypeService {

    DeductionTypeDTO createDeductionType(DeductionTypeDTO deductionTypeDTO) throws Exception;

    List<DeductionTypeDTO> getAllDeductionTypes() throws Exception;

    DeductionTypeDTO updateDeductionType(Long deductionTypeId, DeductionTypeDTO deductionTypeDTO) throws Exception;

    Boolean deleteDeductionType(Long deductionTypeId) throws Exception;
}
