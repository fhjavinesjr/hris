package com.administrative.impl;

import com.administrative.dtos.PermissionRulesetDTO;
import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.administrative.services.PermissionRulesetService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionRulesetImpl implements PermissionRulesetService {

    private final PermissionRulesetRepository repository;

    public PermissionRulesetImpl(PermissionRulesetRepository repository) {
        this.repository = repository;
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

    private PermissionRulesetDTO toDTO(PermissionRuleset entity) {
        return new PermissionRulesetDTO(
                entity.getPermissionId(),
                entity.getPermissionName(),
                entity.getIsAdministrator(),
                entity.getPermissionData()
        );
    }

    private PermissionRuleset toEntity(PermissionRulesetDTO dto) {
        return new PermissionRuleset(
                dto.getPermissionName(),
                dto.getIsAdministrator() != null ? dto.getIsAdministrator() : false,
                dto.getPermissionData()
        );
    }
}
