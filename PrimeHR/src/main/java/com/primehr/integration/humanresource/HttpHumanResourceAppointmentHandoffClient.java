package com.primehr.integration.humanresource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.config.PrimeHrProperties;
import com.primehr.shared.web.CorrelationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
class HttpHumanResourceAppointmentHandoffClient implements HumanResourceAppointmentHandoffClient {
    private static final String PATH = "/api/integration/v1/primehr/appointment-handoffs";
    private final RestClient restClient;
    private final String endpoint;
    private final String secret;
    private final String issuer;
    private final String audience;
    private final long tokenSeconds;

    HttpHumanResourceAppointmentHandoffClient(PrimeHrProperties properties,
            @Qualifier("humanResourceHandoffRestClient") RestClient restClient,
            @Value("${primehr.handoff.jwt-secret:}") String secret,
            @Value("${primehr.handoff.issuer:primehr}") String issuer,
            @Value("${primehr.handoff.audience:humanresource}") String audience,
            @Value("${primehr.handoff.token-seconds:60}") long tokenSeconds) {
        this.restClient = restClient;
        this.endpoint = properties.humanResource().baseUrl() + PATH;
        this.secret = secret;
        this.issuer = issuer;
        this.audience = audience;
        this.tokenSeconds = Math.max(15, tokenSeconds);
    }

    @Override
    public DeliveryResult deliver(DeliveryRequest request) {
        try {
            Receipt receipt = restClient.post().uri(PATH)
                    .header(HttpHeaders.AUTHORIZATION, bearer(request.agencyId()))
                    .header("Idempotency-Key", request.handoffId())
                    .header(CorrelationContext.HEADER,
                            CorrelationContext.normalizeOrGenerate(request.correlationId()))
                    .body(request).retrieve().body(Receipt.class);
            return receipt == null
                    ? failure("EMPTY_RESPONSE", null, "HumanResource returned no receipt")
                    : new DeliveryResult("ACKNOWLEDGED", 202, null, receipt);
        } catch (HttpStatusCodeException exception) {
            return httpFailure(exception);
        } catch (RestClientException | IllegalStateException exception) {
            return failure("UNAVAILABLE", null, safeDiagnostic(exception));
        }
    }

    @Override
    public DeliveryResult reconcile(String agencyId, String handoffId, String correlationId) {
        try {
            Receipt receipt = restClient.get().uri(PATH + "/{id}", handoffId)
                    .header(HttpHeaders.AUTHORIZATION, bearer(agencyId))
                    .header(CorrelationContext.HEADER,
                            CorrelationContext.normalizeOrGenerate(correlationId))
                    .retrieve().body(Receipt.class);
            return receipt == null
                    ? failure("NOT_FOUND", 404, "HumanResource receipt was not found")
                    : new DeliveryResult("RECONCILED", 200, null, receipt);
        } catch (HttpStatusCodeException exception) {
            return httpFailure(exception);
        } catch (RestClientException | IllegalStateException exception) {
            return failure("UNAVAILABLE", null, safeDiagnostic(exception));
        }
    }

    private DeliveryResult httpFailure(HttpStatusCodeException exception) {
        int status = exception.getStatusCode().value();
        String category = status == 401 || status == 403 ? "UNAUTHORIZED"
                : status == 409 ? "CONFLICT" : status == 429 || status >= 500
                ? "UNAVAILABLE" : "REJECTED";
        return failure(category, status, "HumanResource rejected the handoff (HTTP " + status + ")");
    }

    private DeliveryResult failure(String category, Integer status, String diagnostic) {
        return new DeliveryResult(category, status, diagnostic, null);
    }

    private String bearer(String agencyId) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("PrimeHR handoff service secret is not configured");
        }
        Instant now = Instant.now();
        String token = JWT.create().withIssuer(issuer).withAudience(audience)
                .withSubject("primehr-service")
                .withClaim("scope", List.of("appointment-handoff.write"))
                .withClaim("agencyId", agencyId).withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(tokenSeconds)))
                .sign(Algorithm.HMAC256(secret));
        return "Bearer " + token;
    }

    private static String safeDiagnostic(Exception exception) {
        String value = exception.getMessage();
        if (value == null || value.isBlank()) value = exception.getClass().getSimpleName();
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    String endpointIdentity() {
        return endpoint;
    }
}
