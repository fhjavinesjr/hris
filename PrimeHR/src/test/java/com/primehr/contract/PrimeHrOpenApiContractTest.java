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
                .contains(
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
        assertThat(hrPaths.keySet().stream().map(Object::toString).toList()).contains(
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

    @Test
    void phaseFourPointOneContractsExposePriorityAdministrationAndTransparentGapAnalysis() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        Map<?, ?> paths = (Map<?, ?>) primeHr.get("paths");
        List<String> pathKeys = paths.keySet().stream().map(Object::toString).toList();
        assertThat(pathKeys).contains(
                "/admin/gap-priority-schemes", "/admin/gap-priority-schemes/{schemeId}",
                "/admin/gap-priority-schemes/{schemeId}/archive",
                "/admin/gap-priority-schemes/{schemeId}/publish",
                "/admin/gap-priority-schemes/{schemeId}/versions",
                "/admin/gap-priority-schemes/{schemeId}/levels",
                "/admin/gap-priority-schemes/{schemeId}/levels/{levelId}",
                "/admin/gap-priority-schemes/{schemeId}/levels/{levelId}/archive",
                "/admin/gap-priority-schemes/{schemeId}/rules",
                "/admin/gap-priority-schemes/{schemeId}/rules/{ruleId}",
                "/admin/gap-priority-schemes/{schemeId}/rules/{ruleId}/archive",
                "/competency-gaps", "/competency-gaps/{analysisId}",
                "/competency-gaps/{analysisId}/report.pdf",
                "/competency-gaps/employees/{employeeNo}/latest",
                "/competency-gaps/employees/{employeeNo}/history");
        assertThat(((Map<?, ?>) paths.get("/competency-gaps")).keySet().stream().map(Object::toString).toList())
                .contains("get", "post");
        assertThat(((Map<?, ?>) paths.get("/admin/gap-priority-schemes/{schemeId}")).keySet().stream()
                .map(Object::toString).toList()).contains("get", "put");

        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "GapClassification", "NotAssessedReason", "GapPrioritySchemeStatus",
                "CreateGapPrioritySchemeRequest", "GapPriorityLevel", "GapPriorityRule",
                "GenerateCompetencyGapRequest", "CompetencyGapItem", "CompetencyGapAnalysis");
        Map<?, ?> generation = (Map<?, ?>) schemas.get("GenerateCompetencyGapRequest");
        assertThat(((Map<?, ?>) generation.get("properties")).keySet().stream().map(Object::toString).toList())
                .containsExactlyInAnyOrder("employeeId", "expectedHrmSourceFingerprint", "requestKey");
    }

    @Test
    void phaseFourPointTwoContractsExposeManualLdReferralLifecycleOnly() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        Map<?, ?> paths = (Map<?, ?>) primeHr.get("paths");
        List<String> pathKeys = paths.keySet().stream().map(Object::toString).toList();
        assertThat(pathKeys).contains("/ld-referrals", "/ld-referrals/{referralId}",
                "/ld-referrals/{referralId}/items", "/ld-referrals/{referralId}/items/{itemId}/archive",
                "/ld-referrals/{referralId}/submit", "/ld-referrals/{referralId}/archive");
        assertThat(((Map<?, ?>) paths.get("/ld-referrals")).keySet().stream().map(Object::toString).toList())
                .containsExactlyInAnyOrder("get", "post");
        assertThat(((Map<?, ?>) paths.get("/ld-referrals/{referralId}")).keySet().stream()
                .map(Object::toString).toList()).containsExactlyInAnyOrder("get", "put");
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "LdReferralStatus", "CreateLdReferralRequest", "UpdateLdReferralRequest",
                "AddLdReferralItemsRequest", "LdReferralTransitionRequest",
                "LdReferralItemTransitionRequest", "LdReferralItem", "LdReferral");
        assertThat(pathKeys).noneMatch(path -> path.contains("idp") || path.contains("training")
                || path.contains("enrollment"));
    }

    @Test
    void phaseFiveAContractsExposeGovernedPlanningPublicationAndOfficialNoticeWithoutApplicantProcessing() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        Map<String, Object> administrative = yaml("administrative-primehr-integration-v1.yaml");
        Map<String, Object> humanResource = yaml("humanresource-primehr-integration-v1.yaml");
        List<String> primePaths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(primePaths).contains("/rsp/recruitment-plans", "/rsp/recruitment-plans/{planId}",
                "/rsp/recruitment-plans/{planId}/archive",
                "/rsp/recruitment-plans/{planId}/submit",
                "/rsp/recruitment-plans/{planId}/return",
                "/rsp/recruitment-plans/{planId}/approve",
                "/rsp/recruitment-plans/{planId}/vacancies",
                "/rsp/vacancy-requests/{requestId}",
                "/rsp/vacancy-requests/{requestId}/archive",
                "/rsp/vacancy-requests/{requestId}/submit",
                "/rsp/vacancy-requests/{requestId}/return",
                "/rsp/vacancy-requests/{requestId}/authorize",
                "/rsp/vacancy-requests/{requestId}/decline",
                "/rsp/vacancy-requests/{requestId}/cancel",
                "/rsp/vacancy-requests/{requestId}/readiness", "/rsp/vacancy-readiness",
                "/rsp/vacancy-publications", "/rsp/vacancy-publications/{publicationId}",
                "/rsp/vacancy-publications/{publicationId}/notice.pdf",
                "/rsp/vacancy-publications/{publicationId}/submit",
                "/rsp/vacancy-publications/{publicationId}/return",
                "/rsp/vacancy-publications/{publicationId}/approve",
                "/rsp/vacancy-publications/{publicationId}/publish",
                "/rsp/vacancy-publications/{publicationId}/cancel",
                "/rsp/vacancy-publications/{publicationId}/close");
        assertThat(primePaths).noneMatch(path -> path.contains("shortlist") || path.contains("selection"));
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "VacancyType", "RecruitmentPlanStatus", "VacancyRequestStatus",
                "VacancyPublicationStatus", "VacancyVisibility", "CreateRecruitmentPlanRequest",
                "SaveVacancyRequest", "RspTransitionRequest", "VacancyPublicationChannelInput",
                "CreateVacancyPublicationRequest", "UpdateVacancyPublicationRequest");

        assertThat(((Map<?, ?>) administrative.get("paths")).keySet().stream().map(Object::toString))
                .contains("/api/integration/v1/primehr/rsp/position-sources/{plantillaId}");
        assertThat(((Map<?, ?>) humanResource.get("paths")).keySet().stream().map(Object::toString))
                .contains("/api/integration/v1/primehr/plantilla-occupancy/{plantillaId}");

        Map<?, ?> hrSchemas = (Map<?, ?>) ((Map<?, ?>) humanResource.get("components")).get("schemas");
        Map<?, ?> occupancy = (Map<?, ?>) hrSchemas.get("PlantillaOccupancy");
        assertThat(((Map<?, ?>) occupancy.get("properties")).keySet().stream()
                .map(Object::toString).map(String::toLowerCase))
                .noneMatch(name -> name.matches(".*(password|biometric|salary|address|contact|email|pds).*"));
    }

    @Test
    void phaseFiveBPointOneContractExposesOnlyApplicantFoundationAndPublicVacancyReads() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        List<String> paths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(paths).contains(
                "/public/v1/privacy-notices/current",
                "/public/v1/applicant-accounts/register",
                "/public/v1/applicant-sessions",
                "/public/v1/vacancies",
                "/public/v1/vacancies/{publicationId}",
                "/applicant/v1/session",
                "/applicant/v1/me",
                "/applicant/v1/me/profile",
                "/applicant/v1/me/consents",
                "/applicant/v1/me/documents",
                "/applicant/v1/me/documents/{documentId}",
                "/applicant/v1/me/documents/{documentId}/content",
                "/applicant/v1/me/documents/{documentId}/replace");
        assertThat(paths).noneMatch(path -> path.contains("/shortlist"));
    }

    @Test
    void phaseFiveBPointTwoContractExposesApplicationIntakeWithoutScreeningOrSelection() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        List<String> paths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(paths).contains("/applicant/v1/me/applications",
                "/applicant/v1/me/applications/{applicationId}",
                "/applicant/v1/me/applications/{applicationId}/submit",
                "/applicant/v1/me/applications/{applicationId}/withdraw",
                "/applicant/v1/me/applications/{applicationId}/communications",
                "/rsp/applications", "/rsp/applications/{applicationId}",
                "/rsp/applications/{applicationId}/documents/{documentId}/content",
                "/rsp/applications/{applicationId}/communications");
        assertThat(paths).noneMatch(path -> path.contains("shortlist") || path.contains("selection"));
    }

    @Test
    void phaseFiveCPointOneContractExposesPolicyBindingAndAdvisoryEvaluationOnly() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        List<String> paths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(paths).contains("/rsp/screening-policies", "/rsp/screening-policies/{policyId}",
                "/rsp/screening-policies/{policyId}/publish",
                "/rsp/screening-policies/{policyId}/successors",
                "/rsp/screening-policies/{policyId}/preview",
                "/rsp/vacancy-publications/{publicationId}/screening-policy");
        assertThat(paths).noneMatch(path -> path.contains("shortlist")
                || path.contains("interview") || path.contains("selection"));
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "ScreeningCriterionCategory", "ScreeningEvaluationMode", "ScreeningPolicyStatus",
                "ScreeningCriterionInput", "ScreeningReasonCodeInput", "SaveScreeningPolicyRequest",
                "PublishScreeningPolicyRequest", "BindScreeningPolicyRequest", "ScreeningEvaluation");
    }

    @Test
    void phaseFiveCPointTwoContractExposesAssignedScreeningWithoutLaterSelection() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        List<String> paths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(paths).contains("/rsp/screening-cases",
                "/rsp/applications/{applicationId}/screening-cases",
                "/rsp/screening-cases/{caseId}", "/rsp/screening-cases/{caseId}/successors",
                "/rsp/screening-cases/{caseId}/assignments",
                "/rsp/screening-cases/{caseId}/findings/{criterionId}",
                "/rsp/screening-cases/{caseId}/submit", "/rsp/screening-cases/{caseId}/return",
                "/rsp/screening-cases/{caseId}/finalize", "/rsp/screening-cases/{caseId}/override",
                "/rsp/screening-cases/{caseId}/history");
        assertThat(paths).noneMatch(path -> path.contains("shortlist") || path.contains("interview")
                || path.contains("ranking") || path.contains("selection") || path.contains("appointment"));
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "ScreeningCaseStatus", "ScreeningOutcome", "ScreeningFindingResult",
                "ScreeningAssignmentRole", "ScreeningEvidenceType", "OpenScreeningCaseRequest",
                "SaveScreeningFindingRequest", "SubmitScreeningRecommendationRequest", "ScreeningCase");
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
