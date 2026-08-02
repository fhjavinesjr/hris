package com.hris.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

/**
 * Runtime source of truth for centralized ISOFT HRIS configuration.
 *
 * Values are read directly from the shared system_config table on every resolution,
 * so an authorized Administrative settings change takes effect without restarting a
 * standalone module or the combined HRISApp. Application properties/environment
 * values are bootstrap fallbacks only when the centralized value is unavailable.
 */
@Component
public class SystemConfigRuntimeResolver {

    public static final String API_ADMINISTRATIVE = "api.url.administrative";
    public static final String API_HRM = "api.url.hrm";
    public static final String API_TIMEKEEPING = "api.url.timekeeping";
    public static final String API_PAYROLL = "api.url.payroll";

    private static final Logger log = LoggerFactory.getLogger(SystemConfigRuntimeResolver.class);
    private static final String FIND_VALUE_SQL =
            "SELECT configValue FROM system_config WHERE configKey = ?";

    private final JdbcTemplate jdbcTemplate;

    public SystemConfigRuntimeResolver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String resolveApiUrl(String configKey, String bootstrapFallback) {
        String configured = readValue(configKey);
        if (StringUtils.hasText(configured)) {
            return normalizeHttpUrl(configKey, configured);
        }
        if (StringUtils.hasText(bootstrapFallback)) {
            log.warn("SystemConfig key '{}' is unavailable; using bootstrap fallback", configKey);
            return normalizeHttpUrl(configKey, bootstrapFallback);
        }
        throw new IllegalStateException("Missing required SystemConfig URL: " + configKey);
    }

    private String readValue(String configKey) {
        try {
            List<String> values = jdbcTemplate.query(
                    FIND_VALUE_SQL,
                    (rs, rowNum) -> rs.getString(1),
                    configKey);
            return values.isEmpty() ? null : values.get(0);
        } catch (DataAccessException ex) {
            log.warn("Unable to read SystemConfig key '{}': {}", configKey, ex.getMessage());
            return null;
        }
    }

    private String normalizeHttpUrl(String configKey, String value) {
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid URL in SystemConfig key: " + configKey, ex);
        }
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || !StringUtils.hasText(uri.getHost())) {
            throw new IllegalStateException("SystemConfig key '" + configKey
                    + "' must contain an absolute HTTP(S) URL");
        }
        return normalized;
    }
}
