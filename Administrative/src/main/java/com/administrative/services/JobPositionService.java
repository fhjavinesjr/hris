package com.administrative.services;

import com.administrative.dtos.JobPositionDTO;

import java.util.List;

public interface JobPositionService {

    JobPositionDTO createJobPosition(JobPositionDTO jobPositionDTO) throws Exception;

    List<JobPositionDTO> getAllJobPosition() throws Exception;

    JobPositionDTO getJobPositionById(Long jobPositionId) throws Exception;

    JobPositionDTO updateJobPosition(Long jobPositionId, JobPositionDTO JobPositionDTO) throws Exception;

    Boolean deleteJobPosition(Long jobPositionId) throws Exception;

}
