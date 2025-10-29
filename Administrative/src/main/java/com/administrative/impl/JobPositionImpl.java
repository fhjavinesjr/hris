package com.administrative.impl;

import com.administrative.dtos.JobPositionDTO;
import com.administrative.entitymodels.JobPosition;
import com.administrative.repositories.JobPositionRepository;
import com.administrative.services.JobPositionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobPositionImpl implements JobPositionService {

    private static final Logger log = LoggerFactory.getLogger(JobPositionImpl.class);
    private final JobPositionRepository jobPositionRepository;

    public JobPositionImpl(JobPositionRepository jobPositionRepository) {
        this.jobPositionRepository = jobPositionRepository;
    }

    @Transactional
    @Override
    public JobPositionDTO createJobPosition(JobPositionDTO jobPositionDTO) throws Exception {
        try {
            JobPosition jobPosition = new JobPosition(jobPositionDTO.getJobPositionName(), jobPositionDTO.getSalaryGrade());
            jobPositionRepository.save(jobPosition);

            return jobPositionDTO;
        } catch(Exception e) {
            log.error("Error in creating Job Position: ", e);
            return null;
        }
    }

    @Override
    public List<JobPositionDTO> getAllJobPosition() throws Exception {
        return List.of();
    }

    @Override
    public JobPositionDTO getJobPositionById(Long JobPositionId) throws Exception {
        return null;
    }

    @Override
    public JobPositionDTO updateJobPosition(Long JobPositionId, JobPositionDTO JobPositionDTO) throws Exception {
        return null;
    }

    @Override
    public Boolean deleteJobPosition(Long JobPositionId) throws Exception {
        return null;
    }
}
