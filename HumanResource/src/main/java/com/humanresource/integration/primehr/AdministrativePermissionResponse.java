package com.humanresource.integration.primehr;

public record AdministrativePermissionResponse(String featureKey, boolean administrator, boolean canAccess,
        boolean canAdd, boolean canEdit, boolean canDelete, boolean canPublish, boolean canSubmit,
        boolean canApprove, boolean canAssess, boolean canValidate, boolean canFinalize, String dataScope) {
}
