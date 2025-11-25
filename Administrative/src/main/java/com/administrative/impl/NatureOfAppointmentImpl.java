package com.administrative.impl;

import com.administrative.dtos.NatureOfAppointmentDTO;
import com.administrative.entitymodels.NatureOfAppointment;
import com.administrative.repositories.NatureOfAppointmentRepository;
import com.administrative.services.NatureOfAppointmentService;
import com.administrative.services.NatureOfAppointmentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NatureOfAppointmentImpl implements NatureOfAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(NatureOfAppointmentImpl.class);
    private final NatureOfAppointmentRepository natureOfAppointmentRepository;

    public NatureOfAppointmentImpl(NatureOfAppointmentRepository natureOfAppointmentRepository) {
        this.natureOfAppointmentRepository = natureOfAppointmentRepository;
    }

    @Transactional
    @Override
    public NatureOfAppointmentDTO createNatureOfAppointment(NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception {
        try {
            NatureOfAppointment natureOfAppointment = new NatureOfAppointment(natureOfAppointmentDTO.getNatureOfAppointmentId(), natureOfAppointmentDTO.getCode(), natureOfAppointmentDTO.getNature());
            natureOfAppointmentRepository.save(natureOfAppointment);

            return natureOfAppointmentDTO;
        } catch(Exception e) {
            log.error("Error in creating NatureOfAppointment: ", e);
            return null;
        }
    }

    @Override
    public List<NatureOfAppointmentDTO> getAllNatureOfAppointment() throws Exception {
        List<NatureOfAppointment> natureOfAppointmentList = natureOfAppointmentRepository.findAll();
        List<NatureOfAppointmentDTO> natureOfAppointmenDTOList = new ArrayList<>();

        for(NatureOfAppointment natureOfAppointment : natureOfAppointmentList) {
            NatureOfAppointmentDTO natureOfAppointmentDTO = new NatureOfAppointmentDTO();
            natureOfAppointmentDTO.setNatureOfAppointmentId(natureOfAppointment.getNatureOfAppointmentId());
            natureOfAppointmentDTO.setCode(natureOfAppointment.getCode());
            natureOfAppointmentDTO.setNature(natureOfAppointment.getNature());

            natureOfAppointmenDTOList.add(natureOfAppointmentDTO);
        }

        return natureOfAppointmenDTOList;
    }

    @Override
    public NatureOfAppointmentDTO getNatureOfAppointmentById(Long natureOfAppointmentId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public NatureOfAppointmentDTO updateNatureOfAppointment(Long natureOfAppointmentId, NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception {
        try {
            NatureOfAppointment natureOfAppointment = natureOfAppointmentRepository.findById(natureOfAppointmentId).orElseThrow(() -> new RuntimeException("NatureOfAppointment not found"));
            if(natureOfAppointment != null) {
                natureOfAppointment.setCode(natureOfAppointmentDTO.getCode());
                natureOfAppointment.setNature(natureOfAppointmentDTO.getNature());

                natureOfAppointmentRepository.save(natureOfAppointment);

                return natureOfAppointmentDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching NatureOfAppointment: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteNatureOfAppointment(Long natureOfAppointmentId) throws Exception {
        try {
            natureOfAppointmentRepository.deleteById(natureOfAppointmentId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete NatureOfAppointment: {}", e.getMessage());
        }

        return false;
    }
}