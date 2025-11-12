package com.administrative.impl;

import com.administrative.dtos.JobPositionDTO;
import com.administrative.entitymodels.JobPosition;
import com.administrative.repositories.JobPositionRepository;
import com.administrative.services.JobPositionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
            JobPosition jobPosition = new JobPosition(jobPositionDTO.getJobPositionName(), jobPositionDTO.getSalaryGrade(), jobPositionDTO.getSalaryStep());
            jobPositionRepository.save(jobPosition);

            return jobPositionDTO;
        } catch(Exception e) {
            log.error("Error in creating Job Position: ", e);
            return null;
        }
    }

    @Override
    public List<JobPositionDTO> getAllJobPosition() throws Exception {
        List<JobPosition> jobPositionList = jobPositionRepository.findAll();
        List<JobPositionDTO> jobPositionDTOList = new ArrayList<>();

        for(JobPosition jobPosition : jobPositionList) {
            JobPositionDTO jobPositionDTO = new JobPositionDTO();
            jobPositionDTO.setJobPositionId(jobPosition.getJobPositionId());
            jobPositionDTO.setJobPositionName(jobPosition.getJobPositionName());
            jobPositionDTO.setSalaryGrade(jobPosition.getSalaryGrade());
            jobPositionDTO.setSalaryStep(jobPosition.getSalaryStep());

            jobPositionDTOList.add(jobPositionDTO);
        }

        return jobPositionDTOList;
    }

    @Override
    public JobPositionDTO getJobPositionById(Long jobPositionId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public JobPositionDTO updateJobPosition(Long jobPositionId, JobPositionDTO jobPositionDTO) throws Exception {
        try {
            JobPosition jobPosition = jobPositionRepository.findById(jobPositionId).orElseThrow(() -> new RuntimeException("Job Position not found"));
            if(jobPosition != null) {
                jobPosition.setJobPositionName(jobPositionDTO.getJobPositionName());
                jobPosition.setSalaryGrade(jobPositionDTO.getSalaryGrade());
                jobPosition.setSalaryStep(jobPositionDTO.getSalaryStep());

                jobPositionRepository.save(jobPosition);

                return jobPositionDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching JobPosition: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteJobPosition(Long jobPositionId) throws Exception {
        try {
            jobPositionRepository.deleteById(jobPositionId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete JobPosition: {}", e.getMessage());
        }

        return false;
    }
}
