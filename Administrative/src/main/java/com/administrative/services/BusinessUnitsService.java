package com.administrative.services;

import com.administrative.dtos.BusinessUnitsDTO;

import java.util.List;

public interface BusinessUnitsService {

    BusinessUnitsDTO createBusinessUnits(BusinessUnitsDTO BusinessUnitsDTO) throws Exception;

    List<BusinessUnitsDTO> getAllBusinessUnits() throws Exception;

    BusinessUnitsDTO getBusinessUnitsById(Long businessUnitsId) throws Exception;

    BusinessUnitsDTO updateBusinessUnits(Long businessUnitsId, BusinessUnitsDTO BusinessUnitsDTO) throws Exception;

    Boolean deleteBusinessUnits(Long businessUnitsId) throws Exception;

}
