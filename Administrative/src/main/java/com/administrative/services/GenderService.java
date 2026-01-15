package com.administrative.services;

import com.administrative.dtos.GenderDTO;

import java.util.List;

public interface GenderService {

    GenderDTO createGender(GenderDTO genderDTO) throws Exception;

    List<GenderDTO> getAllGender() throws Exception;

    GenderDTO getGenderById(Long genderId) throws Exception;

    GenderDTO updateGender(Long genderId, GenderDTO genderDTO) throws Exception;

    Boolean deleteGender(Long genderId) throws Exception;

}
