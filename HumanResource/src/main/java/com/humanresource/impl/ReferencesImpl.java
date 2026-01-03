package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.ReferencesDTO;
import com.humanresource.entitymodels.References;
import com.humanresource.repositories.ReferencesRepository;
import com.humanresource.services.ReferencesService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReferencesImpl implements ReferencesService {

    private static final Logger log = LoggerFactory.getLogger(ReferencesImpl.class);
    private final ReferencesRepository referencesRepository;
    private final ObjectMapper objectMapper;

    public ReferencesImpl(ReferencesRepository referencesRepository, ObjectMapper objectMapper) {
        this.referencesRepository = referencesRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public ReferencesDTO createReferences(ReferencesDTO referencesDTO) {
        try {
            References references = new References(referencesDTO.getReferencesId(), referencesDTO.getPersonalDataId()
                ,referencesDTO.getRefName(), referencesDTO.getAddress(), referencesDTO.getContactNo());

            referencesRepository.save(references);
            return referencesDTO;
        } catch(Exception e) {
            log.info("Error creating references data: {}", e.getMessage());
            return null;
        }
    }

    public References getReferencesEntityByReferencesId(Long referencesId) {
        Optional<References> referencesOpt = referencesRepository.findById(referencesId);
        References references;
        if(referencesOpt.isPresent()) {
            references = referencesOpt.get();
            return references;
        }

        return null;
    }

    public List<References> getReferencesEntityByPersonalDataId(Long personalDataId) {
        List<References> referencesList = referencesRepository.findByPersonalDataId(personalDataId);
        if(referencesList.isEmpty()) {
            return null;
        }

        return referencesList;
    }

    @Override
    public ReferencesDTO getReferencesByReferencesId(Long referencesId) {
        References references = getReferencesEntityByReferencesId(referencesId);
        if(references == null) {
            return null;
        }

        return new ReferencesDTO(references.getReferencesId(), references.getPersonalDataId()
                ,references.getRefName(), references.getAddress(), references.getContactNo());
    }

    @Override
    public List<ReferencesDTO> getReferencesByPersonalDataId(Long personalDataId) throws Exception {
        List<References> referencesList = getReferencesEntityByPersonalDataId(personalDataId);
        if(referencesList.isEmpty()) {
            return null;
        }
        List<ReferencesDTO> referencesDTOS = new ArrayList<>();
        for(References references : referencesList) {
            ReferencesDTO referencesDTO = new ReferencesDTO(references.getReferencesId(), references.getPersonalDataId()
                    ,references.getRefName(), references.getAddress(), references.getContactNo());
            referencesDTOS.add(referencesDTO);
        }

        return referencesDTOS;
    }

    @Transactional
    @Override
    public Boolean updateReferences(Long referencesId, Map<String, Object> updates) throws Exception {
        try {
            References referencesExisting = getReferencesEntityByReferencesId(referencesId);
            if(referencesExisting != null) {
                objectMapper.updateValue(referencesExisting, updates);
                referencesRepository.save(referencesExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteReferences(Long referencesId) throws Exception {
        try {
            referencesRepository.deleteById(referencesId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}