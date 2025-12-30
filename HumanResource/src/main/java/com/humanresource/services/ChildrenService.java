package com.humanresource.services;

import com.humanresource.dtos.ChildrenDTO;

import java.util.List;
import java.util.Map;

public interface ChildrenService {

    ChildrenDTO createChildren(ChildrenDTO childrenDTO);

    ChildrenDTO getChildrenByChildrenId(Long childrenId) throws Exception;

    List<ChildrenDTO> getChildrenByPersonalDataId(Long personalDataId) throws Exception;

    Boolean updateChildren(Long childrenId, Map<String, Object> updates) throws Exception;

    Boolean deleteChildren(Long childrenId) throws Exception;

}
