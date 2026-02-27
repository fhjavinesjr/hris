package com.administrative.impl;

import com.administrative.dtos.BusinessUnitsDTO;
import com.administrative.entitymodels.Areas;
import com.administrative.entitymodels.BusinessUnits;
import com.administrative.repositories.AreasRepository;
import com.administrative.repositories.BusinessUnitsRepository;
import com.administrative.services.BusinessUnitsService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusinessUnitsImpl implements BusinessUnitsService {

    private static final Logger log = LoggerFactory.getLogger(BusinessUnitsImpl.class);
    private final BusinessUnitsRepository businessUnitsRepository;
    private final AreasRepository areasRepository;

    public BusinessUnitsImpl(BusinessUnitsRepository businessUnitsRepository, AreasRepository areasRepository) {
        this.businessUnitsRepository = businessUnitsRepository;
        this.areasRepository = areasRepository;
    }

    @Transactional
    @Override
    public BusinessUnitsDTO createBusinessUnits(BusinessUnitsDTO dto) throws Exception {
        try {
            BusinessUnits businessUnits = new BusinessUnits();
            businessUnits.setBusinessUnitsCode(dto.getBusinessUnitsCode());
            businessUnits.setBusinessUnitsName(dto.getBusinessUnitsName());

            // Use the repository to find the proxy/reference of the Area
            if (dto.getAreasId() != null) {
                Areas area = areasRepository.findById(dto.getAreasId()).orElseThrow(() -> new RuntimeException("Area not found"));
                businessUnits.setAreas(area); // This links them via Hibernate
            }

            businessUnitsRepository.save(businessUnits);

            dto.setBusinessUnitsId(businessUnits.getBusinessUnitsId());
            return dto;
        } catch(Exception e) {
            log.error("Error: ", e);
            return null;
        }
    }

    @Override
    public List<BusinessUnitsDTO> getAllBusinessUnits() throws Exception {
        List<BusinessUnits> businessUnitsList = businessUnitsRepository.findAll();
        List<BusinessUnitsDTO> businessUnitsDTOList = new ArrayList<>();

        for(BusinessUnits businessUnits : businessUnitsList) {
            BusinessUnitsDTO businessUnitsDTO = new BusinessUnitsDTO();
            businessUnitsDTO.setBusinessUnitsId(businessUnits.getBusinessUnitsId());
            businessUnitsDTO.setBusinessUnitsCode(businessUnits.getBusinessUnitsCode());
            businessUnitsDTO.setBusinessUnitsName(businessUnits.getBusinessUnitsName());

            if (businessUnits.getAreas() != null) {
                businessUnitsDTO.setAreasId(businessUnits.getAreas().getAreasId());
            }

            businessUnitsDTOList.add(businessUnitsDTO);
        }

        return businessUnitsDTOList;
    }

    @Override
    public BusinessUnitsDTO getBusinessUnitsById(Long businessUnitsId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public BusinessUnitsDTO updateBusinessUnits(Long businessUnitsId, BusinessUnitsDTO businessUnitsDTO) throws Exception {
        try {
            BusinessUnits businessUnits = businessUnitsRepository.findById(businessUnitsId).orElseThrow(() -> new RuntimeException("BusinessUnits not found"));
            if(businessUnits != null) {
                businessUnits.setBusinessUnitsCode(businessUnitsDTO.getBusinessUnitsCode());
                businessUnits.setBusinessUnitsName(businessUnitsDTO.getBusinessUnitsName());
                // Update the relationship by fetching the Area entity
                if (businessUnitsDTO.getAreasId() != null) {
                    Areas area = areasRepository.findById(businessUnitsDTO.getAreasId()).orElseThrow(() -> new RuntimeException("Area not found"));
                    businessUnits.setAreas(area);
                }

                businessUnitsRepository.save(businessUnits);

                return businessUnitsDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching BusinessUnits: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteBusinessUnits(Long businessUnitsId) throws Exception {
        try {
            businessUnitsRepository.deleteById(businessUnitsId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete BusinessUnits: {}", e.getMessage());
        }

        return false;
    }
}