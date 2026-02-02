package com.administrative.impl;

import com.administrative.dtos.GSISContributionDTO;
import com.administrative.entitymodels.GSISContribution;
import com.administrative.repositories.GSISContributionRepository;
import com.administrative.services.GSISContributionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GSISContributionImpl implements GSISContributionService {

    private static final Logger log = LoggerFactory.getLogger(GSISContributionImpl.class);
    private final GSISContributionRepository gsisContributionRepository;

    public GSISContributionImpl(GSISContributionRepository gsisContributionRepository) {
        this.gsisContributionRepository = gsisContributionRepository;
    }

    @Transactional
    @Override
    public GSISContributionDTO createGSISContribution(GSISContributionDTO gsisContributionDTO) throws Exception {
        try {
            GSISContribution gsisContribution = new GSISContribution(gsisContributionDTO.getGsisContributionId(), gsisContributionDTO.getEffectivityDate(), gsisContributionDTO.getEmployerSharePercentage(), gsisContributionDTO.getEmployeeSharePercentage());
            gsisContributionRepository.save(gsisContribution);

            return gsisContributionDTO;
        } catch(Exception e) {
            log.error("Error in creating GSISContribution: ", e);
            return null;
        }
    }

    @Override
    public List<GSISContributionDTO> getAllGSISContribution() throws Exception {
        List<GSISContribution> gsisContributionList = gsisContributionRepository.findAll();
        List<GSISContributionDTO> natureOfAppointmenDTOList = new ArrayList<>();

        for(GSISContribution gsisContribution : gsisContributionList) {
            GSISContributionDTO gsisContributionDTO = new GSISContributionDTO();
            gsisContributionDTO.setGsisContributionId(gsisContribution.getGsisContributionId());
            gsisContributionDTO.setEffectivityDate(gsisContribution.getEffectivityDate());
            gsisContributionDTO.setEmployerSharePercentage(gsisContribution.getEmployerSharePercentage());
            gsisContributionDTO.setEmployeeSharePercentage(gsisContribution.getEmployeeSharePercentage());

            natureOfAppointmenDTOList.add(gsisContributionDTO);
        }

        return natureOfAppointmenDTOList;
    }

    @Override
    public GSISContributionDTO getGSISContributionById(Long gsisContributionId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public GSISContributionDTO updateGSISContribution(Long gsisContributionId, GSISContributionDTO gsisContributionDTO) throws Exception {
        try {
            GSISContribution gsisContribution = gsisContributionRepository.findById(gsisContributionId).orElseThrow(() -> new RuntimeException("GSISContribution not found"));
            if(gsisContribution != null) {
                gsisContribution.setEffectivityDate(gsisContributionDTO.getEffectivityDate());
                gsisContribution.setEmployerSharePercentage(gsisContributionDTO.getEmployerSharePercentage());
                gsisContribution.setEmployeeSharePercentage(gsisContributionDTO.getEmployeeSharePercentage());

                gsisContributionRepository.save(gsisContribution);

                return gsisContributionDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching GSISContribution: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteGSISContribution(Long gsisContributionId) throws Exception {
        try {
            gsisContributionRepository.deleteById(gsisContributionId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete GSISContribution: {}", e.getMessage());
        }

        return false;
    }
}