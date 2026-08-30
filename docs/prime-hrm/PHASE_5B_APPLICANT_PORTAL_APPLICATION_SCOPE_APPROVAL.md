# ISOFT PRIME-HRM Phase 5B - Applicant Portal and Application Scope Approval

Prepared: 2026-08-29

Status: Complete. Phase 5B.1 and Phase 5B.2 backend gates passed, and the separately approved Phase 5B.3 Administrative controls, Careers/applicant UI, staff intake UI, documentation, and SQL Server Playwright acceptance were completed on 2026-08-30. Phase 5C has not started.

## 1. Objective and hard boundary

Phase 5B will let a person who is not an ISOFT HRIS employee create a separate applicant account, accept the effective privacy notice, maintain an application profile, record PDS/Work Experience Sheet-compatible information, upload controlled supporting documents, apply to an open PUBLISHED Phase 5A vacancy, receive an acknowledgment, withdraw where allowed, and view their own application status and communication history.

Phase 5B stops at secure application intake and applicant self-service. It does not determine documentary completeness, compare an applicant to the Qualification Standard, mark qualified/disqualified, perform examinations or interviews, score/rank candidates, record HRMPSB deliberation, select an appointee, create an employee, change an appointment, or onboard a selected applicant. Those remain Phase 5C-5F.

## 2. Validated architecture decisions

The actual repositories support the Master Plan V2 recommendation with these exact decisions:

1. Public routes initially live in `prime-hr-software`, but use a separate public/applicant layout, API client, session keys, and authorization boundary. They do not render the employee/management `PrimeHrShell`.
2. Applicant identity is PrimeHR-owned and separate from Administrative employee SSO. An applicant token has a distinct subject type and audience and must never authorize `/api/primehr/v1/**`, Administrative, HRM, Timekeeping, Payroll, or Employee Portal APIs.
3. Employee JWT/SSO must never authenticate an applicant application, and an applicant account does not create or update an HRIS employee.
4. PrimeHR owns applicant/application/document metadata in its database. It references a published Phase 5A vacancy and preserves immutable vacancy and applicant submission snapshots.
5. Files use a `DocumentStorage` abstraction. Metadata, checksum, classification, owner, retention state, and authorization remain in PrimeHR; bytes use a configurable local on-premise provider or S3-compatible cloud provider. Source code and documentation contain no real bucket, endpoint, key, or credential.
6. Phase 5B uses direct REST and transactional persistence. Email/SMS, RabbitMQ, outbox, antivirus service integration, and external job-board posting remain deferred.
7. The current trusted single-agency configuration remains the server-side agency authority. The browser cannot select or forge an agency. Multi-agency public routing requires a later explicit design.

## 3. Applicant identity, privacy, and security

### 3.1 Account

An applicant account contains a generated immutable ID, normalized unique email/login, password hash, display name, status, failed-attempt/lock fields, last-login time, created/updated metadata, and optimistic record version. Passwords use Spring Security's adaptive password encoder and are never stored, logged, returned, or placed in audit JSON.

Initial lifecycle:

```text
ACTIVE -> LOCKED -> ACTIVE
ACTIVE -> DISABLED
```

Self-registration creates an ACTIVE account for the first generic release because no verified notification provider exists. Email verification, password reset delivery, CAPTCHA, and federated applicant identity are explicitly deferred and must be recorded as production-hardening risks. Login responses must be indistinguishable for unknown email and wrong password. Rate/attempt controls are backend enforced.

### 3.2 Token/session isolation

- Applicant JWTs use a separately configured signing secret/key and explicit applicant audience/subject claim.
- Public endpoints are under `/api/primehr/public/v1/**`; authenticated applicant endpoints are under `/api/primehr/applicant/v1/**`.
- Existing management endpoints remain `/api/primehr/v1/**` and continue to require the Administrative employee token.
- The browser uses applicant-specific session storage keys and clears them on logout. Existing `authToken`, employee, and SSO storage contracts are not reused.
- CORS origins and token lifetimes are configured; no `localhost`, wildcard production origin, or literal secret is introduced.

### 3.3 Privacy notice and consent

PrimeHR owns versioned privacy notices with title, body/reference, effectivity, status, retention summary, and audit metadata. Registration/application requires the exact effective notice. Consent records preserve notice ID/version, applicant, accepted time, declared IP/user-agent evidence subject to minimization, and withdrawal/retention state. A later notice does not rewrite historical consent.

## 4. Applicant profile and PDS/WES representation

Phase 5B stores applicant-owned, editable profile data separately from HRM employee/PDS tables:

- legal/personal identity and contact data needed for recruitment;
- address and citizenship facts required by the supported form;
- education;
- work experience/WES-compatible entries;
- training and learning entries;
- eligibility;
- licenses and other credentials;
- references and optional applicant declarations required by the configured form.

DTOs must use explicit fields and validation. Sensitive fields are minimized, masked where appropriate, never exposed in public search, and readable only by the owning applicant or authorized staff. Phase 5B does not claim complete CSC form certification unless the exact current government form is separately validated.

Submitted applications capture an immutable profile snapshot. Later profile edits affect only future submissions; they cannot silently rewrite an application already submitted.

## 5. Secure document handling

Document metadata includes applicant/application ownership, document type, original safe filename, storage object key, media type, byte size, SHA-256 checksum, classification, upload time, active/replaced state, retention disposition, and optimistic version.

Controls:

- allowlisted PDF/image/office formats defined by configuration, with content-signature validation rather than trusting the extension;
- configurable per-file and per-application limits;
- randomized storage keys; no applicant-supplied path;
- local provider confines resolved paths to an explicit configured root and prevents traversal;
- S3-compatible provider uses private objects and backend-authorized streaming or short-lived signed access;
- upload/download/delete/replace checks applicant ownership or exact staff permission;
- submitted application documents are immutable evidence; replacements create a new version;
- checksum/size/type are audited; file bytes and secrets are not audited;
- scanning hook and `PENDING_SCAN/CLEAN/REJECTED` metadata exist, but no external antivirus product is claimed unless configured and tested;
- production startup fails closed if uploads are enabled without a durable configured provider.

Retention period and legal hold remain configurable policy values. Hard physical deletion is not exposed as an ordinary applicant operation; withdrawal/deactivation follows the configured retention disposition.

## 6. Vacancy application lifecycle

Only a Phase 5A PUBLISHED vacancy whose application window is currently open can accept a new application. CLOSED, CANCELLED, APPROVED-but-not-published, future, or expired publications reject intake.

Lifecycle:

```text
DRAFT -> SUBMITTED -> WITHDRAWN
```

Phase 5C will add screening states through a forward migration; Phase 5B must not pre-judge completeness or qualification.

Rules:

- one active application per applicant per vacancy publication;
- applicant ownership comes from the applicant token, never a request applicant ID;
- DRAFT remains editable and may be abandoned/deactivated;
- submit requires effective consent, required profile declarations, required configured document types, an open published vacancy, and matching optimistic versions;
- submission atomically creates an acknowledgment number, immutable vacancy/QS/profile/competency/application/document manifest snapshots, audit event, and initial communication-history entry;
- submitted content is immutable; correction requires withdrawal and a new controlled version only if the vacancy remains open and policy permits;
- withdrawal requires an explicit reason and never deletes the submission evidence;
- status shown to the applicant is a safe public status, not an internal screening/decision field;
- the browser cannot set status, acknowledgment, applicant identity, vacancy facts, or decision metadata.

## 7. Communication history

Phase 5B records in-portal communication history only:

- system acknowledgment of submission/withdrawal;
- authorized staff informational message linked to an application;
- applicant read time where applicable;
- actor, timestamp, safe subject/body, channel `PORTAL`, and correlation ID.

No email/SMS delivery is claimed. Templates, bulk messaging, interview invitations, screening results, and notification provider integration require later approval. Messages cannot alter application status.

## 8. Persistence and migrations

Proposed forward-only provider-equivalent migrations:

### Phase 5B.1 / V13 - identity, privacy, profile, and storage foundation

- `rsp_applicant_account`;
- `rsp_privacy_notice` and `rsp_applicant_consent`;
- `rsp_applicant_profile` plus normalized education, work experience, training, eligibility, license/credential, and reference children;
- `rsp_applicant_document` and document-version/storage metadata;
- indexes/constraints for normalized login, ownership, notice version/effectivity, profile children, checksum/object key, lifecycle, and optimistic versions.

### Phase 5B.2 / V14 - application intake

- `rsp_position_application`;
- immutable vacancy/application snapshot fields or normalized children where required;
- `rsp_application_document_snapshot` manifest;
- `rsp_applicant_communication`;
- unique applicant + publication active application, acknowledgment, owner/status/date, and staff queue indexes.

SQL Server and PostgreSQL migrations must be structurally equivalent. Shared Java uses JPA, derived queries, Specifications, `Pageable`, and provider-neutral validation. No `TOP`, `LIMIT`, `ILIKE`, vendor date functions, provider casts, database-specific blobs in shared logic, or cross-database joins.

## 9. REST contract

### Public, unauthenticated

```http
GET  /api/primehr/public/v1/privacy-notices/current
GET  /api/primehr/public/v1/vacancies
GET  /api/primehr/public/v1/vacancies/{publicationId}
POST /api/primehr/public/v1/applicant-accounts/register
POST /api/primehr/public/v1/applicant-sessions
```

Only minimum public vacancy fields from PUBLISHED/open or historical-public records are exposed. Internal actor IDs, audit payloads, source fingerprints, private contacts, and management remarks are excluded.

### Authenticated applicant

```http
DELETE /api/primehr/applicant/v1/session
GET    /api/primehr/applicant/v1/me
PUT    /api/primehr/applicant/v1/me
GET    /api/primehr/applicant/v1/me/profile
PUT    /api/primehr/applicant/v1/me/profile
POST   /api/primehr/applicant/v1/me/consents
GET    /api/primehr/applicant/v1/me/documents
POST   /api/primehr/applicant/v1/me/documents
GET    /api/primehr/applicant/v1/me/documents/{documentId}/content
POST   /api/primehr/applicant/v1/me/documents/{documentId}/replace
DELETE /api/primehr/applicant/v1/me/documents/{documentId}
GET    /api/primehr/applicant/v1/me/applications
POST   /api/primehr/applicant/v1/me/applications
GET    /api/primehr/applicant/v1/me/applications/{applicationId}
PUT    /api/primehr/applicant/v1/me/applications/{applicationId}
POST   /api/primehr/applicant/v1/me/applications/{applicationId}/submit
POST   /api/primehr/applicant/v1/me/applications/{applicationId}/withdraw
GET    /api/primehr/applicant/v1/me/applications/{applicationId}/communications
```

### Authenticated PrimeHR staff

```http
GET  /api/primehr/v1/rsp/applications
GET  /api/primehr/v1/rsp/applications/{applicationId}
GET  /api/primehr/v1/rsp/applications/{applicationId}/documents/{documentId}/content
GET  /api/primehr/v1/rsp/applications/{applicationId}/communications
POST /api/primehr/v1/rsp/applications/{applicationId}/communications
```

Staff access in Phase 5B is read/intake administration only. There is no completeness, qualified/disqualified, scoring, ranking, shortlist, or selection mutation.

All endpoints use explicit DTOs, Bean Validation, stable problem responses, correlation IDs, safe pagination, ownership/data-scope enforcement, optimistic conflicts, and non-disclosing authentication errors.

## 10. Administrative permissions and UI routes

Add one staff feature:

- `primehr.rsp-applicant-intake`
  - Access: list/view applicant submissions and allowed documents;
  - Add: send an informational portal message;
  - no Edit/Delete/Submit/Approve/Publish semantics in Phase 5B.

Administrator compatibility remains, but sensitive document access and staff messages are audited. Public/applicant self-service is controlled by applicant identity/ownership, not an Administrative permission ruleset.

Public/applicant routes in `prime-hr-software`:

```text
/careers
/careers/vacancies/[id]
/careers/register
/careers/login
/careers/profile
/careers/documents
/careers/apply/[publicationId]
/careers/my-applications
/careers/my-applications/[id]
```

Staff route:

```text
/prime-hr/applicant-intake
```

The public layout is clearly branded ISOFT HRIS Careers, accessible and responsive, and does not expose employee navigation/session details. It provides loading, empty, expired/closed, denied/ownership, validation, upload-progress/failure, conflict, and server-error states.

## 11. Controlled implementation slices

### Phase 5B.1 - Applicant security, privacy, profile, and storage foundation

- applicant token/filter/security boundary and account lifecycle;
- versioned privacy notice/consent;
- applicant profile and PDS/WES-compatible normalized data;
- `DocumentStorage` abstraction, local and S3-compatible adapters, metadata and secured content endpoints;
- V13 SQL Server/PostgreSQL migrations, OpenAPI, audit, unit/integration/provider/security tests;
- public vacancy read contract derived from Phase 5A PUBLISHED notices;
- backend only; no application intake, staff decisions, or public UI.

Gates:

- applicant tokens rejected by all management/HRIS endpoints and employee tokens rejected by applicant endpoints;
- password, lockout, enumeration, consent version, ownership, traversal, type/size/checksum, replacement, and unauthorized download tests;
- durable-provider configuration fails closed;
- SQL Server fresh V1-V13 and populated V12-to-V13;
- PostgreSQL-mode/parity and provider-neutral shared-code audit;
- affected clean test/package, OpenAPI, secret/generated-file, and Phase 5C boundary audits.

### Phase 5B.2 - Application and communication backend

Begins only after every Phase 5B.1 gate passes.

- V14 application, immutable snapshots, document manifest, acknowledgment, withdrawal, safe status, communication history;
- applicant self-service and staff read/message APIs;
- exact vacancy-window, duplicate, readiness, ownership, optimistic conflict, rollback, audit, and idempotency behavior;
- backend only; no screening/qualification decision or UI.

Gates:

- complete DRAFT/SUBMITTED/WITHDRAWN matrix;
- expired/future/closed/cancelled vacancy rejection;
- duplicate/ownership/status-forging/document-snapshot/transaction rollback tests;
- staff denied/allowed/data-scope and sensitive-document audit tests;
- SQL Server fresh V1-V14 and populated V13-to-V14;
- PostgreSQL-mode/parity, affected clean test/package, OpenAPI, secret and Phase 5C boundary audits.

### Phase 5B.3 - Administrative control, public/staff UI, and Playwright

Requires separate approval after Phase 5B.2 passes.

- Administrative applicant-intake permission control;
- separate public Careers/applicant layout and routes;
- applicant registration/login/profile/consent/document/application/status/communication UI;
- staff intake read/message UI without screening controls;
- repeatable Playwright for token isolation, registration/login, consent, profile, document validation/ownership, application success/validation/duplicate/withdrawal, staff allowed/denied, immutable snapshot, and full regression;
- user/operator guide, E2E guide, progress update, and final Phase 5B review manifest.

No Jasper report is required in Phase 5B. Vacancy notices remain Phase 5A; application screening and RSP evidence reports remain Phase 5C/5F.

## 12. Acceptance criteria

Phase 5B is complete only when:

1. Applicant identity/token/session is demonstrably isolated from employee and PrimeHR staff identity.
2. An applicant sees and changes only their own profile, documents, applications, and communications.
3. Consent preserves the exact effective privacy-notice version.
4. Files are private, validated, checksummed, path-safe, provider-abstracted, authorized per download, and retained according to configured policy.
5. Only an open PUBLISHED vacancy accepts applications.
6. Submission atomically preserves immutable vacancy, QS, competency, applicant profile, and document-manifest evidence.
7. Duplicate and stale submissions cannot overwrite or multiply active applications.
8. Staff can administer intake without receiving any Phase 5C screening/qualification mutation.
9. SQL Server gates pass and PostgreSQL portability evidence contains no provider-specific shared query/storage logic.
10. Full Playwright regression passes with no unexplained skips.
11. No employee creation/appointment mutation or Phase 5C+ functionality exists.

## 13. Explicit exclusions

- email verification/reset delivery, SMS/email provider, CAPTCHA, external identity provider, or social login;
- automatic antivirus product, OCR, PDS parsing, document classification, or AI evaluation;
- documentary completeness, QS matching, qualified/disqualified decisions, overrides, shortlist, or screening queue actions;
- examination/interview scheduling or scores, HRMPSB deliberation, ranking, selection, appointment handoff, employee creation, or onboarding;
- public disclosure of applicant lists or personal documents;
- hard deletion that defeats retention/audit, unrestricted staff document access, or browser-supplied applicant/agency/status facts;
- external CSC/job-board posting, notification broker/outbox, generic BPMN engine, or cross-service database writes;
- full RSP Jasper/analytics package.

## 14. Known risks and policy decisions

Approving this scope also approves these initial generic decisions:

1. Accounts activate without email verification because no delivery provider exists; production hardening is recorded and not hidden.
2. Password login uses normalized email and a dedicated applicant JWT boundary.
3. Public routing is initially single-agency through the server's trusted agency configuration.
4. Storage supports local on-premise and S3-compatible cloud providers; deployment must configure one durable provider before enabling uploads.
5. Retention duration, file limits/types, privacy notice text, and required document types are configuration/policy data, not Java/TS constants.
6. Staff intake is agency-wide until an authoritative narrower assignment model exists.
7. Phase 5B captures supported PDS/WES data but does not claim formal CSC certification without separate form validation.

Items still required before production deployment, but not blockers to implementing/test-driving the generic foundation: actual privacy/legal text, retention duration, approved file-type/size policy, cloud storage credentials/bucket, rate limits, and the organization's applicant support process.

## 15. Approval gate

Recommended approval wording:

> Approve Phase 5B as defined. Proceed with Phase 5B.1, and continue to Phase 5B.2 only after all Phase 5B.1 gates pass. Stop before Phase 5B.3 until I approve the Administrative control, public/staff UI, and Playwright acceptance.

No Phase 5B implementation may start before explicit approval.
