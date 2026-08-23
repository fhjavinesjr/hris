package com.administrative.impl;

import com.administrative.dtos.SystemConfigDTO;
import com.administrative.entitymodels.SystemConfig;
import com.administrative.repositories.SystemConfigRepository;
import com.administrative.services.SystemConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SystemConfigImpl implements SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigImpl.class);
    private static final Set<String> PUBLIC_RUNTIME_KEYS = Set.of(
            "api.url.administrative",
            "api.url.hrm",
            "api.url.timekeeping",
            "api.url.payroll",
            "api.url.primehr",
            "ui.url.administrative",
            "ui.url.hrm",
            "ui.url.timekeeping",
            "ui.url.payroll",
            "ui.url.employee-portal",
            "ui.url.primehr",
            "security.inactivity.timeout");
    private final SystemConfigRepository systemConfigRepository;

    public SystemConfigImpl(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    /**
     * Seeds default configuration values on first startup if they don't already exist.
     */
    @PostConstruct
    @Transactional
    public void seedDefaults() {
        seed("api.url.administrative", "http://localhost:8082", "Base URL of the Administrative backend service", "API Endpoints", true);
        seed("api.url.hrm",            "http://localhost:8085", "Base URL of the HR Management backend service",    "API Endpoints", true);
        seed("api.url.timekeeping",    "http://localhost:8083", "Base URL of the TimeKeeping backend service",      "API Endpoints", true);
        seed("api.url.payroll",        "http://localhost:8087", "Base URL of the Payroll backend service",          "API Endpoints", true);
        seed("api.url.primehr",        "http://localhost:8086", "Base URL of the PrimeHR backend service",          "API Endpoints", true);

        seed("security.inactivity.timeout", "1800", "Session inactivity timeout in seconds (default: 1800 = 30 min)", "Security", true);

        seed("payroll.batch.compute-threads", "0", "Parallel worker threads for payroll batch computation. 0 = auto (2 × CPU cores, max 64). Requires Payroll service restart to take effect.", "Payroll", true);

        seed("ui.url.administrative", "http://localhost:3082", "URL of the Administrative UI application", "UI Navigation", true);
        seed("ui.url.hrm",            "http://localhost:3085", "URL of the HR Management UI application",  "UI Navigation", true);
        seed("ui.url.timekeeping",    "http://localhost:3083", "URL of the TimeKeeping UI application",    "UI Navigation", true);
        seed("ui.url.payroll",        "http://localhost:3087", "URL of the Payroll UI application",        "UI Navigation", true);
        seed("ui.url.employee-portal","http://localhost:3081", "URL of the Employee Portal UI application","UI Navigation", true);
        seed("ui.url.primehr",        "http://localhost:3086", "URL of the PrimeHR UI application",        "UI Navigation", true);
    }

    private void seed(String key, String value, String description, String category, Boolean editable) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            systemConfigRepository.save(new SystemConfig(key, value, description, category, editable));
        }
    }

    @Override
    public List<SystemConfigDTO> getAllConfigs() throws Exception {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        List<SystemConfigDTO> dtos = new ArrayList<>();
        for (SystemConfig c : configs) {
            dtos.add(toDTO(c));
        }
        return dtos;
    }

    @Override
    public Map<String, String> getPublicRuntimeConfig() {
        Map<String, String> runtimeConfig = new LinkedHashMap<>();
        systemConfigRepository.findAll().stream()
                .filter(config -> PUBLIC_RUNTIME_KEYS.contains(config.getConfigKey()))
                .forEach(config -> runtimeConfig.put(config.getConfigKey(), config.getConfigValue()));
        return runtimeConfig;
    }

    @Override
    public SystemConfigDTO getByKey(String configKey) throws Exception {
        return systemConfigRepository.findById(configKey)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    @Override
    public SystemConfigDTO updateConfig(String configKey, SystemConfigDTO dto) throws Exception {
        try {
            SystemConfig config = systemConfigRepository.findById(configKey).orElse(null);
            if (config == null) {
                log.warn("SystemConfig key '{}' not found", configKey);
                return null;
            }
            if (!Boolean.TRUE.equals(config.getEditable())) {
                log.warn("SystemConfig key '{}' is not editable", configKey);
                return null;
            }
            if (dto == null || dto.getConfigValue() == null || dto.getConfigValue().isBlank()) {
                log.warn("SystemConfig key '{}' cannot be blank", configKey);
                return null;
            }
            config.setConfigValue(normalizeValue(configKey, dto.getConfigValue()));
            systemConfigRepository.save(config);
            return toDTO(config);
        } catch (IllegalArgumentException e) {
            log.warn("Rejected invalid SystemConfig value for key '{}': {}", configKey, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error updating SystemConfig key '{}': ", configKey, e);
            return null;
        }
    }

    private String normalizeValue(String configKey, String value) {
        String normalized = value.trim();
        if (!configKey.startsWith("api.url.") && !configKey.startsWith("ui.url.")) {
            return normalized;
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        URI uri = URI.create(normalized);
        boolean supportedScheme = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!supportedScheme || uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException(
                    "SystemConfig key '" + configKey + "' must contain an absolute HTTP(S) URL");
        }
        return normalized;
    }

    private SystemConfigDTO toDTO(SystemConfig c) {
        return new SystemConfigDTO(c.getConfigKey(), c.getConfigValue(), c.getDescription(), c.getCategory(), c.getEditable());
    }
}
