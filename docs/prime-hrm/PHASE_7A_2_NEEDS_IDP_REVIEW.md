# Phase 7A.2 - Learning Needs and Individual Development Plans Review

Prepared: 2026-10-25  
Status: Complete and verified; stopped before Phase 7B

## Delivered behavior

- Added provider-neutral learning-needs intake with explicit source type, immutable employee,
  appointment, competency/source identifiers, source version/fingerprint, intended outcome, success
  measure, target date, priority, lifecycle, optimistic version, and audit history.
- Kept competency-gap referral intake explicit. An authorized actor records `ACCEPTED`, `RETURNED`,
  or `DECLINED`, a reason, actor/time, and the exact immutable Phase 4 referral version/fingerprint.
  Acceptance creates the learning need; no rule or algorithm accepts a referral automatically.
- Linked an accepted referral deterministically to the exact IDP item created from its need. Removing
  that editable draft item clears the link before deletion. Both transitions are audited, and neither
  rewrites the immutable Phase 4 referral.
- Added IDPs with frozen HumanResource employee/appointment and Administrative organization
  snapshots, need-backed item snapshots, content revision, optimistic locking, and immutable approved
  history through successor amendments.
- Implemented `DRAFT -> SUBMITTED -> RECOMMENDED -> APPROVED`, return/resubmission, cancellation,
  and approved-plan amendment. Submission snapshots the configured Administrative route for
  `LEARNING_INDIVIDUAL_DEVELOPMENT_PLAN`; only the exact current actor can decide, and current
  appointment identity is revalidated before each decision.
- Prevented an employee from finally approving their own IDP and rejected incomplete, non-contiguous,
  cross-business-unit, duplicate-actor, stale, cancelled-need, and duplicate-need submissions.

## Authorization and contracts

- Added exact backend permission guards for `primehr.ld-needs` and
  `primehr.ld-individual-plan`, including action and Own/Assigned/Agency data-scope enforcement.
- Administrative effective authorization recognizes both feature keys, and the existing typed
  approval-route source accepts the IDP request code.
- Extended the PrimeHR OpenAPI contract with needs, referral disposition, IDP item, route, lifecycle,
  error, and optimistic-version contracts. Employee identity and approval state remain server-owned.

## Persistence

Paired PostgreSQL and SQL Server `V38__ld_needs_and_idp.sql` migrations add:

1. `ld_learning_need`
2. `ld_referral_disposition`
3. `ld_individual_plan`
4. `ld_individual_plan_item`
5. `ld_idp_route`
6. `ld_idp_route_step`

The schema uses portable types, constraints, foreign keys, uniqueness, audit columns, and optimistic
record versions. Fresh V1-to-V38 validation, V37-to-V38 populated upgrade preservation, structural
parity, and Hibernate mapping validation passed. No configured live PostgreSQL or SQL Server endpoint
was available for this run.

## Verification

- Focused Phase 7A.2 domain/service/security/migration tests: passed.
- PrimeHR OpenAPI contract tests: 31/31 passed.
- Focused Administrative authorization/route tests: 28/28 passed.
- Clean affected reactor: Common 3/3, Administrative 73/73, PrimeHR 370/370 passed with zero
  failures, errors, or skips; both executable production JARs were built.

Playwright was intentionally not run because Phase 7A.2 contains no frontend interface. End-to-end
browser acceptance remains scheduled for Phase 7E with the Administrative, PrimeHR, and Employee
Portal L&D interfaces.

## Boundary and next gate

Phase 7A.2 adds no automatic referral acceptance, consolidation, program/offering, nomination,
attendance, completion, evaluation result, report, UI, CoreHR/PDS write, or downstream competency,
performance, reward, payroll, or timekeeping mutation.

The next unit is Phase 7B. Its exact versioned office/agency plan, consolidation, program design,
scheduled offering, session/resource/capacity, and budget-fact scope must be reviewed and confirmed
before implementation.
