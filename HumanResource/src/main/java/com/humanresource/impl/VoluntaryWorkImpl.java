package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.VoluntaryWorkDTO;
import com.humanresource.entitymodels.VoluntaryWork;
import com.humanresource.repositories.VoluntaryWorkRepository;
import com.humanresource.services.VoluntaryWorkService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VoluntaryWorkImpl implements VoluntaryWorkService {

    private static final Logger log = LoggerFactory.getLogger(VoluntaryWorkImpl.class);
    private final VoluntaryWorkRepository voluntaryWorkRepository;
    private final ObjectMapper objectMapper;

    public VoluntaryWorkImpl(VoluntaryWorkRepository voluntaryWorkRepository, ObjectMapper objectMapper) {
        this.voluntaryWorkRepository = voluntaryWorkRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public VoluntaryWorkDTO createVoluntaryWork(VoluntaryWorkDTO voluntaryWorkDTO) {
        try {
            VoluntaryWork voluntaryWork = new VoluntaryWork(voluntaryWorkDTO.getVoluntaryWorkId(), voluntaryWorkDTO.getPersonalDataId()
                ,voluntaryWorkDTO.getOrganizationName(), voluntaryWorkDTO.getFromDate(), voluntaryWorkDTO.getToDate()
                ,voluntaryWorkDTO.getVoluntaryHrs(), voluntaryWorkDTO.getPositionTitle());

            voluntaryWorkRepository.save(voluntaryWork);
            return voluntaryWorkDTO;
        } catch(Exception e) {
            log.info("Error creating voluntaryWork data: {}", e.getMessage());
            return null;
        }
    }

    public VoluntaryWork getVoluntaryWorkEntityByVoluntaryWorkId(Long voluntaryWorkId) {
        Optional<VoluntaryWork> voluntaryWorkOpt = voluntaryWorkRepository.findById(voluntaryWorkId);
        VoluntaryWork voluntaryWork;
        if(voluntaryWorkOpt.isPresent()) {
            voluntaryWork = voluntaryWorkOpt.get();
            return voluntaryWork;
        }

        return null;
    }

    public List<VoluntaryWork> getVoluntaryWorkEntityByPersonalDataId(Long personalDataId) {
        List<VoluntaryWork> voluntaryWorkList = voluntaryWorkRepository.findByPersonalDataId(personalDataId);
        if(voluntaryWorkList.isEmpty()) {
            return null;
        }

        return voluntaryWorkList;
    }

    @Override
    public VoluntaryWorkDTO getVoluntaryWorkByVoluntaryWorkId(Long voluntaryWorkId) {
        VoluntaryWork voluntaryWork = getVoluntaryWorkEntityByVoluntaryWorkId(voluntaryWorkId);
        if(voluntaryWork == null) {
            return null;
        }

        return new VoluntaryWorkDTO(voluntaryWork.getVoluntaryWorkId(), voluntaryWork.getPersonalDataId()
                ,voluntaryWork.getOrganizationName(), voluntaryWork.getFromDate(), voluntaryWork.getToDate()
                ,voluntaryWork.getVoluntaryHrs(), voluntaryWork.getPositionTitle());
    }

    @Override
    public List<VoluntaryWorkDTO> getVoluntaryWorkByPersonalDataId(Long personalDataId) throws Exception {
        List<VoluntaryWork> voluntaryWorkList = getVoluntaryWorkEntityByPersonalDataId(personalDataId);
        if(voluntaryWorkList.isEmpty()) {
            return null;
        }
        List<VoluntaryWorkDTO> voluntaryWorkDTOS = new ArrayList<>();
        for(VoluntaryWork voluntaryWork : voluntaryWorkList) {
            VoluntaryWorkDTO voluntaryWorkDTO = new VoluntaryWorkDTO(voluntaryWork.getVoluntaryWorkId(), voluntaryWork.getPersonalDataId()
                    ,voluntaryWork.getOrganizationName(), voluntaryWork.getFromDate(), voluntaryWork.getToDate()
                    ,voluntaryWork.getVoluntaryHrs(), voluntaryWork.getPositionTitle());
            voluntaryWorkDTOS.add(voluntaryWorkDTO);
        }

        return voluntaryWorkDTOS;
    }

    @Transactional
    @Override
    public Boolean updateVoluntaryWork(Long voluntaryWorkId, Map<String, Object> updates) throws Exception {
        try {
            VoluntaryWork voluntaryWorkExisting = getVoluntaryWorkEntityByVoluntaryWorkId(voluntaryWorkId);
            if(voluntaryWorkExisting != null) {
                objectMapper.updateValue(voluntaryWorkExisting, updates);
                voluntaryWorkRepository.save(voluntaryWorkExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteVoluntaryWork(Long voluntaryWorkId) throws Exception {
        try {
            voluntaryWorkRepository.deleteById(voluntaryWorkId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}