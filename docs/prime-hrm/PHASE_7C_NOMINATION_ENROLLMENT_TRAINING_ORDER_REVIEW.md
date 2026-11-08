# Phase 7C - L&D Nomination, Enrollment, and Training Order Review

Reviewed: 2026-10-26  
Status: Complete and verified; stopped before Phase 7D.1

## Delivered scope

Phase 7C adds a controlled participation workflow on top of published Phase 7B offerings:

- employee self-nomination, same-business-unit supervisor nomination, and authorized L&D staff nomination;
- exact approved-IDP-item, approved plan-source, or reasoned justified nomination sources;
- snapshotted `LEARNING_NOMINATION` approval routes with sequential recommendation and final approval;
- invitation, employee confirmation/decline, authorized cancellation, and controlled substitution;
- capacity, duplicate nomination, overlapping enrollment, active-employment, current-appointment,
  current-organization, lifecycle, and optimistic-write validation; and
- an immutable approved Training Order source for one published offering and its confirmed participants.

Nomination approval creates an invitation; it never records attendance or completion. A Training Order
can be finalized only after all invitations are resolved and at least one participant is confirmed.
Finalization freezes the exact offering and confirmed-participant roster and prevents later roster
mutation. The published offering already owns immutable session and resource-person snapshots, so the
report layer can compose the order from those exact records without duplicating mutable master data.

## Authorization and integrity

- `primehr.ld-nomination` and `primehr.ld-delivery` are recognized by Administrative effective
  authorization with independent actions and Own, Assigned, or Agency Wide scope.
- Employee operations force the authenticated employee identity. Supervisor initiation requires the
  current authoritative HR participant plus same-business-unit head/co-approver membership.
- Approval decisions require the current snapshotted route actor and revalidate appointment and
  organization facts before changing state.
- Substitution requires a separately approved replacement invitation for the same offering; it does
  not silently create or approve a nominee.
- One nomination per employee/offering is enforced consistently by service and database constraints.
- Every mutation is transactional, audited, lifecycle checked, and protected by optimistic versions.

## Database and contract

Paired PostgreSQL and SQL Server V40 migrations add nomination, route, route-step, enrollment,
Training Order source, and Training Order participant tables with provider-neutral constraints,
foreign keys, uniqueness rules, and indexes. The OpenAPI contract now covers nomination creation and
lifecycle, approval inbox decisions, enrollment confirmation/decline/cancellation/substitution, and
Training Order finalization/read models.

## Verification

- Clean focused Phase 7C PrimeHR suite: **91/91 passed**, covering domain rules, service integration,
  permissions, OpenAPI, migration parity, populated V39-to-V40 upgrade, fresh migration through V40,
  and Hibernate validation.
- Administrative effective-authorization regression: **25/25 passed**.
- Full clean affected reactor: Common **3/3**, Administrative **74/74**, and PrimeHR **387/387**
  passed with zero failures, errors, or skips; Administrative and PrimeHR production JARs were built.
- V40 was exercised in H2 PostgreSQL compatibility mode for both fresh and populated upgrades. No
  configured live PostgreSQL or SQL Server endpoint was available in this workspace.

## Stop boundary

No session attendance, credited-hour completion, certificate/evidence verification, evaluation
instrument/result, PDS/CoreHR write, downstream competency/performance/reward decision, frontend,
report rendering, or Playwright behavior was introduced. Browser acceptance remains scheduled for
Phase 7E. The next gated unit is Phase 7D.1.
