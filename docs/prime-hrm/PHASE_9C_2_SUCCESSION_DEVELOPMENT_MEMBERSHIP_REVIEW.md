# Phase 9C.2 - Succession Development and Membership Lineage Review

Reviewed: 2026-11-04

Status: Complete

## Delivered boundary

Phase 9C.2 adds the controlled development and pool-membership layer after an authorized `INCLUDED`
succession-pool decision:

- target-position development plans bounded by the final pool decision and its membership dates;
- frozen links to the employee's approved IDP items and completed L&D cases;
- measurable development milestones with due dates, evidence-controlled completion, and final-state
  immutability;
- candidate acknowledgment or response without granting panel or membership authority;
- periodic human readiness reviews with separate employee-visible and restricted deliberation text;
- controlled plan activation and completion; and
- append-only membership activation, correction, expiry, removal, and reinstatement lineage.

The implementation does not automatically enroll an employee in L&D, mutate an IDP or completion,
extend an expired pool decision, refer or apply the employee to RSP, rank candidates, promote, appoint,
change salary, or execute payroll activity.

## Authorization, privacy, and integrity

- `primehr.succession-development` is an independent Administrative permission feature.
- Access combines permission action, Own/Assigned/Agency data scope, authenticated employee identity,
  frozen panel assignment, lifecycle state, authoritative source ownership, and optimistic record version.
- The candidate cannot perform panel or membership decisions on their own plan, including when otherwise
  granted Agency Wide scope.
- Own-scope responses omit restricted milestone feedback, review deliberation, and restricted membership
  rationale.
- Only approved IDP items and completed L&D cases belonging to the same employee may be linked; their
  identifiers, versions, fingerprints, summaries, and dates are frozen on the plan.
- Activation requires at least one authoritative development source and one measurable milestone.
- Plan and milestone dates cannot exceed the authoritative pool-decision validity window.
- Completion requires every milestone to be completed with evidence and the latest periodic review to
  explicitly recommend completion.
- Removal requires a latest `REMOVE_FROM_POOL` review. Expiry cannot be recorded early, correction or
  reinstatement cannot extend beyond the final decision, and an expired membership cannot be silently
  reinstated without a new decision.
- Membership history is append-only and sensitive mutations are transactionally audited.

## Database and contracts

Paired PostgreSQL and SQL Server V51 migrations add:

- `succession_development_plan`
- `succession_development_source`
- `succession_development_milestone`
- `succession_development_review`
- `succession_pool_membership_event`

The OpenAPI contract exposes plan listing/detail, controlled creation and draft editing, authoritative-source
and milestone management, activation, candidate acknowledgment, periodic review, completion, and membership
lineage routes.

## Verification

- Focused Phase 9C.2 domain, service privacy and ownership, permission, OpenAPI, V51 migration parity,
  populated V50-to-V51 upgrade, fresh V1-to-V51 migration, repository bootstrap, and Hibernate validation
  checks passed.
- Administrative effective-authorization regression passed: 35/35.
- Complete clean PrimeHR regression passed: 478/478 with zero failures, errors, or skips.
- The clean regression also proved that the earlier `ScreeningAssignmentEligibilityServiceTest` error was
  a stale compiled test-class artifact; the correctly packaged source test passed 4/4 after `clean`.
- Live PostgreSQL and SQL Server endpoints were not configured. Paired migration parity/upgrade checks and
  the PostgreSQL-compatible H2 provider gate passed; live-provider validation remains a deployment gate.

## Next gated unit

Phase 9D may add position-continuity plans, confidential bench-coverage views, and a controlled RSP referral
packet with receipt/status/vacancy linkage. RSP remains authoritative: succession must not automatically
create an application, shortlist or select a candidate, issue an appointment, or mutate CoreHR.
