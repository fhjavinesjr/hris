# Phase 5E Selection, Appointment Handoff, and Onboarding Scope Approval

> Implementation status (2026-09-07): Phase 5E.1-5E.4 are implemented and accepted. The valid 49-group SQL Server preflight finding was reviewed and remediated without deleting history, HumanResource V2 applied through Flyway, and all 37 Playwright tests passed. Work remains stopped before Phase 5F pending separate approval.

## 1. Purpose and approval boundary

This document defines Phase 5E only. It is a repository-validated scope proposal, not implementation authorization.

Phase 5E begins with a finalized Phase 5D comparative evaluation and HRMPSB recommendation. It adds an authorized appointing-authority decision, controlled applicant offer/notice handling, an idempotent PrimeHR-to-HumanResource handoff, and a HumanResource-owned onboarding process that may create or link the employee and create the authoritative appointment.

The system may present evidence and enforce policy, but it must never automatically select an appointee. The appointing authority remains the legally responsible human decision-maker.

No Phase 5E code, table, migration, endpoint, permission row, UI, account activation, or Playwright fixture may be implemented until the user approves this scope. Phase 5F reporting remains separately gated.

## 2. Repository findings

### PrimeHR source state

- Phase 5D ends with `EvaluationProceeding.FINALIZED`, an immutable comparative evaluation, and candidate resolutions of `ENDORSED_FOR_SELECTION_DECISION`, `NOT_ENDORSED`, or `DEFERRED`.
- There is no selection aggregate, appointing-authority decision, selected/not-selected state, offer response, appointment handoff, or onboarding entity/API.
- `PositionApplication` currently ends at `QUALIFIED`, `DISQUALIFIED`, or `WITHDRAWN`; applicant-safe selection states do not exist.
- PrimeHR already has provider-specific Flyway locations, `ddl-auto=validate`, optimistic versions, agency scope, audit, secured applicant identity, document storage, and typed Administrative/HRM clients that Phase 5E can extend.

### HumanResource source state

- HumanResource owns `employee`, `personaldata`, and `employeeappointment`.
- Existing `POST /api/employee/register` creates an employee/login row. It is currently permitted without authentication and is called from multiple registration/PDS screens.
- The current PDS screen creates the employee and Personal Data through separate browser requests. Failure of the second request can leave a partial employee.
- Existing appointment creation accepts browser-supplied employee, position, Plantilla, nature, salary, dates, and active state. It has no recruitment-handoff provenance or idempotency key.
- Existing appointment service code catches broad exceptions and may return null, and it does not provide a transactional cross-service receipt.
- The database constraint prevents the same employee/assumption date duplicate, but there is no provider-enforced guarantee of only one active appointment per employee or only one active occupant per Plantilla.
- HumanResource uses `ddl-auto=update`; it has PostgreSQL and SQL Server drivers but no Flyway migration history. This must be addressed before adding Phase 5E-owned tables.
- There is no onboarding case, versioned checklist, pre-employment workflow, appointment-intake contract, one-time employee-account activation, or recruitment handoff receipt.
- The existing Personnel Action PDF is an HRM operational report. Phase 5E does not require a new Jasper report.

### Administrative and UI source state

- Administrative owns permissions and authoritative Job Position, Plantilla, Nature of Appointment, salary schedule, organization, and technical settings.
- `hrm.employmentRecord` is the existing broad UI permission. It is not sufficient authority for appointing-authority selection or an integration mutation.
- Current HRM appointment UI checks Plantilla occupancy client-side and calculates a daily value in the browser. Phase 5E must resolve authoritative appointment values server-side and must not trust those browser calculations.
- No authoritative supervisor/appointing-authority hierarchy exists. Phase 5E must use an exact Administrative permission plus process assignment, not a hard-coded job title.
- No onboarding UI exists in PrimeHR, HRM, Careers, or Employee Portal.

## 3. Ownership and integration decisions

| Concern | Owner | Phase 5E rule |
|---|---|---|
| final comparative evaluation and HRMPSB recommendation | PrimeHR | immutable Phase 5D source; never rewritten by selection |
| appointing-authority selection and offer/notice | PrimeHR | new versioned selection aggregate and applicant-safe view |
| applicant identity/application evidence | PrimeHR | referenced through immutable snapshots; applicant password is never copied |
| appointment-handoff request and delivery history | PrimeHR | durable, retryable, auditable handoff aggregate |
| handoff receipt, onboarding, employee/PDS and appointment | HumanResource | new idempotent integration boundary and HRM-owned workflow |
| Job Position, Plantilla, Nature of Appointment, salary schedule and organization | Administrative | authoritative typed reads with source fingerprint/effectivity |
| permission policy | Administrative | exact independent feature/action/data-scope rows |
| employee credentials | HumanResource | one-time activation; never a PrimeHR/applicant credential reuse |
| formal RSP analytics/evidence reports | Phase 5F | excluded |

PrimeHR must not write HumanResource, Administrative, Timekeeping, Payroll, or Employee Portal tables directly. HumanResource must not read PrimeHR tables directly. The first implementation remains synchronous REST with explicit receipt/retry/reconciliation state; it does not introduce a general message broker or BPM engine.

Mutating service-to-service calls require a configurable service identity with an intended HumanResource audience and handoff-write authority. A browser bearer token or an actor header alone is not sufficient service authentication. Actor employee number, source permission, correlation ID, handoff ID, and payload fingerprint remain audit metadata.

## 4. Phase 5E.1 — appointing-authority selection and offer

Phase 5E.1 is PrimeHR backend only.

### Selection source and eligibility

A selection case is created only from a current finalized Phase 5D proceeding. It freezes:

- publication/vacancy/Plantilla identifiers and snapshots;
- finalized proceeding, meeting, comparative-evaluation and resolution identifiers/versions/fingerprints;
- every eligible evaluated candidate and final rank/tie/exclusion/recommendation evidence;
- application/applicant identifiers and safe contact snapshot;
- policy, committee, appointing-authority assignment and capture timestamps.

The selected candidate must belong to that finalized candidate set, remain qualified and non-withdrawn, and not be excluded by a mandatory gate. Selecting a person outside the evaluated candidate set is rejected and requires a corrected/reopened lawful recruitment process.

Selecting a candidate who was not HRMPSB-endorsed is allowed only when agency policy permits it and the appointing authority supplies an explicit legal/policy basis and reason. The system records the variance; it does not infer or approve it.

Exactly one of these outcomes is allowed per current selection case:

- `SELECTED` — exactly one eligible candidate;
- `NO_SELECTION` — no appointment will proceed, with a required reason;
- `DEFERRED` — no final decision yet, with review date/reason.

### Lifecycle and separation of duties

Proposed lifecycle:

```text
DRAFT → SUBMITTED → RETURNED → SUBMITTED
                  ↘ APPROVED → FINALIZED
DRAFT/SUBMITTED/RETURNED → CANCELLED
FINALIZED → controlled successor correction only
```

- Add/Edit prepares the evidence-backed draft.
- Submit freezes the proposed outcome for review.
- Approve/Return belongs to the assigned appointing authority or an explicitly authorized administrator.
- Finalize releases the immutable decision and applicant-safe notices; it cannot change the approved selected candidate.
- The submitter cannot approve their own proposal. Administrator exception requires explicit reason and audit; it cannot select an ineligible candidate.
- A finalized correction creates a successor linked to the prior case and superseding notices. It never edits or deletes the original decision.
- One current non-cancelled selection case is allowed per vacancy publication.

### Offer and applicant-safe notice

After finalization, the selected applicant receives an in-app offer/selection notice with agency-configured response deadline and safe instructions. The applicant may `ACCEPT` or `DECLINE`; the response is versioned, timestamped, idempotent, and limited to the owning applicant.

Only an accepted offer may become ready for appointment handoff. A declined or expired offer preserves history and requires an authorized successor selection before another candidate can be offered the position.

Non-selected applicants receive only approved safe status/text. They do not receive scores, ranks, other candidates, panel identities/ratings, conflicts, reference notes, minutes, confidential reasons, or appointment data. No external email/SMS is sent in this phase.

### PrimeHR V21 migration

Paired PostgreSQL and SQL Server `V21__rsp_selection_decision_offer.sql` migrations are proposed for:

- `rsp_selection_case`;
- `rsp_selection_candidate_decision`;
- `rsp_selection_notice`;
- `rsp_offer_response`.

Constraints/indexes must enforce current-case uniqueness, one selected candidate, source ownership, successor lineage, notice uniqueness, valid states/timestamps, and applicant response idempotency. Provider-specific filtered/partial indexes are isolated in the paired scripts.

## 5. Phase 5E.2 — durable appointment handoff and HRM receipt

Phase 5E.2 begins only after every Phase 5E.1 gate passes. It creates transport and receipt state only; it does not yet create or mutate an employee or appointment.

### PrimeHR handoff

A handoff is created only from a current finalized `SELECTED` case with an accepted offer. Its immutable payload contains the minimum required facts:

- handoff ID/idempotency key, selection ID/version/fingerprint and correlation ID;
- selected applicant/application identifiers and verified name/contact snapshot;
- publication, vacancy, Plantilla, Job Position and organizational source identifiers/snapshots;
- HRMPSB/appointing-authority evidence references, not unrestricted confidential content;
- applicant consent/offer acceptance evidence;
- intended existing-employee versus new-employee handling remains undecided until HRM review.

Proposed handoff lifecycle:

```text
DRAFT → READY → SENT → ACKNOWLEDGED
                  ↘ RETRYABLE_FAILURE
READY/RETRYABLE_FAILURE → CANCELLED (before HRM acceptance only)
ACKNOWLEDGED → CLOSED after HRM completion acknowledgment
```

Every delivery attempt records attempt number, timestamp, endpoint identity, HTTP/result category, safe diagnostic, and payload fingerprint. Retrying uses the same handoff ID and payload. A changed payload requires a new handoff revision, never reuse of an idempotency key.

### HumanResource receipt

HumanResource exposes a service-authenticated integration endpoint, conceptually:

```text
POST /api/integration/v1/primehr/appointment-handoffs
GET  /api/integration/v1/primehr/appointment-handoffs/{handoffId}
```

The POST atomically creates or returns the existing receipt for the handoff ID. The same ID plus same fingerprint returns the prior result; the same ID plus a different fingerprint returns conflict. It creates no employee or appointment.

HRM verifies service identity, schema version, agency, selection/offer state, required snapshots, and duplicate applicant/selection/handoff relationships. It returns a stable receipt ID/status/version and never exposes unrelated HRM records.

### Migrations

PrimeHR paired `V22__rsp_appointment_handoff.sql` migrations are proposed for:

- `rsp_appointment_handoff`;
- `rsp_appointment_handoff_attempt`.

HumanResource must first establish a reviewed provider-specific Flyway boundary for new integration-owned tables. Because the existing populated HRM schema is not Flyway-managed, the proposed approach is:

- add Flyway to HumanResource;
- use provider-specific PostgreSQL/SQL Server locations;
- baseline existing databases at version `0` after a preflight schema/data audit;
- start with paired `V1__primehr_appointment_handoff_receipt.sql`;
- migrate new Phase 5E tables explicitly;
- do not claim that the baseline retrospectively manages or validates every legacy table;
- prove startup does not silently alter legacy schema before changing production `ddl-auto` behavior.

`V1` creates `rsp_appointment_handoff_receipt` with unique agency/handoff ID, source fingerprint, receipt state, selection/application references, timestamps, versions, and audit metadata.

## 6. Phase 5E.3 — HRM intake, onboarding, employee and appointment activation

Phase 5E.3 begins only after every Phase 5E.2 gate passes.

### HRM onboarding configuration and case

HumanResource owns versioned onboarding checklist templates because they define an HRM operational workflow, not an Administrative reference master. Administrative remains the permission authority and source of technical settings such as default response/expiry durations.

Template definitions are draft/published/superseded and effective-dated. Items contain code, label, instructions, required/optional flag, evidence requirement/classification/retention tag, responsible role, order, and completion rule. No agency-specific medical, clearance, oath, tax, benefits, or document list is hard-coded.

An acknowledged handoff creates one onboarding case with an immutable template snapshot. Proposed lifecycle:

```text
RECEIVED → IN_REVIEW → RETURNED → IN_REVIEW
                     ↘ READY_FOR_APPOINTMENT
READY_FOR_APPOINTMENT → APPOINTMENT_CREATED → COMPLETED
RECEIVED/IN_REVIEW/RETURNED → CANCELLED
```

Required checklist items and evidence must be independently verified before `READY_FOR_APPOINTMENT`. Evidence uses secured storage metadata and per-download authorization. A return/cancellation requires a reason and never deletes the source selection or evidence.

### Existing versus new employee

HRM performs an explicit human-controlled identity decision:

- `LINK_EXISTING_EMPLOYEE` for a verified internal/existing employee; or
- `CREATE_NEW_EMPLOYEE` for a person who does not already have an employee record.

The system must not auto-match solely by name, email, birth date, applicant ID, or biometric number. An existing link requires the exact employee ID/number and authorized confirmation. A new employee requires HRM-assigned unique employee number, biometric number, non-administrator role, and reviewed identity fields.

Applicant credentials/password hashes are never copied. Administrator role `"1"` cannot be assigned by the handoff unless a separately authorized Administrative user-management action explicitly permits it.

Applicant profile/application snapshots remain recruitment evidence. They do not become the authoritative PDS automatically. PDS creation/completion continues through an authorized HRM workflow and is tracked as an onboarding requirement.

### Appointment creation

The final HRM command atomically:

1. locks the handoff receipt/onboarding case and relevant employee/Plantilla appointment rows;
2. re-resolves the authoritative Administrative Job Position, Plantilla, Nature of Appointment, organization and salary schedule for the issued/assumption dates;
3. rechecks current HRM Plantilla occupancy and source fingerprints;
4. creates or links the employee;
5. creates the later appointment, deactivating the prior active appointment only within the same successful transaction;
6. records recruitment provenance and marks the receipt/case `APPOINTMENT_CREATED`;
7. returns stable employee/appointment IDs to PrimeHR reconciliation.

The browser cannot supply authoritative salary or active-state values. Salary Grade/Step and compensation come from Administrative sources. The Administrative salary-schedule daily equivalent remains `monthly × 12 ÷ 365`; the existing HRM browser `monthly ÷ 22` calculation is not accepted for this integration contract.

Rules include:

- one active appointment per employee;
- one active occupant per Plantilla;
- a later appointment deactivates the prior active appointment atomically;
- the same employee/assumption date updates the controlled intake attempt rather than creating duplicate history;
- appointment issued/assumption dates pass chronological validation;
- an occupied/stale/changed Plantilla or source fails without partial employee/appointment creation;
- historical appointments are never deleted;
- repeated, concurrent or response-lost requests return the same employee/appointment result.

### Employee account activation

For a newly created employee, HRM creates an unusable random credential placeholder plus a separate one-time activation invitation containing only a hashed token, expiry, consumed timestamp and employee reference. The plain token is never stored or logged. Activation sets the initial employee password and consumes the token atomically.

An existing employee keeps the existing account and receives no replacement credential. Applicant and employee sessions remain separate. Completing onboarding never copies the applicant password or automatically grants permissions.

### HumanResource V2 migration

Paired `V2__onboarding_employee_appointment_activation.sql` migrations are proposed for:

- `hrm_onboarding_template`;
- `hrm_onboarding_template_item`;
- `hrm_onboarding_case`;
- `hrm_onboarding_item`;
- `hrm_appointment_provenance`;
- `employee_activation_invitation`.

Provider-specific filtered/partial indexes enforce one current published template chain, one active onboarding case per handoff, one active appointment per employee, and one active occupant per Plantilla. Migration preflight must report existing duplicate active rows and stop for reviewed data correction; it must not silently deactivate or delete history.

## 7. Phase 5E.4 — Administrative controls, UI, and Playwright

Phase 5E.4 requires separate approval after Phase 5E.1–5E.3 backend gates pass.

### Administrative permission rows

All new operational permissions initially require `AGENCY_WIDE` because authoritative organization-scoped appointing authority is not modeled. Process assignment further narrows each record.

| Feature key | Actions |
|---|---|
| `primehr.rsp-selection-decision` | Access, Add, Edit, Submit, Approve, Finalize, Data Scope |
| `primehr.rsp-appointment-handoff` | Access, Add, Edit, Submit, Finalize, Data Scope |
| `hrm.appointment-intake` | Access, Add, Edit, Approve, Finalize, Data Scope |
| `hrm.onboarding-configuration` | Access, Add, Edit, Publish, Data Scope |
| `hrm.onboarding` | Access, Add, Edit, Submit, Approve, Finalize, Data Scope |

No new feature exposes Delete. Hiding a link is not authorization. PrimeHR and HumanResource controllers/services enforce the exact action, data scope, process assignment, state and separation-of-duties rule.

`hrm.employmentRecord` remains the normal employment-record permission and does not implicitly grant appointment-intake/onboarding authority. Conversely, intake authority does not grant unrestricted editing/deletion of all employment records.

### UI surfaces

- PrimeHR staff: selection-case evidence, proposed outcome, variance basis, submit/return/approve/finalize, notices, offer response, handoff status/retry/reconciliation.
- Careers: own selected/not-selected/deferred safe notice, offer acceptance/decline and safe onboarding progress only.
- HRM staff: handoff inbox, identity link/create decision, checklist/evidence verification, authoritative appointment preview, atomic create action, activation status and audit history.
- HRM configuration: versioned/effective onboarding checklist templates.
- Employee activation: one-time password setup without exposing applicant or employee data beyond the invitation's purpose.
- Existing Personnel Action PDF may be opened only after appointment creation using its existing HRM authorization. No new Phase 5E Jasper template is required.

Legacy public employee registration pages/endpoints must be removed, disabled, or protected before Phase 5E acceptance. `/api/hris/installAuth` must also be production-disabled or strongly one-time protected. Equivalent legacy employee/appointment mutations must receive backend permission enforcement so they cannot bypass the new controlled intake.

## 8. Proposed API families

Exact DTOs remain subject to implementation review, but the contract families are:

```text
Employee staff / PrimeHR
POST /api/primehr/v1/rsp/selection-cases
GET  /api/primehr/v1/rsp/selection-cases
GET  /api/primehr/v1/rsp/selection-cases/{id}
PUT  /api/primehr/v1/rsp/selection-cases/{id}
POST /api/primehr/v1/rsp/selection-cases/{id}/submit
POST /api/primehr/v1/rsp/selection-cases/{id}/return
POST /api/primehr/v1/rsp/selection-cases/{id}/approve
POST /api/primehr/v1/rsp/selection-cases/{id}/finalize
POST /api/primehr/v1/rsp/selection-cases/{id}/successors
POST /api/primehr/v1/rsp/selection-cases/{id}/appointment-handoffs
POST /api/primehr/v1/rsp/appointment-handoffs/{id}/submit
POST /api/primehr/v1/rsp/appointment-handoffs/{id}/retry
GET  /api/primehr/v1/rsp/appointment-handoffs/{id}

Applicant / Careers
GET  /api/primehr/applicant/v1/me/applications/{applicationId}/selection
POST /api/primehr/applicant/v1/me/applications/{applicationId}/offer-response

Service integration / HumanResource
POST /api/integration/v1/primehr/appointment-handoffs
GET  /api/integration/v1/primehr/appointment-handoffs/{handoffId}

HRM staff
GET  /api/hrm/v1/appointment-intakes
GET  /api/hrm/v1/appointment-intakes/{id}
POST /api/hrm/v1/appointment-intakes/{id}/begin-review
POST /api/hrm/v1/appointment-intakes/{id}/return
PUT  /api/hrm/v1/appointment-intakes/{id}/identity-resolution
PUT  /api/hrm/v1/appointment-intakes/{id}/checklist/{itemId}
POST /api/hrm/v1/appointment-intakes/{id}/approve
POST /api/hrm/v1/appointment-intakes/{id}/create-appointment
POST /api/hrm/v1/appointment-intakes/{id}/complete-onboarding
```

Commands include `recordVersion`; repeatable cross-service commands also include idempotency key and payload fingerprint. Responses return stable IDs, status, record version, source versions and safe errors.

## 9. Security, privacy, and audit requirements

- Employee and applicant JWT boundaries remain separate.
- Service identity is distinct from the initiating employee identity.
- Appointing authority is an assigned, currently eligible employee with exact Approve authority; no hard-coded title grants power.
- Submitter/approver separation, administrator exception reason, and finalizer state restrictions are backend enforced.
- Every selection, variance, return, approval, finalization, notice, offer response, handoff attempt, receipt, checklist verification, identity resolution, employee creation, appointment creation, activation and correction is audited.
- Audit stores IDs/fingerprints and safe summaries, not passwords, activation tokens, JWTs, full PDS, or unrestricted applicant documents.
- Applicant ownership is derived from the authenticated applicant account, never a browser-supplied applicant ID.
- Confidential Phase 5D evaluation evidence is not copied into applicant notices or the HRM handoff unless explicitly required and authorized.
- Activation tokens are high-entropy, hashed, expiring, single-use and rate-limited.
- File evidence follows existing DocumentStorage validation, classification, retention and download authorization.

## 10. Database and portability gates

PrimeHR continues paired SQL Server/PostgreSQL Flyway migrations with `ddl-auto=validate`.

HumanResource migration introduction is a blocking Phase 5E.2 prerequisite. Acceptance requires:

- reviewed baseline-at-zero behavior against a populated SQL Server HRM database;
- equivalent PostgreSQL scripts and PostgreSQL-mode Flyway/Hibernate tests;
- no implicit legacy-table DDL in production startup;
- fresh and populated V1/V2 migration tests;
- duplicate-active-appointment/Plantilla preflight and explicit failure;
- sequence/identity, boolean, timestamp, index and unique-null semantics verified for both providers;
- provider-neutral JPA/service logic and no shared native vendor SQL.

Live PostgreSQL remains non-blocking only if the user retains the current policy; the non-run must be stated explicitly. SQL Server fresh, populated-upgrade and browser acceptance remain blocking.

## 11. Acceptance gates by slice

### Phase 5E.1 gates

- finalized/current Phase 5D source only;
- eligible candidate-set ownership and one-current-case constraints;
- selected, no-selection, deferred and outside-recommendation-with-basis cases;
- submit/return/approve/finalize separation and administrator exception audit;
- immutable finalization and successor correction;
- applicant ownership, offer accept/decline/expiry and notice confidentiality;
- V21 dual migration/parity, OpenAPI, focused/full backend package and Phase 5E.2 boundary audit.

### Phase 5E.2 gates

- Phase 5E.1 gates all pass first;
- accepted offer required;
- stable payload schema/fingerprint and service authentication;
- same-key/same-payload idempotency, changed-payload conflict, retry and response-loss recovery;
- HRM V1 baseline/fresh/populated provider gates;
- no employee, PDS, appointment, activation or onboarding-completion mutation;
- cross-service timeout, unavailable, unauthorized and rollback/reconciliation tests.

### Phase 5E.3 gates

- Phase 5E.2 gates all pass first;
- versioned/effective checklist and immutable instance snapshot;
- required evidence and independent verification;
- explicit existing/new employee identity decision and duplicate protection;
- no applicant credential/PDS automatic copy;
- authoritative Administrative source/effectivity/fingerprint resolution;
- chronological dates, one active employee appointment and one active Plantilla occupant;
- atomic employee/appointment/provenance/case mutation and response-loss idempotency;
- internal promotion preserves history; external hire activation is single-use/expiring;
- stale/occupied/dependency/storage/activation failures leave no partial employee or appointment;
- HRM V2 dual migration and populated duplicate-preflight tests;
- affected Common, Administrative, HumanResource and PrimeHR clean package gates.

### Phase 5E.4 gates

- separate user approval after Phase 5E.1–5E.3 pass;
- exact Administrative rows and fail-closed frontend helpers;
- PrimeHR, Careers and HRM UI allowed/denied/empty/error/stale states;
- real SQL Server Playwright covering selection, variance, notices, decline/successor, handoff retry/idempotency, onboarding, internal/external employee handling, atomic appointment, activation and confidentiality;
- full existing Playwright regression with no unexplained skips;
- Administrative, PrimeHR and HRM strict type-check/lint/production builds;
- user/operator/E2E guides and final Phase 5E review manifest.

## 12. Explicit exclusions

- automatic or AI-assisted selection, opaque ranking, auto-approval, or selection outside the finalized evaluated candidate set;
- direct database writes or cross-database joins between PrimeHR, HumanResource and Administrative;
- copying applicant passwords, automatically treating applicant profile data as authoritative PDS, or automatically granting employee permissions;
- automatic employee number, biometric number, administrator role, appointment nature, salary, signatory, issued date or assumption date without an approved authoritative rule;
- destructive replacement/deletion of selection, offer, notice, handoff, onboarding, employee or appointment history;
- automatic payroll enrollment, leave balances, timekeeping schedule, biometric-device registration, benefits enrollment, property issuance, email/SMS, or external government submission;
- generalized workflow/BPM, message broker/event bus, enterprise identity merge, background-check provider, document signing, or external calendar integration;
- new appointment/Oath of Office/assumption-to-duty Jasper templates, RSP register, comparative report, evidence index, analytics or dashboard; these require a separate report scope, with Phase 5F owning RSP reporting;
- Phase 5F or later behavior.

## 13. Known risks and decisions accepted by approval

1. HumanResource migration governance is the largest prerequisite. Phase 5E.2 cannot begin until baseline-at-zero behavior is proven safely against the populated schema.
2. Existing public employee registration and authenticated-but-broad mutation endpoints are privilege-bypass paths. They must be hardened before Phase 5E browser acceptance.
3. Current employee identity and login are one table. Phase 5E uses a one-time activation record rather than copying applicant credentials; broader identity redesign is out of scope.
4. Applicant profile evidence is not a complete authoritative government PDS. HRM staff/employee must complete and verify PDS through the HRM workflow.
5. No authoritative appointing-authority hierarchy exists. Exact Administrative permission plus resource assignment is the approved initial authority model.
6. Internal applicants require explicit existing-employee linkage; automatic matching is intentionally rejected.
7. Agency onboarding requirements and deadlines vary. Versioned configuration and Administrative technical settings replace hard-coded lists/durations.
8. Synchronous handoff cannot be one distributed database transaction. Durable state, idempotency, retry and reconciliation provide safe recovery.
9. Downstream Timekeeping/Payroll visibility may change naturally after an active HRM appointment, but Phase 5E creates no direct records in those modules.
10. Live PostgreSQL evidence remains truthful about any non-run; paired migrations and provider-neutral code remain mandatory.

## 14. Approval gate

Recommended approval wording:

> Approve Phase 5E as defined. Proceed with Phase 5E.1, and continue to Phase 5E.2 only after all Phase 5E.1 gates pass. Continue to Phase 5E.3 only after all Phase 5E.2 gates pass. Stop before Phase 5E.4 until I approve the Administrative controls, PrimeHR/Careers/HRM UI, employee activation UI, and Playwright acceptance. Stop before Phase 5F.

No Phase 5E implementation may start before explicit approval. Phase 5E.4 and Phase 5F remain separately gated.
