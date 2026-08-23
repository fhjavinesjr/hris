package com.administrative.impl;

import com.administrative.dtos.SystemConfigDTO;
import com.administrative.entitymodels.SystemConfig;
import com.administrative.repositories.SystemConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigImplTest {

    @Mock
    private SystemConfigRepository repository;

    private SystemConfigImpl service;

    @BeforeEach
    void setUp() {
        service = new SystemConfigImpl(repository);
    }

    @Test
    void publicRuntimeConfigContainsOnlyAllowlistedValues() {
        when(repository.findAll()).thenReturn(List.of(
                config("api.url.hrm", "http://192.168.1.2:8085"),
                config("ui.url.employee-portal", "http://192.168.1.2:3081"),
                config("security.inactivity.timeout", "1800"),
                config("payroll.batch.compute-threads", "8")));

        Map<String, String> result = service.getPublicRuntimeConfig();

        assertThat(result).containsOnly(
                Map.entry("api.url.hrm", "http://192.168.1.2:8085"),
                Map.entry("ui.url.employee-portal", "http://192.168.1.2:3081"),
                Map.entry("security.inactivity.timeout", "1800"));
    }

    @Test
    void updateNormalizesAbsoluteHttpUrl() throws Exception {
        SystemConfig existing = config("api.url.hrm", "http://localhost:8085");
        when(repository.findById("api.url.hrm")).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        SystemConfigDTO result = service.updateConfig(
                "api.url.hrm", new SystemConfigDTO(null, " https://hris.example.gov.ph/ ", null, null, null));

        assertThat(result).isNotNull();
        assertThat(result.getConfigValue()).isEqualTo("https://hris.example.gov.ph");
        verify(repository).save(existing);
    }

    @Test
    void updateRejectsUrlWithoutHttpScheme() throws Exception {
        SystemConfig existing = config("api.url.hrm", "http://localhost:8085");
        when(repository.findById("api.url.hrm")).thenReturn(Optional.of(existing));

        SystemConfigDTO result = service.updateConfig(
                "api.url.hrm", new SystemConfigDTO(null, "192.168.1.2:8085", null, null, null));

        assertThat(result).isNull();
        assertThat(existing.getConfigValue()).isEqualTo("http://localhost:8085");
        verify(repository, never()).save(existing);
    }

    private SystemConfig config(String key, String value) {
        return new SystemConfig(key, value, "description", "category", true);
    }
}
