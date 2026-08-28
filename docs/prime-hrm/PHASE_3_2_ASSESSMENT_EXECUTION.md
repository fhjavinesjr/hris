# ISOFT PRIME-HRM Phase 3.2 - Assessment Execution and Structured Evidence

Status: Complete under the user-approved SQL Server-primary acceptance policy

Updated: 2026-08-27

## Scope and boundary

Phase 3.2 implements the execution portion approved in `PHASE_3_COMPETENCY_ASSESSMENT_PERSON_PROFILES_SCOPE_APPROVAL.md`: assessment-tool publication, cycle opening/closing, the authenticated assessor inbox, exact assigned work, ratings, structured evidence, completeness checks, submission, return for correction, and resubmission.

The user approved one narrow validation-side operation in this slice: a validator with `canValidate` and `AGENCY_WIDE` may return a `FOR_VALIDATION` case for correction with a mandatory reason. Phase 3.2 does not implement validation decisions, final-level selection, administrator validation override, immutable person profiles, person-profile reads, UI pages, reports, gap analysis, or any Phase 4 behavior.

## Lifecycle and authorization

- A complete DRAFT tool may be published only while its cycle is DRAFT and its exact ACTIVE Position Profile snapshot is still current.
- A cycle may open only when every active tool is published and each active subject has exactly the assessment methods configured by that tool.
- Opening atomically changes cases and assessor assignments to `ASSIGNED`; closing blocks further execution writes.
- Inbox and work reads derive the actor from the authenticated identity. An assessor sees only explicitly assigned contributions.
- `OWN_RECORDS` is accepted only for genuine self-assessment where assessor and subject are the same employee. Assigned and agency scopes never replace the exact-assignment check.
- Ratings accept only an active level from the competency's exact proficiency scale.
- Evidence is structured metadata linked to one rating; Phase 3.2 adds no binary upload/storage behavior.
- Submission requires every exact profile requirement to be rated and every evidence-required method to contain active evidence.
- The last required contribution moves the case to `FOR_VALIDATION`; duplicate and stale submissions fail without duplicate data or audit.
- Return is atomic, requires `canValidate`, `AGENCY_WIDE`, a current case record version, and a non-blank reason. It returns every submitted contribution together. A returned assessor may correct and resubmit.
- All lifecycle and mutable child operations are transactional, agency-scoped, audited, and guarded by optimistic record versions at the child, assignment, and case roots.

## API delivered

Assessment administration lifecycle:

- `POST /api/primehr/v1/admin/assessment-tools/{toolId}/publish`
- `POST /api/primehr/v1/admin/assessment-cycles/{cycleId}/open`
- `POST /api/primehr/v1/admin/assessment-cycles/{cycleId}/close`

Assigned-assessor execution:

- `GET /api/primehr/v1/assessments/mine`
- `GET /api/primehr/v1/assessments/{caseId}`
- `PUT /api/primehr/v1/assessments/{caseId}/assignments/{assignmentId}/ratings/{competencyVersionId}`
- `POST /api/primehr/v1/assessments/{caseId}/assignments/{assignmentId}/evidence`
- `PUT /api/primehr/v1/assessments/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}`
- `POST /api/primehr/v1/assessments/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}/archive`
- `POST /api/primehr/v1/assessments/{caseId}/assignments/{assignmentId}/submit`

Return-only validation boundary:

- `POST /api/primehr/v1/validation/assessment-cases/{caseId}/return`

The authoritative contract is `contracts/openapi/primehr-v1.yaml`, version `3.2.0-phase-3.2`. Validation list/detail/validate operations and all person-profile operations are deliberately absent.

## Persistence and portability

Equivalent provider-specific V7 migrations are isolated at:

- `PrimeHR/src/main/resources/db/migration/sqlserver/V7__assessment_execution.sql`
- `PrimeHR/src/main/resources/db/migration/postgresql/V7__assessment_execution.sql`

V7 adds publication/open/close/submission timestamps and actors, `for_validation_at`, `prime_assessment_rating`, `prime_assessment_evidence`, their foreign keys and uniqueness constraints, and inbox/rating/evidence indexes. Shared Java uses JPA, JPQL, Spring Data pagination, and provider-neutral conditional updates. No native SQL or provider branching was added to shared Java.

The first full test run identified that PostgreSQL's multi-column `ALTER TABLE ... ADD COLUMN` form was not accepted by the PostgreSQL-mode H2 migration harness. V7 was changed to one portable `ADD COLUMN` statement per column. The focused Flyway and parity suite then passed 17/17, and the full suite passed.

## Main implementation files

- `PrimeHR/.../assessment/api/AssessmentExecutionController.java`, `AssessmentExecutionDtos.java`, and `AssessmentValidationController.java` define the HTTP/DTO boundary.
- `PrimeHR/.../assessment/application/AssessmentExecutionService.java` and `AssessmentExecutionServiceImpl.java` own exact-assignment visibility, transactional execution, completeness, submission, return, and mapping.
- `PrimeHR/.../assessment/domain/AssessmentRating.java` and `AssessmentEvidence.java` model mutable assessor contributions; existing cycle/tool/case/assignment aggregates gained Phase 3.2 lifecycle metadata and transitions.
- `PrimeHR/.../assessment/infrastructure/AssessmentRatingRepository.java`, `AssessmentEvidenceRepository.java`, and updated aggregate repositories provide agency-scoped provider-neutral persistence.
- `PrimeHR/.../security/AssessmentPermissionGuard.java` enforces exact actions and data scopes.
- `PrimeHR/.../shared/exception/PrimeHrExceptionHandler.java` preserves explicit validation/lifecycle/conflict responses.
- `contracts/openapi/primehr-v1.yaml` records request and response contracts.

Phase 3.1 Administrative permission, HRM subject-contract, V6 model, configuration, and tests are part of the same uncommitted backend change set and remain documented in `PHASE_3_1_ASSESSMENT_DRAFT_FOUNDATION.md`.

## Automated verification

Focused execution/authorization/contract/parity gate:

```text
mvn -pl PrimeHR -am "-Dtest=AssessmentExecutionServiceIntegrationTest,AssessmentAdministrationServiceIntegrationTest,AssessmentPermissionGuardTest,CompetencyMigrationParityTest,PrimeHrOpenApiContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
PASS: 17 tests, 0 failures, 0 errors, 0 skipped
```

Focused PostgreSQL-mode Flyway/parity rerun after the V7 correction:

```text
mvn -pl PrimeHR -am "-Dtest=PrimeHrFlywaySchemaIntegrationTest,CompetencyMigrationParityTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
PASS: 17 tests, 0 failures, 0 errors, 0 skipped
```

Full affected clean test:

```text
mvn -pl Administrative,HumanResource,PrimeHR -am clean test "-Ddebug=false" "-Dlogging.level.org.hibernate.SQL=INFO"
BUILD SUCCESS
Common: 3 tests, 0 failures, 0 errors, 0 skipped
Administrative: 33 tests, 0 failures, 0 errors, 0 skipped
HumanResource: 45 tests, 0 failures, 0 errors, 0 skipped
PrimeHR: 86 tests, 0 failures, 0 errors, 0 skipped
```

Full affected package:

```text
mvn -pl Administrative,HumanResource,PrimeHR -am clean package "-Ddebug=false" "-Dlogging.level.org.hibernate.SQL=INFO"
BUILD SUCCESS with the same 167 tests and zero skipped
```

Coverage proves exact assessor/cross-subject isolation, genuine self-assessment identity, valid level membership, required evidence, no partial state after incomplete or invalid operations, duplicate submission rejection, return reason enforcement, return/resubmit, closed-cycle rejection, stale root conflicts, permission/data-scope enforcement, audit, OpenAPI boundaries, migration structure/parity, Flyway V1-V7, Hibernate validation, and application startup.

## Real-provider verification

Real Microsoft SQL Server 14.0:

- fresh V1-to-V7 migration and provider suite: PASS, 9 tests, 0 skipped, isolated schema `primehr_phase32_fresh_20260826`;
- populated V6-to-V7 upgrade: PASS, 1 test, 0 skipped, isolated schema `primehr_phase32_upgrade_20260826`;
- the seeded V6 cycle survived the V7 upgrade;
- schemas were retained for independent inspection.

During an earlier invocation, environment-based schema selection was overridden by literal application configuration, so Flyway additively advanced the local default `primehr` `dbo` schema from V5 to V7. The run passed and did not delete existing records, but this unintended target is disclosed and should be reviewed before manually removing anything. The later isolated fresh and populated-upgrade runs used explicit Maven system-property overrides and passed.

Real PostgreSQL was not run, following the approved SQL Server-primary policy. PostgreSQL portability is covered by the equivalent V7 migration, successful PostgreSQL-mode H2 Flyway V1-V7 execution, migration structural parity tests, and provider-neutral Java/JPA. A live PostgreSQL V7 fresh/upgrade run remains unverified and non-blocking under that policy.

## Repository, secret, and phase-boundary audit

- `git diff --check`: PASS; only Git line-ending conversion warnings were emitted.
- No generated `target`, IDE, credential, E2E-secret, or binary artifact is tracked by this change set.
- Credential-pattern filenames were inspected. Hits are existing configuration/security or employee credential fields; no new secret value was introduced by Phase 3.2.
- No provider-specific query construct was found in the new shared Java. Provider-specific DDL remains isolated by migration directory.
- `VALIDATED` enum values and the predeclared person-profile permission key are V6 forward-compatible vocabulary only. There is no validation-decision service/route and no person-profile entity, repository, service, controller, migration, or route.
- No Phase 3.3, Phase 3.4, or Phase 4 behavior was implemented.

## Known limitations and next gate

- Phase 3.2 is backend/API only; there is no browser UI in this slice.
- Evidence is structured metadata, not a file upload. Storage policy remains a later explicitly approved concern.
- Supervisor authority is still explicit assignment; the system does not infer an organizational supervisor relationship.
- The server remains configured for a generic/single agency until a client-specific multi-agency identity policy is approved.
- Live PostgreSQL V7 remains unverified as described above.

All approved Phase 3.2 gates pass. Stop here. Phase 3.3 human validation and immutable person profiles require separate explicit authorization before implementation.
