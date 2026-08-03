package com.administrative.dtos;

public record EffectiveFeaturePermissionResponse(
        String featureKey,
        boolean administrator,
        boolean canAccess,
        boolean canAdd,
        boolean canEdit,
        boolean canDelete
) {
    public static EffectiveFeaturePermissionResponse denied(String featureKey) {
        return new EffectiveFeaturePermissionResponse(featureKey, false, false, false, false, false);
    }

    public static EffectiveFeaturePermissionResponse administrator(String featureKey) {
        return new EffectiveFeaturePermissionResponse(featureKey, true, true, true, true, true);
    }
}
