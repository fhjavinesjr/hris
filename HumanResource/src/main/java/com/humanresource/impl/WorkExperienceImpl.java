package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.WorkExperienceDTO;
import com.humanresource.entitymodels.WorkExperience;
import com.humanresource.repositories.WorkExperienceRepository;
import com.humanresource.services.WorkExperienceService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkExperienceImpl implements WorkExperienceService {

    private static final Logger log = LoggerFactory.getLogger(WorkExperienceImpl.class);
    private final WorkExperienceRepository workExperienceRepository;
    private final ObjectMapper objectMapper;

    public WorkExperienceImpl(WorkExperienceRepository workExperienceRepository, ObjectMapper objectMapper) {
        this.workExperienceRepository = workExperienceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public WorkExperienceDTO createWorkExperience(WorkExperienceDTO workExperienceDTO) {
        try {
            WorkExperience workExperience = new WorkExperience(workExperienceDTO.getWorkExperienceId(), workExperienceDTO.getPersonalDataId()
                ,workExperienceDTO.getFromDate(), workExperienceDTO.getToDate(), workExperienceDTO.getPositionTitle()
                ,workExperienceDTO.getAgencyName(), workExperienceDTO.getMonthlySalary(), workExperienceDTO.getPayGrade()
                ,workExperienceDTO.getWorkStatus(), workExperienceDTO.getBoolGovernmentService());

            workExperienceRepository.save(workExperience);
            return workExperienceDTO;
        } catch(Exception e) {
            log.info("Error creating workExperience data: {}", e.getMessage());
            return null;
        }
    }

    public WorkExperience getWorkExperienceEntityByWorkExperienceId(Long workExperienceId) {
        Optional<WorkExperience> workExperienceOpt = workExperienceRepository.findById(workExperienceId);
        WorkExperience workExperience;
        if(workExperienceOpt.isPresent()) {
            workExperience = workExperienceOpt.get();
            return workExperience;
        }

        return null;
    }

    public List<WorkExperience> getWorkExperienceEntityByPersonalDataId(Long personalDataId) {
        List<WorkExperience> workExperienceList = workExperienceRepository.findByPersonalDataId(personalDataId);
        if(workExperienceList.isEmpty()) {
            return null;
        }

        return workExperienceList;
    }

    @Override
    public WorkExperienceDTO getWorkExperienceByWorkExperienceId(Long workExperienceId) {
        WorkExperience workExperience = getWorkExperienceEntityByWorkExperienceId(workExperienceId);
        if(workExperience == null) {
            return null;
        }

        return new WorkExperienceDTO(workExperience.getWorkExperienceId(), workExperience.getPersonalDataId()
                ,workExperience.getFromDate(), workExperience.getToDate(), workExperience.getPositionTitle()
                ,workExperience.getAgencyName(), workExperience.getMonthlySalary(), workExperience.getPayGrade()
                ,workExperience.getWorkStatus(), workExperience.getBoolGovernmentService());
    }

    @Override
    public List<WorkExperienceDTO> getWorkExperienceByPersonalDataId(Long personalDataId) throws Exception {
        List<WorkExperience> workExperienceList = getWorkExperienceEntityByPersonalDataId(personalDataId);
        if(workExperienceList.isEmpty()) {
            return null;
        }
        List<WorkExperienceDTO> workExperienceDTOS = new ArrayList<>();
        for(WorkExperience workExperience : workExperienceList) {
            WorkExperienceDTO workExperienceDTO = new WorkExperienceDTO(workExperience.getWorkExperienceId(), workExperience.getPersonalDataId()
                    ,workExperience.getFromDate(), workExperience.getToDate(), workExperience.getPositionTitle()
                    ,workExperience.getAgencyName(), workExperience.getMonthlySalary(), workExperience.getPayGrade()
                    ,workExperience.getWorkStatus(), workExperience.getBoolGovernmentService());
            workExperienceDTOS.add(workExperienceDTO);
        }

        return workExperienceDTOS;
    }

    @Transactional
    @Override
    public Boolean updateWorkExperience(Long workExperienceId, Map<String, Object> updates) throws Exception {
        try {
            WorkExperience workExperienceExisting = getWorkExperienceEntityByWorkExperienceId(workExperienceId);
            if(workExperienceExisting != null) {
                objectMapper.updateValue(workExperienceExisting, updates);
                workExperienceRepository.save(workExperienceExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteWorkExperience(Long workExperienceId) throws Exception {
        try {
            workExperienceRepository.deleteById(workExperienceId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}