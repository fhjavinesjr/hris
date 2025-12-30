package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.EducationalBackgroundDTO;
import com.humanresource.entitymodels.EducationalBackground;
import com.humanresource.repositories.EducationalBackgroundRepository;
import com.humanresource.services.EducationalBackgroundService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EducationalBackgroundImpl implements EducationalBackgroundService {

    private static final Logger log = LoggerFactory.getLogger(EducationalBackgroundImpl.class);
    private final EducationalBackgroundRepository educationalBackgroundRepository;
    private final ObjectMapper objectMapper;

    public EducationalBackgroundImpl(EducationalBackgroundRepository educationalBackgroundRepository, ObjectMapper objectMapper) {
        this.educationalBackgroundRepository = educationalBackgroundRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public EducationalBackgroundDTO createEducationalBackground(EducationalBackgroundDTO educationalBackgroundDTO) {
        try {
            EducationalBackground educationalBackground = new EducationalBackground(educationalBackgroundDTO.getEducationalBackgroundId(), educationalBackgroundDTO.getPersonalDataId()
                ,educationalBackgroundDTO.getLevelOfEducation(), educationalBackgroundDTO.getNameOfSchool(), educationalBackgroundDTO.getDegreeCourse()
                ,educationalBackgroundDTO.getScoreGrade(), educationalBackgroundDTO.getYearGraduated(), educationalBackgroundDTO.getFromDate()
                ,educationalBackgroundDTO.getToDate() ,educationalBackgroundDTO.getHonorsReceived());

            educationalBackgroundRepository.save(educationalBackground);
            return educationalBackgroundDTO;
        } catch(Exception e) {
            log.info("Error creating educationalBackground data: {}", e.getMessage());
            return null;
        }
    }

    public EducationalBackground getEducationalBackgroundEntityByEducationalBackgroundId(Long educationalBackgroundId) {
        Optional<EducationalBackground> educationalBackgroundOpt = educationalBackgroundRepository.findById(educationalBackgroundId);
        EducationalBackground educationalBackground;
        if(educationalBackgroundOpt.isPresent()) {
            educationalBackground = educationalBackgroundOpt.get();
            return educationalBackground;
        }

        return null;
    }

    public List<EducationalBackground> getEducationalBackgroundEntityByPersonalDataId(Long personalDataId) {
        List<EducationalBackground> educationalBackgroundList = educationalBackgroundRepository.findByPersonalDataId(personalDataId);
        if(educationalBackgroundList.isEmpty()) {
            return null;
        }

        return educationalBackgroundList;
    }

    @Override
    public EducationalBackgroundDTO getEducationalBackgroundByEducationalBackgroundId(Long educationalBackgroundId) {
        EducationalBackground educationalBackground = getEducationalBackgroundEntityByEducationalBackgroundId(educationalBackgroundId);
        if(educationalBackground == null) {
            return null;
        }

        return new EducationalBackgroundDTO(educationalBackground.getEducationalBackgroundId(), educationalBackground.getPersonalDataId()
                ,educationalBackground.getLevelOfEducation(), educationalBackground.getNameOfSchool(), educationalBackground.getDegreeCourse()
                ,educationalBackground.getScoreGrade(), educationalBackground.getYearGraduated(), educationalBackground.getFromDate()
                ,educationalBackground.getToDate() ,educationalBackground.getHonorsReceived());
    }

    @Override
    public List<EducationalBackgroundDTO> getEducationalBackgroundByPersonalDataId(Long personalDataId) throws Exception {
        List<EducationalBackground> educationalBackgroundList = getEducationalBackgroundEntityByPersonalDataId(personalDataId);
        if(educationalBackgroundList.isEmpty()) {
            return null;
        }
        List<EducationalBackgroundDTO> educationalBackgroundDTOS = new ArrayList<>();
        for(EducationalBackground educationalBackground : educationalBackgroundList) {
            EducationalBackgroundDTO educationalBackgroundDTO = new EducationalBackgroundDTO(educationalBackground.getEducationalBackgroundId(), educationalBackground.getPersonalDataId()
                    ,educationalBackground.getLevelOfEducation(), educationalBackground.getNameOfSchool(), educationalBackground.getDegreeCourse()
                    ,educationalBackground.getScoreGrade(), educationalBackground.getYearGraduated(), educationalBackground.getFromDate()
                    ,educationalBackground.getToDate() ,educationalBackground.getHonorsReceived());
            educationalBackgroundDTOS.add(educationalBackgroundDTO);
        }

        return educationalBackgroundDTOS;
    }

    @Transactional
    @Override
    public Boolean updateEducationalBackground(Long educationalBackgroundId, Map<String, Object> updates) throws Exception {
        try {
            EducationalBackground educationalBackgroundExisting = getEducationalBackgroundEntityByEducationalBackgroundId(educationalBackgroundId);
            if(educationalBackgroundExisting != null) {
                objectMapper.updateValue(educationalBackgroundExisting, updates);
                educationalBackgroundRepository.save(educationalBackgroundExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteEducationalBackground(Long educationalBackgroundId) throws Exception {
        try {
            educationalBackgroundRepository.deleteById(educationalBackgroundId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }


}