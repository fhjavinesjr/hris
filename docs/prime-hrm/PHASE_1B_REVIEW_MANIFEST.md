# ISOFT PRIME-HRM Phase 1B Review Manifest

Prepared: 2026-08-03  
Scope: Phase 1B.1 and Phase 1B.2 only  
Status: Implemented and ready for independent review; not committed by Codex

## 1. Scope and requirement mapping

| Approved requirement | Implementing files |
|---|---|
| Draft lifecycle, business version, lineage, and optimistic locking | `PrimeHR/.../domain/DefinitionStatus.java`; the five competency domain entities; `CompetencyAdminService*.java`; repositories and specifications |
| Provider-equivalent V2 schema | PostgreSQL and SQL Server `V2__competency_draft_administration.sql`; migration parity, fresh-schema, and upgrade tests |
| Validated command/read REST API | `CompetencyAdminController.java`; `Draft*Request.java`; `Admin*Response.java`; OpenAPI contract |
| Append-only transactional audit | `PrimeHrAuditEvent.java`, repository, service, response DTO, V2 migrations, and service integration tests |
| Authoritative Administrative authorization | `EffectiveAuthorizationController.java`, service/interface/DTO; PrimeHR Administrative client, action enum, and permission guard |
| PrimeHR SSO and centralized runtime destinations | Administrative SSO target/service/config seed; Employee Portal runtime config/sidebar/storage types; focused SSO tests |
| Administrative permission catalog | `administrative-software/src/app/administrative/permission/Permission.tsx` |
| Standalone strict TypeScript management UI | all tracked files in `prime-hr-software`, especially `CompetencyManager.tsx`, SSO callback, runtime/auth helpers, and SCSS |
| Documentation and phase control | this manifest, `PHASE_1B_SCOPE_APPROVAL.md`, `PHASE_1B_COMPETENCY_DRAFT_ADMINISTRATION.md`, and `PRIME_HRM_PROGRESS.md` |

The implementation contains no publishing/activation transition, HTTP `DELETE`, HTTP `PATCH`, hard delete, applicant route, assessment, position profile, or other Phase 1C/2 capability.

## 2. Backend repository files

Repository: `hris`

### Created

- Administrative authorization: `Administrative/src/main/java/com/administrative/controllers/EffectiveAuthorizationController.java`, `dtos/EffectiveFeaturePermissionResponse.java`, `services/EffectiveAuthorizationService.java`, `impl/EffectiveAuthorizationServiceImpl.java`, and `src/test/java/com/administrative/impl/EffectiveAuthorizationServiceImplTest.java`.
- PrimeHR API DTO/controller: `AdminCategoryResponse.java`, `AdminCompetencyResponse.java`, `AdminIndicatorResponse.java`, `AdminLevelResponse.java`, `AdminScaleResponse.java`, `CompetencyAdminController.java`, `DraftCategoryRequest.java`, `DraftCompetencyRequest.java`, `DraftIndicatorRequest.java`, `DraftLevelRequest.java`, `DraftScaleRequest.java`, and `DraftTransitionRequest.java` under `PrimeHR/src/main/java/com/primehr/competency/api`.
- PrimeHR application layer: `PrimeHR/src/main/java/com/primehr/competency/application/CompetencyAdminService.java` and `CompetencyAdminServiceImpl.java`.
- PrimeHR lifecycle/security/integration: `competency/domain/DefinitionStatus.java`; `integration/administrative/AdministrativeAuthorizationClient.java`, `AuthorizationDependencyException.java`, `EffectiveFeaturePermission.java`; `security/PrimeHrAction.java` and `PrimeHrPermissionGuard.java`.
- PrimeHR audit/errors: `shared/audit/AuditEventResponse.java`, `PrimeHrAuditEvent.java`, `PrimeHrAuditEventRepository.java`, `PrimeHrAuditService.java`; `shared/exception/IllegalLifecycleTransitionException.java` and `OptimisticConflictException.java`.
- Migrations: `PrimeHR/src/main/resources/db/migration/postgresql/V2__competency_draft_administration.sql` and `sqlserver/V2__competency_draft_administration.sql`.
- Tests: `PrimeHR/src/test/java/com/primehr/competency/api/CompetencyAdminControllerTest.java`, `competency/application/CompetencyAdminServiceIntegrationTest.java`, `migration/PrimeHrV1ToV2UpgradeIT.java`, and `security/PrimeHrPermissionGuardTest.java`.
- Documents: `docs/prime-hrm/PHASE_1B_SCOPE_APPROVAL.md`, `PHASE_1B_COMPETENCY_DRAFT_ADMINISTRATION.md`, and this manifest.

### Modified

- Administrative SSO/config/tests: `Administrative/src/main/java/com/administrative/impl/SsoServiceImpl.java`, `SystemConfigImpl.java`, `sso/SsoTarget.java`, and `src/test/java/com/administrative/repositories/SsoServiceImplTest.java` add the dedicated `primehr` destination and `primeHr` module authorization.
- Domain model: `BehavioralIndicator.java`, `Competency.java`, `CompetencyCategory.java`, `ProficiencyLevel.java`, and `ProficiencyScale.java` add lifecycle-aware aggregate/child behavior, lineage, and version mapping.
- Persistence: `BehavioralIndicatorRepository.java`, `CompetencyCategoryRepository.java`, `CompetencyRepository.java`, `CompetencySpecifications.java`, `ProficiencyLevelRepository.java`, and `ProficiencyScaleRepository.java` add provider-neutral administration queries and relationship checks.
- Runtime/security/audit/errors: `PrimeHrProperties.java`, `PrimeHrSecurityConfiguration.java`, `AgencyAuditableEntity.java`, and `PrimeHrExceptionHandler.java` wire the Administrative dependency, guarded admin API, actor audit, and stable conflict/lifecycle responses.
- Configuration/tests: `PrimeHR/src/main/resources/application.properties`, `src/test/java/com/primehr/migration/AbstractPrimeHrProviderIntegration.java`, `CompetencyMigrationParityTest.java`, `src/test/resources/application-flyway-h2.properties`, and `application-test.properties` add the bootstrap Administrative URL and V2 provider validation.
- Contract/docs: `contracts/openapi/primehr-v1.yaml` and `docs/prime-hrm/PRIME_HRM_PROGRESS.md` describe the new contract and checkpoint evidence.

No existing backend HRIS business module outside Administrative and PrimeHR was modified.

## 3. Frontend repository files

### `administrative-software`

- Modified: `src/app/administrative/permission/Permission.tsx` adds `app.primehr`, `primehr.competency`, all four existing permission flags, and the `primeHr` portal-module mapping/default/admin behavior.
- Existing `.env` changes are user-owned, were not modified for Phase 1B, and must not be staged accidentally as part of this phase.

### `employee-portal-UI`

- Modified: `src/components/sidebar/Sidebar.tsx` adds the permission-controlled PrimeHR launch and `/prime-hr/sso` target; `src/lib/utils/localStorageUtil.ts` adds typed `primeHr` access; `src/lib/utils/runtimeConfig.ts` adds centralized PrimeHR API/UI keys and environment fallback names.
- Existing `.env` changes are user-owned, were not modified for Phase 1B, and must not be staged accidentally as part of this phase.

### `prime-hr-software` (new independent repository)

- Created configuration/root files: `.env.example`, `.gitignore`, `README.md`, `eslint.config.mjs`, `next-env.d.ts`, `next.config.ts`, `package.json`, `package-lock.json`, and `tsconfig.json`.
- Created application files: `src/app/globals.scss`, `layout.tsx`, `page.tsx`, `prime-hr/competencies/page.tsx`, `CompetencyManager.tsx`, `CompetencyManager.module.scss`, `prime-hr/sso/page.tsx`, `src/lib/auth.ts`, and `src/lib/runtimeConfig.ts`.
- Generated `tsconfig.tsbuildinfo`, `.next`, and `node_modules` are ignored and must not be committed.

## 4. Package and module structure

PrimeHR preserves Controller -> Application Service -> Repository/Specification -> Entity layering. Cross-module authorization is a typed HTTP adapter under `integration.administrative`; PrimeHR does not read Administrative persistence. Shared PrimeHR audit and exception behavior remains under `com.primehr.shared`. Administrative owns authoritative permission resolution and SSO ticket issuance. The standalone UI owns only presentation, typed API calls, and SSO bootstrap; backend authorization remains mandatory.

## 5. Database objects and relationships

V2 changes `prime_competency_category`, `prime_proficiency_scale`, and `prime_competency` with lifecycle status, positive `definition_version`, and nullable self-referencing `supersedes_id`. Existing V1 rows become `ACTIVE`, version 1. Existing agency/code uniqueness becomes agency/code/business-version uniqueness. Status check constraints allow only `DRAFT`, `ACTIVE`, and `ARCHIVED`.

`prime_proficiency_level` remains owned by one scale version; `prime_behavioral_indicator` remains owned by one competency version and references a level. Successor creation clones children into the successor aggregate. Application checks reject archived references, wrong-scale levels, cross-agency relationships, illegal lifecycle changes, and duplicate active draft lineage/code conditions.

New `prime_audit_event` has a primary key; agency, actor, action, aggregate identity, business/record versions, timestamp, before/after JSON text, reason, source module, and optional correlation ID. It has aggregate/time and actor/time indexes. The application exposes read/insert only; command and audit writes share one transaction.

Provider files are intentionally separate because identifier quoting and timestamp/large-text types differ. Both implement equivalent constraints, foreign keys, indexes, and data conversion. Hibernate remains `ddl-auto=validate`.

## 6. REST endpoints and authorization

Administrative exposes authenticated `GET /api/authorization/effective?featureKey=primehr.competency`. It derives employee and role from the authenticated context and returns canonical persisted flags plus administrator state.

PrimeHR uses base path `/api/primehr/v1/admin`:

- Categories: list (`canAccess`), create/version (`canAdd`), update (`canEdit`), archive (`canDelete`).
- Scales: list (`canAccess`), create/version (`canAdd`), update and level create/update (`canEdit`), scale/level archive (`canDelete`).
- Competencies: list (`canAccess`), create/version (`canAdd`), update and indicator create/update (`canEdit`), competency/indicator archive (`canDelete`).
- Audit: `GET /audit-events` requires `canAccess`.

All PrimeHR admin requests require JWT authentication. The caller bearer token is forwarded to Administrative and failures, timeouts, invalid responses, 401, and 403 fail closed. Established role `1`, Administrative administrator rulesets, and the install administrator retain their existing bypass. Agency always comes from required server-side `PRIMEHR_AGENCY_ID`; requests cannot supply it.

## 7. DTOs, entities, repositories, services, and controllers

- Request DTOs: five `Draft*Request` records plus `DraftTransitionRequest`; validation controls accepted fields, optimistic `recordVersion`, and required reasons.
- Response DTOs: five `Admin*Response` records plus `AuditEventResponse`; JPA entities are not exposed directly.
- Entities: five existing competency entities plus new `PrimeHrAuditEvent`; roots carry business lineage and optimistic record version while children inherit their root version through ownership.
- Repositories/specifications: provider-neutral Spring Data/JPQL/specification queries; no shared native provider SQL was introduced.
- Services: `CompetencyAdminServiceImpl` owns validation, cloning, lifecycle, transaction, mapping, and audit; `PrimeHrAuditService` appends audit; Administrative resolves effective permissions; PrimeHR client/guard maps each action.
- Controllers: `CompetencyAdminController` owns HTTP validation/status/parameters; `EffectiveAuthorizationController` exposes the narrow authenticated authority contract.

## 8. Tests and what they verify

- `CompetencyAdminControllerTest`: unauthenticated 401, exact-action 403, server agency/filter use, and absence of DELETE/PATCH routes.
- `CompetencyAdminServiceIntegrationTest`: optimistic draft commands and one audit per success; active immutability and child cloning; archive rules without deletion.
- `PrimeHrPermissionGuardTest`: exact persisted action flag, administrator bypass, and fail-closed dependency handling.
- `EffectiveAuthorizationServiceImplTest`: canonical persisted flags, administrator compatibility, and malformed/missing rules fail closed.
- `SsoServiceImplTest`: ten cases including hashed/target-bound tickets, permission denial, single-use/wrong-target/expired rejection, PrimeHR module permission, and install/role-1 administrator behavior.
- `CompetencyMigrationParityTest`: provider migration object/constraint parity.
- `PrimeHrFlywaySchemaIntegrationTest`: six supplemental H2 schema/entity validations.
- `PrimeHrV1ToV2UpgradeIT`: populated V1 rows upgrade to V2 without loss and with deterministic lifecycle/version defaults.
- Existing Phase 1A query, domain, repository, controller, and application smoke tests remain green.

No frontend test script exists in the new UI. Frontend validation therefore consists of lint, strict type-check, production builds, and code inspection; browser automation was not invented for this phase.

## 9. Commands and results

| Command | Result |
|---|---|
| `./mvnw.cmd -pl PrimeHR,Administrative -am clean test` | Passed; PrimeHR 34 tests, 0 failures/errors/skips; Administrative tests passed, including 3 authorization and 10 SSO tests. Common has zero tests (pre-existing). |
| `./mvnw.cmd clean package` | Passed; all 9 Maven reactor projects successful. |
| `./mvnw.cmd -pl PrimeHR,Administrative -am test` after final SSO/reference changes | Passed; no skipped Phase 1B tests. |
| PostgreSQL fresh V1+V2 provider gate | Passed 6/6 on real Neon PostgreSQL 17.10. |
| SQL Server fresh V1+V2 provider gate | Passed 6/6 on real local SQL Server 14.0. |
| PostgreSQL populated V1-to-V2 upgrade gate | Passed 1/1 on a real retained validation schema. |
| SQL Server populated V1-to-V2 upgrade gate | Passed 1/1 on a real retained validation schema. |
| `npm run build` in `administrative-software` | Passed. |
| `npm run build` in `employee-portal-UI` | Passed. |
| `npm run typecheck`, `npm run lint`, `npm run build` in `prime-hr-software` | Passed; generated routes `/`, `/prime-hr/competencies`, and `/prime-hr/sso`. |
| `npm audit --omit=dev` in `prime-hr-software` | Passed; 0 vulnerabilities with Next.js 16.2.12. |
| OpenAPI YAML parser check | Passed using the installed Node `yaml` parser. |
| `git diff --check` and new-file trailing-whitespace scan | Passed. |
| Phase-boundary and secret/configuration scans | Passed; no prohibited endpoint/transition or runtime credential in the Phase 1B source set. |

The real validation schemas were deliberately retained; no destructive cleanup was performed.

## 10. Unverified behavior, risks, and decisions

- Manual end-to-end browser acceptance against simultaneously running Administrative, PrimeHR, and Employee Portal services has not been performed. Portal launch/return, every form action, 409 display, audit display, and 401/403/dependency-unavailable presentation require reviewer smoke testing.
- PostgreSQL 17.10 is newer than Flyway 9.22.3's declared PostgreSQL 15 tested maximum; tests passed but the warning remains.
- The new UI currently audits clean on Next.js 16.2.12. Dependency advisories are time-sensitive and should still be rechecked in CI and before deployment.
- Dynamic employee-to-agency resolution remains deferred. The deployment is deliberately single-agency through required `PRIMEHR_AGENCY_ID`.
- Protected PrimeHR operations synchronously depend on Administrative and intentionally fail closed during an outage.
- Publishing governance and a distinct `canPublish` permission remain unresolved and outside Phase 1B.
- H2 remains supplemental; it is not evidence for either provider. Both real providers were separately tested as recorded above.
- Existing user `.env` changes in the Administrative and Employee Portal repositories are unrelated and must be reviewed/staged separately.
- No functionality from Phase 1B's explicit exclusion list was intentionally or accidentally implemented.

## 11. Commit-readiness conclusion

All automated Phase 1B gates passed, both real database providers passed fresh and upgrade paths, and the phase boundary is intact. The implementation is ready for independent diff review and manual end-to-end smoke testing before staging. Stage each repository separately, explicitly excluding the two user-owned `.env` files and all ignored generated artifacts. Do not begin Phase 1C or publishing until separately approved.
