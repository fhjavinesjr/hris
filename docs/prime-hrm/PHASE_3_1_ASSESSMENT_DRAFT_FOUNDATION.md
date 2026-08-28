# ISOFT PRIME-HRM Phase 3.1 - Assessment Draft Foundation

Status: Complete under the user-approved SQL Server-primary acceptance policy

Updated: 2026-08-26

## Scope and boundary

Phase 3.1 implements the HRM subject contract, additive Administrative assessment permissions/data scope, PrimeHR authorization and HRM clients, V6 draft persistence, and DRAFT-only cycle/tool/subject/assessor administration approved in `PHASE_3_COMPETENCY_ASSESSMENT_PERSON_PROFILES_SCOPE_APPROVAL.md`.

It does not implement tool publication, cycle opening/closing, an assignment inbox, ratings, structured evidence records, submission/return/resubmission, validation, person competency profiles, UI pages, reports, or Phase 4 behavior. Method evidence requirements are draft tool configuration only; no evidence record or upload behavior exists.

## Source ownership and HRM integration

HumanResource remains authoritative for employee identity, active employment, and the current appointment. It exposes authenticated, paginated, read-only endpoints:

- `GET /api/integration/v1/primehr/assessment-subjects`
- `GET /api/integration/v1/primehr/assessment-subjects/{employeeId}`

Responses contain only employee ID/number/display name, eligibility, current appointment and position/Plantilla identifiers, assumption date, source fingerprint/timestamps, and retrieval time. Passwords, biometric numbers, salary, contact data, the PDS, and unrelated history are excluded. HumanResource independently checks `primehr.assessment-administration` and `AGENCY_WIDE`; unavailable or denied Administrative authorization fails closed.

PrimeHR forwards the caller bearer token through a typed HRM client. It stores immutable minimal subject and assessor snapshots and does not join or directly query HumanResource or Administrative tables.

## Authorization foundation

Administrative's additive effective-permission response now includes:

- `canAssess`;
- `canValidate`;
- `canFinalize`;
- `dataScope` (`NONE`, `OWN_RECORDS`, `ASSIGNED_RECORDS`, or `AGENCY_WIDE`).

Legacy/missing action and scope values fail closed. Administrator behavior remains compatible and resolves to all supported actions with `AGENCY_WIDE`. PrimeHR provides separate assessment-administration, assessment, validation, and person-profile guards; only assessment administration is used by Phase 3.1 endpoints, and non-administrators require exact CRUD permission plus `AGENCY_WIDE`.

## Draft API foundation

The DRAFT-only API is under `/api/primehr/v1/admin` and supports:

- list/get/create/update/archive assessment cycles;
- list/create tools within a cycle and get/update/archive tools;
- list/add/archive assessment subjects;
- get an assessment case;
- add/update/archive explicit assessor assignments.

Every action derives agency and actor from the authenticated context, applies exact permission checks, validates aggregate relationships, uses transactions and optimistic record versions, and writes append-only audit events. Supervisor and panel methods require an explicit assignment reason. Self-assessment requires subject/assessor identity equality; non-self methods reject it.

## Persistence and portability

Equivalent SQL Server and PostgreSQL V6 migrations add:

- `prime_assessment_cycle`;
- `prime_assessment_tool`;
- `prime_assessment_tool_method`;
- `prime_assessment_case`;
- `prime_assessor_assignment`;
- lifecycle, identity, uniqueness, effectivity, and method checks;
- aggregate/reference foreign keys and query indexes.

Provider-specific DDL remains isolated in provider migration directories. Shared Java uses JPQL, Spring Data, `Pageable`, and provider-neutral conditional update statements. The conditional root version increments prevent stale child writes without relying on provider-specific locking syntax.

## Contracts and automated coverage

OpenAPI contracts:

- `contracts/openapi/humanresource-primehr-integration-v1.yaml`;
- `contracts/openapi/primehr-v1.yaml`.

Coverage verifies minimal HRM payloads, active/current appointment mapping, direct authorization denial, legacy permission fail-closed behavior, administrator compatibility, data-scope enforcement, DRAFT lifecycle constraints, self/non-self assignment identity, explicit assignment reasons, audit, rollback, optimistic conflicts, migration structure/parity, application startup, and the absence of Phase 3.2 routes/tables.

## Verification results

```text
.\mvnw.cmd -pl PrimeHR "-Dtest=AssessmentAdministrationServiceIntegrationTest" test
PASS: 1 test, 0 failures, 0 errors, 0 skipped

.\mvnw.cmd -pl Administrative,HumanResource,PrimeHR -am clean test
BUILD SUCCESS
Administrative: 33 tests, 0 failures, 0 errors, 0 skipped
HumanResource: 45 tests, 0 failures, 0 errors, 0 skipped
PrimeHR: 82 tests, 0 failures, 0 errors, 0 skipped
Common: 0 tests discovered by its pre-existing Surefire 2.12.4 configuration

.\mvnw.cmd -pl Administrative,HumanResource,PrimeHR -am clean package "-Ddebug=false" "-Dlogging.level.org.hibernate.SQL=INFO"
BUILD SUCCESS with the same discovered test counts and zero skipped

git diff --check
PASS (line-ending conversion warnings only)
```

An earlier package invocation failed immediately because an unquoted PowerShell `-Dlogging.level...` argument was parsed as a Maven lifecycle phase. No compilation or tests ran in that invocation; the corrected command above passed.

## Real-provider verification

Real Microsoft SQL Server 14.0:

- fresh V1-to-V6 migration and persistence/constraint suite: PASS, 9 tests, 0 skipped;
- populated V5-to-V6 upgrade: PASS, 1 test, 0 skipped;
- retained isolated schemas: `primehr_phase31_fresh_20260826` and `primehr_phase31_upgrade_20260826`;
- the populated V5 Position Profile survived unchanged and the new V6 tables began empty.

Real PostgreSQL was not run, as approved by the user's SQL Server-primary direction. PostgreSQL portability is covered by an equivalent V6 migration, PostgreSQL-mode H2 Flyway execution, structural parity tests, and provider-neutral Java/JPA. A live PostgreSQL V6 run remains explicitly unverified and non-blocking.

## Audit and known limitations

- No new password, token, private key, API key, or connection secret was introduced.
- Existing tracked legacy configuration secret material was not copied or expanded.
- No native/provider-specific SQL was added to shared Java.
- No Phase 3.2 rating/evidence persistence or execution endpoint and no Phase 3.3 person-profile behavior exists.
- `Common` still reports zero executed tests because its pre-existing old Surefire provider does not discover its JUnit 5 test. The Phase 3.1 affected modules all use Surefire 3.2.5 and executed their tests.
- The two isolated SQL Server schemas are retained for independent inspection and may be removed manually after review.

## Checkpoint

Every Phase 3.1 gate passes under the approved SQL Server-primary policy. Phase 3.2 may begin. Phase 3.3, Phase 3.4, and Phase 4 remain out of scope until their respective gates and approvals are satisfied.
