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
    void apiErrorsExposeTheSupportCorrelationIdentifier() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        Map<?, ?> apiError = (Map<?, ?>) schemas.get("ApiError");
        assertThat(((List<?>) apiError.get("required")).stream().map(Object::toString).toList())
                .contains("correlationId");
        assertThat(((Map<?, ?>) apiError.get("properties")).keySet().stream()
                .map(Object::toString).toList()).contains("correlationId");
    }

    @Test
    void phaseTenAContractExposesFrameworkDefinitionsWithoutAssessmentOrOfficialDetermination() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/evidence/frameworks", "/evidence/frameworks/effective",
                "/evidence/frameworks/{id}", "/evidence/frameworks/{id}/content",
                "/evidence/frameworks/{id}/revisions", "/evidence/frameworks/{id}/publish",
                "/evidence/frameworks/{id}/retire");
        assertThat(paths.stream().filter(path->path.startsWith("/evidence/")).toList())
                .noneMatch(path->path.contains("assessment")||path.contains("readiness")||
                        path.contains("score")||path.contains("determination")||path.contains("submission"));
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString)).contains(
                "EvidenceFrameworkStatus","EvidenceRequirementType","EvidenceFrameworkInput",
                "EvidenceFrameworkContentInput","EvidenceFramework");
    }

    @Test
    void phaseNineBContractExposesConsentEligibilityAndEvidenceWithoutReadinessRankingOrRspAction() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/succession/intake/cycles","/succession/intake/cycles/{id}/risk-reviews",
                "/succession/intake/candidates","/succession/intake/candidates/{id}/notice",
                "/succession/intake/candidates/{id}/consent","/succession/intake/candidates/{id}/submit",
                "/succession/intake/candidates/{id}/eligibility","/succession/intake/candidates/{id}/withdraw",
                "/succession/intake/candidates/{id}/evidence/{evidenceId}/content");
        assertThat(paths.stream().filter(p->p.startsWith("/succession/intake")).toList())
                .noneMatch(p->p.contains("readiness")||p.contains("ranking")||p.contains("development")||p.contains("rsp")||p.contains("appointment"));
    }
    @Test
    void phaseNineCPointOneContractExposesConflictControlledReadinessWithoutRankingOrRspAction() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/succession/readiness","/succession/readiness/{id}",
                "/succession/readiness/candidates/{candidateId}",
                "/succession/readiness/{id}/assignments/{assignmentId}/conflict",
                "/succession/readiness/{id}/assignments/{assignmentId}/replacement",
                "/succession/readiness/{id}/findings","/succession/readiness/{id}/recommendation",
                "/succession/readiness/{id}/decision");
        assertThat(paths.stream().filter(p->p.startsWith("/succession/readiness")).toList())
                .noneMatch(p->p.contains("ranking")||p.contains("development")||p.contains("rsp")||p.contains("appointment"));
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString)).contains("SuccessionReadinessAssessmentStatus",
                "SuccessionReadinessFindingOutcome","SuccessionPoolDecisionOutcome",
                "SuccessionReadinessGapItem","SuccessionReadinessAssessment");
    }
    @Test void phaseNineCPointTwoContractExposesDevelopmentAndMembershipLineageWithoutAutomation()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/succession/development","/succession/development/{id}","/succession/development/decisions/{decisionId}","/succession/development/{id}/sources","/succession/development/{id}/milestones","/succession/development/{id}/milestones/{milestoneId}/status","/succession/development/{id}/activation","/succession/development/{id}/acknowledgment","/succession/development/{id}/reviews","/succession/development/{id}/completion","/succession/development/{id}/membership-events");assertThat(paths.stream().filter(x->x.startsWith("/succession/development")).toList()).noneMatch(x->x.contains("enroll")||x.contains("rsp")||x.contains("promotion")||x.contains("appointment"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("SuccessionDevelopmentPlanStatus","SuccessionDevelopmentSourceType","SuccessionDevelopmentMilestoneStatus","SuccessionDevelopmentReviewRecommendation","SuccessionPoolMembershipEventType","SuccessionPoolMembershipStatus","SuccessionDevelopmentPlan","SuccessionPoolMembershipEvent");}
    @Test void phaseNineDContractExposesContinuityAndReferralWithoutMeritSelectionAutomation()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/succession/continuity/plans","/succession/continuity/plans/{id}","/succession/continuity/risk-reviews/{riskId}/plans","/succession/continuity/plans/{id}/submission","/succession/continuity/plans/{id}/approval","/succession/continuity/plans/{id}/finalization","/succession/continuity/referrals","/succession/continuity/referrals/{id}","/succession/continuity/plans/{planId}/referrals","/succession/continuity/referrals/{id}/vacancy-link","/succession/continuity/referrals/{id}/authorization","/succession/continuity/referrals/{id}/dispatch","/succession/continuity/referrals/{id}/receipt","/succession/continuity/referrals/{id}/status","/succession/continuity/referrals/{id}/withdrawal");assertThat(paths.stream().filter(x->x.startsWith("/succession/continuity")).toList()).noneMatch(x->x.contains("application")||x.contains("shortlist")||x.contains("selection")||x.contains("appointment")||x.contains("promotion"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("SuccessionContinuityPlanStatus","SuccessionBenchCoverageStatus","SuccessionRspReferralStatus","SuccessionContinuityPlan","SuccessionBenchCoverageEntry","SuccessionRspReferralPacket","SuccessionRspReferralEvent");}
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
        assertThat(pathKeys.stream().filter(path -> path.startsWith("/ld-referrals")).toList())
                .noneMatch(path -> path.contains("idp") || path.contains("training") || path.contains("enrollment"));
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
        assertThat(primePaths).noneMatch(path -> path.contains("shortlist"));
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
        assertThat(paths).noneMatch(path -> path.contains("shortlist"));
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
        assertThat(paths).noneMatch(path -> path.contains("shortlist"));
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
        assertThat(paths).noneMatch(path -> path.contains("shortlist") || path.contains("onboarding"));
        Map<?, ?> schemas = (Map<?, ?>) ((Map<?, ?>) primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString).toList()).contains(
                "ScreeningCaseStatus", "ScreeningOutcome", "ScreeningFindingResult",
                "ScreeningAssignmentRole", "ScreeningEvidenceType", "OpenScreeningCaseRequest",
                "SaveScreeningFindingRequest", "SubmitScreeningRecommendationRequest", "ScreeningCase");
    }

    @Test
    void phaseFiveDPointOneContractExposesGovernedFoundationWithoutExecutionOrSelection() throws IOException {
        Map<String, Object> primeHr = yaml("primehr-v1.yaml");
        List<String> paths = ((Map<?, ?>) primeHr.get("paths")).keySet().stream()
                .map(Object::toString).toList();
        assertThat(paths).contains("/rsp/evaluation-policies", "/rsp/evaluation-policies/{policyId}",
                "/rsp/evaluation-policies/{policyId}/publish",
                "/rsp/evaluation-policies/{policyId}/successors",
                "/rsp/vacancy-publications/{publicationId}/evaluation-policy",
                "/committees", "/committees/{committeeId}", "/committees/{committeeId}/publish",
                "/committees/{committeeId}/successors", "/rsp/evaluation-proceedings",
                "/rsp/evaluation-proceedings/{proceedingId}",
                "/rsp/evaluation-proceedings/{proceedingId}/candidates",
                "/rsp/evaluation-proceedings/{proceedingId}/open",
                "/rsp/evaluation-proceedings/{proceedingId}/cancel");
        assertThat(paths).noneMatch(path -> path.contains("onboarding") || path.contains("employee-creation"));
    }

    @Test
    void phaseFiveDPointTwoContractExposesExecutionAndDeliberationWithoutSelectionOrAppointment() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/rsp/evaluation/proceedings/{proceedingId}/sessions",
                "/rsp/evaluation/proceedings/{proceedingId}",
                "/rsp/evaluation/sessions/{sessionId}/candidates/{candidateId}/attendance",
                "/rsp/evaluation/proceedings/{proceedingId}/assignments",
                "/rsp/evaluation/proceedings/{proceedingId}/conflicts",
                "/rsp/evaluation/proceedings/{proceedingId}/stage-results",
                "/rsp/evaluation/proceedings/{proceedingId}/panel-ratings",
                "/rsp/evaluation/proceedings/{proceedingId}/reference-checks",
                "/rsp/evaluation/proceedings/{proceedingId}/comparative-evaluations",
                "/rsp/evaluation/proceedings/{proceedingId}/meetings",
                "/rsp/evaluation/meetings/{meetingId}/attendance",
                "/rsp/evaluation/meetings/{meetingId}/resolutions",
                "/rsp/evaluation/proceedings/{proceedingId}/evidence",
                "/applicant/v1/me/applications/{applicationId}/evaluation");
        assertThat(paths).noneMatch(path->path.contains("employee-creation")||path.contains("onboarding"));
    }

    @Test
    void phaseFiveEPointOneContractExposesHumanSelectionAndSafeOffer() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/rsp/selection-cases","/rsp/selection-cases/{selectionId}",
                "/rsp/selection-cases/{selectionId}/decision","/rsp/selection-cases/{selectionId}/submit",
                "/rsp/selection-cases/{selectionId}/return","/rsp/selection-cases/{selectionId}/approve",
                "/rsp/selection-cases/{selectionId}/finalize","/rsp/selection-cases/{selectionId}/cancel",
                "/rsp/selection-cases/{selectionId}/successors",
                "/applicant/v1/me/applications/{applicationId}/selection",
                "/applicant/v1/me/applications/{applicationId}/selection/response");
        assertThat(paths).noneMatch(path->path.contains("onboarding"));
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString)).contains("SelectionOutcome",
                "SelectionCaseStatus","OfferResponseStatus","CreateSelectionRequest",
                "DecideSelectionRequest","ApplicantOfferResponseRequest");
    }

    @Test
    void phaseFiveEPointTwoContractsExposeDurableHandoffAndReceiptOnly() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");Map<String,Object> humanResource=yaml("humanresource-primehr-integration-v1.yaml");
        List<String> primePaths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(primePaths).contains("/rsp/selection-cases/{selectionId}/appointment-handoffs",
                "/rsp/appointment-handoffs/{handoffId}","/rsp/appointment-handoffs/{handoffId}/submit",
                "/rsp/appointment-handoffs/{handoffId}/retry","/rsp/appointment-handoffs/{handoffId}/reconcile");
        assertThat(primePaths).noneMatch(path->path.contains("onboarding")||path.contains("employee-creation"));
        Map<?,?> hrPaths=(Map<?,?>)humanResource.get("paths");assertThat(hrPaths.keySet().stream().map(Object::toString)).contains(
                "/api/integration/v1/primehr/appointment-handoffs",
                "/api/integration/v1/primehr/appointment-handoffs/{handoffId}");
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)humanResource.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString)).contains("AppointmentHandoffRequest","AppointmentHandoffReceipt");
    }

    @Test
    void phaseFiveFContractsExposeFormalReportsAndControlledProcessReporting() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains(
                "/rsp/reports/comparative-evaluations/{proceedingId}.pdf",
                "/rsp/reports/selections/{selectionId}.pdf",
                "/rsp/reports/evidence-index/{selectionId}.pdf");
        assertThat(paths).contains("/rsp/reports/register","/rsp/reports/register.pdf",
                "/rsp/reports/process-analytics","/rsp/reports/process-analytics.pdf");
        assertThat(paths).noneMatch(path -> path.contains("demographic") || path.contains("ranking-analytics"));
    }

    @Test
    void phaseSixAPointOneContractExposesPolicyCycleCalendarWithoutCommitmentsOrRatings() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");
        List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/performance-management/policies","/performance-management/policies/effective",
                "/performance-management/policies/{policyId}","/performance-management/policies/{policyId}/publish",
                "/performance-management/policies/{policyId}/revisions","/performance-management/policies/{policyId}/retire",
                "/performance-management/cycles","/performance-management/cycles/{cycleId}",
                "/performance-management/cycles/{cycleId}/milestones","/performance-management/cycles/{cycleId}/open",
                "/performance-management/cycles/{cycleId}/close","/performance-management/cycles/{cycleId}/cancel");
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)primeHr.get("components")).get("schemas");
        assertThat(schemas.keySet().stream().map(Object::toString)).contains("PerformancePolicyFrequency","PerformancePolicyStatus","PerformancePolicyInput","PerformancePolicyResponse","PerformanceCycleStatus","PerformanceMilestoneType","PerformanceCycleInput","PerformanceMilestoneResponse","PerformanceCycleResponse","PerformanceCyclePage");
    }

    @Test
    void phaseSixAPointTwoContractExposesPmtGovernanceWithoutCalibration() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/performance-management/pmts","/performance-management/pmts/{pmtId}","/performance-management/pmts/{pmtId}/members","/performance-management/pmts/{pmtId}/activate","/performance-management/pmts/{pmtId}/deactivate");
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)primeHr.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("PerformanceManagementTeamStatus","PerformanceManagementTeamRole","PerformanceManagementTeamInput","PerformanceManagementTeamMembersInput","PerformanceManagementTeamTransition","PerformanceManagementTeamMemberResponse","PerformanceManagementTeamResponse","PerformanceManagementTeamPage");
    }

    @Test
    void phaseSixBPointOneContractExposesClosedRatingScaleDefinitions() throws IOException {
        Map<String,Object> primeHr=yaml("primehr-v1.yaml");List<String> paths=((Map<?,?>)primeHr.get("paths")).keySet().stream().map(Object::toString).toList();
        assertThat(paths).contains("/performance-management/rating-scales","/performance-management/rating-scales/{versionId}","/performance-management/rating-scales/{versionId}/bands","/performance-management/rating-scales/{versionId}/revisions","/performance-management/rating-scales/{versionId}/publish","/performance-management/rating-scales/{versionId}/retire","/performance-management/rating-scales/{versionId}/preview");
        Map<?,?> schemas=(Map<?,?>)((Map<?,?>)primeHr.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("PerformanceMeasureType","PerformanceMeasureDirection","PerformanceDimensionScoreSource","PerformanceRoundingMode","PerformanceRatingScaleStatus","PerformanceRatingScaleInput","PerformanceRatingBandInput","PerformanceRatingBandsInput","PerformanceRatingScalePublish","PerformanceRatingPreviewInput","PerformanceRatingPreview");
    }
    @Test void phaseSixBPointTwoContractExposesSuccessIndicators()throws IOException{Map<String,Object> p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/success-indicators","/performance-management/success-indicators/{versionId}","/performance-management/success-indicators/{versionId}/dimensions","/performance-management/success-indicators/{versionId}/revisions","/performance-management/success-indicators/{versionId}/publish","/performance-management/success-indicators/{versionId}/retire","/performance-management/success-indicators/{versionId}/preview");}
    @Test void phaseSixBPointThreeContractExposesTemplatesWithoutRatings()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/templates","/performance-management/templates/{versionId}","/performance-management/templates/{versionId}/structure","/performance-management/templates/{versionId}/revisions","/performance-management/templates/{versionId}/publish","/performance-management/templates/{versionId}/retire","/performance-management/templates/{versionId}/readiness","/performance-management/templates/{versionId}/preview");}
    @Test void phaseSixCPointOneContractsExposeObjectivesAssignmentsAndAuthoritativeSourcesOnly()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml"),a=yaml("administrative-primehr-integration-v1.yaml"),h=yaml("humanresource-primehr-integration-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/objectives","/performance-management/objectives/{versionId}","/performance-management/objectives/{versionId}/revisions","/performance-management/objectives/{versionId}/publish","/performance-management/objectives/{versionId}/retire","/performance-management/objectives/{versionId}/readiness","/performance-management/plan-assignments","/performance-management/plan-assignments/{assignmentId}","/performance-management/plan-assignments/{assignmentId}/activate","/performance-management/plan-assignments/{assignmentId}/retire","/performance-management/plan-assignments/{assignmentId}/readiness");assertThat(((Map<?,?>)a.get("paths")).keySet().stream().map(Object::toString)).contains("/api/integration/v1/primehr/performance/organization-targets","/api/integration/v1/primehr/performance/organization-targets/{type}/{id}","/api/integration/v1/primehr/performance/personnel-membership/{employeeId}","/api/integration/v1/primehr/performance/approval-routes");assertThat(((Map<?,?>)h.get("paths")).keySet().stream().map(Object::toString)).contains("/api/integration/v1/primehr/performance-participants","/api/integration/v1/primehr/performance-participants/{employeeId}","/api/integration/v1/primehr/performance-participants/by-employee-no/{employeeNo}");}
    @Test void phaseSixCPointTwoContractRetainsCommitmentComposition()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/commitments","/performance-management/commitments/from-assignment/{assignmentId}","/performance-management/commitments/{versionId}","/performance-management/commitments/{versionId}/targets","/performance-management/commitments/{versionId}/cascades","/performance-management/commitments/{versionId}/readiness");}
    @Test void phaseSixDPointOneContractExposesMonitoringFoundation()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/commitments/{versionId}/monitoring-case","/performance-management/monitoring-cases","/performance-management/monitoring-cases/mine","/performance-management/monitoring-cases/{caseId}/updates","/performance-management/monitoring-updates/{updateId}/submit","/performance-management/monitoring-updates/{updateId}/accept","/performance-management/monitoring-updates/{updateId}/return","/performance-management/monitoring-updates/{updateId}/corrections","/performance-management/monitoring-updates/{updateId}/evidence","/performance-management/monitoring-evidence/{evidenceId}/content","/performance-management/monitoring-evidence/{evidenceId}/void","/performance-management/monitoring-cases/{caseId}/accomplishments/submit","/performance-management/monitoring-cases/{caseId}/accomplishments/return");}
    @Test void phaseSixDPointTwoContractRetainsCoachingBehavior()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/monitoring-cases/{caseId}/coaching-sessions","/performance-management/coaching-sessions/{sessionId}","/performance-management/coaching-sessions/{sessionId}/issue","/performance-management/coaching-sessions/{sessionId}/acknowledge","/performance-management/coaching-sessions/{sessionId}/corrections","/performance-management/coaching-sessions/{sessionId}/void","/performance-management/coaching-sessions/{sessionId}/action-items","/performance-management/coaching-action-items/{actionItemId}","/performance-management/coaching-action-items/{actionItemId}/verify","/performance-management/coaching-action-items/{actionItemId}/reopen","/performance-management/monitoring-cases/{caseId}/mid-cycle-review","/performance-management/mid-cycle-reviews/{reviewId}","/performance-management/mid-cycle-reviews/{reviewId}/submit","/performance-management/mid-cycle-reviews/{reviewId}/acknowledge","/performance-management/mid-cycle-reviews/{reviewId}/close-without-acknowledgment");}
    @Test void phaseSixEPointOneContractRetainsRatingFoundation()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/monitoring-cases/{caseId}/rating-case","/performance-management/rating-cases","/performance-management/rating-cases/mine","/performance-management/rating-cases/{caseId}","/performance-management/rating-cases/{caseId}/assessments","/performance-management/rating-assessments/{assessmentId}","/performance-management/rating-assessments/{assessmentId}/corrections","/performance-management/rating-assessments/{assessmentId}/submit");}
    @Test void phaseSixEPointTwoContractExposesCalibrationAndFinalization()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/rating-cases/{caseId}/calibration","/performance-management/calibrations/{calibrationId}","/performance-management/calibrations/{calibrationId}/proposal","/performance-management/calibrations/{calibrationId}/decisions","/performance-management/rating-cases/{caseId}/finalize");}
    @Test void phaseSixEPointThreeContractExposesReceiptsAppealsAndInterventions()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/final-ratings","/performance-management/final-ratings/{finalRatingId}/acknowledgment","/performance-management/final-ratings/{finalRatingId}/close-without-acknowledgment","/performance-management/final-ratings/{finalRatingId}/appeals","/performance-management/appeals","/performance-management/appeals/{appealId}/submit","/performance-management/appeals/{appealId}/decision","/performance-management/appeals/{appealId}/evidence","/performance-management/intervention-rules","/performance-management/interventions","/performance-management/interventions/{interventionId}/issue","/performance-management/interventions/{interventionId}/acknowledge","/performance-management/interventions/{interventionId}/checkpoints");}
    @Test void phaseSixEPointFourContractExposesPermissionSafeWorkQueues()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/calibrations","/performance-management/final-ratings/mine","/performance-management/appeals/mine","/performance-management/interventions/mine");}
    @Test void phaseSixFPointTwoContractExposesFormalPerformancePdfs()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml"),a=yaml("administrative-primehr-integration-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/reports/commitments/{id}.pdf","/performance-management/reports/final-ratings/{id}.pdf","/performance-management/reports/monitoring/{id}.pdf","/performance-management/reports/coaching/{id}.pdf","/performance-management/reports/development-plans/{id}.pdf","/performance-management/reports/interventions/{id}.pdf");assertThat(((Map<?,?>)a.get("paths")).keySet().stream().map(Object::toString)).contains("/api/integration/v1/primehr/report-sources/branding","/api/integration/v1/primehr/report-sources/positions/{plantillaId}");}
    @Test void phaseSixFPointThreeContractExposesPrivacyControlledReportingAndReadOnlyOutcomeFeed()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/performance-management/reports/register","/performance-management/reports/analytics","/integration/v1/performance-outcomes");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("PerformanceRegisterPage","PerformanceRegisterRow","PerformanceAnalytics","PerformanceAggregateMetric","PerformanceAggregateBreakdown","PerformanceOutcomePage","PerformanceOutcome");Map<?,?>outcome=(Map<?,?>)schemas.get("PerformanceOutcome");List<String>properties=((Map<?,?>)outcome.get("properties")).keySet().stream().map(Object::toString).map(String::toLowerCase).toList();assertThat(properties).noneMatch(name->name.contains("note")||name.contains("reason")||name.contains("narrative")||name.contains("response")||name.contains("support")||name.contains("objective"));}
    @Test void phaseSevenAPointOneContractRetainsLdFoundation()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/policies","/learning-development/policies/effective","/learning-development/policies/{versionId}","/learning-development/policies/{versionId}/revisions","/learning-development/policies/{versionId}/publish","/learning-development/policies/{versionId}/retire","/learning-development/interventions","/learning-development/interventions/{versionId}","/learning-development/providers","/learning-development/resource-persons","/learning-development/evaluation-frameworks","/learning-development/evaluation-frameworks/{versionId}/levels");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdDefinitionStatus","LdInterventionCategory","LdModality","LdPolicyInput","LdPolicyResponse","LdInterventionInput","LdProvider","LdResourcePerson","LdEvaluationLevel","LdEvaluationFramework");}
    @Test void phaseSevenAPointTwoContractExposesNeedsReferralDispositionAndIdp()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/needs","/learning-development/needs/{id}","/learning-development/needs/{id}/cancel","/learning-development/referrals/{id}/disposition","/learning-development/individual-plans","/learning-development/individual-plans/{id}","/learning-development/individual-plans/{id}/items","/learning-development/individual-plans/{id}/items/{itemId}","/learning-development/individual-plans/{id}/{transition}","/learning-development/individual-plans/{id}/amendments");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdLearningNeedSource","LdLearningNeedStatus","LdLearningNeedInput","LdLearningNeed","LdReferralDispositionDecision","LdReferralDispositionInput","LdIdpStatus","LdIdpCreate","LdIdpGoalUpdate","LdIdpTransition","LdIdpItemInput","LdIdpItemUpdate","LdIdpItem","LdIdpRouteStep","LdIndividualDevelopmentPlan");}
    @Test void phaseSevenBContractExposesPlansProgramsAndScheduledOfferings()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/plans","/learning-development/plans/{id}","/learning-development/plans/{id}/sources","/learning-development/plans/{id}/sources/{sourceId}","/learning-development/plans/{id}/{transition}","/learning-development/plans/{id}/revisions","/learning-development/programs","/learning-development/programs/{id}","/learning-development/programs/{id}/sources","/learning-development/programs/{id}/sources/{sourceId}","/learning-development/programs/{id}/{transition}","/learning-development/programs/{id}/revisions","/learning-development/offerings","/learning-development/offerings/{id}","/learning-development/offerings/{id}/sessions","/learning-development/offerings/{id}/resources","/learning-development/offerings/{id}/{transition}","/learning-development/offerings/{id}/revisions");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdConsolidatedPlanStatus","LdConsolidatedPlan","LdConsolidatedPlanSource","LdProgramStatus","LdProgram","LdOfferingStatus","LdOffering");}
    @Test void phaseSevenCContractExposesNominationsEnrollmentsAndApprovedTrainingOrderSources()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/nominations","/learning-development/nominations/{id}","/learning-development/nominations/{id}/{transition}","/learning-development/enrollments","/learning-development/enrollments/{id}","/learning-development/enrollments/{id}/{transition}","/learning-development/enrollments/{id}/substitution","/learning-development/offerings/{id}/training-order-source");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdNominationSourceType","LdNominationStatus","LdNominationCreate","LdNomination","LdEnrollmentStatus","LdEnrollment","LdEnrollmentSubstitution","LdTrainingOrderInput","LdTrainingOrderSource");}
    @Test void phaseSevenDPointOneContractExposesAttendanceOutputsEvidenceAndVerifiedCompletionOnly()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/completion-cases","/learning-development/completion-cases/mine","/learning-development/completion-cases/{id}","/learning-development/completion-cases/{id}/mine","/learning-development/enrollments/{id}/completion-case","/learning-development/completion-cases/{id}/attendance/{sessionId}","/learning-development/completion-cases/{id}/outputs","/learning-development/completion-cases/{id}/outputs/{outputId}/{action}","/learning-development/completion-cases/{id}/evidence","/learning-development/completion-cases/{id}/evidence/mine","/learning-development/completion-cases/{id}/evidence/{evidenceId}/content","/learning-development/completion-cases/{id}/evidence/{evidenceId}/content/mine","/learning-development/completion-cases/{id}/submit","/learning-development/completion-cases/{id}/return","/learning-development/completion-cases/{id}/finalize");assertThat(paths.stream().filter(path->path.startsWith("/learning-development/")).toList()).noneMatch(path->path.contains("evaluation-result")||path.contains("outcome-feed"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdCompletionStatus","LdCompletionDecisionType","LdAttendanceStatus","LdCompletionOutputStatus","LdCompletionEvidenceType","LdCompletionOpen","LdAttendanceInput","LdCompletionOutputInput","LdCompletionOutputDecision","LdCompletionTransition","LdCompletionDecision","LdCompletionEvidenceUpload","LdSessionAttendance","LdCompletionOutput","LdCompletionEvidence","LdCompletionCase");}
    @Test void phaseSevenDPointTwoContractExposesVersionedEvaluationFollowThroughAndControlledCompletionFeed()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/evaluation/tasks","/learning-development/evaluation/tasks/mine","/learning-development/evaluation/tasks/assigned","/learning-development/evaluation/completion-cases/{id}/initialize","/learning-development/evaluation/tasks/{id}/results","/learning-development/evaluation/tasks/{id}/results/{resultId}/decision","/learning-development/evaluation/action-plans","/learning-development/evaluation/action-plans/mine","/learning-development/evaluation/action-plans/assigned","/learning-development/evaluation/completion-cases/{id}/action-plan","/learning-development/evaluation/action-plans/{id}/follow-up","/learning-development/evaluation/effectiveness","/learning-development/evaluation/completion-cases/{id}/hrm-review-packet","/integration/v1/learning-completions");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdEvaluationInitialize","LdEvaluationResultInput","LdEvaluationResult","LdEvaluationTask","LdLearningActionPlanInput","LdHrmReviewPacket","LdCompletionOutcome");Map<?,?>outcome=(Map<?,?>)schemas.get("LdCompletionOutcome");List<String>properties=((Map<?,?>)outcome.get("properties")).keySet().stream().map(Object::toString).map(String::toLowerCase).toList();assertThat(properties).noneMatch(name->name.contains("narrative")||name.contains("reason")||name.contains("evidence")||name.contains("objective")||name.contains("score"));}

    @Test void phaseSevenEContractExposesFormalReportsAndPrivacyMetadata()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/learning-development/reports/individual-plans/{id}.pdf","/learning-development/reports/training-orders/{offeringId}.pdf","/learning-development/reports/completion-cases/{id}.pdf","/learning-development/reports/evaluation-summaries/{completionId}.pdf");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("LdEffectiveness","LdEffectivenessRow");Map<?,?>effectiveness=(Map<?,?>)schemas.get("LdEffectiveness");assertThat(((Map<?,?>)effectiveness.get("properties")).keySet().stream().map(Object::toString)).contains("minimumCohort","suppressedGroups","rows");}

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
    @Test void phaseEightAPointOneContractExposesPraiseGovernanceCategoriesAndCommittee()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/rewards-recognition/policies","/rewards-recognition/policies/effective","/rewards-recognition/policies/{id}","/rewards-recognition/policies/{id}/revisions","/rewards-recognition/policies/{id}/publish","/rewards-recognition/categories","/rewards-recognition/categories/{id}/criteria","/rewards-recognition/committee-charters","/rewards-recognition/committee-charters/{id}/roster");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("RrDefinitionStatus","RrNomineeType","RrIncentiveType","RrQuorumType","RrCommitteeRole","RrPolicyInput","RrPolicyResponse","RrAwardCriterion","RrAwardCategory","RrCommitteeMember","RrCommitteeCharter");}
    @Test void phaseEightAPointTwoContractExposesCyclesNominationsEvidenceAndExactSources()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/rewards-recognition/cycles","/rewards-recognition/cycles/{id}","/rewards-recognition/cycles/{id}/{action}","/rewards-recognition/nominations","/rewards-recognition/nominations/{id}","/rewards-recognition/nominations/{id}/submit","/rewards-recognition/nominations/{id}/withdraw","/rewards-recognition/nominations/{id}/evidence","/rewards-recognition/nominations/{id}/evidence/{evidenceId}/content","/rewards-recognition/nominations/{id}/evidence/{evidenceId}");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("RrAwardCycleStatus","RrNominationStatus","RrNominationSourceType","RrEvidenceVisibility","RrAwardCycleInput","RrAwardCycle","RrNominationInput","RrNominationSourceInput","RrNominationEvidence","RrNomination");}
    @Test void phaseEightBContractExposesEligibilityEvaluationDecisionAndReconsideration()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/rewards-recognition/evaluation-cases","/rewards-recognition/evaluation-cases/{id}","/rewards-recognition/nominations/{id}/evaluation","/rewards-recognition/evaluation-cases/{id}/eligibility","/rewards-recognition/evaluation-cases/{id}/assignments/{assignmentId}/conflict","/rewards-recognition/evaluation-cases/{id}/assignments/{assignmentId}/replacement","/rewards-recognition/evaluation-cases/{id}/assignments/{assignmentId}/score","/rewards-recognition/evaluation-cases/{id}/recommendation","/rewards-recognition/evaluation-cases/{id}/decision","/rewards-recognition/evaluation-cases/{id}/issues","/rewards-recognition/evaluation-cases/{id}/issues/{issueId}/resolve");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("RrEvaluationStatus","RrEligibilityOutcome","RrEvaluatorStatus","RrDecisionOutcome","RrEvaluationCase");}
    @Test void phaseEightCContractExposesImmutableAwardsFulfillmentHapAndAnnualRegister()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/rewards-recognition/awards","/rewards-recognition/awards/{id}","/rewards-recognition/evaluation-cases/{id}/award","/rewards-recognition/awards/{id}/corrections","/rewards-recognition/awards/{id}/revocations","/rewards-recognition/awards/{id}/incentives","/rewards-recognition/awards/{id}/incentives/{authorizationId}/fulfillment","/rewards-recognition/awards/{id}/hap-events","/rewards-recognition/reports/annual-register");assertThat(paths.stream().filter(path->path.startsWith("/rewards-recognition/")).toList()).noneMatch(path->path.contains("payroll")||path.contains("promotion")||path.contains("appointment"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("RrAwardRecordType","RrFulfillmentStatus","RrHapStatus","RrIncentiveAuthorization","RrAwardRecord","RrAnnualRegister");}
    @Test void phaseNineAPointOneContractExposesSuccessionGovernanceReadinessCommitteeAndConflicts()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/succession/governance-policies","/succession/governance-policies/effective","/succession/governance-policies/{id}/revisions","/succession/readiness-scales","/succession/readiness-scales/{id}/levels","/succession/committee-charters","/succession/committee-charters/{id}/roster","/succession/committee-charters/{id}/conflicts","/succession/committee-conflicts/{id}/resolve");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("SuccessionDefinitionStatus","SuccessionPolicy","SuccessionReadinessLevel","SuccessionReadinessScale","SuccessionCommitteeMember","SuccessionCommitteeCharter","SuccessionCommitteeConflict");}
    @Test void phaseNineAPointTwoContractExposesCareerPathsAndHumanApprovedCriticalPositionsWithoutReadinessAssessment()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/succession/career-families","/succession/career-families/{id}/pathways","/succession/career-families/{id}/publish","/succession/criticality-frameworks","/succession/criticality-frameworks/{id}/criteria","/succession/critical-positions","/succession/critical-positions/{id}/submit","/succession/critical-positions/{id}/approve","/succession/critical-positions/{id}/reject");assertThat(paths.stream().filter(x->x.startsWith("/succession/")&&!x.startsWith("/succession/intake/")&&!x.startsWith("/succession/readiness/")).toList()).noneMatch(x->x.contains("candidate")||x.contains("pool")||x.contains("readiness-assessment"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("SuccessionCareerPath","SuccessionCareerFamily","SuccessionCriticalityCriterion","SuccessionCriticalityFramework","SuccessionCriticalityFinding","SuccessionCriticalPositionDesignation");}
    @Test void phaseNineEContractExposesBeanReportsAndPrivacyAnalyticsWithoutRspSelectionOrCoreHrMutation()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/succession/reports/readiness/{id}.pdf","/succession/reports/development/{id}.pdf","/succession/reports/continuity/{id}.pdf","/succession/reports/referrals/{id}.pdf","/succession/reports/coverage-register.pdf","/succession/reports/analytics");List<String>reports=paths.stream().filter(path->path.startsWith("/succession/reports/")).map(String::toLowerCase).toList();assertThat(reports).noneMatch(path->path.contains("application")||path.contains("shortlist")||path.contains("selection")||path.contains("appointment")||path.contains("payroll"));}
    @Test void phaseTenBContractExposesHumanEvidenceAssessmentWorkflowWithoutReadinessScoring()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/evidence-assessments/cycles","/evidence-assessments/cycles/{id}","/evidence-assessments/cycles/{id}/{action}","/evidence-assessments/cases","/evidence-assessments/validation/cases","/evidence-assessments/action/cases","/evidence-assessments/cases/{id}","/evidence-assessments/cases/{id}/submit","/evidence-assessments/cases/{id}/withdraw","/evidence-assessments/cases/{id}/evidence/live","/evidence-assessments/cases/{id}/evidence/upload","/evidence-assessments/cases/{id}/evidence/{evidenceId}/reverify","/evidence-assessments/cases/{id}/validator/recuse","/evidence-assessments/cases/{id}/validator/reassign","/evidence-assessments/cases/{id}/findings","/evidence-assessments/cases/{id}/return","/evidence-assessments/cases/{id}/finalize","/evidence-assessments/cases/{id}/gaps","/evidence-assessments/cases/{id}/actions");assertThat(paths.stream().filter(path->path.startsWith("/evidence-assessments/")).toList()).noneMatch(path->path.contains("readiness")||path.contains("score")||path.contains("snapshot")||path.contains("report"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("EvidenceAssessmentCycleStatus","EvidenceAssessmentCaseStatus","EvidenceArtifactKind","EvidenceSourceType","EvidenceClassification","EvidenceVisibility","EvidenceVerificationStatus","EvidenceFindingOutcome","EvidenceGapStatus","EvidenceCorrectiveActionStatus","EvidenceAssessmentCycleInput","EvidenceAssessmentCaseInput","EvidenceAssessmentTransition","EvidenceArtifactMetadata","EvidenceFindingInput","EvidenceAssessmentCase");}
    @Test void phaseTenCContractExposesTransparentReadinessSnapshotsReportsAndPrivacyAnalyticsWithoutOfficialScoring()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/evidence-readiness/cases/{id}/calculation","/evidence-readiness/cases/{id}/snapshots","/evidence-readiness/exceptions","/evidence-readiness/reports/cases/{id}/evidence-index.pdf","/evidence-readiness/reports/snapshots/{id}/internal-assessment.pdf","/evidence-readiness/analytics");List<String>readiness=paths.stream().filter(path->path.startsWith("/evidence-readiness/")).map(String::toLowerCase).toList();assertThat(readiness).noneMatch(path->path.contains("official")||path.contains("governance-level")||path.contains("ranking")||path.contains("prediction")||path.contains("payroll")||path.contains("appointment"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("EvidenceReadinessSignOff","EvidenceRequirementFact","EvidenceReadinessCalculation","EvidenceReadinessSnapshot","EvidenceExceptionView","EvidenceAggregateAnalytics");Map<?,?>calculation=(Map<?,?>)schemas.get("EvidenceReadinessCalculation");List<String>properties=((Map<?,?>)calculation.get("properties")).keySet().stream().map(Object::toString).map(String::toLowerCase).toList();assertThat(properties).contains("completenesspercent","coveragepercent","missinginputs","disclaimer").noneMatch(name->name.contains("officialscore")||name.contains("governancelevel")||name.contains("rank")||name.contains("potential"));}
    @Test void phaseElevenDContractExposesProtectedSanitizedOperationsStatus()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/operations/status","/operations/reconciliation-runs","/operations/retention-eligibility");Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("OperationsStatus","OperationsDependencyStatus");Map<?,?>status=(Map<?,?>)schemas.get("OperationsStatus");assertThat(((Map<?,?>)status.get("properties")).keySet().stream().map(Object::toString).map(String::toLowerCase)).noneMatch(name->name.contains("url")||name.contains("token")||name.contains("employee")||name.contains("filename"));}
    @Test void phaseTwelveCContractExposesAuthenticatedOwnRevalidatedNotificationStateOnly()throws IOException{Map<String,Object>p=yaml("primehr-v1.yaml");assertThat(((Map<?,?>)p.get("info")).get("version")).isEqualTo("12.3.0-phase-12c");List<String>paths=((Map<?,?>)p.get("paths")).keySet().stream().map(Object::toString).toList();assertThat(paths).contains("/notifications/me","/notifications/me/unread-count","/notifications/me/{id}/read","/notifications/me/read-all","/notifications/me/{id}/dismiss","/notifications/me/{id}/target");assertThat(paths.stream().filter(x->x.startsWith("/notifications/")).toList()).noneMatch(x->x.contains("employees")||x.contains("publish")||x.contains("broadcast")||x.contains("email")||x.contains("sms")||x.contains("push"));Map<?,?>schemas=(Map<?,?>)((Map<?,?>)p.get("components")).get("schemas");assertThat(schemas.keySet().stream().map(Object::toString)).contains("Notification","NotificationPage","NotificationUnreadCount","NotificationMarkRequest","NotificationMarkAllResult","NotificationTargetResolution");Map<?,?>notification=(Map<?,?>)schemas.get("Notification");List<String>properties=((Map<?,?>)notification.get("properties")).keySet().stream().map(Object::toString).map(String::toLowerCase).toList();assertThat(properties).noneMatch(x->x.contains("recipient")||x.contains("employee")||x.contains("token")||x.contains("url")||x.contains("html")||x.contains("attachment"));}
}
