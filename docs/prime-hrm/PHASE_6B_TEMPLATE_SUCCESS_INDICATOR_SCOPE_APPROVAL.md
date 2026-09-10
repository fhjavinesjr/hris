# Phase 6B Performance Template and Success-Indicator Scope Approval

Date: 2026-09-09  
Status: Phase 6B complete through 6B.4; stopped before Phase 6C  
Authority: `ISOFT_PRIME_HRM_CODEX_MASTER_PLAN_V2.md`, Phase 6B and sections 5.6, 5.13, 10.1-10.2, 16-18

## 1. Outcome and delivery sequence

Phase 6B creates immutable, agency-configurable definitions for performance rating scales, the Index of Success Indicators (ISI), and OPCR/DPCR/IPCR-equivalent templates. It defines deterministic formula metadata but does not create a performance plan, commitment, accomplishment, rating, approval, report, or downstream referral.

The controlled implementation sequence is:

- **6B.1 - Rating scale and computation contract:** versioned rating scales/bands, controlled calculation vocabulary, backend permissions, audit, OpenAPI, V25 paired migrations, and deterministic validation tests.
- **6B.2 - Index of Success Indicators:** versioned output/target/measure definitions, applicable dimensions and per-level rubrics, backend permissions, audit, OpenAPI, V26 paired migrations, only after all 6B.1 gates pass.
- **6B.3 - Performance template composition:** versioned OPCR/DPCR/IPCR/custom templates, sections, exact ISI-version references, normalized weights, publish/readiness rules, audit, OpenAPI, and V27 paired migrations, only after all 6B.2 gates pass.
- **6B.4 - Administrative controls, PrimeHR UI, and Playwright acceptance:** three separately approved permission rows and management screens, production frontend gates, documentation, and browser acceptance, only after all 6B.3 gates pass.

This scope-definition request does not authorize implementation. A later explicit implementation approval must preserve the sequence and stop before 6B.4 unless that UI/control slice is separately approved. All Phase 6B work stops before Phase 6C.

## 2. Contracts inspected

### Current Phase 6A foundation

- PrimeHR owns the `performanceManagement` domain and agency scope; SPMS is a configured policy label.
- V23 provides immutable/effective policy versions and cycles bound to an exact published policy version.
- V24 provides effective PMT/member history and authenticated HRM eligibility checks.
- Existing patterns provide action-specific backend guards, `AGENCY_WIDE` scope, optimistic `recordVersion`, reasoned lifecycle actions, fingerprints/audit, OpenAPI, pageable reads, and paired PostgreSQL/SQL Server migrations.
- Phase 6A contains no template, success-indicator, rating-scale, commitment, rating, or performance report tables or APIs.

### Master Plan V2

- Templates may represent OPCR, DPCR, IPCR, or other approved forms and define sections, outputs/KRAs/KPIs/success indicators, measure type, target, weight, dimensions, rating scale, required evidence, and computation rule.
- Performance definitions and rating scales require immutable historical snapshots/version references.
- Configuration must never execute arbitrary code or bypass legal, authorization, integrity, or history controls.
- PrimeHR owns performance commitments/ratings but never writes directly to Administrative, HRM, Timekeeping, or Payroll tables.

### Legacy models and workflows

- `Ipcr`, `Dpcr`, and `Opcr` combine a filing period, employee, mutable function rows, recommendation/approval flags, final averages, adjectival rating, and signatories.
- Separate Core, Strategic, and Support row classes repeat output, `successIndicator`, Q/E/T flags and grades, five textual rubrics per dimension, verification, remarks, rate/weight, and calculated averages.
- `TargetSpms` identifies an employee/station/department date window; it is not a normalized success-indicator catalogue.
- `SpmsLocking` stores an employee/station/department date window and numeric lock type; it is not a versioned cycle milestone or definition lock contract.
- Legacy approval statuses are only Pending, Approved, Disapproved, and Cancel. Those transaction transitions belong to Phase 6C or later.

### Legacy Jasper references

- The user-directed `web/reports/spms/Individual_Performance.jrxml` exposes Q/E/T columns, Core/Support percentages, 1-5 labels, averages, summary ratings, signatories, and a fixed row layout.
- The operational PCMC family is under `web/reports/pcmc/spms`: `OPCR.jrxml`, `DPCR.jrxml`, `IPCR.jrxml`, Q/E/T function subreports, `ipcr_report.jrxml`, the monthly summary, and the development-plan form.
- TWD has additional OPCR/IPCR/monthly-summary variants. These prove that layout and workflow vary by agency.
- The PCMC reports use bean fields/subreports, but OPCR/DPCR flatten some data into fixed A/B/C row slots. They are layout/data-dictionary references only and are not a suitable persistence model.

## 3. Legacy adjudication

1. **Concepts retained:** configurable function sections; output/KRA/KPI text; target plus measure; Q/E/T or agency dimensions; 1-5-style scales where approved; verification/evidence; weights; summary/adjectival labels; OPCR/DPCR/IPCR form identities.
2. **Values not made global defaults:** legacy IPCR 80/0/20, OPCR 45/45/10, and DPCR 60/30/10 are agency/template data. Seed them only through an explicitly approved deployment data plan.
3. **Legacy arithmetic is non-authoritative:** inspected code contains integer division, repeated nested accumulation, contradictory strategic weighting, duplicated target fields, unsafe null expressions, swallowed exceptions, and unreachable/open interval rating boundaries. None may be copied.
4. **No legacy transaction import in 6B:** OPCR/DPCR/IPCR filings, approvals, accomplishments, grades, final averages, `TargetSpms`, and `SpmsLocking` need a later reconciliation/migration design after the new planning/rating contracts exist.
5. **No Jasper implementation in 6B:** reports are reserved for Phase 6F. Phase 6B must preserve enough structured metadata for later bean-driven forms without fixed six-row columns.

## 4. Phase 6B.1 - rating scale and computation contract

### 4.1 Rating scale aggregate

Create a stable rating-scale identity and immutable versions:

- agency, stable code, version number, title, description, legal/policy reference;
- exact published Phase 6A policy-version ID;
- numeric minimum/maximum, decimal precision, rounding mode;
- `DRAFT`, `PUBLISHED`, `RETIRED`, effectivity, supersession, actor/time/reason, optimistic version;
- ordered, non-overlapping bands with code, numeric score, label, inclusive lower/upper bounds, display order, and optional guidance.

Initial rounding modes are `HALF_UP`, `HALF_EVEN`, and `DOWN`. Precision is 0-6. Bounds and scores use `BigDecimal`; binary floating point is forbidden.

Publish requires complete gap-free coverage of the declared numeric range. Bands cannot overlap, invert, or leave ambiguous boundary ownership. Published versions are immutable; corrections use a successor.

### 4.2 Closed calculation vocabulary

Phase 6B stores no script, SQL, SpEL, JavaScript, class name, URL, or user-provided expression. Allowed configuration is limited to validated enums and decimal operands:

- measure type: `COUNT`, `NUMBER`, `PERCENTAGE`, `CURRENCY`, `DURATION`, `DATE_MILESTONE`, `BOOLEAN`, `MANUAL_RUBRIC`;
- direction: `HIGHER_IS_BETTER`, `LOWER_IS_BETTER`, `EXACT_TARGET`, `WITHIN_RANGE`, `MANUAL_RUBRIC`;
- aggregation: `WEIGHTED_AVERAGE` only for initial overall/section/item aggregation;
- missing-result policy: `ERROR` only; missing evidence/dimension is never zero;
- dimension score source: `THRESHOLD_BAND` or `MANUAL_RUBRIC`;
- rounding: at the published scale precision and mode, applied only at documented calculation boundaries.

The definition service must expose a pure preview/validation operation that accepts non-persisted sample values and returns every intermediate decimal, matched band/rubric, weight, and rounding step. Preview stores no employee result and grants no later rating authority.

## 5. Phase 6B.2 - Index of Success Indicators

Create stable indicator identities with immutable versions:

- code, title, output/KRA/KPI statement, success-indicator statement, description;
- exact policy-version and rating-scale-version IDs;
- measure type, unit, direction, target value or target range where applicable;
- verification/evidence guidance and `requiresEvidence`;
- `DRAFT`, `PUBLISHED`, `RETIRED`, effectivity, supersession, audit, optimistic version;
- ordered applicable dimensions, initially controlled codes `QUALITY`, `EFFICIENCY`, `TIMELINESS`, plus `AGENCY_DEFINED` with a required code/label;
- each dimension has a positive weight and one rubric row for every rating-scale band/level.

Rubric rows contain only controlled comparison metadata (`LT`, `LTE`, `EQ`, `GTE`, `GT`, `BETWEEN`) with decimal/date/duration operands, or human-readable manual criteria. A threshold dimension must be mechanically exhaustive and non-overlapping. A manual dimension requires explicit criteria and later human attribution; it cannot masquerade as an automatic rule.

Dimension weights for each indicator version total exactly 100.0000. At least one dimension is required. Q/E/T are optional individually; the system must not force three dimensions when the approved measure does not use all three.

Publishing validates the exact referenced policy/scale version and effectivity, complete rubrics, target compatibility, unit consistency, unique dimension codes/orders, and evidence guidance when evidence is required.

## 6. Phase 6B.3 - template composition and publication

Create stable template identities with immutable versions:

- code, title, description, form type `OPCR`, `DPCR`, `IPCR`, or `CUSTOM`;
- configured form label and legal/agency reference;
- exact policy-version and rating-scale-version IDs;
- applicability initially `AGENCY_WIDE`; organization/position assignment is deferred to Phase 6C;
- fixed computation contract `WEIGHTED_AVERAGE`, missing-result policy `ERROR`, scale precision/rounding inherited from the exact rating-scale version;
- `DRAFT`, `PUBLISHED`, `RETIRED`, effectivity, supersession, actor/time/reason, optimistic version.

Each version owns ordered sections:

- stable within-template code, label, description, category `CORE`, `STRATEGIC`, `SUPPORT`, or `OTHER`;
- section weight percent and display order;
- ordered items referencing one exact published ISI version;
- item label override, item weight percent, required flag, evidence override that may strengthen but never weaken the ISI requirement, and display order.

Normalization is explicit:

```text
sum(section weights) = 100.0000
for each section: sum(item weights) = 100.0000
for each indicator: sum(applicable dimension weights) = 100.0000

dimension contribution = dimension score * dimension weight / 100
indicator score = sum(dimension contributions)
section contribution = sum(indicator score * item weight / 100)
overall score = sum(section contribution * section weight / 100)
adjectival result = exact rating-scale band containing rounded overall score
```

The formula is a future rating contract only. Phase 6B validates definitions and sample previews; it creates no employee/office score.

Publish/readiness requires nonempty sections/items, unique positive orders/codes, exact 100 totals, published/effective policy/scale/indicator versions for the complete template interval, compatible scale references, no duplicate indicator within a section, and deterministic preview fixtures for minimum, maximum, every boundary, and missing input. Published structures are immutable.

## 7. API contract

All routes are agency-scoped under `/api/primehr/v1/performance-management`, use DTOs, trusted server agency resolution, optimistic versions, existing error shapes, and correlation/audit support.

### Rating scales

- `GET/POST /rating-scales`
- `GET/PUT /rating-scales/{versionId}`
- `PUT /rating-scales/{versionId}/bands` (atomic draft replacement)
- `POST /rating-scales/{versionId}/revisions`
- `POST /rating-scales/{versionId}/publish`
- `POST /rating-scales/{versionId}/retire`
- `POST /rating-scales/{versionId}/preview` (definition-only, no persistence)

### Success indicators

- `GET/POST /success-indicators`
- `GET/PUT /success-indicators/{versionId}`
- `PUT /success-indicators/{versionId}/dimensions` (atomic draft replacement including rubrics)
- `POST /success-indicators/{versionId}/revisions`
- `POST /success-indicators/{versionId}/publish`
- `POST /success-indicators/{versionId}/retire`
- `POST /success-indicators/{versionId}/preview` (definition-only, no persistence)

### Templates

- `GET/POST /templates`
- `GET/PUT /templates/{versionId}`
- `PUT /templates/{versionId}/structure` (atomic draft replacement)
- `GET /templates/{versionId}/readiness`
- `POST /templates/{versionId}/revisions`
- `POST /templates/{versionId}/publish`
- `POST /templates/{versionId}/retire`
- `POST /templates/{versionId}/preview` (definition-only, no persistence)

All mutations require `recordVersion`. Stale writes return 409; denial returns 403; invalid definitions/lifecycles return stable validation/conflict codes. There is no delete, execute-formula, assign-template, generate-plan, submit, recommend, approve, rate, calibrate, or report endpoint in 6B.

## 8. Permission contract

| Feature key | Actions | Scope |
|---|---|---|
| `primehr.performance-rating-scale` | Access, Add, Edit, Publish | Agency-wide |
| `primehr.performance-success-indicator` | Access, Add, Edit, Publish | Agency-wide |
| `primehr.performance-template` | Access, Add, Edit, Publish | Agency-wide |

`Publish` controls publish/retire; Add controls initial drafts and successor revisions; Edit controls draft content/atomic child replacement. Preview requires Access and never bypasses mutation permissions. Administrator role `"1"` retains the established contract. Backend enforcement is mandatory. PMT membership alone grants no definition or rating authority.

Administrative permission rows and frontend consumption are reserved for 6B.4.

## 9. Persistence and migrations

Paired PostgreSQL/SQL Server migrations continue after V24:

- **V25:** `spms_rating_scale`, `spms_rating_scale_version`, `spms_rating_band`;
- **V26:** `spms_success_indicator`, `spms_success_indicator_version`, `spms_indicator_dimension`, `spms_indicator_dimension_level`;
- **V27:** `spms_template`, `spms_template_version`, `spms_template_section`, `spms_template_item`.

Required database constraints include agency/stable-code/version uniqueness, positive version/order values, valid status/type/operator enums, chronological effectivity, decimal precision/weight bounds, unique child codes/orders, exact foreign keys to policy/scale/indicator versions, and no cross-database FK. Exact 100 totals, gap-free bands, compatible references, and cross-row rubric exhaustiveness remain transactional service validations because portable CHECK constraints cannot reliably enforce them.

Use `DECIMAL/NUMERIC`, provider-neutral JPA/JPQL, explicit pageable queries, and Flyway with `ddl-auto=validate`. No JSON column, database function, trigger, computed column, SQL Server-only, or PostgreSQL-only calculation is permitted. V25-V27 contain no seed data and no legacy copy.

## 10. Audit, history, and privacy

Audit create/update/child replacement/revision/publish/retire/preview failure with agency, actor, action, target stable/version ID, expected/result record version, reason, correlation ID, and before/after canonical fingerprints. Successful previews may be diagnostic logs without user-supplied confidential data; they do not create business records.

Canonical fingerprints sort children by stable order/code and normalize `BigDecimal` text so both providers produce the same digest. Published definitions and fingerprints never change. Retirement preserves every reference.

Phase 6B stores configuration only. It must contain no employee, applicant, accomplishment, rating, evidence file, coaching note, appeal, medical/demographic fact, or confidential performance result.

## 11. Phase 6B.4 UI and acceptance

This separately approved slice delivers:

- add the three exact rows to Administrative Permission;
- add PrimeHR Rating Scales, Success Indicator Index, and Performance Templates workspaces;
- provide structured editors rather than raw executable/formula text;
- show weight totals, boundary coverage, preview breakdowns, effective references, readiness, revision/history, immutable, denied, validation, and stale states;
- keep Careers, HRM, Timekeeping, Payroll, and Employee Portal unchanged;
- add Playwright for exact permissions, lifecycle/version history, scale boundary/gap validation, indicator rubric exhaustiveness, normalized template weights, deterministic previews, stale writes, denied direct routes, and absence of Phase 6C planning behavior;
- run strict lint/type/build/package and the complete PrimeHR Playwright regression.

## 12. Acceptance gates

### 6B.1 gates before 6B.2

1. V25 paired migrations pass parity, fresh-schema, upgrade, Hibernate validation, and available live-provider gates.
2. Scale bands are ordered, complete, gap-free, non-overlapping, boundary-safe, decimal, and immutable after publication.
3. Only the closed formula vocabulary is accepted; injection/executable-expression payloads are rejected and never evaluated.
4. Preview is deterministic across rounding modes/boundaries, persists no rating, and exposes intermediate calculations.
5. Exact permissions, stale writes, lifecycle/audit, OpenAPI, focused tests, full PrimeHR tests, and affected reactor package pass.

### 6B.2 gates before 6B.3

1. V26 provider/migration gates pass.
2. Indicator targets, units, direction, dimensions, rubrics, weights, and evidence rules validate as one atomic definition.
3. Every referenced policy/scale version is exact, published, and effective; later edits cannot rewrite published indicators.
4. Threshold rules are exhaustive/non-overlapping; manual rubrics are explicit and not auto-calculated.
5. Permissions, conflict/audit/fingerprint, OpenAPI, focused/full/package gates pass; no template or plan transaction exists.

### 6B.3 gates before 6B.4

1. V27 provider/migration gates pass.
2. Template sections/items and three-level weight totals validate exactly with `BigDecimal`.
3. Template publication rejects incomplete, stale, incompatible, non-effective, or mutable source definitions.
4. Boundary previews reproduce documented inputs/intermediates/results identically on SQL Server and PostgreSQL-compatible tests.
5. Published revisions retain exact source IDs/fingerprints and cannot be mutated.
6. Permissions, lifecycle/audit, OpenAPI, focused/full/package gates pass; no 6B.4 UI or Phase 6C behavior exists.

### 6B.4 final gates

1. Administrative controls and PrimeHR navigation/actions exactly match backend permissions.
2. Structured editors make weights, bands, rubric completeness, and preview arithmetic inspectable without arbitrary code.
3. Affected frontend lint/type/build/package and focused/full Playwright pass with zero skips.
4. Provider evidence, operator/data dictionary, deployment/rollback, privacy, and final review manifest are complete.
5. No Phase 6C behavior is present.

## 13. Explicit exclusions and stop boundary

- agency objectives, office/employee template assignment, cascading, target assignment, OPCR/DPCR/IPCR plan creation, commitment copying, submission, recommendation, or approval (Phase 6C);
- accomplishments, progress/evidence uploads, monitoring, coaching, feedback, or mid-cycle review (Phase 6D);
- self/supervisor rating, calibration, final rating, acknowledgment, appeal, PIP, or alerts (Phase 6E);
- L&D/R&R/RSP feeds and all SPMS Jasper/PDF/analytics reports (Phase 6F);
- legacy transaction migration, external workflow engines, notifications, arbitrary code/formulas, direct cross-domain database access, hardcoded agency weights/signatories, or Employee Portal changes.

No Phase 6B behavior was implemented while preparing the original scope. Following explicit approvals, Phase 6B.1-6B.4 were implemented sequentially and passed their gates. Work is stopped before Phase 6C.

## 14. Implementation acceptance record

- **6B.1 passed:** immutable rating-scale identities/versions/bands, closed calculation vocabulary, deterministic non-persistent preview, action-specific backend permission guard, audit/lifecycle controls, OpenAPI, and paired V25 migrations.
- **6B.2 passed after 6B.1:** immutable success-indicator versions, controlled dimensions and complete per-band rubrics, exact policy/scale references, weighted deterministic preview, permission/audit/lifecycle controls, OpenAPI, and paired V26 migrations.
- **6B.3 passed after 6B.2:** immutable OPCR/DPCR/IPCR/custom template versions, normalized sections/items, exact published indicator references, readiness diagnostics, weighted preview, permission/audit/lifecycle controls, OpenAPI, and paired V27 migrations.
- Focused Phase 6B contract, migration, permission, and behavior verification passed **69/69**.
- The complete compiled PrimeHR regression passed **267/267**, with zero failures, errors, or skips.
- PrimeHR production JAR packaging passed. PostgreSQL and SQL Server migration structures passed parity checks, and the PostgreSQL migration chain through V27 passed the available Flyway/Hibernate compatibility test. No live PostgreSQL or SQL Server container profile exists in this repository, so live-provider execution was not available in this gate.
- **6B.4 passed after separate approval:** Administrative exposes the three exact agency-wide permission rows; PrimeHR exposes permission-filtered Rating Scales, Success Indicators, and Performance Templates routes with structured editors, immutable-version actions, readiness, preview, validation, and stale-write states.
- PrimeHR lint and strict TypeScript passed. Administrative lint passed with one unrelated pre-existing `Sidebar.tsx` hook-dependency warning. Both production builds and standalone packaging passed.
- Focused Phase 6B.4 Playwright passed **4/4** and the complete PrimeHR browser regression passed **49/49** with zero skips.
- The browser matrix verifies exact permission denial/navigation, absence of Phase 6C, rating-band coverage errors, deterministic scale/indicator/template previews, rubric exhaustiveness, normalized weights/readiness, immutable publication/revision history, and HTTP 409 reload guidance.
- No performance commitment, target assignment, plan transaction, accomplishment, employee rating, calibration, appeal, report, or other Phase 6C behavior was implemented.
