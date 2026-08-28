package com.primehr.integration.administrative;

public record EffectiveFeaturePermission(
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
    public EffectiveFeaturePermission(String featureKey, boolean administrator, boolean canAccess,
                                      boolean canAdd, boolean canEdit, boolean canDelete,
                                      boolean canPublish, boolean canSubmit, boolean canApprove) {
        this(featureKey, administrator, canAccess, canAdd, canEdit, canDelete, canPublish, canSubmit, canApprove,
                false, false, false, PermissionDataScope.NONE);
    }
}
