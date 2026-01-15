package com.administrative.impl;

import com.administrative.dtos.LeaveTypesDTO;
import com.administrative.entitymodels.LeaveTypes;
import com.administrative.repositories.LeaveTypesRepository;
import com.administrative.services.LeaveTypesService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveTypesImpl implements LeaveTypesService {

    private static final Logger log = LoggerFactory.getLogger(LeaveTypesImpl.class);
    private final LeaveTypesRepository leaveTypesRepository;

    public LeaveTypesImpl(LeaveTypesRepository leaveTypesRepository) {
        this.leaveTypesRepository = leaveTypesRepository;
    }

    @Transactional
    @Override
    public LeaveTypesDTO createLeaveTypes(LeaveTypesDTO leaveTypesDTO) throws Exception {
        try {
            LeaveTypes leaveTypes = new LeaveTypes(leaveTypesDTO.getLeaveTypesId(), leaveTypesDTO.getCode(), leaveTypesDTO.getName());
            leaveTypesRepository.save(leaveTypes);

            return leaveTypesDTO;
        } catch(Exception e) {
            log.error("Error in creating LeaveTypes: ", e);
            return null;
        }
    }

    @Override
    public List<LeaveTypesDTO> getAllLeaveTypes() throws Exception {
        List<LeaveTypes> leaveTypesList = leaveTypesRepository.findAll();
        List<LeaveTypesDTO> leaveTypesDTOList = new ArrayList<>();

        for(LeaveTypes leaveTypes : leaveTypesList) {
            LeaveTypesDTO leaveTypesDTO = new LeaveTypesDTO();
            leaveTypesDTO.setLeaveTypesId(leaveTypes.getLeaveTypesId());
            leaveTypesDTO.setCode(leaveTypes.getCode());
            leaveTypesDTO.setName(leaveTypes.getName());

            leaveTypesDTOList.add(leaveTypesDTO);
        }

        return leaveTypesDTOList;
    }

    @Override
    public LeaveTypesDTO getLeaveTypesById(Long leaveTypesId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public LeaveTypesDTO updateLeaveTypes(Long leaveTypesId, LeaveTypesDTO leaveTypesDTO) throws Exception {
        try {
            LeaveTypes leaveTypes = leaveTypesRepository.findById(leaveTypesId).orElseThrow(() -> new RuntimeException("LeaveTypes not found"));
            if(leaveTypes != null) {
                leaveTypes.setCode(leaveTypesDTO.getCode());
                leaveTypes.setName(leaveTypesDTO.getName());

                leaveTypesRepository.save(leaveTypes);

                return leaveTypesDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching LeaveTypes: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteLeaveTypes(Long leaveTypesId) throws Exception {
        try {
            leaveTypesRepository.deleteById(leaveTypesId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete LeaveTypes: {}", e.getMessage());
        }

        return false;
    }
}