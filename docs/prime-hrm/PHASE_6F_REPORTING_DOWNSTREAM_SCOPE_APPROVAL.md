# Phase 6F — Performance Reporting and Controlled Downstream Feeds

Date: 2026-10-23  
Status: Complete through Phase 6F.4; stop before a new domain phase

## 1. Purpose and boundary

Phase 6F completes reporting for the Phase 6 performance-management records and exposes a controlled, read-only outcome contract for later PrimeHR consumers. It does not make performance results an automatic competency, L&D, rewards, RSP, CoreHR, payroll, timekeeping, appointment, or disciplinary decision.

| Unit | Included | Stop boundary |
|---|---|---|
| 6F.1 | Government/legacy form comparison, immutable report-metadata contract, paired V36 migrations, portability and upgrade gates | No PDF, report endpoint, permission seed, UI, analytics, or downstream feed |
| 6F.2 | Bean-driven OPCR/DPCR/IPCR and monitoring/coaching/intervention PDFs from eligible authoritative revisions | No register, analytics, downstream mutation, or UI |
| 6F.3 | Paginated register, privacy-controlled aggregate analytics, and versioned read-only performance-outcome pull feed | No automatic consumer writes or decisions |
| 6F.4 | Administrative controls, PrimeHR reports workspace, own-record Employee Portal print actions, Playwright and documentation | Stop before rewards/recognition or another new domain phase |

## 2. Philippine government source comparison

Authoritative policy baseline:

- Civil Service Commission SPMS: <https://csc.gov.ph/programs/spms>
- CSC-approved NCDA SPMS manual and Annexes A-C: <https://ncda.gov.ph/wp-content/uploads/2022/06/Approved-NCDA-SPMS.pdf>
- DOH/ARMMC SPMS Guidelines and annex set: <https://armmc.doh.gov.ph/wp-content/uploads/2024/02/ARMMC_SPMS-Guidelines_Adoption-of-DOH-Guidelines.pdf>
- CSC-approved PCA SPMS manual/IPCRF example: <https://www.pca.gov.ph/images/In_compliance_with_sec106/CSC_APPROVED_PCA_SPMS_MANUAL.pdf>

CSC establishes the four-stage SPMS cycle and requires user-friendly forms that align individual and organizational goals. It does not prescribe one universal pixel-identical OPCR/DPCR/IPCR layout for every agency. Agency forms and later amendments remain subject to the agency's CSC-approved SPMS.

| Required government-form concept | Legacy ISOFT JRXML | Phase 6F treatment |
|---|---|---|
| Rating period, ratee/head, office and position | Present | Populate from immutable metadata, never browser-entered report parameters |
| Strategic, core and support outputs | OPCR/IPCR present; DPCR has hard-coded A/B/C slots | Render dynamic stored sections/items; no six-row limit |
| Success indicator, target and measure | Present | Render the exact approved commitment revision |
| Actual accomplishment | Present | Render the exact monitoring/rating source revision |
| Quantity, quality, timeliness and average where applicable | Present as Q/E/T/A | Render configured dimensions; never assume fixed dimensions or weights |
| Budget and accountable unit/person | In government examples; incomplete in legacy layouts | Print only where approved template/data supplies it |
| Category/final numerical and adjectival ratings | Present | Use immutable server-calculated final-rating values only |
| Remarks and development recommendations | Present | Exclude private PMT/coaching notes from employee output |
| Review/approval signatories | Present, but legacy titles are mutable | Resolve authoritatively and freeze names/titles in metadata |
| Agency heading/address/branding | Current-setting parameters | Resolve from Administrative and freeze a branding fingerprint |

Legacy PBB matrices, performance-bonus calculations, fixed DPCR slots, SQL queries and hardcoded weights are not authoritative Phase 6F inputs.

## 3. Phase 6F.1 contract

V36 adds `spms_report_metadata_snapshot`. One immutable row is identified by agency, report kind, source type, source ID and source business revision. It freezes source revisions/fingerprints, template version, employee/position/organization data, applicable supervisor/approver data, rating period, agency branding fingerprint, capture actor/time, and a server-calculated SHA-256 metadata fingerprint.

Capture is idempotent for identical data and rejects different authoritative values for the same frozen source revision. Existing records cannot retroactively prove what mutable titles were at the original lifecycle event; their first eligible report must be labeled as metadata captured at generation time. New integrations should capture at approval/finalization.

## 4. Phase 6F.2 eligibility

- Planning OPCR/DPCR/IPCR: approved commitment revision only.
- Rated OPCR/DPCR/IPCR: final-rating revision only; current/superseded state must be explicit.
- Monitoring: exact data scope; draft/private supervisor notes excluded from employee output.
- Coaching and intervention/development: employee-visible content only.
- Missing position, signatory, branding or source fingerprint fails closed rather than substituting mutable or blank data.

## 5. Downstream and permissions

Phase 6F.3 may expose a versioned, paginated and audited pull contract. It cannot update competencies/gaps, create an L&D referral, nominate a reward, rank an RSP candidate, or mutate CoreHR.

Planned permission keys are `primehr.performance-form-report`, `primehr.performance-monitoring-report`, `primehr.performance-register-report`, `primehr.performance-analytics`, and `primehr.performance-outcome-feed`. All are backend-enforced. The outcome feed is agency-wide/service-level only and is not an Employee Portal feature.

## 6. Phase 6F.1 verification

- Idempotent capture and immutable conflict tests.
- PostgreSQL/SQL Server V36 structural parity.
- Populated V35-to-V36 upgrade preservation.
- Fresh V1-to-V36 migration and Hibernate validation in the provider harness.

No live PostgreSQL or SQL Server endpoint was configured during this run.

## 7. Phase 6F.2 delivery

Phase 6F.2 adds SQL-free, bean-driven OPCR/DPCR/IPCR planning and rated PDFs plus monitoring,
coaching, development-plan, and intervention PDFs. The six authenticated endpoints enforce the
existing performance feature permission/data scope, lifecycle eligibility, exact record ownership,
inline PDF headers, no-store caching, and generation audit hashes.

On first eligible generation, the backend captures missing immutable metadata from authenticated
Administrative sources for agency branding and Plantilla/Job Position titles together with the
stored commitment approval route. Existing exact snapshots are reused. Missing authoritative
settings, position data, route data, or source fingerprints fail closed.

The templates contain no SQL and no developer-machine resource paths. Rated forms read frozen final
rating source items rather than later monitoring edits. Coaching and intervention outputs exclude
private supervisor/PMT notes. Dynamic form rows paginate and are not constrained by the legacy
fixed DPCR slots.

Phase 6F.2 intentionally has no frontend route, so Playwright is not applicable to this unit.
Automated report rendering, PDF response, metadata preparation, source failure, privacy, OpenAPI,
migration, application-context, and full backend regression gates are used instead. Phase 6F.4 owns
the PrimeHR/Employee Portal print interfaces and browser acceptance.

## 8. Phase 6F.3 delivery

Phase 6F.3 adds three independently authorized, agency-wide read surfaces:

- a date-filtered and paginated final-rating register that defaults to the current revision while
  allowing explicitly requested immutable history;
- aggregate metrics and form/rating/business-unit breakdowns protected by a configurable minimum
  distinct-employee cohort (`PRIMEHR_REPORTING_MINIMUM_AGGREGATE_COHORT`, default 5); and
- a versioned, paginated, audited `/api/primehr/integration/v1/performance-outcomes` pull feed using
  an exclusive `changedAfter` and inclusive `changedThrough` window.

Suppressed analytics expose neither metrics, breakdown cells, nor the exact below-threshold cohort
count. Each breakdown cell independently meets the same threshold. The outcome feed returns only
the current final-rating revision and factual lifecycle states/fingerprints; it excludes appeal
grounds, responses, reasons, narrative, support plans, private notes, objectives, and evidence.

Administrative recognizes the three canonical permission keys so authorization can resolve at
runtime, but Phase 6F.3 does not add permission-management UI or default role grants. No consumer
write, competency update, L&D referral, reward nomination, RSP decision, CoreHR mutation, or frontend
route was introduced. Phase 6F.4 remains responsible for controls, interfaces, and Playwright.

## 9. Phase 6F.4 delivery

Administrative exposes the five exact reporting permission keys with independently persisted Access
and data-scope controls. Formal planning/rating reports and monitoring/development reports support
Own, Assigned, or Agency scope; the register, aggregate analytics, and outcome feed remain
agency-wide-only at backend enforcement.

PrimeHR adds a permission-filtered **Performance Reports** workspace for eligible formal PDFs,
paginated final-rating register results, and privacy-controlled aggregate analytics. Employee Portal
adds **My Performance Reports**, populated only from authenticated `/mine` sources and without an
employee selector. Its shared route guard redirects unauthorized direct URLs to Dashboard.

Formal report endpoints now require both the underlying workflow permission and the exact report
permission, intersect their scopes, and fail closed for incompatible Own/Assigned combinations.
The browser cannot expand backend data scope. The versioned performance-outcome feed remains an
API-only service integration and is not exposed in either frontend.

Phase 6F.4 passed frontend lint/type/build/package gates, focused backend tests, the complete clean
Administrative and PrimeHR package regression, and focused packaged-frontend Playwright acceptance.
No schema change, downstream write, reward nomination, competency or L&D mutation, RSP decision,
CoreHR change, payroll/timekeeping effect, or new-domain behavior was introduced.
