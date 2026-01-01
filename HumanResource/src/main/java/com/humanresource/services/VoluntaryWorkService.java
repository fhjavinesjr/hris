package com.humanresource.services;

import com.humanresource.dtos.VoluntaryWorkDTO;

import java.util.List;
import java.util.Map;

public interface VoluntaryWorkService {

    VoluntaryWorkDTO createVoluntaryWork(VoluntaryWorkDTO voluntaryWorkDTO);

    VoluntaryWorkDTO getVoluntaryWorkByVoluntaryWorkId(Long voluntaryWorkId) throws Exception;

    List<VoluntaryWorkDTO> getVoluntaryWorkByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateVoluntaryWork(Long voluntaryWorkId, Map<String, Object> updates) throws Exception;

    Boolean deleteVoluntaryWork(Long voluntaryWorkId) throws Exception;

}