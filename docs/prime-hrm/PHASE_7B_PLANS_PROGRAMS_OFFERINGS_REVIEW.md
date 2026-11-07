# Phase 7B - L&D Plans, Programs, and Offerings Review

Reviewed: 2026-10-26  
Status: Complete and verified; stopped before Phase 7C

## Delivered scope

Phase 7B adds three separately versioned and audited PrimeHR aggregates:

- office/agency consolidated L&D plans with exact approved-IDP-item or authorized organizational-need snapshots;
- program designs tied to a published plan, published intervention definition, and published evaluation framework; and
- scheduled offerings with ordered sessions, capacity, modality, venue/platform, provider, resource-person snapshots, approved budget, funding, and registration windows.

Plans follow `DRAFT -> SUBMITTED -> APPROVED -> PUBLISHED`; program design requires a published plan.
Programs follow `DRAFT -> SUBMITTED -> APPROVED`; offerings follow
`DRAFT -> SUBMITTED -> APPROVED -> PUBLISHED`. Published offerings may be explicitly cancelled.
Approved/published records are not edited in place: supported changes create forward draft successors
that preserve the series and frozen source/version fingerprints.

All mutable HR and Administrative facts are read through authenticated typed clients. Employee,
appointment, organization, plan-source, policy, intervention, evaluation, provider, and resource-person
facts required for historical meaning are frozen with IDs/versions, readable snapshots, fingerprints,
and audit metadata. No PrimeHR code joins or writes CoreHR tables.

## Authorization and integrity

- `primehr.ld-agency-plan` and `primehr.ld-program` are recognized by Administrative effective authorization.
- Access, add, edit, submit, approve, publish, and archive/cancel checks are enforced at the backend with
  Assigned Records or Agency Wide scope as appropriate.
- Plan and program approval require Agency Wide authority and independent actors. Offering approval also
  rejects its submitter and the accountable program owner.
- Optimistic record versions, lifecycle guards, stale authoritative-snapshot checks, duplicate-source
  checks, schedule boundaries, non-overlapping sessions, active provider/resource checks, and audit
  before/after capture are enforced transactionally.

## Database and contract

Paired PostgreSQL and SQL Server V39 migrations add the seven Phase 7B tables and their foreign keys,
constraints, uniqueness rules, and indexes. The OpenAPI contract now covers plan consolidation,
program design, offering sessions/resources, lifecycle transitions, publication, cancellation, and
forward revisions.

## Verification

- Focused Phase 7B acceptance: **89/89 passed** across domain, service integration, permission,
  OpenAPI, migration parity, populated V38-to-V39 upgrade, fresh migration, and Hibernate validation.
- Full clean affected reactor before the final publication correction: Common **3/3**,
  Administrative **73/73**, and PrimeHR **379/379** passed; the exact final code was then subjected to
  the same clean reactor gate recorded in the progress ledger.
- V39 was structurally exercised in H2 PostgreSQL compatibility mode for both fresh and populated
  upgrades. No configured live PostgreSQL or SQL Server endpoint was available in this workspace.

## Stop boundary

No nomination, nomination approval, participant enrollment/confirmation/substitution, Training Order,
attendance, completion, evaluation result, PDS/CoreHR write, downstream competency/performance/reward
decision, frontend, report, or Playwright behavior was introduced. Browser acceptance remains scheduled
for Phase 7E when the interfaces exist. The next gated unit is Phase 7C.
