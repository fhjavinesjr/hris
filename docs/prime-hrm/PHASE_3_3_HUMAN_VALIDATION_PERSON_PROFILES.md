# ISOFT PRIME-HRM Phase 3.3 - Human Validation and Immutable Person Profiles

Status: Complete under the user-approved SQL Server-primary acceptance policy

Updated: 2026-08-28

## Scope and boundary

Phase 3.3 implements the backend-only checkpoint approved in `PHASE_3_COMPETENCY_ASSESSMENT_PERSON_PROFILES_SCOPE_APPROVAL.md`: independent human validation of submitted assessment cases, explicit final competency decisions, administrator override with a mandatory reason, and atomic creation of immutable person competency profile versions.

This checkpoint also provides latest-as-of, history, and exact-version profile reads with ownership/data-scope enforcement. It does not implement Phase 3.4 Administrative controls, PrimeHR browser pages, Playwright fixtures, reports, notifications, competency gaps, L&D referrals, or any Phase 4 behavior.

## Validation and profile rules

- Only a `FOR_VALIDATION` case whose active assessor assignments are all `SUBMITTED` can be validated.
- The validator selects one final attained level for every exact competency requirement. Contributor ratings remain distinguishable and are not averaged or automatically promoted to an official result.
- Every active contributor and its expected record version must be supplied exactly once. Stale, missing, duplicate, or extra contribution versions reject the transaction.
- Every final decision identifies the exact contributing assignments, and those IDs must match the case's active submitted assignments.
- The final level must belong to the exact published proficiency scale used by the competency.
- An ordinary validator cannot validate a case to which that validator contributed. An administrator may override this separation-of-duties rule only when the request explicitly selects administrator override and supplies a non-blank reason.
- Validation is transactional: the validation record, validated decisions, person profile, person results, predecessor closure, assignment/case transitions, and audit either all succeed or all roll back.
- Each successfully validated case creates exactly one immutable `VALIDATED` person-profile version. No edit, delete, or unvalidate endpoint exists.
- A successor links to the prior version and closes the predecessor on the day before the successor's `validFrom`. The successor must begin after the predecessor's `validFrom`.
- Latest-as-of selection is deterministic by effectivity, profile version, and validation time. History remains append-only and queryable.
- `/me` derives the employee number from the authenticated identity. Employee-number and exact-version routes permit another employee's data only with `AGENCY_WIDE` person-profile scope.

## API delivered

Validation:

- `GET /api/primehr/v1/validation/assessment-cases`
- `GET /api/primehr/v1/validation/assessment-cases/{caseId}`
- `POST /api/primehr/v1/validation/assessment-cases/{caseId}/validate`
- Existing `POST /api/primehr/v1/validation/assessment-cases/{caseId}/return` remains available from Phase 3.2.

Person profiles:

- `GET /api/primehr/v1/person-profiles/me`
- `GET /api/primehr/v1/person-profiles/me/history`
- `GET /api/primehr/v1/person-profiles/employees/{employeeNo}`
- `GET /api/primehr/v1/person-profiles/employees/{employeeNo}/history`
- `GET /api/primehr/v1/person-profiles/versions/{profileVersionId}`

The authoritative contract is `contracts/openapi/primehr-v1.yaml`, version `3.3.0-phase-3.3`. Validation mutations require `primehr.assessment-validation` Access + Validate with `AGENCY_WIDE`. Person-profile reads require `primehr.person-profile` Access and are constrained by `OWN_RECORDS` or `AGENCY_WIDE` as applicable.

## Persistence and portability

Equivalent forward-only V8 migrations are isolated at:

- `PrimeHR/src/main/resources/db/migration/sqlserver/V8__assessment_validation_person_profiles.sql`
- `PrimeHR/src/main/resources/db/migration/postgresql/V8__assessment_validation_person_profiles.sql`

V8 adds:

- `prime_assessment_validation`: one validation per assessment case, validator/override metadata, reason constraint, audit, and optimistic version;
- `prime_assessment_validated_rating`: one human final decision per validation and competency, exact attained level, remarks, and contributing assignment IDs;
- `prime_person_competency_profile`: one immutable version per validated case, unique employee/version lineage, source snapshots, effectivity, predecessor relationship, and `VALIDATED`-only constraint;
- `prime_person_competency_result`: one exact competency/result per profile linked to its validated decision.

Indexes support validation-time reads, deterministic latest-profile selection, and profile-result retrieval. Foreign keys preserve case, validation, predecessor, competency, proficiency-level, profile, and validated-decision relationships. SQL Server uses its native `BIT`, `DATETIMEOFFSET`, and `NVARCHAR` DDL; PostgreSQL uses `BOOLEAN`, `TIMESTAMP WITH TIME ZONE`, and `VARCHAR`. Those differences are isolated in provider migration folders.

Shared Java uses JPA entities, Spring Data derived queries, JPQL, `Pageable`, Java time types, and transactional service logic. No native SQL, provider branch, SQL Server-only query, or PostgreSQL-only query was added to shared application code.

## Main implementation files

- `PrimeHR/.../assessment/api/AssessmentValidationController.java`, `AssessmentValidationDtos.java`, and `PersonCompetencyProfileController.java` define the typed HTTP boundary.
- `PrimeHR/.../assessment/application/AssessmentValidationService.java` and `AssessmentValidationServiceImpl.java` enforce validation, separation of duties, concurrency, atomic profile generation, predecessor closure, ownership, and mapping.
- `PrimeHR/.../assessment/domain/AssessmentValidation.java` and `AssessmentValidatedRating.java` preserve immutable human validation decisions.
- `PrimeHR/.../assessment/domain/PersonCompetencyProfile.java` and `PersonCompetencyResult.java` preserve versioned official results; `AssessmentCase` and `AssessorAssignment` gained controlled validation transitions.
- `PrimeHR/.../assessment/infrastructure/AssessmentValidationRepository.java`, `AssessmentValidatedRatingRepository.java`, `PersonCompetencyProfileRepository.java`, `PersonCompetencyResultRepository.java`, and the updated `AssessmentRatingRepository.java` provide provider-neutral persistence.
- `contracts/openapi/primehr-v1.yaml` records Phase 3.3 requests, responses, errors, and routes.
- `PrimeHR/.../migration/PrimeHrV7ToV8UpgradeIT.java`, provider/parity tests, OpenAPI tests, and `AssessmentExecutionServiceIntegrationTest.java` cover the new checkpoint.

Phase 3.1 and 3.2 remain part of the same uncommitted backend change set and are documented separately in `PHASE_3_1_ASSESSMENT_DRAFT_FOUNDATION.md` and `PHASE_3_2_ASSESSMENT_EXECUTION.md`.

## Automated verification

Focused Phase 3.3 service gate:

```text
mvn -pl PrimeHR -am "-Dtest=AssessmentExecutionServiceIntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
PASS: 5 tests, 0 failures, 0 errors, 0 skipped
```

Contract and migration parity coverage passed, including the Phase 3.3 OpenAPI contract, V8 logical structure, PostgreSQL-mode Flyway V1-V8, and Hibernate schema validation.

Full affected clean test:

```text
mvn -pl Administrative,HumanResource,PrimeHR -am clean test "-Ddebug=false" "-Dlogging.level.org.hibernate.SQL=INFO"
BUILD SUCCESS
Common: 3 tests, 0 failures, 0 errors, 0 skipped
Administrative: 33 tests, 0 failures, 0 errors, 0 skipped
HumanResource: 45 tests, 0 failures, 0 errors, 0 skipped
PrimeHR: 90 tests, 0 failures, 0 errors, 0 skipped
Total: 171 tests, 0 failures, 0 errors, 0 skipped
```

Full affected package:

```text
mvn -pl Administrative,HumanResource,PrimeHR -am clean package "-Ddebug=false" "-Dlogging.level.org.hibernate.SQL=INFO"
BUILD SUCCESS with the same 171 tests and zero skipped
```

The tests prove independent human decisions, contributor preservation, ordinary self-validation denial, mandatory audited administrator-override reason, stale/duplicate validation rejection without partial profile/audit creation, immutable profile generation, ownership/data-scope restrictions, successor linkage and predecessor closure, deterministic latest-as-of/history reads, OpenAPI boundaries, V8 migration equivalence, Flyway execution, and application wiring.

## Real-provider verification

Real Microsoft SQL Server 14.0:

- fresh V1-to-V8 migration/provider suite: PASS, 9 tests, 0 skipped, isolated retained schema `primehr_phase33_fresh_20260827`;
- populated V7-to-V8 upgrade: PASS, 1 test, 0 skipped, isolated retained schema `primehr_phase33_upgrade_20260827`;
- the seeded V7 assessment cycle survived unchanged and all four new V8 tables began empty;
- Hibernate validation and the V8 constraints, foreign keys, and indexes passed against SQL Server.

Real PostgreSQL was not run, by user direction and the approved SQL Server-primary policy. PostgreSQL portability is covered by the equivalent V8 migration, successful PostgreSQL-mode H2 Flyway V1-V8 execution, structural migration-parity tests, and provider-neutral Java/JPA. A live PostgreSQL V8 fresh/upgrade run remains unverified and non-blocking; this is not represented as real-provider validation.

## Repository, secret, and boundary audit

- `git diff --check`: PASS; only Git's existing LF-to-CRLF conversion warnings were emitted.
- New Phase 3.3 source, migration, test, and contract files contain no credential, private-key, or token value.
- Maven `target/` output remains ignored and no generated/IDE/binary artifact was added to the change set.
- Shared Phase 3 Java contains JPQL/derived repository queries only. Provider-specific DDL remains isolated under `db/migration/sqlserver` and `db/migration/postgresql`.
- Existing application configuration caveats elsewhere in the repository are not hidden, but Phase 3.3 introduces no new secret.
- No Phase 3.4 UI/Playwright implementation and no Phase 4 gap, L&D, recommendation, notification, or reporting behavior was added.

## Known limitations and next gate

- Phase 3.3 is backend/API only. End-user validation and person-profile browser workflows belong to Phase 3.4.
- Supervisor authority remains explicit assessor assignment; no unverified organization hierarchy is inferred.
- Structured evidence remains metadata only; binary evidence storage is outside the approved scope.
- Live PostgreSQL V8 remains unverified as disclosed above.
- Phase 3.1 through 3.3 currently form one uncommitted backend change set and should be reviewed/committed together unless deliberately separated.

All approved Phase 3.3 gates pass. Stop here. Phase 3.4 requires explicit authorization before Administrative permission UI, PrimeHR pages, Playwright acceptance, user-guide updates, or review-manifest work begins.
