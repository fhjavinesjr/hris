# ISOFT PRIME-HRM Phase 5C.1/5C.2 Backend Review Manifest

Prepared: 2026-08-31

Status: Phase 5C.1 and Phase 5C.2 backend implementation and verification passed. Work stopped before Phase 5C.3 Administrative controls, PrimeHR/Careers UI, Jasper, and Playwright.

## 1. Scope and conclusion

This manifest compares the implementation with `PHASE_5C_SCREENING_QS_VALIDATION_SCOPE_APPROVAL.md` and Master Plan V2 Phase 5C. It covers:

- Phase 5C.1: versioned generic screening policies, criteria, reason codes, exact vacancy-publication binding, deterministic objective evaluation with explicit human-review fallback, RBAC, audit, REST/OpenAPI, and V15 migrations;
- Phase 5C.2: assigned screening cases, findings, immutable evidence references, recommendation, independent return/finalization, administrator override, superseding correction, withdrawal cancellation, applicant-safe status, REST/OpenAPI, and V16 migrations;
- authoritative assignee identity and exact permission checks before opening, succeeding, or reassigning a case.

The approved backend boundary is satisfied. No Phase 5C.3 or Phase 5D+ application behavior was implemented.

## 2. Requirement map

| Requirement | Implementing files |
|---|---|
| Versioned generic policy lifecycle and immutable published versions | `ScreeningPolicy`, `ScreeningCriterion`, `ScreeningReasonCode`, policy service/controller/DTO/repositories, V15 |
| Exact vacancy-publication policy binding | `PublicationScreeningPolicy`, repository, `ScreeningPolicyServiceImpl`, V15 |
| Transparent objective evaluation and manual fallback | `ScreeningEvidenceEvaluator`, policy domain tests |
| Assigned cases and immutable submission/policy snapshots | `ScreeningCase`, `ScreeningCaseServiceImpl`, V16 |
| Screener/validator assignment and separation of duties | `ScreeningAssignment`, `ScreeningAssignmentEligibilityService`, HR directory/Administrative authorization clients, controller, V16 |
| Complete human-confirmed findings and evidence | `ScreeningFinding`, `ScreeningEvidenceLink`, service/DTOs/repositories, V16 |
| Qualified/disqualified consistency and safe reasons | `ScreeningDecision`, `PositionApplication`, service, reason-code rules, V16 |
| Return, resubmit, finalization, administrator override, correction lineage | `ScreeningCase`, `ScreeningCaseServiceImpl`, case endpoints/tests |
| Atomic withdrawal cancellation | `ScreeningWithdrawalCoordinator`, `ApplicantApplicationServiceImpl`, `PositionApplication` |
| Sensitive read/action authorization | both Phase 5C permission guards, case repository permitted query, Administrative effective authorization |
| Optimistic conflicts and transactional audit | audited entities, service version checks/transactions, `PrimeHrAuditService` calls, integration tests |
| Stable REST contract | controllers/DTOs and `contracts/openapi/primehr-v1.yaml` |
| SQL Server/PostgreSQL portability | paired V15/V16 migrations, JPA/JPQL repositories, provider/parity/upgrade tests |

## 3. Package and module structure

Runtime ownership remains in the existing Maven modules:

```text
Administrative
  com.administrative.controllers   restricted effective employee-permission lookup
  com.administrative.impl          canonical Phase 5C feature recognition

PrimeHR
  com.primehr.integration          authoritative Administrative and HR lookups
  com.primehr.rsp.screening.api    explicit REST DTOs/controllers
  com.primehr.rsp.screening.application
                                    transactions, rules, evaluation, withdrawal coordination
  com.primehr.rsp.screening.domain JPA aggregates and lifecycle rules
  com.primehr.rsp.screening.infrastructure
                                    Spring Data repositories/JPQL
  com.primehr.security             exact Phase 5C permission guards
```

PrimeHR does not read or write Administrative/HumanResource tables. It uses authenticated service APIs and preserves module boundaries.

## 4. Created files

### Administrative

- `Administrative/src/test/java/com/administrative/controllers/EffectiveAuthorizationControllerTest.java` - target-permission lookup caller authorization and feature confinement.

### PrimeHR integration, API, and application

- `PrimeHR/src/main/java/com/primehr/integration/humanresource/HumanResourceEmployeeDirectoryClient.java` - authoritative employee number/role lookup using caller authentication.
- `PrimeHR/src/main/java/com/primehr/integration/humanresource/HumanResourceEmployeeDirectoryEntry.java` - typed minimal HR response.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/api/ScreeningCaseController.java` - case workflow REST endpoints and assignment eligibility entry checks.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/api/ScreeningCaseDtos.java` - explicit case/assignment/finding/evidence/decision contracts.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/api/ScreeningPolicyController.java` - policy and publication-binding REST endpoints.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/api/ScreeningPolicyDtos.java` - explicit policy/criterion/reason/binding contracts.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningAssignmentEligibilityService.java` - authoritative employee, exact action, agency-scope, and SOD checks.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningCaseService.java` - case workflow boundary.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningCaseServiceImpl.java` - transactional workflow, consistency, correction, safe decision, and audit rules.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningEvidenceEvaluator.java` - deterministic objective evaluation/manual-review fallback.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningPolicyService.java` - policy workflow boundary.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningPolicyServiceImpl.java` - policy lifecycle, child validation, binding, and audit.
- `PrimeHR/src/main/java/com/primehr/rsp/screening/application/ScreeningWithdrawalCoordinator.java` - atomic cancellation of an open case during applicant withdrawal.

### PrimeHR domain and persistence

- `PublicationScreeningPolicy.java`, `ScreeningPolicy.java`, `ScreeningCriterion.java`, and `ScreeningReasonCode.java` - V15 policy/binding aggregates.
- `ScreeningCase.java`, `ScreeningAssignment.java`, `ScreeningFinding.java`, `ScreeningEvidenceLink.java`, and `ScreeningDecision.java` - V16 workflow/evidence/decision aggregates.
- `PublicationScreeningPolicyRepository.java`, `ScreeningPolicyRepository.java`, `ScreeningCriterionRepository.java`, and `ScreeningReasonCodeRepository.java` - policy persistence.
- `ScreeningCaseRepository.java`, `ScreeningAssignmentRepository.java`, `ScreeningFindingRepository.java`, `ScreeningEvidenceLinkRepository.java`, and `ScreeningDecisionRepository.java` - assigned case persistence and permitted reads.
- `RspScreeningPolicyPermissionGuard.java` and `RspApplicationScreeningPermissionGuard.java` - exact actions and agency-wide scope.

### Migrations

- PostgreSQL: `V15__rsp_screening_policy_foundation.sql`, `V16__rsp_application_screening.sql`.
- SQL Server: `V15__rsp_screening_policy_foundation.sql`, `V16__rsp_application_screening.sql`.

### Tests

- `PrimeHrV14ToV15UpgradeIT.java` - populated V14-to-V15 preservation.
- `PrimeHrV15ToV16UpgradeIT.java` - populated V15-to-V16 preservation.
- `ScreeningPolicyDomainTest.java` - lifecycle, compatibility, ordering, effectivity, and immutability.
- `ScreeningPolicyServiceIntegrationTest.java` - policy lifecycle/version/audit behavior.
- `ScreeningPolicyBindingServiceTest.java` - exact ownership/binding/immutability/conflict rules.
- `ScreeningAssignmentEligibilityServiceTest.java` - employee eligibility, required actions, scope, and SOD.
- `ScreeningCaseServiceIntegrationTest.java` - qualified flow, invalid/incomplete/return paths, withdrawal, and stale conflict.
- `RspScreeningPolicyPermissionGuardTest.java` and `RspApplicationScreeningPermissionGuardTest.java` - exact permission enforcement.

### Documentation

- `docs/prime-hrm/PHASE_5C_SCREENING_QS_VALIDATION_SCOPE_APPROVAL.md` - approved exact scope and boundaries.
- `docs/prime-hrm/PHASE_5C_1_5C_2_BACKEND_REVIEW_MANIFEST.md` - this review record.

## 5. Modified files

- `Administrative/.../EffectiveAuthorizationController.java` - restricted target employee screening-permission resolution for authorized coordinators.
- `Administrative/.../EffectiveAuthorizationServiceImpl.java` - recognizes the two canonical Phase 5C feature keys.
- `Administrative/.../EffectiveAuthorizationServiceImplTest.java` - verifies feature independence, actions, scope, and fail-closed behavior.
- `PrimeHR/.../AdministrativeAuthorizationClient.java` - resolves an authoritative target employee's application-screening permission.
- `PrimeHR/.../ApplicantApplicationServiceImpl.java` - includes under-screening applications in active rules and atomically coordinates withdrawal cancellation.
- `PrimeHR/.../PositionApplication.java` - adds `UNDER_SCREENING`, `QUALIFIED`, and `DISQUALIFIED` plus safe labels and controlled transitions.
- `PrimeHR/.../PrimeHrOpenApiContractTest.java` - asserts Phase 5C routes/schemas and continued Phase 5D+ exclusion.
- `PrimeHR/.../AbstractPrimeHrProviderIntegration.java` - validates schema through V16.
- `PrimeHR/.../CompetencyMigrationParityTest.java` - structural parity through V16.
- `contracts/openapi/primehr-v1.yaml` - contract version `5.6.0-phase-5c.2`, policy and case endpoints/schemas.
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md` - records completed backend gates and the Phase 5C.3 stop.

`.idea/compiler.xml` is an unrelated existing IDE change. It was preserved, was not needed for Phase 5C, and must not be included in the Phase 5C commit.

## 6. Database tables, constraints, indexes, and relationships

V15 adds `rsp_screening_policy`, `rsp_screening_policy_criterion`, `rsp_screening_reason_code`, and `rsp_publication_screening_policy`. It enforces agency/code/version uniqueness, unique ordered child codes/display order, lifecycle/effectivity and category/evaluation metadata, one exact policy binding per vacancy publication, foreign keys, optimistic versions, and queue/binding indexes.

V16 adds `rsp_screening_case`, `rsp_screening_assignment`, `rsp_screening_finding`, `rsp_screening_evidence_link`, and `rsp_screening_decision`; it extends application status constraints for under-review/final outcomes. It enforces a current case key, revision/supersession lineage, active role assignments, one finding per criterion/case, evidence ownership metadata, consistent decision/reason/override fields, foreign keys, optimistic versions, and agency/status/assignee/application/publication indexes.

SQL Server uses its provider-appropriate filtered current-case uniqueness. PostgreSQL uses nullable uniqueness semantics compatible with PostgreSQL and the PostgreSQL-mode migration gate. Shared Java contains no native provider-specific query.

## 7. REST endpoints and authorization

Policy endpoints: list/create/get/update/publish/successor and publication binding under `/api/primehr/v1/rsp`. They require `primehr.rsp-screening-policy` with Access/Add/Edit/Publish as applicable and agency-wide scope unless administrator.

Case endpoints: list, open, get, successor, assignments, finding update, submit, return, finalize, override, and history under `/api/primehr/v1/rsp`. They require `primehr.rsp-application-screening` with Access/Add/Edit/Submit/Approve as applicable, agency-wide scope, and active case assignment for sensitive reads/actions. Override additionally requires administrator authority and a reason.

Opening/succeeding/reassigning verifies distinct authoritative HR employees. Screener requires Access+Edit+Submit+AGENCY_WIDE; validator requires Access+Approve+AGENCY_WIDE. The Administrative lookup endpoint is callable only by administrator or Access+Add+AGENCY_WIDE and is confined to this exact feature key.

Applicant APIs expose existing owner-only application detail/communication with safe status/reason only. They expose no internal finding, policy instruction, evidence assessment, actor, or audit content.

## 8. Verification evidence

| Command/gate | Result |
|---|---|
| Phase 5C.1 focused suite | 32 passed; zero failures/errors/skips |
| Phase 5C.1 full PrimeHR gate | 174 passed; zero failures/errors/skips; package passed |
| Phase 5C.2 focused service/guard/parity/OpenAPI/applicant suite | 33 passed; zero failures/errors/skips |
| Assignment eligibility and case focused rerun | 9 passed; zero failures/errors/skips |
| Administrative focused authorization final rerun | 17 passed; zero failures/errors/skips |
| `mvn -pl PrimeHR clean test` | 185 passed; zero failures/errors/skips; BUILD SUCCESS |
| `mvn -pl PrimeHR clean package` | 185 passed; zero failures/errors/skips; executable JAR packaged |
| `mvn -pl Administrative -am clean package` | Common 3 and Administrative 45 passed; zero failures/errors/skips; JARs packaged |
| PostgreSQL-mode `PrimeHrFlywaySchemaIntegrationTest` V1-V16 | 9 passed; zero skipped |
| Real configured SQL Server `PrimeHrSqlServerSchemaIT` V1-V16 | 9 passed; zero skipped |
| Disposable SQL Server `PrimeHrV15ToV16UpgradeIT` | 1 passed; submitted V15 application preserved; new case tables empty |
| `git diff --check` | passed; only LF-to-CRLF conversion warnings |
| provider-neutral shared-query scan | no native/vendor-specific screening query found |
| secret/generated/later-phase scan | no new secret/generated artifact or Phase 5D+ runtime behavior found |

The disposable SQL Server database `primehr_5c2_upgrade_20260830` was dropped after the upgrade test. It contained only test fixtures and is intentionally unrecoverable. No production/user database was deleted.

Two initial Maven attempts could not create generated `target` files because the backend repository is outside the active frontend sandbox writable root. No test ran in those attempts. The commands were rerun with approved backend write access and passed; this was not a compilation or application failure.

## 9. Mocked, assumed, skipped, or unverified behavior

- No live PostgreSQL instance was run, by the established SQL Server-primary direction. PostgreSQL evidence is paired DDL, H2 PostgreSQL-mode Flyway/Hibernate, parity tests, and provider-neutral JPA/Java.
- Assignment eligibility unit tests mock the HR directory and Administrative HTTP clients; service workflows use real Spring/JPA/H2 integration tests. The live SQL Server gate validates persistence/migrations, not cross-service HTTP availability.
- The populated V15-to-V16 test temporarily disables/re-enables unrelated SQL Server fixture constraints only inside its disposable test database so an isolated submitted application can be seeded.
- Final safe reason wording is generic policy data and still requires agency approval/configuration before production use.
- No UI or browser behavior was tested because Phase 5C.3 is not approved.

## 10. Known risks and unresolved decisions

1. Live PostgreSQL remains unverified and non-blocking under user direction.
2. Administrative permission rows and staff/applicant UI do not yet expose the new feature; that is Phase 5C.3.
3. Assignment lookup currently reads the established HR basic-info collection and filters exact employee number. A future minimal employee-by-number contract would reduce payload size but is not required for correctness.
4. Published policies/final decisions are forward-only. Applied V15/V16 migrations must not be edited after deployment.
5. Client-specific QS equivalence, evidence rules, reason wording, and screening roles remain configuration/policy decisions, not hard-coded behavior.

## 11. Explicit boundary and next gate

No Administrative permission UI, PrimeHR screening-policy/case UI, Careers safe-status UI, Playwright, Jasper report, examination, interview, HRMPSB/committee, scoring, ranking, shortlist, selection, appointment handoff, employee creation, onboarding, or Phase 5D+ workflow was implemented.

The next allowed action is review and explicit approval of Phase 5C.3. Do not begin it automatically.
