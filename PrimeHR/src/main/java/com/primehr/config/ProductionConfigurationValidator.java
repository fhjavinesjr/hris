package com.primehr.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@Component
@Profile({"production-postgresql", "production-sqlserver"})
public class ProductionConfigurationValidator {
    private static final int MINIMUM_SECRET_LENGTH = 32;
    private static final List<String> DEVELOPMENT_SECRETS = List.of(
            "secret", "changeme", "password", "local-development-secret-change-before-production",
            "local-handoff-secret-change-before-production");

    private final PrimeHrProperties properties;
    private final Environment environment;

    public ProductionConfigurationValidator(PrimeHrProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @PostConstruct
    public void validate() {
        requireSecret("PRIMEHR_JWT_SECRET", properties.security().jwtSecret());
        requireSecret("PRIMEHR_HANDOFF_JWT_SECRET", environment.getProperty("primehr.handoff.jwt-secret"));
        if (properties.applicant().enabled()) {
            requireSecret("PRIMEHR_APPLICANT_JWT_SECRET", properties.applicant().jwtSecret());
            if (properties.applicant().jwtSecret().equals(properties.security().jwtSecret())) {
                throw invalid("PRIMEHR_APPLICANT_JWT_SECRET must be different from PRIMEHR_JWT_SECRET");
            }
        }
        requireText("PRIMEHR_AGENCY_ID", properties.agency().id());
        if ("DEFAULT".equalsIgnoreCase(properties.agency().id())) {
            throw invalid("PRIMEHR_AGENCY_ID cannot use the development default");
        }
        requireProductionUrl("PRIMEHR_ADMINISTRATIVE_API_URL", properties.administrative().baseUrl());
        requireProductionUrl("PRIMEHR_HUMAN_RESOURCE_API_URL", properties.humanResource().baseUrl());
        properties.cors().allowedOrigins()
                .forEach(value -> requireProductionUrl("PRIMEHR_CORS_ALLOWED_ORIGINS", value));
        properties.cors().allowedOriginPatterns().forEach(value -> {
            if (value == null || value.isBlank() || "*".equals(value.trim()) || isLocal(value)) {
                throw invalid("PRIMEHR_CORS_ALLOWED_ORIGIN_PATTERNS contains an unsafe production value");
            }
        });
        if (properties.cors().allowedOrigins().isEmpty() && properties.cors().allowedOriginPatterns().isEmpty()) {
            throw invalid("At least one production CORS origin or origin pattern is required");
        }
        requireProductionJdbcUrl(environment.getProperty("spring.datasource.url"));
        requireText("PRIMEHR_DB_USERNAME", environment.getProperty("spring.datasource.username"));
        requireText("PRIMEHR_DB_PASSWORD", environment.getProperty("spring.datasource.password"));
        validateStorage();
        if (properties.reconciliation().schedulingEnabled()) {
            requireSecret("PRIMEHR_RECONCILIATION_SERVICE_TOKEN", properties.reconciliation().serviceToken());
        }
    }

    private void validateStorage() {
        if (!properties.storage().enabled()) return;
        if ("local".equals(properties.storage().provider())) {
            requireText("PRIMEHR_DOCUMENT_ROOT", properties.storage().localRoot());
        } else if ("s3".equals(properties.storage().provider())) {
            requireText("PRIMEHR_DOCUMENT_BUCKET", properties.storage().s3Bucket());
        } else {
            throw invalid("PRIMEHR_STORAGE_PROVIDER must be local or s3");
        }
    }

    private static void requireSecret(String name, String value) {
        requireText(name, value);
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (value.trim().length() < MINIMUM_SECRET_LENGTH || DEVELOPMENT_SECRETS.contains(normalized)) {
            throw invalid(name + " must contain at least 32 non-default characters");
        }
    }

    private void requireProductionJdbcUrl(String value) {
        requireText("PRIMEHR_DB_URL", value);
        String normalized = value.toLowerCase(Locale.ROOT);
        String expectedPrefix = environment.matchesProfiles("production-postgresql")
                ? "jdbc:postgresql://"
                : "jdbc:sqlserver://";
        if (!normalized.startsWith(expectedPrefix) || isLocal(normalized)) {
            throw invalid("PRIMEHR_DB_URL must match the active production database profile and be non-local");
        }
    }

    private static void requireProductionUrl(String name, String value) {
        requireText(name, value);
        final URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException exception) {
            throw invalid(name + " must be an absolute non-local HTTP(S) URL");
        }
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || isLocal(uri.getHost())) {
            throw invalid(name + " must be an absolute non-local HTTP(S) URL");
        }
    }

    private static boolean isLocal(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return normalized.contains("localhost") || normalized.contains("127.0.0.1")
                || normalized.contains("0.0.0.0") || normalized.contains("[::1]")
                || normalized.equals("::1");
    }

    private static void requireText(String name, String value) {
        if (value == null || value.isBlank()) throw invalid(name + " is required in production");
    }

    private static IllegalStateException invalid(String message) {
        return new IllegalStateException("Unsafe PrimeHR production configuration: " + message);
    }
}
