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
import java.util.List;

@Service
public class SystemConfigImpl implements SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigImpl.class);
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

        seed("security.inactivity.timeout", "1800", "Session inactivity timeout in seconds (default: 1800 = 30 min)", "Security", true);

        seed("ui.url.administrative", "http://localhost:3082", "URL of the Administrative UI application", "UI Navigation", true);
        seed("ui.url.hrm",            "http://localhost:3085", "URL of the HR Management UI application",  "UI Navigation", true);
        seed("ui.url.timekeeping",    "http://localhost:3083", "URL of the TimeKeeping UI application",    "UI Navigation", true);
        seed("ui.url.payroll",        "http://localhost:3087", "URL of the Payroll UI application",        "UI Navigation", true);
        seed("ui.url.employee-portal","http://localhost:3081", "URL of the Employee Portal UI application","UI Navigation", true);
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
            config.setConfigValue(dto.getConfigValue());
            systemConfigRepository.save(config);
            return toDTO(config);
        } catch (Exception e) {
            log.error("Error updating SystemConfig key '{}': ", configKey, e);
            return null;
        }
    }

    private SystemConfigDTO toDTO(SystemConfig c) {
        return new SystemConfigDTO(c.getConfigKey(), c.getConfigValue(), c.getDescription(), c.getCategory(), c.getEditable());
    }
}
