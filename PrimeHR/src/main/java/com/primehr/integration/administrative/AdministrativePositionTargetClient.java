package com.primehr.integration.administrative;

import com.primehr.positionprofile.domain.PositionTargetType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AdministrativePositionTargetClient {
    private final RestClient restClient;

    public AdministrativePositionTargetClient(
            @Qualifier("administrativeRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public AdministrativePositionTarget get(PositionTargetType type, Long id, String authorizationHeader) {
        try {
            AdministrativePositionTarget target = restClient.get()
                    .uri("/api/integration/v1/primehr/position-targets/{type}/{id}", type, id)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(AdministrativePositionTarget.class);
            validate(target, type, id);
            return target;
        } catch (AccessDeniedException | IllegalArgumentException | PositionTargetDependencyException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PositionTargetDependencyException("Administrative position targets are unavailable", exception);
        }
    }

    private static void validate(AdministrativePositionTarget target, PositionTargetType type, Long id) {
        if (target == null || target.type() != type || !id.equals(target.targetId())
                || target.jobPositionId() == null || blank(target.jobPositionName())
                || blank(target.sourceFingerprint()) || target.fetchedAt() == null) {
            throw new PositionTargetDependencyException(
                    "Administrative returned an invalid position target response", null);
        }
        if (type == PositionTargetType.JOB_POSITION
                && (target.plantillaId() != null || target.plantillaName() != null)) {
            throw new PositionTargetDependencyException(
                    "Administrative returned an invalid Job Position target", null);
        }
        if (type == PositionTargetType.PLANTILLA
                && (target.plantillaId() == null || blank(target.plantillaName()))) {
            throw new PositionTargetDependencyException(
                    "Administrative returned an invalid Plantilla target", null);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
