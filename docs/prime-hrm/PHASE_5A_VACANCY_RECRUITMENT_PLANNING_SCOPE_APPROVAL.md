# ISOFT PRIME-HRM Phase 5A - Vacancy and Recruitment Planning Scope Approval

Prepared: 2026-08-29

Status: Complete. Phase 5A.1, Phase 5A.2, and the separately approved Phase 5A.3 gates passed on 2026-08-29. Work is stopped before Phase 5B.

## 1. Objective and phase boundary

Phase 5A will establish the Recruitment, Selection and Placement (RSP) foundation needed to identify actual or anticipated vacancies, prepare an auditable recruitment plan, request authority to fill a vacancy, approve and publish a vacancy notice, record configurable publication channels and periods, and generate the approved vacancy notice PDF.

Phase 5A ends at publication planning and notice generation. It does not create applicant accounts, accept applications or documents, screen qualifications, conduct examinations/interviews, rank candidates, record HRMPSB deliberations, select an appointee, create an employee, modify an appointment, or perform onboarding. Those belong to Phases 5B-5F and require separate approval.

## 2. Repository findings and architecture corrections

- Administrative currently owns Job Position, Plantilla, Area, Business Unit, salary grade/step, and permission rules. `JobPosition` contains only name and salary grade/step; `Plantilla` contains only name and Job Position ID.
- Administrative has no authoritative Qualification Standards model. Phase 5A cannot safely publish education, training, experience, and eligibility as manually typed unversioned vacancy text.
- The existing Administrative PrimeHR position-target contract already provides stable Job Position/Plantilla identity, salary grade/step, source fingerprint, and fetched time. It can be extended additively or complemented by an RSP-specific contract without changing existing Phase 2 consumers.
- HumanResource owns appointments and has an `activeAppointment` flag plus Plantilla ID. Its legacy `is-plantilla-taken` endpoint returns only a boolean and is insufficient as the historical basis for a vacancy decision because it has no source timestamp, appointment reference, or fingerprint.
- PrimeHR owns the RSP transaction and its immutable decision snapshots. It must not query Administrative or HRM tables directly.
- Phase 2 already provides approved, effective Position Competency Profiles and Plantilla-over-Job-Position resolution. Phase 5A will reference and snapshot the exact resolved profile version; it will not copy or recalculate competency requirements.
- PrimeHR's latest migration is V10. Phase 5A must add equivalent forward-only SQL Server and PostgreSQL migrations after V10.
- Existing authorization supports Access, Add, Edit, Delete/archive, Submit, Approve, Publish, administrator compatibility, optimistic conflict handling, and `AGENCY_WIDE`. These are sufficient for the initial Phase 5A workflow without adding a generic workflow engine.
- `prime-hr-software` already has typed clients, permission-aware navigation/pages, SweetAlert2, SCSS modules, and repeatable Playwright support. No Applicant Portal exists and none will be introduced in Phase 5A.

## 3. Authoritative source readiness

### 3.1 Administrative Qualification Standards

Phase 5A.1 will add an additive, versioned Qualification Standard master owned by Administrative and linked to one Job Position. It will capture at minimum:

- education requirement;
- training requirement, including hours where applicable;
- experience requirement, including duration where applicable;
- eligibility requirement;
- optional license or other statutory requirement;
- effectivity, definition version, predecessor, status, source/legal basis, and audit metadata.

The lifecycle is controlled and immutable after publication:

```text
DRAFT -> ACTIVE
DRAFT -> ARCHIVED
ACTIVE -> successor DRAFT -> ACTIVE
```

Only one ACTIVE version may be effective for the same Job Position and date. Publishing a successor closes an overlapping predecessor atomically. A vacancy cannot proceed to publication without an effective ACTIVE Qualification Standard. Existing Job Position and Plantilla records remain compatible; this is an additive relationship, not a destructive rewrite.

Administrative will expose authenticated, minimum-necessary integration responses for:

- exact Plantilla and parent Job Position facts;
- selected Business Unit/organizational placement facts;
- effective Qualification Standard and its version/fingerprint;
- salary grade/step and source timestamps/fingerprints.

The Phase 2 position-target API remains backward compatible.

### 3.2 HRM Plantilla occupancy

HumanResource will expose an authenticated PrimeHR integration contract for exact Plantilla occupancy. It returns only the minimum necessary facts:

- Plantilla ID;
- occupied/unoccupied as of the server check;
- active appointment reference when occupied;
- appointment/assumption date needed to explain the result;
- source fingerprint and fetched timestamp.

Employee PDS, contact, payroll, leave, and unrelated appointment history are excluded. PrimeHR treats HRM as authoritative and stores only the source reference and vacancy-decision snapshot.

### 3.3 Actual and anticipated vacancy rules

- `ACTUAL`: HRM reports no active appointment for the exact Plantilla at readiness and again at the material transition.
- `ANTICIPATED`: HRM may still report an active appointment, but the planner must provide an anticipated vacancy date, reason category, explanation, and supporting authority/reference where applicable.
- A user cannot merely override an occupied Plantilla into `ACTUAL`.
- Source readiness is rechecked before plan submission, authority approval, publication approval, and publication. A changed source fingerprint produces `409 Conflict` or a documented readiness failure before mutation.
- Source changes do not rewrite already approved/published historical snapshots.

## 4. Phase 5A domain model

### 4.1 Recruitment Plan

A recruitment plan groups vacancy intentions for a defined planning period:

- code, title, period start/end, description, status, and audit metadata;
- one or more vacancy requests;
- optimistic record version and immutable decision history.

Lifecycle:

```text
DRAFT -> SUBMITTED -> APPROVED
          |             |
          v             v
       RETURNED      ARCHIVED
```

Returned plans go back to DRAFT with the return reason retained in audit history. Approval requires at least one valid vacancy request and separation of duties. An administrator override remains possible only with an explicit reason and audit event.

### 4.2 Vacancy Request / Authority to Fill

Each vacancy request belongs to one plan and references one exact Plantilla. It records:

- actual or anticipated vacancy type and supporting reason/date;
- recruitment priority and target fill date;
- justification and requested headcount, initially exactly one per Plantilla;
- selected authoritative organizational placement;
- Administrative and HRM source references/fingerprints;
- exact Qualification Standard and Position Competency Profile versions;
- source facts required for later immutable publication.

Lifecycle:

```text
DRAFT -> SUBMITTED -> AUTHORIZED
          |              |
          v              v
       RETURNED       CANCELLED

SUBMITTED -> DECLINED
```

Only an AUTHORIZED request in an APPROVED plan can create a publication draft. Duplicate active requests for the same Plantilla and overlapping recruitment period are rejected transactionally.

### 4.3 Vacancy Publication

A publication is linked to one authorized vacancy request and captures the immutable public basis:

- Plantilla, Job Position, salary grade/step, and organizational placement snapshot;
- exact Qualification Standard version and text snapshot;
- exact Position Competency Profile version and requirement snapshot;
- application/publication opening and closing dates;
- internal, external, or both visibility;
- instructions, place of assignment, contact/submission guidance, and approved notice text;
- one or more agency-configurable publication channel records;
- approval/publication actors, times, reasons, and optimistic record version.

Lifecycle:

```text
DRAFT -> SUBMITTED -> APPROVED -> PUBLISHED -> CLOSED
          |             |
          v             v
       RETURNED      CANCELLED
```

Approval and publication are independent permissions. The submitter cannot approve or publish the same notice unless using the audited administrator override. A PUBLISHED notice is immutable; corrections require a controlled successor or cancellation, never silent editing.

Publication channel names are data, not hard-coded government assumptions. Examples such as agency website, CSC portal, bulletin board, or social media may be configured by the agency. Each selected channel records its publication date/reference and does not imply that ISOFT HRIS has automatically posted to an external service.

## 5. Persistence and portability

Proposed provider-equivalent migrations:

### Phase 5A.1 / V11 foundation

- Administrative additive Qualification Standard schema/migration appropriate to its existing deployment model;
- `rsp_recruitment_plan`;
- `rsp_vacancy_request`;
- source snapshot/version fields, uniqueness, relationships, status checks, indexes, and optimistic versions.

### Phase 5A.2 / V12 publication lifecycle

- `rsp_vacancy_publication`;
- `rsp_vacancy_publication_channel`;
- any normalized immutable publication requirement snapshot child needed to preserve the exact QS/competency basis without mutable JSON dependence.

The final names may be adjusted during implementation only to match established repository naming consistently; the logical ownership and relationships may not be weakened.

SQL Server and PostgreSQL scripts must remain logically equivalent. Shared Java uses derived queries, JPQL, Specifications, and `Pageable`; it must not contain `TOP`, `LIMIT`, `GETDATE`, `ILIKE`, provider casts, dialect branches, or cross-database joins. Production migration changes are forward-only and reviewable. SQL Server is the blocking real-provider gate under the current user-approved policy; PostgreSQL parity/mode tests remain mandatory and a missing live PostgreSQL run is disclosed.

## 6. REST contracts

All PrimeHR endpoints remain under `/api/primehr/v1`, use explicit DTOs, token-derived actor/agency context, validation, pagination, correlation IDs, stable errors, and server-side authorization.

Proposed Phase 5A resources:

```http
GET    /rsp/recruitment-plans
POST   /rsp/recruitment-plans
GET    /rsp/recruitment-plans/{planId}
PUT    /rsp/recruitment-plans/{planId}
POST   /rsp/recruitment-plans/{planId}/submit
POST   /rsp/recruitment-plans/{planId}/return
POST   /rsp/recruitment-plans/{planId}/approve
POST   /rsp/recruitment-plans/{planId}/archive

POST   /rsp/recruitment-plans/{planId}/vacancies
PUT    /rsp/vacancy-requests/{requestId}
GET    /rsp/vacancy-requests/{requestId}/readiness
POST   /rsp/vacancy-requests/{requestId}/submit
POST   /rsp/vacancy-requests/{requestId}/return
POST   /rsp/vacancy-requests/{requestId}/authorize
POST   /rsp/vacancy-requests/{requestId}/decline
POST   /rsp/vacancy-requests/{requestId}/cancel

GET    /rsp/vacancy-publications
POST   /rsp/vacancy-publications
GET    /rsp/vacancy-publications/{publicationId}
PUT    /rsp/vacancy-publications/{publicationId}
POST   /rsp/vacancy-publications/{publicationId}/submit
POST   /rsp/vacancy-publications/{publicationId}/return
POST   /rsp/vacancy-publications/{publicationId}/approve
POST   /rsp/vacancy-publications/{publicationId}/publish
POST   /rsp/vacancy-publications/{publicationId}/cancel
POST   /rsp/vacancy-publications/{publicationId}/close
GET    /rsp/vacancy-publications/{publicationId}/notice.pdf
```

Administrative Qualification Standard and HRM occupancy contracts use their existing service-specific `/api/integration/v1/primehr/...` conventions. Exact OpenAPI paths are finalized from the implementation pattern without breaking existing endpoints.

The API never trusts the browser to supply source names, salary values, occupancy, Qualification Standard content, Position Profile requirements, status, approval actor, or publication actor as authoritative facts.

## 7. Authorization and Administrative controls

Add these PrimeHR permission features:

- `primehr.rsp-recruitment-planning`
- `primehr.rsp-vacancy-publication`

Use existing action columns:

| Feature | Actions | Initial data scope |
|---|---|---|
| Recruitment planning and authority to fill | Access, Add, Edit, Delete/archive/cancel, Submit, Approve | `AGENCY_WIDE` |
| Vacancy publication | Access, Add, Edit, Delete/cancel, Submit, Approve, Publish | `AGENCY_WIDE` |

Administrative Qualification Standard maintenance receives its own exact Administrative permission row using Access/Add/Edit/Delete/Publish as applicable.

The initial Phase 5A workflow requires `AGENCY_WIDE` because verified organizational-unit supervisor/assignment authority remains incomplete. `OWN_RECORDS` must not be misused for agency vacancy transactions. Assigned-office/organizational-unit scopes are deferred until authoritative assignments exist. Legacy permission JSON fails closed. Administrator retains supported actions with the same audit requirements and cannot bypass statutory/source validity.

## 8. UI and Jasper notice

### Administrative UI

- Qualification Standards administration linked to Job Position;
- version/effectivity/history and controlled publication;
- Phase 5A permission controls.

### PrimeHR UI

Add a permission-aware `Recruitment Planning` area with:

- recruitment plan list/detail/draft workflow;
- vacancy source search and readiness display;
- actual/anticipated vacancy request forms;
- source freshness and exact QS/competency version display;
- authority-to-fill submission/return/authorization decisions;
- publication draft, channel/period, approval, publication, cancellation/closure, history, and conflict states;
- clear empty/loading/denied/validation/server-error behavior.

No `/careers`, applicant registration, public application, or document-upload route is created in Phase 5A.

### Vacancy notice PDF

The approved notice PDF is generated only from a PrimeHR-owned typed report DTO and `JRBeanCollectionDataSource`. JRXML owns layout only and contains no database query. It includes authoritative agency header, position/Plantilla, salary grade, place of assignment, QS, competency requirements where applicable, publication/application period, channels/instructions, notice identifier, and approval/publication evidence. It handles null logos/optional fields, pagination, long requirements, and no developer-machine file paths.

Only an APPROVED or PUBLISHED notice may produce an official PDF. DRAFT, SUBMITTED, RETURNED, CANCELLED, and CLOSED behavior must be deliberately represented; a cancelled notice cannot masquerade as current.

## 9. Auditing, concurrency, and invariants

- Every create/update/submit/return/approve/authorize/decline/publish/cancel/close/archive action records actor, time, reason where required, correlation ID, and before/after evidence in `prime_audit_event`.
- Multi-row transitions and snapshot capture are transactional.
- Optimistic versions produce `409 Conflict`; stale screens never overwrite current decisions.
- Separation of duties is enforced in services, not only in the UI.
- Duplicate active vacancy requests/publications for the same Plantilla and period are rejected.
- Approval/publication rechecks source existence, occupancy, Qualification Standard, Position Profile, effectivity, and source fingerprint.
- Historical snapshots remain readable after source changes or record closure.
- No hard delete is exposed for submitted/approved/published records.

## 10. Controlled implementation slices

### Phase 5A.1 - Source readiness and draft foundation

- Administrative versioned Qualification Standard foundation and minimum integration contract;
- HRM authenticated Plantilla-occupancy integration contract;
- PrimeHR recruitment-plan and vacancy-request draft/readiness domain, V11 dual migrations, API, RBAC, audit, OpenAPI, and tests;
- no lifecycle approval/publication UI, Jasper, applicant route, or Phase 5B behavior.

Phase 5A.1 gates:

- source ownership and minimum-data contract tests;
- fail-closed Access/Add/Edit/data-scope behavior;
- draft validation, source freshness, duplicate Plantilla, optimistic conflict, tenant scope, and rollback tests;
- SQL Server fresh V1-V11 and populated V10-to-V11;
- PostgreSQL-mode/parity checks;
- affected Maven clean test/package and repository/secret/boundary audits.

### Phase 5A.2 - Controlled authority and publication backend

Begins only after every Phase 5A.1 gate passes.

- recruitment-plan submission/return/approval;
- vacancy authority submission/return/authorize/decline/cancel;
- publication/channel model and V12 dual migrations;
- approval/publication/close/cancel lifecycle;
- exact snapshots, separation of duties, audit, OpenAPI, and backend tests;
- no UI/Jasper/public applicant route.

Phase 5A.2 gates:

- complete transition matrix including denied and invalid transitions;
- separation-of-duties and administrator-override audit tests;
- source-change conflict and transactional rollback tests;
- SQL Server fresh V1-V12 and populated V11-to-V12;
- PostgreSQL-mode/parity checks;
- affected Maven clean test/package and boundary audit.

### Phase 5A.3 - Administrative UI, PrimeHR UI, Jasper, and Playwright

Separately approved and completed on 2026-08-29 after all Phase 5A.2 gates passed.

- Administrative Qualification Standard and permission UI;
- PrimeHR recruitment planning/publication UI;
- portable vacancy notice Jasper PDF;
- repeatable Playwright allowed/denied/success/validation/return/approval/publication/conflict/PDF matrix;
- user guide, E2E documentation, progress update, and final Phase 5A review manifest.

Work stops before Phase 5B.

## 11. Acceptance criteria

Phase 5A is complete only when:

1. Actual vacancy status comes from the exact HRM Plantilla occupancy result and cannot be falsified by the browser.
2. Anticipated vacancies require a future date, reason, and evidence/reference while preserving current occupancy truth.
3. Every published notice contains immutable exact source, QS, Position Profile, workflow, and policy evidence.
4. Source changes are detected before material transitions and never rewrite historical decisions.
5. Qualification Standards are versioned and authoritative rather than copied into uncontrolled Job Position text.
6. Recruitment plan, authority-to-fill, approval, and publication states are distinct and auditable.
7. Submitter/approver/publisher separation and exact permissions are enforced on the backend.
8. Duplicate active vacancy transactions for the same Plantilla/period are prevented atomically.
9. SQL Server acceptance passes and PostgreSQL portability evidence is retained without provider-specific shared queries.
10. Vacancy notice JRXML compiles and representative/long-data PDFs are generated and inspected.
11. Full PrimeHR Playwright regression passes with zero unexplained skips.
12. No applicant, screening, assessment, selection, appointment, onboarding, or Phase 6+ functionality is present.

## 12. Explicit exclusions

- Phase 5B Applicant Portal, applicant identity, privacy consent, PDS/WES, applications, uploads, communications, and status tracking;
- Phase 5C document completeness, QS screening, qualified/disqualified decisions, and screening overrides;
- Phase 5D examinations, interviews, HRMPSB membership/deliberation, scoring, ranking, minutes, or resolution;
- Phase 5E selection, appointment handoff, employee creation, appointment mutation, or onboarding;
- Phase 5F full RSP analytics/evidence reports beyond the operational vacancy notice;
- automatic external posting to CSC, social media, email, SMS, or third-party job boards;
- generic BPMN/workflow designer, RabbitMQ/outbox, document storage, notifications, AI recommendations, or automatic appointment decisions;
- direct reads/writes of another module's database or cross-database Jasper SQL;
- organization-unit data scope until authoritative actor-to-unit responsibility is available.

## 13. Decisions included in this proposal

Approval of this scope accepts these generic, client-neutral decisions:

1. Administrative owns versioned Qualification Standards; PrimeHR snapshots them for RSP history.
2. HRM alone determines actual Plantilla occupancy.
3. Phase 5A initially uses agency-wide RSP roles because narrower authoritative scope is unavailable.
4. Plan approval, authority to fill, publication approval, and publication are separate auditable decisions.
5. Publication channels are configurable records; integration with external posting services is deferred.
6. Applicant/public routes begin only in Phase 5B.
7. Phase 5A is delivered in 5A.1 and gated 5A.2 backend slices, followed by separately approved 5A.3 UI/report/acceptance.

## 14. Approval gate

Recommended approval wording:

> Approve Phase 5A as defined. Proceed with Phase 5A.1, and continue to Phase 5A.2 only after all Phase 5A.1 gates pass. Stop before Phase 5A.3 until I approve the UI, Jasper report, and Playwright acceptance.

Until that approval is received, no tables, migrations, APIs, permissions, Java behavior, or UI pages from Phase 5 are authorized.
