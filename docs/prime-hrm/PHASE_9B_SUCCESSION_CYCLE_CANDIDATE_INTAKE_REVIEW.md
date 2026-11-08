# Phase 9B - Succession Cycle and Candidate Intake Review

Date: 2026-11-04  
Status: Complete; changes remain uncommitted

## Delivered boundary

Phase 9B adds the governed intake layer that precedes any readiness or pool-membership decision:

- versioned-policy and committee-bound succession cycles with chronological nomination and eligibility windows;
- human-authored position continuity-risk reviews against an approved, effective critical-position designation;
- authorized nomination and policy-permitted self-expression of interest;
- immutable HRM employee/active-appointment and Administrative target-position snapshots;
- candidate notice, authenticated consent, submission, reasoned eligibility review, and withdrawal;
- criterion-level eligibility findings with a separate employee-visible flag;
- private PDF/PNG/JPEG evidence with size/media/signature validation, safe filenames, SHA-256 checksum,
  private object keys, retention tags, visibility classes, authorized downloads, `no-store`, and audit;
- `Own Records`, effective-committee `Assigned Records`, and `Agency Wide` data-scope enforcement; and
- independent `primehr.succession-pool` authorization alongside the existing critical-position authority.

## Integrity and privacy controls

- A cycle may open only against published/effective succession policy and committee versions.
- Candidate intake is accepted only during the open nomination window and only for an approved,
  effective critical-position designation.
- Self-expression is permitted only when enabled by the frozen policy and only for the authenticated employee.
- The employee alone records consent; the employee cannot decide their own eligibility.
- Submission fails closed when the current HRM appointment fingerprint or Administrative target-position
  fingerprint differs from the frozen intake snapshot.
- Candidate-safe reads exclude committee-restricted evidence and non-employee-visible eligibility findings.
- At least one secured evidence object is required before submission.
- Optimistic record versions and transactional audit protect every lifecycle mutation.

## Persistence and portability

Paired V49 migrations create:

- `succession_cycle`
- `succession_continuity_risk_review`
- `succession_candidate`
- `succession_eligibility_finding`
- `succession_candidate_evidence`

The migrations use provider-appropriate timestamp/boolean types while preserving the same tables,
constraints, foreign keys, lifecycle checks, and indexes. No provider-specific application query was added.

## Verification

- Phase 9B domain, permission, OpenAPI, migration parity, and populated V48-to-V49 upgrade checks passed.
- PostgreSQL-compatible fresh V1-to-V49 migration, repository bootstrap, Hibernate validation, table/index/FK
  metadata, and inherited persistence regression passed: 60/60 focused tests.
- Administrative effective-authorization regression passed: 33/33.
- Complete PrimeHR regression passed: 457/457 with zero failures, errors, or skips.
- The affected Common, Administrative, and PrimeHR reactor production package build passed.
- Live PostgreSQL and SQL Server endpoints were not configured; paired structural checks and the
  PostgreSQL-compatible H2 provider gate were used. Live-provider validation remains a deployment gate.

## Explicit stop boundary

Phase 9B does not create a readiness result or score, candidate comparison/ranking, pool-membership decision,
succession development plan, automatic L&D action, RSP referral/application/selection, appointment/promotion,
salary/payroll action, frontend, report, analytics, or Playwright behavior.

## Next gated unit

Phase 9C.1 is the next proposed unit: conflict-controlled panel assignments, exact competency-gap decision
support, human readiness recommendation, and authorized final pool decision. It requires separate user approval
before implementation.
