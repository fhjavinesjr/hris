package com.administrative.impl;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;
import com.administrative.dtos.PermissionDataScope;
import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import com.administrative.services.EffectiveAuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class EffectiveAuthorizationServiceImpl implements EffectiveAuthorizationService {
    public static final String PRIMEHR_COMPETENCY = "primehr.competency";
    public static final String PRIMEHR_POSITION_PROFILE = "primehr.position-profile";
    public static final String PRIMEHR_ASSESSMENT_ADMINISTRATION = "primehr.assessment-administration";
    public static final String PRIMEHR_COMPETENCY_ASSESSMENT = "primehr.competency-assessment";
    public static final String PRIMEHR_ASSESSMENT_VALIDATION = "primehr.assessment-validation";
    public static final String PRIMEHR_PERSON_PROFILE = "primehr.person-profile";
    public static final String PRIMEHR_GAP_CONFIGURATION = "primehr.gap-configuration";
    public static final String PRIMEHR_COMPETENCY_GAP = "primehr.competency-gap";
    public static final String PRIMEHR_LD_REFERRAL = "primehr.ld-referral";
    public static final String ADMIN_QUALIFICATION_STANDARD = "administrative.qualification-standard";
    public static final String PRIMEHR_RSP_RECRUITMENT_PLANNING = "primehr.rsp-recruitment-planning";
    public static final String PRIMEHR_RSP_VACANCY_PUBLICATION = "primehr.rsp-vacancy-publication";
    public static final String PRIMEHR_RSP_APPLICANT_INTAKE = "primehr.rsp-applicant-intake";
    private static final Set<String> SUPPORTED_FEATURES = Set.of(PRIMEHR_COMPETENCY,
            PRIMEHR_POSITION_PROFILE, PRIMEHR_ASSESSMENT_ADMINISTRATION, PRIMEHR_COMPETENCY_ASSESSMENT,
            PRIMEHR_ASSESSMENT_VALIDATION, PRIMEHR_PERSON_PROFILE, PRIMEHR_GAP_CONFIGURATION,
            PRIMEHR_COMPETENCY_GAP, PRIMEHR_LD_REFERRAL, ADMIN_QUALIFICATION_STANDARD,
            PRIMEHR_RSP_RECRUITMENT_PLANNING, PRIMEHR_RSP_VACANCY_PUBLICATION,
            PRIMEHR_RSP_APPLICANT_INTAKE);
    private static final String INSTALL_ADMIN_EMPLOYEE_NO = "admin";

    private final PermissionRulesetRepository repository;
    private final ObjectMapper objectMapper;

    public EffectiveAuthorizationServiceImpl(PermissionRulesetRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public EffectiveFeaturePermissionResponse resolve(String employeeNo, String role, String featureKey) {
        if (!SUPPORTED_FEATURES.contains(featureKey)) {
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
            PermissionDataScope dataScope = canAccess
                    ? PermissionDataScope.fromPersisted(permission.path("dataScope").asText(null))
                    : PermissionDataScope.NONE;
            return new EffectiveFeaturePermissionResponse(featureKey, false,
                    canAccess,
                    canAccess && permission.path("canAdd").asBoolean(false),
                    canAccess && permission.path("canEdit").asBoolean(false),
                    canAccess && permission.path("canDelete").asBoolean(false),
                    canAccess && permission.path("canPublish").asBoolean(false),
                    canAccess && permission.path("canSubmit").asBoolean(false),
                    canAccess && permission.path("canApprove").asBoolean(false),
                    canAccess && permission.path("canAssess").asBoolean(false),
                    canAccess && permission.path("canValidate").asBoolean(false),
                    canAccess && permission.path("canFinalize").asBoolean(false),
                    dataScope);
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
