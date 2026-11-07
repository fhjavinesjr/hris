# Phase 6F.2 — Formal Performance PDF Reports Review

Date: 2026-10-23  
Status: Complete and verified; stop before Phase 6F.3

## Delivered reports

| Report | Eligible source | Template/API behavior |
|---|---|---|
| OPCR, DPCR, IPCR planning | Approved or superseded approved commitment revision | Exact commitment sections/items; dynamic rows; no legacy fixed-slot limit |
| OPCR, DPCR, IPCR rated | Finalized final-rating revision | Frozen final-rating accomplishment and server-calculated score/dimensions |
| Monitoring | Monitoring case with submitted accomplishment revision | Employee-visible accepted/submitted updates; draft, voided, and superseded updates excluded |
| Coaching | Issued or later employee-visible coaching session | Agenda, goals, agreed actions, response, and action items; private notes excluded |
| Development plan / intervention | Issued or later intervention lifecycle | Employee-visible plan/checkpoints and response; private PMT notes excluded |

All four JRXML templates are SQL-free and use `JRBeanCollectionDataSource`. They are packaged resources,
compile during application startup, and use dynamic pagination. The layouts preserve the government
SPMS concepts found in the CSC baseline and reviewed Philippine agency forms without treating one
agency's pixel layout, fixed DPCR slots, PBB matrix, or hard-coded weight as universal policy.

## APIs and safeguards

Six authenticated `application/pdf` endpoints are under
`/api/primehr/v1/performance-management/reports`. Existing feature guards and data scopes are enforced,
responses are inline/no-store/nosniff, and every successful generation records the metadata fingerprint
and PDF SHA-256 in the audit trail.

Missing legacy metadata is captured once at first eligible generation. PrimeHR reads current agency
branding and exact Plantilla/Job Position names through authenticated Administrative integration
endpoints, combines them with the stored approval route, and saves the V36 immutable snapshot. Missing
or incomplete authoritative sources fail closed; browser-supplied signatories or headings are not used.

## Verification

- Administrative and PrimeHR main-source compilation passed together.
- Focused source, preparation, renderer, service, controller, privacy, and OpenAPI tests passed:
  Administrative 2/2 and PrimeHR 38/38, including denial before metadata-source lookup or capture.
- Renderer tests generated all four PDF families and exercised a 30-row dynamic DPCR pagination case.
- Rated-report tests prove later monitoring edits are not used in place of the frozen final-rating source.
- Coaching tests prove private notes are not accessed for PDF output.
- The full affected-module clean package passed: Administrative 70/70 and PrimeHR 347/347, with zero
  failures, errors, or skips and both production JARs built successfully.

No frontend route was added in Phase 6F.2, so Playwright would not exercise this unit. Phase 6F.4 owns
the report workspace, Employee Portal print actions, permissions, and browser acceptance. No live
PostgreSQL or SQL Server endpoint was configured for this run; V36 provider migration parity and upgrade
coverage were retained from Phase 6F.1, and Phase 6F.2 adds no schema change.

## Stop boundary

No register, aggregate analytics, performance-outcome feed, automatic competency/L&D/rewards/RSP
decision, CoreHR mutation, permission seed, or frontend behavior was introduced. Those remain in the
approved later units.
