package com.administrative.impl;

import com.administrative.dtos.AreasDTO;
import com.administrative.entitymodels.Areas;
import com.administrative.repositories.AreasRepository;
import com.administrative.services.AreasService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AreasImpl implements AreasService {

    private static final Logger log = LoggerFactory.getLogger(AreasImpl.class);
    private final AreasRepository areasRepository;

    public AreasImpl(AreasRepository areasRepository) {
        this.areasRepository = areasRepository;
    }

    @Transactional
    @Override
    public AreasDTO createAreas(AreasDTO areasDTO) throws Exception {
        try {
            Areas areas = new Areas(areasDTO.getAreasId(), areasDTO.getAreasName(), areasDTO.getAreasDescription());
            areasRepository.save(areas);

            return areasDTO;
        } catch(Exception e) {
            log.error("Error in creating Areas: ", e);
            return null;
        }
    }

    @Override
    public List<AreasDTO> getAllAreas() throws Exception {
        List<Areas> areasList = areasRepository.findAll();
        List<AreasDTO> areasDTOList = new ArrayList<>();

        for(Areas areas : areasList) {
            AreasDTO areasDTO = new AreasDTO();
            areasDTO.setAreasId(areas.getAreasId());
            areasDTO.setAreasName(areas.getAreasName());
            areasDTO.setAreasDescription(areas.getAreasDescription());

            areasDTOList.add(areasDTO);
        }

        return areasDTOList;
    }

    @Override
    public AreasDTO getAreasById(Long areasId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public AreasDTO updateAreas(Long areasId, AreasDTO areasDTO) throws Exception {
        try {
            Areas areas = areasRepository.findById(areasId).orElseThrow(() -> new RuntimeException("Areas not found"));
            if(areas != null) {
                areas.setAreasName(areasDTO.getAreasName());
                areas.setAreasDescription(areasDTO.getAreasDescription());

                areasRepository.save(areas);

                return areasDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching Areas: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteAreas(Long areasId) throws Exception {
        try {
            areasRepository.deleteById(areasId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete Areas: {}", e.getMessage());
        }

        return false;
    }
}