# ISOFT PRIME-HRM Phase 4 - Competency Gap and L&D Referral Scope Approval

Prepared: 2026-08-28

Status: Approved by the user on 2026-08-28; Phase 4.1, 4.2, and 4.3 complete on 2026-08-29; stopped before Phase 5

## 1. Objective and phase boundary

Phase 4 will compare an employee's latest valid immutable Person Competency Profile with the effective approved Position Competency Profile for the employee's current authoritative appointment. It will create a transparent, historical Competency Gap Analysis, assign explainable development priorities through approved configuration, permit an authorized user to create and submit a manual L&D referral from selected gap items, and generate an operational gap report.

This phase is decision support. A gap result or referral will not create, approve, or modify an Individual Development Plan, training request, course enrollment, annual L&D plan, budget, or HRM training-history record.

## 2. Repository facts and completed foundations

- `PrimeHR` is a standalone-first Spring Boot module in the `hris` Maven reactor. It owns `prime_*` tables and Flyway migrations selected from provider-specific PostgreSQL and SQL Server folders.
- Current local deployment may use the existing configured physical database, but PrimeHR retains logical table ownership. Phase 4 will not require another database and will not read or write legacy tables directly.
- Administrative remains the source of effective permissions. Phase 3 already supports additive feature actions, administrator compatibility, fail-closed legacy JSON, and data scopes.
- HumanResource remains authoritative for employee identity and current appointment facts. Phase 3 exposes a minimal authenticated PrimeHR integration contract and source fingerprint.
- Phase 2 provides immutable ACTIVE Position Profile versions, exact competency/level requirements, Job Position/Plantilla precedence, effectivity resolution, and target snapshots.
- Phase 3 provides immutable validated Person Competency Profile versions and latest/history/exact-version reads.
- `prime-hr-software` already has strict typed API/runtime/auth helpers, a shared shell, permission-aware routes, and repeatable Playwright infrastructure.
- PrimeHR currently has no Jasper dependency or report loader. A Phase 4 PDF report therefore requires a narrow PrimeHR-owned Jasper dependency/resource addition and must use typed DTOs plus `JRBeanCollectionDataSource`, not database-specific JRXML SQL.

## 3. Authoritative comparison selection

### 3.1 Current-analysis request

An authorized generation request identifies the employee and contains an expected current HRM source fingerprint. The server, not the browser, resolves:

1. the employee's current eligible appointment from HumanResource;
2. its Job Position and optional Plantilla identifiers;
3. the effective ACTIVE Position Profile using Plantilla-over-Job-Position precedence;
4. the latest valid immutable Person Competency Profile as of the server-approved analysis date; and
5. the active Development Priority Scheme effective on that date.

The initial Phase 4 command supports current-state generation only. Arbitrary historical or future generation is excluded because the verified HRM contract exposes current appointment facts, not a historical as-of appointment contract. Stored analyses remain historically readable.

If the HRM fingerprint changes between page load and generation, the command fails with `409 Conflict`. Missing employee, current appointment, effective Position Profile, Person Profile, or active priority scheme produces a specific validation/readiness response; no partial analysis is stored.

### 3.2 Exact historical inputs

Every completed analysis stores exact references and immutable material snapshots for:

- agency, employee ID/number/display name;
- appointment, Job Position, optional Plantilla, and HRM source fingerprint;
- resolved Position Profile ID, definition version, record revision, target snapshot, and effectivity;
- Person Profile ID/version, validity period, assessment case/tool/cycle references, and validation timestamp;
- Development Priority Scheme ID/version/effectivity;
- generator and generation timestamp.

Later employee, appointment, position, competency, scale, profile, or priority-policy changes must not rewrite a completed analysis.

## 4. Transparent gap-calculation rules

The unit of comparison is each active requirement in the exact resolved Position Profile.

For an exactly comparable competency and scale:

```text
gap = requiredLevelOrder - attainedLevelOrder
```

- positive gap: `BELOW`;
- zero gap: `MEETS`;
- negative gap: `EXCEEDS`.

The API and UI show the required and attained codes, labels, numeric order, formula inputs, result, and explanation. The implementation must not assume four proficiency levels or particular labels.

An item is `NOT_ASSESSED` and has no numeric gap when:

- the Person Profile has no result for the required exact competency version;
- the employee has no usable attained level;
- the competency definition or proficiency scale version is not safely comparable; or
- required source data is internally inconsistent.

The item stores a reason code such as `NO_RESULT` or `VERSION_NOT_COMPARABLE`. Missing or incompatible results are never converted to level zero. Phase 4 will not silently equate different competency or scale versions by code/name.

Person competencies that are not requirements of the resolved Position Profile are not gap items. They remain available in Person Profile history and are not discarded.

## 5. Configurable development priorities

Agency priority policy must not be hard-coded as universal `LOW/MEDIUM/HIGH`. Phase 4 introduces a small normalized, versioned Development Priority Scheme owned by PrimeHR:

- scheme code, name, description, definition version, predecessor, status, effectivity, and audit metadata;
- ordered priority levels with agency-defined code, label, rank, description, and active flag;
- ordered rules that may match gap classification, minimum/maximum positive gap, requirement classification, and optional criticality code;
- one explicit fallback priority for actionable `BELOW` and `NOT_ASSESSED` outcomes.

Rules are evaluated in persisted display order. Publication rejects ambiguous overlapping rules at the same precedence, invalid ranges, missing fallback coverage, duplicate codes/ranks, or an effectivity overlap. The analysis item stores the matched rule, priority code/label/rank, and explanation snapshot.

`MEETS` and `EXCEEDS` are non-development-gap results and receive no referral priority unless a later approved agency policy explicitly expands this model. No uncontrolled JSON rule engine or generic workflow engine is introduced.

Priority schemes use the proven Phase 1 lifecycle:

```text
DRAFT -> ACTIVE
DRAFT -> ARCHIVED
ACTIVE -> successor DRAFT -> ACTIVE
```

ACTIVE definitions are immutable. Publishing a successor closes an overlapping predecessor atomically. Ordinary publication uses independent `canPublish`; administrator override remains audited.

## 6. Gap-analysis persistence

### V9 provider-equivalent tables

- `prime_gap_priority_scheme`
- `prime_gap_priority_level`
- `prime_gap_priority_rule`
- `prime_competency_gap_analysis`
- `prime_competency_gap_item`

The analysis root is immutable after successful generation. Items retain exact competency/level references and report-ready snapshots. A deterministic request key covering agency, employee, exact source profile versions, priority scheme version, and analysis date prevents accidental duplicate generation. Repeating the same completed request returns the existing result or a documented conflict; it never creates duplicate rows.

Equivalent PostgreSQL and SQL Server migrations must define matching keys, foreign keys, lengths, status checks, uniqueness, booleans, date/timestamp precision, optimistic versions where mutable drafts require them, and indexes for employee, analysis date, source profile, classification, priority, and current-history queries.

Shared Java code uses JPA/JPQL/derived queries/Specifications/Pageable. No provider-specific SQL or database-dialect branching is permitted in controllers, services, repositories, or JRXML.

## 7. Manual L&D referral boundary

Phase 4 selects the Master Plan's allowed **manual referral** option. Rule-assisted automatic referral creation is deferred. The priority scheme assists a human decision but does not create a referral by itself.

An authorized user may create one referral draft from one completed Gap Analysis and select one or more actionable `BELOW` or `NOT_ASSESSED` items. The draft supports:

- employee and analysis snapshot references;
- selected gap-item references;
- development need statement;
- recommended intervention/action text;
- target completion date where known;
- remarks and referral reason;
- creator/updater and optimistic record version.

Lifecycle:

```text
DRAFT -> REFERRED
DRAFT -> ARCHIVED
```

- Drafts may be edited or archived by an authorized user.
- Submission requires at least one active actionable gap item and a development need/recommendation.
- Submission is atomic, immutable, and audited.
- `REFERRED` means sent to the future L&D intake queue only. It does not mean accepted, approved, funded, scheduled, enrolled, or completed.
- No Phase 7 entity is created and no HRM Learning and Development row is written.
- A later Phase 7 intake may accept/reject/link a referral through a separately approved contract and lifecycle.

### V10 provider-equivalent tables

- `prime_ld_referral`
- `prime_ld_referral_item`

Uniqueness prevents the same gap item from being actively referred more than once for the same analysis unless a later policy explicitly introduces referral closure/reopening.

## 8. REST contract

All endpoints remain under `/api/primehr/v1`, use request/response DTOs, validation, pagination, stable error responses, token-derived actor/agency context, and audit correlation where available.

### Priority administration

```http
GET    /admin/gap-priority-schemes
POST   /admin/gap-priority-schemes
GET    /admin/gap-priority-schemes/{schemeId}
PUT    /admin/gap-priority-schemes/{schemeId}
POST   /admin/gap-priority-schemes/{schemeId}/archive
POST   /admin/gap-priority-schemes/{schemeId}/publish
POST   /admin/gap-priority-schemes/{schemeId}/versions
POST   /admin/gap-priority-schemes/{schemeId}/levels
PUT    /admin/gap-priority-schemes/{schemeId}/levels/{levelId}
POST   /admin/gap-priority-schemes/{schemeId}/levels/{levelId}/archive
POST   /admin/gap-priority-schemes/{schemeId}/rules
PUT    /admin/gap-priority-schemes/{schemeId}/rules/{ruleId}
POST   /admin/gap-priority-schemes/{schemeId}/rules/{ruleId}/archive
```

### Gap analysis

```http
GET    /competency-gaps?employeeNo=&classification=&priority=&page=&size=
POST   /competency-gaps
GET    /competency-gaps/{analysisId}
GET    /competency-gaps/employees/{employeeNo}/latest
GET    /competency-gaps/employees/{employeeNo}/history
GET    /competency-gaps/{analysisId}/report.pdf
```

The create request carries only employee identity, expected HRM fingerprint, and an idempotency/request key. It cannot supply attained levels, required levels, gap values, priority results, or source profile IDs as authoritative facts.

### L&D referrals

```http
GET    /ld-referrals?employeeNo=&status=&page=&size=
POST   /ld-referrals
GET    /ld-referrals/{referralId}
PUT    /ld-referrals/{referralId}
POST   /ld-referrals/{referralId}/items
POST   /ld-referrals/{referralId}/items/{itemId}/archive
POST   /ld-referrals/{referralId}/submit
POST   /ld-referrals/{referralId}/archive
```

OpenAPI is updated additively and contract tests must verify every path, method, request, response, enum, error, and PDF media type.

## 9. Authorization and Administrative permissions

Add three Administrative feature keys without changing existing feature semantics:

- `primehr.gap-configuration`
- `primehr.competency-gap`
- `primehr.ld-referral`

Use existing action fields rather than introducing an unneeded new permission column:

| Feature | Actions used | Data scope |
|---|---|---|
| Gap configuration | Access, Add, Edit, Delete/archive, Publish | `AGENCY_WIDE` |
| Competency gap | Access, Add/generate | `OWN_RECORDS` read or `AGENCY_WIDE`; generation requires `AGENCY_WIDE` |
| L&D referral | Access, Add, Edit, Delete/archive, Submit | `AGENCY_WIDE` |

PDF generation is an alternate representation of an analysis the actor is already authorized to view; it does not broaden data access and therefore uses the same Competency Gap Access/data-scope check. Legacy permission JSON fails closed for every new feature. Administrator retains all supported actions and agency-wide behavior with auditing.

Phase 4 does not implement `DIRECT_SUBORDINATES` because the repository still has no verified authoritative supervisor contract. No UI-only authorization is accepted.

## 10. UI and report scope

### Administrative UI

Update the existing permission matrix only to show the three Phase 4 feature rows and their applicable action/data-scope controls. Preserve existing permission JSON and unrelated modules.

### PrimeHR UI

Add one navigation area and route:

```text
/prime-hr/competency-gaps
```

The route provides permission-aware panels for:

- employee lookup using the existing minimal HRM contract;
- readiness/current-source display before generation;
- analysis generation with double-submit protection;
- latest/history search and filters;
- exact source-version and calculation explanation;
- per-item required/attained/gap/classification/priority details;
- priority-scheme draft/version/publish administration when permitted;
- referral draft, item selection, edit/archive/submit controls when permitted;
- printable PDF action;
- loading, empty, denied, validation, stale conflict, dependency, and server-error states.

No Employee Portal page is added in Phase 4. An employee with own-record API permission may be supported by the backend, but employee-facing gap/development-plan presentation requires separate privacy and product approval.

### Jasper gap report

PrimeHR adds only the dependencies needed by the established repository Jasper stack and packages one report resource, provisionally:

```text
PrimeHR/src/main/resources/reports/competency_gap_report.jrxml
```

The service supplies typed report DTOs through `JRBeanCollectionDataSource`. JRXML owns layout only and contains no SQL. The PDF displays analysis identity/date, employee/position snapshots, exact source versions, calculation legend, all gap items, priorities/reasons, not-assessed reasons, generator, and a clear statement that recommendations are not approved IDPs. It must handle null attained levels, long labels, multiple pages, repeated headers, and no actionable gaps.

## 11. Controlled execution checkpoints

### Phase 4.1 - Priority configuration and transparent gap engine

- Administrative/backend permission contract for gap configuration and analysis;
- V9 PostgreSQL and SQL Server migrations;
- priority scheme draft/version/publish model;
- exact current-source resolution and immutable gap generation;
- priority matching, idempotency, audit, REST/OpenAPI, and focused tests.

Gate: stop if exact source resolution, missing-assessment handling, transparent formula, historical immutability, authorization, migration parity, or SQL Server verification fails. Do not begin referrals or UI before every 4.1 gate passes.

### Phase 4.2 - Manual L&D referral

- V10 PostgreSQL and SQL Server migrations;
- draft/item CRUD, archive, submit, optimistic conflict, uniqueness, and audit;
- referral REST/OpenAPI and focused tests;
- explicit future-L&D boundary with no IDP/training side effect.

Gate: stop if a non-actionable/unauthorized/duplicate/stale referral can be submitted, if submission creates an IDP/training record, or if provider/atomicity gates fail. Do not begin UI/report work before every 4.2 gate passes.

### Phase 4.3 - Administrative controls, PrimeHR UI, Jasper report, and Playwright acceptance

- Administrative permission rows;
- PrimeHR Competency Gap route and navigation;
- permission-aware priority, analysis, referral, and history UI;
- portable bean-driven Jasper PDF;
- repeatable Playwright Phase 4 matrix and user/testing documentation.

Gate: both frontend lint/type-check/build gates, full affected Maven test/package gates, JRXML compile/PDF inspection, focused and full Playwright suites, repository/secret/generated-file audits, and Phase 4 review manifest must pass. Stop before Phase 5.

## 12. Test and acceptance matrix

At minimum, automated tests must prove:

- dynamic scales of more or fewer than four levels calculate by persisted order;
- positive/zero/negative gaps map to BELOW/MEETS/EXCEEDS;
- missing and version-incompatible assessment results are NOT_ASSESSED, never zero;
- exact Plantilla-over-Job-Position resolution and HRM fingerprint conflict behavior;
- no effective Position Profile, Person Profile, or priority scheme fails without partial rows;
- priority rule order, fallback, overlap validation, effectivity, versioning, and immutable snapshots;
- same idempotency/source tuple cannot create duplicate analyses;
- later source/profile/policy changes do not rewrite stored results;
- own-record reads cannot expose another employee and generation/referral requires agency-wide scope;
- draft referral validation, selected actionable items, stale version conflict, archive, submit, immutability, uniqueness, and audit;
- referral submission creates no IDP, training request, enrollment, HRM L&D row, event, notification, or payroll transaction;
- equivalent V9/V10 PostgreSQL and SQL Server schema constraints/indexes and populated upgrade paths;
- OpenAPI contract completeness;
- Jasper compilation and representative PDF data/pagination/null behavior;
- Playwright allowed, denied, generation, formula, not-assessed, history, referral, conflict, and PDF cases;
- no Phase 5+ route, table, API, event, or behavior appears.

Blocking provider acceptance remains real local SQL Server under the user's current policy. PostgreSQL migrations and structural/provider-neutral tests remain mandatory; a live PostgreSQL run is reported honestly if unavailable and is not falsely claimed.

## 13. Expected repository impact

### `hris`

- `Administrative`: additive feature constants/effective-permission tests only; no new action field is expected.
- `HumanResource`: reuse the existing current assessment-subject contract; modify only if a proven minimal fingerprint/readiness field is missing.
- `PrimeHR`: new `gap` and `learning.referral` internal packages using API/application/domain/infrastructure layering; permission guards; DTOs/controllers/services/repositories; V9/V10 dual migrations; OpenAPI; tests; Jasper resource/loader/dependencies.
- `contracts/openapi/primehr-v1.yaml` and phase/progress/user documentation.
- No `HRISApp`, TimeKeeping, Payroll, legacy HRM L&D controller/entity, or existing Jasper report change is expected.

### `administrative-software`

- `src/app/administrative/permission/Permission.tsx` only, unless repository inspection during implementation proves a shared typed permission definition also requires an additive change.

### `prime-hr-software`

- typed gap/referral API models/client;
- shared shell/navigation update;
- `/prime-hr/competency-gaps` manager/page and existing SCSS design patterns;
- `e2e/phase4.spec.ts`, support/config/runbook additions;
- user-guide references as appropriate.

### Explicitly untouched

- `employee-portal-UI`, `hr-management-UI`, `time-keeping-software`, and `payroll-ui`;
- existing Administrative/HRM/Timekeeping/Payroll behavior and reports;
- existing Phase 1-3 immutable records;
- deployment, commit, and push unless separately requested.

## 14. Explicit exclusions

- approved IDPs, office/agency development plans, annual L&D plans, budgets, program/course/provider catalogs;
- nomination, approval, enrollment, attendance, training orders, certificates, post-training evaluation, LAP/REAP, and gap-closure tracking;
- automatic referral creation, automatic IDP approval, machine-learning recommendation, or generic rule/workflow designer;
- supervisor/direct-subordinate inference;
- Employee Portal pages or notifications;
- binary evidence/document upload or object storage;
- RabbitMQ, outbox, `CompetencyGapIdentified` event publishing, cache, analytics read model, or Evidence Center;
- RSP, SPMS, full L&D, R&R, applicants, succession, reports other than the Phase 4 gap PDF, or Phase 5+ behavior;
- direct cross-database joins/writes and combined `HRISApp` runtime integration.

## 15. Decisions accepted by approving this scope

Approval accepts these decisions:

1. current-state generation uses authoritative HRM current appointment data and exact effective Phase 2/3 versions;
2. mismatched competency/scale versions are NOT_ASSESSED with an explicit reason, not guessed or treated as zero;
3. priorities come from a normalized versioned agency scheme, not universal hard-coded labels;
4. completed analyses are immutable historical snapshots and duplicate-safe;
5. Phase 4 implements human-created manual referrals only;
6. `REFERRED` is an intake handoff status, never an approved IDP or intervention;
7. report authorization reuses exact gap-view permission/data scope;
8. the gap PDF is bean-driven and database-neutral;
9. SQL Server is the blocking live provider while dual-provider portability artifacts remain required;
10. Phase 4 is delivered through gated 4.1, 4.2, and 4.3 checkpoints and stops before Phase 5.

## 16. Approval and execution condition

This document authorizes no implementation by itself. Phase 4.1 may begin only after the user explicitly approves this exact scope. Phase 4.2 may begin only after every Phase 4.1 gate passes and the user has authorized continuation under this scope. Phase 4.3 may begin only after every Phase 4.2 gate passes. Any material change to calculation semantics, policy configuration, data scope, referral lifecycle, report architecture, or exclusions requires renewed approval.

Recommended approval command:

```text
Approve Phase 4 as defined. Proceed with Phase 4.1, and continue to Phase 4.2 only after all Phase 4.1 gates pass.
```
