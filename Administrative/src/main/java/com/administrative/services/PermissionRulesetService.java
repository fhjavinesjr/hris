package com.administrative.services;

import com.administrative.dtos.PermissionRulesetDTO;
import java.util.List;
import java.util.Optional;

public interface PermissionRulesetService {
    List<PermissionRulesetDTO> getAll();
    PermissionRulesetDTO create(PermissionRulesetDTO dto);
    PermissionRulesetDTO update(Long id, PermissionRulesetDTO dto);
    void delete(Long id);
    Optional<PermissionRulesetDTO> resolveByRole(String role);
    Optional<PermissionRulesetDTO> resolveForEmployee(String employeeNo);
}
