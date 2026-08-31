# ISOFT PRIME-HRM Phase 5C - Screening and Qualification Standard Validation Scope Approval

Prepared: 2026-08-30

Status: Approved and complete. Phase 5C.1, 5C.2, and separately approved Phase 5C.3 implementation/acceptance gates passed; work stopped before Phase 5D.

## 1. Objective and hard boundary

Phase 5C will let authorized recruitment staff screen an immutable Phase 5B submission for required-document completeness and compliance with the exact Qualification Standard and screening policy used for that vacancy. It will preserve structured findings, evidence links, an independently validated `QUALIFIED` or `DISQUALIFIED` outcome, applicant-safe status/communication, reasons, version history, and audit.

Phase 5C is decision support plus an authorized human decision. It must not silently infer a legal qualification decision from résumé text, file names, keywords, or AI. It stops before examinations, interviews, HRMPSB deliberation, comparative evaluation, scoring/ranking, shortlist, selection, appointment handoff, employee creation, onboarding, and the Phase 5F RSP report/evidence package.

## 2. Repository validation and architecture decisions

The approved Master Plan V2 names Phase 5C as **Screening and QS validation** and requires document completeness, Qualification Standard screening, configurable questions, permitted competency prerequisites, qualified/disqualified outcomes, explicit reasons, and audited overrides. The actual repositories support that intent with these corrections and decisions:

1. Phase 5B already stores applicant identity separately from employees and preserves immutable vacancy, Qualification Standard, Position Competency Profile, applicant profile, and document-manifest snapshots at submission. Phase 5C evaluates those snapshots, not mutable current profile/document rows and not a live cross-service join.
2. Administrative owns the authoritative published Qualification Standard. PrimeHR already snapshots its exact ID/version and human-readable education, training, experience, eligibility, licence, and source-basis fields into the vacancy/application evidence.
3. Applicant profile entries are normalized by type but their titles/details and the Qualification Standard requirements are partly free-form. Objective document presence, dates, durations, counts, and explicitly structured criteria may be calculated. Relevance/equivalence judgments remain human findings.
4. No complete HRMPSB/committee authority model exists. Phase 5C uses case-specific `SCREENER` and `VALIDATOR` assignments to authorized employees. Committee membership, conflict declarations, deliberation, and panel actions remain Phase 5D.
5. Existing Administrative effective authorization supports the required Access/Add/Edit/Submit/Approve/Publish actions and agency-wide data scope. New features can be added without changing legacy permissions.
6. Existing applicant communication and safe-status channels can disclose a final applicant-safe result without exposing internal notes, other applicants, audit JSON, or staff-only findings.
7. PrimeHR remains the owner of screening policy, case, finding, outcome, and audit data. It does not write to Administrative, HRM, Timekeeping, or Payroll tables.

## 3. Generic policy decisions

Phase 5C introduces no agency-specific screening rule or hard-coded qualification interpretation. The agency must publish a versioned screening policy containing ordered criteria and reason codes.

Supported criterion categories:

- `REQUIRED_DOCUMENT`;
- `EDUCATION`;
- `TRAINING`;
- `EXPERIENCE`;
- `ELIGIBILITY`;
- `LICENSE`;
- `COMPETENCY_PREREQUISITE`;
- `SCREENING_QUESTION`.

Supported evaluation modes:

- `PRESENCE` for exact immutable document/profile evidence presence;
- `NUMERIC_THRESHOLD` only where the policy supplies a structured unit/value and the captured evidence provides a comparable value;
- `DATE_OR_DURATION` for transparent date/duration calculations;
- `MANUAL_REVIEW` for relevance, equivalence, authenticity, or legal judgment;
- `DECLARATION` for an explicit staff checklist response supported by evidence.

Each criterion records whether it is mandatory, disqualifying, permits `NOT_APPLICABLE`, requires remarks/evidence, its public-safe label, internal instructions, display order, and the Qualification Standard section or document type it supports. Criteria cannot contain executable expressions or provider-specific SQL.

No seed data will claim that a specific degree, training title, experience description, eligibility, licence, or competency is equivalent. Client policy can be configured later without changing the generic engine.

## 4. Policy lifecycle and vacancy binding

Screening policy lifecycle:

```text
DRAFT -> PUBLISHED -> SUPERSEDED
```

- Drafts may be edited with optimistic version checks.
- Published versions are immutable. A change creates a successor definition version.
- Duplicate code/version, invalid display order, missing mandatory metadata, unsupported result/evaluation combinations, and overlapping effectivity are rejected.
- A published policy is bound to an exact Phase 5A vacancy publication before its first screening case is opened.
- The publication-to-policy binding is immutable after any case exists. Existing already-published Phase 5A vacancies may receive one audited policy binding without rewriting the vacancy publication.
- A case snapshots the exact policy/version/criteria/reason codes. Later policy or Qualification Standard changes never rewrite a completed screening.

Reason codes have an internal label, outcome compatibility, public-safe text, effectivity/status, display order, and whether explanatory remarks are mandatory. Internal-only comments are never returned to the applicant.

## 5. Screening case, assignments, and lifecycle

Only an active, non-withdrawn `SUBMITTED` Phase 5B application can begin screening. Opening a case verifies immutable submission evidence, exact policy binding, agency ownership, and optimistic versions.

Screening case lifecycle:

```text
DRAFT/RETURNED -> SUBMITTED -> QUALIFIED | DISQUALIFIED
              \-> CANCELLED when the application is withdrawn before a final decision
```

Application/internal and applicant-safe progression:

```text
SUBMITTED -> UNDER_SCREENING -> QUALIFIED | DISQUALIFIED
SUBMITTED or UNDER_SCREENING -> WITHDRAWN

Applicant-safe labels:
SUBMITTED, UNDER REVIEW, QUALIFIED, NOT QUALIFIED, WITHDRAWN
```

Rules:

1. One current screening case per application; historical revisions/outcomes remain immutable.
2. A coordinator with Add assigns exact employee numbers as `SCREENER` and `VALIDATOR`. Assignment requires the corresponding effective screening permission and never creates an employee, user, committee, or HRM role.
3. An assigned screener records a result for every active criterion: `MET`, `NOT_MET`, `NEEDS_REVIEW`, or policy-permitted `NOT_APPLICABLE`, with required remarks and evidence links.
4. Evidence links point to immutable application document snapshots, profile snapshot entries/sections, vacancy/QS/competency snapshots, or a recorded staff declaration. Phase 5C adds no unrestricted file upload.
5. The engine may prefill objective findings but a human screener must confirm them. Free-text similarity or keyword matching cannot create `MET`, `NOT_MET`, `QUALIFIED`, or `DISQUALIFIED` automatically.
6. Submission requires every criterion resolved, all required evidence/remarks present, and a `QUALIFIED` or `DISQUALIFIED` recommendation consistent with the policy.
7. `QUALIFIED` requires every mandatory/disqualifying criterion to be `MET` or explicitly policy-permitted `NOT_APPLICABLE`. `NEEDS_REVIEW` blocks submission/finalization.
8. `DISQUALIFIED` requires at least one disqualifying `NOT_MET` finding, an outcome-compatible reason code, internal explanation, and applicant-safe reason text.
9. The validator cannot finalize their own screening recommendation. They may return it with a reason or finalize the same supported outcome.
10. A system administrator may override the recommended outcome only with administrator authority, Approve permission, explicit override reason, and complete previous/new state audit. Override does not delete or alter findings.
11. Final outcomes and decision evidence are immutable. A correction uses a controlled superseding screening revision and must preserve the earlier outcome, actor, reason, evidence, and audit.
12. All commands use `recordVersion`; stale writes return HTTP 409 and do not partially change findings, assignments, application status, communication, or audit.
13. Applicant withdrawal from `SUBMITTED` or `UNDER_SCREENING` atomically cancels the open case and preserves all evidence/history. A final screening decision is historical and cannot be erased by a later operation.

## 6. Authorization and separation of duties

Add two Administrative features:

### `primehr.rsp-screening-policy`

- Access: list/detail published and permitted draft policy versions;
- Add: create a draft or successor;
- Edit: update a draft;
- Publish: publish an immutable policy version;
- no Delete, Submit, or Approve action in Phase 5C.

### `primehr.rsp-application-screening`

- Access: list/detail permitted screening cases;
- Add: open a case and manage screener/validator assignment;
- Edit: assigned screener records findings/evidence;
- Submit: assigned screener submits a recommendation;
- Approve: independent validator returns or finalizes the decision;
- no Delete, Publish, scoring, ranking, shortlist, or selection action.

Both features require agency-wide scope until a verified narrower recruitment assignment authority exists. Case assignment further restricts Edit/Submit/Approve actions. Applicant Intake Access/Add remains independent and does not grant screening authority. Policy publishers, screeners, validators, and administrators receive only their exact action surfaces; the backend enforces every action even if an API is called directly.

## 7. Persistence and migrations

Proposed forward-only, provider-equivalent migrations:

### Phase 5C.1 / V15 - screening policy and deterministic rule foundation

- `rsp_screening_policy`;
- `rsp_screening_policy_criterion`;
- `rsp_screening_reason_code`;
- `rsp_publication_screening_policy`.

Important constraints/indexes:

- agency + normalized policy code + definition version uniqueness;
- one immutable screening-policy binding per vacancy publication;
- ordered unique criteria/reason codes within a policy;
- effectivity/status/lifecycle consistency;
- criterion category/evaluation/result metadata checks;
- foreign keys to the exact policy and Phase 5A publication;
- optimistic record versions and audit metadata.

### Phase 5C.2 / V16 - assigned screening and final decisions

- `rsp_screening_case`;
- `rsp_screening_assignment`;
- `rsp_screening_finding`;
- `rsp_screening_evidence_link`;
- `rsp_screening_decision`.

V16 also extends the application status/safe-status constraint for `UNDER_SCREENING`, `QUALIFIED`, and `DISQUALIFIED` while retaining existing DRAFT/SUBMITTED/WITHDRAWN data.

Important constraints/indexes:

- one current case/revision chain per application;
- one active assignment per case/employee/process role;
- one finding per case criterion;
- evidence links confined to the owning application/case snapshots;
- consistent recommendation/outcome/reason/override metadata;
- immutable final decision and supersession lineage;
- indexes for agency/status/assignee/application/publication/reason/date queues.

SQL Server and PostgreSQL scripts must be structurally equivalent. Shared Java uses JPA, derived queries/JPQL, `Pageable`, Java time, and provider-neutral calculations. No `TOP`, `LIMIT`, vendor casts/functions, native JSON operators, database file access, or cross-database joins are allowed in shared production logic.

## 8. Proposed REST contract

Exact naming will follow the existing `/api/primehr/v1/rsp/**` convention.

### Screening policy management

```http
GET  /api/primehr/v1/rsp/screening-policies
POST /api/primehr/v1/rsp/screening-policies
GET  /api/primehr/v1/rsp/screening-policies/{policyId}
PUT  /api/primehr/v1/rsp/screening-policies/{policyId}
POST /api/primehr/v1/rsp/screening-policies/{policyId}/publish
POST /api/primehr/v1/rsp/screening-policies/{policyId}/successors
PUT  /api/primehr/v1/rsp/vacancy-publications/{publicationId}/screening-policy
```

### Application screening

```http
GET  /api/primehr/v1/rsp/screening-cases
POST /api/primehr/v1/rsp/applications/{applicationId}/screening-cases
GET  /api/primehr/v1/rsp/screening-cases/{caseId}
PUT  /api/primehr/v1/rsp/screening-cases/{caseId}/assignments
PUT  /api/primehr/v1/rsp/screening-cases/{caseId}/findings/{criterionId}
POST /api/primehr/v1/rsp/screening-cases/{caseId}/submit
POST /api/primehr/v1/rsp/screening-cases/{caseId}/return
POST /api/primehr/v1/rsp/screening-cases/{caseId}/finalize
POST /api/primehr/v1/rsp/screening-cases/{caseId}/override
GET  /api/primehr/v1/rsp/screening-cases/{caseId}/history
```

Applicant APIs receive no internal screening mutation. Existing applicant application/detail and communication responses expose only the final safe status, safe reason/message, and applicant-owned history. Public vacancy APIs expose no applicant or screening data.

Every command uses explicit DTO validation, authenticated actor/agency resolution, assignment/action/data-scope checks, idempotent transition handling, optimistic versioning, transactional audit/communication, and safe problem responses.

## 9. UI scope

Administrative UI:

- add **Screening Policy** and **Application Screening** permission rows with exactly the action columns described above;
- do not add client-specific criteria or reason-code defaults to Administrative master data.

PrimeHR staff UI:

```text
/prime-hr/screening-policies
/prime-hr/application-screening
```

- versioned policy list/detail/draft/successor/publish and vacancy binding;
- screening work queue filtered by status/assignee/publication/search;
- immutable submission, QS, competency, profile, and document evidence presented side by side with ordered findings;
- transparent auto-check explanation and clear manual-review indicator;
- assignment, save, submit, return, finalize, administrator override, conflict, audit/history, empty, denied, and dependency-error states;
- no examination, interview, HRMPSB, score, rank, shortlist, select, appoint, or onboard control.

Careers applicant UI:

- existing application detail displays only applicant-safe status and safe communication/reason after finalization;
- no internal findings, staff identities, policy instructions, other applicants, ranking, or screening control.

No Employee Portal change is required because applicants remain separate identities and Phase 5C creates no employee record.

## 10. Reporting boundary

No Jasper report is required in Phase 5C. Screening reports, disqualification counts/reasons, comparative evaluation, demographic analysis, and formal RSP evidence links belong to Phase 5F. Phase 5C APIs/UI may show one authorized case and its audit history but must not introduce an export that bypasses report/privacy approval.

## 11. Controlled implementation slices and gates

### Phase 5C.1 - Policy, binding, and deterministic engine foundation

- V15 provider-equivalent schema;
- policy/criterion/reason-code lifecycle and exact publication binding;
- objective evidence evaluator with explicit `NEEDS_REVIEW` fallback;
- REST/OpenAPI, authorization guards, audit, domain/service/repository tests;
- backend only: no screening case decision, application status change, UI, or report.

Gates:

- policy lifecycle/version/effectivity/order/duplicate/immutability tests;
- criterion/evaluation compatibility and no-free-text-auto-decision tests;
- publication binding ownership/immutability/stale-write tests;
- exact permission/action/data-scope denied/allowed tests;
- SQL Server fresh V1-V15 and populated V14-to-V15;
- PostgreSQL-mode Flyway/Hibernate, migration parity, provider-neutral query audit;
- affected clean test/package, OpenAPI, secret/generated-file, and Phase 5D+ boundary audits.

### Phase 5C.2 - Assignment, screening workflow, and validated outcome

Begins only after every Phase 5C.1 gate passes.

- V16 cases, assignments, findings, evidence links, decisions, application/safe statuses;
- screener recommendation, independent return/finalization, administrator override, controlled superseding correction;
- applicant-safe communication/status and under-screening withdrawal behavior;
- backend only: no Administrative/PrimeHR/Careers UI, Jasper, examination, interview, committee, score, rank, shortlist, or selection.

Gates:

- full DRAFT/RETURNED/SUBMITTED/QUALIFIED/DISQUALIFIED/CANCELLED matrix;
- assignment and separation-of-duties tests;
- mandatory finding/evidence/reason consistency and transaction rollback tests;
- immutable snapshot, final decision, correction lineage, ownership, withdrawal, idempotency, optimistic conflict, and audit tests;
- applicant-safe disclosure and internal-data non-disclosure tests;
- SQL Server fresh V1-V16 and populated V15-to-V16;
- PostgreSQL-mode/parity, affected clean test/package, OpenAPI, secret and Phase 5D+ boundary audits.

### Phase 5C.3 - Administrative controls, staff/applicant UI, and Playwright

Requires separate approval after Phase 5C.2 passes.

- Administrative permission controls;
- PrimeHR policy and application-screening UI;
- Careers safe-status/communication update;
- repeatable SQL Server Playwright for policy version/publish/binding, objective/manual checks, assignment, SOD, return/resubmit/finalize, disqualification reason, qualified outcome, override, conflict, withdrawal, applicant-safe disclosure, denied actions, and full regression;
- user/operator guide, E2E guide, progress update, and final Phase 5C review manifest.

No Jasper report is included.

## 12. Acceptance criteria

Phase 5C is complete only when:

1. Screening uses the exact immutable application, policy, QS, competency, profile, and document evidence versions.
2. Objective calculations are transparent and free-text/relevance/equivalence questions remain explicit human review.
3. Every final decision has complete findings, evidence, reason metadata, authorized actors, and audit history.
4. A screener cannot validate their own recommendation except an explicitly authorized, reasoned administrator override.
5. Unauthorized/unassigned users cannot read sensitive evidence or mutate screening.
6. Applicants see only safe status/reasons/communications for their own application.
7. Stale/duplicate/direct API calls cannot create multiple current cases or partially overwrite a decision.
8. Withdrawal and correction preserve historical evidence and outcomes.
9. Real SQL Server gates pass; PostgreSQL portability evidence remains provider-neutral and truthful about any live-provider non-run.
10. Full Playwright regression passes without unexplained skips.
11. No Phase 5D, 5E, or 5F functionality exists.

## 13. Explicit exclusions

- examination definitions, schedules, test administration, results, or proctoring;
- technical/skills tests, competency-based interviews, panel scoring, or reference/background checking;
- HRMPSB committee management, conflict-of-interest declarations, deliberation, minutes, resolutions, or voting;
- comparative evaluation, weights/formulas, consolidated scores, ranking, shortlist, selection, appointing-authority decision, or non-selection workflow;
- appointment handoff, employee/account creation, onboarding, pre-employment requirements, or HRM writes;
- AI/NLP résumé parsing, OCR, automatic equivalence/relevance/authenticity judgment, or automatic legal disqualification;
- email/SMS provider, bulk notices, external job boards, broker/outbox, or generic workflow engine;
- RSP Jasper reports, analytics, demographic reports, or formal evidence package;
- destructive edit/delete of submissions, findings, final decisions, or evidence.

## 14. Known risks and policy decisions

Approving this generic scope accepts these initial decisions:

1. Qualification Standard text remains authoritative but is not assumed machine-comparable; agencies configure structured criteria and human findings.
2. No agency-specific screening template or reason code is seeded as universal policy.
3. Applicant competency prerequisites remain manual review unless valid applicant-specific competency evidence exists; employee person profiles are never silently reused for a public applicant.
4. Screening uses case-specific screener/validator assignments, not an unverified HRMPSB model.
5. Staff screening remains agency-wide plus assignment-restricted under the current trusted single-agency architecture.
6. Final applicant-safe reasons require agency-approved wording before production use; internal remarks remain confidential.
7. Malware scanning/authenticity verification and document legal validity remain operational/human controls, not claims made by this phase.
8. Live PostgreSQL remains a non-blocking gate only under the user's existing SQL Server-primary direction; equivalent migrations and provider-neutral code remain mandatory.

Client-specific Merit Selection Plan details, required evidence, criterion thresholds, equivalence rules, safe reason wording, and screening officer roles must be configured later as policy data, not coded into the generic product.

## 15. Approval gate

Recommended approval wording:

> Approve Phase 5C as defined. Proceed with Phase 5C.1, and continue to Phase 5C.2 only after all Phase 5C.1 gates pass. Stop before Phase 5C.3 until I approve the Administrative controls, staff/applicant UI, and Playwright acceptance.

No Phase 5C implementation may start before explicit approval. Phase 5D and later remain separately gated.
