package com.primehr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "primehr")
public record PrimeHrProperties(Security security, Cors cors, Agency agency, Administrative administrative,
                                HumanResource humanResource, Applicant applicant, Storage storage) {

    public PrimeHrProperties {
        security = security == null ? new Security(null, null) : security;
        cors = cors == null ? new Cors(null, null) : cors;
        agency = agency == null ? new Agency(null) : agency;
        administrative = administrative == null ? new Administrative(null, null, null) : administrative;
        humanResource = humanResource == null ? new HumanResource(null, null, null) : humanResource;
        applicant = applicant == null ? new Applicant(null, null, null, null, null, null, null) : applicant;
        storage = storage == null ? new Storage(null, null, null, null, null, null, null, null) : storage;
    }

    public record Security(String jwtSecret, List<String> competencyReaderRoles) {
        public Security {
            if (jwtSecret == null || jwtSecret.isBlank()) {
                jwtSecret = "";
            }
            competencyReaderRoles = competencyReaderRoles == null
                    ? List.of()
                    : competencyReaderRoles.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .toList();
        }
    }

    public record Cors(List<String> allowedOrigins, List<String> allowedOriginPatterns) {
        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
            allowedOriginPatterns = allowedOriginPatterns == null ? List.of() : List.copyOf(allowedOriginPatterns);
        }
    }

    public record Agency(String id) {
        public Agency {
            id = id == null ? "" : id.trim();
        }
    }

    public record Administrative(String baseUrl, Integer connectTimeoutMillis, Integer readTimeoutMillis) {
        public Administrative {
            baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
            connectTimeoutMillis = connectTimeoutMillis == null ? 3000 : Math.max(250, connectTimeoutMillis);
            readTimeoutMillis = readTimeoutMillis == null ? 5000 : Math.max(250, readTimeoutMillis);
        }
    }

    public record HumanResource(String baseUrl, Integer connectTimeoutMillis, Integer readTimeoutMillis) {
        public HumanResource {
            baseUrl = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
            connectTimeoutMillis = connectTimeoutMillis == null ? 3000 : Math.max(250, connectTimeoutMillis);
            readTimeoutMillis = readTimeoutMillis == null ? 5000 : Math.max(250, readTimeoutMillis);
        }
    }

    public record Applicant(Boolean enabled, String jwtSecret, Long tokenMinutes, Integer maximumFailedAttempts,
                            Long lockMinutes, List<String> requiredDocumentTypes,
                            Boolean allowReapplicationAfterWithdrawal) {
        public Applicant {
            enabled = enabled != null && enabled;
            jwtSecret = jwtSecret == null ? "" : jwtSecret.trim();
            tokenMinutes = tokenMinutes == null ? 30L : Math.max(5L, tokenMinutes);
            maximumFailedAttempts = maximumFailedAttempts == null ? 5 : Math.max(1, maximumFailedAttempts);
            lockMinutes = lockMinutes == null ? 15L : Math.max(1L, lockMinutes);
            requiredDocumentTypes = requiredDocumentTypes == null ? List.of("PDS")
                    : requiredDocumentTypes.stream().filter(value -> value != null && !value.isBlank())
                    .map(String::trim).distinct().toList();
            allowReapplicationAfterWithdrawal = allowReapplicationAfterWithdrawal != null
                    && allowReapplicationAfterWithdrawal;
        }
    }

    public record Storage(Boolean enabled, String provider, String localRoot, Long maximumBytes,
                          List<String> allowedMediaTypes, String s3Bucket, String s3Region,
                          String s3Endpoint) {
        public Storage {
            enabled = enabled != null && enabled;
            provider = provider == null || provider.isBlank() ? "local" : provider.trim().toLowerCase();
            localRoot = localRoot == null ? "" : localRoot.trim();
            maximumBytes = maximumBytes == null ? 10_485_760L : Math.max(1L, maximumBytes);
            allowedMediaTypes = allowedMediaTypes == null ? List.of("application/pdf", "image/jpeg", "image/png")
                    : allowedMediaTypes.stream().filter(v -> v != null && !v.isBlank()).map(String::trim).toList();
            s3Bucket = s3Bucket == null ? "" : s3Bucket.trim();
            s3Region = s3Region == null || s3Region.isBlank() ? "ap-southeast-1" : s3Region.trim();
            s3Endpoint = s3Endpoint == null ? "" : s3Endpoint.trim();
        }
    }
}
