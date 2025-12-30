package com.humanresource.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.dtos.ChildrenDTO;
import com.humanresource.entitymodels.Children;
import com.humanresource.repositories.ChildrenRepository;
import com.humanresource.services.ChildrenService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChildrenImpl implements ChildrenService {

    private static final Logger log = LoggerFactory.getLogger(ChildrenImpl.class);
    private final ChildrenRepository childrenRepository;
    private final ObjectMapper objectMapper;

    public ChildrenImpl(ChildrenRepository childrenRepository, ObjectMapper objectMapper) {
        this.childrenRepository = childrenRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public ChildrenDTO createChildren(ChildrenDTO childrenDTO) {
        try {
            Children children = new Children(childrenDTO.getChildrenId(), childrenDTO.getPersonalDataId(),
                    childrenDTO.getChildFullname(), childrenDTO.getDob());

            childrenRepository.save(children);
            return childrenDTO;
        } catch(Exception e) {
            log.info("Error creating children data: {}", e.getMessage());
            return null;
        }
    }

    public Children getChildrenEntityByChildrenId(Long childrenId) {
        Optional<Children> childrenOpt = childrenRepository.findById(childrenId);
        Children children;
        if(childrenOpt.isPresent()) {
            children = childrenOpt.get();
            return children;
        }

        return null;
    }

    public List<Children> getChildrenEntityByPersonalDataId(Long personalDataId) {
        List<Children> childrenList = childrenRepository.findByPersonalDataId(personalDataId);
        if(childrenList.isEmpty()) {
            return null;
        }

        return childrenList;
    }

    @Override
    public ChildrenDTO getChildrenByChildrenId(Long childrenId) {
        Children children = getChildrenEntityByChildrenId(childrenId);
        if(children == null) {
            return null;
        }

        return new ChildrenDTO(children.getChildrenId(), children.getPersonalDataId(),
                    children.getChildFullname(), children.getDob());
    }

    @Override
    public List<ChildrenDTO> getChildrenByPersonalDataId(Long personalDataId) throws Exception {
        List<Children> childrenList = getChildrenEntityByPersonalDataId(personalDataId);
        if(childrenList.isEmpty()) {
            return null;
        }
        List<ChildrenDTO> childrenDTOS = new ArrayList<>();
        for(Children children : childrenList) {
            ChildrenDTO childrenDTO = new ChildrenDTO(children.getChildrenId(), children.getPersonalDataId(),
                    children.getChildFullname(), children.getDob());
            childrenDTOS.add(childrenDTO);
        }

        return childrenDTOS;
    }

    @Transactional
    @Override
    public Boolean updateChildren(Long childrenId, Map<String, Object> updates) throws Exception {
        try {
            Children childrenExisting = getChildrenEntityByChildrenId(childrenId);
            if(childrenExisting != null) {
                objectMapper.updateValue(childrenExisting, updates);
                childrenRepository.save(childrenExisting);
            }
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public Boolean deleteChildren(Long childrenId) throws Exception {
        try {
            childrenRepository.deleteById(childrenId);
            return true;
        } catch(Exception e) {
            return false;
        }
    }


}