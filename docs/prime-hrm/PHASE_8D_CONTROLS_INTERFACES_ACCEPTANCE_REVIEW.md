# Phase 8D - PRAISE Controls, Interfaces, Reports, and Acceptance Review

Date: 2026-11-04  
Status: Complete

## Delivered scope

Phase 8D completes the approved Rewards and Recognition (PRAISE) phase without crossing into
Succession Planning or mutating CoreHR, Payroll, or Timekeeping records.

- Administrative exposes independent permission rules for governance, committee, cycle,
  nomination, eligibility, evaluation, award, fulfillment, reports, and aggregate analytics.
- PrimeHR provides permission-filtered workspaces for governance; committee and cycles;
  nominations; eligibility and evaluation; awards and fulfillment; and reports and analytics.
- Employee Portal provides authenticated-own **My Nominations & Recognitions**. It does not accept
  an employee selector and exposes only lifecycle-eligible withdrawal and print actions.
- Backend guards remain authoritative for action, data scope, ownership/assignment, frozen
  committee role, lifecycle, and record version. Direct routes fail closed without the exact
  feature permission.
- SQL-free Jasper reports are generated from typed service DTOs through a bean collection data
  source for nomination, evaluation,
  award/citation source, and annual-register records. The generic layout is explicitly an ISOFT
  operational record, not an agency-approved CSC/HAP nomination form.
- Aggregate analytics requires the separate `primehr.rr-analytics` Agency Wide permission and
  suppresses results below `primehr.reporting.minimum-aggregate-cohort` (default 5).

## Form comparison decision

The legacy application contains agency forms HRD-009-0 through HRD-012-0 for athletic/sports,
career and development, best organizational unit, and Gawad Kagitingan nominations. They remain
reference layouts only. Current CSC Honor Awards Program material uses cycle-specific prescribed
forms and requires the applicable PRAISE/head-of-agency endorsement. Phase 8D therefore does not
misrepresent the generic operational PDF as the current official form. Deployment-specific
branding or a prescribed form requires separate agency approval and a reviewed template.

## Verification

| Gate | Result |
|---|---|
| Focused authorization | Passed: Administrative 30/30; R&R award guard 1/1 |
| Jasper compilation/rendering | Passed: clean compile and representative bean-driven PDF renderer 1/1 |
| Backend affected reactor | Passed: Common 3/3, Administrative 79/79, PrimeHR 436/436 |
| Administrative frontend | Strict TypeScript, focused lint, production build/package passed |
| PrimeHR frontend | Strict TypeScript, focused lint, production build/package passed |
| Employee Portal | Strict TypeScript, focused lint, production build/package passed |
| Packaged Playwright | Passed 3/3 in Chromium |

The browser suite verifies exact-permission direct-route denial, privacy-threshold analytics, and
authenticated-own Employee Portal recognition visibility/printing. The Employee Portal package
logged expected fallback warnings because the centralized runtime-configuration service was not
started in the isolated acceptance run; mocked contracts and all assertions passed.

## Portability and boundaries

Phase 8D introduces no schema migration. It uses the already validated paired PostgreSQL and SQL
Server V43-V46 structures and provider-neutral repositories/service data. No live PostgreSQL or
SQL Server endpoint was configured for this final UI/report unit.

No automatic award, employee ranking, payout, promotion, appointment, salary change, competency
change, L&D completion, performance mutation, or Succession Planning behavior was introduced.
Phase 8 is complete. The next gated work is Phase 9 Succession Planning discovery and scope only;
implementation requires separate approval.
