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

    @Test
    void phaseThreePointThreeContractsExposeExecutionValidationAndImmutablePersonProfiles() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        Map<String, Object> humanResource = yaml("humanresource-primehr-integration-v1.yaml");
        List<String> primePaths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(primePaths).contains("/admin/assessment-cycles", "/admin/assessment-cycles/{cycleId}/tools",
                "/admin/assessment-tools/{toolId}/subjects", "/admin/assessment-cases/{caseId}/assessors",
                "/admin/assessment-cases/{caseId}/assessors/{assignmentId}/archive",
                "/admin/assessment-cycles/{cycleId}/open", "/admin/assessment-cycles/{cycleId}/close",
                "/admin/assessment-tools/{toolId}/publish", "/assessments/mine", "/assessments/{caseId}",
                "/assessments/{caseId}/assignments/{assignmentId}/ratings/{competencyVersionId}",
                "/assessments/{caseId}/assignments/{assignmentId}/evidence",
                "/assessments/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}",
                "/assessments/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}/archive",
                "/assessments/{caseId}/assignments/{assignmentId}/submit",
                "/validation/assessment-cases/{caseId}/return");
        assertThat(primePaths).contains("/validation/assessment-cases",
                "/validation/assessment-cases/{caseId}",
                "/validation/assessment-cases/{caseId}/validate",
                "/person-profiles/me", "/person-profiles/me/history",
                "/person-profiles/employees/{employeeNo}",
                "/person-profiles/employees/{employeeNo}/history",
                "/person-profiles/versions/{profileVersionId}");

        Map<?, ?> primeSchemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(primeSchemas.keySet().stream().map(Object::toString).toList()).contains(
                "AssessmentWork", "AssessmentInboxPage", "SaveAssessmentRatingRequest",
                "CreateAssessmentEvidenceRequest", "UpdateAssessmentEvidenceRequest",
                "AssessmentWorkTransitionRequest", "ReturnAssessmentCaseRequest",
                "ReturnAssessmentCaseResponse", "AssessmentValidation", "ValidateAssessmentCaseRequest",
                "FinalAssessmentDecision", "PersonCompetencyProfile", "PersonCompetencyResult");

        Map<?, ?> hrPaths = (Map<?, ?>) humanResource.get("paths");
        assertThat(hrPaths.keySet().stream().map(Object::toString).toList()).containsExactlyInAnyOrder(
                "/api/integration/v1/primehr/assessment-subjects",
                "/api/integration/v1/primehr/assessment-subjects/{employeeId}");
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) humanResource.get("components")).get("schemas");
        Map<?, ?> subject = (Map<?, ?>) schemas.get("AssessmentSubject");
        List<String> properties = ((Map<?, ?>) subject.get("properties")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(properties).containsExactlyInAnyOrder("employeeId", "employeeNo", "displayName", "eligible",
                "appointmentId", "assumptionToDutyDate", "jobPositionId", "plantillaId",
                "sourceFingerprint", "sourceUpdatedAt", "fetchedAt");
        assertThat(properties).noneMatch(name -> name.toLowerCase().matches(
                ".*(password|biometric|salary|address|contact|email).*"));
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
