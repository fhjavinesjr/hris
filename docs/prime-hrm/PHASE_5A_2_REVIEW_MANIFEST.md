# ISOFT PRIME-HRM Phase 5A.1/5A.2 Backend Review Manifest

Prepared: 2026-08-29

Status: Phase 5A.1 and Phase 5A.2 backend implementation and gates passed. Work is intentionally stopped before Phase 5A.3 UI, Jasper, and Playwright acceptance.

## 1. Delivered requirements

| Requirement | Implementing files |
|---|---|
| Versioned authoritative Qualification Standards | `Administrative/.../QualificationStandard*`, `QualificationStandardServiceImpl`, `QualificationStandardController`, and both `phase5a1__qualification_standard.sql` scripts |
| Minimum Administrative RSP source contract | `RspPositionSourceIntegrationController`, `RspPositionSourceResponse`, `administrative-primehr-integration-v1.yaml` |
| Minimum HRM Plantilla occupancy contract | `HumanResource/.../integration/primehr/PlantillaOccupancy*`, `RspPlanningAuthorization`, `EmployeeAppointmentRepository`, `humanresource-primehr-integration-v1.yaml` |
| Recruitment plan and vacancy draft/readiness foundation | PrimeHR `rsp/domain`, `RspPlanningDtos`, `RspPlanningService*`, `RspPlanningController`, repositories, V11 migrations |
| Plan submission/return/approval and vacancy authority decisions | `RecruitmentPlan`, `VacancyRequest`, `RspPlanningServiceImpl`, `RspPlanningController` |
| Publication draft, channels, exact snapshots, approval and publication lifecycle | `VacancyPublication*`, `RspPublicationDtos`, `RspPublicationService*`, `RspPublicationController`, repositories, V12 migrations |
| RBAC and agency-wide scope | Administrative authorization mapping, `RspPlanningPermissionGuard`, `RspPublicationPermissionGuard`, `PrimeHrSecurityConfiguration` |
| Optimistic conflicts, SOD, administrator override, audit, and rollback | domain versions, service transition guards, `PrimeHrAuditService` calls, service/domain/permission tests |
| Dual-provider and API contract evidence | V11/V12 SQL Server and PostgreSQL migrations, migration tests, parity test, three OpenAPI contracts |

## 2. Created files and purpose

### Administrative

- `Administrative/src/main/java/com/administrative/controllers/QualificationStandardController.java` - CRUD/version publication HTTP boundary for Qualification Standards.
- `Administrative/src/main/java/com/administrative/controllers/RspPositionSourceIntegrationController.java` - authenticated minimum RSP position-source read.
- `Administrative/src/main/java/com/administrative/dtos/QualificationStandardDtos.java` - explicit requests and responses.
- `Administrative/src/main/java/com/administrative/dtos/RspPositionSourceResponse.java` - minimum cross-domain position, organization, QS, and fingerprint response.
- `Administrative/src/main/java/com/administrative/entitymodels/QualificationStandard.java` - versioned QS aggregate.
- `Administrative/src/main/java/com/administrative/entitymodels/QualificationStandardStatus.java` - controlled DRAFT/ACTIVE/ARCHIVED status.
- `Administrative/src/main/java/com/administrative/impl/QualificationStandardServiceImpl.java` - lifecycle, effectivity, predecessor closure, and source resolution.
- `Administrative/src/main/java/com/administrative/repositories/QualificationStandardRepository.java` - provider-neutral Spring Data persistence.
- `Administrative/src/main/java/com/administrative/services/QualificationStandardService.java` - service contract.
- `Administrative/src/main/resources/db/administrative-migration/postgresql/phase5a1__qualification_standard.sql` - PostgreSQL-equivalent QS schema.
- `Administrative/src/main/resources/db/administrative-migration/sqlserver/phase5a1__qualification_standard.sql` - SQL Server QS schema.
- `Administrative/src/test/java/com/administrative/entitymodels/QualificationStandardDomainTest.java` - validation, lifecycle, immutability, and successor behavior.

### HumanResource

- `HumanResource/src/main/java/com/humanresource/integration/primehr/PlantillaOccupancyIntegrationController.java` - protected occupancy endpoint.
- `HumanResource/src/main/java/com/humanresource/integration/primehr/PlantillaOccupancyIntegrationService.java` - integration service contract.
- `HumanResource/src/main/java/com/humanresource/integration/primehr/PlantillaOccupancyIntegrationServiceImpl.java` - authoritative active-appointment occupancy and fingerprint calculation.
- `HumanResource/src/main/java/com/humanresource/integration/primehr/PlantillaOccupancyResponse.java` - minimum occupancy response.
- `HumanResource/src/main/java/com/humanresource/integration/primehr/RspPlanningAuthorization.java` - fail-closed agency-wide Administrative authorization.
- `HumanResource/src/test/java/com/humanresource/integration/primehr/PlantillaOccupancyIntegrationServiceImplTest.java` - occupied/unoccupied facts and tenant/source behavior.

### PrimeHR integration, RSP, security, and persistence

- `PrimeHR/src/main/java/com/primehr/integration/administrative/AdministrativeRspPositionSource.java` and `AdministrativeRspPositionSourceClient.java` - typed Administrative source client.
- `PrimeHR/src/main/java/com/primehr/integration/humanresource/HumanResourcePlantillaOccupancy.java` and `HumanResourcePlantillaOccupancyClient.java` - typed HRM occupancy client.
- `PrimeHR/src/main/java/com/primehr/rsp/api/RspPlanningController.java` and `RspPlanningDtos.java` - planning/readiness HTTP contract and DTOs.
- `PrimeHR/src/main/java/com/primehr/rsp/api/RspPublicationController.java` and `RspPublicationDtos.java` - publication HTTP contract and DTOs.
- `PrimeHR/src/main/java/com/primehr/rsp/application/RspPlanningService.java` and `RspPlanningServiceImpl.java` - plan/vacancy rules, source checks, decisions, audit, and transactions.
- `PrimeHR/src/main/java/com/primehr/rsp/application/RspPublicationService.java` and `RspPublicationServiceImpl.java` - snapshot capture and controlled publication lifecycle.
- `PrimeHR/src/main/java/com/primehr/rsp/domain/RspAuditedEntity.java` - shared tenant/audit/version base.
- `PrimeHR/src/main/java/com/primehr/rsp/domain/RecruitmentPlan.java`, `RecruitmentPlanStatus.java` - plan aggregate and lifecycle.
- `PrimeHR/src/main/java/com/primehr/rsp/domain/VacancyRequest.java`, `VacancyRequestStatus.java`, `VacancyType.java` - vacancy/authority aggregate and actual-versus-anticipated rules.
- `PrimeHR/src/main/java/com/primehr/rsp/domain/VacancyPublication.java`, `VacancyPublicationStatus.java`, `VacancyVisibility.java` - publication aggregate and lifecycle.
- `PrimeHR/src/main/java/com/primehr/rsp/domain/VacancyPublicationChannel.java` - agency-configurable channel records.
- `PrimeHR/src/main/java/com/primehr/rsp/domain/VacancyPublicationRequirementSnapshot.java` - immutable competency requirement evidence.
- `PrimeHR/src/main/java/com/primehr/rsp/infrastructure/RecruitmentPlanRepository.java`, `VacancyRequestRepository.java`, `VacancyPublicationRepository.java`, `VacancyPublicationChannelRepository.java`, and `VacancyPublicationRequirementRepository.java` - tenant-scoped, provider-neutral persistence.
- `PrimeHR/src/main/java/com/primehr/security/RspPlanningPermissionGuard.java` and `RspPublicationPermissionGuard.java` - exact action and AGENCY_WIDE enforcement.
- `PrimeHR/src/main/resources/db/migration/{postgresql,sqlserver}/V11__rsp_recruitment_planning_foundation.sql` - provider-equivalent plan/vacancy foundation.
- `PrimeHR/src/main/resources/db/migration/{postgresql,sqlserver}/V12__rsp_authority_and_publication.sql` - provider-equivalent decision metadata and publication tables.

### Tests and scope document

- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV10ToV11UpgradeIT.java` - populated V10-to-V11 upgrade.
- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV11ToV12UpgradeIT.java` - populated V11-to-V12 upgrade and data retention.
- `PrimeHR/src/test/java/com/primehr/rsp/application/RspPlanningServiceImplTest.java` - tenant isolation, duplicate prevention, persistence rollback, and all-active-vacancies approval invariant.
- `PrimeHR/src/test/java/com/primehr/rsp/application/RspPublicationServiceImplTest.java` - SOD, administrator audit, source conflict before mutation, and persistence rollback.
- `PrimeHR/src/test/java/com/primehr/rsp/domain/RspDraftDomainTest.java` - plan, vacancy, publication, snapshot, and invalid terminal transitions.
- `PrimeHR/src/test/java/com/primehr/security/RspPlanningPermissionGuardTest.java` and `RspPublicationPermissionGuardTest.java` - exact action, administrator, and agency-wide behavior.
- `docs/prime-hrm/PHASE_5A_VACANCY_RECRUITMENT_PLANNING_SCOPE_APPROVAL.md` - approved scope, gates, exclusions, and 5A.3 checkpoint.
- `docs/prime-hrm/PHASE_5A_2_REVIEW_MANIFEST.md` - this independent review evidence.

## 3. Modified files and purpose

- `Administrative/src/main/java/com/administrative/impl/EffectiveAuthorizationServiceImpl.java` - registers the two Phase 5A PrimeHR features/actions without changing legacy rules.
- `Administrative/src/test/java/com/administrative/impl/EffectiveAuthorizationServiceImplTest.java` - verifies the additive feature/action mappings.
- `HumanResource/src/main/java/com/humanresource/repositories/EmployeeAppointmentRepository.java` - adds a derived, tenant-relevant active-Plantilla lookup.
- `PrimeHR/src/main/java/com/primehr/security/PrimeHrSecurityConfiguration.java` - keeps RSP APIs authenticated and routed to controller guards.
- `PrimeHR/src/test/java/com/primehr/contract/PrimeHrOpenApiContractTest.java` - verifies Phase 5A paths, schemas, and exclusions.
- `PrimeHR/src/test/java/com/primehr/migration/AbstractPrimeHrProviderIntegration.java` - expects V12 tables, relationships, indexes, and Flyway version.
- `PrimeHR/src/test/java/com/primehr/migration/CompetencyMigrationParityTest.java` - includes V11/V12 SQL Server/PostgreSQL structural parity and forbidden shared-SQL checks.
- `contracts/openapi/administrative-primehr-integration-v1.yaml` - additive QS and RSP source contracts.
- `contracts/openapi/humanresource-primehr-integration-v1.yaml` - additive Plantilla occupancy contract.
- `contracts/openapi/primehr-v1.yaml` - planning, vacancy authority, publication paths and DTO schemas; no applicant API.
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md` - records approval, implementation, tests, providers, boundary, and next gate.

## 4. Database schema

Administrative adds `qualification_standard`, related many-to-one to `job_position` and self-related through `predecessor_id`. It has a primary key, definition/record versions, lifecycle/effectivity/source/audit columns, unique Job Position + definition version, status checks, and `ix_qs_job_status_effective`.

PrimeHR V11 adds:

- `rsp_recruitment_plan` with unique agency + code and period/status index;
- `rsp_vacancy_request`, many-to-one to plan and exact Position Profile, unique plan + Plantilla, source/QS/profile snapshots, optimistic version, status checks, and plan/Plantilla indexes.

PrimeHR V12 adds decision metadata to plan/vacancy and:

- `rsp_vacancy_publication`, one-to-one with an authorized vacancy request, exact source/QS/position/organization/publication snapshot, lifecycle actors/times, and optimistic version;
- `rsp_vacancy_publication_channel`, many-to-one publication channels with unique publication + normalized channel name;
- `rsp_vacancy_publication_requirement`, many-to-one immutable competency/level/classification/criticality snapshots with unique publication + competency version;
- status, Plantilla, channel, and requirement indexes plus foreign keys and checks.

SQL Server uses the configured bracketed schema and SQL Server types. PostgreSQL uses the configured quoted schema and PostgreSQL types. The migrations are intentionally separate; shared Java/JPA has no provider branch or native query.

## 5. REST authorization matrix

All paths require a bearer token, token-derived agency, controller guard, and non-administrator `AGENCY_WIDE` data scope.

| Resource/action | Permission |
|---|---|
| planning list/get/readiness | `primehr.rsp-recruitment-planning` Access |
| plan/vacancy create | Add |
| plan/vacancy update | Edit |
| plan/vacancy archive/cancel | Delete/archive |
| plan/vacancy submit | Submit |
| plan return/approve; vacancy return/authorize/decline | Approve |
| publication list/get | `primehr.rsp-vacancy-publication` Access |
| publication create/update | Add/Edit |
| publication cancel/close | Delete/archive |
| publication submit | Submit |
| publication return/approve | Approve |
| publication publish | Publish |

Endpoints are those documented in `contracts/openapi/primehr-v1.yaml` under `/api/primehr/v1/rsp`. No PDF endpoint is implemented in 5A.2 because it belongs to 5A.3.

## 6. Verification evidence

| Command/gate | Result |
|---|---|
| `mvn -pl Administrative,HumanResource,PrimeHR -am clean test` | PASS: Common 3, Administrative 39, HumanResource 48, PrimeHR 140; 230 total, zero failures/errors/skips before the final invariant test |
| focused `RspPlanningServiceImplTest` | PASS: 4/4 after adding the all-active-vacancies approval regression |
| `mvn -pl Administrative,HumanResource,PrimeHR -am clean package` | PASS: Common 3, Administrative 39, HumanResource 48, PrimeHR 141; 231 total, zero failures/errors/skips |
| PostgreSQL-mode `PrimeHrFlywaySchemaIntegrationTest` | PASS: 9/9; V1-V12 migration plus Hibernate validation; zero skipped |
| real SQL Server fresh schema | PASS: 9/9 in `primehr_phase5a2_fresh_20260829`; V1-V12 plus Hibernate validation |
| real SQL Server populated V11-to-V12 | PASS: 1/1 in `primehr_phase5a2_upgrade_20260829`; preexisting plan retained |
| migration parity | PASS for provider-equivalent V11/V12 structure and provider-neutral shared code |
| `git diff --check` | PASS; only Git line-ending notices, no whitespace error |

The isolated SQL Server schemas were retained and existing schemas/data were not changed or deleted. A real PostgreSQL instance was not tested, by the approved SQL Server-primary policy. PostgreSQL-mode Flyway/Hibernate and parity tests are evidence, not a substitute claim for a live provider run.

The first focused Maven invocation failed before test discovery because PowerShell split an unquoted dotted property; the same command was immediately rerun with quoted Maven properties and passed 4/4. This was a command invocation error, not a compilation or test failure.

## 7. Audits, limitations, and boundary

- Source changes are checked before submission/approval/publication and fail before mutation/audit. Published snapshots remain historical.
- SOD denies submitter self-decision; administrator self-action requires an explicit audited reason.
- Service methods are transactional and persist/flush before audit, so failed persistence cannot create a success audit event.
- Approval now requires every active vacancy to remain SUBMITTED, preventing a returned item from being hidden by another submitted item.
- No credential, token, connection string, generated artifact, or developer-machine path was added.
- No provider-specific construct was added to shared Java/JPA. Provider-specific DDL is isolated in matching migration folders.
- No Administrative UI, PrimeHR UI, JRXML/PDF, Playwright, public careers route, applicant identity/application/upload, screening, examination, ranking, selection, appointment handoff, onboarding, or Phase 5B+ behavior was implemented.
- Administrative migration scripts remain explicitly operated according to that module's existing migration process; PrimeHR Flyway migrations are automatic.
- Live PostgreSQL remains unverified and is non-blocking only because the user approved SQL Server as the primary runtime gate.

## 8. Next approval checkpoint

Phase 5A.3 is not authorized by the Phase 5A.1/5A.2 approval. Its exact boundary is Administrative Qualification Standard/permission UI, PrimeHR planning/publication UI, SQL-free bean-driven vacancy-notice Jasper PDF, and repeatable Playwright acceptance. It must stop before Phase 5B applicant functionality.
