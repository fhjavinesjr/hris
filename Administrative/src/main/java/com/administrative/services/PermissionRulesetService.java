package com.administrative.services;

import com.administrative.dtos.PermissionRulesetDTO;
import java.util.List;

public interface PermissionRulesetService {
    List<PermissionRulesetDTO> getAll();
    PermissionRulesetDTO create(PermissionRulesetDTO dto);
    PermissionRulesetDTO update(Long id, PermissionRulesetDTO dto);
    void delete(Long id);
}
