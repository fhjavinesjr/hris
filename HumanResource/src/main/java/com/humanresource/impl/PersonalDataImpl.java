package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.dtos.PersonalDataDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.PersonalData;
import com.humanresource.repositories.PersonalDataRepository;
import com.humanresource.services.PersonalDataService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PersonalDataImpl implements PersonalDataService {

    private static final Logger log = LoggerFactory.getLogger(PersonalDataImpl.class);
    private final PersonalDataRepository personalDataRepository;
    private final ObjectMapper objectMapper;

    public PersonalDataImpl(PersonalDataRepository personalDataRepository, ObjectMapper objectMapper) {
        this.personalDataRepository = personalDataRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public PersonalDataDTO createPersonalData(PersonalDataDTO personalDataDTO) {
        try {
            PersonalData personalData = new PersonalData(
                    personalDataDTO.getPersonalDataId(),
                    personalDataDTO.getEmployeeId(),
                    personalDataDTO.getSurname(),
                    personalDataDTO.getFirstname(),
                    personalDataDTO.getMiddlename(),
                    personalDataDTO.getExtname(),
                    personalDataDTO.getDob(),
                    personalDataDTO.getPob(),
                    personalDataDTO.getSex_id(),
                    personalDataDTO.getCivilStatus_id(),
                    personalDataDTO.getHeight(),
                    personalDataDTO.getWeight(),
                    personalDataDTO.getBloodType(),
                    personalDataDTO.getGsisId(),
                    personalDataDTO.getPagibigId(),
                    personalDataDTO.getPhilhealthNo(),
                    personalDataDTO.getSssNo(),
                    personalDataDTO.getTinNo(),
                    personalDataDTO.getAgencyEmpNo(),
                    personalDataDTO.getCitizenship(),
                    personalDataDTO.getResAddress(),
                    personalDataDTO.getResZip(),
                    personalDataDTO.getPermAddress(),
                    personalDataDTO.getPermZip(),
                    personalDataDTO.getTelNo(),
                    personalDataDTO.getMobileNo(),
                    personalDataDTO.getEmail(),
                    personalDataDTO.getEmployeePicture(),
                    personalDataDTO.getEmployeeSignature(),
                    personalDataDTO.getSpouseSurname(),
                    personalDataDTO.getSpouseFirstname(),
                    personalDataDTO.getSpouseMiddlename(),
                    personalDataDTO.getSpouseOccupation(),
                    personalDataDTO.getSpouseEmployer(),
                    personalDataDTO.getSpouseBusinessAddress(),
                    personalDataDTO.getSpouseTelNo(),
                    personalDataDTO.getFatherSurname(),
                    personalDataDTO.getFatherFirstname(),
                    personalDataDTO.getFatherMiddlename(),
                    personalDataDTO.getMotherSurname(),
                    personalDataDTO.getMotherFirstname(),
                    personalDataDTO.getMotherMiddlename(),
                    personalDataDTO.getGovIdNumber(),
                    personalDataDTO.getGovIdType(),
                    personalDataDTO.getGovIdDate(),
                    personalDataDTO.getGovIdPlace(),
                    personalDataDTO.getSkillOrHobby(),
                    personalDataDTO.getDistinction(),
                    personalDataDTO.getAssociation(),
                    personalDataDTO.getQ34a(),
                    personalDataDTO.getQ34b(),
                    personalDataDTO.getQ35a(),
                    personalDataDTO.getQ35b(),
                    personalDataDTO.getQ36(),
                    personalDataDTO.getQ37a(),
                    personalDataDTO.getQ37b(),
                    personalDataDTO.getQ37c(),
                    personalDataDTO.getQ38(),
                    personalDataDTO.getQ39a(),
                    personalDataDTO.getQ39b(),
                    personalDataDTO.getQ39c(),
                    personalDataDTO.getQ34aDetails(),
                    personalDataDTO.getQ34bDetails(),
                    personalDataDTO.getQ35aDetails(),
                    personalDataDTO.getQ35bDetails(),
                    personalDataDTO.getQ35bDateFiled(),
                    personalDataDTO.getQ35bStatus(),
                    personalDataDTO.getQ36Details(),
                    personalDataDTO.getQ37aDetails(),
                    personalDataDTO.getQ37bDetails(),
                    personalDataDTO.getQ37cDetails(),
                    personalDataDTO.getQ38Details(),
                    personalDataDTO.getQ39aDetails(),
                    personalDataDTO.getQ39bDetails(),
                    personalDataDTO.getQ39cDetails(),
                    personalDataDTO.getQ42()
            );

            personalDataRepository.save(personalData);
            return personalDataDTO;
        } catch(Exception e) {
            log.info("Error creating personal data: {}", e.getMessage());
            return null;
        }
    }

    public PersonalData getPersonalDataEntityByEmployeeId(Long employeeId) {
        PersonalData personalData = personalDataRepository.findByEmployeeId(employeeId);
        if(personalData == null) {
            return null;
        }

        return personalData;
    }

    @Override
    public PersonalDataDTO getPersonalDataByEmployeeId(Long employeeId) {
        PersonalData personalData = getPersonalDataEntityByEmployeeId(employeeId);
        if(personalData != null) {
            PersonalDataDTO personalDataDTO = new PersonalDataDTO(
                    personalData.getPersonalDataId(),
                    personalData.getEmployeeId(),
                    personalData.getSurname(),
                    personalData.getFirstname(),
                    personalData.getMiddlename(),
                    personalData.getExtname(),
                    personalData.getDob(),
                    personalData.getPob(),
                    personalData.getSex_id(),
                    personalData.getCivilStatus_id(),
                    personalData.getHeight(),
                    personalData.getWeight(),
                    personalData.getBloodType(),
                    personalData.getGsisId(),
                    personalData.getPagibigId(),
                    personalData.getPhilhealthNo(),
                    personalData.getSssNo(),
                    personalData.getTinNo(),
                    personalData.getAgencyEmpNo(),
                    personalData.getCitizenship(),
                    personalData.getResAddress(),
                    personalData.getResZip(),
                    personalData.getPermAddress(),
                    personalData.getPermZip(),
                    personalData.getTelNo(),
                    personalData.getMobileNo(),
                    personalData.getEmail(),
                    personalData.getEmployeePicture(),
                    personalData.getEmployeeSignature(),
                    personalData.getSpouseSurname(),
                    personalData.getSpouseFirstname(),
                    personalData.getSpouseMiddlename(),
                    personalData.getSpouseOccupation(),
                    personalData.getSpouseEmployer(),
                    personalData.getSpouseBusinessAddress(),
                    personalData.getSpouseTelNo(),
                    personalData.getFatherSurname(),
                    personalData.getFatherFirstname(),
                    personalData.getFatherMiddlename(),
                    personalData.getMotherSurname(),
                    personalData.getMotherFirstname(),
                    personalData.getMotherMiddlename(),
                    personalData.getGovIdNumber(),
                    personalData.getGovIdType(),
                    personalData.getGovIdDate(),
                    personalData.getGovIdPlace(),
                    personalData.getSkillOrHobby(),
                    personalData.getDistinction(),
                    personalData.getAssociation(),
                    personalData.getQ34a(),
                    personalData.getQ34b(),
                    personalData.getQ35a(),
                    personalData.getQ35b(),
                    personalData.getQ36(),
                    personalData.getQ37a(),
                    personalData.getQ37b(),
                    personalData.getQ37c(),
                    personalData.getQ38(),
                    personalData.getQ39a(),
                    personalData.getQ39b(),
                    personalData.getQ39c(),
                    personalData.getQ34aDetails(),
                    personalData.getQ34bDetails(),
                    personalData.getQ35aDetails(),
                    personalData.getQ35bDetails(),
                    personalData.getQ35bDateFiled(),
                    personalData.getQ35bStatus(),
                    personalData.getQ36Details(),
                    personalData.getQ37aDetails(),
                    personalData.getQ37bDetails(),
                    personalData.getQ37cDetails(),
                    personalData.getQ38Details(),
                    personalData.getQ39aDetails(),
                    personalData.getQ39bDetails(),
                    personalData.getQ39cDetails(),
                    personalData.getQ42()
            );

            return personalDataDTO;
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean updatePersonalData(Long employeeId, Map<String, Object> updates) throws Exception {
        try {
            PersonalData personalDataExisting = getPersonalDataEntityByEmployeeId(employeeId);
            if(personalDataExisting != null) {
                objectMapper.updateValue(personalDataExisting, updates);
                personalDataRepository.save(personalDataExisting);
                return true;
            }

            return false;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public String deletePersonalData(String employeeId) {
        return "";
    }
}