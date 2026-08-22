# ISOFT PRIME-HRM Phase 2.1 - Position Profile Draft Foundation

Status: Complete under the user-approved SQL Server-primary acceptance policy

Updated: 2026-08-13

## Scope and boundary

Phase 2.1 implements the Administrative position-target read contract and the PrimeHR draft persistence/API foundation approved in `PHASE_2_POSITION_COMPETENCY_PROFILES_SCOPE_APPROVAL.md`. It does not implement Submit, Return, Approve, ACTIVE profile resolution, profile comparison, UI pages, employee/person profiles, reports, or HRISApp assembly.

Successor drafts are allowed only from ACTIVE profiles. Because activation belongs to Phase 2.2, the Phase 2.1 version endpoint correctly rejects DRAFT and ARCHIVED sources until the lifecycle exists.

## Administrative reference ownership

Administrative remains the source of truth for Job Position and Plantilla. It now exposes authenticated, paginated, read-only contracts:

- `GET /api/integration/v1/primehr/position-targets`
- `GET /api/integration/v1/primehr/position-targets/{type}/{id}`

The response provides exact source IDs, the Plantilla parent Job Position, names, salary grade/step, a deterministic SHA-256 fingerprint, and retrieval time. Existing Job Position and Plantilla CRUD contracts are unchanged. The dedicated `primehr.position-profile` feature is enforced server-side; administrator behavior remains compatible.

## PrimeHR draft foundation

PrimeHR calls Administrative with the caller's bearer token and fails closed for denied or unavailable dependencies. It stores source identifiers and a refreshable draft snapshot, never an editable copy of the Administrative master.

The Phase 2.1 endpoints under `/api/primehr/v1/admin/position-profiles` are:

- list and detail;
- create/update/archive a draft;
- request a successor version (ACTIVE source only);
- add/update/archive a draft competency requirement.

Every command uses server-resolved agency scope, exact action permission, optimistic root/child versions, a transaction, and append-only audit events. Requirements reference an exact ACTIVE competency version and an active/effective level from that competency's exact ACTIVE proficiency-scale version. Classification is exactly `MANDATORY` or `DESIRABLE`.

## Persistence and portability

Equivalent V4 migrations add:

- `prime_position_profile`;
- `prime_position_profile_requirement`;
- target/version and requirement uniqueness constraints;
- target identity, status, effectivity, classification, and display-order checks;
- self-version, competency, proficiency-level, and profile foreign keys;
- `ix_prime_profile_filter`, `ix_prime_profile_target_chain`, and `ix_prime_profile_requirement_order`.

Provider-specific DDL is isolated in the PostgreSQL and SQL Server Flyway scripts. Shared Java persistence uses JPA/Spring Data and contains no provider-specific SQL.

## Contract and test coverage

OpenAPI contracts:

- `contracts/openapi/primehr-v1.yaml`;
- `contracts/openapi/administrative-primehr-integration-v1.yaml`.

Focused coverage verifies Administrative authorization/pagination/parent resolution/fingerprints; PrimeHR permissions and fail-closed lifecycle behavior; draft validation, audit, optimistic conflict, exact competency-scale-level relationships, rollback on invalid input, effective-date revalidation, migrations, controller authorization, and absence of later-phase endpoints.

## Verification results

```text
.\mvnw.cmd -pl Administrative,PrimeHR -am test
BUILD SUCCESS

.\mvnw.cmd -pl Administrative,PrimeHR -am clean package
BUILD SUCCESS
Administrative: 25 tests, 0 failures, 0 errors, 0 skipped
PrimeHR: 64 tests, 0 failures, 0 errors, 0 skipped
Common: 0 tests (pre-existing module coverage gap)

git diff --check
PASS (line-ending conversion warnings only)
```

Real SQL Server 14.0:

- fresh V1-V4: PASS, 8 tests, retained schema `primehr_phase21_sql_20260813_fresh`;
- populated V3-to-V4: PASS, 1 test, retained schema `primehr_phase21_sql_20260813_upgrade`;
- Flyway reached V4, Hibernate validation passed, repository behavior passed, and the populated V3 competency row remained intact.

Real PostgreSQL:

- not executed successfully;
- the attempted datasource resolved to the literal placeholder `${HRIS_POSTGRES_URL}` and failed before connecting;
- no HRIS PostgreSQL variables exist in the Process, User, or Machine environment scopes;
- local PostgreSQL port 5432 is unavailable;
- no PostgreSQL validation schema was created.

This is an environment/credential limitation, not evidence that the V4 PostgreSQL migration was executed. The user explicitly waived a live PostgreSQL gate on 2026-08-13. Database portability remains mandatory through provider-neutral Java/JPA, equivalent PostgreSQL and SQL Server migrations, H2 PostgreSQL-mode tests, and migration-parity tests.

## Required PostgreSQL rerun

Provide these locally without committing or printing them:

```text
HRIS_POSTGRES_URL
HRIS_POSTGRES_USERNAME
HRIS_POSTGRES_PASSWORD
```

These commands remain recommended when a PostgreSQL environment is available, but they are no longer a prerequisite for Phase 2.2 under the approved SQL Server-primary acceptance policy.

## Checkpoint

Phase 2.1 application code, SQL Server, tests, packaging, authorization, source ownership, provider-neutral design, migration parity, and phase-boundary checks pass. Phase 2.1 is complete. Live PostgreSQL execution remains an explicitly recorded unverified limitation rather than a blocking gate.
