# Phase 5F.3 RSP Register and Process Analytics Review

Date: 2026-09-07  
Status: Passed; final reconciliation complete; Phase 5F.4 subsequently passed

## Delivered API and reports

- `GET /api/primehr/v1/rsp/reports/register`
- `GET /api/primehr/v1/rsp/reports/register.pdf`
- `GET /api/primehr/v1/rsp/reports/process-analytics`
- `GET /api/primehr/v1/rsp/reports/process-analytics.pdf`

The register is bounded to 1-100 publications per page, a maximum 366-day range, and a 10,000-publication safety scan. A publication may produce multiple rows when explicit history is requested. It accepts five explicit date bases: publication, publication closing, proceeding finalization, selection finalization, and handoff acknowledgment. Exact optional filters cover publication/proceeding/selection status, outcome, Job Position, Plantilla, Area, and Business Unit. Area is resolved through the authenticated Administrative API rather than direct database access. Current/latest application, screening, proceeding, selection, offer, and handoff revisions are the default. `includeHistory=true` labels preserved selection history separately and never silently double-counts it in current-state metrics.

## Metric contract

All calculations use the `Asia/Manila` reporting timezone and the exact inclusive date basis and filters selected by the operator.

- Current applications are the highest application version per applicant/publication. Drafts are excluded from the submitted funnel numerator; withdrawals remain counted.
- Current screening is the highest case revision for each current application. Finalized means `QUALIFIED` or `DISQUALIFIED`. Correction count means more than one preserved screening revision.
- Evaluation admission uses the latest proceeding and its admitted-candidate count.
- Selection outcomes use only the highest selection revision. `SELECTED`, `NO_SELECTION`, and `DEFERRED` are separate.
- Offers expose all four states: pending, accepted, declined, and expired.
- Latest handoffs expose every lifecycle state, plus an acknowledged-or-closed completion measure and retryable-failure measure. Failed delivery attempts remain visible in the register and auditable.
- Active publication-channel counts, controlled screening-disqualification reason counts, and current/history selection counts remain separately labeled.
- Durations use actual elapsed minutes divided by 1,440 for publication-to-close, close-to-screening-final, screening-to-proceeding-final, proceeding-to-selection-final, selection-to-offer-response, and accepted-offer-to-handoff-acknowledgment. Application-to-screening remains an additional operational measure. Null, incomplete, and negative pairs are excluded; the number of included pairs is shown.
- A zero denominator produces `0.00%`, and empty ranges return a typed empty page/no-data PDF rather than fabricated totals.

No demographic characteristic is collected or inferred. There is no candidate performance league table, quota, ranking analytic, prediction, or automated recommendation.

## Security, portability, and verification

- Independent agency-wide access permissions are `primehr.rsp-register-report` and `primehr.rsp-process-analytics`; success and denial are audited without report contents.
- Repository access is Spring Data derived-query/JPQL based. Jasper templates contain no SQL and there is no HumanResource table access or cross-domain write.
- The OpenAPI contract documents endpoints, limits, date basis, revision semantics, and the non-demographic boundary.
- PrimeHR clean suite passed 238 tests with zero failures/errors/skips. The 18 focused report/service/permission/OpenAPI tests also passed clean. Administrative passed 52 tests, and the complete nine-project reactor package succeeded.
- Generated register and analytics PDFs were opened through Playwright/Chrome and visually inspected. The four-page register has readable columns, repeated table headers and page footers; the analytics output has intact classification, filter, value, and definition fields.

Phase 5F.4 subsequently passed. Master Plan V2 has no Phase 5G; the next controlled phase is Phase 6A.
