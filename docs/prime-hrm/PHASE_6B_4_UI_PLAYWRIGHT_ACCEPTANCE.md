# Phase 6B.4 UI and Playwright Acceptance

Date: 2026-09-09  
Status: Passed; stopped before Phase 6C

## Delivered controls

- Administrative permission rows: `primehr.performance-rating-scale`, `primehr.performance-success-indicator`, and `primehr.performance-template`, each with independent Access/Add/Edit/Publish and agency-wide scope.
- PrimeHR routes: `/prime-hr/performance-rating-scales`, `/prime-hr/performance-success-indicators`, and `/prime-hr/performance-templates`.
- Structured rating-band editing with declared-range coverage, server-enforced gaps/overlaps, controlled rounding/direction/source fields, deterministic sample preview, effectivity, publish/retire, and revision lineage.
- Structured success-indicator editing with exact effective policy/scale selection, targets, evidence requirements, weighted QET/agency-defined dimensions, one rubric level per scale band, and preview breakdown.
- Structured OPCR/DPCR/IPCR/custom template composition with exact effective source selections, section/item totals, backend-normalized weights, readiness diagnostics, and overall/section preview breakdown.

All pages fail closed on their exact feature key. Published definitions are read-only; corrections use successor drafts. The UI provides no script, SQL, expression, commitment, assignment, rating, or Phase 6C action.

## Acceptance evidence

| Gate | Result |
|---|---|
| PrimeHR ESLint | Passed |
| PrimeHR strict TypeScript | Passed |
| PrimeHR production build/package | Passed; 35 routes |
| Administrative ESLint | Passed; one unrelated pre-existing `Sidebar.tsx` dependency warning |
| Administrative production build/package | Passed; 41 App Router routes plus existing API routes |
| Focused Phase 6B.4 Playwright | 4/4 passed |
| Complete PrimeHR Playwright | 49/49 passed, zero skips, 5.5 minutes |

The focused browser matrix covers exact navigation/direct-route denial, absence of Phase 6C, rating-band gap feedback, deterministic previews, immutable publication/revision history, exhaustive indicator levels, three-level weight visibility/readiness, Authorization-header transport, and stale HTTP 409 guidance.

## Provider, deployment, privacy, and rollback

Phase 6B.4 adds no database schema. It consumes the provider-neutral Phase 6B APIs and paired V25-V27 SQL Server/PostgreSQL migrations accepted in 6B.1-6B.3. Deploy the Administrative and PrimeHR packaged outputs together with the accepted backend and migrations; permission changes take effect after a new login/ruleset load.

Rollback before deployment is reverting only the 6B.4 frontend/permission-row files. After deployment, remove route exposure through permission configuration or deploy a reviewed predecessor build; do not alter applied V25-V27 migrations or delete definition history. The screens store configuration only and do not expose employee/applicant performance results, evidence files, coaching notes, appeals, or medical/demographic data.

## Stop boundary

No objective catalogue, organizational/employee assignment, cascading, target allocation, OPCR/DPCR/IPCR commitment instance, submission, recommendation, approval, accomplishment, monitoring, or rating behavior was introduced. Phase 6C requires scope definition and separate approval.
