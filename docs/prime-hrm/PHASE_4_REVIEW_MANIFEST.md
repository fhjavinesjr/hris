# Phase 4 Review Manifest

Date: 2026-08-29  
Scope: Phase 4 only — competency gap analysis, configurable development priority, manual L&D referral, Administrative permission controls, PrimeHR UI, portable Jasper PDF, and automated acceptance. Phase 5 is not implemented.

## 1. Scope and requirement mapping

| Approved requirement | Implementing files |
|---|---|
| Compare the effective Position Profile with the validated Person Profile | `PrimeHR/.../gap/application/CompetencyGapServiceImpl.java`, gap repositories/entities/DTOs/controllers, V9 migrations |
| Preserve exact source versions and transparent calculations | `CompetencyGapAnalysis.java`, `CompetencyGapItem.java`, `CompetencyGapServiceImpl.java`, V9 constraints, Phase 4 UI detail view |
| Classify BELOW, MEETS, EXCEEDS, and NOT_ASSESSED without treating missing evidence as zero | `GapClassification.java`, `NotAssessedReason.java`, service tests, UI and PDF |
| Configure and version development-priority policy | gap priority domain/application/API packages, V9 migrations, Administrative permission row, PrimeHR UI |
| Create manual/rule-assisted L&D referrals without creating an approved IDP | learning/referral packages, V10 migrations, referral UI and tests |
| Enforce feature/action/data-scope RBAC | Administrative authorization changes, `GapPermissionGuard.java`, controllers, security routing, UI permission helpers, Playwright RBAC test |
| Produce portable gap PDF | `CompetencyGapReportRow.java`, report service, SQL-free `competency_gap_report.jrxml`, report endpoint/test |
| Repeatable browser acceptance and operating guidance | `e2e/phase4.spec.ts`, E2E support/docs, user guide, this manifest |

## 2. Created files

### HRIS backend repository

Gap API/application/domain/infrastructure:

- `PrimeHR/src/main/java/com/primehr/gap/api/CompetencyGapController.java`
- `PrimeHR/src/main/java/com/primehr/gap/api/CompetencyGapDtos.java`
- `PrimeHR/src/main/java/com/primehr/gap/api/GapPriorityAdminController.java`
- `PrimeHR/src/main/java/com/primehr/gap/api/GapPriorityDtos.java`
- `PrimeHR/src/main/java/com/primehr/gap/application/CompetencyGapService.java`
- `PrimeHR/src/main/java/com/primehr/gap/application/CompetencyGapServiceImpl.java`
- `PrimeHR/src/main/java/com/primehr/gap/application/GapPriorityService.java`
- `PrimeHR/src/main/java/com/primehr/gap/application/GapPriorityServiceImpl.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/CompetencyGapAnalysis.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/CompetencyGapItem.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/GapClassification.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/GapPriorityLevel.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/GapPriorityRule.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/GapPriorityScheme.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/GapPrioritySchemeStatus.java`
- `PrimeHR/src/main/java/com/primehr/gap/domain/NotAssessedReason.java`
- `PrimeHR/src/main/java/com/primehr/gap/infrastructure/CompetencyGapAnalysisRepository.java`
- `PrimeHR/src/main/java/com/primehr/gap/infrastructure/CompetencyGapItemRepository.java`
- `PrimeHR/src/main/java/com/primehr/gap/infrastructure/GapPriorityLevelRepository.java`
- `PrimeHR/src/main/java/com/primehr/gap/infrastructure/GapPriorityRuleRepository.java`
- `PrimeHR/src/main/java/com/primehr/gap/infrastructure/GapPrioritySchemeRepository.java`

Referral API/application/domain/infrastructure:

- `PrimeHR/src/main/java/com/primehr/learning/referral/api/LdReferralController.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/api/LdReferralDtos.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/application/LdReferralService.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/application/LdReferralServiceImpl.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/domain/LdReferral.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/domain/LdReferralItem.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/domain/LdReferralStatus.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/infrastructure/LdReferralItemRepository.java`
- `PrimeHR/src/main/java/com/primehr/learning/referral/infrastructure/LdReferralRepository.java`

Security/report/migrations:

- `PrimeHR/src/main/java/com/primehr/security/GapPermissionGuard.java`
- `PrimeHR/src/main/java/com/primehr/gap/report/CompetencyGapReportRow.java`
- `PrimeHR/src/main/java/com/primehr/gap/report/CompetencyGapReportService.java`
- `PrimeHR/src/main/java/com/primehr/gap/report/CompetencyGapReportServiceImpl.java`
- `PrimeHR/src/main/resources/reports/competency_gap_report.jrxml`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V9__competency_gap_analysis.sql`
- `PrimeHR/src/main/resources/db/migration/postgresql/V9__competency_gap_analysis.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V10__manual_ld_referrals.sql`
- `PrimeHR/src/main/resources/db/migration/postgresql/V10__manual_ld_referrals.sql`

Tests and documents:

- `PrimeHR/src/test/java/com/primehr/gap/application/CompetencyGapServiceImplTest.java`
- `PrimeHR/src/test/java/com/primehr/gap/application/GapPriorityServiceIntegrationTest.java`
- `PrimeHR/src/test/java/com/primehr/gap/report/CompetencyGapReportServiceTest.java`
- `PrimeHR/src/test/java/com/primehr/learning/referral/api/LdReferralControllerTest.java`
- `PrimeHR/src/test/java/com/primehr/learning/referral/application/LdReferralServiceImplTest.java`
- `PrimeHR/src/test/java/com/primehr/learning/referral/domain/LdReferralDomainTest.java`
- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV8ToV9UpgradeIT.java`
- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV9ToV10UpgradeIT.java`
- `PrimeHR/src/test/java/com/primehr/security/GapPermissionGuardTest.java`
- `docs/prime-hrm/PHASE_4_COMPETENCY_GAP_LD_REFERRAL_SCOPE_APPROVAL.md`
- `docs/prime-hrm/PHASE_4_1_COMPETENCY_GAP_ANALYSIS.md`
- `docs/prime-hrm/PHASE_4_2_MANUAL_LD_REFERRAL.md`
- `docs/prime-hrm/PHASE_4_3_UI_REPORT_ACCEPTANCE.md`
- `docs/prime-hrm/PHASE_4_REVIEW_MANIFEST.md`

### PrimeHR frontend repository

- `src/app/prime-hr/competency-gaps/page.tsx`
- `src/app/prime-hr/competency-gaps/CompetencyGapManager.tsx`
- `src/app/prime-hr/competency-gaps/CompetencyGapManager.module.scss`
- `src/lib/competencyGaps.ts`
- `e2e/phase4.spec.ts`
- `docs/PRIME_HRM_USER_GUIDE.md`

## 3. Modified files and reasons

### HRIS backend repository

- `Administrative/.../EffectiveAuthorizationServiceImpl.java` — recognizes and resolves the three Phase 4 permission features and their action/data-scope fields.
- `Administrative/.../EffectiveAuthorizationServiceImplTest.java` — verifies effective Phase 4 permission resolution.
- `PrimeHR/pom.xml` — adds JasperReports PDF dependencies for the report service.
- `PrimeHR/.../security/PrimeHrSecurityConfiguration.java` — allows authenticated Phase 4 GET routes to reach their exact controller guards; fixes the acceptance-discovered conflict with the older catalog-read matcher.
- `PrimeHR/.../contract/PrimeHrOpenApiContractTest.java` — verifies the new contract paths and report response.
- `PrimeHR/.../migration/AbstractPrimeHrProviderIntegration.java` — extends provider migration/parity infrastructure through V10.
- `PrimeHR/.../migration/CompetencyMigrationParityTest.java` — compares the provider-specific V9/V10 schemas and constraints.
- `contracts/openapi/primehr-v1.yaml` — documents Phase 4 REST requests/responses, errors, and PDF.
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md` — records Phase 4 completion and the stop-before-Phase-5 checkpoint.

The existing `.idea/compiler.xml` and `.idea/encodings.xml` changes are unrelated/generated and are excluded from this Phase 4 change set.

### PrimeHR frontend repository

- `src/components/PrimeHrShell.tsx` — adds the Competency Gaps navigation item subject to live permission.
- `src/lib/auth.ts` — adds typed Phase 4 feature/action/data-scope permission helpers.
- `e2e/support/primeHrTestSupport.ts` — adds deterministic Phase 4 permission and fixture preparation.
- `docs/PRIME_HRM_E2E_TESTING.md` — documents the Phase 4 browser suite and commands.

The generated `next-env.d.ts` change was restored and is not part of Phase 4.

### Administrative frontend repository

- `src/app/administrative/permission/Permission.tsx` — adds Development Priority Configuration, Competency Gap Analysis, and L&D Referral permission controls with their applicable actions/scopes.

The existing `.env` change is user-owned and excluded from this Phase 4 change set.

## 4. Package and module structure

- `com.primehr.gap.api`: REST controllers and typed request/response records.
- `com.primehr.gap.application`: transactional calculation and priority-policy services.
- `com.primehr.gap.domain`: immutable analysis snapshots and configurable priority aggregates.
- `com.primehr.gap.infrastructure`: Spring Data JPA repositories using provider-neutral methods/JPQL.
- `com.primehr.gap.report`: JavaBean report rows and Jasper fill/export service.
- `com.primehr.learning.referral.*`: separate manual-referral aggregate with API, application, domain, and infrastructure layers.
- `com.primehr.security`: centralized Phase 4 feature/action/data-scope authorization.
- Administrative remains the authoritative permission service; PrimeHR remains the owner of Phase 4 business data.

## 5. Database schema, constraints, indexes, and relationships

V9 creates five tables:

- `prime_gap_priority_scheme`: versioned DRAFT/ACTIVE/ARCHIVED policy, predecessor lineage, publication audit/effectivity; unique agency/code/version.
- `prime_gap_priority_level`: child levels with unique code and rank per scheme.
- `prime_gap_priority_rule`: ordered rules linked to a scheme and priority level; classification/range validity constraints.
- `prime_competency_gap_analysis`: immutable analysis header linked to exact Position Profile, Person Profile, and Priority Scheme versions; unique request key and source combination.
- `prime_competency_gap_item`: immutable result rows linked to requirements, competencies, proficiency levels, priority/rule evidence; unique competency per analysis and database-checked formula/value invariants.

V10 creates two tables:

- `prime_ld_referral`: DRAFT/REFERRED/ARCHIVED referral linked to one analysis with submission audit and optimistic version.
- `prime_ld_referral_item`: snapshot of actionable BELOW/NOT_ASSESSED gap evidence; linked to referral, analysis, and gap item; unique gap per referral and checked value/reason invariants.

Indexes cover policy effectivity, level/rule ordering, employee analysis history, source profiles, result filtering, referral employee/status, referral analysis/status, and active gap claims. Foreign keys preserve all parent-child/source relationships. SQL Server uses the configured schema placeholder and appropriate SQL Server syntax; PostgreSQL uses the equivalent configured schema and portable logical model. No application query depends on provider-specific SQL.

## 6. REST endpoints and authorization

All endpoints require JWT authentication and live Administrative permission resolution. Administrators retain compatibility override; otherwise each action and scope is independent.

- `/api/primehr/v1/admin/gap-priority-schemes`: GET/list/detail = `primehr.gap-configuration` Access + AGENCY_WIDE; POST/version = Add + AGENCY_WIDE; PUT/level/rule mutation = Edit + AGENCY_WIDE; archive = Delete + AGENCY_WIDE; publish = Publish + AGENCY_WIDE.
- `/api/primehr/v1/competency-gaps`: GET/history/detail/latest/PDF = `primehr.competency-gap` Access, respecting OWN_RECORD or AGENCY_WIDE; POST generation = Add + AGENCY_WIDE.
- `/api/primehr/v1/ld-referrals`: GET/list/detail = `primehr.ld-referral` Access + AGENCY_WIDE; POST = Add; PUT/add items = Edit; item/referral archive = Delete; submit = Submit. All referral actions require AGENCY_WIDE.

The report response is `application/pdf` with a safely quoted attachment filename. Validation, access denial, not-found, conflict, and business-rule responses remain documented by the shared error contract.

## 7. DTOs, entities, repositories, services, controllers, and report

- DTOs are explicit records in `CompetencyGapDtos`, `GapPriorityDtos`, and `LdReferralDtos`; JPA entities are not exposed as the API.
- Entities model priority policy, immutable analysis/results, and referral/referral items with optimistic locking.
- Repositories are Spring Data JPA interfaces; pagination uses `Pageable` and queries avoid database-specific limit/date/cast functions.
- Services own transactions, readiness/source validation, version resolution, formula classification, rule priority, uniqueness, status transitions, immutable snapshots, audit events, and DTO mapping.
- Controllers own HTTP validation/headers and invoke `GapPermissionGuard` before services.
- Jasper receives standard JavaBean rows through `JRBeanCollectionDataSource`; the JRXML contains layout/field expressions and no SQL query.

## 8. Test classes and verified behavior

- `CompetencyGapServiceImplTest`: dynamic-scale BELOW/MEETS/EXCEEDS/NOT_ASSESSED calculation, explicit missing evidence, stale HRM fingerprint rollback, missing Person Profile rollback, own-record isolation.
- `GapPriorityServiceIntegrationTest`: publication coverage, immutable published policy, successor version/effectivity, invalid NOT_ASSESSED/rank rules.
- `GapPermissionGuardTest`: independent actions, own versus agency-wide data, fail-closed bearer/feature handling, administrator behavior, referral actions.
- `LdReferralDomainTest`: immutable submitted aggregate and actionable-item restriction.
- `LdReferralServiceImplTest`: stale conflict, active-gap uniqueness, invalid gap rejection, submission prerequisites/no downstream side effect, audited atomic submit.
- `LdReferralControllerTest`: independent Add/Delete/Submit permissions and scope.
- `CompetencyGapReportServiceTest`: representative calculated/missing-evidence PDF plus 70-row multi-page output and repeated header.
- `PrimeHrV8ToV9UpgradeIT` and `PrimeHrV9ToV10UpgradeIT`: populated history upgrades without mutation.
- `CompetencyMigrationParityTest`: SQL Server/PostgreSQL V9/V10 logical parity.
- `PrimeHrOpenApiContractTest`: API/report contract coverage.
- `e2e/phase4.spec.ts`: four repeatable tests covering configured/denied scopes, exact history/formula/missing evidence/PDF, referral/no-IDP boundary, and stale-update 409.
- Existing Phase 1–3 tests and E2E scenarios also run as regression coverage.

## 9. Commands and results

Final evidence is recorded here after execution:

- `mvn -pl Administrative,PrimeHR -am clean package`: **PASS** — Common 3, Administrative 34, PrimeHR 118; total 155 tests, 0 failures, 0 errors, 0 skipped.
- PrimeHR UI `npx tsc --noEmit --incremental false`: **PASS**.
- PrimeHR UI `npm run lint`: **PASS**.
- PrimeHR UI production `npm run build`: **PASS**.
- Administrative UI `npm run lint`: **PASS**, with one pre-existing Sidebar React-hooks dependency warning.
- Administrative UI production `npm run build`: **PASS**.
- Focused Phase 4 Playwright `npx playwright test e2e/phase4.spec.ts`: **PASS**, 4/4, 0 skipped.
- Full Playwright `npx playwright test`: **PASS**, 15/15, 0 skipped, against local SQL Server and locally started services.
- `git diff --check` in all three repositories: **PASS**.

An intermediate jar rebuild used `-DskipTests` only to expose the security matcher correction to the running browser suite. It is not the final verification; the final clean package above reruns all tests.

## 10. Provider and report evidence

- Real SQL Server: **tested** through migration/application/E2E acceptance.
- Real PostgreSQL: **not tested**, per the user's direction that PostgreSQL is not currently used.
- PostgreSQL coverage: dual V9/V10 migration scripts, migration parser/parity tests, provider-neutral JPA/application code, and SQL-free Jasper design. This is design/parity evidence, not a claim of live PostgreSQL acceptance.
- Jasper: JRXML compilation/fill/export is exercised by two tests, including representative null/missing evidence and multi-page data. The browser suite downloads and validates a real non-empty PDF.

## 11. Mocked, assumed, skipped, or unverified behavior

- Unit tests mock external Administrative/HRM integrations where isolation is required; full Playwright uses live local services and SQL Server data.
- The automated suite may create uniquely named test competencies/profile successors when no actionable gap fixture exists. It uses normal audited APIs and remains repeatable.
- Existing acceptance credentials are read only from ignored `.env.e2e.local`; no password or token is committed.
- No live PostgreSQL connection, Render, or Vercel deployment was tested.
- No approved IDP, training catalog/intake, funding, scheduling, attendance, completion, or annual L&D planning behavior exists in Phase 4.
- No tests were skipped and no module reported zero tests in the final Maven/Playwright gates.

## 12. Defects found and corrected

- Phase 4 authenticated GET routes were initially intercepted by a legacy generic competency-catalog authority matcher. Dedicated authenticated route matchers now allow requests to reach the exact Phase 4 controller guards; feature/action/data-scope enforcement remains server-side.
- Acceptance fixture generation and referral reuse were made deterministic so repeated runs do not violate immutable successor dates or the one-active-referral-per-gap invariant.

## 13. Risks and unresolved decisions

- Live PostgreSQL deployment remains unverified; it should receive a real migration/smoke gate before any future PostgreSQL production use.
- Phase 4 referrals deliberately stop at REFERRED. The future L&D/IDP workflow needs separate scope and approval.
- Test-created audited master/profile records remain in the local acceptance database by design; do not delete workflow history casually.
- Permission changes require a fresh effective authorization/session as documented in the user guide.

## 14. Commit boundary

Include the files listed in sections 2 and 3. Exclude real `.env`/`.env.e2e.local`, credentials, `.idea`, `.next`, `target`, Playwright reports/results, and other generated output. Review and commit each repository independently. Phase 4 is complete at this checkpoint; prepare and approve an exact Phase 5 scope before any Phase 5 implementation.
