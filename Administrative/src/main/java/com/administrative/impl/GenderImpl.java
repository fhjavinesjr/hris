package com.administrative.impl;

import com.administrative.dtos.GenderDTO;
import com.administrative.entitymodels.Gender;
import com.administrative.repositories.GenderRepository;
import com.administrative.services.GenderService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GenderImpl implements GenderService {

    private static final Logger log = LoggerFactory.getLogger(GenderImpl.class);
    private final GenderRepository genderRepository;

    public GenderImpl(GenderRepository genderRepository) {
        this.genderRepository = genderRepository;
    }

    @Transactional
    @Override
    public GenderDTO createGender(GenderDTO genderDTO) throws Exception {
        try {
            Gender gender = new Gender(genderDTO.getGenderId(), genderDTO.getCode(), genderDTO.getName());
            genderRepository.save(gender);

            return genderDTO;
        } catch(Exception e) {
            log.error("Error in creating Gender: ", e);
            return null;
        }
    }

    @Override
    public List<GenderDTO> getAllGender() throws Exception {
        List<Gender> genderList = genderRepository.findAll();
        List<GenderDTO> genderDTOList = new ArrayList<>();

        for(Gender gender : genderList) {
            GenderDTO genderDTO = new GenderDTO();
            genderDTO.setGenderId(gender.getGenderId());
            genderDTO.setCode(gender.getCode());
            genderDTO.setName(gender.getName());

            genderDTOList.add(genderDTO);
        }

        return genderDTOList;
    }

    @Override
    public GenderDTO getGenderById(Long genderId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public GenderDTO updateGender(Long genderId, GenderDTO genderDTO) throws Exception {
        try {
            Gender gender = genderRepository.findById(genderId).orElseThrow(() -> new RuntimeException("Gender not found"));
            if(gender != null) {
                gender.setCode(genderDTO.getCode());
                gender.setName(genderDTO.getName());

                genderRepository.save(gender);

                return genderDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching Gender: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteGender(Long genderId) throws Exception {
        try {
            genderRepository.deleteById(genderId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete Gender: {}", e.getMessage());
        }

        return false;
    }
}