package com.administrative.impl;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.administrative.services.EffectiveAuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EffectiveAuthorizationServiceImpl implements EffectiveAuthorizationService {
    public static final String PRIMEHR_COMPETENCY = "primehr.competency";
    public static final String PRIMEHR_POSITION_PROFILE = "primehr.position-profile";
    private static final String INSTALL_ADMIN_EMPLOYEE_NO = "admin";

    private final PermissionRulesetRepository repository;
    private final ObjectMapper objectMapper;

    public EffectiveAuthorizationServiceImpl(PermissionRulesetRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public EffectiveFeaturePermissionResponse resolve(String employeeNo, String role, String featureKey) {
        if (!PRIMEHR_COMPETENCY.equals(featureKey) && !PRIMEHR_POSITION_PROFILE.equals(featureKey)) {
            throw new IllegalArgumentException("Unsupported feature key");
        }
        if (employeeNo != null && INSTALL_ADMIN_EMPLOYEE_NO.equalsIgnoreCase(employeeNo.trim())) {
            return EffectiveFeaturePermissionResponse.administrator(featureKey);
        }
        Optional<PermissionRuleset> resolved = resolveRuleset(role);
        if (resolved.isEmpty()
                && role != null
                && "1".equals(role.trim().replaceFirst("(?i)^ROLE_", ""))) {
            return EffectiveFeaturePermissionResponse.administrator(featureKey);
        }
        if (resolved.isEmpty()) return EffectiveFeaturePermissionResponse.denied(featureKey);
        PermissionRuleset ruleset = resolved.get();
        if (Boolean.TRUE.equals(ruleset.getIsAdministrator())) {
            return EffectiveFeaturePermissionResponse.administrator(featureKey);
        }
        try {
            JsonNode permission = objectMapper.readTree(ruleset.getPermissionData()).path(featureKey);
            boolean canAccess = permission.path("canAccess").asBoolean(false);
            return new EffectiveFeaturePermissionResponse(featureKey, false,
                    canAccess,
                    permission.path("canAdd").asBoolean(false),
                    permission.path("canEdit").asBoolean(false),
                    permission.path("canDelete").asBoolean(false),
                    canAccess && permission.path("canPublish").asBoolean(false),
                    canAccess && permission.path("canSubmit").asBoolean(false),
                    canAccess && permission.path("canApprove").asBoolean(false));
        } catch (Exception exception) {
            return EffectiveFeaturePermissionResponse.denied(featureKey);
        }
    }

    private Optional<PermissionRuleset> resolveRuleset(String role) {
        if (role == null || role.isBlank()) return Optional.empty();
        String normalized = role.trim().replaceFirst("(?i)^ROLE_", "");
        try {
            return repository.findById(Long.valueOf(normalized));
        } catch (NumberFormatException ignored) {
            return repository.findByPermissionNameIgnoreCase(normalized);
        }
    }
}
