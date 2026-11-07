# Phase 9C.1 - Succession Readiness and Pool Decision Review

Reviewed: 2026-11-04  
Status: Complete

## Delivered boundary

Phase 9C.1 adds the confidential human decision layer after candidate eligibility:

- one readiness assessment per eligible candidate, with frozen candidate, cycle, charter, readiness-scale,
  target Position Profile, Person Profile, and calculation metadata;
- a read-only competency application port that compares the exact effective target Position Profile with
  the candidate's exact effective validated Person Profile;
- immutable criterion-level gap snapshots, including missing/non-comparable evidence instead of invented scores;
- an effective frozen panel roster, standing-conflict/self-conflict recusal, candidate-specific conflict
  declaration, and authorized replacement from the frozen non-conflicted panel;
- reasoned configurable criterion findings with separate employee-visible and restricted deliberation views;
- quorum-controlled human readiness recommendation using agency-configured readiness levels; and
- an authorized append-only final `INCLUDED` or `NOT_INCLUDED` pool decision with employee-visible reason,
  restricted rationale, decision identity, and policy-derived membership validity.

There is no employee-wide ranking, leaderboard, automatic readiness recommendation, automatic inclusion,
development plan, L&D enrollment, RSP referral, promotion, appointment, salary, or payroll action.

## Authorization, privacy, and integrity

- `primehr.succession-readiness` is an independent Administrative permission feature.
- Access combines permission action, Own/Assigned/Agency data scope, authenticated employee identity,
  frozen panel assignment, recusal status, lifecycle, source freshness, and optimistic record version.
- A candidate cannot perform panel action or access restricted deliberation on their own assessment even if
  that employee otherwise has Agency Wide permission.
- Recused/replaced panel members cannot continue acting or use Assigned scope to read restricted deliberation.
- Missing competency results, non-comparable versions, or `NEEDS_EVIDENCE` findings block recommendation.
- Finalization rechecks the HRM appointment fingerprint and exact target/person profile identifiers and
  versions. Changed sources require a successor review rather than overwriting history.
- Pool inclusion requires a readiness level that explicitly permits membership.
- Sensitive mutations and final decisions are transactionally audited.

## Database and contracts

Paired PostgreSQL and SQL Server V50 migrations add:

- `succession_readiness_assessment`
- `succession_readiness_gap_item`
- `succession_readiness_panel_assignment`
- `succession_readiness_criterion_finding`
- `succession_pool_decision`

The OpenAPI contract exposes readiness listing/detail, initialization, conflict/recusal, replacement,
findings, recommendation, and final-decision routes.

## Verification

- Focused Phase 9C.1 domain, service privacy, exact target-gap, permission, OpenAPI, migration parity,
  populated V49-to-V50 upgrade, fresh V1-to-V50 migration, repository bootstrap, and Hibernate validation
  checks passed.
- Administrative effective-authorization regression passed: 34/34.
- Complete PrimeHR regression passed: 467/467 with zero failures, errors, or skips.
- Live PostgreSQL and SQL Server endpoints were not configured; paired structural checks and the
  PostgreSQL-compatible H2 provider gate were used. Live-provider validation remains a deployment gate.

## Next gated unit

Phase 9C.2 may add target-position succession development plans, approved IDP/L&D references, milestones,
employee acknowledgment, periodic readiness review, and correction/expiry/removal lineage. It must not
automatically enroll an employee in L&D, mutate CoreHR, refer to RSP, rank employees, promote, or appoint.
