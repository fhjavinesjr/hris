package com.primehr.contract;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeHrOpenApiContractTest {
    @Test
    void phaseTwoPointTwoContractsExposeLifecycleResolutionComparisonAndAuditEndpoints() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        Map<String, Object> administrative = yaml("administrative-primehr-integration-v1.yaml");

        Map<?, ?> primePaths = (Map<?, ?>) primeHr.get("paths");
        List<String> primePathKeys = primePaths.keySet().stream().map(Object::toString).toList();
        assertThat(primePathKeys).contains(
                "/admin/position-profiles",
                "/admin/position-profiles/{id}",
                "/admin/position-profiles/{id}/requirements",
                "/admin/position-profiles/{id}/requirements/{requirementId}/archive",
                "/admin/position-profiles/{id}/submit",
                "/admin/position-profiles/{id}/return",
                "/admin/position-profiles/{id}/approve",
                "/admin/position-profiles/{id}/audit-events",
                "/admin/position-profiles/resolve",
                "/admin/position-profiles/compare");

        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "SubmitPositionProfileRequest", "ApprovePositionProfileRequest",
                "PositionProfileResolution", "PositionProfileComparison", "AuditEventPage");

        Map<?, ?> administrativePaths = (Map<?, ?>) administrative.get("paths");
        assertThat(administrativePaths.keySet().stream().map(Object::toString).toList())
                .containsExactlyInAnyOrder(
                "/api/integration/v1/primehr/position-targets",
                "/api/integration/v1/primehr/position-targets/{type}/{id}");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> yaml(String filename) throws IOException {
        Path path = List.of(Path.of("contracts", "openapi", filename),
                        Path.of("..", "contracts", "openapi", filename)).stream()
                .filter(Files::isRegularFile).findFirst()
                .orElseThrow(() -> new IOException("OpenAPI contract not found: " + filename));
        Object parsed = new Yaml().load(Files.readString(path));
        assertThat(parsed).isInstanceOf(Map.class);
        return (Map<String, Object>) parsed;
    }
}
