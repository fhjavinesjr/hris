# ISOFT PRIME-HRM Progress Ledger

Last updated: 2026-08-28
Current phase: Phase 3 complete; Phase 4 requires a separate exact scope and explicit approval
Status: Phase 3 backend, dual-provider migrations/parity, permissions, UI, SQL Server Playwright acceptance, documentation, and final commit-readiness gates pass

Canonical detail: [PHASE_0_ARCHITECTURE_DISCOVERY.md](./PHASE_0_ARCHITECTURE_DISCOVERY.md)

## Phase status

| Phase | Status | Delivered |
|---|---|---|
| 0 — Architecture Discovery | Complete | repository inventory; architecture correction; ownership, integration, migration, security and Phase 1 decisions |
| 1A — Standalone Competency Foundation | Complete | standalone module, isolated datasource profiles, dual migrations, read-only APIs, OpenAPI and tests |
| 1A.1 - Foundation Hardening | Complete | trusted configured agency scope, competency-read authority, Flyway-created-schema tests, real PostgreSQL and SQL Server validation |
| 1B — Competency Draft Administration | Implemented | dual-provider lifecycle/audit backend, Administrative authorization, SSO, permission configuration, and standalone management UI |
| 1C - Controlled Competency Publishing | Complete | dedicated `canPublish`, transactional/audited publication, dual-provider V3 migrations, OpenAPI, Administrative permission control, PrimeHR publishing UI, and successful manual acceptance |
| 2.1 - Position Profile Draft Foundation | Complete | Administrative typed target reads; PrimeHR V4 draft profiles/requirements, dual migrations, OpenAPI, authorization, audit, tests, and real SQL Server fresh/upgrade validation; live PostgreSQL waived as a gate while portability remains mandatory |
| 2.2 - Submission, Approval, Resolution, Comparison | Complete | independent permissions, two-stage lifecycle, separation of duties, effective Plantilla precedence, exact-version comparison, conflicts, complete audit, V5 migrations, and SQL Server validation |
| 2.3 - Position Profile UI | Complete | Administrative permission controls; typed profile UI; user-confirmed browser acceptance; repeatable eight-test Playwright matrix; lint/type/build gates; user and test documentation |
| 3.1 - Assessment Draft Foundation | Complete | HRM subject contract; assessment permissions/data scope; V6 draft cycle/tool/subject/assessor model and API; SQL Server and portability gates |
| 3.2 - Assessment Execution | Complete | V7 lifecycle, assessor inbox/work, exact ratings/evidence, completeness, submit/return/resubmit, audit, OpenAPI, SQL Server and portability gates |
| 3.3 - Human Validation and Person Profiles | Complete | independent human decisions; separation of duties; audited administrator override; immutable versioned person profiles; deterministic latest/history reads; V8 SQL Server and portability gates |
| 3.4 - Assessment and Person Profile UI | Not started | Administrative permission controls, PrimeHR routes/pages, Playwright acceptance, user/test documentation, and final Phase 3 review remain excluded pending approval |

## Decisions recorded

- One modular PrimeHR backend, not six microservices.
- `PrimeHR` is implemented in the `hris` reactor as a standalone-first module with its own database; it is not yet assembled into HRISApp.
- No HRISApp runtime dependency until isolated second-datasource tests pass.
- `prime-hr-software` now exists as the standalone management frontend; the existing Employee Portal remains the employee self-service integration point, and public applicant routes remain deferred to the RSP phase.
- Separate applicant and employee identities.
- Administrative owns SSO/permissions/config/reference masters; HRM owns employee/PDS/appointment; Timekeeping and Payroll expose only needed finalized facts.
- No direct cross-domain database access.
- Versioned REST first; broker/outbox deferred.
- Definition versions and source snapshots preserve history.
- Flyway + `ddl-auto=validate` + PostgreSQL/SQL Server parity from the first schema.
- Backend enforcement combines action, data scope, process role, state, and module access.
- New reports favor service DTOs/bean data sources.
- Storage, notifications, analytics, gateway, and separate reporting service are deferred.
- Common JWT classes are not reused directly in PrimeHR because their hardcoded secret/logging cannot be changed without affecting existing modules; PrimeHR preserves the token contract with mandatory environment configuration.
- Phase 1A.1 exposes authorized reads only. The current safe scope is a required server-side single-agency configuration because the verified identity model has no agency claim or directory relationship. Full Administrative action/dynamic-agency authorization remains a Phase 1B prerequisite for writes.

## Current-state corrections

- Existing backend modules share a physical datasource in combined deployment.
- No gateway, broker, outbox, storage abstraction, or reporting service exists.
- Some config lookup directly reads `system_config` because of shared storage.
- Current permissions chiefly model CRUD and are not broadly backend-enforced.
- Area → Business Unit is the actual organization structure; supervisor authority is incomplete.
- Job Position/Plantilla are not complete Qualification Standards/vacancy models.
- Frontends declare Next.js 16.2.6, not the plan's Next.js 15 assumption.
- At Phase 0 discovery, PrimeHR did not exist. It now exists through the completed Phase 1 foundation and administration work.

## Foundation files created

```text
docs/prime-hrm/PHASE_0_ARCHITECTURE_DISCOVERY.md
docs/prime-hrm/PRIME_HRM_PROGRESS.md
docs/prime-hrm/PHASE_1A_COMPETENCY_FOUNDATION.md
docs/prime-hrm/PHASE_1A_1_HARDENING.md
PrimeHR/**
contracts/openapi/primehr-v1.yaml
```

Subsequent Phase 1B and Phase 1C implementation/review documents are maintained in `docs/prime-hrm/`. Exact current change sets and verification evidence are recorded in the applicable review manifest rather than duplicated in this foundation list.

## Implementation ledger

- Maven: `PrimeHR` added to the root reactor; not to HRISApp.
- Tables: category, competency, proficiency scale, proficiency level, behavioral indicator, position profile, and position profile requirement.
- Migrations: equivalent PostgreSQL and SQL Server V1 through V8 scripts.
- APIs: Phase 1 competency APIs, Phase 2 Position Profile APIs, Phase 3.1 assessment draft administration, Phase 3.2 assigned-assessor execution/return, and Phase 3.3 validation/person-profile reads; Administrative and HRM retain their authoritative integration endpoints.
- Contract: `contracts/openapi/primehr-v1.yaml`.
- UI routes/pages: standalone `prime-hr-software` SSO, competency administration, and Position Competency Profiles; Employee Portal launch integration.
- Existing module behavior, Jasper reports, messaging, storage, and deployment: unchanged.

## Verification

Performed:

- read the complete Master Plan V2;
- inspected backend reactor, entry points, HRISApp assembly, configuration, entities/controllers, security, SSO, permissions, tests, and Jasper resources;
- inspected all five frontend repositories for routes, dependencies, auth/config/SSO, permissions, sidebars, and patterns;
- confirmed no existing PrimeHR implementation;
- confirmed no Flyway/Liquibase or Testcontainers dual-provider migration suite;
- preserved unrelated user work.

Phase 1A command and result:

```text
.\mvnw.cmd -pl PrimeHR -am verify
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Phase 1A.1 final gates:

```text
.\mvnw.cmd clean test
.\mvnw.cmd clean package
Surefire XML: 119 tests, 0 failures, 0 errors, 0 skipped
PrimeHR: 24 tests, 0 failures, 0 errors, 0 skipped
Both reactor commands: BUILD SUCCESS
```

`Common`, `EmployeePortal`, and `HRISApp` execute zero tests in the current reactor. This pre-existing coverage gap is not hidden by the successful build.

Phase 1A.1 real-provider validation passed against Neon PostgreSQL 17.10 and local SQL Server 2017 Express (14.0), each in an isolated `primehr_phase1a1_20260803_v3` schema. Flyway applied V1, Hibernate validated it, and six provider-test invocations passed on each engine. See `PHASE_1A_1_HARDENING.md` for exact command shapes, failures found and fixed, test coverage, and retained validation schemas.

## Risks

1. Critical: direct HRISApp inclusion would bind PrimeHR to the legacy datasource.
2. Critical: tracked configuration/source contains secret material requiring externalization and rotation.
3. Critical: frontend permissions alone do not protect data/decisions; Phase 1B protects PrimeHR administration server-side through live fail-closed Administrative authorization, while unrelated legacy endpoints retain their existing enforcement maturity.
4. Medium: Flyway 9.22.3 reports PostgreSQL 17.10 newer than its tested maximum PostgreSQL 15, although the real migration/integration suite passed.
5. High: supervisor and complete Qualification Standards ownership remain unresolved.
6. High: applicant/employee identity separation must be enforced.
7. Medium: duplicated frontend helpers may drift.
8. Medium: dynamic identity-to-agency resolution is not yet implemented; the required server-side single-agency scope is intentionally safe but not multi-agency capable.

## Decisions needed before affected phases

- supervisor relationship authority and contract;
- Qualification Standards ownership;
- acceptance of standalone-first deployment;
- applicant authentication, document storage, and retention policy;
- repeatable CI credentials/containers for PostgreSQL and SQL Server;
- governance, approval roles, and the dedicated `canPublish` permission required before activation/publishing is designed.

## Next phase

Phase 3.4 was explicitly approved and completed. Administrative permissions, all four PrimeHR routes, repeatable Playwright acceptance, user/testing documentation, and in-scope browser defect corrections pass. The final repository, secret, diff, clean-package, and review-manifest gates also pass. Phase 4 has not started.

## Master Plan V2 alignment

Phase 1A, Phase 1A.1, Phase 1B, and Phase 1C are controlled delivery slices of Master Plan V2 Phase 1 - Competency Foundation. Together they cover competency categories, dictionary records, dynamic proficiency scales/levels, behavioral indicators, effective dating/versioning, read APIs/UI, draft administration, RBAC, audit, controlled immutable publication, and PostgreSQL/SQL Server portability. They intentionally exclude position profiles, person assessments, gap analysis, and RSP/SPMS/L&D/R&R functionality as required by the Master Plan.

Master Plan Phase 2 is complete. Phase 2.1 implements authoritative Job Position/Plantilla references, exact competency/level requirements, and effective-dated draft/version foundations without duplicating the Administrative position master. Phase 2.2 implements submission/approval, ACTIVE snapshots, precedence resolution, and exact-version comparison. Phase 2.3 implements the Administrative permission controls, standalone PrimeHR UI, accepted browser behavior, and repeatable Playwright coverage. Master Plan Phase 3 is complete, including assessment administration/execution, human validation, immutable person profiles, Administrative and PrimeHR UI, and Playwright acceptance. All Phase 4 gap functionality remains unimplemented.

## Proactive execution and approval workflow

This ledger is the canonical handoff between phases. A separate web-chat review is optional, not required for deciding the next step. At the end of every phase, Codex must update this section and present the recommended next action to the user.

The recurring phase gate is:

- [ ] Confirm the requested phase and explicit exclusions against the Master Plan.
- [ ] Inspect the current repositories and active instructions before implementation.
- [ ] Preserve unrelated work and record the starting Git status.
- [ ] Implement only the approved phase.
- [ ] Run focused tests plus the appropriate full build/package gate.
- [ ] Validate PostgreSQL and SQL Server for provider-sensitive work, or record the exact blocker.
- [ ] Check authentication, authorization, agency scope, and denied behavior as applicable.
- [ ] Run `git status --short`, `git diff --stat`, and `git diff --check`.
- [ ] Audit new files for credentials, generated artifacts, IDE files, and accidental later-phase work.
- [ ] Update the phase detail document and this progress ledger.
- [ ] State the next recommended scope, exclusions, risks, and decisions needed.
- [ ] Ask for explicit approval before starting the next major phase.

### Current checkpoint

| Gate | Status | Evidence/action |
|---|---|---|
| Phase 1B.1 backend | Passed | lifecycle, audit, Administrative authorization, OpenAPI, and SSO implemented |
| Real provider gates | Passed | fresh V1+V2 and populated V1-to-V2 on PostgreSQL 17.10 and SQL Server 14.0 |
| Phase 1B.2 UI | Passed | Administrative and Employee Portal builds; standalone lint, strict type-check, and build |
| Full reactor package | Passed | all nine reactor projects built successfully after Phase 1B.1 |
| Secret/configuration audit | Passed for this change set | runtime credentials are environment placeholders; test values are synthetic |
| Generated/IDE artifact audit | Passed | `target/` is ignored; `.idea` changes reverted |
| Phase-boundary audit | Passed | no publishing, activation, hard delete, HRISApp integration, or later-domain implementation |
| Git whitespace check | Passed | `git diff --check` and new-file trailing-whitespace scan clean |
| Backend commit/push | Complete | Phase 1B backend and documentation checkpoint `12eb6ee` |
| Frontend deployment | Intentionally deferred | Administrative, Employee Portal, and standalone PrimeHR changes remain local to avoid affecting QA/Vercel |
| Phase 1B | Complete for current checkpoint | automated gates passed and Administrative/Employee Portal behavior manually validated by user |
| Phase 1C.1 backend | Passed | focused and full Maven gates, authorization/lifecycle/audit tests, OpenAPI, and real PostgreSQL/SQL Server fresh and V2-to-V3 validation passed |
| Phase 1C.2 UI implementation | Passed automated build gate | user confirmed `npm run build` succeeds in both Administrative and PrimeHR frontends |
| Phase 1C manual acceptance | Passed | ordinary publishing, incomplete validation, competency dependencies, two-tab conflict, denied visibility, audit refresh, immutability, and administrator publishing verified by user |
| Phase 1C repository/secret audit | Passed with repository caveats | no credential-like additions or generated artifacts found in the Phase 1C change set; unrelated backend and `.env` changes remain present and must be excluded from a selective commit |
| PrimeHR consolidated configuration | Passed SQL Server; PostgreSQL rerun pending | one application file requested by user; 46 tests and real local SQL Server startup/Flyway/API passed, while consolidated PostgreSQL switching has not been rerun |
| Phase 2.1 implementation boundary | Passed | no submit/return/approve/resolve/compare endpoints, no UI, no person/assessment/report/HRISApp behavior |
| Phase 2.1 affected clean package | Passed | Administrative 25 and PrimeHR 64 tests; zero failures, errors, or skips; Common has zero tests |
| Phase 2.1 SQL Server fresh V1-V4 | Passed | 8/8 in retained isolated schema `primehr_phase21_sql_20260813_fresh` |
| Phase 2.1 SQL Server populated V3-to-V4 | Passed | 1/1 in retained isolated schema `primehr_phase21_sql_20260813_upgrade` |
| Phase 2.1 PostgreSQL fresh/upgrade | Waived as blocking gate | no usable local datasource was available; user approved SQL Server-primary validation while retaining provider-neutral code, dual migrations, and parity tests |
| Phase 2.2 start gate | Passed | Phase 2.1 SQL Server, package, authorization, portability, and boundary gates passed under the revised acceptance policy |
| Phase 2.2 affected tests/package | Passed | Administrative 26 and PrimeHR 73 tests; zero failures, errors, or skips; Common has zero tests; affected Maven package succeeded |
| Phase 2.2 SQL Server fresh V1-V5 | Passed | 9/9 in retained isolated schema `primehr_phase22_sql_20260813_fresh2`; Flyway V5 and Hibernate validation passed |
| Phase 2.2 SQL Server populated V4-to-V5 | Passed | 1/1 in retained isolated schema `primehr_phase22_sql_20260813_upgrade`; existing draft preserved with null lifecycle metadata |
| Phase 2.2 PostgreSQL live run | Not run by user direction | equivalent PostgreSQL V5, PostgreSQL-mode Flyway/Hibernate, provider-neutral JPA, and migration-parity gates pass; live provider remains unverified |
| Phase 2.2 backend boundary | Passed | no Phase 2.3 UI, person profiles, assessments, gaps, reports, notifications, or HRISApp integration implemented |
| Phase 2.3 approval gate | Passed | user explicitly approved Administrative permission controls and PrimeHR Position Profile UI |
| Phase 2.3 automated frontend gates | Passed | PrimeHR strict type-check, ESLint, and production build; Administrative focused ESLint and production build |
| Phase 2.3 Administrative lint script | Existing limitation | `npm run lint` uses removed `next lint` behavior under Next.js 16; direct ESLint of the changed file passes |
| Phase 2.3 repository/secret/boundary audit | Passed for implemented source | no credential-like additions, explicit `any`, generated/IDE files, or Phase 3+ behavior; unrelated Administrative `.env` edit remains preserved |
| Phase 2.3 manual browser acceptance | Passed | user confirmed allowed/denied, validation, submit/return/resubmit/approve, admin override, immutability, conflict, history, successor, comparison, and resolution behavior |
| Phase 2.3 Playwright matrix | Passed | 8/8 against local SQL Server in 32.1 seconds on the final commit-readiness run; reused fixtures and zero skips |
| Phase 2.3 conflict correction | Passed | stale 409 closes/reset the edit form before current-data reload; first accepted value remains authoritative |
| Phase 2.3 documentation | Complete | repeatable E2E runbook and PRIME-HRM user guide created |
| Phase 2 boundary | Passed | no person profiles, assessments, gaps, or other Phase 3 behavior implemented |
| Phase 2 commit | Complete | backend `51922ea`; PrimeHR frontend `a8f34eb`; both repositories clean after user commit |
| Phase 3 repository/master-plan comparison | Complete | HRM owns employee/appointment; supervisor authority remains incomplete; PrimeHR owns assessments/person profiles |
| Phase 3 scope | Approved | user approved the exact 3.1-3.4 scope and authorized 3.1, then 3.2 only after all 3.1 gates pass |
| Phase 3.1 HRM subject contract | Passed | authenticated minimal employee/current-appointment list/detail contract; no PDS, password, salary, contact, or unrelated history |
| Phase 3.1 authorization | Passed | additive Assess/Validate/Finalize/Data Scope contract; legacy values fail closed; direct HRM denial and PrimeHR exact-action/scope guards tested |
| Phase 3.1 V6 draft foundation | Passed | dual migrations, DRAFT cycle/tool/subject/assessor domain/API, audit, atomic optimistic child writes, and no later-phase execution behavior |
| Phase 3.1 SQL Server | Passed | fresh V1-V6: 9 tests; populated V5-V6: 1 test; zero skipped; isolated schemas retained |
| Phase 3.1 affected build | Passed | Administrative 33, HumanResource 45, PrimeHR 82; zero failures/errors/skips; clean package successful |
| Phase 3.1 PostgreSQL | Portability passed; live run unverified | dual V6 migration, PostgreSQL-mode Flyway, structural parity, provider-neutral Java/JPA; live run non-blocking by approved policy |
| Phase 3.2 lifecycle/execution | Passed | tool publication, cycle open/close, exact assigned work, ratings/evidence, completeness, submit, atomic return, correction, and resubmit |
| Phase 3.2 authorization/concurrency | Passed | cross-subject, invalid identity/scope/level, missing evidence, duplicate submit, stale writes, blank return reason, and closed-cycle mutations are blocked without partial data |
| Phase 3.2 affected tests/package | Passed | Common 3, Administrative 33, HumanResource 45, PrimeHR 86; 167 total, zero failures/errors/skips; clean package successful |
| Phase 3.2 SQL Server | Passed | fresh V1-V7: 9 tests; populated V6-V7: 1 test; isolated schemas retained; an earlier additive default-schema V5-V7 run is disclosed in the phase detail |
| Phase 3.2 PostgreSQL | Portability passed; live run unverified | equivalent V7 migration, PostgreSQL-mode Flyway V1-V7, structural parity, provider-neutral Java/JPA; live run non-blocking by approved policy |
| Phase 3.2 repository/boundary audit | Passed | diff check clean; no new secret/generated artifact or provider-specific shared query; no validation decision/person profile/UI/Phase 4 behavior |
| Phase 3.2 documentation | Complete | `PHASE_3_2_ASSESSMENT_EXECUTION.md` records scope, API, persistence, tests, provider evidence, limitations, and boundary |
| Phase 3.3 validation/profile lifecycle | Passed | independent human decisions, exact contributor/version checks, self-validation separation, audited administrator override, atomic immutable profile generation, and predecessor closure |
| Phase 3.3 authorization/history | Passed | own versus agency-wide reads, immutable version history, exact-version access, and deterministic latest-as-of selection |
| Phase 3.3 affected tests/package | Passed | Common 3, Administrative 33, HumanResource 45, PrimeHR 90; 171 total, zero failures/errors/skips; clean package successful |
| Phase 3.3 SQL Server | Passed | fresh V1-V8: 9 tests; populated V7-V8: 1 test; isolated schemas `primehr_phase33_fresh_20260827` and `primehr_phase33_upgrade_20260827` retained |
| Phase 3.3 PostgreSQL | Portability passed; live run unverified | equivalent V8 migration, PostgreSQL-mode Flyway V1-V8, structural parity, and provider-neutral Java/JPA; live run non-blocking by approved policy |
| Phase 3.3 repository/boundary audit | Passed | diff check clean; no new credential/generated artifact or provider-specific shared query; no Phase 3.4 UI/Playwright or Phase 4 behavior |
| Phase 3.3 documentation | Complete | `PHASE_3_3_HUMAN_VALIDATION_PERSON_PROFILES.md` records scope, APIs, persistence, tests, provider evidence, limitations, and boundary |
| Phase 3.4 permission/UI implementation | Passed | Administrative Phase 3 feature/action/data-scope controls and four typed PrimeHR routes implemented with backend enforcement and accessible form labels |
| Phase 3.4 focused browser acceptance | Passed | 3/3 against local SQL Server: allowed/denied surfaces, assigned work, stale conflict, submission, validation override, immutable latest/history |
| Phase 3 full Playwright regression | Passed | 11/11 in 2.0 minutes against isolated local APIs and SQL Server; zero skipped |
| Phase 3.4 frontend gates | Passed | both UIs type-check/build; PrimeHR lint clean; Administrative lint has one pre-existing Sidebar hook warning and zero errors |
| Phase 3.4 backend clean tests | Passed | Common 3, Administrative 33, HumanResource 45, PrimeHR 90; 171 total, zero failures/errors/skips |
| Phase 3.4 backend clean package | Passed | affected five-project reactor packaged successfully; all 171 tests executed again with zero failures/errors/skips |
| Phase 3.4 PostgreSQL live run | Not run by user direction | SQL Server browser acceptance is blocking; provider-neutral application code, dual V6-V8 migrations, parity, and PostgreSQL-mode gates retained |
| Phase 3.4 documentation | Complete | UI/acceptance detail, Playwright runbook, and user guide cover the delivered Phase 3 workflows |
| Phase 3 final repository audit | Passed with disclosed local caveat | all three `git diff --check` and untracked whitespace scans clean; no sensitive signature/test password found; generated E2E/build outputs ignored; unrelated Administrative `.env` remains excluded |
| Phase 3 boundary | Passed | no competency gap, L&D referral, applicant assessment, Phase 4 UI/API/table, or later-domain behavior implemented |
| Phase 3 | Complete | final review manifest records files, contracts, provider evidence, tests, risks, and commit guidance |

### Next recommended action

Review, commit, and push the completed Phase 3 change sets. After Phase 3 is committed, prepare an exact Phase 4 scope for review without implementing it. Phase 4 must not start without explicit user approval.

## Rollback

Before Phase 1B deployment, rollback is reverting the Phase 1B changes while retaining the committed Phase 1A/1A.1 foundation. After V2 reaches an environment, use an explicit reviewed forward migration; do not delete tables or edit an applied migration automatically.
