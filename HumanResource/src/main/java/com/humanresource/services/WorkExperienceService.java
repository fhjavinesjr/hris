package com.humanresource.services;


import com.humanresource.dtos.WorkExperienceDTO;

import java.util.List;
import java.util.Map;

public interface WorkExperienceService {

    WorkExperienceDTO createWorkExperience(WorkExperienceDTO workExperienceDTO);

    WorkExperienceDTO getWorkExperienceByWorkExperienceId(Long workExperienceId) throws Exception;

    List<WorkExperienceDTO> getWorkExperienceByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateWorkExperience(Long workExperienceId, Map<String, Object> updates) throws Exception;

    Boolean deleteWorkExperience(Long workExperienceId) throws Exception;

}