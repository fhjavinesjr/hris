package com.humanresource.impl;

import com.humanresource.dtos.SeparationDTO;
import com.humanresource.entitymodels.Separation;
import com.humanresource.repositories.SeparationRepository;
import com.humanresource.services.SeparationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SeparationImpl implements SeparationService {

    private static final Logger log = LoggerFactory.getLogger(SeparationImpl.class);
    private final SeparationRepository separationRepository;

    public SeparationImpl(SeparationRepository separationRepository) {
        this.separationRepository = separationRepository;
    }

    @Transactional
    @Override
    public SeparationDTO createSeparation(SeparationDTO separationDTO) throws Exception {
        try {
            Separation separation = new Separation(separationDTO.getSeparationId(),separationDTO.getEmployeeId()
                ,separationDTO.getSeparationDate(),separationDTO.getNatureOfSeparationId()
                ,separationDTO.getRemarks(),separationDTO.getEmployeeInterviewerId()
                ,separationDTO.getExitInterviewDate(),separationDTO.getEmployeeIdProcessingBy()
                ,separationDTO.getApprovedById());

            separationRepository.save(separation);

            return separationDTO;
        } catch(Exception e) {
            log.error("Error in creating Separation: ", e);
            return null;
        }
    }

    @Override
    public List<SeparationDTO> getAllSeparation() throws Exception {
        List<Separation> separationList = separationRepository.findAll();
        List<SeparationDTO> separationDTOList = new ArrayList<>();

        for(Separation separation : separationList) {
            SeparationDTO separationDTO = new SeparationDTO();
            separationDTO.setSeparationId(separation.getSeparationId());
            separationDTO.setEmployeeId(separation.getEmployeeId());
            separationDTO.setNatureOfSeparationId(separation.getNatureOfSeparationId());
            separationDTO.setSeparationDate(separation.getSeparationDate());
            separationDTO.setRemarks(separation.getRemarks());
            separationDTO.setEmployeeInterviewerId(separation.getEmployeeInterviewerId());
            separationDTO.setExitInterviewDate(separation.getExitInterviewDate());
            separationDTO.setEmployeeIdProcessingBy(separation.getEmployeeIdProcessingBy());
            separationDTO.setApprovedById(separation.getApprovedById());

            separationDTOList.add(separationDTO);
        }

        return separationDTOList;
    }

    @Override
    public List<SeparationDTO> getAllSeparationByEmployeeId(Long employeeId) throws Exception {
        List<Separation> separationList = separationRepository.findByEmployeeId(employeeId);
        List<SeparationDTO> separationDTOList = new ArrayList<>();

        for(Separation separation : separationList) {
            SeparationDTO separationDTO = new SeparationDTO();
            separationDTO.setSeparationId(separation.getSeparationId());
            separationDTO.setEmployeeId(separation.getEmployeeId());
            separationDTO.setNatureOfSeparationId(separation.getNatureOfSeparationId());
            separationDTO.setSeparationDate(separation.getSeparationDate());
            separationDTO.setRemarks(separation.getRemarks());
            separationDTO.setEmployeeInterviewerId(separation.getEmployeeInterviewerId());
            separationDTO.setExitInterviewDate(separation.getExitInterviewDate());
            separationDTO.setEmployeeIdProcessingBy(separation.getEmployeeIdProcessingBy());
            separationDTO.setApprovedById(separation.getApprovedById());

            separationDTOList.add(separationDTO);
        }

        return separationDTOList;
    }

    @Override
    public SeparationDTO getLatestSeparationByEmployeeId(Long employeeId) throws Exception {
        Optional<Separation> separation = separationRepository.findById(employeeId);
        SeparationDTO separationDTO = new SeparationDTO();
        if(separation.isPresent()) {
            Separation separationData = separation.get();
            separationDTO.setSeparationId(separationData.getSeparationId());
            separationDTO.setEmployeeId(separationData.getEmployeeId());
            separationDTO.setNatureOfSeparationId(separationData.getNatureOfSeparationId());
            separationDTO.setSeparationDate(separationData.getSeparationDate());
            separationDTO.setRemarks(separationData.getRemarks());
            separationDTO.setEmployeeInterviewerId(separationData.getEmployeeInterviewerId());
            separationDTO.setExitInterviewDate(separationData.getExitInterviewDate());
            separationDTO.setEmployeeIdProcessingBy(separationData.getEmployeeIdProcessingBy());
            separationDTO.setApprovedById(separationData.getApprovedById());

            return separationDTO;
        }

        return null;
    }

    @Override
    public SeparationDTO getSeparationById(Long separationId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public SeparationDTO updateSeparation(Long separationId, SeparationDTO separationDTO) throws Exception {
        try {
            Separation separation = separationRepository.findById(separationId).orElseThrow(() -> new RuntimeException("Separation not found"));
            if(separation != null) {
                separation.setSeparationId(separationId);
                separation.setEmployeeId(separationDTO.getEmployeeId());
                separation.setSeparationDate(separationDTO.getSeparationDate());
                separation.setNatureOfSeparationId(separationDTO.getNatureOfSeparationId());
                separation.setRemarks(separationDTO.getRemarks());
                separation.setEmployeeInterviewerId(separationDTO.getEmployeeInterviewerId());
                separation.setExitInterviewDate(separationDTO.getExitInterviewDate());
                separation.setEmployeeIdProcessingBy(separationDTO.getEmployeeIdProcessingBy());
                separation.setApprovedById(separationDTO.getApprovedById());

                separationRepository.save(separation);

                return separationDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching Separation: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteSeparation(Long separationId) throws Exception {
        try {
            separationRepository.deleteById(separationId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete Separation: {}", e.getMessage());
        }

        return false;
    }
}