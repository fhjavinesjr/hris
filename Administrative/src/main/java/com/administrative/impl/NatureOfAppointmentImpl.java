package com.administrative.impl;

import com.administrative.dtos.NatureOfAppointmentDTO;
import com.administrative.entitymodels.NatureOfAppointment;
import com.administrative.repositories.NatureOfAppointmentRepository;
import com.administrative.services.NatureOfAppointmentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NatureOfAppointmentImpl implements NatureOfAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(NatureOfAppointmentImpl.class);
    private static final Set<String> BIR_EMPLOYMENT_STATUS_CODES =
            Set.of("R", "C", "CP", "S", "P", "AL");
    private final NatureOfAppointmentRepository natureOfAppointmentRepository;

    public NatureOfAppointmentImpl(NatureOfAppointmentRepository natureOfAppointmentRepository) {
        this.natureOfAppointmentRepository = natureOfAppointmentRepository;
    }

    @Transactional
    @Override
    public NatureOfAppointmentDTO createNatureOfAppointment(NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception {
        try {
            NatureOfAppointment natureOfAppointment = new NatureOfAppointment(natureOfAppointmentDTO.getNatureOfAppointmentId(), natureOfAppointmentDTO.getCode(), natureOfAppointmentDTO.getNature());
            natureOfAppointment.setIsContractual(Boolean.TRUE.equals(natureOfAppointmentDTO.getIsContractual()));
            String birStatus = normalizeBirEmploymentStatusCode(
                    natureOfAppointmentDTO.getBirEmploymentStatusCode());
            natureOfAppointment.setBirEmploymentStatusCode(birStatus);
            natureOfAppointmentRepository.save(natureOfAppointment);
            natureOfAppointmentDTO.setBirEmploymentStatusCode(birStatus);

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
            natureOfAppointmentDTO.setIsContractual(Boolean.TRUE.equals(natureOfAppointment.getIsContractual()));
            natureOfAppointmentDTO.setBirEmploymentStatusCode(
                    natureOfAppointment.getBirEmploymentStatusCode());

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
                natureOfAppointment.setIsContractual(Boolean.TRUE.equals(natureOfAppointmentDTO.getIsContractual()));
                String birStatus = normalizeBirEmploymentStatusCode(
                        natureOfAppointmentDTO.getBirEmploymentStatusCode());
                natureOfAppointment.setBirEmploymentStatusCode(birStatus);

                natureOfAppointmentRepository.save(natureOfAppointment);
                natureOfAppointmentDTO.setBirEmploymentStatusCode(birStatus);

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

    private String normalizeBirEmploymentStatusCode(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!BIR_EMPLOYMENT_STATUS_CODES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported BIR employment status code: " + value);
        }
        return normalized;
    }
}
