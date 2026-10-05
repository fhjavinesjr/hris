# Phase 6D Monitoring and Coaching Scope Approval

Prepared: 2026-09-24

Status: Complete through Phase 6D.3; stopped before separately reviewed Phase 6E rating work

Authority: `ISOFT_PRIME_HRM_CODEX_MASTER_PLAN_V2.md`, Phase 6D and sections 5.10-5.14 and 10.3-10.4

## 1. Outcome and controlled delivery sequence

Phase 6D adds governed monitoring and coaching to an approved Phase 6C performance commitment. It records progress, actual accomplishments, evidence, supervisor feedback, coaching sessions, action items, and a configured mid-cycle review without calculating or publishing a performance rating.

Delivery is split into three gated runs:

1. **6D.1 monitoring foundation:** progress/accomplishment updates, secured evidence, supervisor feedback, correction history, permissions, audit, OpenAPI, and paired SQL Server/PostgreSQL migrations.
2. **6D.2 coaching and mid-cycle review:** coaching journal, action items, employee acknowledgment, mid-cycle review snapshot/outcome, permissions, audit, OpenAPI, and paired migrations.
3. **6D.3 controls and interfaces:** Administrative permission rows, PrimeHR supervisor/PMT interfaces, Employee Portal monitoring/coaching interface, and Playwright acceptance.

Each backend run passed its focused and complete gates before the next began. The separately approved Phase 6D.3 controls and interfaces are complete. Stop before Phase 6E.

## 2. Contracts inspected and legacy adjudication

The approved Phase 6C commitment version remains the immutable planning baseline. Monitoring data references its exact version and item IDs; it never edits the approved target, source indicator, cascade, owner, route, or organization snapshot.

The legacy ZCMC monitoring and coaching forms establish useful concepts: employee, coach, agenda, goal, issue, agreed action, required resources, commitment date, next meeting, remarks, task dates, accomplishment, periodic progress, and monitoring remarks. They are not adopted as authoritative persistence because they permit mutable update/delete behavior, lack exact commitment/item lineage, do not enforce restricted visibility, and do not preserve correction or acknowledgment history.

Retained concepts:

- actual accomplishment and periodic progress against a planned item;
- evidence and remarks;
- coach/coachee, agenda, goal, issue, agreed action, resources, due date, next meeting, and follow-through;
- monitoring dates and mid-cycle review where policy requires it.

Rejected concepts:

- physical deletion of monitoring/coaching history;
- editing the approved commitment through a monitoring form;
- free selection of another employee or supervisor by a portal user;
- password-in-action approval or acknowledgment;
- hard-coded quarters, weights, scoring, or rating logic;
- storing secured evidence binaries directly in the transaction table;
- treating a progress percentage or accomplishment as an approved rating.

## 3. Phase 6D.1 - monitoring foundation

### 3.1 Monitoring case

- One active monitoring case is created idempotently for each approved commitment version.
- A case stores the exact commitment/version, cycle, policy, form type, owner, organization, route, and source fingerprints needed for historical interpretation.
- Only `APPROVED` Phase 6C versions may open monitoring. A superseded planning version remains readable but cannot receive new monitoring transactions after its approved successor becomes effective.
- Monitoring uses the cycle timezone and the configured `MONITORING_START` and `ACCOMPLISHMENT_DUE` milestones. An authorized override requires a reason and is audited.

### 3.2 Progress and accomplishment updates

- Updates are attached to an exact commitment item and identify the reporting date/period.
- An update may contain narrative accomplishment, optional numeric accomplished value, optional progress percentage, issues/risks, support needed, and employee remarks.
- Numeric values use `BigDecimal` with explicit scale. Percentages are bounded from 0 through 100 but are informational and never become a Phase 6E rating.
- Draft updates are editable by their owner. Submission creates an immutable submitted revision.
- Corrections create a successor revision linked to the prior update; submitted history is never overwritten or deleted.
- A supervisor may accept for monitoring record, return for correction, or add structured feedback. Acceptance does not approve a rating or change the approved commitment.
- Final accomplishment submission is a distinct lifecycle action that freezes the employee/office accomplishment set for the later Phase 6E rating workflow. A supervisor may return it for correction before Phase 6E starts.

### 3.3 Secured evidence

- Evidence metadata references an exact monitoring update and commitment item.
- Files use the existing secured storage abstraction with generated storage keys, checksum, MIME allow-list, size limit, and path-traversal protection.
- Download is authorized on every request. Storage keys and local paths are never returned to browsers.
- Evidence may be superseded or voided with a reason; it is not physically deleted through the workflow.
- Required-evidence readiness is evaluated from the immutable Phase 6C item snapshot. Missing required evidence blocks final accomplishment submission, not ordinary progress updates.

## 4. Phase 6D.2 - coaching and mid-cycle review

### 4.1 Coaching journal

- A coaching session belongs to the monitoring case and may optionally reference one or more commitment items.
- The supervisor/coach creates the session using agenda, goal, observed issue, agreed action, resources/support, action due date, next meeting, and restricted notes.
- The employee may add a response and acknowledge the session. Acknowledgment confirms receipt only; it does not mean agreement and does not waive any later appeal right.
- Corrections create a successor revision. A session may be voided only with a reason and remains in the audit/history view.

### 4.2 Action items and follow-through

- Action items have an accountable actor, due date, status (`OPEN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`), completion note/date, and version.
- The accountable employee may update progress on their own action item. The coach may verify completion or reopen it with a reason.
- Action items do not automatically create L&D referrals, performance improvement plans, disciplinary records, or ratings.

### 4.3 Mid-cycle review

- A mid-cycle review exists only when the exact policy version requires it.
- It snapshots monitoring readiness, unresolved issues, open action items, evidence completeness, and supervisor/employee narratives.
- Completion requires both supervisor submission and employee acknowledgment. Authorized non-acknowledgment closure requires a reason and audit evidence.
- The review may recommend target amendment through the existing Phase 6C amendment workflow, but it cannot directly mutate a target.

## 5. Authority, scope, and privacy

Canonical features:

| Feature key | Actions | Allowed data scopes |
|---|---|---|
| `primehr.performance-monitoring` | Access, Add, Edit, Submit, Approve | Own Records, Assigned Records, Agency Wide |
| `primehr.performance-coaching` | Access, Add, Edit, Submit, Approve | Own Records, Assigned Records, Agency Wide |

Rules:

- Employee Portal always forces `OWN_RECORDS`; it accepts no employee identity from the browser.
- The owner may maintain own drafts, submit updates/accomplishments, respond to coaching, and acknowledge sessions/reviews when the exact action permission exists.
- Assigned-record authority is derived from the immutable Phase 6C approval-route snapshot. The first recommending/review step is the operational supervisor/coach unless an explicit effective assignment is introduced later.
- Other route actors may read within their assigned case only. Final approver status alone does not grant edit/coaching authority.
- PMT access requires both the exact feature/action permission and an effective PMT process role. PMT membership alone grants nothing.
- Agency-wide administrators remain permission-bound; administrator status does not bypass case confidentiality or audit.
- Private coaching notes are visible only to their author and explicitly authorized supervisory/PMT readers. Employee-visible feedback is stored separately.
- Backend ownership, route assignment, process role, lifecycle, and data-scope enforcement is mandatory; sidebar/page hiding is not authorization.

## 6. Lifecycle and concurrency

- Monitoring case: `OPEN`, `ACCOMPLISHMENT_SUBMITTED`, `RETURNED`, `READY_FOR_RATING`, `VOIDED`.
- Progress update: `DRAFT`, `SUBMITTED`, `ACCEPTED`, `RETURNED`, `SUPERSEDED`, `VOIDED`.
- Coaching session: `DRAFT`, `ISSUED`, `ACKNOWLEDGED`, `SUPERSEDED`, `VOIDED`.
- Mid-cycle review: `DRAFT`, `SUBMITTED`, `ACKNOWLEDGED`, `CLOSED_WITHOUT_ACKNOWLEDGMENT`, `SUPERSEDED`, `VOIDED`.
- Every mutable command requires an optimistic record version. Stale writes return a conflict and never silently overwrite data.
- Idempotency keys protect case opening, update submission, acknowledgment, final accomplishment submission, and mid-cycle completion.
- All lifecycle actions record actor, timestamp, reason, correlation/idempotency key, old/new state, aggregate version, and relevant fingerprints.

## 7. API direction

All routes are under `/api/primehr/v1/performance-management`.

Monitoring:

- `POST /monitoring-cases`
- `GET /monitoring-cases`
- `GET /monitoring-cases/mine`
- `GET /monitoring-cases/{caseId}`
- `POST /monitoring-cases/{caseId}/updates`
- `PUT /monitoring-updates/{updateId}`
- `POST /monitoring-updates/{updateId}/submit`
- `POST /monitoring-updates/{updateId}/accept`
- `POST /monitoring-updates/{updateId}/return`
- `POST /monitoring-updates/{updateId}/corrections`
- `POST /monitoring-cases/{caseId}/accomplishments/submit`
- `POST /monitoring-cases/{caseId}/accomplishments/return`

Evidence:

- `POST /monitoring-updates/{updateId}/evidence`
- `GET /monitoring-evidence/{evidenceId}/content`
- `POST /monitoring-evidence/{evidenceId}/supersede`
- `POST /monitoring-evidence/{evidenceId}/void`

Coaching and mid-cycle:

- `POST /monitoring-cases/{caseId}/coaching-sessions`
- `PUT /coaching-sessions/{sessionId}`
- `POST /coaching-sessions/{sessionId}/issue`
- `POST /coaching-sessions/{sessionId}/acknowledge`
- `POST /coaching-sessions/{sessionId}/corrections`
- `POST /coaching-sessions/{sessionId}/void`
- `POST /coaching-sessions/{sessionId}/action-items`
- `PUT /coaching-action-items/{actionItemId}`
- `POST /coaching-action-items/{actionItemId}/verify`
- `POST /coaching-action-items/{actionItemId}/reopen`
- `POST /monitoring-cases/{caseId}/mid-cycle-review`
- `PUT /mid-cycle-reviews/{reviewId}`
- `POST /mid-cycle-reviews/{reviewId}/submit`
- `POST /mid-cycle-reviews/{reviewId}/acknowledge`
- `POST /mid-cycle-reviews/{reviewId}/close-without-acknowledgment`

Exact request/response DTOs will expose IDs and safe metadata, never entities, storage paths, unrestricted private notes, or client-selected employee authority.

## 8. Persistence and portability

- Paired forward-only SQL Server/PostgreSQL V31 and V32 migrations use equivalent constraints, indexes, decimal precision, timestamp semantics, and tenant keys.
- Delivered 6D.1 tables: `spms_monitoring_case`, `spms_monitoring_update`, `spms_monitoring_feedback`, `spms_monitoring_evidence`, and monitoring action history.
- Delivered 6D.2 tables: `spms_coaching_session`, `spms_coaching_session_item`, `spms_coaching_action_item`, and `spms_mid_cycle_review`.
- No cross-database join, provider-specific shared Java query, destructive migration, or `ddl-auto` schema mutation is allowed.
- Existing Phase 6C rows and legacy ZCMC rows are not rewritten or imported automatically.

## 9. Phase 6D.3 interfaces and acceptance

Administrative:

- add exact monitoring and coaching permission rows with independent action flags and allowed scopes.

PrimeHR:

- Monitoring Inbox for assigned supervisors/authorized PMT;
- commitment/item progress, evidence, accomplishment readiness, feedback, coaching, action-item, and mid-cycle history;
- exact permission/lifecycle controls and stale-write feedback.

Employee Portal:

- My Performance Monitoring page using authenticated own-record endpoints;
- own progress/accomplishment drafts, evidence, submissions, returned corrections, coaching response/acknowledgment, action-item follow-through, and mid-cycle acknowledgment;
- no employee selector and no access to another employee's URL or private supervisor notes.

Playwright must cover denied access, own/assigned/agency scope, direct URL enforcement, authoritative supervisor route, evidence ownership/download, return/correction history, stale conflicts, idempotency, coaching visibility, acknowledgment semantics, mid-cycle policy gating, and absence of Phase 6E actions.

## 10. Acceptance gates

### 6D.1 gates before 6D.2

1. Only approved exact commitment versions open monitoring cases, idempotently.
2. Progress/accomplishment lifecycle, correction lineage, final accomplishment freeze/return, and required-evidence readiness pass.
3. Evidence storage/download/void rules, ownership, MIME/size/checksum/path safety, and restricted responses pass.
4. Permission, route assignment, scope, audit, concurrency, OpenAPI, V31 parity, fresh-schema, upgrade, focused/full tests, and package gates pass.
5. No coaching, mid-cycle, rating, calibration, appeal, report, or downstream referral behavior exists.

### 6D.2 gates before 6D.3

1. Coaching session/revision/issue/acknowledgment/void and private-versus-employee-visible note rules pass.
2. Action-item ownership, completion verification/reopen, history, and due-date rules pass.
3. Policy-gated mid-cycle review, acknowledgment/non-acknowledgment closure, and Phase 6C amendment referral pass.
4. Permission plus process-role checks, scope, audit, concurrency, OpenAPI, V32 parity, fresh-schema, upgrade, focused/full tests, and package gates pass.
5. No rating, Q/E/T score, calibration, appeal, report, PIP, L&D/R&R feed, or Phase 6E/6F behavior exists.

### 6D.3 final gates

1. Administrative, PrimeHR, and Employee Portal strict type-check/lint/production builds pass for affected files/apps.
2. Backend permission remains authoritative for every page and action.
3. Focused Phase 6D Playwright and the complete feasible regression pass; unrelated environment failures are disclosed with evidence.
4. SQL Server/PostgreSQL portability and live-provider limitations are recorded.
5. Documentation, deployment/rollback notes, and the Phase 6E stop boundary are current.

## 11. Explicit exclusions and stop boundary

Phase 6D does not implement:

- self-rating, supervisor rating, Q/E/T scoring, weighted score, adjectival rating, or rating history;
- PMT calibration, final rating approval, employee rating acknowledgment, appeal/protest, or rating reopening;
- automatic low-rating alert, PIP, competency reassessment, L&D recommendation, R&R eligibility, or RSP/promotion feed;
- monitoring/coaching Jasper/PDF or analytics reporting;
- Payroll, Timekeeping, HRM, RSP, Careers, or other CoreHR workflow mutation;
- automatic legacy transaction import.

Those belong to Phase 6E, Phase 6F, or a separately reviewed migration. Implementation must stop after Phase 6D.3 acceptance.
