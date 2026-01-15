package com.administrative.impl;

import com.administrative.dtos.OfficialEngagementDTO;
import com.administrative.entitymodels.OfficialEngagement;
import com.administrative.repositories.OfficialEngagementRepository;
import com.administrative.services.OfficialEngagementService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OfficialEngagementImpl implements OfficialEngagementService {

    private static final Logger log = LoggerFactory.getLogger(OfficialEngagementImpl.class);
    private final OfficialEngagementRepository officialEngagementRepository;

    public OfficialEngagementImpl(OfficialEngagementRepository officialEngagementRepository) {
        this.officialEngagementRepository = officialEngagementRepository;
    }

    @Transactional
    @Override
    public OfficialEngagementDTO createOfficialEngagement(OfficialEngagementDTO officialEngagementDTO) throws Exception {
        try {
            OfficialEngagement officialEngagement = new OfficialEngagement(officialEngagementDTO.getOfficialEngagementId(), officialEngagementDTO.getCode(), officialEngagementDTO.getName());
            officialEngagementRepository.save(officialEngagement);

            return officialEngagementDTO;
        } catch(Exception e) {
            log.error("Error in creating OfficialEngagement: ", e);
            return null;
        }
    }

    @Override
    public List<OfficialEngagementDTO> getAllOfficialEngagement() throws Exception {
        List<OfficialEngagement> officialEngagementList = officialEngagementRepository.findAll();
        List<OfficialEngagementDTO> officialEngagementDTOList = new ArrayList<>();

        for(OfficialEngagement officialEngagement : officialEngagementList) {
            OfficialEngagementDTO officialEngagementDTO = new OfficialEngagementDTO();
            officialEngagementDTO.setOfficialEngagementId(officialEngagement.getOfficialEngagementId());
            officialEngagementDTO.setCode(officialEngagement.getCode());
            officialEngagementDTO.setName(officialEngagement.getName());

            officialEngagementDTOList.add(officialEngagementDTO);
        }

        return officialEngagementDTOList;
    }

    @Override
    public OfficialEngagementDTO getOfficialEngagementById(Long officialEngagementId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public OfficialEngagementDTO updateOfficialEngagement(Long officialEngagementId, OfficialEngagementDTO officialEngagementDTO) throws Exception {
        try {
            OfficialEngagement officialEngagement = officialEngagementRepository.findById(officialEngagementId).orElseThrow(() -> new RuntimeException("OfficialEngagement not found"));
            if(officialEngagement != null) {
                officialEngagement.setCode(officialEngagementDTO.getCode());
                officialEngagement.setName(officialEngagementDTO.getName());

                officialEngagementRepository.save(officialEngagement);

                return officialEngagementDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching OfficialEngagement: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteOfficialEngagement(Long officialEngagementId) throws Exception {
        try {
            officialEngagementRepository.deleteById(officialEngagementId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete OfficialEngagement: {}", e.getMessage());
        }

        return false;
    }
}