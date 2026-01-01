package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.CivilServiceEligibilityDTO;
import com.humanresource.entitymodels.CivilServiceEligibility;
import com.humanresource.repositories.CivilServiceEligibilityRepository;
import com.humanresource.services.CivilServiceEligibilityService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CivilServiceEligibilityImpl implements CivilServiceEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(CivilServiceEligibilityImpl.class);
    private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
    private final ObjectMapper objectMapper;

    public CivilServiceEligibilityImpl(CivilServiceEligibilityRepository civilServiceEligibilityRepository, ObjectMapper objectMapper) {
        this.civilServiceEligibilityRepository = civilServiceEligibilityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public CivilServiceEligibilityDTO createCivilServiceEligibility(CivilServiceEligibilityDTO civilServiceEligibilityDTO) {
        try {
            CivilServiceEligibility civilServiceEligibility = new CivilServiceEligibility(civilServiceEligibilityDTO.getCivilServiceEligibilityId(), civilServiceEligibilityDTO.getPersonalDataId()
                , civilServiceEligibilityDTO.getCareerServiceName(), civilServiceEligibilityDTO.getCivilServiceRating()
                ,civilServiceEligibilityDTO.getDateOfExamination(), civilServiceEligibilityDTO.getPlaceOfExamination()
                ,civilServiceEligibilityDTO.getLicenseNumber(), civilServiceEligibilityDTO.getLicenseValidityDate());

            civilServiceEligibilityRepository.save(civilServiceEligibility);
            return civilServiceEligibilityDTO;
        } catch(Exception e) {
            log.info("Error creating civilServiceEligibility data: {}", e.getMessage());
            return null;
        }
    }

    public CivilServiceEligibility getCivilServiceEligibilityEntityByCivilServiceEligibilityId(Long civilServiceEligibilityId) {
        Optional<CivilServiceEligibility> civilServiceEligibilityOpt = civilServiceEligibilityRepository.findById(civilServiceEligibilityId);
        CivilServiceEligibility civilServiceEligibility;
        if(civilServiceEligibilityOpt.isPresent()) {
            civilServiceEligibility = civilServiceEligibilityOpt.get();
            return civilServiceEligibility;
        }

        return null;
    }

    public List<CivilServiceEligibility> getCivilServiceEligibilityEntityByPersonalDataId(Long personalDataId) {
        List<CivilServiceEligibility> civilServiceEligibilityList = civilServiceEligibilityRepository.findByPersonalDataId(personalDataId);
        if(civilServiceEligibilityList.isEmpty()) {
            return null;
        }

        return civilServiceEligibilityList;
    }

    @Override
    public CivilServiceEligibilityDTO getCivilServiceEligibilityByCivilServiceEligibilityId(Long civilServiceEligibilityId) {
        CivilServiceEligibility civilServiceEligibility = getCivilServiceEligibilityEntityByCivilServiceEligibilityId(civilServiceEligibilityId);
        if(civilServiceEligibility == null) {
            return null;
        }

        return new CivilServiceEligibilityDTO(civilServiceEligibility.getCivilServiceEligibilityId(), civilServiceEligibility.getPersonalDataId()
                , civilServiceEligibility.getCareerServiceName(), civilServiceEligibility.getCivilServiceRating()
                ,civilServiceEligibility.getDateOfExamination(), civilServiceEligibility.getPlaceOfExamination()
                ,civilServiceEligibility.getLicenseNumber(), civilServiceEligibility.getLicenseValidityDate());
    }

    @Override
    public List<CivilServiceEligibilityDTO> getCivilServiceEligibilityByPersonalDataId(Long personalDataId) throws Exception {
        List<CivilServiceEligibility> civilServiceEligibilityList = getCivilServiceEligibilityEntityByPersonalDataId(personalDataId);
        if(civilServiceEligibilityList.isEmpty()) {
            return null;
        }
        List<CivilServiceEligibilityDTO> civilServiceEligibilityDTOS = new ArrayList<>();
        for(CivilServiceEligibility civilServiceEligibility : civilServiceEligibilityList) {
            CivilServiceEligibilityDTO civilServiceEligibilityDTO = new CivilServiceEligibilityDTO(civilServiceEligibility.getCivilServiceEligibilityId(), civilServiceEligibility.getPersonalDataId()
                    ,civilServiceEligibility.getCareerServiceName(), civilServiceEligibility.getCivilServiceRating()
                    ,civilServiceEligibility.getDateOfExamination(), civilServiceEligibility.getPlaceOfExamination()
                    ,civilServiceEligibility.getLicenseNumber(), civilServiceEligibility.getLicenseValidityDate());
            civilServiceEligibilityDTOS.add(civilServiceEligibilityDTO);
        }

        return civilServiceEligibilityDTOS;
    }

    @Transactional
    @Override
    public Boolean updateCivilServiceEligibility(Long civilServiceEligibilityId, Map<String, Object> updates) throws Exception {
        try {
            CivilServiceEligibility civilServiceEligibilityExisting = getCivilServiceEligibilityEntityByCivilServiceEligibilityId(civilServiceEligibilityId);
            if(civilServiceEligibilityExisting != null) {
                objectMapper.updateValue(civilServiceEligibilityExisting, updates);
                civilServiceEligibilityRepository.save(civilServiceEligibilityExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteCivilServiceEligibility(Long civilServiceEligibilityId) throws Exception {
        try {
            civilServiceEligibilityRepository.deleteById(civilServiceEligibilityId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}