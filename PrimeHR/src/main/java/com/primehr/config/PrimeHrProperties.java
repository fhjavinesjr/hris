package com.primehr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "primehr")
public record PrimeHrProperties(Security security, Cors cors, Agency agency, Administrative administrative) {

    public PrimeHrProperties {
        security = security == null ? new Security(null, null) : security;
        cors = cors == null ? new Cors(null, null) : cors;
        agency = agency == null ? new Agency(null) : agency;
        administrative = administrative == null ? new Administrative(null, null, null) : administrative;
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
}
