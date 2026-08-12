# ISOFT PRIME-HRM Phase 1C Review Manifest

Prepared: 2026-08-12

Scope: Controlled direct competency publishing

Overall state: Complete; backend, provider, frontend build, and focused manual acceptance gates passed

## 1. Scope delivered

Phase 1C adds direct, privileged publication of competency categories, proficiency scales, and competencies. Publication is independent from CRUD through `canPublish`, requires a reason and current `recordVersion`, is transactional and audited, and makes the published aggregate immutable. It does not introduce a multi-step approval workflow or any Phase 2 domain.

The three publish endpoints are:

- `POST /api/primehr/v1/admin/competency-categories/{categoryId}/publish`
- `POST /api/primehr/v1/admin/proficiency-scales/{scaleId}/publish`
- `POST /api/primehr/v1/admin/competencies/{competencyId}/publish`

Each endpoint requires authenticated PrimeHR access and the effective Administrative `primehr.competency.canPublish` permission. A missing flag fails closed. Existing administrator contracts retain effective publish authority and remain audited.

## 2. Backend files in the Phase 1C change set

### Created

- `PrimeHR/src/main/java/com/primehr/competency/api/PublishDefinitionRequest.java` - request contract for optimistic record version and mandatory publication reason.
- `PrimeHR/src/main/java/com/primehr/shared/exception/PublicationConflictException.java` - stable publication-conflict exception mapped to HTTP 409.
- `PrimeHR/src/main/resources/db/migration/postgresql/V3__competency_controlled_publishing.sql` - PostgreSQL publication metadata and supporting indexes.
- `PrimeHR/src/main/resources/db/migration/sqlserver/V3__competency_controlled_publishing.sql` - SQL Server equivalent migration.
- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV2ToV3UpgradeIT.java` - populated V2-to-V3 migration and Hibernate validation test.
- `docs/prime-hrm/PHASE_1C_COMPETENCY_PUBLISHING_SCOPE_APPROVAL.md` - approved scope and lifecycle decisions.
- `docs/prime-hrm/PHASE_1C_REVIEW_MANIFEST.md` - this review and acceptance handoff.

### Modified

- `Administrative/src/main/java/com/administrative/dtos/EffectiveFeaturePermissionResponse.java` - adds effective `canPublish`.
- `Administrative/src/main/java/com/administrative/impl/EffectiveAuthorizationServiceImpl.java` - resolves publish permission, fails closed for old rulesets, and preserves administrator behavior.
- `Administrative/src/test/java/com/administrative/impl/EffectiveAuthorizationServiceImplTest.java` - verifies ordinary, missing-flag, and administrator permission outcomes.
- `PrimeHR/src/main/java/com/primehr/competency/api/AdminCategoryResponse.java` - adds publication metadata.
- `PrimeHR/src/main/java/com/primehr/competency/api/AdminScaleResponse.java` - adds publication metadata.
- `PrimeHR/src/main/java/com/primehr/competency/api/AdminCompetencyResponse.java` - adds publication metadata.
- `PrimeHR/src/main/java/com/primehr/competency/api/CompetencyAdminController.java` - exposes the three publish endpoints.
- `PrimeHR/src/main/java/com/primehr/competency/application/CompetencyAdminService.java` - defines category, scale, and competency publication operations.
- `PrimeHR/src/main/java/com/primehr/competency/application/CompetencyAdminServiceImpl.java` - implements locking, optimistic checks, lifecycle/dependency/completeness validation, effectivity closure, publication metadata, and audit transaction boundaries.
- `PrimeHR/src/main/java/com/primehr/competency/domain/CompetencyCategory.java` - stores publisher/time and supports controlled publication.
- `PrimeHR/src/main/java/com/primehr/competency/domain/ProficiencyScale.java` - stores publisher/time and supports controlled publication.
- `PrimeHR/src/main/java/com/primehr/competency/domain/Competency.java` - stores publisher/time and supports controlled publication.
- `PrimeHR/src/main/java/com/primehr/competency/infrastructure/CompetencyCategoryRepository.java` - provider-neutral locked version-chain retrieval.
- `PrimeHR/src/main/java/com/primehr/competency/infrastructure/ProficiencyScaleRepository.java` - provider-neutral locked version-chain retrieval.
- `PrimeHR/src/main/java/com/primehr/competency/infrastructure/CompetencyRepository.java` - provider-neutral locked version-chain retrieval.
- `PrimeHR/src/main/java/com/primehr/integration/administrative/EffectiveFeaturePermission.java` - consumes `canPublish` from Administrative.
- `PrimeHR/src/main/java/com/primehr/security/PrimeHrAction.java` - adds `PUBLISH`.
- `PrimeHR/src/main/java/com/primehr/security/PrimeHrPermissionGuard.java` - enforces the dedicated publish flag.
- `PrimeHR/src/main/java/com/primehr/shared/audit/PrimeHrAuditService.java` - supports publication and predecessor-effectivity audit actions.
- `PrimeHR/src/main/java/com/primehr/shared/exception/PrimeHrExceptionHandler.java` - returns structured 409 publication conflicts.
- `PrimeHR/src/test/java/com/primehr/competency/api/CompetencyAdminControllerTest.java` - verifies endpoint contract, authentication, publish authorization, and conflict mapping.
- `PrimeHR/src/test/java/com/primehr/competency/application/CompetencyAdminServiceIntegrationTest.java` - verifies publication rules, atomic history closure, completeness/dependency validation, conflicts, and audit behavior.
- `PrimeHR/src/test/java/com/primehr/migration/AbstractPrimeHrProviderIntegration.java` - extends real-provider validation through V3 and representative publication on all three aggregate roots.
- `PrimeHR/src/test/java/com/primehr/migration/CompetencyMigrationParityTest.java` - checks PostgreSQL/SQL Server V3 parity.
- `PrimeHR/src/test/java/com/primehr/security/PrimeHrPermissionGuardTest.java` - verifies independent publish authorization and fail-closed behavior.
- `contracts/openapi/primehr-v1.yaml` - documents the three endpoints, request, publication metadata, permission, lifecycle, and error outcomes.
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md` - updates the canonical ledger from proposed scope to actual implementation and remaining acceptance gate.

## 3. Frontend files in the Phase 1C change set

### `administrative-software`

- `src/app/administrative/permission/Permission.tsx` - adds `hasPublish`/`canPublish`, shows Publish only for `primehr.competency`, requires Access, clears Publish when Access is cleared, preserves old JSON compatibility, and preserves administrator behavior.

The repository also has a modified `.env`. It is unrelated user/deployment configuration, was not changed for Phase 1C, was not inspected for values during the secret audit, and must not be included in a Phase 1C-only commit.

### `prime-hr-software`

- `src/lib/auth.ts` - adds typed `canPublish`, fail-closed parsing, and administrator handling.
- `src/app/prime-hr/competencies/CompetencyManager.tsx` - adds completeness preview, publish confirmation/reason, immutable warning, status/publisher display, error handling, and list/detail/audit refresh.
- `src/app/prime-hr/competencies/CompetencyManager.module.scss` - styles the publish action consistently with the existing UI.
- `README.md` - documents the controlled-publishing UI and permission behavior.

The entire `prime-hr-software` repository is currently untracked, so Git cannot independently distinguish its Phase 1B baseline from Phase 1C edits. The four files above are the recorded Phase 1C edits; all project files must be reviewed when the repository receives its first commit.

No `employee-portal-UI`, HRM UI, Timekeeping UI, Payroll UI, Jasper, HumanResource, Payroll, EmployeePortal, or HRISApp behavior belongs to Phase 1C.

## 4. Persistence and portability

V3 adds nullable `published_at` and `published_by` to each aggregate root and equivalent indexes supporting agency/code/status/effectivity/version-chain access. Existing ACTIVE records retain null publication metadata rather than receiving fabricated history. Existing V1/V2 migrations were not edited.

Application publication logic uses JPA and provider-neutral pessimistic locking. PostgreSQL and SQL Server use equivalent isolated Flyway migrations. Non-overlap is enforced transactionally in shared service logic rather than with a provider-specific range feature.

## 5. Verification evidence

### Backend and providers

- Final affected-module gate `\.\mvnw.cmd -pl Administrative,PrimeHR -am test` passed on 2026-08-12: Administrative 18 tests and PrimeHR 46 tests, with zero failures, errors, or skipped tests. `Common` was built as a dependency and has no tests.
- Full `\.\mvnw.cmd clean package` passed for all nine reactor modules.
- Full reactor Surefire reports contained 150 tests: Common 0, TimeKeeping 7, Administrative 18, HumanResource 34, EmployeePortal 0, Payroll 45, HRISApp 0, PrimeHR 46; zero failures, errors, or skips.
- Fresh V1+V2+V3 migration/Hibernate/provider tests passed on real Neon PostgreSQL 17.10: 7 tests.
- Populated V2-to-V3 upgrade passed on real Neon PostgreSQL 17.10: 1 test.
- Fresh V1+V2+V3 migration/Hibernate/provider tests passed on real SQL Server 14.0: 7 tests.
- Populated V2-to-V3 upgrade passed on real SQL Server 14.0: 1 test.
- Strengthened fresh-provider tests published category, scale, and competency aggregates on both providers.
- OpenAPI YAML parsed successfully and contains exactly the three approved publish paths.
- Backend `git diff --check` passed. Line-ending conversion warnings are informational.

Zero-test modules are explicitly reported: Common, EmployeePortal, and HRISApp. No configured test was skipped. PostgreSQL 17.10 is newer than Flyway 9.22.3's formally tested maximum PostgreSQL 15; the real migration/integration suite nevertheless passed.

### Frontends

- The user confirmed `npm run build` passes in `administrative-software`.
- The user confirmed `npm run build` passes in `prime-hr-software`.
- Earlier focused checks passed strict TypeScript in both repositories and ESLint in both repositories. Administrative used direct ESLint because its configured `npm run lint` command is stale for the installed Next.js version.
- Manual publishing acceptance was completed successfully by the user on 2026-08-12 against the local standalone Administrative, PrimeHR UI/API, and SQL Server `primehr` database.

## 6. Manual acceptance matrix

Use a disposable draft and non-production test accounts/data. After changing a ruleset, sign out and sign in again so the UI obtains current effective permissions.

### Minimum browser execution script

The user does not need to repeat backend/provider tests. Perform these five browser checks against one consistent local or QA environment:

1. **Administrator success and audit:** In Administrative Permission, confirm `primehr.competency` has Access, Add, Edit, and Publish enabled for the administrator. Sign out/in, open PrimeHR **Categories**, select **New Draft**, create a uniquely named disposable category with an effective-from date, and save it. Select **Publish**, first confirm a blank reason is rejected, then enter a reason. Confirm the row becomes **Published (ACTIVE)**; open **Details** and confirm publisher/time plus `PUBLISH_DRAFT` and the entered reason appear in **Audit History**.
2. **Published immutability:** On that published category, confirm **Edit**, **Archive**, and **Publish** are absent and **New version** is the available correction path.
3. **Validation presentation:** Open **Proficiency Scales**, create a disposable draft with an effective-from date but do not add a proficiency level. Select **Publish**. Confirm the warning says at least one enabled proficiency level is missing, the draft stays DRAFT, and no publish audit appears.
4. **Stale/conflict presentation:** Create another complete disposable category. Open the list in two browser tabs before publishing. Publish it in tab A; without reloading tab B, publish the stale row in tab B. Confirm tab B displays **Publication conflict**, refreshes the list, and does not create a second publish audit.
5. **Denied visibility:** Using an ordinary non-administrator test user, configure Access/Edit on but Publish off for `primehr.competency`, save, and sign out/in as that user. Confirm DRAFT rows have no **Publish** button. If no ordinary test user is available, record this one browser check as not run; the backend 403 and old-ruleset fail-closed cases are already covered by automated authorization tests.

Because successful publication is immutable historical data, use a local/test database or clearly prefixed QA records such as `QA1C-*`. Do not run these checks with disposable data in a production agency database.

### Acceptance result - 2026-08-12

All required browser scenarios passed:

- authorized ordinary publisher `EMP-00001` created and published `QA1C-CAT-001`, `QA1C-SCALE-001`, and `QA1C-COMP-001`;
- blank publication reason and incomplete scale/competency publication were rejected without a success audit;
- publisher/time, reason, `PUBLISH_DRAFT`, refreshed details, and immutable published controls were verified;
- the two-tab stale publication produced the expected conflict behavior, one successful publication, and no stale success audit;
- disabling Publish while retaining Access/Add/Edit hid Publish while preserving permitted draft behavior;
- the established `admin` account published `QA1C-ADMIN-001`, and the publisher/audit actor was recorded as `admin`;
- SSO, CORS, JWT compatibility, standalone Administrative authorization, and PrimeHR API access were exercised end to end.

One non-blocking UI limitation was observed: the level dialog exposes code, label, and order but not optional description/effectivity fields. New levels therefore store blank description and open-ended (`null`) effectivity through the current UI. The backend contract retains those optional fields and publication correctly validates open-ended coverage. This should be considered for a later usability enhancement; it did not invalidate Phase 1C acceptance.

| Scenario | Setup and action | Expected result |
|---|---|---|
| Old JSON / denied | Use an ordinary ruleset with Access/Edit but no `canPublish` property. Open Competencies. | Draft CRUD remains available as permitted, Publish is hidden, and a direct publish request is rejected with 403. |
| Explicitly denied | Set Publish off for an ordinary user, retain Access/Edit, then re-login. | Same denied behavior; Add/Edit never substitutes for Publish. |
| Allowed success | Enable Publish for the user. Create a complete category draft with `effectiveFrom`, publish it, enter a reason. | Confirmation warns about immutability; request succeeds; status displays Published; publisher/time appear; list/detail/audit refresh. |
| Validation | Attempt to publish an incomplete scale (no enabled level) or competency (missing an enabled indicator for one enabled scale level). | UI explains incompleteness; backend rejects with structured 400; draft remains unchanged; no success audit is added. |
| Conflict | Load the same publishable draft in two browser tabs. Publish in tab A, then submit the still-stale tab B confirmation. | Tab B shows the 409 publication-conflict message and refreshes; no duplicate publication or success audit is created. |
| Dependency | Attempt competency publication using an unpublished or wrong-version category/scale dependency. | Publication is rejected; no partial state or success audit is created. |
| Administrator | Publish a disposable complete draft as role `1`, ruleset administrator, or install administrator. | Publish is allowed and the real authenticated administrator is recorded in metadata/audit. |
| Immutability | Open a successfully published record. | Publish/Edit/Archive controls that would mutate published content are unavailable; creating a successor remains the correction path. |
| Successor history | Publish a successor whose start overlaps an open predecessor. | Predecessor remains Published/ACTIVE, its end date closes to the day before the successor, and audit shows both publish and closure events with the same reason/correlation. |

The backend automated suite already covers authorization, validation, conflicts, atomicity, dependencies, and audit. This matrix is the remaining browser-level acceptance check for permission transport, presentation, and error messaging.

## 7. Repository, generated-file, and secret audit

Audit locations:

- Backend: `git status --short`, `git diff --stat`, `git diff --check` from the `hris` root.
- Administrative UI: the same commands from `administrative-software`.
- PrimeHR UI: `git status --short --ignored` from `prime-hr-software`, because the project is not yet tracked.

Results on 2026-08-12:

- No credential-like added line was detected in the Phase 1C backend, Administrative UI, or PrimeHR UI source/config examples.
- Generated Maven `target/`, frontend `.next/`, `node_modules/`, and `*.tsbuildinfo` outputs are ignored and are not proposed for commit.
- Backend `.idea/workspace.xml` is ignored and is not proposed for commit.
- No Phase 2 position-profile, assessment, workflow, notification, bulk-publish, or similar source implementation was found. Matches for "unpublished" occur only in Phase 1C dependency validation/tests and are not an unpublish feature.
- The backend working tree contains unrelated HRISApp configuration and extensive TimeKeeping/ADMS/DTR work. Those files must be preserved and excluded from a Phase 1C-only commit.
- Administrative `.env` is unrelated and must be excluded from a Phase 1C-only commit.
- PrimeHR UI's entirely untracked state is a review risk: its first commit must intentionally include the complete application baseline, not only the four Phase 1C files.

Never print or copy live `.env` values into review output. A safe final audit should report filenames/counts only, and use `git diff --cached --check` plus a staged-file secret scanner before commit.

## 8. Explicitly unverified or outstanding

- No frontend was pushed or deployed as part of Phase 1C; deployment remains under the user's separate control.
- The retained real-provider validation schemas were not deleted.
- Repeatable CI does not yet provision both real database providers automatically.
- Unrelated backend and Administrative `.env` changes make a blanket `git add -A` unsafe.
- At the user's request, PrimeHR datasource configuration was consolidated after the original provider gates into one `application.properties`, with the SQL Server provider block active and the PostgreSQL block visible but commented. All 46 PrimeHR tests passed after consolidation, and startup/Flyway/API behavior passed against the real local SQL Server `primehr` database. The PostgreSQL block itself has not been rerun against PostgreSQL since consolidation; the unchanged PostgreSQL V1-V3 migrations and application behavior had passed on real Neon PostgreSQL before this configuration-only change.

## 9. Phase boundary and next gate

No multi-step approval workflow, unpublish, bulk publishing, position competency profile, assessment, or other Phase 2+ behavior was intentionally implemented.

Phase 1C is complete. The next action is review and explicit approval of `PHASE_2_POSITION_COMPETENCY_PROFILES_SCOPE_APPROVAL.md`. Do not implement Phase 2 from this manifest alone.
