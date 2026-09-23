package com.administrative.impl;

import com.administrative.dtos.PermissionRulesetDTO;
import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.administrative.services.PermissionRulesetService;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PermissionRulesetImpl implements PermissionRulesetService {

    private final PermissionRulesetRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public PermissionRulesetImpl(PermissionRulesetRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PermissionRulesetDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PermissionRulesetDTO create(PermissionRulesetDTO dto) {
        if (repository.existsByPermissionName(dto.getPermissionName())) {
            throw new IllegalArgumentException("Permission ruleset name already exists: " + dto.getPermissionName());
        }
        PermissionRuleset entity = toEntity(dto);
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public PermissionRulesetDTO update(Long id, PermissionRulesetDTO dto) {
        PermissionRuleset entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission ruleset not found: " + id));

        if (repository.existsByPermissionNameAndPermissionIdNot(dto.getPermissionName(), id)) {
            throw new IllegalArgumentException("Permission ruleset name already exists: " + dto.getPermissionName());
        }

        entity.setPermissionName(dto.getPermissionName());
        entity.setIsAdministrator(dto.getIsAdministrator() != null ? dto.getIsAdministrator() : false);
        entity.setPermissionData(dto.getPermissionData());
        entity.setPortalModuleAccess(dto.getPortalModuleAccess());
        return toDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Permission ruleset not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public Optional<PermissionRulesetDTO> resolveByRole(String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }

        String normalizedRole = role.trim().replaceFirst("(?i)^ROLE_", "");
        if (normalizedRole.isBlank()
                || "null".equalsIgnoreCase(normalizedRole)
                || "undefined".equalsIgnoreCase(normalizedRole)) {
            return Optional.empty();
        }
        try {
            return repository.findById(Long.valueOf(normalizedRole)).map(this::toDTO);
        } catch (NumberFormatException ignored) {
            return repository.findByPermissionNameIgnoreCase(normalizedRole).map(this::toDTO);
        }
    }

    @Override
    public Optional<PermissionRulesetDTO> resolveForEmployee(String employeeNo) {
        if (employeeNo == null || employeeNo.isBlank()) {
            return Optional.empty();
        }

        List<String> roles = jdbcTemplate.query(
                "SELECT userRole FROM employee WHERE LOWER(employeeNo) = LOWER(?)",
                (resultSet, rowNumber) -> resultSet.getString(1),
                employeeNo.trim());
        return roles.stream()
                .filter(this::isCurrentPermissionId)
                .findFirst()
                .flatMap(this::resolveByRole);
    }

    private boolean isCurrentPermissionId(String role) {
        if (role == null || !role.trim().matches("\\d+")) {
            return false;
        }
        try {
            return Long.parseLong(role.trim()) > 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private PermissionRulesetDTO toDTO(PermissionRuleset entity) {
        return new PermissionRulesetDTO(
                entity.getPermissionId(),
                entity.getPermissionName(),
                entity.getIsAdministrator(),
                entity.getPermissionData(),
                entity.getPortalModuleAccess()
        );
    }

    private PermissionRuleset toEntity(PermissionRulesetDTO dto) {
        return new PermissionRuleset(
                dto.getPermissionName(),
                dto.getIsAdministrator() != null ? dto.getIsAdministrator() : false,
                dto.getPermissionData(),
                dto.getPortalModuleAccess()
        );
    }
}
