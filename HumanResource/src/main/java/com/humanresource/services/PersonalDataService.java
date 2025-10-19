package com.humanresource.services;

import com.humanresource.dtos.PersonalDataDTO;
import com.humanresource.entitymodels.PersonalData;

import java.util.Map;

public interface PersonalDataService {

    PersonalDataDTO createPersonalData(PersonalDataDTO personalDataDTO);

    PersonalDataDTO getPersonalDataByEmployeeId(Long employeeId) throws Exception;

    Boolean updatePersonalData(Long employeeId, Map<String, Object> updates) throws Exception;

    String deletePersonalData(String employeeId) throws Exception;

}
