# Phase 6A.1 Policy, Cycle, and Calendar Review

Date: 2026-09-08  
Status: Complete; all gates passed before Phase 6A.2 began  
Authority: `PHASE_6A_SPMS_POLICY_CYCLE_CALENDAR_PMT_SCOPE_APPROVAL.md`

## Delivered

- Versioned agency-scoped SPMS policy definitions with draft, publish, revision, and retirement lifecycles.
- Immutable published versions, effectivity conflict protection, reasons, optimistic versions, and audit snapshots.
- Draft/open/closed/cancelled performance cycles bound to an exact published policy version.
- Ordered milestone calendars with policy-required milestones, timezone/window validation, and draft-only replacement.
- Backend feature guards for `primehr.performance-policy` and `primehr.performance-cycle`.
- PostgreSQL and SQL Server V23 migrations for `spms_policy`, `spms_policy_version`, `spms_cycle`, and `spms_cycle_milestone`.
- Complete request/response/status OpenAPI schemas under `/performance-management`.

## Gate evidence

- Focused lifecycle, permission, migration-parity, and OpenAPI tests passed before 6A.2 began.
- Final combined Phase 6A plus fresh-schema gate: 61 tests, zero failures/errors/skips.
- Fresh PostgreSQL-compatible Flyway schema test applied 25 migrations through V24, verified the Phase 6A tables/indexes/foreign keys, and passed 9/9 checks with Hibernate validation.
- Final PrimeHR suite: 252 tests, zero failures/errors/skips; the immediately preceding clean run produced the same result.
- Affected reactor package: `HRIS_Project` and `PrimeHR` succeeded; executable PrimeHR JAR produced.

## Boundary

No PMT behavior was introduced until the 6A.1 gates passed. No template, commitment, IPCR/DPCR/OPCR, rating, calibration, coaching, appeal, report, Administrative permission row, frontend, Playwright, or Phase 6B behavior was added.

Live PostgreSQL and SQL Server instances were not configured for this run. Structural parity tests cover both scripts and the PostgreSQL migrations execute in the compatibility Flyway gate; live-provider validation remains an explicit 6A.3 deployment-acceptance item and does not weaken the paired migration contract.
