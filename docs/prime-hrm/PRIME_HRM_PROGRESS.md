# ISOFT PRIME-HRM Progress Ledger

Last updated: 2026-08-03
Current phase: Phase 1B - Competency Draft Administration
Status: Implemented; final commit-readiness review pending

Canonical detail: [PHASE_0_ARCHITECTURE_DISCOVERY.md](./PHASE_0_ARCHITECTURE_DISCOVERY.md)

## Phase status

| Phase | Status | Delivered |
|---|---|---|
| 0 — Architecture Discovery | Complete | repository inventory; architecture correction; ownership, integration, migration, security and Phase 1 decisions |
| 1A — Standalone Competency Foundation | Complete | standalone module, isolated datasource profiles, dual migrations, read-only APIs, OpenAPI and tests |
| 1A.1 - Foundation Hardening | Complete | trusted configured agency scope, competency-read authority, Flyway-created-schema tests, real PostgreSQL and SQL Server validation |
| 1B — Competency Draft Administration | Implemented | dual-provider lifecycle/audit backend, Administrative authorization, SSO, permission configuration, and standalone management UI |
| 2+ | Not started | none |

## Decisions recorded

- One modular PrimeHR backend, not six microservices.
- Future `PrimeHR` module in the `hris` reactor; standalone-first with its own database.
- No HRISApp runtime dependency until isolated second-datasource tests pass.
- Future `prime-hr-software` management/applicant frontend; existing Employee Portal for employee self-service.
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
- PrimeHR does not currently exist.

## Files created

```text
docs/prime-hrm/PHASE_0_ARCHITECTURE_DISCOVERY.md
docs/prime-hrm/PRIME_HRM_PROGRESS.md
docs/prime-hrm/PHASE_1A_COMPETENCY_FOUNDATION.md
docs/prime-hrm/PHASE_1A_1_HARDENING.md
PrimeHR/**
contracts/openapi/primehr-v1.yaml
```

## Implementation ledger

- Maven: `PrimeHR` added to the root reactor; not to HRISApp.
- Tables: category, competency, proficiency scale, proficiency level, and behavioral indicator.
- Migrations: equivalent PostgreSQL and SQL Server V1 scripts.
- APIs: Phase 1A reads plus audited permission-protected draft administration under `/api/primehr/v1/admin`, with agency resolved server-side; public minimal Actuator health.
- Contract: `contracts/openapi/primehr-v1.yaml`.
- UI routes/pages: standalone `prime-hr-software` SSO and competency administration; Employee Portal launch integration.
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

Phase 1B was explicitly authorized and is implemented. See `PHASE_1B_COMPETENCY_DRAFT_ADMINISTRATION.md` for the lifecycle, authorization, dual-provider, API, UI, and verification record, and `PHASE_1B_REVIEW_MANIFEST.md` for the exact independent-review inventory. Publishing remains excluded; active versions stay immutable and may only be cloned into successor drafts.

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
| Commit | Awaiting independent review | Exact file inventory and exclusions are in `PHASE_1B_REVIEW_MANIFEST.md`; stage each repository separately |
| Push | Awaiting user action | push only after reviewing the staged diff |
| Phase 1B | Implemented | final commit-readiness and independent review remain |

### Next recommended action

Perform independent diff review and manual end-to-end smoke testing across the backend, Administrative UI, Employee Portal, and standalone PrimeHR UI, then commit each repository separately. Do not begin publishing or Phase 1C/2 without a separately approved exact scope.

## Rollback

Before Phase 1B deployment, rollback is reverting the Phase 1B changes while retaining the committed Phase 1A/1A.1 foundation. After V2 reaches an environment, use an explicit reviewed forward migration; do not delete tables or edit an applied migration automatically.
