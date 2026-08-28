# ISOFT PRIME-HRM Phase 3 - Competency Assessment and Person Profiles Approval Scope

Status: Complete; Phase 3.1 through Phase 3.4 implemented and accepted, Phase 4 not started

Prepared: 2026-08-26

Depends on: completed and committed Master Plan Phase 1 competency foundation and Phase 2 Position Competency Profiles

## 1. Objective

Implement controlled employee competency assessment cycles, tools, subjects, explicitly assigned assessors, ratings, structured evidence, human validation, and immutable person competency profiles. The result must preserve every assessment contribution and identify the latest valid profile without rewriting history.

This scope implements only Master Plan V2 Phase 3. It does not calculate competency gaps, create L&D referrals, assess applicants, implement RSP/SPMS/L&D/R&R, upload evidence files, or create a generic workflow engine.

## 2. Repository and architecture facts used

- `PrimeHR` is a standalone-first Spring Boot module with its own datasource, Flyway V1-V5 migrations, JPA persistence, Administrative authorization integration, append-only audit, and Phase 1/2 versioning patterns.
- `prime-hr-software` is the standalone SSO-enabled PrimeHR UI. Employee Portal already launches it, so Phase 3 does not need a second portal application.
- Administrative owns SSO, permission rules, Job Position/Plantilla masters, and system configuration.
- HumanResource owns employee identity, employment master data, appointments, and separation history.
- Existing `/api/employees/basicInfo` is not an adequate Phase 3 integration contract: it is an unpaged general endpoint and does not provide a controlled active-appointment snapshot.
- `EmployeeAppointment` contains employee, Job Position, Plantilla, appointment dates, and active status. PrimeHR must read these through a typed HRM API, never through shared tables or cross-database joins.
- No verified authoritative employee-to-supervisor relationship exists. `ManagePersonnel.head` and request-specific `ApprovalWorkflow` records do not prove a direct supervisor relationship.
- Current PrimeHR permissions cover CRUD, Publish, Submit, and Approve. Phase 3 requires distinct Assess, Validate, Finalize, and data-scope semantics rather than overloading existing actions.
- No approved secure document-storage abstraction or retention policy exists.
- Phase 2 is committed in backend commit `51922ea` and PrimeHR frontend commit `a8f34eb`; both repositories were clean when this scope was prepared. Administrative retains an unrelated local `.env` edit.

## 3. Decisions proposed for approval

Approval of this scope accepts all of the following:

1. **Employees only in Phase 3.** Applicant competency assessments remain in the RSP phase under a separate applicant identity boundary.
2. **Explicit assessor assignment.** `IMMEDIATE_SUPERVISOR` is a supported assessment method, but an authorized HR assessment administrator must explicitly assign the employee who will act as supervisor-assessor. The system will not infer supervisors from `ManagePersonnel` or approval workflows.
3. **Structured evidence only.** Phase 3 stores evidence type, title/reference, observation date, and remarks. Binary uploads, external object storage, antivirus/scanning, and retention automation remain excluded until storage governance is approved.
4. **Human validation determines the official result.** The system shows submitted contributor ratings but does not average them or automatically decide an attained level. An authorized validator selects/confirms the final level per competency and records validation remarks.
5. **Self-assessment remains distinguishable.** Self ratings are retained as `SELF_ASSESSMENT` contributions and never become an official person profile without independent validation.
6. **Immutable generated profiles.** Validation atomically creates a complete person-profile version. It is not directly editable; later validated assessments create successors while retaining prior versions.
7. **Confidentiality by relationship and scope.** Subjects see their own self-assessment and validated profile; assessors see only explicit assignments; validators see submitted cases within their configured scope; authorized HR profile viewers see only allowed validated profiles; administrators retain audited override behavior.
8. **SQL Server-primary verification.** Real SQL Server is the blocking provider gate, following the user's standing direction. Equivalent PostgreSQL migrations, provider-neutral application code, parity tests, and PostgreSQL-mode tests remain mandatory; a live PostgreSQL run is recorded as unverified unless separately requested.
9. **Standalone PrimeHR remains the deployment boundary.** No HRISApp assembly or shared-database dependency is added in Phase 3.

## 4. Domain model

### 4.1 Assessment cycle

`AssessmentCycle` defines a controlled assessment period:

- ID, agency scope, unique code, name, description;
- reference/effective dates and optional reassessment-validity rule;
- status `DRAFT`, `OPEN`, `CLOSED`, or `ARCHIVED`;
- optimistic record version;
- created/updated/opened/closed metadata and audit.

Lifecycle:

```text
DRAFT -> OPEN -> CLOSED
    -> ARCHIVED (only before opening)
```

An OPEN cycle is immutable except for controlled assessor assignment corrections that have not received ratings. Closing prevents new submissions but does not destroy history.

### 4.2 Assessment tool

An `AssessmentTool` belongs to a cycle and references one exact ACTIVE Phase 2 Position Profile version. It contains:

- name/instructions;
- exact position-profile ID and definition version;
- immutable target/requirement snapshot;
- one or more required assessment methods;
- whether structured evidence is required per method/requirement;
- status `DRAFT`, `PUBLISHED`, or `ARCHIVED`;
- optimistic version and audit.

Supported method codes are explicit domain values:

- `SELF_ASSESSMENT`
- `IMMEDIATE_SUPERVISOR`
- `AUTHORIZED_ASSESSOR`
- `PANEL`
- `BEHAVIORAL_EVENT_INTERVIEW`
- `WRITTEN_PRACTICAL`
- `VALIDATED_PRIOR_EVIDENCE`

These codes identify the method; agency-facing labels/instructions remain configurable in the tool.

### 4.3 Assessment case and subject

An `AssessmentCase` represents one employee subject assessed with one tool in one cycle. It stores:

- HRM employee ID and employee number;
- immutable minimal employee/name snapshot;
- current appointment ID, Job Position ID, Plantilla ID, assumption date, and source fingerprint captured at assignment;
- exact resolved Position Profile version;
- status and optimistic version;
- submission/validation metadata and full transition audit.

The subject must be an active employee with a verifiable current appointment. The effective Phase 2 profile is resolved using Plantilla precedence followed by Job Position fallback. Assignment fails closed if HRM or Administrative facts cannot be verified.

### 4.4 Assessor assignment

Each case has one or more `AssessorAssignment` records:

- assessment method;
- assessor employee ID/number and minimal immutable name snapshot;
- assignment status;
- assigned-by/at, submitted-by/at, and optimistic version;
- explicit assignment reason when method is supervisor or panel.

The subject is the assessor only for `SELF_ASSESSMENT`. Other methods reject subject/assessor identity equality. Duplicate assessor/method assignments are rejected.

Assignment lifecycle:

```text
ASSIGNED -> IN_PROGRESS -> SUBMITTED
                       -> RETURNED -> IN_PROGRESS -> SUBMITTED
SUBMITTED -> VALIDATED (through case validation)
```

### 4.5 Ratings and evidence

Each assessor rates the exact competency versions contained in the tool snapshot:

- exact competency definition/version ID;
- selected attained proficiency level ID from the exact published scale;
- remarks;
- observable-behavior notes;
- structured evidence entries;
- record version and audit.

Structured evidence contains no binary content:

- evidence type;
- title or official reference number;
- observation/evidence date;
- description/remarks;
- optional source-system/reference ID;
- actor and timestamp.

Mandatory tool requirements require a rating. A tool can require evidence or remarks. A rating cannot use a level from another scale/version.

### 4.6 Validation and official result

Validation begins only after every required assessor assignment is submitted. The validator sees each contributor separately and records a human decision per competency:

- final attained level;
- validation remarks/reason;
- contributing assessment assignment IDs;
- decision actor/time.

The validator may return the case with a mandatory reason. Ordinary validators cannot validate a case in which they submitted a rating. An administrator may override only with an explicit audited reason.

Case lifecycle:

```text
ASSIGNED/IN_PROGRESS -> FOR_VALIDATION -> RETURNED
                                      -> VALIDATED
```

### 4.7 Person competency profile

Successful validation atomically creates `PersonCompetencyProfileVersion` and its result rows:

- employee/appointment source IDs and immutable snapshot;
- exact assessment cycle/tool/case and Position Profile versions;
- profile version number and predecessor link;
- valid-from, optional valid-to/reassessment date;
- exact competency and attained-level IDs;
- source method summary and validation metadata;
- immutable status `VALIDATED` and append-only audit.

The latest valid profile is selected deterministically by employee, validity period, profile version, and validation timestamp. Creating a later validated version closes the prior open validity period atomically when applicable. No API directly edits or deletes a validated profile.

## 5. HRM integration contract

HumanResource adds a versioned, authenticated, read-only contract:

```http
GET /api/integration/v1/primehr/assessment-subjects?search=&page=&size=&activeOnly=true
GET /api/integration/v1/primehr/assessment-subjects/{employeeId}
```

The response contains only:

- employee ID, employee number, and display name;
- employment/separation eligibility needed for assessment;
- current appointment ID and assumption date;
- Job Position ID and Plantilla ID;
- source fingerprint, source-updated value when available, and fetched timestamp.

It must never return password, biometric number, complete PDS, salary, contact details, or unrelated history.

HRM enforces authenticated Administrative permission for `primehr.assessment-administration` before returning employee lists/details. PrimeHR forwards the browser JWT through its typed HRM client. PrimeHR adds configurable HRM base URL and timeouts; no localhost or provider value is hardcoded into shared Java logic.

New contract document:

```text
contracts/openapi/humanresource-primehr-integration-v1.yaml
```

## 6. Permissions and data scope

Administrative adds these feature rows:

| Feature key | Purpose | Relevant actions |
|---|---|---|
| `primehr.assessment-administration` | cycles, tools, subjects, assignments | Access, Add, Edit, Delete/Archive, Publish/Open, Finalize/Close |
| `primehr.competency-assessment` | assigned/self ratings | Access, Assess, Submit |
| `primehr.assessment-validation` | return and validate submitted cases | Access, Validate |
| `primehr.person-profile` | view validated profiles/history | Access |

The permission contract/UI gains additive `canAssess`, `canValidate`, `canFinalize`, and `dataScope` fields. Missing legacy values fail closed. Administrator behavior grants supported actions and `AGENCY_WIDE`, while retaining auditing.

Allowed Phase 3 data scopes:

- `OWN_RECORDS`
- `ASSIGNED_RECORDS`
- `AGENCY_WIDE`

`DIRECT_SUBORDINATES` is deliberately not implemented until an authoritative supervisor contract exists. Frontend controls mirror permissions, but every read and mutation is constrained again in PrimeHR services/repositories.

## 7. REST contract

All endpoints are versioned under `/api/primehr/v1` and use DTOs, validation, pagination, optimistic record versions, consistent errors, and correlation-aware audit.

### Assessment administration

```http
GET/POST              /admin/assessment-cycles
GET/PUT               /admin/assessment-cycles/{cycleId}
POST                  /admin/assessment-cycles/{cycleId}/archive
POST                  /admin/assessment-cycles/{cycleId}/open
POST                  /admin/assessment-cycles/{cycleId}/close

GET/POST              /admin/assessment-cycles/{cycleId}/tools
GET/PUT               /admin/assessment-tools/{toolId}
POST                  /admin/assessment-tools/{toolId}/archive
POST                  /admin/assessment-tools/{toolId}/publish

GET/POST              /admin/assessment-tools/{toolId}/subjects
GET                   /admin/assessment-cases/{caseId}
POST                  /admin/assessment-cases/{caseId}/assessors
PUT                   /admin/assessment-cases/{caseId}/assessors/{assignmentId}
POST                  /admin/assessment-cases/{caseId}/assessors/{assignmentId}/archive
```

No hard DELETE is added.

### Assessor and subject work

```http
GET                   /assessments/mine
GET                   /assessments/{caseId}
PUT                   /assessments/{caseId}/assignments/{assignmentId}/ratings/{competencyVersionId}
POST                  /assessments/{caseId}/assignments/{assignmentId}/evidence
PUT                   /assessments/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}
POST                  /assessments/{caseId}/assignments/{assignmentId}/evidence/{evidenceId}/archive
POST                  /assessments/{caseId}/assignments/{assignmentId}/submit
```

The backend derives actor identity from the authenticated token. It does not accept a browser-supplied assessor or subject identity as authority.

### Validation

```http
GET                   /validation/assessment-cases
GET                   /validation/assessment-cases/{caseId}
POST                  /validation/assessment-cases/{caseId}/return
POST                  /validation/assessment-cases/{caseId}/validate
```

Return and administrator override require reasons. Validation receives explicit final competency decisions and expected record versions.

### Person profiles

```http
GET                   /person-profiles/me
GET                   /person-profiles/me/history
GET                   /person-profiles/employees/{employeeNo}
GET                   /person-profiles/employees/{employeeNo}/history
GET                   /person-profiles/versions/{profileVersionId}
```

`/me` always derives employee number from the token. Employee-number routes require authorized data scope. Raw assessor contributions are not exposed through person-profile endpoints.

The existing `contracts/openapi/primehr-v1.yaml` remains authoritative and is extended at each checkpoint.

## 8. Persistence and portability

Equivalent forward-only migrations are added for both providers:

```text
PostgreSQL: PrimeHR/src/main/resources/db/migration/postgresql/V6-V8
SQL Server: PrimeHR/src/main/resources/db/migration/sqlserver/V6-V8
```

Planned tables:

### V6 - cycle/tool/assignment foundation

- `prime_assessment_cycle`
- `prime_assessment_tool`
- `prime_assessment_tool_method`
- `prime_assessment_case`
- `prime_assessor_assignment`

### V7 - assessment execution

- `prime_assessment_rating`
- `prime_assessment_evidence`

### V8 - validation and person profiles

- `prime_assessment_validation`
- `prime_assessment_validated_rating`
- `prime_person_competency_profile`
- `prime_person_competency_result`

The existing shared audit table records lifecycle/action events.

Migrations must define equivalent primary keys, foreign keys, uniqueness, status checks, lengths, booleans, timestamp/date precision, optimistic versions, and indexes for employee, assessor, status, cycle/tool, effectivity, and latest-profile queries. Shared Java code uses JPA/JPQL/Specifications/Pageable; no SQL Server- or PostgreSQL-specific application query is permitted.

## 9. Validation, history, and concurrency rules

- Agency scope must match across every aggregate/reference.
- Cycle/tool codes are unique within agency and controlled version.
- A published tool references one exact ACTIVE Position Profile version and immutable requirements.
- A subject must resolve from HRM with an eligible current appointment.
- A case uses the exact effective Position Profile for its snapshot date.
- One subject cannot have duplicate cases for the same tool/cycle.
- Assessor and method assignments are unique within a case.
- Non-self methods cannot assign the subject as assessor.
- Ratings use only tool competencies and valid levels from their exact scales.
- Required ratings/evidence must be complete before submission.
- Repeated submissions/validation requests are idempotent or rejected without duplicate rows/audit.
- Stale record versions return 409 and never overwrite current data.
- Submitted contributions are immutable unless returned.
- Validation requires all required contributions and explicit final levels.
- Ordinary validators cannot validate their own contribution.
- Validation, predecessor closure, person-profile generation, and audit are one transaction.
- Historical employee, appointment, tool, position-profile, rating, evidence, validation, and person-profile snapshots remain unchanged when source records later change.
- Dependency/authentication failures fail closed for commands and return actionable errors.

## 10. UI scope

### Administrative UI

- Add the four Phase 3 feature rows.
- Add Assess, Validate, Finalize, and Data Scope controls.
- Preserve old permission JSON and administrator behavior.
- Turning Access off clears all actions/scope; enabling an action requires Access.

### PrimeHR UI

Add navigation/routes:

```text
/prime-hr/assessment-administration
/prime-hr/assessments
/prime-hr/assessment-validation
/prime-hr/person-profiles
```

Capabilities:

- cycle/tool draft management, publication/opening, closure, subject selection, and assessor assignment;
- exact Position Profile/tool snapshot and HRM source freshness;
- own/assigned assessment inbox, rating form, indicators, structured evidence, remarks, completeness, submit/return states;
- validator comparison of contributor ratings and explicit final-level decisions;
- immutable validated profile summary/history and latest-valid indicator;
- clear SELF versus VALIDATED labels;
- audit history, stale conflict handling, loading/empty/denied/dependency states;
- strict TypeScript, SCSS modules, SweetAlert2, accessible labels, and existing SSO/runtime configuration.

Employees may view only their own validated person profile and their own self-assessment contribution. They do not see confidential ratings/remarks from other assessors. Assigned assessors do not see other assessors' contributions. Validators see all submitted contributions only for cases allowed by scope.

No new Employee Portal page is required; the existing PRIME-HRM SSO launch is reused.

## 11. Controlled execution checkpoints

### Phase 3.1 - HRM subject contract, permission/data-scope foundation, and V6 draft model

- typed HRM employee/current-appointment integration and OpenAPI;
- Administrative Assess/Validate/Finalize/Data Scope contract and UI-independent backend mapping;
- PrimeHR clients/guards and domain-specific scope enforcement;
- V6 dual migrations/entities/repositories;
- DRAFT cycle/tool/subject/assessor administration API only;
- focused authorization, contract, migration, repository, and domain tests.

Gate: do not start 3.2 until the HRM contract exposes no excess PDS/password data, direct unauthorized calls fail, SQL Server fresh/V5-to-V6 upgrades pass, PostgreSQL parity passes, affected Maven tests/package pass, and no rating/profile behavior exists.

### Phase 3.2 - Assessment execution and structured evidence

- V7 migrations;
- tool publication, cycle open/close, assignment inbox;
- rating/evidence save, completeness, submit, return, resubmit;
- exact method/subject/assessor visibility and optimistic conflicts;
- audit and OpenAPI updates;
- backend tests and SQL Server fresh/upgrade gates.

Gate: do not start 3.3 until unauthorized cross-subject reads/writes, duplicate submission, invalid levels, missing required evidence, self/non-self identity violations, stale updates, and illegal transitions are proven blocked without partial data.

### Phase 3.3 - Human validation and immutable person profiles

- V8 migrations;
- validation decision UI-independent API/service;
- administrator override with reason;
- atomic profile generation/predecessor closure;
- latest-valid and history reads with ownership/data scope;
- OpenAPI, domain, authorization, concurrency, migration, and integration tests.

Gate: do not start 3.4 until self-assessment cannot masquerade as validated, ordinary self-validation is blocked, history remains immutable, latest-valid selection is deterministic, and SQL Server fresh/V7-to-V8 plus affected package gates pass.

### Phase 3.4 - Administrative and PrimeHR UI plus Playwright acceptance

- Administrative permission controls;
- all four PrimeHR routes and responsive UI states;
- extend the repeatable Playwright harness with controlled Phase 3 fixtures;
- manual and automated allowed/denied/own/assigned/validator/admin/conflict/history acceptance;
- user guide, test runbook, review manifest, secret/generated/phase-boundary audits.

Gate: Phase 3 is complete only after lint, strict type-check, production builds, repeatable Playwright, SQL Server browser acceptance, documentation, and final repository audit pass with zero hidden skips/failures.

## 12. Test and acceptance matrix

Required automated coverage includes:

- legacy permission JSON fails closed for new actions/scope;
- administrator mapping remains compatible and audited;
- HRM list/detail contract excludes credentials/PDS and enforces authorization;
- subject/appointment/position snapshot and Plantilla precedence are exact;
- explicit supervisor assignment is required; no inferred hierarchy access;
- subjects can access only own records;
- assessors can access only explicit assignments;
- validators cannot access out-of-scope/draft cases or validate own contributions;
- incomplete ratings/evidence cannot submit;
- return reason and administrator override reason are mandatory;
- duplicate/stale submissions and validations do not duplicate results/audit;
- exact scale-level membership is enforced;
- each contributor remains distinguishable, especially self-assessment;
- validated final levels are human-selected, not automatically averaged;
- validation atomically creates exactly one immutable profile version;
- failed validation creates no profile/partial audit;
- prior profiles remain queryable and latest-valid selection is correct by date;
- source master changes do not rewrite snapshots;
- SQL Server fresh and populated upgrades pass at every migration checkpoint;
- PostgreSQL/SQL Server migrations remain structurally equivalent;
- existing Phase 1/2 APIs/tests and Playwright suite remain green;
- no Phase 4 gap or L&D referral behavior appears.

## 13. Expected repository impact

### `hris`

- `HumanResource/.../integration/primehr/**` for the minimal employee/appointment contract and authorization;
- `Administrative` permission DTO/service tests for new actions/data scope;
- `PrimeHR/.../assessment/{api,application,domain,infrastructure}/**`;
- `PrimeHR/.../personprofile/{api,application,domain,infrastructure}/**`;
- additive PrimeHR security/integration/audit/exception updates;
- dual V6-V8 migrations and provider tests;
- `contracts/openapi/primehr-v1.yaml` and new HRM integration OpenAPI;
- Phase 3 detail/review/progress documentation.

### `administrative-software`

- `src/app/administrative/permission/Permission.tsx` only as required for Phase 3 permissions/data scope.

### `prime-hr-software`

- typed assessment/person-profile clients and authorization helpers;
- four Phase 3 routes/components/SCSS/navigation updates;
- Playwright support/spec additions and documentation.

### Explicitly untouched

- Employee Portal UI behavior beyond the existing SSO launch;
- HRM employment/PDS/appointment behavior and HRM UI;
- Timekeeping, Payroll, Jasper reports, and their UIs;
- HRISApp combined datasource/runtime;
- deployed Vercel/Render branches and user environment files.

## 14. Explicit exclusions

- competency gap calculation, development priorities, IDP, and L&D referral (Phase 4);
- applicant subjects/identity and recruitment assessment (Phase 5 RSP);
- performance/SPMS, L&D program management, R&R, succession, Evidence Center, and analytics;
- automatic supervisor inference or `DIRECT_SUBORDINATES` data scope;
- automatic averaging, scoring, ranking, or legal/HR decision replacement;
- binary evidence uploads, object storage, document scanning, signatures, and retention automation;
- email/in-app notifications, broker, outbox, and scheduled escalation;
- generic workflow/BPMN designer, committees, configurable multi-step routing;
- reports, Jasper/PDF/export;
- hard delete, unvalidate, retroactive mutation, or direct editing of person profiles;
- direct PrimeHR reads/writes to HRM or Administrative tables;
- live PostgreSQL as a blocking gate under the current SQL Server-primary direction;
- commit, push, deployment, or Phase 4 implementation.

## 15. Known risks and unresolved future decisions

- A true direct-supervisor contract remains unresolved. Explicit assignments are safe for Phase 3 but require HR administration until ownership is established.
- Full organization-scoped authorization is limited because current employee appointments do not expose a verified organizational-unit relationship. Phase 3 supports OWN, ASSIGNED, and AGENCY_WIDE only.
- Binary evidence cannot be accepted safely until storage, malware scanning, classification, retention, backup, and per-download authorization are approved.
- Current tracked legacy configuration contains secret material; Phase 3 must not copy or expand it. Secret externalization/rotation remains a separate production-hardening task.
- Live PostgreSQL Phase 2/3 runtime remains unverified under the user's SQL Server-primary testing direction, although dual portability artifacts remain mandatory.
- Multi-assessor validation is a human decision workflow and will require careful usability testing to prevent accidental selection of the wrong final level.

## 16. Approval and execution condition

The user approved this exact scope on 2026-08-26 and authorized Phase 3.1, followed by Phase 3.2 only after all Phase 3.1 gates pass. Later checkpoints remain controlled by the gates in section 11; Phase 4 remains excluded.
