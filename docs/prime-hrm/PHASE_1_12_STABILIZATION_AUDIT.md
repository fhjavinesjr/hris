# PRIME-HRM Phase 1-12 Stabilization Audit

Date opened: 2026-11-07  
Status: Implementation audit complete; live credential/provider acceptance remains  
Branch: `feature/continue-primehr`

## Purpose

This audit revisits the completed PRIME-HRM roadmap using the current integrated repositories as the
source of truth. It checks design completeness, business rules, authorization, lifecycle integrity,
provider portability, interfaces, reports, and browser behavior. Findings are corrected under their
originating phase; this work does not create a Phase 13.

The existing uncommitted work is preserved. Each phase must pass its affected backend tests,
strict frontend gates, production packaging, and focused packaged Playwright acceptance where a
browser interface exists. Report phases additionally require JRXML compilation and representative
PDF inspection. Live-provider limitations must be disclosed rather than represented as passed.

## Audit matrix

| Phase | Capability | Primary acceptance focus | Status |
|---|---|---|---|
| 1 | Competency foundation, draft administration, controlled publishing | definition lifecycle, child metadata, authorization, audit, effectivity, immutable publication | Complete with one UI gap corrected |
| 2 | Position competency profiles | authoritative target references, requirements, approval separation, precedence and version resolution | Reviewed; backend pass, live browser pending valid QA identities |
| 3 | Assessment and person competency profiles | assignment/ownership, evidence, validation separation, immutable profiles | Reviewed; backend pass, live browser pending valid QA identities |
| 4 | Gap analysis and L&D referral | exact-version comparison, deterministic priority, ownership, referral boundary and PDF | Reviewed; backend pass, live browser pending valid QA identities |
| 5 | Recruitment, Selection and Placement | vacancy through appointment/onboarding, applicant isolation, screening/evaluation, reports | Reviewed; Phase 5F browser pass, Phase 5A-5D live browser pending valid QA identities |
| 6 | Performance Management | policy through rating/appeal/intervention, route integrity, reports and outcome feed | Complete; packaged browser acceptance passed after portal-runtime corrections |
| 7 | Learning and Development | needs/IDP, plans/programs, nomination/enrollment, completion/evaluation and reports | Complete; packaged browser acceptance passed |
| 8 | Rewards and Recognition | governance, nomination, eligibility/evaluation, awards/fulfillment, privacy analytics and reports | Complete; packaged browser acceptance passed |
| 9 | Career and Succession Planning | governance, critical positions, confidential readiness, pools/development, continuity and referral | Complete; packaged browser acceptance passed |
| 10 | Evidence Center and Readiness Analytics | framework, assessment/evidence, corrective action, internal readiness, privacy and reports | Complete; packaged browser acceptance passed |
| 11 | Integration and operational hardening | deployment safety, retries/traceability, reconciliation, retention, recovery and operations | Complete; packaged browser acceptance passed |
| 12 | Action inbox and in-app notifications | authenticated-own feed, projection lifecycle, source reauthorization, reminders and safe routing | Complete; packaged browser acceptance passed |

## Phase 1 result

### Design and logic reviewed

- Standalone PrimeHR service and database ownership remain intact.
- Competency categories, proficiency scales/levels, competencies/behavioral indicators remain
  agency-scoped, versioned, audited, and optimistic-lock protected.
- Administrative permission checks remain independent for access, add, edit, archive/delete, and
  publish; direct frontend access fails closed without `primehr.competency.canAccess`.
- Publication remains reasoned, transactional, immutable, and dependent on complete/effective exact
  category, scale, level, and indicator versions.
- Current service/controller/domain/repository tests cover agency isolation, lifecycle validation,
  conflicts, dependencies, audit, provider-neutral mappings, and migration parity.

### Finding and correction

`P1-UI-001` - The backend and response contracts supported descriptions/effectivity on proficiency
levels and evidence guidance/effectivity on behavioral indicators, but the management interface did
not allow those values to be entered or changed. New records silently submitted `null`; edits merely
preserved the old hidden values.

Corrected behavior:

- add/edit level now captures optional description, effective-from, and effective-to;
- add/edit indicator now captures optional evidence guidance, effective-from, and effective-to;
- invalid inclusive date ranges are rejected before an API mutation;
- detail tables display the metadata so reviewers can verify publication coverage;
- request payloads retain optimistic record versions and convert blank optional fields to `null`.

No backend contract or schema change was required.

### Verification

| Gate | Result |
|---|---|
| Complete PrimeHR backend regression | Passed: 571 tests, zero failures/errors/skips |
| PrimeHR strict TypeScript | Passed |
| Focused ESLint for changed UI/config/spec | Passed |
| PrimeHR production build and standalone package | Passed; 72 application routes generated |
| Packaged Chromium Playwright | Passed: 3/3 |
| Playwright coverage | denied direct route; visible/editable level metadata and invalid-range rejection; visible/editable indicator guidance/effectivity; exact authenticated request payloads |

The backend suite exercised H2/Flyway schema validation and PostgreSQL/SQL Server migration parity.
No live PostgreSQL or SQL Server endpoint was configured for this audit run. Historical Phase 1 real-
provider evidence remains recorded in the Phase 1 manifests, but current live-provider revalidation
is still a deployment acceptance gate.

## Phase 2-5 result

The current implementation and original scope/review manifests were re-read for Position Profiles,
Assessment/Person Profiles, Gap/L&D Referral, and the complete RSP lifecycle. The audit confirmed
the authoritative-reference, exact-version, optimistic-lock, segregation, ownership,
applicant-isolation, report-privacy, and immutable-handoff designs. No unfinished code markers were
found in the backend, frontend, tests, or phase records.

The complete backend regression passed all relevant domain, controller, integration, permission,
report-rendering, database-migration-parity, and security tests. Phase 5F also passed packaged
Chromium acceptance 4/4, covering explicit register filters, candidate privacy, stale report
feedback, valid no-data analytics, independent HRM report access, and immutable finalized legal
documents.

The existing live Phase 2-5D Playwright suites could not reach a workflow assertion. Their shared
login prerequisite returned HTTP 500 because `.env.e2e.local` identifies `EMP-00001` as ADMIN,
while that employee number is absent from the active SQL Server `hrisof` employee table. The other
configured QA identities also require reconciliation with the current database and Administrative
permission rules. This is a test-environment fixture defect, not evidence of a Position Profile,
assessment, gap, or RSP workflow failure. No credential or production data was changed during the
audit.

## Phase 6-12 result

The phase manifests, current interfaces, shared permission helpers, and lifecycle actions were
checked against the present packaged applications. Self-contained Chromium acceptance passed:

| Phase | Packaged Playwright result | Acceptance emphasis |
|---|---:|---|
| 6 | 20/20 | policy, calendar, PMT, scales/templates, planning, authenticated ownership, monitoring/coaching privacy, ratings, reports, analytics, PDF actions |
| 7 | 3/3 | exact L&D report permission, privacy-controlled effectiveness, authenticated-own learning and eligible print |
| 8 | 3/3 | exact PRAISE report permission, minimum-cohort suppression, authenticated-own recognition and eligible print |
| 9 | 3/3 | exact succession report permission, minimum-cohort suppression, employee-visible career data and eligible print |
| 10 | 4/4 | exact Evidence Center permission, lineage snapshot, evidence/source suppression, analytics-only isolation |
| 11 | 3/3 | agency-wide operations permission, sanitized dependency status, report-only retention, explicit reconciliation without purge controls |
| 12 | 3/3 | own notification feed, dismiss/read actions, stale-source refusal, revalidated safe routing, real portal records |

Phase 1 additionally passed 3/3 packaged tests and Phase 5F passed 4/4, for 46 passing packaged
Chromium tests in this audit.

### Findings and corrections

`P6-PORTAL-001` - Employee Portal standalone packaging traced Sharp's native addon but omitted its
platform runtime libraries. The packaged server failed while optimizing the header image. The
packager now overlays installed `@img/sharp-*` platform packages into the standalone artifact; the
Windows package was verified to contain the native addon and both required libvips DLLs.

`P6-PORTAL-002` - Header, sidebar, and profile fallbacks referenced missing
`/avatar-default.png` and `/default-avatar.jpg` files. They now use the existing packaged
`/accounts.png` asset. The deprecated broad `images.domains` setting was also replaced by the
equivalent HTTPS `remotePatterns` entry for `cdn.builder.io`.

`P12-SHELL-001` - A malformed but successful notification response could dereference an undefined
`content` array and crash every Employee Portal page using the global notification bell. The shell
now validates the feed and unread-count shapes and safely falls back to an empty notification state.
Real request failures continue to show the existing actionable error.

`QA-E2E-001` - Older packaged acceptance fixtures predated the current HRM module guard and Phase 12
notification shell. Shared notification response fixtures and current HRM security/module-access
responses were added. Missing isolated packaged runners were added for Phase 5F, Phase 6A-6D, and
Phase 9E. These changes update test prerequisites only; they do not bypass feature authorization.

## Verification summary

| Gate | Result |
|---|---|
| Complete PrimeHR backend regression | Passed: 571 tests, zero failures/errors/skips |
| PrimeHR strict TypeScript | Passed after stabilization fixtures/configuration |
| PrimeHR production build and standalone package | Passed; 72 application routes generated |
| Employee Portal production build and standalone package | Passed; native Sharp runtime and fallback assets verified |
| Packaged Chromium acceptance | Passed: 46/46 across Phases 1, 5F, and 6-12 |
| Live Phase 2-5D Chromium | Environment-blocked before workflow assertions by stale QA identities |
| Current live PostgreSQL validation | Not configured for this audit run |
| Current live SQL Server workflow validation | Backend connected successfully; browser login fixtures require reconciliation |

## Remaining deployment acceptance

Before declaring a release candidate, assign four dedicated, non-production QA employees in the
active SQL Server database to the ADMIN, SUBMITTER, APPROVER, and DENIED test roles, configure their
Administrative permission rules, update the ignored `.env.e2e.local`, and rerun the live Phase 2-5D
matrix. Then repeat provider-sensitive acceptance against PostgreSQL. No application-code change is
currently indicated by either outstanding gate.
