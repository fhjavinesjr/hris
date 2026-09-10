# ISOFT PRIME-HRM Phase 5D - Examination, Interview, and HRMPSB Evaluation Scope Approval

Prepared: 2026-08-31

Status: Proposed for approval only. No Phase 5D implementation is authorized by this document.

## 1. Objective and hard boundary

Phase 5D will let an agency configure and execute the assessment portion of Recruitment, Selection, and Placement after Phase 5C qualification. Authorized staff will manage examination and interview definitions and schedules, effective HRMPSB membership, conflict-of-interest declarations, individual panel ratings, technical/skills test results, lawful background/reference checks, transparent consolidated scores, comparative evaluation, and an audited HRMPSB recommendation/resolution.

The system may calculate, order, flag, and present evidence according to the exact published Merit Selection Plan evaluation policy. It must not silently replace an examiner, panel member, HRMPSB, appointing authority, or other legally responsible decision-maker.

Phase 5D ends with an immutable comparative evaluation and HRMPSB recommendation. It does **not** select an appointee, issue non-selection notices, create an appointment or employee record, hand data to the Appointment module, perform onboarding, or implement Phase 5F reports/evidence indexes.

## 2. Master Plan and repository validation

The Master Plan V2 requires Phase 5D to cover examination definitions and schedules, technical/skills tests, competency-based interviews or BEI, panel assignments and individual ratings, configurable criteria/weights, conflict declarations, background/reference checks, comparative evaluation, consolidated results, and HRMPSB meeting/minutes/resolution/supporting documents. Ranking formulas must be versioned and tied to the approved Merit Selection Plan.

The actual repositories support that intent with these architecture decisions:

1. Phase 5C is committed and ends with immutable screening evidence and a final `QUALIFIED` or `DISQUALIFIED` outcome. Only the current, final, non-withdrawn `QUALIFIED` screening/application chain may be admitted to Phase 5D.
2. Phase 5A already preserves the exact vacancy, publication, Qualification Standard, Position Competency Profile, position, Plantilla, and salary snapshots. Phase 5B preserves applicant-owned submission evidence. Phase 5D references and snapshots those versions; it does not query another module's database or rewrite upstream evidence.
3. PrimeHR has no complete committee/HRMPSB authority model. Phase 5D must add an effective-dated, versioned committee foundation rather than treating an Administrative permission ruleset, employee role text, or frontend link as HRMPSB appointment.
4. Administrative already exposes fail-closed effective permissions with `canAssess`, `canValidate`, `canFinalize`, and data scope in addition to Access/Add/Edit/Delete/Publish/Submit/Approve. Phase 5D will reuse that contract and will not add a parallel authorization system.
5. The existing Phase 3 employee competency assessment model includes panel and BEI method names, but its subject is an employee person profile. It is not an applicant examination/HRMPSB aggregate and must not be reused in a way that mixes employee and applicant records. Its authorization, immutable-rating, audit, and optimistic-locking patterns may be reused.
6. Applicant and employee identities remain separate. HRMPSB members are authoritative employees resolved through the existing HR/Administrative integration; applicants remain Careers identities.
7. Existing private local/S3-compatible document storage can be generalized behind the same secured storage abstraction for staff evidence. Phase 5D must use separate metadata and authorization for committee evidence and must not expose storage keys or public URLs.
8. PrimeHR owns the Phase 5D policy, committee, proceeding, assessment, deliberation, and audit records. Administrative remains the permission and employee/reference authority; HRM remains the employee/appointment authority. No direct cross-domain database access is allowed.
9. Existing migrations end at V17. Phase 5D begins with paired PostgreSQL and SQL Server V18 migrations and never edits an applied migration.

## 3. Generic Merit Selection Plan evaluation policy

No agency-specific score, weight, passing mark, tie rule, panel size, schedule, committee roster, or qualification judgment will be seeded as a universal rule. An authorized agency user must publish an effective, immutable evaluation policy version tied to the agency's approved Merit Selection Plan reference.

The policy supports ordered stages such as:

- `WRITTEN_EXAMINATION`;
- `TECHNICAL_SKILLS_TEST`;
- `COMPETENCY_BASED_INTERVIEW`;
- `BEHAVIORAL_EVENT_INTERVIEW`;
- `BACKGROUND_REFERENCE_CHECK`;
- `OTHER_CONFIGURED`.

Each stage is one of:

- `SCORED`: normalized to a policy-defined scale and included in the consolidated score;
- `GATE`: a human-validated pass/fail or eligibility result with no invented score;
- `INFORMATIONAL`: recorded as evidence but does not alter the total.

Each published policy records:

- policy code, name, Merit Selection Plan reference, definition version, effectivity, and supersession lineage;
- ordered stages and criteria with public/internal labels, instructions, score bounds, weights, required evidence, minimum raters, and completion rules;
- allowed result-entry and validation roles;
- stage and overall rounding scale/mode;
- missing-result handling, which defaults to blocking consolidation and never silently treats missing work as zero;
- an explicit tie rule such as competition rank or dense rank;
- applicant disclosure rules limited to safe statuses/schedules in this phase.

Scored-stage weights must total exactly 100 percent. Scored criteria within a stage must total exactly 100 percent unless that stage deliberately uses a single direct normalized score. Gate and informational stages have zero score weight. Score bounds, passing rules, rater counts, aggregation mode, rounding, and tie behavior are validated before publication.

The engine will support a bounded set of transparent calculation primitives, initially normalized weighted sum and arithmetic mean of independently submitted panel ratings. It will not execute scripts, SQL, SpEL, JavaScript, AI prompts, or arbitrary formulas stored in the database. A future agency formula outside the approved primitives requires a separately reviewed extension, not a hidden client-specific branch.

Published policies are immutable:

```text
DRAFT -> PUBLISHED -> SUPERSEDED
```

A successor has a later effectivity date and preserves the previous definition. A vacancy publication is bound to one exact published evaluation policy before an evaluation proceeding opens. The binding cannot be replaced after a proceeding or candidate record exists.

## 4. HRMPSB committee governance

Phase 5D adds a reusable committee foundation inside PrimeHR, with the first supported committee type `HRMPSB`. It is deliberately narrower than a generic BPM engine.

Committee definition lifecycle:

```text
DRAFT -> PUBLISHED -> SUPERSEDED
```

A committee version records its name, type, legal/policy basis, membership/effectivity period, definition version, and supersession lineage. Members reference exact employee numbers and have effective-dated roles:

- `CHAIRPERSON`;
- `VICE_CHAIRPERSON`;
- `MEMBER`;
- `SECRETARIAT`;
- `ALTERNATE`;
- `OBSERVER`.

The authoritative employee integration must confirm that a proposed member exists and is active when appointed. Historical participation remains readable after membership expires, but an expired or future appointment grants no access to a new proceeding. Alternates act only when explicitly activated for the proceeding. Observers are read-only and cannot rate, vote, validate, or finalize.

Publishing validates required roles and non-overlapping active appointment periods according to the configured committee policy. It does not infer legal membership from the employee's general position, role string, permission ruleset, or prior participation.

## 5. Evaluation proceeding and candidate admission

One proceeding represents the Phase 5D evaluation of one exact Phase 5A vacancy publication using one exact evaluation policy and one exact HRMPSB committee version.

Proceeding lifecycle:

```text
DRAFT -> OPEN -> IN_ASSESSMENT -> FOR_DELIBERATION -> FINALIZED
   \-> CANCELLED before finalization with authority, reason, and audit
```

Rules:

1. Creation snapshots the vacancy/publication/QS/position-profile sources, evaluation policy, committee roster, and authoritative target metadata.
2. A coordinator admits applications individually or as an explicitly reviewed batch. Admission requires the current Phase 5C case and application to be final `QUALIFIED`, non-withdrawn, for the same publication, and not already admitted.
3. Each candidate snapshot identifies the exact application, applicant, submission version, screening case/revision/outcome, and admission actor/time. Later upstream corrections do not silently rewrite the proceeding; they create a visible source-freshness conflict requiring an authorized resolution or a controlled successor proceeding/candidate revision.
4. Opening freezes the admitted candidate set and exact policy/committee sources. Late admission requires an audited reopen action before any assessment result exists; after results exist, a successor proceeding is required.
5. Candidate withdrawal is recorded without deleting assessments already performed. It excludes the candidate from future consolidation and preserves the historical reason/evidence.
6. Cancellation, reopen, replacement, and source-conflict handling use explicit legal transitions, reasons, optimistic versions, and audit.

## 6. Schedules, examinations, tests, and interview execution

Phase 5D records administration and results; it is not an online testing platform.

For each configured stage, authorized staff can create sessions with date/time, timezone, venue or approved remote mode, instructions, capacity, responsible staff/panel, and status:

```text
DRAFT -> SCHEDULED -> COMPLETED
   \-> CANCELLED or RESCHEDULED with reason and history
```

Candidate session assignments preserve invitation and attendance states such as `INVITED`, `CONFIRMED`, `ATTENDED`, `NO_SHOW`, `EXCUSED`, and `WITHDRAWN`. Rescheduling creates history and applicant-safe communication; it does not overwrite the earlier schedule silently.

Examination and technical/skills test results record raw score, maximum score, normalized score, pass/fail where the policy requires it, examiner/recorder, evidence reference, remarks, and independent validation. A raw result cannot be changed after validation; correction uses a superseding revision with reason and audit.

Interview/BEI panels record criterion-level ratings and remarks independently per panel member. A member cannot view another member's unsubmitted rating or have their score overwritten by the chair/secretariat. Submission freezes that member's rating; a returned correction creates an audited new revision.

Phase 5D excludes question banks, answer sheets, browser-based exam delivery, automatic marking of applicant answers, plagiarism detection, biometric/remote proctoring, video recording, and third-party assessment-provider integration.

## 7. Conflict of interest and separation of duties

Every activated HRMPSB/panel participant must make a proceeding declaration before accessing confidential evaluation evidence or acting. Candidate-specific conflicts can then be declared as discovered.

Declaration outcomes are:

- `NO_CONFLICT`;
- `POTENTIAL_CONFLICT_REVIEW_REQUIRED`;
- `CONFLICT_RECUSED`.

A conflicted/recused member cannot access non-minimal evidence, rate, validate, deliberate, or vote for that candidate. A potential conflict blocks action until the chair/authorized governance officer records a resolution. The conflicted member cannot resolve their own declaration. Replacement/alternate activation is explicit and audited.

Additional separation-of-duties rules:

1. Panel members submit only their own ratings.
2. Secretariat may schedule, record attendance, and prepare minutes but cannot fabricate or edit another member's rating.
3. The person who records an examination result cannot independently validate that same result.
4. A validator cannot alter a raw result; they validate, return, or reject it with reason.
5. Consolidation is deterministic from validated inputs. A finalizer cannot directly type a different total or rank.
6. Administrator override is limited to an exceptional, explicit transition or validated-result correction with administrator authority, exact action permission, reason, prior/new state, and audit. It cannot create a false panel rating or silently bypass conflict rules.
7. HRMPSB recommendation remains separate from the Phase 5E appointing-authority selection decision.

## 8. Background/reference checking and evidence

Background/reference checking is a configured stage and remains human-controlled. It records lawful basis or applicant consent reference, check type, source category, requested/completed dates, authorized checker, structured outcome, applicant-safe/public-safe text where applicable, confidential internal notes, and secured evidence metadata.

No external data broker, social-media search, criminal-record integration, email/SMS provider, authenticity claim, or automatic disqualification is included. A check can affect eligibility or scoring only when the published policy explicitly defines the rule and an authorized human validates the finding.

Staff evidence and HRMPSB supporting documents use the existing provider-abstracted private storage service with new Phase 5D metadata, ownership, content-type/size controls, retention tags, and authorized streaming. Meeting minutes, resolutions, signed rating sheets, and check evidence are never exposed by a public URL or applicant API.

## 9. Comparative evaluation and HRMPSB deliberation

Consolidation requires every mandatory gate and scored stage to have complete, validated results and the configured minimum number of independent ratings. It produces an immutable calculation snapshot containing each input revision, normalized stage result, weight, rounding step, total, tie handling, exclusions, and formula/policy version.

The UI/API presents a transparent comparative matrix. It may calculate rank according to the published policy, but rank is decision support and is never an automatic appointment or selection.

An HRMPSB meeting records schedule, agenda, participants, attendance/quorum evidence, deliberation status, minutes, supporting-document references, and resolution. Candidate-specific resolution entries may record `ENDORSED_FOR_SELECTION_DECISION`, `NOT_ENDORSED`, or `DEFERRED`, with reason and vote/consensus metadata permitted by the published policy. Finalization requires the authorized HRMPSB role, complete conflict declarations, complete validated evaluation evidence, and a reasoned resolution.

Finalization freezes the comparative matrix, minutes/resolution metadata, candidate recommendations, actors, timestamps, and audit trail. A correction is a controlled successor deliberation/revision; it never erases the original result.

The appointing authority's selection among or outside the HRMPSB recommendation, together with its legal reason and notifications, belongs exclusively to Phase 5E.

## 10. Authorization model

Add three exact Administrative feature rows.

### `primehr.rsp-evaluation-policy`

- Access: list/detail permitted policy versions;
- Add: create draft/successor policies;
- Edit: update drafts;
- Publish: publish immutable policy versions and bind one to a publication;
- no Delete, Submit, Approve, Assess, Validate, or Finalize.

### `primehr.hrmpsb-governance`

- Access: list/detail permitted committee versions and assigned proceedings;
- Add: create draft committee versions, meetings, and assignments;
- Edit: update drafts, schedules, attendance, minutes, and supporting evidence;
- Publish: publish an effective immutable committee roster;
- Finalize: finalize an HRMPSB meeting/resolution when the actor also has an eligible effective proceeding role;
- no destructive Delete; supersession/cancellation preserves history.

### `primehr.rsp-candidate-evaluation`

- Access: list/detail assigned proceedings and minimally necessary candidate data;
- Add: create proceedings, admit qualified candidates, create sessions, and assign authorized examiners/panel members;
- Edit: manage schedules, attendance, result drafts, reference checks, and returned corrections within the actor's assignment;
- Assess: record the actor's examination/test/interview/check assessment;
- Submit: submit the actor's own completed result/rating;
- Validate: independently validate/return examination, test, and check results;
- Finalize: generate/freeze an authorized comparative evaluation after every prerequisite passes;
- no selection, appointment, or employee action.

Policy and committee administration require `AGENCY_WIDE` scope under the current single-agency configuration. Proceeding reads/actions additionally require an active committee/process assignment, action permission, current role/effectivity, conflict clearance, and eligible state. Administrator status does not silently make the actor an HRMPSB member; exceptional override paths are named, reasoned, and audited.

Applicant access remains ownership-based through Careers and never uses these staff feature permissions.

## 11. Persistence and migrations

Proposed forward-only, provider-equivalent migrations follow the existing `${primehrSchema}` convention.

### Phase 5D.1 / V18 - policy, committee, and proceeding foundation

- `rsp_evaluation_policy`;
- `rsp_evaluation_policy_stage`;
- `rsp_evaluation_policy_criterion`;
- `rsp_publication_evaluation_policy`;
- `prime_committee`;
- `prime_committee_member`;
- `rsp_evaluation_proceeding`;
- `rsp_evaluation_candidate`.

Important constraints/indexes:

- agency + normalized code + definition version uniqueness for policy and committee;
- immutable supersession/effectivity/status consistency;
- unique stage and criterion code/display order within their owner;
- valid score bounds, stage/criterion modes, weights, rater counts, rounding, and tie rules;
- one exact evaluation-policy binding per vacancy publication;
- one current proceeding per publication unless an explicit successor exists;
- one candidate per current proceeding/application and only one current candidate revision;
- foreign keys to the exact Phase 5A publication and Phase 5B application/Phase 5C case records owned by PrimeHR;
- agency/status/effectivity/publication/member/application queue indexes;
- optimistic `record_version`, audit metadata, and source snapshots.

### Phase 5D.2 / V19 - execution, deliberation, and consolidation

- `rsp_evaluation_session`;
- `rsp_evaluation_session_candidate`;
- `rsp_evaluation_assignment`;
- `rsp_conflict_declaration`;
- `rsp_stage_result`;
- `rsp_panel_rating`;
- `rsp_panel_rating_item`;
- `rsp_reference_check`;
- `rsp_evaluation_evidence`;
- `rsp_hrmpsb_meeting`;
- `rsp_hrmpsb_attendance`;
- `rsp_hrmpsb_resolution`;
- `rsp_comparative_evaluation`;
- `rsp_comparative_evaluation_item`.

Important constraints/indexes:

- no duplicate active session assignment, panel assignment, declaration, attendance, result, or rating for the same scoped subject;
- submitted/validated/finalized-state metadata consistency;
- score bounds and exact policy criterion ownership;
- independent recorder/validator and member-owned rating constraints enforced in services plus database invariants where portable;
- immutable result/rating/consolidation revision lineage;
- unique finalized comparative evaluation revision per proceeding;
- private evidence ownership and storage metadata without database-provider file paths;
- assignee, candidate, session, stage, status, date, and meeting indexes.

SQL Server and PostgreSQL scripts must be structurally equivalent. Shared code uses JPA/JPQL/derived queries, `Pageable`, `BigDecimal`, Java time, explicit rounding, and service-layer calculation. No `TOP`, `LIMIT`, provider casts/functions, native JSON operators, database file access, or cross-database joins are allowed in shared production logic.

## 12. Proposed REST contract

Exact request/response DTO names will follow the existing `/api/primehr/v1/**` conventions.

### Policy and binding

```http
GET  /api/primehr/v1/rsp/evaluation-policies
POST /api/primehr/v1/rsp/evaluation-policies
GET  /api/primehr/v1/rsp/evaluation-policies/{policyId}
PUT  /api/primehr/v1/rsp/evaluation-policies/{policyId}
POST /api/primehr/v1/rsp/evaluation-policies/{policyId}/publish
POST /api/primehr/v1/rsp/evaluation-policies/{policyId}/successors
PUT  /api/primehr/v1/rsp/vacancy-publications/{publicationId}/evaluation-policy
```

### Committee governance

```http
GET  /api/primehr/v1/committees
POST /api/primehr/v1/committees
GET  /api/primehr/v1/committees/{committeeId}
PUT  /api/primehr/v1/committees/{committeeId}
POST /api/primehr/v1/committees/{committeeId}/publish
POST /api/primehr/v1/committees/{committeeId}/successors
GET  /api/primehr/v1/committees/member-candidates
```

### Proceedings, sessions, and assessments

```http
GET  /api/primehr/v1/rsp/evaluation-proceedings
POST /api/primehr/v1/rsp/evaluation-proceedings
GET  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}
PUT  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}
PUT  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/candidates
POST /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/open
POST /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/cancel
POST /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/sessions
PUT  /api/primehr/v1/rsp/evaluation-sessions/{sessionId}
POST /api/primehr/v1/rsp/evaluation-sessions/{sessionId}/schedule
POST /api/primehr/v1/rsp/evaluation-sessions/{sessionId}/complete
PUT  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/assignments
PUT  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/conflicts/{candidateId}
PUT  /api/primehr/v1/rsp/evaluation-sessions/{sessionId}/candidates/{candidateId}/result
POST /api/primehr/v1/rsp/stage-results/{resultId}/submit
POST /api/primehr/v1/rsp/stage-results/{resultId}/validate
POST /api/primehr/v1/rsp/stage-results/{resultId}/return
PUT  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/candidates/{candidateId}/ratings
POST /api/primehr/v1/rsp/panel-ratings/{ratingId}/submit
PUT  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/candidates/{candidateId}/reference-checks
```

### Comparative evaluation and deliberation

```http
POST /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/comparative-evaluations
GET  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/comparative-evaluations/{evaluationId}
POST /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/meetings
PUT  /api/primehr/v1/rsp/hrmpsb-meetings/{meetingId}
POST /api/primehr/v1/rsp/hrmpsb-meetings/{meetingId}/evidence
POST /api/primehr/v1/rsp/hrmpsb-meetings/{meetingId}/finalize
GET  /api/primehr/v1/rsp/evaluation-proceedings/{proceedingId}/history
```

Applicant-owned APIs expose only safe schedule/invitation and progress information. All staff commands use explicit DTO validation, authenticated actor/agency resolution, exact feature/action/data-scope/process-role/conflict/state checks, optimistic versions, transactions, idempotent transition handling, audit, and safe problem responses.

## 13. UI and reporting scope

Administrative UI:

- add the three exact Phase 5D permission rows with only their supported action columns;
- do not add agency-specific scores, committee members, or Merit Selection Plan defaults to source code.

PrimeHR staff UI:

```text
/prime-hr/evaluation-policies
/prime-hr/hrmpsb-committees
/prime-hr/examinations-interviews
/prime-hr/hrmpsb-deliberations
/prime-hr/comparative-evaluation
```

The UI includes policy/committee version administration, qualified-candidate admission, schedules and attendance, assignments and conflicts, examination/test results, member-owned interview/BEI ratings, validation/correction, background/reference checks, transparent calculation breakdown, comparative matrix, meetings/minutes/evidence, recommendation/resolution, audit/history, source freshness, stale conflict, denied, empty, and dependency-error states.

Careers applicant UI:

- show only the applicant's safe examination/interview schedule, reschedule/cancellation, attendance confirmation where enabled, and broad `ASSESSMENT SCHEDULED`, `ASSESSMENT IN PROGRESS`, or `ASSESSMENT COMPLETED` progress;
- do not expose raw/internal scores, panel identity, individual ratings, conflict declarations, reference-check details, internal remarks, other candidates, comparative rank, minutes, resolution, or recommendation in Phase 5D.

No Employee Portal change is required.

No Jasper report is included. Formal comparative-evaluation reports, process evidence indexes, funnel/demographic analytics, and RSP reporting belong to Phase 5F. Authorized Phase 5D screens can render one proceeding's evidence but cannot export a report that bypasses Phase 5F privacy/report approval.

## 14. Controlled implementation slices and gates

### Phase 5D.1 - Evaluation policy, committee, and proceeding foundation

- V18 provider-equivalent policy/stage/criterion, publication binding, committee/member, proceeding/candidate schema;
- versioned policy and committee lifecycle;
- exact qualified-candidate admission and immutable source snapshots;
- proceeding lifecycle through opening, cancellation, and source conflict only;
- REST/OpenAPI, permission guards, audit, repositories, services, DTOs, and focused tests;
- backend only: no result/rating execution, deliberation, UI, Jasper, selection, appointment, or onboarding.

Gates:

- policy weights/bounds/rounding/tie/effectivity/version/immutability tests;
- committee role/effectivity/employee-eligibility/supersession tests;
- exact permission/action/data-scope/process-role denied/allowed tests;
- qualified/same-publication/non-withdrawn admission, duplicate, source-freshness, stale-write, cancellation, and immutable-snapshot tests;
- SQL Server fresh V1-V18 and populated V17-to-V18 migration tests;
- PostgreSQL-mode Flyway/Hibernate V1-V18, structural parity, and provider-neutral query/calculation audit; live PostgreSQL remains non-blocking by current user direction;
- affected clean test/package, OpenAPI, secret/generated-file, and Phase 5E+ boundary audits.

### Phase 5D.2 - Assessment execution, deliberation, and comparative result

Begins only after every Phase 5D.1 gate passes.

- V19 sessions/candidates/assignments/conflicts/results/ratings/checks/evidence/meetings/resolutions/comparative schema;
- scheduling, attendance, result entry, independent validation/correction, member-owned interview ratings, conflict recusal/replacement, background/reference checks;
- deterministic consolidation, transparent comparison/ranking, HRMPSB meeting/minutes/resolution, and immutable recommendation;
- applicant-safe schedule/status communication backend;
- backend only: no Administrative/PrimeHR/Careers UI, Jasper, selection, appointment handoff, employee creation, or onboarding.

Gates:

- complete lifecycle and illegal-transition matrix for sessions, results, ratings, meetings, and proceedings;
- missing result is not zero; bounds/weights/rounding/tie/minimum-rater/gate-stage deterministic calculation tests;
- member-owned rating privacy, independent validation, SOD, effective membership, alternate, observer, conflict/recusal, quorum, and assignment tests;
- admission ownership, applicant withdrawal, reschedule/no-show, source conflict, correction lineage, immutable finalization, idempotency, optimistic conflict, rollback, and audit tests;
- applicant-safe disclosure and confidential-data non-disclosure tests;
- secured evidence ownership/content controls and storage failure rollback tests;
- SQL Server fresh V1-V19 and populated V18-to-V19 migration tests;
- PostgreSQL-mode/parity, affected clean test/package, OpenAPI, secret, and Phase 5E+ boundary audits.

### Phase 5D.3 - Administrative controls, staff/applicant UI, and Playwright

Requires separate approval after Phase 5D.2 passes.

- Administrative permission controls;
- PrimeHR policy, committee, examination/interview, deliberation, and comparative-evaluation UI;
- Careers applicant-safe schedule/progress UI;
- repeatable real SQL Server Playwright for policy/committee versioning, qualified admission, schedules, assigned/conflicted/expired/observer RBAC, independent results/ratings, return/correction, reference checks, deterministic consolidation/ties, meeting/quorum/resolution, stale conflicts, applicant-safe disclosure, evidence protection, and full regression;
- user/operator guide, E2E guide, progress update, and final Phase 5D review manifest.

No Jasper report is included, and work stops before Phase 5E.

## 15. Acceptance criteria

Phase 5D is complete only when:

1. Every proceeding uses exact immutable vacancy, applicant, screening, evaluation-policy, and committee versions.
2. Only current qualified, non-withdrawn applications for the exact publication can be admitted.
3. Policies and ranking formulas are versioned, effective-dated, transparent, bounded, and tied to the Merit Selection Plan reference.
4. Committee authority comes from an effective published membership version plus proceeding assignment, not a role string or UI link.
5. Conflict declarations, recusals, alternates, individual ratings, validation, quorum, minutes, evidence, and resolution are complete and auditable.
6. Missing/incomplete results block consolidation and are never silently treated as zero.
7. Every total and rank can be reproduced from immutable validated inputs using explicit `BigDecimal` rounding and tie rules.
8. Unauthorized, unassigned, expired, observer, conflicted, or stale actors cannot read or mutate protected records.
9. Applicants see only their own safe schedule/progress; confidential checks, ratings, comparisons, and other applicants remain private.
10. Final comparative evaluation and recommendation are immutable; correction preserves prior revisions.
11. Real SQL Server and full Playwright gates pass; PostgreSQL portability remains provider-neutral and truthful about any live-provider non-run.
12. No automatic selection, appointment, employee creation, onboarding, Phase 5F Jasper/report, or other Phase 5E+ behavior exists.

## 16. Explicit exclusions

- appointing-authority selection, selected/not-selected decision, shortlist approval, or non-selection notification;
- appointment document generation/handoff, Oath of Office, assumption-to-duty, appointment status, pre-employment, employee/account creation, or onboarding;
- online examination delivery, question bank, answer capture, automatic marking, plagiarism detection, biometric/remote proctoring, or video conferencing/recording;
- external assessment vendor, reference/background data broker, email/SMS provider, external calendar, or job-board integration;
- AI/NLP applicant scoring, résumé ranking, facial/emotion analysis, automated equivalence/authenticity/background judgment, or opaque recommendation;
- unrestricted workflow/BPM or organization-wide committee suite beyond the reusable minimum needed by HRMPSB;
- client-specific Merit Selection Plan weights, panel composition, quorum, score, passing, tie, retention, or disclosure rules hard-coded as defaults;
- destructive deletion of policies, committees, proceedings, ratings, checks, minutes, resolutions, evidence, or comparative results;
- RSP Jasper reports, comparative-evaluation PDF, evidence index, analytics, funnel/demographic report, or dashboard;
- direct writes to Administrative, HRM, Timekeeping, Payroll, or Employee Portal data.

## 17. Known risks and decisions accepted by approval

1. The generic engine deliberately supports bounded transparent formula primitives, not arbitrary executable agency formulas. An unsupported client MSP formula requires a separately reviewed extension.
2. HRMPSB membership is modeled in PrimeHR because no authoritative committee source exists. Employee identity remains authoritative outside PrimeHR and is referenced, not duplicated as a new employee.
3. Current trusted agency resolution is single-agency and policy/committee administration is agency-wide. Proceeding assignment and conflict controls provide the narrower operational boundary until multi-agency/organizational authority exists.
4. Committee governance is reusable but initially limited to the HRMPSB concepts required by Phase 5D; it is not a complete workflow engine for PMT, PRAISE, or every agency committee.
5. The agency must configure and approve its real Merit Selection Plan policy, roster, quorum, scoring, tie, disclosure, lawful background-check, retention, and applicant-notice wording before production use.
6. A background/reference check records authorized human work; it does not claim external truth, legal sufficiency, or automatic disqualification.
7. Applicant score/rank disclosure is intentionally excluded from Phase 5D's safe portal contract. A legal/policy requirement to disclose it needs an explicit configured and reviewed extension.
8. Live PostgreSQL remains a non-blocking gate under the user's SQL Server-primary direction, but equivalent migrations, structural parity, and provider-neutral shared code remain mandatory.
9. Phase 5D is materially larger than Phase 5C. Keeping backend foundation, execution, and UI acceptance as separate gates is required to contain risk.

## 18. Approval gate

Recommended approval wording:

> Approve Phase 5D as defined. Proceed with Phase 5D.1, and continue to Phase 5D.2 only after all Phase 5D.1 gates pass. Stop before Phase 5D.3 until I approve the Administrative controls, staff/applicant UI, and Playwright acceptance.

No Phase 5D implementation may start before explicit approval. Phase 5E and later remain separately gated.
