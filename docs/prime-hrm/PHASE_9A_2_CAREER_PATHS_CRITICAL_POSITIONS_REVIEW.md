# Phase 9A.2 — Career Pathways and Critical-Position Governance Review

## Delivered boundary

Phase 9A.2 adds versioned career-family definitions with explicit source-to-target position pathways,
versioned qualitative criticality frameworks, and a human-controlled critical-position designation
workflow. It does not create employee candidate pools, assess employee readiness, rank people, or
change appointments, promotion, RSP, CoreHR, Payroll, or Timekeeping data.

## Career pathways

- Career families use immutable published versions and effective dates.
- Each pathway explicitly identifies an Administrative `JOB_POSITION` or `PLANTILLA` source and
  target and classifies the relationship as `VERTICAL`, `LATERAL`, or `DEVELOPMENTAL`.
- Position labels, identifiers, source fingerprints, and fetch timestamps are snapshotted from the
  authoritative Administrative integration. Publication re-fetches both endpoints and rejects stale
  snapshots.
- Draft pathway collections require unique relationships and display order. Published versions are
  changed only through a successor revision.

## Critical-position governance

- Agencies define versioned criticality frameworks containing ordered qualitative criteria and
  evidence guidance. No numeric criticality score or hardcoded designation threshold is used.
- A designation binds one exact Administrative position snapshot, one published/effective
  criticality-framework version, and one published/effective succession-committee charter version.
- Exactly one reasoned finding (`MET`, `NOT_MET`, or `NOT_APPLICABLE`) is required for every frozen
  framework criterion.
- The workflow is `DRAFT → SUBMITTED → APPROVED` or `REJECTED`; approved records may be retired.
  The submitter cannot approve or reject the same record, and decisions require an active member of
  the bound committee roster.
- A position cannot have another draft, submitted, or approved designation. Historical rejected and
  retired records remain available.
- Source changes, stale record versions, incomplete findings, invalid effectivity, and invalid
  committee assignments fail closed. Every mutation writes an audit event.

## Authorization

- `primehr.succession-career-path` uses independent Access/Add/Edit/Publish/Delete controls and
  requires Agency Wide scope.
- `primehr.succession-critical-position` uses independent Access/Add/Edit/Submit/Approve/Finalize
  controls. Configuration and authoring actions require Agency Wide scope; Assigned Records access
  and decisions are intersected with active membership in the bound committee.
- API routes remain employee-JWT authenticated. Hidden UI links are not relied upon for security.

## Persistence and portability

V48 adds eight tables for career families/versions/path links, criticality frameworks/versions/
criteria, critical-position designations, and qualitative findings. PostgreSQL and SQL Server
migrations use equivalent provider-appropriate timestamp, boolean, integer, constraint, foreign-key,
and index structures. The migration is additive and preserves V47 data.

## Verification

- Java compilation passed.
- Domain lifecycle and permission-scope tests passed.
- OpenAPI contract, paired V48 migration parity, and populated V47-to-V48 preservation passed.
- Administrative effective-authorization regression passed.
- Focused Phase 9A.2, OpenAPI, and migration checks passed 48/48; Administrative effective
  authorization passed 32/32.
- The complete PrimeHR regression passed 450/450 with zero failures, errors, or skips, including
  PostgreSQL-compatible fresh migration/Hibernate validation.
- Live PostgreSQL and SQL Server endpoints were not configured in this workspace and were not run.

## Boundary retained

No employee candidate/pool, talent nomination, readiness assessment, readiness score, development
plan, continuity referral, vacancy handoff, promotion recommendation, frontend, PDF, analytics, or
Playwright behavior was introduced. Those remain gated to later approved units.

## Next gated unit

Phase 9B may introduce the succession cycle, continuity/risk review, candidate nomination/intake,
notice/consent/withdrawal, eligibility review, secured evidence, and frozen target-position snapshots.
It must still stop before readiness results, ranking, development plans, RSP referral, or appointment.
