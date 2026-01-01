package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.LearningAndDevelopmentDTO;
import com.humanresource.entitymodels.LearningAndDevelopment;
import com.humanresource.repositories.LearningAndDevelopmentRepository;
import com.humanresource.services.LearningAndDevelopmentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LearningAndDevelopmentImpl implements LearningAndDevelopmentService {

    private static final Logger log = LoggerFactory.getLogger(LearningAndDevelopmentImpl.class);
    private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
    private final ObjectMapper objectMapper;

    public LearningAndDevelopmentImpl(LearningAndDevelopmentRepository learningAndDevelopmentRepository, ObjectMapper objectMapper) {
        this.learningAndDevelopmentRepository = learningAndDevelopmentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public LearningAndDevelopmentDTO createLearningAndDevelopment(LearningAndDevelopmentDTO learningAndDevelopmentDTO) {
        try {
            LearningAndDevelopment learningAndDevelopment = new LearningAndDevelopment(learningAndDevelopmentDTO.getLearningAndDevelopmentId(), learningAndDevelopmentDTO.getPersonalDataId()
                ,learningAndDevelopmentDTO.getProgramName(), learningAndDevelopmentDTO.getFromDate(), learningAndDevelopmentDTO.getToDate()
                ,learningAndDevelopmentDTO.getLndHrs(), learningAndDevelopmentDTO.getLndType(), learningAndDevelopmentDTO.getConductedBy());

            learningAndDevelopmentRepository.save(learningAndDevelopment);
            return learningAndDevelopmentDTO;
        } catch(Exception e) {
            log.info("Error creating learningAndDevelopment data: {}", e.getMessage());
            return null;
        }
    }

    public LearningAndDevelopment getLearningAndDevelopmentEntityByLearningAndDevelopmentId(Long learningAndDevelopmentId) {
        Optional<LearningAndDevelopment> learningAndDevelopmentOpt = learningAndDevelopmentRepository.findById(learningAndDevelopmentId);
        LearningAndDevelopment learningAndDevelopment;
        if(learningAndDevelopmentOpt.isPresent()) {
            learningAndDevelopment = learningAndDevelopmentOpt.get();
            return learningAndDevelopment;
        }

        return null;
    }

    public List<LearningAndDevelopment> getLearningAndDevelopmentEntityByPersonalDataId(Long personalDataId) {
        List<LearningAndDevelopment> learningAndDevelopmentList = learningAndDevelopmentRepository.findByPersonalDataId(personalDataId);
        if(learningAndDevelopmentList.isEmpty()) {
            return null;
        }

        return learningAndDevelopmentList;
    }

    @Override
    public LearningAndDevelopmentDTO getLearningAndDevelopmentByLearningAndDevelopmentId(Long learningAndDevelopmentId) {
        LearningAndDevelopment learningAndDevelopment = getLearningAndDevelopmentEntityByLearningAndDevelopmentId(learningAndDevelopmentId);
        if(learningAndDevelopment == null) {
            return null;
        }

        return new LearningAndDevelopmentDTO(learningAndDevelopment.getLearningAndDevelopmentId(), learningAndDevelopment.getPersonalDataId()
                ,learningAndDevelopment.getProgramName(), learningAndDevelopment.getFromDate(), learningAndDevelopment.getToDate()
                ,learningAndDevelopment.getLndHrs(), learningAndDevelopment.getLndType(), learningAndDevelopment.getConductedBy());
    }

    @Override
    public List<LearningAndDevelopmentDTO> getLearningAndDevelopmentByPersonalDataId(Long personalDataId) throws Exception {
        List<LearningAndDevelopment> learningAndDevelopmentList = getLearningAndDevelopmentEntityByPersonalDataId(personalDataId);
        if(learningAndDevelopmentList.isEmpty()) {
            return null;
        }
        List<LearningAndDevelopmentDTO> learningAndDevelopmentDTOS = new ArrayList<>();
        for(LearningAndDevelopment learningAndDevelopment : learningAndDevelopmentList) {
            LearningAndDevelopmentDTO learningAndDevelopmentDTO = new LearningAndDevelopmentDTO(learningAndDevelopment.getLearningAndDevelopmentId(), learningAndDevelopment.getPersonalDataId()
                    ,learningAndDevelopment.getProgramName(), learningAndDevelopment.getFromDate(), learningAndDevelopment.getToDate()
                    ,learningAndDevelopment.getLndHrs(), learningAndDevelopment.getLndType(), learningAndDevelopment.getConductedBy());
            learningAndDevelopmentDTOS.add(learningAndDevelopmentDTO);
        }

        return learningAndDevelopmentDTOS;
    }

    @Transactional
    @Override
    public Boolean updateLearningAndDevelopment(Long learningAndDevelopmentId, Map<String, Object> updates) throws Exception {
        try {
            LearningAndDevelopment learningAndDevelopmentExisting = getLearningAndDevelopmentEntityByLearningAndDevelopmentId(learningAndDevelopmentId);
            if(learningAndDevelopmentExisting != null) {
                objectMapper.updateValue(learningAndDevelopmentExisting, updates);
                learningAndDevelopmentRepository.save(learningAndDevelopmentExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteLearningAndDevelopment(Long learningAndDevelopmentId) throws Exception {
        try {
            learningAndDevelopmentRepository.deleteById(learningAndDevelopmentId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}