package com.administrative.impl;

import com.administrative.dtos.PlantillaDTO;
import com.administrative.entitymodels.Plantilla;
import com.administrative.repositories.PlantillaRepository;
import com.administrative.services.PlantillaService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlantillaImpl implements PlantillaService {

    private static final Logger log = LoggerFactory.getLogger(PlantillaImpl.class);
    private final PlantillaRepository plantillaRepository;

    public PlantillaImpl(PlantillaRepository plantillaRepository) {
        this.plantillaRepository = plantillaRepository;
    }

    @Transactional
    @Override
    public PlantillaDTO createPlantilla(PlantillaDTO plantillaDTO) throws Exception {
        try {
            Plantilla plantilla = new Plantilla(plantillaDTO.getPlantillaId(), plantillaDTO.getPlantillaName(), plantillaDTO.getJobPositionId());
            plantillaRepository.save(plantilla);

            return plantillaDTO;
        } catch(Exception e) {
            log.error("Error in creating Plantilla: ", e);
            return null;
        }
    }

    @Override
    public List<PlantillaDTO> getAllPlantilla() throws Exception {
        List<Plantilla> plantillaList = plantillaRepository.findAll();
        List<PlantillaDTO> plantillaDTOList = new ArrayList<>();

        for(Plantilla plantilla : plantillaList) {
            PlantillaDTO plantillaDTO = new PlantillaDTO();
            plantillaDTO.setPlantillaId(plantilla.getPlantillaId());
            plantillaDTO.setPlantillaName(plantilla.getPlantillaName());
            plantillaDTO.setJobPositionId(plantilla.getJobPositionId());

            plantillaDTOList.add(plantillaDTO);
        }

        return plantillaDTOList;
    }

    @Override
    public PlantillaDTO getPlantillaById(Long plantillaId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public PlantillaDTO updatePlantilla(Long plantillaId, PlantillaDTO plantillaDTO) throws Exception {
        try {
            Plantilla plantilla = plantillaRepository.findById(plantillaId).orElseThrow(() -> new RuntimeException("Plantilla not found"));
            if(plantilla != null) {
                plantilla.setPlantillaName(plantillaDTO.getPlantillaName());
                plantilla.setJobPositionId(plantillaDTO.getJobPositionId());

                plantillaRepository.save(plantilla);

                return plantillaDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching Plantilla: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deletePlantilla(Long plantillaId) throws Exception {
        try {
            plantillaRepository.deleteById(plantillaId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete Plantilla: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public List<PlantillaDTO> getByJobPositionId(Long jobPositionId) throws Exception {
        List<Plantilla> plantillaList = plantillaRepository.findByJobPositionId(jobPositionId);

        List<PlantillaDTO> plantillaDTOList = new ArrayList<>();

        for(Plantilla plantilla : plantillaList) {
            PlantillaDTO plantillaDTO = new PlantillaDTO();
            plantillaDTO.setPlantillaId(plantilla.getPlantillaId());
            plantillaDTO.setPlantillaName(plantilla.getPlantillaName());
            plantillaDTO.setJobPositionId(plantilla.getJobPositionId());

            plantillaDTOList.add(plantillaDTO);
        }

        return plantillaDTOList;
    }
}