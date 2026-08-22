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
        boolean canApprove
) {
}
