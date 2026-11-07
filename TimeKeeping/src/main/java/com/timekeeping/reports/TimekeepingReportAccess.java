package com.timekeeping.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Enforces the independent, access-only permissions for Timekeeping PDFs. */
@Component
public class TimekeepingReportAccess {
    private static final Set<String> REPORT_KEYS = Set.of(
            "tk.reports.mra", "tk.reports.tardiness", "tk.reports.undertime",
            "tk.reports.overtime", "tk.reports.nightDifferential", "tk.reports.incompleteDtr");

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public TimekeepingReportAccess(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public void requireAccess(Authentication authentication, String featureKey) {
        if (!REPORT_KEYS.contains(featureKey)) {
            throw new IllegalArgumentException("Unknown Timekeeping report permission.");
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required for Timekeeping reports.");
        }
        String role = authentication.getAuthorities().stream().findFirst()
                .map(authority -> authority.getAuthority().replaceFirst("(?i)^ROLE_", ""))
                .orElse("");
        if ("admin".equalsIgnoreCase(authentication.getName())) return;
        List<Rule> rules;
        try {
            long id = Long.parseLong(role);
            rules = jdbc.query("SELECT permissionData FROM permission_ruleset "
                    + "WHERE permissionId = ?", (rs, index) -> new Rule(rs.getString("permissionData")), id);
        } catch (NumberFormatException ignored) {
            rules = jdbc.query("SELECT permissionData FROM permission_ruleset "
                    + "WHERE LOWER(permissionName) = LOWER(?)", (rs, index) -> new Rule(rs.getString("permissionData")), role);
        }
        if (!rules.isEmpty()) {
            Rule rule = rules.get(0);
            try {
                if (json.readTree(rule.permissions()).path(featureKey).path("canAccess").asBoolean(false)) return;
            } catch (Exception ignored) {
                // Malformed permission data fails closed.
            }
        }
        throw new AccessDeniedException("Access to this Timekeeping report is required.");
    }

    private record Rule(String permissions) { }
}
