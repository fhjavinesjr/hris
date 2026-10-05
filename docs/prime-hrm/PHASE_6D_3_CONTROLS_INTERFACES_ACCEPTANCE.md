# Phase 6D.3 Controls, Interfaces, and Acceptance Review

Prepared: 2026-09-24

Status: Complete; stop before Phase 6E

## Delivered outcome

Phase 6D.3 exposes the completed monitoring and coaching backend through permission-aware Administrative, PrimeHR, and Employee Portal interfaces. The backend remains authoritative for action permission, data scope, ownership, stored supervisor route, PMT process role, lifecycle, optimistic version, idempotency, and note privacy.

Administrative now defines the exact `primehr.performance-monitoring` and `primehr.performance-coaching` features. Each independently supports Access, Add, Edit, Submit, and Approve with Own Records, Assigned Records, and Agency Wide scopes.

PrimeHR adds `/prime-hr/performance-monitoring` as the Monitoring Inbox for assigned supervisors and authorized PMT users. It provides approved commitment-item snapshots, progress and evidence history, update decisions, accomplishment return, coaching and restricted notes, action-item verification/reopen, and policy-gated mid-cycle controls. No rating action is present.

Employee Portal adds `/employee-portal/selfservice/MyPerformanceMonitoring`. It uses only authenticated-own endpoints, exposes no employee selector, and supports own update drafts/submission/corrections, evidence, final accomplishment submission, employee-visible coaching acknowledgment, own action-item follow-through, and mid-cycle acknowledgment. Restricted supervisor notes are not rendered.

The monitoring case response now includes safe, read-only snapshots of the exact approved commitment items. This lets an employee create the first progress update without granting access to the separate planning feature or allowing target mutation.

## Verification

- Administrative strict TypeScript, focused permission-file ESLint, and production build/package passed. Its complete lint still reports unrelated pre-existing unused helpers in System Setup and hook warnings in PhilHealth and Sidebar.
- PrimeHR strict TypeScript, complete ESLint, and production build/package passed.
- Employee Portal strict TypeScript, complete ESLint, and production build/package passed.
- Backend focused monitoring plus OpenAPI contract suite passed 28/28.
- Phase 6D.3 Playwright passed 3/3; combined Phase 6C.4 and 6D.3 focused regression passed 6/6.
- Complete clean PrimeHR regression passed 306/306 with zero failures, errors, or skips. The executable Spring Boot JAR packaged successfully.
- PostgreSQL and SQL Server V31/V32 migration parity, fresh-schema, and upgrade preservation were verified in 6D.1/6D.2. No live PostgreSQL or SQL Server endpoint was configured for this interface run.

## Deployment and rollback

1. Apply the paired provider-specific V31 and V32 forward migrations if the target database has not received them.
2. Deploy the matching PrimeHR backend, Administrative UI, PrimeHR UI, and Employee Portal artifacts.
3. Configure the two exact permission features and require users to sign in again so effective permissions are refreshed.
4. Smoke-test denied access, assigned supervisor access, authenticated-own portal access, evidence download, coaching note privacy, and mid-cycle policy gating.

If an application rollback is required, restore the prior application artifacts together. Do not reverse an already applied monitoring/coaching migration or delete workflow history. Any schema correction must be a separately reviewed forward migration.

## Stop boundary

Phase 6D.3 introduces no self-rating, supervisor rating, Q/E/T score, weighted or adjectival rating, calibration, final-rating approval, rating acknowledgment, appeal, PIP, downstream L&D/R&R/RSP feed, or monitoring report. Phase 6E requires separate scope review and explicit approval.
