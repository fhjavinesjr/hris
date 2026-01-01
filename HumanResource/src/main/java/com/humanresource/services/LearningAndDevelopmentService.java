package com.humanresource.services;

import com.humanresource.dtos.LearningAndDevelopmentDTO;

import java.util.List;
import java.util.Map;

public interface LearningAndDevelopmentService {

    LearningAndDevelopmentDTO createLearningAndDevelopment(LearningAndDevelopmentDTO learningAndDevelopmentDTO);

    LearningAndDevelopmentDTO getLearningAndDevelopmentByLearningAndDevelopmentId(Long learningAndDevelopmentId) throws Exception;

    List<LearningAndDevelopmentDTO> getLearningAndDevelopmentByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateLearningAndDevelopment(Long learningAndDevelopmentId, Map<String, Object> updates) throws Exception;

    Boolean deleteLearningAndDevelopment(Long learningAndDevelopmentId) throws Exception;

}