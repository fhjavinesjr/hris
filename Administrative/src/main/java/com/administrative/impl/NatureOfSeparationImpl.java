package com.administrative.impl;

import com.administrative.dtos.NatureOfSeparationDTO;
import com.administrative.entitymodels.NatureOfSeparation;
import com.administrative.repositories.NatureOfSeparationRepository;
import com.administrative.services.NatureOfSeparationService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class NatureOfSeparationImpl implements NatureOfSeparationService {

    private static final Logger log = LoggerFactory.getLogger(NatureOfSeparationImpl.class);
    private final NatureOfSeparationRepository natureOfSeparationRepository;
    private static final Set<String> BIR_REASONS = Set.of("T", "TR", "R", "D");

    private String birReason(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!BIR_REASONS.contains(normalized)) {
            throw new IllegalArgumentException("BIR separation reason must be T, TR, R, or D.");
        }
        return normalized;
    }

    public NatureOfSeparationImpl(NatureOfSeparationRepository natureOfSeparationRepository) {
        this.natureOfSeparationRepository = natureOfSeparationRepository;
    }

    @Transactional
    @Override
    public NatureOfSeparationDTO createNatureOfSeparation(NatureOfSeparationDTO natureOfSeparationDTO) throws Exception {
        String reason = birReason(natureOfSeparationDTO.getBirSeparationReasonCode());
        natureOfSeparationDTO.setBirSeparationReasonCode(reason);
        try {
            NatureOfSeparation natureOfSeparation = new NatureOfSeparation(natureOfSeparationDTO.getNatureOfSeparationId(), natureOfSeparationDTO.getCode(), natureOfSeparationDTO.getNature());
            natureOfSeparation.setBirSeparationReasonCode(reason);
            natureOfSeparationRepository.save(natureOfSeparation);

            return natureOfSeparationDTO;
        } catch(Exception e) {
            log.error("Error in creating NatureOfSeparation: ", e);
            return null;
        }
    }

    @Override
    public List<NatureOfSeparationDTO> getAllNatureOfSeparation() throws Exception {
        List<NatureOfSeparation> natureOfSeparationList = natureOfSeparationRepository.findAll();
        List<NatureOfSeparationDTO> natureOfSeparationDTOList = new ArrayList<>();

        for(NatureOfSeparation natureOfSeparation : natureOfSeparationList) {
            NatureOfSeparationDTO natureOfSeparationDTO = new NatureOfSeparationDTO();
            natureOfSeparationDTO.setNatureOfSeparationId(natureOfSeparation.getNatureOfSeparationId());
            natureOfSeparationDTO.setCode(natureOfSeparation.getCode());
            natureOfSeparationDTO.setNature(natureOfSeparation.getNature());
            natureOfSeparationDTO.setBirSeparationReasonCode(natureOfSeparation.getBirSeparationReasonCode());

            natureOfSeparationDTOList.add(natureOfSeparationDTO);
        }

        return natureOfSeparationDTOList;
    }

    @Override
    public NatureOfSeparationDTO getNatureOfSeparationById(Long natureOfSeparationId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public NatureOfSeparationDTO updateNatureOfSeparation(Long natureOfSeparationId, NatureOfSeparationDTO natureOfSeparationDTO) throws Exception {
        String reason = birReason(natureOfSeparationDTO.getBirSeparationReasonCode());
        natureOfSeparationDTO.setBirSeparationReasonCode(reason);
        try {
            NatureOfSeparation natureOfSeparation = natureOfSeparationRepository.findById(natureOfSeparationId).orElseThrow(() -> new RuntimeException("NatureOfSeparation not found"));
            if(natureOfSeparation != null) {
                natureOfSeparation.setCode(natureOfSeparationDTO.getCode());
                natureOfSeparation.setNature(natureOfSeparationDTO.getNature());
                natureOfSeparation.setBirSeparationReasonCode(reason);

                natureOfSeparationRepository.save(natureOfSeparation);

                return natureOfSeparationDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching NatureOfSeparation: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteNatureOfSeparation(Long natureOfSeparationId) throws Exception {
        try {
            natureOfSeparationRepository.deleteById(natureOfSeparationId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete NatureOfSeparation: {}", e.getMessage());
        }

        return false;
    }
}
