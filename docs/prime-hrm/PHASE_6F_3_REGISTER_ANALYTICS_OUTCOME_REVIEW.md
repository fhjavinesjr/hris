# Phase 6F.3 - Register, Aggregate Analytics, and Outcome Feed Review

Date: 2026-10-24  
Status: Complete and verified; stop before Phase 6F.4

## Delivered behavior

| Surface | Contract | Controls |
|---|---|---|
| Performance register | `GET /api/primehr/v1/performance-management/reports/register` | Date range, cycle, form, business unit, employee and rating-label filters; 0-based pages up to 100 rows; current revision by default; explicit immutable history option |
| Aggregate analytics | `GET /api/primehr/v1/performance-management/reports/analytics` | Counts, average score, acknowledgment/appeal/intervention rates, and form/rating/business-unit breakdowns |
| Outcome pull feed | `GET /api/primehr/integration/v1/performance-outcomes` | Version `1.0`; current outcomes only; deterministic pagination; exclusive `changedAfter` and inclusive `changedThrough`; record and outcome fingerprints |

All three surfaces are read-only and audited. Register, analytics, and feed access use separate
Administrative feature keys and require agency-wide scope. Administrative now recognizes those
keys, but permission controls/default grants remain part of Phase 6F.4.

## Privacy and downstream boundary

The analytics minimum distinct-employee cohort is configured through
`PRIMEHR_REPORTING_MINIMUM_AGGREGATE_COHORT` and defaults to 5. A below-threshold result returns no
metrics or breakdowns and does not reveal the exact cohort count. Each breakdown group must also
meet the threshold independently.

The outcome feed contains factual identifiers, rating values, lifecycle statuses, source and result
fingerprints, versions, and timestamps. It deliberately omits employee responses, appeal grounds,
decision reasons, narrative accomplishments, coaching/support content, objectives, private notes,
and evidence. It does not write to Competency, L&D, Rewards and Recognition, RSP, CoreHR, Payroll,
Timekeeping, or any consumer domain.

## Verification

- Focused Administrative authorization: 23/23 passed.
- Focused PrimeHR service/controller/permission/OpenAPI: 35/35 passed.
- Clean affected build: Administrative 71/71 and PrimeHR 354/354 passed with zero failures,
  errors, or skips; both production JARs were built.
- The existing paired PostgreSQL/SQL Server V36 schema remained unchanged. Fresh provider-compatible
  migration and Hibernate validation passed through V36. No live PostgreSQL or SQL Server endpoint
  was configured for this run.
- No frontend was changed, so Playwright is not applicable to Phase 6F.3. Browser acceptance remains
  an explicit Phase 6F.4 gate.

## Stop boundary

No Administrative permission UI, PrimeHR reporting workspace, Employee Portal print action,
automatic consumer decision/write, or Rewards and Recognition behavior was added. Proceed only with
separate approval for Phase 6F.4.
