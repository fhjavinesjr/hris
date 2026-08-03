package com.administrative.sso;

import java.util.Locale;

public enum SsoTarget {
    ADMINISTRATIVE("administrative", "administrative"),
    HRM("hrm", "hrManagement"),
    TIMEKEEPING("timekeeping", "timeKeeping"),
    PAYROLL("payroll", "payroll"),
    PRIMEHR("primehr", "primeHr");

    private final String value;
    private final String permissionKey;

    SsoTarget(String value, String permissionKey) {
        this.value = value;
        this.permissionKey = permissionKey;
    }

    public String getValue() {
        return value;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public static SsoTarget fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("SSO target is required");
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (SsoTarget target : values()) {
            if (target.value.equals(normalized)) {
                return target;
            }
        }
        throw new IllegalArgumentException("Unsupported SSO target: " + value);
    }
}
