package com.administrative.impl;

import com.administrative.dtos.WTAXContributionDTO;
import com.administrative.entitymodels.WTAXContribution;
import com.administrative.repositories.WTAXContributionRepository;
import com.administrative.services.WTAXContributionService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WTAXContributionImpl implements WTAXContributionService {

    private static final Logger log = LoggerFactory.getLogger(WTAXContributionImpl.class);
    private final WTAXContributionRepository wTAXContributionRepository;

    public WTAXContributionImpl(WTAXContributionRepository wTAXContributionRepository) {
        this.wTAXContributionRepository = wTAXContributionRepository;
    }

    @Transactional
    @Override
    public WTAXContributionDTO createWTAXContribution(WTAXContributionDTO wTAXContributionDTO) throws Exception {
        try {
            WTAXContribution wTAXContribution = new WTAXContribution(wTAXContributionDTO.getwTaxContributionId(), wTAXContributionDTO.getSalaryType(),
                    wTAXContributionDTO.getFixedAmount(), wTAXContributionDTO.getPercentageOverBase(), wTAXContributionDTO.getTaxAmount());

            wTAXContributionRepository.save(wTAXContribution);

            return wTAXContributionDTO;
        } catch(Exception e) {
            log.error("Error in creating WTAXContribution: ", e);
            return null;
        }
    }

    @Override
    public List<WTAXContributionDTO> getAllWTAXContribution() throws Exception {
        List<WTAXContribution> wTAXContributionList = wTAXContributionRepository.findAll();
        List<WTAXContributionDTO> natureOfAppointmenDTOList = new ArrayList<>();

        for(WTAXContribution wTAXContribution : wTAXContributionList) {
            WTAXContributionDTO wTAXContributionDTO = new WTAXContributionDTO();
            wTAXContributionDTO.setwTaxContributionId(wTAXContribution.getwTaxContributionId());
            wTAXContributionDTO.setSalaryType(wTAXContribution.getSalaryType());
            wTAXContributionDTO.setFixedAmount(wTAXContribution.getFixedAmount());
            wTAXContributionDTO.setPercentageOverBase(wTAXContribution.getPercentageOverBase());
            wTAXContributionDTO.setTaxAmount(wTAXContribution.getTaxAmount());

            natureOfAppointmenDTOList.add(wTAXContributionDTO);
        }

        return natureOfAppointmenDTOList;
    }

    @Override
    public WTAXContributionDTO getWTAXContributionById(Long wTAXContributionId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public WTAXContributionDTO updateWTAXContribution(Long wTAXContributionId, WTAXContributionDTO wTAXContributionDTO) throws Exception {
        try {
            WTAXContribution wTAXContribution = wTAXContributionRepository.findById(wTAXContributionId).orElseThrow(() -> new RuntimeException("WTAXContribution not found"));
            if(wTAXContribution != null) {
                wTAXContribution.setSalaryType(wTAXContributionDTO.getSalaryType());
                wTAXContribution.setFixedAmount(wTAXContributionDTO.getFixedAmount());
                wTAXContribution.setPercentageOverBase(wTAXContributionDTO.getPercentageOverBase());
                wTAXContribution.setTaxAmount(wTAXContributionDTO.getTaxAmount());

                wTAXContributionRepository.save(wTAXContribution);

                return wTAXContributionDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching WTAXContribution: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteWTAXContribution(Long wTAXContributionId) throws Exception {
        try {
            wTAXContributionRepository.deleteById(wTAXContributionId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete WTAXContribution: {}", e.getMessage());
        }

        return false;
    }
}