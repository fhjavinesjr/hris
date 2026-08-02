package com.hris.common.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemConfigRuntimeResolverTest {

    private EmbeddedDatabase database;
    private JdbcTemplate jdbc;
    private SystemConfigRuntimeResolver resolver;

    @BeforeEach
    void setUp() {
        database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        jdbc = new JdbcTemplate(database);
        jdbc.execute("CREATE TABLE system_config (configKey VARCHAR(100) PRIMARY KEY, configValue VARCHAR(500))");
        resolver = new SystemConfigRuntimeResolver(jdbc);
    }

    @AfterEach
    void tearDown() {
        database.shutdown();
    }

    @Test
    void centralizedValueHasPriorityAndIsReadAgainAfterAnUpdate() {
        jdbc.update("INSERT INTO system_config (configKey, configValue) VALUES (?, ?)",
                SystemConfigRuntimeResolver.API_HRM, "https://first.example.com/");

        assertThat(resolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, "http://localhost:8085"))
                .isEqualTo("https://first.example.com");

        jdbc.update("UPDATE system_config SET configValue = ? WHERE configKey = ?",
                "https://second.example.com", SystemConfigRuntimeResolver.API_HRM);

        assertThat(resolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, "http://localhost:8085"))
                .isEqualTo("https://second.example.com");
    }

    @Test
    void usesBootstrapFallbackOnlyWhenCentralizedValueIsMissing() {
        assertThat(resolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_TIMEKEEPING, "http://localhost:8083/"))
                .isEqualTo("http://localhost:8083");
    }

    @Test
    void rejectsInvalidCentralizedUrlsInsteadOfSilentlyRoutingElsewhere() {
        jdbc.update("INSERT INTO system_config (configKey, configValue) VALUES (?, ?)",
                SystemConfigRuntimeResolver.API_PAYROLL, "not-a-url");

        assertThatThrownBy(() -> resolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_PAYROLL, "http://localhost:8087"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SystemConfigRuntimeResolver.API_PAYROLL);
    }
}
