# ISOFT PRIME-HRM Progress Ledger

Last updated: 2026-08-02  
Current phase: Phase 0 — Architecture Discovery  
Status: Complete; implementation intentionally not started

Canonical detail: [PHASE_0_ARCHITECTURE_DISCOVERY.md](./PHASE_0_ARCHITECTURE_DISCOVERY.md)

## Phase status

| Phase | Status | Delivered |
|---|---|---|
| 0 — Architecture Discovery | Complete | repository inventory; architecture correction; ownership, integration, migration, security and Phase 1 decisions |
| 1 — Competency Foundation | Not started | none; requires a later instruction |
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
```

## Implementation ledger

- Migrations/tables/entities/repositories: none.
- APIs/contracts implemented: none.
- UI repositories/routes/pages: none.
- Security/config/deployment changes: none.
- Jasper reports: none.

## Verification

Performed:

- read the complete Master Plan V2;
- inspected backend reactor, entry points, HRISApp assembly, configuration, entities/controllers, security, SSO, permissions, tests, and Jasper resources;
- inspected all five frontend repositories for routes, dependencies, auth/config/SSO, permissions, sidebars, and patterns;
- confirmed no existing PrimeHR implementation;
- confirmed no Flyway/Liquibase or Testcontainers dual-provider migration suite;
- preserved unrelated user work.

Not applicable because only documentation changed: Maven/Next builds, database migrations, Jasper compilation/PDF inspection.

## Risks

1. Critical: direct HRISApp inclusion would bind PrimeHR to the legacy datasource.
2. Critical: tracked configuration/source contains secret material requiring externalization and rotation.
3. Critical: frontend permissions alone do not protect data/decisions.
4. High: no migration framework or dual-provider integration harness.
5. High: supervisor and complete Qualification Standards ownership remain unresolved.
6. High: applicant/employee identity separation must be enforced.
7. Medium: duplicated frontend helpers may drift.
8. Medium: Phase 1 read-only scope conflicts with draft CRUD acceptance wording.

## Decisions needed before affected phases

- supervisor relationship authority and contract;
- Qualification Standards ownership;
- acceptance of standalone-first deployment;
- applicant authentication, document storage, and retention policy;
- SQL Server CI test mechanism/license;
- whether Phase 1 is read-only or includes audited draft CRUD.

## Next phase

Phase 1 is not authorized by the present request. If later authorized:

1. recheck repository state/instructions;
2. resolve read-only versus draft CRUD;
3. prove standalone datasource isolation;
4. establish PostgreSQL and SQL Server migration tests;
5. define minimum Administrative permission/SSO contract;
6. implement only the Competency foundation boundary in the architecture document;
7. update this ledger after verification.

## Rollback

Remove the two Phase 0 Markdown files. There is no database, API, runtime, frontend, report, or deployment state to reverse.
