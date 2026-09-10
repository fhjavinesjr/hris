# Phase 6C Performance Planning and Approval Scope Approval

Date: 2026-09-09  
Status: Scope defined only; implementation not approved  
Authority: `ISOFT_PRIME_HRM_CODEX_MASTER_PLAN_V2.md`, Phase 6C and sections 5.6, 5.10-5.13, 10.1-10.3, 16-18

## 1. Outcome and controlled delivery sequence

Phase 6C creates the planning transaction between the approved Phase 6A cycle/policy foundation and the approved Phase 6B objective-independent definitions. It covers strategic objectives, organization/employee plan assignments, deterministic creation of office and individual commitment drafts, explicit cascade links, and review/approval of commitments.

The proposed sequence is:

- **6C.1 - Objectives, authoritative participants, and plan assignments:** versioned strategic objectives; read-only Administrative organization/approval-route projections; read-only HRM performance-participant projections; exact cycle/template/organization/employee assignments; backend permissions, audit, OpenAPI, V28 paired migrations, and provider gates.
- **6C.2 - Commitment composition and cascading:** idempotent creation from an active assignment; immutable template/indicator/owner/organization snapshots; structured target allocation; office-to-office and office-to-individual cascade links; readiness; version history; backend permissions, audit, OpenAPI, V29 paired migrations, and provider gates, only after all 6C.1 gates pass.
- **6C.3 - Submission, recommendation, and approval:** immutable approval-route snapshots; sequential independent decisions; return/resubmit/reject/withdraw/approve/amend/void controls; cycle-window enforcement; audit/fingerprints; OpenAPI; V30 paired migrations; and provider gates, only after all 6C.2 gates pass.
- **6C.4 - Administrative controls, PrimeHR/Employee Portal UI, and Playwright:** exact permission rows, planning/approval workspaces, employee-owned commitment UI, and complete browser regression, only after all 6C.3 gates pass and after separate approval.

Approval of this document is not approval to implement 6C.4. Every Phase 6C slice remains stopped before Phase 6D monitoring/coaching.

## 2. Contracts inspected

### Current Phase 6A/6B authority

- Phase 6A owns immutable/effective policies, controlled cycle dates/timezone/milestones, and effective PMT rosters.
- A commitment may exist only in one exact `OPEN` cycle and must retain that cycle's policy-version ID and calendar revision.
- `PLANNING_OPEN`, `PLANNING_DUE`, and `APPROVAL_DUE` milestones replace the legacy numeric/date lock table. Required milestones are server-authoritative; browser time never opens or closes a window.
- Phase 6B owns exact published/effective rating-scale, success-indicator, and OPCR/DPCR/IPCR/custom template versions. Planning copies their display/calculation metadata into a transaction snapshot but never edits those definitions.
- Template section, item, and indicator-dimension weights remain decimal and normalized. Phase 6C allocates targets; it does not calculate accomplishments or ratings.

### Current Administrative and HRM integration

- Administrative owns Area, Business Unit, Plantilla, Job Position, employee-request types, ordered approval-workflow rows, and feature permissions.
- The current organization-scope client resolves Area to Business Unit IDs but does not provide names, hierarchy snapshots, Plantilla membership, or a fingerprint suitable for an immutable commitment.
- The current Approval Workflow rows are mutable and keyed by Business Unit plus Employee Request. They may be used only through a new authenticated read projection that resolves a stable request code, validates levels, and returns a canonical fingerprint. PrimeHR must never read Administrative tables directly.
- HRM's assessment-subject projection already provides active employee, appointment, Job Position, Plantilla, source-update, and fingerprint fields, but its authorization is assessment-specific. Phase 6C requires a performance-participant projection with the same authoritative data and feature-specific authorization, including lookup by authenticated employee number.
- PrimeHR must combine the HRM appointment/Plantilla snapshot with Administrative `manage_personnel` employee-to-Area/Business-Unit membership. The current Plantilla master has no Business Unit field and must not be treated as if it did. If either source is unavailable, inconsistent, duplicated, or changes before submission, the operation fails closed and saves no transition.

### Legacy ZCMC planning and approval

- Legacy `Ipcr`, `Dpcr`, and `Opcr` retain employee, filing/period dates, recommender, approver, recommendation/final approval flags, comments, function rows, and final-submission state.
- Core/Strategic/Support rows mix planning statements and verification with later accomplishments, Q/E/T grades, rating averages, and fixed five-level rubric text.
- `TargetSpms` targets an employee/station/department during a date range but does not identify an exact template, cycle, objective, appointment, or source revision.
- `SpmsLocking` uses numeric lock types and scans dates client-side. It is ambiguous and is replaced by Phase 6A cycle milestones and explicit server-side override audit.
- Legacy routing derives supervisor/department-head employees from mutable Area Management state, sometimes self-recommends, accepts password text in the workflow action, and can delete filings. Those behaviors are not authoritative.
- Legacy fixed IPCR 80/0/20 and other agency-specific weights remain rejected as defaults. Phase 6B template versions are authoritative.
- Legacy final averages, actual accomplishments, Q/E/T grades, summary ratings, and adjectival ratings belong to 6D/6E and are not migrated or exposed in 6C.

## 3. Phase 6C.1 - objectives, participants, and assignments

### 3.1 Strategic objective versions

Create stable objective identities with immutable versions:

- agency, stable code, title, objective statement, expected outcome, legal/strategy reference;
- level `AGENCY`, `AREA`, or `BUSINESS_UNIT`;
- exact authoritative organization target ID and frozen code/name/parent snapshot where applicable;
- optional exact parent objective-version ID, with the parent at a higher organizational level;
- exact Phase 6A policy-version ID;
- optional exact Phase 6B success-indicator-version ID and compatible target value/range/unit;
- `DRAFT`, `PUBLISHED`, `RETIRED`, definition version, effectivity, supersession, actor/time/reason, source fingerprint, and optimistic `recordVersion`.

An objective graph must be acyclic. A child cannot outlive its parent, cross agencies, reference a mutable/unpublished definition, or point to the same/lower organizational level. Published objective versions are immutable; corrections use a successor.

No arbitrary formula, strategic-score calculation, budget, accomplishment, evidence, or rating is stored.

### 3.2 Authoritative read projections

Add authenticated, read-only integration contracts:

- Administrative `/api/integration/v1/primehr/performance/organization-targets` and `/{type}/{id}` for current Area/Business Unit identity, hierarchy, and canonical fingerprint;
- Administrative `/api/integration/v1/primehr/performance/personnel-membership/{employeeId}` for the exact employee, Business Unit, Area, head/co-approver flags, and fingerprint from `manage_personnel`;
- Administrative `/api/integration/v1/primehr/performance/approval-routes?businessUnitId=&requestCode=` for a validated ordered route and canonical fingerprint;
- HRM `/api/integration/v1/primehr/performance-participants`, `/{employeeId}`, and `/by-employee-no/{employeeNo}` for current active employee/appointment/Plantilla/Job Position identity and fingerprint.

Stable request codes are `PERFORMANCE_OFFICE_COMMITMENT` and `PERFORMANCE_INDIVIDUAL_COMMITMENT`. They are product workflow identifiers, not agency labels or hard-coded approver IDs. The agency configures their approval levels through the existing Administrative masters. Missing, duplicate, non-positive, or non-contiguous levels make assignment/submission readiness fail; there is no implicit default approver.

Integration DTOs contain no salary, PDS, applicant, medical, leave, payroll, or credential data. URLs and timeouts remain environment/system configured.

### 3.3 Plan assignments

An assignment binds:

- one exact open/upcoming cycle and one exact published/effective template version;
- subject type `AREA`, `BUSINESS_UNIT`, or `EMPLOYEE`;
- the authoritative subject ID and frozen organization or employee/appointment snapshot;
- for organization commitments, one accountable active employee whose Administrative `manage_personnel` membership belongs to that Area/Business Unit; HRM separately supplies the employee's active appointment, Job Position, and Plantilla snapshot;
- the authoritative routing Business Unit; it is derived for Business Unit/employee subjects, while an Area assignment must explicitly select one Business Unit within that Area because a route cannot be guessed across multiple units;
- optional exact published objective-version IDs applicable to the subject;
- form type inherited from the template; display label remains template data;
- `DRAFT`, `ACTIVE`, or `RETIRED`, activation/retirement actor/time/reason, source fingerprints, and optimistic version.

Initial mapping is IPCR to `EMPLOYEE`; OPCR/DPCR to `AREA` or `BUSINESS_UNIT`; `CUSTOM` declares one allowed subject type in the assignment. The system must not infer that every agency uses identical OPCR/DPCR organization levels.

Only one active assignment may exist for the same agency, cycle, subject type/ID, and form type. Activation revalidates cycle, template, policy, objective, employee, appointment, Plantilla, organization membership, and approval-route sources atomically.

## 4. Phase 6C.2 - commitments and cascading

### 4.1 Commitment aggregate and immutable snapshot

Create a stable commitment identity with ordered versions. The root is unique per active assignment. Each version retains:

- exact assignment, cycle, policy, template, rating-scale, objective, and prior commitment-version IDs;
- form type/label, planning period, subject, accountable owner/preparer, organization, employee, appointment, Job Position, and Plantilla snapshots as applicable;
- exact source fingerprints and fetch times;
- `DRAFT`, `SUBMITTED`, `IN_REVIEW`, `RETURNED`, `REJECTED`, `APPROVED`, `WITHDRAWN`, `VOIDED`, or `SUPERSEDED`;
- content revision, lifecycle version, actor/time/reason fields, and optimistic `recordVersion`.

Creation from an assignment is idempotent. Retrying returns the same current draft instead of duplicating a commitment. There is no free-form creation that bypasses an active assignment.

The snapshot copies ordered template sections/items and exact success-indicator metadata needed to understand the commitment later: codes/labels, weights, output/KRA/KPI/success statement, measure/unit/direction, default target, evidence requirement, and definition fingerprints. It does not copy executable behavior.

### 4.2 Structured target allocation

For each commitment item, planning may set only:

- committed target value or compatible from/to range;
- target period within the cycle;
- deliverable/expected-output clarification;
- responsible owner for organization plans, validated as an active employee in the same organization;
- planning remarks and required-evidence clarification that may strengthen but not weaken the source requirement.

Measure type, direction, rating scale, rubric, section/item/dimension weights, and formula are inherited and immutable. Targets must match the measure/direction contract; ranges cannot invert; item periods cannot leave the cycle; every required item must be complete. Monetary target values are planning quantities, never payroll amounts.

### 4.3 Explicit cascade graph

A cascade edge links one exact upstream approved office-commitment item version to one downstream draft office/individual item version in the same cycle. Modes are:

- `REFERENCE_ONLY`, recording alignment without arithmetic allocation;
- `RESPONSIBILITY_SHARE`, requiring a positive decimal percentage not exceeding 100.0000; total active outgoing shares for an upstream item cannot exceed 100.0000.

The graph must be acyclic. Source/child policy, indicator, measure, unit, and period must be compatible. The downstream committed target remains explicit and is never silently calculated or overwritten. Changing or superseding an upstream approved plan does not rewrite downstream history; readiness reports a stale/superseded source and requires an authorized rebase or successor.

No cascade creates an accomplishment, evidence, score, employee rating, coaching task, L&D referral, R&R feed, or RSP decision.

### 4.4 Readiness and amendments

Readiness returns structured errors for missing targets, broken 100% snapshots, invalid periods, stale source fingerprints, inactive owners, incompatible objectives/cascades, unavailable dependencies, missing cycle milestones, and invalid approval routing. It performs no transition.

An approved commitment is immutable. A correction creates an `AMENDMENT_DRAFT` successor carrying the prior snapshot and explicit reason. It must pass the complete approval route. Approval of the successor marks the prior approved version `SUPERSEDED`; rejection leaves the prior approved version authoritative.

## 5. Phase 6C.3 - review and approval

### 5.1 Route snapshot

Submission resolves the current Administrative route by the assignment's authoritative routing Business Unit and stable request code, then freezes:

- ordered level, action type (`RECOMMEND` for intermediate levels and `APPROVE` for the final level);
- employee ID/no/name and current appointment/position snapshot for each actor;
- organization and route fingerprints;
- submitted commitment fingerprint and content revision.

A one-level route is final approval only. Submitter/accountable owner cannot approve the final step. Duplicate actors across sequential levels, inactive actors, organization mismatch, route gaps, or self-final-approval fail closed. Later Administrative changes do not rewrite an in-flight route; an authorized rebase before any decision creates a new route revision and audit event.

JWT identity is the approval credential. Passwords, employee IDs, status values, actors, timestamps, and route levels supplied by the browser are never trusted.

### 5.2 Lifecycle

- `DRAFT` or `RETURNED` may be edited and submitted by the owner/preparer with Submit authority and ownership/process scope.
- `SUBMITTED`/`IN_REVIEW` accepts only the current route actor's decision.
- An intermediate positive decision records `RECOMMENDED` action evidence and advances the route; the commitment remains `IN_REVIEW`.
- Any current reviewer may `RETURN` with a nonempty reason. The same version becomes editable `RETURNED`; resubmission increments content and route revisions and takes a fresh route/source snapshot.
- The final actor may `APPROVE` or terminally `REJECT`, each with a reason. Approval freezes the commitment version.
- The owner may `WITHDRAW` only before the first route decision. Withdrawal preserves history and requires a new draft/successor to restart.
- `VOID` is an exceptional Finalize-authority action for an invalid in-flight record, requires a reason, and never deletes history.
- Late submission/action is denied according to the exact Phase 6A planning/approval milestones. A separately authorized Finalize override must name the missed milestone, record the reason, and preserve both normal and override decisions.

Every transition is transactional, idempotent under a request key, protected by `recordVersion` and expected lifecycle/content/route revisions, and auditable. Stale writes return HTTP 409.

## 6. API contract

All routes are under `/api/primehr/v1/performance-management`, resolve the agency server-side, use explicit DTOs, and preserve the existing error/correlation conventions.

### Objectives

- `GET/POST /objectives`
- `GET/PUT /objectives/{versionId}`
- `GET /objectives/{versionId}/readiness`
- `POST /objectives/{versionId}/publish`
- `POST /objectives/{versionId}/revisions`
- `POST /objectives/{versionId}/retire`

### Assignments

- `GET/POST /plan-assignments`
- `GET/PUT /plan-assignments/{assignmentId}`
- `GET /plan-assignments/{assignmentId}/readiness`
- `POST /plan-assignments/{assignmentId}/activate`
- `POST /plan-assignments/{assignmentId}/retire`

### Commitments and cascade

- `GET /commitments` with cycle, form, subject, owner, status, and assigned-task filters
- `POST /commitments/from-assignment/{assignmentId}` with an idempotency key
- `GET /commitments/{versionId}`
- `PUT /commitments/{versionId}/targets` as atomic draft replacement
- `PUT /commitments/{versionId}/cascades` as atomic draft replacement
- `GET /commitments/{versionId}/readiness`
- `POST /commitments/{versionId}/submit`
- `POST /commitments/{versionId}/withdraw`
- `POST /commitments/{versionId}/recommend`
- `POST /commitments/{versionId}/return`
- `POST /commitments/{versionId}/approve`
- `POST /commitments/{versionId}/reject`
- `POST /commitments/{versionId}/amendments`
- `POST /commitments/{versionId}/void`
- `POST /commitments/{versionId}/route-rebase`

There is no delete, accomplishment, evidence-upload, coaching, rating, calibration, acknowledgment, appeal, referral, Jasper/PDF, or analytics endpoint in Phase 6C.

## 7. Permission contract

| Feature key | Actions | Allowed scope |
|---|---|---|
| `primehr.performance-objective` | Access, Add, Edit, Publish | `AGENCY_WIDE` |
| `primehr.performance-plan-assignment` | Access, Add, Edit, Publish | `AGENCY_WIDE` |
| `primehr.office-performance-commitment` | Access, Add, Edit, Submit, Approve, Finalize | `ASSIGNED_RECORDS`, `AGENCY_WIDE` |
| `primehr.individual-performance-commitment` | Access, Add, Edit, Submit, Approve, Finalize | `OWN_RECORDS`, `ASSIGNED_RECORDS`, `AGENCY_WIDE` |

`Publish` controls objective publication/retirement and assignment activation/retirement. `Submit` controls submit/withdraw. `Approve` controls intermediate recommendation/return/reject where the actor is the current route participant. `Finalize` controls final approval, route rebase, late-window override, and void, never ordinary drafting.

Permission alone is insufficient. Backend enforcement also requires ownership or current route participation, subject/organization scope, exact lifecycle state, cycle window, and source freshness. PMT membership, supervisor status, or administrator role does not silently make someone the current approver. Administrator role `"1"` retains feature authority but still requires explicit reason and lifecycle validation for exceptional actions.

Employee Portal uses `primehr.individual-performance-commitment` with `OWN_RECORDS`; it never accepts an employee ID from the browser as proof of ownership.

## 8. Persistence and migrations

Paired PostgreSQL/SQL Server migrations continue after V27:

- **V28:** `spms_objective`, `spms_objective_version`, `spms_plan_assignment`, and assignment-objective join rows;
- **V29:** `spms_commitment`, `spms_commitment_version`, `spms_commitment_section`, `spms_commitment_item`, and `spms_commitment_cascade`;
- **V30:** `spms_commitment_route`, `spms_commitment_route_step`, and `spms_commitment_action`.

Required constraints cover agency/code/version uniqueness, chronological periods/effectivity, lifecycle enums, positive orders/revisions, subject/form compatibility, one root per assignment, exact internal foreign keys, cascade uniqueness, and decimal percentage bounds. Cross-row 100% totals, graph acyclicity, single-current-version, route contiguity, source freshness, and ownership remain transactional service validations.

Use UUID strings, `NUMERIC/DECIMAL` for targets/weights/shares, provider-neutral JPA/JPQL, explicit pagination, Flyway, and `ddl-auto=validate`. No provider-specific SQL, JSON column, trigger, computed column, seed approver, legacy copy, or direct foreign key to Administrative/HRM databases is permitted.

Legacy import is not part of V28-V30. Existing OPCR/DPCR/IPCR rows lack reliable exact template/cycle/source/route versions and mix later accomplishments/ratings with planning. A future reviewed reconciliation/import may preserve them as read-only legacy references after duplicate periods, owners, organization mappings, statuses, and child rows are adjudicated.

## 9. Audit, privacy, and concurrency

Audit objective/assignment create/update/publish/retire, participant/source validation, commitment generation/target/cascade replacement, readiness failure, submit/withdraw/recommend/return/approve/reject/amend/void/rebase/override, and denied attempts where supported. Capture agency, actor, action, target/root/version, expected/result versions, reason, correlation/idempotency key, source and route fingerprints, and canonical before/after fingerprints.

Canonical fingerprints sort sections/items/cascades/routes by positive order and normalize decimal/date text identically on both providers. Published objectives, active assignment snapshots, submitted route snapshots, approved commitments, and action evidence are immutable.

Commitments are confidential personnel/process records. List/detail responses minimize employee and organization data by scope. Employee Portal receives only the signed-in employee's commitments and applicant/Careers identities cannot access them. Logs exclude target narratives where sensitive, JWTs, passwords, PDS, and unrelated employee data.

## 10. Phase 6C.4 reserved UI and Playwright acceptance

This slice requires separate approval after 6C.1-6C.3 pass:

- add the four exact Administrative permission rows with action/data-scope controls;
- add PrimeHR Strategic Objectives, Plan Assignments, Office Commitments, Individual Commitments, and Approval Inbox workspaces;
- add Employee Portal **My Performance Commitments** using authenticated ownership, not a browser-supplied employee ID;
- expose source/readiness diagnostics, cycle windows, immutable references, structured targets/cascades, route progress, return reasons, revision/amendment history, stale conflicts, dependency failures, and disabled-state explanations;
- keep accomplishment, coaching, rating, calibration, appeal, report, L&D, R&R, Payroll, Careers, and RSP UI absent;
- add Playwright for exact permissions/scopes, ownership, organization membership, idempotent generation, source changes, cycle windows, target validation, cascade DAG/share limits, route snapshots, segregation of duties, return/resubmit, withdrawal, rejection, approval immutability, amendments, stale writes, dependency failures, and absence of Phase 6D;
- run Administrative, PrimeHR, and Employee Portal lint/type/build/package plus the complete PrimeHR browser regression.

## 11. Acceptance gates

### 6C.1 gates before 6C.2

1. V28 paired migrations pass parity, fresh-schema, upgrade, Hibernate validation, and available provider gates.
2. Objective hierarchy/effectivity/source rules reject cycles, organization mismatch, mutable references, and graph cycles.
3. Administrative/HRM read projections are feature-authorized, minimal, fingerprinted, fail closed, and never use direct cross-domain database access.
4. Assignment activation revalidates exact cycle/template/objective/participant/appointment/Plantilla/personnel-membership/organization/route sources and enforces unique subject/form coverage.
5. Permissions, audit, OpenAPI, stale writes, focused/full PrimeHR tests, and affected reactor package pass; no commitment transaction exists.

### 6C.2 gates before 6C.3

1. V29 provider/migration gates pass.
2. Retried generation is idempotent and exact template/indicator/owner/organization snapshots remain understandable after source changes.
3. Required targets, types, ranges, dates, weights, and evidence clarifications validate atomically; no browser or binary-floating calculation is authoritative.
4. Cascade links are same-cycle, compatible, acyclic, share-bounded, exact-versioned, and cannot rewrite approved upstream/downstream history.
5. Own/assigned/agency scope, confidentiality, audit/fingerprint, OpenAPI, stale writes, focused/full/package gates pass; no approval transition or Phase 6D behavior exists.

### 6C.3 gates before 6C.4

1. V30 provider/migration gates pass.
2. Submission freezes complete source/content/route fingerprints and rejects missing/gapped/duplicate/inactive/self-final routes.
3. Only the current independent route actor can recommend, return, reject, or finally approve; every invalid transition and stale retry fails without partial state.
4. Return/resubmit, pre-decision withdrawal, terminal rejection, approved immutability, amendment supersession, route rebase, and audited late/void overrides pass deterministic tests.
5. Permissions, ownership, cycle-window timezone behavior, idempotency, audit, OpenAPI, focused/full/package gates pass; no UI or Phase 6D behavior exists.

### 6C.4 final gates

1. Administrative controls and PrimeHR/Employee Portal routes/actions exactly match backend permission, ownership, route, and scope enforcement.
2. UI makes all snapshots, targets, cascades, readiness issues, route steps, decisions, and history inspectable without exposing arbitrary code or unrelated employee data.
3. Affected frontend lint/type/build/package and focused/full Playwright pass with zero skips.
4. Provider evidence, integration/data dictionary, deployment/rollback, privacy review, and final manifest are complete.
5. No Phase 6D monitoring, accomplishment, evidence, or coaching behavior is present.

## 12. Explicit exclusions and stop boundary

- actual accomplishments, progress percentages, evidence uploads, monitoring, coaching, supervisor feedback, action items, or mid-cycle review (Phase 6D);
- self/supervisor ratings, Q/E/T results, scoring, calibration, finalization, acknowledgment, appeal, PIP, or low-rating alerts (Phase 6E);
- Jasper/PDF/analytics, L&D/R&R feeds, RSP use, promotion references, notifications, or Payroll effects (Phase 6F or later);
- arbitrary executable formulas, free-form SQL/SpEL/scripts, browser-controlled ownership/status/actor/time, direct cross-domain database writes, hard-coded approvers/weights/organization IDs, password transmission, or destructive deletion;
- legacy transaction migration without a separate data-adjudication plan.

The user approved this scope on 2026-09-09. Phase 6C.1 implementation is recorded in `PHASE_6C_1_PERFORMANCE_PLANNING_FOUNDATION_REVIEW.md`. Sequential gates and the stops before Phase 6C.4 and Phase 6D remain mandatory.
