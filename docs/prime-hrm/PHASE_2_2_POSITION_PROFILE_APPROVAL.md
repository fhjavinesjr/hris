# ISOFT PRIME-HRM Phase 2.2 - Position Profile Approval Backend

Status: Complete under the user-approved SQL Server-primary acceptance policy

Updated: 2026-08-13

## Scope and boundary

Phase 2.2 implements only the backend submission, approval, effective resolution, exact-version comparison, authorization, audit, and persistence changes approved in `PHASE_2_POSITION_COMPETENCY_PROFILES_SCOPE_APPROVAL.md`. It remains aligned with Master Plan V2 Phase 2: position/Plantilla competency profiles, required levels, classification/criticality, effective dating, approval, comparison, and historical snapshots without duplicating the Administrative position master.

Phase 2.3 UI, employee/person profiles, assessments, gaps, RSP, reports, notifications, and HRISApp assembly are not implemented.

## Lifecycle and authorization

The implemented lifecycle is:

```text
DRAFT -> SUBMITTED -> ACTIVE
                  -> DRAFT (returned with reason)
```

- `canSubmit` and `canApprove` are independent Administrative permission flags.
- Every action also requires `canAccess`; missing legacy flags fail closed.
- Ordinary users cannot approve their own submission.
- An established administrator may self-approve only with an explicit reason; the override is separately audited.
- SUBMITTED and ACTIVE content is immutable. Corrections to ACTIVE content use a successor draft.
- Approval uses a pessimistic target-chain lock plus the request's optimistic `recordVersion`.
- A successor must supersede the latest ACTIVE version and begin after it; approval atomically closes the predecessor on the day before the successor starts.
- A target chain cannot have multiple unfinished DRAFT/SUBMITTED successor versions.

## REST contract

The following endpoints were added under `/api/primehr/v1/admin/position-profiles`:

- `POST /{id}/submit`
- `POST /{id}/return`
- `POST /{id}/approve`
- `GET /{id}/audit-events`
- `GET /resolve?jobPositionId=&plantillaId=&asOf=`
- `GET /compare?leftProfileId=&rightProfileId=`

Resolution selects an effective ACTIVE Plantilla profile first, then falls back to the effective ACTIVE Job Position profile. Comparison is restricted to two versions of the same target chain and uses exact stored competency-version IDs. It classifies requirements as added, removed, changed, or unchanged; changed means the required level, mandatory/desirable classification, or criticality changed.

## Persistence and database portability

Equivalent forward V5 migrations add `submitted_by`, `submitted_at`, `approved_by`, and `approved_at`, a lifecycle-metadata check constraint, and `ix_prime_profile_effective_resolution`:

- `PrimeHR/src/main/resources/db/migration/postgresql/V5__position_profile_approval_lifecycle.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V5__position_profile_approval_lifecycle.sql`

Shared application queries use Spring Data JPA, JPQL, Criteria Specifications, `Pageable`, and JPA pessimistic locking. No provider-specific SQL was added to shared Java code. Provider-specific DDL remains isolated in paired Flyway folders. Automated migration-parity tests require the same V5 logical columns, constraint, and index in both providers.

SQL Server requires a Flyway-recognized `GO` batch boundary between adding lifecycle columns and compiling the check constraint. The first live V5 attempt exposed this SQL Server compilation rule; the migration was corrected before the successful fresh and upgrade gates. PostgreSQL uses separate portable `ADD COLUMN` statements and passes the PostgreSQL-mode Flyway harness.

## Automated behavior coverage

Focused and full tests cover:

- legacy permission JSON failing closed for Submit/Approve;
- independent access, CRUD, Submit, and Approve enforcement plus administrator compatibility;
- authenticated controller routing and trusted server-derived agency/administrator context;
- complete submission and independent approval metadata/audit;
- returned submission reason, unlocked draft, and resubmission;
- rejection of missing requirements and invalid exact competency/level effectivity;
- ordinary self-approval denial and reason-required administrator override;
- SUBMITTED/ACTIVE immutability;
- stale approval rejection without predecessor or audit mutation;
- successor chain validation and atomic predecessor closure;
- Plantilla-over-Job Position resolution and date effectivity;
- added, removed, changed, and unchanged exact-version comparison;
- V5 PostgreSQL/SQL Server migration parity and OpenAPI parsing.

## Verification results

```text
.\mvnw.cmd -pl Administrative,PrimeHR -am test -DskipTests=false
BUILD SUCCESS
Administrative: 26 tests, 0 failures, 0 errors, 0 skipped
PrimeHR: 73 tests, 0 failures, 0 errors, 0 skipped
Common: 0 tests (pre-existing coverage gap)

Focused position-profile lifecycle suite
11 tests, 0 failures, 0 errors, 0 skipped

.\mvnw.cmd -pl Administrative,PrimeHR -am package -DskipTests=false
BUILD SUCCESS

git diff --check
PASS (line-ending conversion warnings only)
```

Real SQL Server 14.0:

- fresh V1-V5: PASS, 9 tests, retained schema `primehr_phase22_sql_20260813_fresh2`;
- populated V4-to-V5: PASS, 1 test, retained schema `primehr_phase22_sql_20260813_upgrade`;
- Flyway reached V5, Hibernate schema validation passed, lifecycle timestamps/status and provider-neutral effective-resolution query passed, and the populated V4 draft remained unchanged with null lifecycle metadata.

The first fresh attempt retained schema `primehr_phase22_sql_20260813_fresh` at V4 after SQL Server rolled back the failed V5 batch. It contains test-only schema objects and no production data; it was not deleted because destructive cleanup was not authorized.

Real PostgreSQL was not run, as explicitly directed by the user. This is recorded as unverified rather than implied to have passed. Portability gates that did run are paired V5 migration review, migration-parity tests, PostgreSQL-mode Flyway/Hibernate tests, and provider-neutral shared Java persistence.

## Remaining manual/UI work

No Phase 2.3 UI exists yet, so browser submit/return/approve/comparison acceptance is not applicable at this checkpoint. Phase 2.3 must add Administrative permission controls and the PrimeHR position-profile UI, then run strict frontend builds and the manual allowed/denied/lifecycle/conflict/history matrix.

## Checkpoint

Phase 2.2 backend behavior, authorization, audit, contracts, automated tests, SQL Server fresh migration, populated SQL Server upgrade, and portability design gates pass. Phase 2.2 is complete. Phase 2.3 requires explicit approval before implementation.
