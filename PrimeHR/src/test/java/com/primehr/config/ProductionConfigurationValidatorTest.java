package com.primehr.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTest {
    private static final String STAFF_SECRET = "staff-production-secret-with-32-characters";
    private static final String HANDOFF_SECRET = "handoff-production-secret-with-32-characters";

    @Test
    void acceptsExplicitNonLocalProductionConfiguration() {
        assertThatCode(() -> validator(properties(STAFF_SECRET, false, ""), environment()).validate())
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsDevelopmentSecretAndLocalDependencies() {
        assertThatThrownBy(() -> validator(properties("secret", false, ""), environment()).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("PRIMEHR_JWT_SECRET");

        PrimeHrProperties local = properties(STAFF_SECRET, false, "", "http://localhost:8082");
        assertThatThrownBy(() -> validator(local, environment()).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("PRIMEHR_ADMINISTRATIVE_API_URL");
    }

    @Test
    void requiresSeparateApplicantSecretWhenApplicantPortalIsEnabled() {
        assertThatThrownBy(() -> validator(properties(STAFF_SECRET, true, STAFF_SECRET), environment()).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("must be different");
        assertThatThrownBy(() -> validator(properties(STAFF_SECRET, true, ""), environment()).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("PRIMEHR_APPLICANT_JWT_SECRET");
    }

    @Test
    void rejectsLocalOrIncompleteDatasourceConfiguration() {
        MockEnvironment local = environment().withProperty("spring.datasource.url",
                "jdbc:postgresql://localhost:5432/primehr");
        assertThatThrownBy(() -> validator(properties(STAFF_SECRET, false, ""), local).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("PRIMEHR_DB_URL");

        MockEnvironment missingPassword = new MockEnvironment();
        missingPassword.setActiveProfiles("production-postgresql");
        missingPassword
                .withProperty("spring.datasource.url", "jdbc:postgresql://database.internal:5432/primehr")
                .withProperty("spring.datasource.username", "primehr")
                .withProperty("primehr.handoff.jwt-secret", HANDOFF_SECRET);
        assertThatThrownBy(() -> validator(properties(STAFF_SECRET, false, ""), missingPassword).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("PRIMEHR_DB_PASSWORD");
    }

    @Test
    void rejectsDatasourceThatDoesNotMatchTheActiveProductionProfile() {
        MockEnvironment sqlServerProfile = new MockEnvironment();
        sqlServerProfile.setActiveProfiles("production-sqlserver");
        sqlServerProfile
                .withProperty("spring.datasource.url", "jdbc:postgresql://database.internal:5432/primehr")
                .withProperty("spring.datasource.username", "primehr")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("primehr.handoff.jwt-secret", HANDOFF_SECRET);

        assertThatThrownBy(() -> validator(properties(STAFF_SECRET, false, ""), sqlServerProfile).validate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("PRIMEHR_DB_URL");
    }

    private static ProductionConfigurationValidator validator(PrimeHrProperties properties,
                                                               MockEnvironment environment) {
        return new ProductionConfigurationValidator(properties, environment);
    }

    private static MockEnvironment environment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production-postgresql");
        return environment
                .withProperty("spring.datasource.url", "jdbc:postgresql://database.internal:5432/primehr")
                .withProperty("spring.datasource.username", "primehr")
                .withProperty("spring.datasource.password", "database-password")
                .withProperty("primehr.handoff.jwt-secret", HANDOFF_SECRET);
    }

    private static PrimeHrProperties properties(String staffSecret, boolean applicantEnabled,
                                                 String applicantSecret) {
        return properties(staffSecret, applicantEnabled, applicantSecret, "https://administrative.internal");
    }

    private static PrimeHrProperties properties(String staffSecret, boolean applicantEnabled,
                                                 String applicantSecret, String administrativeUrl) {
        return new PrimeHrProperties(
                new PrimeHrProperties.Security(staffSecret, List.of()),
                new PrimeHrProperties.Cors(List.of("https://primehr.example.gov.ph"), List.of()),
                new PrimeHrProperties.Agency("AGENCY-1"),
                new PrimeHrProperties.Administrative(administrativeUrl, 1000, 2000),
                new PrimeHrProperties.HumanResource("https://hrm.internal", 1000, 2000),
                new PrimeHrProperties.Applicant(applicantEnabled, applicantSecret, 30L, 5, 15L,
                        List.of("PDS"), false),
                new PrimeHrProperties.Storage(false, "local", "", 1024L,
                        List.of("application/pdf"), "", "ap-southeast-1", ""), null);
    }
}
