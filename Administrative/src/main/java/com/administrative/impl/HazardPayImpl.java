package com.administrative.impl;

import com.administrative.dtos.HazardPayDTO;
import com.administrative.entitymodels.HazardPay;
import com.administrative.repositories.HazardPayRepository;
import com.administrative.services.HazardPayService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HazardPayImpl implements HazardPayService {

    private static final Logger log = LoggerFactory.getLogger(HazardPayImpl.class);
    private final HazardPayRepository hazardPayRepository;

    public HazardPayImpl(HazardPayRepository hazardPayRepository) {
        this.hazardPayRepository = hazardPayRepository;
    }

    @Transactional
    @Override
    public List<HazardPayDTO> createHazardPay(List<HazardPayDTO> hazardPayDTOList) throws Exception {
        try {
            for(HazardPayDTO hazardPayDTO : hazardPayDTOList) {
                HazardPay hazardPay = new HazardPay(hazardPayDTO.getHazardPayId(), hazardPayDTO.getEffectivityDate(), hazardPayDTO.getSalaryGrade(), hazardPayDTO.getBasicPayPercentage());
                hazardPayRepository.save(hazardPay);
            }

            return hazardPayDTOList;
        } catch(Exception e) {
            log.error("Error in creating HazardPay: ", e);
            return null;
        }
    }

    @Override
    public List<HazardPayDTO> getAllHazardPay() throws Exception {
        List<HazardPay> hazardPayList = hazardPayRepository.findAll();
        List<HazardPayDTO> hazardPayDTOList = new ArrayList<>();

        for(HazardPay hazardPay : hazardPayList) {
            HazardPayDTO hazardPayDTO = new HazardPayDTO();
            hazardPayDTO.setHazardPayId(hazardPay.getHazardPayId());
            hazardPayDTO.setEffectivityDate(hazardPay.getEffectivityDate());
            hazardPayDTO.setSalaryGrade(hazardPay.getSalaryGrade());
            hazardPayDTO.setBasicPayPercentage(hazardPay.getBasicPayPercentage());

            hazardPayDTOList.add(hazardPayDTO);
        }

        return hazardPayDTOList;
    }

    @Override
    public HazardPayDTO getHazardPayById(Long hazardPayId) throws Exception {
        return null;
    }

    @Override
    public List<HazardPayDTO> getHazardPayByEffectivityDate(LocalDateTime effectivityDate) throws Exception {
        try {
            List<HazardPay> hazardPayList = hazardPayRepository.findByEffectivityDate(effectivityDate);
            List<HazardPayDTO> hazardPayDTOList = new ArrayList<>();

            for(HazardPay hazardPay : hazardPayList) {
                HazardPayDTO hazardPayDTO = new HazardPayDTO();
                hazardPayDTO.setHazardPayId(hazardPay.getHazardPayId());
                hazardPayDTO.setEffectivityDate(hazardPay.getEffectivityDate());
                hazardPayDTO.setSalaryGrade(hazardPay.getSalaryGrade());
                hazardPayDTO.setBasicPayPercentage(hazardPay.getBasicPayPercentage());

                hazardPayDTOList.add(hazardPayDTO);
            }

            return hazardPayDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching HazardPay: {}", e.getMessage());
            return null;
        }
    }

    @Transactional
    @Override
    public List<HazardPayDTO> updateHazardPay(List<HazardPayDTO> hazardPayDTOList) throws Exception {
        try {
            for(HazardPayDTO hazardPayDTO : hazardPayDTOList) {
                if(hazardPayDTO.getHazardPayId() == null || hazardPayDTO.getHazardPayId() == 0) {
                    //added new entry values
                    HazardPay hazardPay = new HazardPay();
                    hazardPay.setEffectivityDate(hazardPayDTO.getEffectivityDate());
                    hazardPay.setSalaryGrade(hazardPayDTO.getSalaryGrade());
                    hazardPay.setBasicPayPercentage(hazardPayDTO.getBasicPayPercentage());

                    hazardPayRepository.save(hazardPay);
                    continue;
                }

                //modify existing entry values
                HazardPay hazardPay = hazardPayRepository.findById(hazardPayDTO.getHazardPayId()).orElseThrow(() -> new RuntimeException("HazardPay not found"));
                if(hazardPay != null) {
                    hazardPay.setEffectivityDate(hazardPayDTO.getEffectivityDate());
                    hazardPay.setSalaryGrade(hazardPayDTO.getSalaryGrade());
                    hazardPay.setBasicPayPercentage(hazardPayDTO.getBasicPayPercentage());

                    hazardPayRepository.save(hazardPay);
                }
            }

            return hazardPayDTOList;
        } catch(Exception e) {
            log.error("Error failed fetching HazardPay: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteHazardPay(LocalDateTime effectivityDate) throws Exception {
        try {
            hazardPayRepository.deleteByEffectivityDate(effectivityDate);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete HazardPay: {}", e.getMessage());
        }

        return false;
    }

    @Transactional
    @Override
    public Boolean deleteHazardPayById(List<HazardPayDTO> hazardPayDTOList) throws Exception {
        try {
            for(HazardPayDTO hazardPayDTO : hazardPayDTOList) {
                hazardPayRepository.deleteById(hazardPayDTO.getHazardPayId());
            }

            return true;
        } catch(Exception e) {
            log.error("Error failed deleting HazardPay: {}", e.getMessage());
        }

        return null;
    }
}