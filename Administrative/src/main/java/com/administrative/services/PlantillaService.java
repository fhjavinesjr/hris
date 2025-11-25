package com.administrative.services;

import com.administrative.dtos.PlantillaDTO;

import java.util.List;

public interface PlantillaService {

    PlantillaDTO createPlantilla(PlantillaDTO plantillaDTO) throws Exception;

    List<PlantillaDTO> getAllPlantilla() throws Exception;

    PlantillaDTO getPlantillaById(Long plantillaId) throws Exception;

    PlantillaDTO updatePlantilla(Long plantillaId, PlantillaDTO PlantillaDTO) throws Exception;

    Boolean deletePlantilla(Long plantillaId) throws Exception;

    List<PlantillaDTO> getByJobPositionId(Long jobPositionId) throws Exception;

}
