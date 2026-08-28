package com.primehr.security;

import com.primehr.integration.administrative.AdministrativeAuthorizationClient;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.integration.administrative.PermissionDataScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AssessmentPermissionGuard {
    public static final String ADMINISTRATION = "primehr.assessment-administration";
    public static final String ASSESSMENT = "primehr.competency-assessment";
    public static final String VALIDATION = "primehr.assessment-validation";
    public static final String PERSON_PROFILE = "primehr.person-profile";
    private static final Set<String> FEATURES = Set.of(ADMINISTRATION, ASSESSMENT, VALIDATION, PERSON_PROFILE);

    private final AdministrativeAuthorizationClient client;

    public AssessmentPermissionGuard(AdministrativeAuthorizationClient client) {
        this.client = client;
    }

    public EffectiveFeaturePermission require(String featureKey, PrimeHrAction action, String authorizationHeader) {
        requireBearer(authorizationHeader);
        if (!FEATURES.contains(featureKey)) throw new IllegalArgumentException("Unsupported assessment feature key");
        EffectiveFeaturePermission permission = client.resolve(featureKey, authorizationHeader);
        boolean allowed = permission.administrator() || permission.canAccess() && switch (action) {
            case ACCESS -> true;
            case ADD -> permission.canAdd();
            case EDIT -> permission.canEdit();
            case ARCHIVE -> permission.canDelete();
            case PUBLISH -> permission.canPublish();
            case SUBMIT -> permission.canSubmit();
            case APPROVE -> permission.canApprove();
            case ASSESS -> permission.canAssess();
            case VALIDATE -> permission.canValidate();
            case FINALIZE -> permission.canFinalize();
        };
        if (!allowed) throw new AccessDeniedException("The required assessment action is not permitted");
        return permission;
    }

    public EffectiveFeaturePermission requireAdministration(PrimeHrAction action, String authorizationHeader) {
        EffectiveFeaturePermission permission = require(ADMINISTRATION, action, authorizationHeader);
        if (!permission.administrator() && permission.dataScope() != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("Assessment administration requires agency-wide data scope");
        }
        return permission;
    }

    public EffectiveFeaturePermission requireAgencyWide(String featureKey, PrimeHrAction action,
                                                        String authorizationHeader) {
        EffectiveFeaturePermission permission = require(featureKey, action, authorizationHeader);
        if (!permission.administrator() && permission.dataScope() != PermissionDataScope.AGENCY_WIDE) {
            throw new AccessDeniedException("This assessment action requires agency-wide data scope");
        }
        return permission;
    }

    private static void requireBearer(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AccessDeniedException("A bearer token is required");
        }
    }
}
