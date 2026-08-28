package com.administrative.dtos;

public record EffectiveFeaturePermissionResponse(
        String featureKey,
        boolean administrator,
        boolean canAccess,
        boolean canAdd,
        boolean canEdit,
        boolean canDelete,
        boolean canPublish,
        boolean canSubmit,
        boolean canApprove,
        boolean canAssess,
        boolean canValidate,
        boolean canFinalize,
        PermissionDataScope dataScope
) {
    public EffectiveFeaturePermissionResponse(String featureKey, boolean administrator, boolean canAccess,
                                              boolean canAdd, boolean canEdit, boolean canDelete,
                                              boolean canPublish, boolean canSubmit, boolean canApprove) {
        this(featureKey, administrator, canAccess, canAdd, canEdit, canDelete, canPublish, canSubmit, canApprove,
                false, false, false, PermissionDataScope.NONE);
    }

    public static EffectiveFeaturePermissionResponse denied(String featureKey) {
        return new EffectiveFeaturePermissionResponse(featureKey, false, false, false, false, false,
                false, false, false, false, false, false, PermissionDataScope.NONE);
    }

    public static EffectiveFeaturePermissionResponse administrator(String featureKey) {
        return new EffectiveFeaturePermissionResponse(featureKey, true, true, true, true, true,
                true, true, true, true, true, true, PermissionDataScope.AGENCY_WIDE);
    }
}
