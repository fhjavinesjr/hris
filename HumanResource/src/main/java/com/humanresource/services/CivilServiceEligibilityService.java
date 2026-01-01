package com.humanresource.services;

import com.humanresource.dtos.CivilServiceEligibilityDTO;

import java.util.List;
import java.util.Map;

public interface CivilServiceEligibilityService {

    CivilServiceEligibilityDTO createCivilServiceEligibility(CivilServiceEligibilityDTO civilServiceEligibilityDTO);

    CivilServiceEligibilityDTO getCivilServiceEligibilityByCivilServiceEligibilityId(Long civilServiceEligibilityId) throws Exception;

    List<CivilServiceEligibilityDTO> getCivilServiceEligibilityByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateCivilServiceEligibility(Long civilServiceEligibilityId, Map<String, Object> updates) throws Exception;

    Boolean deleteCivilServiceEligibility(Long civilServiceEligibilityId) throws Exception;

}