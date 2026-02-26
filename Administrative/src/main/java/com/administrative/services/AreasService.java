package com.administrative.services;

import com.administrative.dtos.AreasDTO;

import java.util.List;

public interface AreasService {

    AreasDTO createAreas(AreasDTO AreasDTO) throws Exception;

    List<AreasDTO> getAllAreas() throws Exception;

    AreasDTO getAreasById(Long areasId) throws Exception;

    AreasDTO updateAreas(Long areasId, AreasDTO AreasDTO) throws Exception;

    Boolean deleteAreas(Long areasId) throws Exception;

}
