package com.primehr.security;

import com.primehr.config.PrimeHrProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ConfiguredCompetencyReadPermissionResolver implements CompetencyReadPermissionResolver {

    private static final String INSTALL_ADMIN_EMPLOYEE_NO = "admin";
    private static final String ESTABLISHED_ADMIN_ROLE = "1";

    private final Set<String> competencyReaderRoles;

    public ConfiguredCompetencyReadPermissionResolver(PrimeHrProperties properties) {
        this.competencyReaderRoles = properties.security().competencyReaderRoles().stream()
                .map(ConfiguredCompetencyReadPermissionResolver::normalizeRole)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean canReadCompetencies(String employeeNo, String role) {
        if (employeeNo != null && INSTALL_ADMIN_EMPLOYEE_NO.equalsIgnoreCase(employeeNo.trim())) {
            return true;
        }
        String normalizedRole = normalizeRole(role);
        return ESTABLISHED_ADMIN_ROLE.equals(normalizedRole) || competencyReaderRoles.contains(normalizedRole);
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        return role.trim().replaceFirst("(?i)^ROLE_", "").toUpperCase(Locale.ROOT);
    }
}
