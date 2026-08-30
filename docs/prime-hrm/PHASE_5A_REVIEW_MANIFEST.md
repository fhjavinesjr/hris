# ISOFT PRIME-HRM Phase 5A Final Review Manifest

Prepared: 2026-08-29

Status: Phase 5A.1, 5A.2, and 5A.3 complete. Ready for independent review and selective commit. Stop boundary: before Phase 5B.

## 1. Delivered requirements

| Requirement | Implementing areas |
|---|---|
| Versioned authoritative Qualification Standards | Administrative entity/repository/service/controller, dual-provider scripts, integration contract, Administrative UI |
| Exact Plantilla vacancy truth | HumanResource occupancy integration service/controller and derived repository lookup |
| Recruitment plan and vacancy request | PrimeHR RSP domain, repositories, services, controllers, V11 migrations, OpenAPI, UI |
| Approval and authority-to-fill workflow | planning service/controller, permission guard, audit events, UI actions and Playwright |
| Immutable vacancy publication | publication domain/service/controller, snapshot children, V12 migrations, OpenAPI, UI |
| Independent publication approval/publishing | publication guard and service separation-of-duties rules, exact UI actions and tests |
| Portable vacancy notice | typed report service/row, SQL-free JRXML, PDF endpoint and report/Playwright tests |
| SQL Server/PostgreSQL portability | equivalent V11/V12 and Administrative scripts, derived/JPQL persistence, migration parity and PostgreSQL-mode tests |
| Repeatable acceptance | Phase 5A Playwright fixture/spec, full regression, E2E guide, user guide |

## 2. Repository file manifest

### Administrative frontend (`administrative-software`)

Modified:

- `src/app/administrative/permission/Permission.tsx` - adds the exact Qualification Standard, recruitment-planning, and vacancy-publication action rows.
- `src/components/sidebar/Sidebar.tsx` - exposes Qualification Standards only when its exact Access permission is present.

Created:

- `src/app/administrative/qualification-standards/page.tsx` - route entry.
- `src/app/administrative/qualification-standards/QualificationStandards.tsx` - typed version/effectivity, draft, publish, archive, and successor controls.
- `src/app/administrative/qualification-standards/QualificationStandards.module.scss` - scoped existing-design-system styling.

Local `.env` is modified but unrelated to Phase 5A and must not be committed with this work.

### PRIME-HRM frontend (`prime-hr-software`)

Modified:

- `src/lib/auth.ts` - typed RSP planning/publication permissions.
- `src/components/PrimeHrShell.tsx` - permission-aware Recruitment Planning navigation.
- `e2e/support/primeHrTestSupport.ts` - exact temporary permission setup/restoration for the Phase 5A actors.
- `docs/PRIME_HRM_E2E_TESTING.md` - Phase 5A fixture, command, cases, and result.

Created:

- `src/lib/rsp.ts` - typed RSP DTOs and API/PDF client.
- `src/app/prime-hr/recruitment-planning/page.tsx` - route entry.
- `src/app/prime-hr/recruitment-planning/RecruitmentPlanningManager.tsx` - planning, readiness, authority, publication, snapshots, audit/conflict/error UI.
- `src/app/prime-hr/recruitment-planning/RecruitmentPlanningManager.module.scss` - scoped responsive styling.
- `e2e/support/rspTestSupport.ts` - repeatable SQL Server acceptance fixture with safe Plantilla/profile/QS preparation.
- `e2e/phase5a.spec.ts` - five serial Phase 5A browser scenarios.

`next-env.d.ts`, build output, Playwright output, and `.env.e2e.local` are not part of the change set. Real credentials were not added or printed.

### HRIS backend (`hris`)

Modified existing files:

- `Administrative/.../EffectiveAuthorizationServiceImpl.java` and its test - additive feature/action authorization mappings.
- `HumanResource/.../EmployeeAppointmentRepository.java` - provider-neutral active-Plantilla lookup.
- `PrimeHR/pom.xml` - retains Java parameter metadata required for reliable Spring request binding.
- `PrimeHR/.../PrimeHrSecurityConfiguration.java` - authenticates RSP routes while allowing exact controller guards to enforce feature authorization.
- `PrimeHR/.../PrimeHrOpenApiContractTest.java` - requires Phase 5A paths/schemas/PDF and rejects later-phase paths.
- `PrimeHR/.../AbstractPrimeHrProviderIntegration.java` - expects V11/V12 schema and relationships.
- `PrimeHR/.../CompetencyMigrationParityTest.java` - extends structural/provider-neutral checks through V12.
- the three OpenAPI files under `contracts/openapi` - additive Administrative source, HRM occupancy, and PrimeHR RSP contracts.
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md` and `PRIME_HRM_USER_GUIDE.md` - completion evidence and operating instructions.

Created Administrative files:

- controllers: `QualificationStandardController.java`, `RspPositionSourceIntegrationController.java`;
- DTOs: `QualificationStandardDtos.java`, `RspPositionSourceResponse.java`;
- domain: `QualificationStandard.java`, `QualificationStandardStatus.java`;
- persistence/service: `QualificationStandardRepository.java`, `QualificationStandardService.java`, `QualificationStandardServiceImpl.java`;
- migrations: `administrative-migration/sqlserver/phase5a1__qualification_standard.sql`, matching `postgresql/phase5a1__qualification_standard.sql`;
- test: `QualificationStandardDomainTest.java`.

Created HumanResource files:

- `PlantillaOccupancyIntegrationController.java`, `PlantillaOccupancyIntegrationService.java`, `PlantillaOccupancyIntegrationServiceImpl.java`, `PlantillaOccupancyResponse.java`, and `RspPlanningAuthorization.java` under `integration/primehr`;
- `PlantillaOccupancyIntegrationServiceImplTest.java`.

Created PrimeHR integration/security files:

- Administrative: `AdministrativeRspPositionSource.java`, `AdministrativeRspPositionSourceClient.java`;
- HumanResource: `HumanResourcePlantillaOccupancy.java`, `HumanResourcePlantillaOccupancyClient.java`;
- guards: `RspPlanningPermissionGuard.java`, `RspPublicationPermissionGuard.java` and their tests.

Created PrimeHR RSP files:

- API: `RspPlanningController.java`, `RspPlanningDtos.java`, `RspPublicationController.java`, `RspPublicationDtos.java`;
- application: `RspPlanningService.java`, `RspPlanningServiceImpl.java`, `RspPublicationService.java`, `RspPublicationServiceImpl.java`;
- domain: `RspAuditedEntity.java`, `RecruitmentPlan.java`, `RecruitmentPlanStatus.java`, `VacancyRequest.java`, `VacancyRequestStatus.java`, `VacancyType.java`, `VacancyPublication.java`, `VacancyPublicationStatus.java`, `VacancyPublicationChannel.java`, `VacancyPublicationRequirementSnapshot.java`, `VacancyVisibility.java`;
- persistence: `RecruitmentPlanRepository.java`, `VacancyRequestRepository.java`, `VacancyPublicationRepository.java`, `VacancyPublicationChannelRepository.java`, `VacancyPublicationRequirementRepository.java`;
- report: `VacancyNoticeReportRow.java`, `VacancyNoticeReportService.java`, `VacancyNoticeReportServiceImpl.java`, and `reports/vacancy_notice.jrxml`;
- tests: `RspDraftDomainTest.java`, `RspPlanningServiceImplTest.java`, `RspPublicationServiceImplTest.java`, `VacancyNoticeReportServiceTest.java`.

Created database/provider tests and migrations:

- SQL Server `V11__rsp_recruitment_planning_foundation.sql` and `V12__rsp_authority_and_publication.sql`;
- PostgreSQL provider-equivalent V11 and V12 scripts;
- `PrimeHrV10ToV11UpgradeIT.java` and `PrimeHrV11ToV12UpgradeIT.java`.

Created documentation:

- `PHASE_5A_VACANCY_RECRUITMENT_PLANNING_SCOPE_APPROVAL.md`;
- `PHASE_5A_2_REVIEW_MANIFEST.md` (backend checkpoint evidence);
- `PHASE_5A_3_UI_REPORT_ACCEPTANCE.md`;
- this final manifest.

## 3. Package/module structure and relationships

Administrative owns Job Position, Plantilla, Business Unit, Qualification Standard, and permission policy. HumanResource owns appointment-derived Plantilla occupancy. PrimeHR owns recruitment plans, vacancy requests, publications, immutable snapshots, decisions, and report generation. Integration is authenticated REST; no module reads another module's tables.

Database relationships:

- Administrative `qualification_standard` -> `job_position`; optional self predecessor; unique Job Position + definition version; lifecycle/effectivity index.
- `rsp_recruitment_plan` -> many `rsp_vacancy_request`; unique agency + plan code.
- vacancy request -> exact approved Position Profile and source/QS snapshots; unique plan + Plantilla.
- vacancy publication -> one vacancy request; many channel and immutable competency requirement snapshot rows.
- optimistic record versions, status/check constraints, foreign keys, Plantilla/status/period indexes, and audit evidence protect concurrency and history.

The SQL Server and PostgreSQL DDL differ only where each provider requires identity, quoting, or type syntax. Shared Java uses JPA/derived queries and contains no database-provider branch.

## 4. REST and authorization

Administrative:

- `/api/qualification-standards` list/get/create/update/archive/successor/publish, protected independently by Access/Add/Edit/Delete/Publish.
- `/api/integration/v1/primehr/rsp-position-source/{plantillaId}` returns minimum authoritative source/QS facts.

HumanResource:

- `/api/integration/v1/primehr/plantilla-occupancy/{plantillaId}` returns minimum occupancy evidence.

PrimeHR `/api/primehr/v1/rsp`:

- recruitment plan list/get/create/update/submit/return/approve/archive;
- vacancy add/update/readiness/submit/return/authorize/decline/cancel and readiness search;
- publication list/get/create/update/submit/return/approve/publish/cancel/close;
- `GET /vacancy-publications/{id}/notice.pdf`.

All use token-derived actor/agency context. Non-administrators require `AGENCY_WIDE` plus the exact feature action. Submitter self-approval/publish is denied; administrator override requires a reason and audit event. Source freshness, occupancy, effective QS/profile, duplicate Plantilla/period, lifecycle, and optimistic-version rules are service-enforced.

## 5. Report acceptance

The report is built from the immutable `PublicationResponse`, typed parameters, `VacancyNoticeReportRow`, and `JRBeanCollectionDataSource`. JRXML contains no `queryString`, connection, vendor SQL, or absolute path. Tests compile/fill/export representative and 70-requirement multi-page reports; Playwright downloads a real approved/published notice and verifies PDF bytes.

## 6. Tests and exact results

| Command | Result |
|---|---|
| `mvn -pl Administrative,HumanResource,PrimeHR -am clean test` | Passed during backend gate: Common 3, Administrative 39, HumanResource 48, PrimeHR 144; 234 total; zero failures/errors/skips |
| `mvn -pl Administrative,HumanResource,PrimeHR -am clean package` | Passed during backend/report gate with the same 234 tests and successful affected reactor packaging |
| final `mvn -pl PrimeHR test` | Passed: 144 tests; zero failures/errors/skips |
| final `mvn -pl PrimeHR package -DskipTests` | Passed and produced the repackaged JAR; this command explicitly skipped the redundant test execution after the separate 144-test run |
| Administrative `npm run typecheck` | Passed |
| Administrative `npm run lint` | Passed, zero errors; one pre-existing Sidebar hook warning |
| Administrative `npm run build` | Passed |
| PRIME-HRM `npm run typecheck` | Passed |
| PRIME-HRM `npm run lint` | Passed, zero errors |
| PRIME-HRM `npm run build` | Passed and production package created |
| `npx playwright test e2e/phase5a.spec.ts` | Passed 5/5 on local SQL Server |
| `npx playwright test` | Passed 20/20 on local SQL Server in 4.6 minutes; zero skipped |

SQL Server fresh V1-V12 passed 9/9 and populated V11-to-V12 passed 1/1 in retained isolated schemas. PostgreSQL-mode migration/Hibernate validation passed 9/9 and structural parity passed. A real PostgreSQL instance was not run, per user direction; that is explicitly unverified and is not represented as live-provider acceptance.

No tests were skipped and no reported suite executed zero tests. Test fixtures use approved local accounts and restore permission rules; they avoid occupied/reserved Plantilla records and reuse valid authoritative QS/Profile sources.

## 7. Defects corrected during Phase 5A.3

- Reliable Spring parameter binding through explicit request parameter names plus compiler parameter metadata.
- Accessible/stable labels for the new forms.
- Repeatable fixture exclusion of non-final reserved Plantilla records.
- Existing Position Profile chain reuse instead of duplicate-chain creation.
- Strict Playwright locators aligned to actual UI content.

## 8. Audits, assumptions, risks, and exclusions

- No real secret, `.env.e2e.local`, JWT, password, generated package, test result, or machine-specific report path belongs in the commit.
- Administrative `.env` is unrelated user work and must remain uncommitted.
- Administrative migrations follow that module's existing explicit operating process; PrimeHR V11/V12 use Flyway.
- Live PostgreSQL remains unverified; portability evidence is equivalent DDL, mode/parity tests, provider-neutral persistence, and SQL-free Jasper.
- The initial workflow intentionally uses `AGENCY_WIDE`; assigned-office scope awaits an authoritative assignment model.
- Dynamic identity-to-agency resolution and public/applicant authentication policy remain later decisions.
- No Phase 5B applicant account, consent, application, document, screening, notification, or public careers behavior was accidentally implemented.

## 9. Commit guidance and next gate

Commit the Phase 5A files in each of the three repositories. In Administrative UI, exclude `.env`. In PRIME-HRM UI, exclude `.env.e2e.local`, `dist/`, `.next/`, `test-results/`, and generated `next-env.d.ts` changes (none should appear in final status). In HRIS, exclude `target/` and local datasource/credential files.

After review and commit, the next permitted action is preparation of the exact Phase 5B scope. Phase 5B implementation remains blocked until the user reviews and explicitly approves that scope.
