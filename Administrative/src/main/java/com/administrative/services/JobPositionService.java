package com.administrative.services;

import com.administrative.dtos.JobPositionDTO;

import java.util.List;

public interface JobPositionService {

    JobPositionDTO createJobPosition(JobPositionDTO jobPositionDTO) throws Exception;

    List<JobPositionDTO> getAllJobPosition() throws Exception;

    JobPositionDTO getJobPositionById(Long JobPositionId) throws Exception;

    JobPositionDTO updateJobPosition(Long JobPositionId, JobPositionDTO JobPositionDTO) throws Exception;

    Boolean deleteJobPosition(Long JobPositionId) throws Exception;

}
