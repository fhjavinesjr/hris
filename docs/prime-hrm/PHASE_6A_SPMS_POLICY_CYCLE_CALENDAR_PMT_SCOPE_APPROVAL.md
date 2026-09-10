# Phase 6A Performance Management Policy, Cycle, Calendar, and PMT Scope Approval

Date: 2026-09-08  
Status: Approved and complete through Phase 6A.3; stopped before Phase 6B  
Authority: `ISOFT_PRIME_HRM_CODEX_MASTER_PLAN_V2.md`, Phase 6A and section 10

## 1. Outcome and phase boundary

Phase 6A establishes the governed foundation for an agency-configurable Performance Management domain. It does not create IPCR, DPCR, OPCR, commitments, success indicators, ratings, coaching records, appeals, referrals, or reports.

The bounded delivery sequence is:

- **6A.1 — Policy and cycle foundation:** versioned SPMS policy, performance cycle, calendar milestones, backend permissions, audit, OpenAPI, and paired SQL Server/PostgreSQL migrations.
- **6A.2 — PMT governance:** effective-dated Performance Management Team and membership/role assignments, eligibility validation, history, backend permissions, audit, OpenAPI, and paired migrations if 6A.1 does not include the tables.
- **6A.3 — Administrative controls, PrimeHR UI, and Playwright acceptance:** separately approved permission rows, policy/cycle/calendar/PMT screens, operator documentation, production builds, and browser acceptance.

The separate 6A.3 approval gate was satisfied on 2026-09-08. Implementation remains stopped before Phase 6B.

## 2. Contracts inspected

### Master Plan V2

- Performance Management is owned by the modular PrimeHR service and uses `spms_*` tables.
- The domain name in code should be `performanceManagement`; `SPMS` is the configured policy/implementation label.
- The model must support agency-specific policy and workflow without source forks or arbitrary executable code.
- Authorization combines module/action permission, data scope, and effective process role.
- Draft or unapproved performance ratings must never feed RSP or R&R.

### Current ISOFT implementation

- PrimeHR has no Performance Management package, `spms_*` table, Phase 6 permission guard, API, UI route, or migration.
- PrimeHR already supplies the patterns to reuse: agency-scoped audited entities, optimistic versions, immutable published definitions, Administrative effective-permission resolution, fail-closed guards, OpenAPI contract tests, and paired provider migrations through V22.
- Administrative permission configuration currently ends at the Phase 5F RSP report rows. No SPMS feature row exists.
- Employee Portal has no current SPMS/IPCR/OPCR/DPCR route. Employee self-service remains out of Phase 6A.

### Legacy `hrblizge-zcmc`

- Legacy models and Hibernate mappings exist for `Ipcr`, `Dpcr`, `Opcr`, `TargetSpms`, `SpmsLocking`, their function/detail children, and simple pending/approved/disapproved/cancel states.
- Legacy IPCR/DPCR/OPCR actions combine filing, recommendation, approval, and reporting concerns and store mutable aggregate results such as final averages and adjectival ratings.
- Legacy Jasper templates include IPCR, DPCR, OPCR, monitoring, coaching, monthly performance, PBB evaluation, and development-plan outputs. The forms contain outputs, success indicators, actual accomplishments, quality/efficiency/timeliness ratings, remarks, rating guides, and signatory blocks.
- These assets are reference contracts only. Phase 6A must not copy agency-specific fixed columns, mutable approval flags, hardcoded signatories, or report logic into the new foundation.

## 3. Phase 6A.1 — policy and cycle foundation

### 3.1 Policy aggregate

Create a versioned policy aggregate with these concepts:

- stable policy identity and immutable version number;
- agency scope;
- code, title, description, legal basis, and configured display label;
- effectivity start/end;
- configurable cycle frequency (`ANNUAL`, `SEMI_ANNUAL`, `QUARTERLY`, or `CUSTOM`);
- flags declaring whether mid-cycle review, employee self-assessment, PMT calibration, acknowledgment, and appeal are required;
- statuses `DRAFT`, `PUBLISHED`, `RETIRED`;
- created/updated/published actor and timestamps;
- optimistic-lock version.

Rules:

- code is unique per agency among stable policy identities;
- published versions are immutable;
- publishing requires a complete, non-overlapping effectivity interval and a reason;
- a new revision is copied from an existing version and receives the next version number;
- no more than one published policy version may be effective for an agency/date;
- retirement preserves all versions and references;
- policy flags declare later workflow capabilities but do not implement those workflows in 6A.

### 3.2 Performance cycle

Create cycles bound to one exact published policy version:

- agency, code, name, period start/end, policy ID/version, timezone;
- statuses `DRAFT`, `OPEN`, `CLOSED`, `CANCELLED`;
- optional organization coverage metadata, initially `AGENCY_WIDE` only;
- created/updated/opened/closed/cancelled actor, timestamp, and reason;
- optimistic-lock version.

Rules:

- dates are inclusive and use `Asia/Manila` unless an agency configuration supplies another valid IANA timezone;
- code is unique per agency;
- an `OPEN` cycle requires a published policy effective for the complete cycle period and a valid calendar;
- overlapping cycles are rejected for the same agency and policy frequency unless the policy explicitly supports parallel named cycles in a later approved revision;
- close/cancel is audited and never deletes the cycle;
- reopening a closed/cancelled cycle is excluded from 6A and requires later explicit authority;
- cycle creation does not generate employee or office performance plans.

### 3.3 Calendar milestones

Each cycle owns ordered milestone definitions:

- controlled milestone type: `PLANNING_OPEN`, `PLANNING_DUE`, `APPROVAL_DUE`, `MONITORING_START`, `MID_CYCLE_REVIEW`, `ACCOMPLISHMENT_DUE`, `RATING_DUE`, `CALIBRATION_DUE`, `FINALIZATION_DUE`, `ACKNOWLEDGMENT_DUE`, `APPEAL_DUE`, or `CUSTOM`;
- label, start/end instant, required flag, display order, and instructions;
- created/updated actor and timestamps.

Rules:

- milestone dates must be ordered, fall within the cycle window unless explicitly marked as an allowed post-cycle closeout milestone, and may not invert start/end;
- required milestones are derived from policy flags and cannot be omitted when opening the cycle;
- custom milestones are labels and dates only, never executable code or state-machine bypasses;
- an opened cycle freezes its policy version and required milestone contract; corrections use audited amendments, not silent overwrites;
- no email, SMS, notification scheduler, or automatic escalation is included.

## 4. Phase 6A.2 — PMT governance

Create a dedicated PMT aggregate rather than reusing the RSP HRMPSB tables, because membership roles, authority, cases, and privacy boundaries differ. Reuse shared design patterns, not RSP records.

PMT fields:

- agency, code, name, mandate/reference, effective start/end;
- statuses `DRAFT`, `ACTIVE`, `INACTIVE`;
- optimistic-lock version and full audit attribution.

PMT membership fields:

- Administrative employee reference, immutable employee number/name snapshot for history, and optional organization snapshot;
- controlled role: `CHAIRPERSON`, `VICE_CHAIRPERSON`, `SECRETARIAT`, `MEMBER`, or `TECHNICAL_SUPPORT`;
- voting flag, effective start/end, designation/order reference, and remarks;
- created/updated actor and timestamps.

Rules:

- membership eligibility is resolved from authoritative HRM active-employment facts through an authenticated API; no direct HRM database read;
- one effective chairperson is required before activation;
- duplicate overlapping membership for the same employee/team is rejected;
- expired/inactive membership remains historical and grants no authority for new work;
- an actor cannot gain PMT authority from a frontend role string or stale snapshot alone;
- activation and deactivation are reasoned, audited actions; active PMTs and referenced membership are never hard-deleted;
- assigning a PMT does not create calibration work or expose employee ratings in Phase 6A.

## 5. API contract

All routes are under `/api/primehr/v1/performance-management`, are agency-scoped by trusted server configuration, use DTOs rather than entities, and return existing PrimeHR error/audit shapes.

### Policy

- `GET /policies`
- `GET /policies/{policyId}`
- `POST /policies`
- `PUT /policies/{policyId}`
- `POST /policies/{policyId}/publish`
- `POST /policies/{policyId}/revisions`
- `POST /policies/{policyId}/retire`
- `GET /policies/effective?on=YYYY-MM-DD`

### Cycle and calendar

- `GET /cycles`
- `GET /cycles/{cycleId}`
- `POST /cycles`
- `PUT /cycles/{cycleId}`
- `POST /cycles/{cycleId}/open`
- `POST /cycles/{cycleId}/close`
- `POST /cycles/{cycleId}/cancel`
- `PUT /cycles/{cycleId}/milestones`

Milestones are replaced atomically while the cycle is draft. Any allowed opened-cycle correction uses an explicit amendment endpoint and reason only if tests demonstrate immutable before/after audit snapshots; otherwise corrections remain excluded.

### PMT

- `GET /pmts`
- `GET /pmts/{pmtId}`
- `POST /pmts`
- `PUT /pmts/{pmtId}`
- `PUT /pmts/{pmtId}/members`
- `POST /pmts/{pmtId}/activate`
- `POST /pmts/{pmtId}/deactivate`

Every mutation carries an expected optimistic version. Stale writes return `409`; invalid lifecycle transitions return a stable validation/conflict code; denied access returns `403`; unavailable authoritative dependencies return `503` and fail closed.

## 6. Permission contract

Phase 6A backend feature keys:

| Feature key | Actions | Initial data scope |
|---|---|---|
| `primehr.performance-policy` | Access, Add, Edit, Publish | Agency-wide |
| `primehr.performance-cycle` | Access, Add, Edit, Finalize | Agency-wide |
| `primehr.performance-pmt` | Access, Add, Edit, Publish | Agency-wide |

Semantics:

- `Publish` controls policy publish/retire and PMT activate/deactivate.
- `Finalize` controls cycle open/close/cancel.
- `Delete` is intentionally absent; draft removal is not part of the initial foundation.
- Administrator role `"1"` preserves the established full-access contract.
- Backend guards enforce every read/action. UI visibility in 6A.3 will be only a usability layer.
- PMT process-role authority is additive to feature permission in later calibration phases; 6A membership alone grants no access to ratings because ratings do not yet exist.

## 7. Persistence and migrations

Create equivalent PostgreSQL and SQL Server Flyway migrations starting at PrimeHR **V23**. Expected portable tables:

- `spms_policy`
- `spms_policy_version`
- `spms_cycle`
- `spms_cycle_milestone`
- `spms_pmt`
- `spms_pmt_member`

Migration requirements:

- explicit constraints for agency/code/version uniqueness, status values, chronological ranges, positive display order, and foreign keys;
- provider-neutral Java/JPA queries and matching provider migrations;
- no cross-database foreign keys or SQL joins;
- no copy of legacy IPCR/DPCR/OPCR transactions during 6A;
- no automatic import of legacy PMT assumptions, `SpmsLocking`, or `TargetSpms` rows;
- a later legacy migration requires a separate mapping/reconciliation plan because current records do not provide the versioned policy/cycle/PMT semantics required here.

## 8. Audit and privacy

Audit create/update/publish/retire/open/close/cancel/activate/deactivate and membership replacement with target ID, agency, expected/result version, reason, and before/after fingerprints. Do not log JWTs, full employee records, or sensitive remarks. General policy/cycle reads contain no employee performance data. PMT membership is staff-only and returns only the minimum authoritative identity fields.

## 9. Phase 6A.3 UI and acceptance scope

This separately approved slice is complete:

- add the three exact feature rows to Administrative Permission;
- add PrimeHR navigation/routes for Performance Policies, Performance Cycles/Calendar, and PMT Governance;
- implement loading, empty, denied, validation, conflict, dependency-unavailable, history, and lifecycle states;
- keep Employee Portal, Careers, HRM, Timekeeping, and Payroll UI unchanged;
- add Playwright scenarios for exact permissions, policy revision/publish, cycle calendar validation/open/close, PMT membership effectivity/activation, stale writes, denied direct routes, and absence of Phase 6B behavior;
- run strict type-check/lint/production packaging for affected frontends and the complete PrimeHR Playwright regression.

## 10. Acceptance gates

### 6A.1 gates before 6A.2

1. Paired V23 migrations are structurally equivalent and pass fresh and upgrade schema tests.
2. Policy versions are immutable after publication and effectivity conflicts are rejected.
3. Cycles bind exact published policy versions; invalid/overlapping windows and incomplete calendars are rejected.
4. Lifecycle actions are independently authorized, reasoned, audited, and stale-write safe.
5. OpenAPI documents every DTO, status, action, error, and boundary.
6. Focused tests, PrimeHR clean tests, and the affected reactor package pass.
7. SQL Server and PostgreSQL behavior is validated where environments are available; an unavailable live provider is explicitly disclosed and does not relax portability tests.
8. No PMT, template, commitment, rating, coaching, report, or UI behavior is introduced.

### 6A.2 gates before 6A.3

1. Effective membership, unique chair, overlap, employee eligibility, historical retention, and inactive-authority rules pass.
2. HRM lookup is authenticated, provider-neutral, bounded, and fails closed without direct database access.
3. PMT mutation permissions and lifecycle transitions are independently enforced and audited.
4. Paired migrations/parity, OpenAPI, focused tests, PrimeHR clean tests, and full affected package pass.
5. No calibration, employee performance data, 6A.3 permission UI, or Phase 6B behavior is introduced.

### 6A.3 final gates

1. Administrative controls and PrimeHR navigation/actions exactly match backend permission semantics.
2. Production type-check/lint/build/package gates pass for every affected frontend.
3. Focused Phase 6A Playwright and the full regression pass with zero skips.
4. SQL Server/PostgreSQL migration evidence, operator guide, deployment/rollback, privacy, and final review manifest are complete.
5. No Phase 6B behavior is present.

## 11. Explicit exclusions

- SPMS template builder, Index of Success Indicators, KRA/KPI definitions, target/weight/rating formulas, or executable expressions (Phase 6B);
- OPCR/DPCR/IPCR planning, cascading, submission, recommendation, or approval (Phase 6C);
- monitoring, accomplishments, evidence attachments, coaching, feedback, action items, or mid-cycle review (Phase 6D);
- self/supervisor rating, calibration cases, final rating, acknowledgment, appeal, PIP, or low-rating alerts (Phase 6E);
- L&D, competency, R&R, promotion/RSP feeds, or performance reports (Phase 6F);
- Employee Portal performance pages, Jasper/PDF output, notifications, scheduler jobs, electronic signatures, document uploads, generic workflow/BPMN builder, automatic decisions, or AI scoring;
- direct reads/writes to another service database, cross-service transactions, hardcoded agency roles/signatories/calendar dates, or legacy data mutation;
- Phase 7 or later behavior.

## 12. Approval semantics

Approval of this scope should authorize 6A.1 only, allow progression to 6A.2 only after every 6A.1 gate passes, and retain a separate gate before 6A.3 and Phase 6B.

Recommended approval wording:

> Approve Phase 6A as defined. Proceed with Phase 6A.1, and continue to Phase 6A.2 only after all Phase 6A.1 gates pass. Stop before Phase 6A.3 until I approve the Administrative permission controls, PrimeHR UI, and Playwright acceptance. Stop before Phase 6B.
