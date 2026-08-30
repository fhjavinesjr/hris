# ISOFT PRIME-HRM Phase 5B.1/5B.2 Backend Review Manifest

Prepared: 2026-08-30

Status: Phase 5B.1 and Phase 5B.2 backend gates passed. Phase 5B.3 Administrative controls, public/applicant/staff UI, and Playwright acceptance have not been implemented and require separate approval.

## 1. Reviewed scope and boundary

This manifest covers the approved backend-only slices in `PHASE_5B_APPLICANT_PORTAL_APPLICATION_SCOPE_APPROVAL.md`:

- Phase 5B.1: separate applicant identity and token boundary, privacy/consent, applicant profile, private document storage, and public vacancy reads;
- Phase 5B.2: DRAFT/SUBMITTED/WITHDRAWN application intake, immutable submission evidence, acknowledgments, applicant communications, and staff read/message APIs;
- provider-equivalent V13 and V14 migrations, OpenAPI, authorization, audit, and automated tests.

It intentionally does not contain:

- Administrative UI permission controls;
- Careers/applicant or staff intake UI routes;
- Playwright acceptance;
- screening, completeness decisions, QS matching, qualified/disqualified decisions, scoring, ranking, shortlist, interview, selection, appointment handoff, employee creation, or onboarding;
- a Phase 5B Jasper report.

## 2. Requirement-to-implementation map

| Requirement | Implementing files |
|---|---|
| Separate applicant account and JWT audience | `ApplicantAccount`, `ApplicantTokenService`, `ApplicantJwtAuthenticationFilter`, `PrimeHrSecurityConfiguration`, applicant session/public controllers |
| Password hashing, lockout, and enumeration-safe login | `ApplicantFoundationServiceImpl`, `ApplicantAccountRepository`, account/security integration tests |
| Versioned privacy notice and exact consent | `PrivacyNotice`, `ApplicantConsent`, repositories, foundation service/controllers, V13 migrations |
| Applicant-owned profile and PDS/WES-compatible entries | `ApplicantProfile`, `ApplicantProfileEntry`, repositories, DTOs, foundation service/controllers |
| Private provider-abstracted documents | `DocumentStorage`, `LocalDocumentStorage`, `S3DocumentStorage`, `ApplicantDocument`, secured content APIs |
| Open published vacancy reads | `PublicApplicantController`, Phase 5A publication repository/service integration |
| DRAFT/SUBMITTED/WITHDRAWN intake | `PositionApplication`, `ApplicantApplicationServiceImpl`, applicant application controller |
| Immutable submission snapshots and document manifest | `PositionApplication`, `ApplicationDocumentSnapshot`, V14 migration, lifecycle integration tests |
| Acknowledgment and safe applicant status | `PositionApplication`, application DTOs/service, V14 constraints/indexes |
| Portal communication history | `ApplicantCommunication`, repository, applicant/staff APIs |
| Staff read and informational-message authorization | `RspApplicantIntakePermissionGuard`, `RspApplicantIntakeController`, guard/integration tests |
| Stable REST contract and errors | `ApplicationDtos`, `ApplicantDtos`, controllers, `ApplicationConflictException`, `PrimeHrExceptionHandler`, `contracts/openapi/primehr-v1.yaml` |
| SQL Server/PostgreSQL portability | provider-specific V13/V14 migrations, JPA repositories, migration parity/provider tests |

## 3. Package/module structure

All new runtime behavior is inside the existing `PrimeHR` Maven module:

```text
com.primehr.rsp.applicant
  api             explicit applicant/public/staff DTOs and controllers
  application     transactional business services
  domain          JPA aggregates and immutable evidence entities
  infrastructure  Spring Data repositories
  storage         local/S3-compatible DocumentStorage adapters
com.primehr.security
  applicant token/filter and staff intake permission guard
com.primehr.shared.exception
  stable application-conflict mapping
```

No new dependency from PrimeHR into Administrative or HumanResource persistence was added. Existing Phase 5A service/API boundaries remain authoritative.

## 4. Created Phase 5B runtime files

### API and application

- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/ApplicantDtos.java` - explicit account, profile, consent, document, and public-vacancy contracts.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/ApplicationDtos.java` - application, manifest, communication, submit, withdraw, and staff contracts.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/PublicApplicantController.java` - public privacy, vacancy, and account registration endpoints.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/ApplicantSessionController.java` - applicant login/logout boundary.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/ApplicantSelfServiceController.java` - owning-account profile, consent, and document endpoints.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/ApplicantApplicationController.java` - owning-applicant application lifecycle endpoints.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/api/RspApplicantIntakeController.java` - authorized staff read/document/message endpoints.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/application/ApplicantFoundationService.java` and `ApplicantFoundationServiceImpl.java` - Phase 5B.1 rules and transactions.
- `PrimeHR/src/main/java/com/primehr/rsp/applicant/application/ApplicantApplicationService.java` and `ApplicantApplicationServiceImpl.java` - Phase 5B.2 application/snapshot/communication rules.

### Domain and repositories

- `ApplicantAccount`, `PrivacyNotice`, `ApplicantConsent`, `ApplicantProfile`, `ApplicantProfileEntry`, and `ApplicantDocument` - Phase 5B.1 aggregates.
- `PositionApplication`, `ApplicationDocumentSnapshot`, and `ApplicantCommunication` - Phase 5B.2 lifecycle and immutable evidence.
- `ApplicantAccountRepository`, `PrivacyNoticeRepository`, `ApplicantConsentRepository`, `ApplicantProfileRepository`, `ApplicantProfileEntryRepository`, and `ApplicantDocumentRepository` - Phase 5B.1 persistence.
- `PositionApplicationRepository`, `ApplicationDocumentSnapshotRepository`, and `ApplicantCommunicationRepository` - Phase 5B.2 persistence.

### Storage and security

- `DocumentStorage`, `LocalDocumentStorage`, and `S3DocumentStorage` - provider abstraction, path-safe local bytes, and private S3-compatible storage.
- `ApplicantTokenService` and `ApplicantJwtAuthenticationFilter` - distinct applicant JWT audience/subject.
- `RspApplicantIntakePermissionGuard` - staff `primehr.rsp-applicant-intake` ACCESS/ADD and agency-wide enforcement.
- `ApplicationConflictException` - stable HTTP 409 optimistic/application conflict.

## 5. Modified Phase 5B files

- `PrimeHR/pom.xml` - password/security, storage, and test dependencies needed by the approved foundation.
- `PrimeHR/src/main/java/com/primehr/config/PrimeHrProperties.java` - applicant JWT, lockout, upload/storage, required-document, and reapplication configuration.
- `PrimeHR/src/main/java/com/primehr/security/PrimeHrSecurityConfiguration.java` - separate public, applicant, and employee/staff security chains.
- `PrimeHR/src/main/java/com/primehr/shared/exception/PrimeHrExceptionHandler.java` - safe applicant/application error mappings.
- `PrimeHR/src/main/resources/application.properties` - visible local defaults and environment-backed applicant/storage settings.
- `PrimeHR/src/test/resources/application-test.properties` - synthetic, isolated test configuration.
- `PrimeHR/src/test/java/com/primehr/contract/PrimeHrOpenApiContractTest.java` - Phase 5B route/security/boundary assertions.
- `PrimeHR/src/test/java/com/primehr/migration/AbstractPrimeHrProviderIntegration.java` - V14 schema/index/FK assertions.
- `PrimeHR/src/test/java/com/primehr/migration/CompetencyMigrationParityTest.java` - provider structural parity through V14.
- `contracts/openapi/primehr-v1.yaml` - public, applicant, and staff application contracts; contract version `5.5.0-phase-5b.2`.
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md` - truthful Phase 5B.1/5B.2 status and next gate.
- `docs/prime-hrm/PHASE_5B_APPLICANT_PORTAL_APPLICATION_SCOPE_APPROVAL.md` - approval/implementation status only; the approved scope remains unchanged.

The working tree also contains earlier Phase 5A Administrative, HumanResource, report, contract, guide, and documentation changes. They were preserved and were not rewritten as part of Phase 5B. `.idea/compiler.xml` is an unrelated user/IDE change and must not be attributed to or committed as Phase 5B work.

## 6. Database schema

### V13 applicant foundation

Tables:

- `rsp_applicant_account`;
- `rsp_privacy_notice`;
- `rsp_applicant_consent`;
- `rsp_applicant_profile`;
- `rsp_applicant_profile_entry`;
- `rsp_applicant_document`.

Important constraints and relationships:

- agency + normalized applicant email and agency + privacy definition version are unique;
- one profile per agency/applicant;
- one consent per applicant/privacy notice;
- profile entries are typed and ordered; invalid types/date ranges/orders are rejected;
- consent/profile/document rows reference the owning applicant;
- document replacement is a self-reference and storage object keys are unique;
- account, notice, document scan, size, and lifecycle values are check-constrained;
- indexes support effective notices, applicant consent, ordered profile entries, and active owner documents.

### V14 application intake

Tables:

- `rsp_position_application`;
- `rsp_application_document_snapshot`;
- `rsp_applicant_communication`.

Important constraints and relationships:

- application references the applicant, exact vacancy publication, and accepted privacy notice;
- agency/applicant/publication/application-version is unique;
- lifecycle is restricted to DRAFT, SUBMITTED, and WITHDRAWN, with consistent acknowledgment/submission/withdrawal metadata;
- acknowledgment is unique when present;
- document snapshots reference both the application and source applicant document and cannot duplicate a source document in one application;
- communications reference application and applicant and are restricted to PORTAL system/staff-to-applicant messages;
- indexes support owner history, staff queue, vacancy applications, ordered evidence, and communication history.

## 7. Migration files

- PostgreSQL: `V13__rsp_applicant_foundation.sql`, `V14__rsp_application_intake.sql`.
- SQL Server: `V13__rsp_applicant_foundation.sql`, `V14__rsp_application_intake.sql`.

Shared Java contains no native SQL. SQL Server uses a filtered acknowledgment index because its nullable-unique behavior requires it. PostgreSQL uses a normal unique index because PostgreSQL permits multiple null values; this also keeps the PostgreSQL-mode H2 gate executable. Other DDL differences are isolated in the provider migration directories.

## 8. REST endpoints and authorization

Public/unauthenticated:

- current privacy notice;
- public vacancy list/detail;
- applicant registration and session creation.

Applicant JWT only, with identity derived from the token:

- logout, account read/update;
- profile read/update and consent acceptance;
- document list/upload/download/replace/deactivate;
- application list/create/detail/draft update/submit/withdraw;
- application communication history.

Employee/staff JWT only:

- application list/detail and evidence stream require `primehr.rsp-applicant-intake` ACCESS plus AGENCY_WIDE unless administrator;
- sending an informational portal message additionally requires ADD;
- every sensitive evidence read and staff message is audited.

Applicant tokens cannot authorize `/api/primehr/v1/**`; employee tokens cannot authorize `/api/primehr/applicant/v1/**`. Ownership failures return a non-disclosing not-found response.

## 9. Tests and what they verify

- `ApplicantFoundationIntegrationTest` - registration/login/lockout, profile/consent, ownership, document lifecycle, validation, and configuration behavior.
- `ApplicantTokenIsolationTest` - applicant token audience/subject and employee-management isolation.
- `ApplicantHttpSecurityIsolationTest` - actual HTTP filter-chain rejection in both directions, including application/staff routes.
- `LocalDocumentStorageTest` - root confinement, traversal prevention, byte round trip, and fail-closed configuration.
- `S3DocumentStorageTest` - private S3-compatible adapter behavior and missing-configuration failure.
- `ApplicantApplicationIntegrationTest` - complete lifecycle, open-window checks, readiness, duplicate/ownership rules, optimistic conflict, immutable profile/vacancy/document evidence, withdrawal/idempotency, staff message, evidence stream, and audit.
- `RspApplicantIntakePermissionGuardTest` - ACCESS versus ADD, AGENCY_WIDE, and administrator compatibility.
- `PrimeHrV12ToV13UpgradeIT` - populated V12-to-V13 preservation/creation.
- `PrimeHrV13ToV14UpgradeIT` - populated V13-to-V14 preservation/creation.
- `PrimeHrSqlServerSchemaIT` / `AbstractPrimeHrProviderIntegration` - real-provider migration, Hibernate validation, tables, constraints, indexes, and relationships.
- `CompetencyMigrationParityTest` - SQL Server/PostgreSQL structural parity through V14.
- `PrimeHrFlywaySchemaIntegrationTest` - PostgreSQL-mode V1-V14 Flyway and Hibernate schema validation.
- `PrimeHrOpenApiContractTest` - exact routes/security plus explicit Phase 5C exclusion.

## 10. Exact verification results

| Command/gate | Result |
|---|---|
| Phase 5B.1 focused suite | 32 tests; 0 failures, 0 errors, 0 skipped |
| `mvn -pl PrimeHR clean package` after Phase 5B.1 | 158 tests; 0 failures, 0 errors, 0 skipped; JAR packaged |
| Phase 5B.2 focused command: `mvn -pl PrimeHR test "-Dtest=ApplicantApplicationIntegrationTest,ApplicantHttpSecurityIsolationTest,RspApplicantIntakePermissionGuardTest,PrimeHrOpenApiContractTest,CompetencyMigrationParityTest"` | 28 tests; 0 failures, 0 errors, 0 skipped |
| `mvn -pl PrimeHR test "-Dtest=PrimeHrFlywaySchemaIntegrationTest"` after PostgreSQL index correction | 9 tests; 0 failures, 0 errors, 0 skipped |
| Final `mvn -pl PrimeHR clean package` | 164 tests; 0 failures, 0 errors, 0 skipped; BUILD SUCCESS; executable JAR packaged |
| `git diff --check` | passed; only Git LF-to-CRLF conversion warnings |
| shared-code provider audit | no native SQL or SQL Server/PostgreSQL-specific query construct found |
| Phase 5C boundary audit | no Phase 5C decision/status/action implementation found |

The first full Phase 5B.2 package run executed 164 tests but had 9 context errors because H2 PostgreSQL mode did not accept the unnecessary PostgreSQL partial-index predicate. The PostgreSQL migration was corrected to an equivalent normal unique index; the failed test and the complete package were rerun successfully. This failure is intentionally disclosed.

## 11. Real database evidence

### SQL Server

Tested against a real local SQL Server instance:

- fresh V1-to-V13 in `primehr_phase5b1_fresh`;
- populated V12-to-V13 in `primehr_phase5b1_upgrade`;
- fresh V1-to-V14 in `primehr_phase5b2_fresh2`: 9 tests, 0 skipped;
- populated V13-to-V14 in `primehr_phase5b2_upgrade`: 1 test, 0 skipped and pre-existing applicant preserved.

An initial Phase 5B.2 attempt used `primehr_phase5b2_fresh` but omitted the Flyway schema placeholder override, so it failed immediately against an existing default-schema V1 table. It did not apply V14 to the default schema. The isolated schema/history may remain and is not deleted automatically. The corrected run supplied Flyway default schema, schemas, migration placeholder, and Hibernate default schema explicitly.

### PostgreSQL

No real PostgreSQL instance was tested, by the established SQL Server-primary project policy. PostgreSQL evidence is provider-equivalent V13/V14 DDL, structural parity tests, PostgreSQL-mode H2 Flyway V1-V14, Hibernate validation, and provider-neutral JPA/shared logic. A live PostgreSQL run remains unverified and non-blocking under the user's direction.

## 12. Mocked, assumed, deferred, or unverified behavior

- S3-compatible behavior is unit-tested with a mocked client; no real S3/MinIO bucket was used.
- Local storage is tested with temporary directories, not a production durable volume.
- Email verification, password-reset delivery, CAPTCHA, notification delivery, external antivirus, and federated login are deferred.
- Self-registration activates accounts because no verified mail provider exists.
- Staff data scope is agency-wide until an authoritative narrower recruitment assignment source exists.
- Required document types, privacy text, retention, storage credentials, limits, and production rate policy still require deployment/client configuration.
- No browser/UI/Playwright acceptance has run because that work is Phase 5B.3.

## 13. Secret and generated-file audit

No new real password, token, private key, bucket, or access key was found. Applicant JWT configuration is environment-backed and blank by default; tests use clearly synthetic secrets. The repository still contains pre-existing visible local values `primehr.security.jwt-secret=secret` and SQL Server password `sa`; they were not introduced by Phase 5B and remain a production-hardening concern. `target/` is ignored and not part of the commit.

## 14. Known risks and unresolved decisions

1. Live PostgreSQL remains unverified.
2. Production applicant JWT secret, storage provider, durable root/bucket, privacy notice, retention, file policy, and rate policy must be configured before deployment.
3. Account activation without verified email and lack of CAPTCHA increase public-registration abuse risk.
4. No real malware scanner is integrated; metadata has scan state but deployment must not claim antivirus coverage.
5. V13/V14 are forward-only. Do not edit them after deployment; use a reviewed successor migration.
6. Phase 5B.3 must add Administrative controls and the separate applicant/staff UI without weakening backend authorization.

## 15. Review conclusion and next gate

Phase 5B.1 and Phase 5B.2 satisfy their approved backend gates under the SQL Server-primary policy. The next permitted step is a separate user approval for Phase 5B.3. Stop before Administrative controls, public/applicant/staff UI, or Playwright implementation.
