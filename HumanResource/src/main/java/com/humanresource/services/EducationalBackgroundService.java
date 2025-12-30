package com.humanresource.services;

import com.humanresource.dtos.EducationalBackgroundDTO;

import java.util.List;
import java.util.Map;

public interface EducationalBackgroundService {

    EducationalBackgroundDTO createEducationalBackground(EducationalBackgroundDTO educationalBackgroundDTO);

    EducationalBackgroundDTO getEducationalBackgroundByEducationalBackgroundId(Long educationalBackgroundId) throws Exception;

    List<EducationalBackgroundDTO> getEducationalBackgroundByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateEducationalBackground(Long educationalBackgroundId, Map<String, Object> updates) throws Exception;

    Boolean deleteEducationalBackground(Long educationalBackgroundId) throws Exception;

}
