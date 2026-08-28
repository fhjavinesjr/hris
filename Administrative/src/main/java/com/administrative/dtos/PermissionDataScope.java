package com.administrative.dtos;

public enum PermissionDataScope {
    NONE,
    OWN_RECORDS,
    ASSIGNED_RECORDS,
    AGENCY_WIDE;

    public static PermissionDataScope fromPersisted(String value) {
        if (value == null || value.isBlank()) return NONE;
        try {
            return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
