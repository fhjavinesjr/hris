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
    private final CivilStatusRepository genderRepository;

    public CivilStatusImpl(CivilStatusRepository genderRepository) {
        this.genderRepository = genderRepository;
    }

    @Transactional
    @Override
    public CivilStatusDTO createCivilStatus(CivilStatusDTO genderDTO) throws Exception {
        try {
            CivilStatus gender = new CivilStatus(genderDTO.getCivilStatusId(), genderDTO.getCode(), genderDTO.getName());
            genderRepository.save(gender);

            return genderDTO;
        } catch(Exception e) {
            log.error("Error in creating CivilStatus: ", e);
            return null;
        }
    }

    @Override
    public List<CivilStatusDTO> getAllCivilStatus() throws Exception {
        List<CivilStatus> genderList = genderRepository.findAll();
        List<CivilStatusDTO> genderDTOList = new ArrayList<>();

        for(CivilStatus gender : genderList) {
            CivilStatusDTO genderDTO = new CivilStatusDTO();
            genderDTO.setCivilStatusId(gender.getCivilStatusId());
            genderDTO.setCode(gender.getCode());
            genderDTO.setName(gender.getName());

            genderDTOList.add(genderDTO);
        }

        return genderDTOList;
    }

    @Override
    public CivilStatusDTO getCivilStatusById(Long genderId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public CivilStatusDTO updateCivilStatus(Long genderId, CivilStatusDTO genderDTO) throws Exception {
        try {
            CivilStatus gender = genderRepository.findById(genderId).orElseThrow(() -> new RuntimeException("CivilStatus not found"));
            if(gender != null) {
                gender.setCode(genderDTO.getCode());
                gender.setName(genderDTO.getName());

                genderRepository.save(gender);

                return genderDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching CivilStatus: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteCivilStatus(Long genderId) throws Exception {
        try {
            genderRepository.deleteById(genderId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete CivilStatus: {}", e.getMessage());
        }

        return false;
    }
}