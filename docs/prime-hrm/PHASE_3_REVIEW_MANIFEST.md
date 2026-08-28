# ISOFT PRIME-HRM Phase 3 Review Manifest

Prepared: 2026-08-28

Status: Phase 3.1-3.4 complete and ready for independent review; Phase 4 not started

## 1. Scope delivered

Phase 3 implements employee competency assessment cycles/tools, eligible HRM subject snapshots, explicit assessor assignments, exact assigned/self contributions, ratings, structured evidence references, submit/return/resubmit, independent human validation, audited administrator override, and immutable versioned Person Competency Profiles. Administrative owns feature permissions; HumanResource owns employee/current-appointment facts; PrimeHR owns the assessment and official-profile aggregates; `prime-hr-software` owns the standalone SSO UI.

Excluded: competency-gap computation, L&D referral/planning, applicants, SPMS, RSP, rewards/recognition, notifications, report generation, binary evidence upload/storage, generic workflow engine, shared-database joins, HRISApp integration, and every Phase 4+ table/API/page.

## 2. Requirement-to-file map

| Requirement | Implementing files |
|---|---|
| HRM minimal subject/current-appointment contract | `HumanResource/.../integration/primehr/*`, `EmployeeRepository.java`, HRM application properties, HRM integration OpenAPI/tests |
| Assess/Validate/Finalize and data scope | Administrative permission DTO/service/test, `PermissionDataScope` in Administrative/PrimeHR, PrimeHR permission guards/actions, Administrative `Permission.tsx` |
| Cycle/tool/subject/assessor draft foundation | PrimeHR assessment domain/API/application/infrastructure packages; V6 migrations |
| Ratings, structured evidence, submit/return | assessment execution DTO/controller/service/domain/repositories; V7 migrations |
| Independent human validation and immutable profiles | validation/profile DTO/controllers/services/domain/repositories; V8 migrations |
| Provider portability | equivalent PostgreSQL/SQL Server V6-V8; JPA repositories/Pageable; migration parity and provider integration tests |
| REST contracts | `contracts/openapi/primehr-v1.yaml`, `contracts/openapi/humanresource-primehr-integration-v1.yaml`, OpenAPI contract tests |
| Four Phase 3 UI routes | PrimeHR route directories, shared shell/styles, typed `assessments.ts`, auth/runtime configuration |
| Repeatable browser acceptance | `e2e/phase3.spec.ts`, support/config updates, E2E runbook |
| User/phase documentation | Phase 3 scope/checkpoint documents, progress ledger, Phase 3.4 acceptance report, user guide, this manifest |

## 3. Package/module structure

- `Administrative`: authoritative permission ruleset and effective feature authorization.
- `HumanResource`: authenticated read-only assessment-subject integration using HRM-owned employee and appointment data.
- `PrimeHR`:
  - `assessment.api`: versioned controllers and explicit request/response DTOs;
  - `assessment.application`: transactional lifecycle, authorization-aware orchestration, validation, mapping, and audit;
  - `assessment.domain`: aggregates/value enums and invariant enforcement;
  - `assessment.infrastructure`: Spring Data JPA repositories;
  - `integration.administrative` and `integration.humanresource`: typed dependency boundaries;
  - `security`: exact action/data-scope guards and narrow HTTP security routing.
- `prime-hr-software`: SSO/runtime configuration, strict typed API client, shared shell/styles, four Phase 3 pages, and Playwright.
- `administrative-software`: permission-matrix controls only.

No module reads another module's tables and no provider-specific query was added to shared Java/JPA logic.

## 4. Persistence

Equivalent forward-only migrations exist in both `db/migration/postgresql` and `db/migration/sqlserver`.

### V6 foundation

- `prime_assessment_cycle`
- `prime_assessment_tool`
- `prime_assessment_tool_method`
- `prime_assessment_case`
- `prime_assessor_assignment`

### V7 execution

- `prime_assessment_rating`
- `prime_assessment_evidence`

### V8 validation/profile

- `prime_assessment_validation`
- `prime_assessment_validated_rating`
- `prime_person_competency_profile`
- `prime_person_competency_result`

Primary/foreign keys, agency identifiers, status checks, optimistic versions, unique cycle/tool/subject/assignment/rating/validation/profile constraints, active flags, timestamps/effectivity, predecessor links, and employee/assessor/status/latest-profile indexes are defined in provider-equivalent form. Assessment records reference existing PrimeHR Position Profile, competency, and proficiency-level identities; profile generation retains exact cycle/tool/case/validation and predecessor lineage. `prime_audit_event` remains the append-only shared audit table.

## 5. REST endpoints and authorization

### HRM integration

- `GET /api/integration/v1/primehr/assessment-subjects`
- `GET /api/integration/v1/primehr/assessment-subjects/{employeeId}`

Both require authenticated `primehr.assessment-administration` access and return only minimal identity/current-appointment/source-fingerprint data.

### Assessment administration

- cycles: list/create/get/update/archive/open/close;
- cycle tools: list/create/get/update/archive/publish;
- tool subjects: list/add;
- cases: get/archive;
- case assessors: add/update/archive.

These are under `/api/primehr/v1/admin/**`, require exact Add/Edit/Archive/Publish/Finalize actions, and require agency-wide scope for administration.

### Assessment execution

- `GET /api/primehr/v1/assessments/mine`
- `GET /api/primehr/v1/assessments/{caseId}`
- `PUT .../ratings/{competencyVersionId}`
- `POST/PUT .../evidence` and `POST .../archive`
- `POST .../submit`

Access, Assess, and Submit are independent. The authenticated employee must own the exact assignment and satisfy `OWN_RECORDS` or `ASSIGNED_RECORDS`; browser-supplied identity is not authoritative.

### Validation

- list/get/return/validate under `/api/primehr/v1/validation/assessment-cases`.

Agency-wide Validate access is required. Ordinary self-validation is denied. Administrator override requires an explicit audited reason.

### Person profiles

- own latest/history;
- authorized employee latest/history;
- exact immutable version lookup.

Own versus agency-wide scope is enforced by the backend. Raw confidential assessor contributions are not returned by Person Profile endpoints.

## 6. DTOs, entities, repositories, services, and controllers

- DTOs: `AssessmentDtos`, `AssessmentExecutionDtos`, `AssessmentValidationDtos`, HRM subject page/row/response types, effective permission responses.
- Entities/domain: cycle, tool/method, case/subject snapshot, assignment, rating/evidence, validation/validated rating, Person Profile/result plus explicit lifecycle/method/status enums.
- Repositories: one Spring Data repository per Phase 3 aggregate/child plus additive exact-profile/level and HRM appointment projections.
- Services: administration, execution, validation/person-profile query, HRM integration, authorization/dependency clients, and audit orchestration.
- Controllers: assessment administration, execution, validation, Person Profile, and HRM assessment-subject integration.

Services own transactions, state transitions, completeness, exact-version validation, separation of duties, predecessor closure, immutable generation, and audit. Controllers own HTTP validation/headers and pass token-derived actor/agency context.

## 7. Created files

### Backend repository

- `Administrative/src/main/java/com/administrative/dtos/PermissionDataScope.java`
- HumanResource integration: `AdministrativePermissionResponse.java`, `AssessmentSubjectIntegrationController.java`, `AssessmentSubjectIntegrationService.java`, `AssessmentSubjectIntegrationServiceImpl.java`, `AssessmentSubjectPageResponse.java`, `AssessmentSubjectResponse.java`, `AssessmentSubjectRow.java`, `PrimeHrSubjectAuthorization.java`
- HumanResource tests: `AssessmentSubjectIntegrationControllerTest.java`, `AssessmentSubjectIntegrationServiceImplTest.java`
- PrimeHR assessment API: `AssessmentAdministrationController.java`, `AssessmentDtos.java`, `AssessmentExecutionController.java`, `AssessmentExecutionDtos.java`, `AssessmentValidationController.java`, `AssessmentValidationDtos.java`, `PersonCompetencyProfileController.java`
- PrimeHR assessment application: `AssessmentAdministrationService.java`, `AssessmentAdministrationServiceImpl.java`, `AssessmentExecutionService.java`, `AssessmentExecutionServiceImpl.java`, `AssessmentValidationService.java`, `AssessmentValidationServiceImpl.java`
- PrimeHR assessment domain: `AssessmentCase.java`, `AssessmentCaseStatus.java`, `AssessmentCycle.java`, `AssessmentCycleStatus.java`, `AssessmentEvidence.java`, `AssessmentMethod.java`, `AssessmentRating.java`, `AssessmentSubjectSnapshot.java`, `AssessmentTool.java`, `AssessmentToolMethod.java`, `AssessmentToolStatus.java`, `AssessmentValidatedRating.java`, `AssessmentValidation.java`, `AssessorAssignment.java`, `AssessorAssignmentStatus.java`, `PersonCompetencyProfile.java`, `PersonCompetencyResult.java`
- PrimeHR repositories: `AssessmentCaseRepository.java`, `AssessmentCycleRepository.java`, `AssessmentEvidenceRepository.java`, `AssessmentRatingRepository.java`, `AssessmentToolMethodRepository.java`, `AssessmentToolRepository.java`, `AssessmentValidatedRatingRepository.java`, `AssessmentValidationRepository.java`, `AssessorAssignmentRepository.java`, `PersonCompetencyProfileRepository.java`, `PersonCompetencyResultRepository.java`
- PrimeHR integration/security: `integration/administrative/PermissionDataScope.java`, `integration/humanresource/HumanResourceAssessmentSubject.java`, `HumanResourceAssessmentSubjectClient.java`, `HumanResourceAssessmentSubjectPage.java`, `HumanResourceDependencyException.java`, `security/AssessmentPermissionGuard.java`
- Migrations: PostgreSQL and SQL Server `V6__assessment_draft_foundation.sql`, `V7__assessment_execution.sql`, `V8__assessment_validation_person_profiles.sql`
- PrimeHR tests: `AssessmentAdministrationServiceIntegrationTest.java`, `AssessmentExecutionServiceIntegrationTest.java`, `AssessmentDraftDomainTest.java`, `PrimeHrV5ToV6UpgradeIT.java`, `PrimeHrV6ToV7UpgradeIT.java`, `PrimeHrV7ToV8UpgradeIT.java`, `AssessmentPermissionGuardTest.java`
- Contracts: `contracts/openapi/humanresource-primehr-integration-v1.yaml`
- Documents: `PHASE_3_COMPETENCY_ASSESSMENT_PERSON_PROFILES_SCOPE_APPROVAL.md`, `PHASE_3_1_ASSESSMENT_DRAFT_FOUNDATION.md`, `PHASE_3_2_ASSESSMENT_EXECUTION.md`, `PHASE_3_3_HUMAN_VALIDATION_PERSON_PROFILES.md`, `PHASE_3_4_UI_PLAYWRIGHT_ACCEPTANCE.md`, `PHASE_3_REVIEW_MANIFEST.md`

### PRIME-HRM UI repository

- `e2e/phase3.spec.ts`
- assessment administration: `AssessmentAdministrationManager.tsx`, `page.tsx`
- assessment validation: `AssessmentValidationManager.tsx`, `page.tsx`
- assessments: `AssessmentInbox.tsx`, `page.tsx`
- person profiles: `PersonProfileManager.tsx`, `page.tsx`
- shared components: `AccessDenied.tsx`, `AssessmentUi.module.scss`, `PrimeHrShell.module.scss`, `PrimeHrShell.tsx`
- `src/lib/assessments.ts`

No file was created in Administrative UI; its permission component was extended in place.

## 8. Modified files and reason

### Backend repository

- Administrative effective-permission response/service/test: additive Phase 3 actions/scopes, administrator compatibility, and legacy fail-closed mapping.
- `HumanResource/EmployeeRepository.java`: provider-neutral projection for eligible current appointments.
- HumanResource/PrimeHR application properties: configurable cross-service URL/CORS support.
- PrimeHR proficiency/profile requirement repositories: exact active definition/requirement queries.
- `PrimeHrProperties`, Administrative permission response, action/permission guards, exception handler, and application properties: HRM client configuration, new actions/scope, consistent dependency/conflict errors.
- `PrimeHrSecurityConfiguration.java`: narrowly permits authenticated assessment `POST`/`PUT` and validation `POST` routes so exact service guards execute; all other unmatched requests remain denied.
- OpenAPI/migration test infrastructure/parity/contract tests: Phase 3 paths, schemas, V6-V8 upgrades, and provider-equivalence verification.
- `PRIME_HRM_PROGRESS.md`, `PRIME_HRM_USER_GUIDE.md`: checkpoint ledger and delivered end-user workflows.

### PRIME-HRM UI repository

- `.env.example`, production package/verification scripts, runtime config route/helper: configurable HRM API URL.
- `auth.ts`: typed permission parsing with administrator compatibility and legacy fail-closed behavior.
- layout, competency manager, Position Profile manager: product metadata/shared navigation to Phase 3 routes.
- `playwright.config.ts`, test support: isolated service ports, runtime-config interception, controlled HRM/profile fixtures, and exact permission restoration.
- ESLint config: ignore generated Playwright output directories.
- `docs/PRIME_HRM_E2E_TESTING.md`: eleven-test repeatable runbook and recorded result.

### Administrative UI repository

- `src/app/administrative/permission/Permission.tsx`: four feature rows and Assess/Validate/Finalize/Data Scope persistence/UI rules.
- `.env`: pre-existing unrelated user change; preserved but not part of Phase 3 and must not be committed with this work.

## 9. Test classes and coverage

- HRM controller/service tests: authentication/authorization, minimal response, paging/search, eligibility, active appointment, and no excess PDS/password data.
- `AssessmentDraftDomainTest`: lifecycle and identity/duplicate invariants.
- administration integration tests: drafts, snapshots, exact Position Profile, publication/open/close, assignment and optimistic/audit behavior.
- execution integration tests: exact inbox ownership, ratings/levels, evidence, completeness, submit, return/resubmit, immutability, stale/invalid/cross-scope failures, and atomicity.
- validation integration tests (within Phase 3 application suite): all-contributor readiness, explicit decisions, separation of duties, override reason, one immutable generated profile, predecessor effectivity, latest/history, and denied scope.
- permission guard/service tests: independent actions, scopes, legacy denial, and administrator behavior.
- contract tests: all documented paths/methods/schemas.
- migration upgrade/parity tests: fresh and populated V5→V6→V7→V8 and PostgreSQL/SQL Server structural equivalence.
- Playwright: 11 full tests, including 3 Phase 3 browser cases for allow/deny, assignment, stale conflict, submit, validation override, and immutable profile history.

## 10. Commands and final results

```text
mvn -pl Administrative,HumanResource,PrimeHR -am clean test
BUILD SUCCESS; Common 3 + Administrative 33 + HumanResource 45 + PrimeHR 90 = 171;
failures 0, errors 0, skipped 0

mvn -pl Administrative,HumanResource,PrimeHR -am clean package
BUILD SUCCESS; all five reactor projects packaged; the same 171 tests executed;
failures 0, errors 0, skipped 0

npx playwright test e2e/phase3.spec.ts
3 passed

npx playwright test
11 passed, zero skipped

prime-hr-software: npm run lint / npm run typecheck / npm run build
PASS / PASS / PASS

administrative-software: npm run lint / npm run typecheck / npm run build
PASS with one pre-existing warning / PASS / PASS

all three repositories: git diff --check
PASS

all three repositories: untracked-file trailing-whitespace scan
PASS
```

No final test command used a skip flag. One intermediate JAR rebuild used `-DskipTests` only before Playwright; it is superseded by both final 171-test clean gates.

## 11. Provider verification

- Real SQL Server: tested for Phase 3 fresh/upgrades in 3.1-3.3 and used by the complete Phase 3.4 Playwright matrix.
- Real PostgreSQL: not run for Phase 3 under the user's approved SQL Server-primary policy.
- PostgreSQL portability: equivalent V6-V8 migrations, PostgreSQL-mode Flyway/Hibernate, structural parity, and provider-neutral JPA/service code pass automated gates. This is not represented as a live PostgreSQL acceptance run.

## 12. Mocked, assumed, skipped, or unverified behavior

- Java service tests use H2 for many unit/integration cases; real SQL Server migration/browser gates supply the blocking provider evidence.
- Administrative/HRM dependency responses are mocked in focused service/controller tests and exercised through real local services in Playwright.
- No authoritative supervisor relationship exists, so `IMMEDIATE_SUPERVISOR` requires explicit assignment and is never inferred.
- Evidence is structured metadata/reference text only; binary storage/scanning/retention is unimplemented by design.
- Live PostgreSQL and deployed Vercel/Render browser acceptance remain unverified.
- The browser matrix used an administrator as an explicitly assigned assessor and then an audited administrator validation override because the supplied ordinary QA accounts did not match eligible HRM assessment subjects. Ordinary separation/action behavior remains covered by backend permission/lifecycle tests and UI denied-surface tests.

## 13. Security, generated files, and repository audit

- Sensitive-pattern scan found no supplied test password, private-key signature, or `sk-` token in the three nonignored trees.
- `.env.e2e.local`, `.next/`, `dist/`, `playwright-report/`, and `test-results/` are ignored and must not be committed.
- No `target/`, IDE metadata, screenshots, traces, browser reports, compiled package, token, or credential appears in Git status.
- Temporary Playwright permission changes were restored after each serial suite.
- Backend authorization is not UI-only; exact feature/action/scope and assignment/identity checks execute on every endpoint.

## 14. Known risks and unresolved decisions

- A real PostgreSQL Phase 3 run remains unverified; run it before adopting PostgreSQL for this module.
- Existing SQL Server Playwright data now includes uniquely named E2E cycles/tools/cases and immutable profile successor versions. This is intentional audit-preserving test data, not production data.
- Administrative lint retains one existing `Sidebar.tsx` exhaustive-deps warning outside this scope.
- Next.js reports the existing Administrative `images.domains` deprecation; it is unrelated to Phase 3.
- Deployment requires correct HRM/Administrative/PrimeHR URLs, shared JWT secret, CORS origins, datasource, and migration locations.
- Future supervisor inference, binary evidence governance, and Phase 4 competency gaps require separate architecture/policy decisions.

## 15. Commit guidance and boundary

Commit the complete Phase 3 backend/document set together because V6-V8, entities, APIs, contracts, tests, and docs form one coherent feature. Commit the complete `prime-hr-software` Phase 3 set including the E2E runbook. In `administrative-software`, commit only `src/app/administrative/permission/Permission.tsx`; exclude the unrelated `.env` modification.

No Phase 1/2 functionality was removed. No Phase 4 or later functionality was accidentally implemented. Stop here until the user approves a separately prepared exact Phase 4 scope.
