package com.humanresource.services;

import com.humanresource.dtos.ReferencesDTO;

import java.util.List;
import java.util.Map;

public interface ReferencesService {

    ReferencesDTO createReferences(ReferencesDTO referencesDTO);

    ReferencesDTO getReferencesByReferencesId(Long referencesId) throws Exception;

    List<ReferencesDTO> getReferencesByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateReferences(Long referencesId, Map<String, Object> updates) throws Exception;

    Boolean deleteReferences(Long referencesId) throws Exception;

}