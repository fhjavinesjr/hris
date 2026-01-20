package com.administrative.impl;

import com.administrative.dtos.CivilStatusDTO;
import com.administrative.entitymodels.CivilStatus;
import com.administrative.repositories.CivilStatusRepository;
import com.administrative.services.CivilStatusService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CivilStatusImpl implements CivilStatusService {

    private static final Logger log = LoggerFactory.getLogger(CivilStatusImpl.class);
    private final CivilStatusRepository civilStatusRepository;

    public CivilStatusImpl(CivilStatusRepository civilStatusRepository) {
        this.civilStatusRepository = civilStatusRepository;
    }

    @Transactional
    @Override
    public CivilStatusDTO createCivilStatus(CivilStatusDTO civilStatusDTO) throws Exception {
        try {
            CivilStatus civilStatus = new CivilStatus(civilStatusDTO.getCivilStatusId(), civilStatusDTO.getCode(), civilStatusDTO.getName());
            civilStatusRepository.save(civilStatus);

            return civilStatusDTO;
        } catch(Exception e) {
            log.error("Error in creating CivilStatus: ", e);
            return null;
        }
    }

    @Override
    public List<CivilStatusDTO> getAllCivilStatus() throws Exception {
        List<CivilStatus> civilStatusList = civilStatusRepository.findAll();
        List<CivilStatusDTO> civilStatusDTOList = new ArrayList<>();

        for(CivilStatus civilStatus : civilStatusList) {
            CivilStatusDTO civilStatusDTO = new CivilStatusDTO();
            civilStatusDTO.setCivilStatusId(civilStatus.getCivilStatusId());
            civilStatusDTO.setCode(civilStatus.getCode());
            civilStatusDTO.setName(civilStatus.getName());

            civilStatusDTOList.add(civilStatusDTO);
        }

        return civilStatusDTOList;
    }

    @Override
    public CivilStatusDTO getCivilStatusById(Long civilStatusId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public CivilStatusDTO updateCivilStatus(Long civilStatusId, CivilStatusDTO civilStatusDTO) throws Exception {
        try {
            CivilStatus civilStatus = civilStatusRepository.findById(civilStatusId).orElseThrow(() -> new RuntimeException("CivilStatus not found"));
            if(civilStatus != null) {
                civilStatus.setCode(civilStatusDTO.getCode());
                civilStatus.setName(civilStatusDTO.getName());

                civilStatusRepository.save(civilStatus);

                return civilStatusDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching CivilStatus: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteCivilStatus(Long civilStatusId) throws Exception {
        try {
            civilStatusRepository.deleteById(civilStatusId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete CivilStatus: {}", e.getMessage());
        }

        return false;
    }
}