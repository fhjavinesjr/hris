package com.administrative.impl;

import com.administrative.dtos.PhilHealthContributionDTO;
import com.administrative.entitymodels.PhilHealthContribution;
import com.administrative.repositories.PhilHealthContributionRepository;
import com.administrative.services.PhilHealthContributionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhilHealthContributionImpl implements PhilHealthContributionService {

    private static final Logger log = LoggerFactory.getLogger(PhilHealthContributionImpl.class);
    private final PhilHealthContributionRepository philHealthContributionRepository;

    public PhilHealthContributionImpl(PhilHealthContributionRepository philHealthContributionRepository) {
        this.philHealthContributionRepository = philHealthContributionRepository;
    }

    @Transactional
    @Override
    public PhilHealthContributionDTO createPhilHealthContribution(PhilHealthContributionDTO philHealthContributionDTO) throws Exception {
        try {
            PhilHealthContribution philHealthContribution = new PhilHealthContribution(philHealthContributionDTO.getPhilhealthContributionId(), philHealthContributionDTO.getEffectivityDate(), philHealthContributionDTO.getRatePercentage(),
                    philHealthContributionDTO.getMonthlySalaryRangeFrom(), philHealthContributionDTO.getMonthlySalaryRangeTo(), philHealthContributionDTO.getPersonalShareFrom(),
                    philHealthContributionDTO.getPersonalShareTo(), philHealthContributionDTO.getEmployerShareFrom(), philHealthContributionDTO.getEmployerShareTo());

            philHealthContributionRepository.save(philHealthContribution);

            return philHealthContributionDTO;
        } catch(Exception e) {
            log.error("Error in creating PhilHealthContribution: ", e);
            return null;
        }
    }

    @Override
    public List<PhilHealthContributionDTO> getAllPhilHealthContribution() throws Exception {
        List<PhilHealthContribution> philHealthContributionList = philHealthContributionRepository.findAll();
        List<PhilHealthContributionDTO> natureOfAppointmenDTOList = new ArrayList<>();

        for(PhilHealthContribution philHealthContribution : philHealthContributionList) {
            PhilHealthContributionDTO philHealthContributionDTO = new PhilHealthContributionDTO();
            philHealthContributionDTO.setPhilhealthContributionId(philHealthContribution.getPhilhealthContributionId());
            philHealthContributionDTO.setEffectivityDate(philHealthContribution.getEffectivityDate());
            philHealthContributionDTO.setRatePercentage(philHealthContribution.getRatePercentage());
            philHealthContributionDTO.setMonthlySalaryRangeFrom(philHealthContribution.getMonthlySalaryRangeFrom());
            philHealthContributionDTO.setMonthlySalaryRangeTo(philHealthContribution.getMonthlySalaryRangeTo());
            philHealthContributionDTO.setPersonalShareFrom(philHealthContribution.getPersonalShareFrom());
            philHealthContributionDTO.setPersonalShareTo(philHealthContribution.getPersonalShareTo());
            philHealthContributionDTO.setEmployerShareFrom(philHealthContribution.getEmployerShareFrom());
            philHealthContributionDTO.setEmployerShareTo(philHealthContribution.getEmployerShareTo());

            natureOfAppointmenDTOList.add(philHealthContributionDTO);
        }

        return natureOfAppointmenDTOList;
    }

    @Override
    public PhilHealthContributionDTO getPhilHealthContributionById(Long philHealthContributionId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public PhilHealthContributionDTO updatePhilHealthContribution(Long philHealthContributionId, PhilHealthContributionDTO philHealthContributionDTO) throws Exception {
        try {
            PhilHealthContribution philHealthContribution = philHealthContributionRepository.findById(philHealthContributionId).orElseThrow(() -> new RuntimeException("PhilHealthContribution not found"));
            if(philHealthContribution != null) {
                philHealthContribution.setEffectivityDate(philHealthContributionDTO.getEffectivityDate());
                philHealthContribution.setRatePercentage(philHealthContributionDTO.getRatePercentage());
                philHealthContribution.setMonthlySalaryRangeFrom(philHealthContributionDTO.getMonthlySalaryRangeFrom());
                philHealthContribution.setMonthlySalaryRangeTo(philHealthContributionDTO.getMonthlySalaryRangeTo());
                philHealthContribution.setPersonalShareFrom(philHealthContributionDTO.getPersonalShareFrom());
                philHealthContribution.setPersonalShareTo(philHealthContributionDTO.getPersonalShareTo());
                philHealthContribution.setEmployerShareFrom(philHealthContributionDTO.getEmployerShareFrom());
                philHealthContribution.setEmployerShareTo(philHealthContributionDTO.getEmployerShareTo());

                philHealthContributionRepository.save(philHealthContribution);

                return philHealthContributionDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching PhilHealthContribution: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deletePhilHealthContribution(Long philHealthContributionId) throws Exception {
        try {
            philHealthContributionRepository.deleteById(philHealthContributionId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete PhilHealthContribution: {}", e.getMessage());
        }

        return false;
    }
}