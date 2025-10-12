package com.humanresource.services;

import com.humanresource.dtos.PersonalDataDTO;
import com.humanresource.entitymodels.PersonalData;

public interface PersonalDataService {

    PersonalDataDTO createPersonalData(PersonalDataDTO personalDataDTO);

    PersonalData getPersonalDataByEmployeeId(String employeeId) throws Exception;

    String updatePersonalData(String employeeId, PersonalData personalData) throws Exception;

    String deletePersonalData(String employeeId) throws Exception;

}
