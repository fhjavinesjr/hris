# ISOFT PRIME-HRM User Guide

This guide covers the delivered standalone PRIME-HRM functions through Phase 6E.3: competency management, RSP, SPMS planning/monitoring/coaching, deterministic self/supervisor ratings, PMT calibration, immutable finalization, acknowledgment, appeal, and intervention/PIP backend controls. Phase 6E rating interfaces and later reporting are not yet implemented.

## 1. Access and sign-in

1. Sign in to the Employee Portal.
2. Select **PRIME-HRM** from the portal sidebar.
3. The portal opens PRIME-HRM through SSO; a second login should not be required.
4. Select **Employee Portal** in PRIME-HRM to return.

If **Unable to sign in** appears, confirm that the Administrative, HRM login, PrimeHR API, and PrimeHR UI services are running and their configured URLs/CORS origins are correct. If **Access denied** appears, the signed-in employee's Administrative ruleset does not grant the requested PRIME-HRM feature.

## 2. Permissions and responsibilities

Administrative permission rules independently control:

- **Access**: open and read the feature;
- **Add**: create drafts and successor versions;
- **Edit**: update draft content and requirements;
- **Delete**: archive drafts or draft requirements;
- **Publish**: publish competency foundation drafts;
- **Submit**: submit a Position Profile for approval;
- **Approve**: return or approve a submitted Position Profile;
- **Assess**: enter ratings/evidence for an explicitly assigned assessment contribution;
- **Validate**: return a completed assessment case or make the human-validated final decisions;
- **Finalize**: close an assessment cycle after its controlled work is complete;
- **Data Scope**: restrict records to `OWN_RECORDS`, `ASSIGNED_RECORDS`, or `AGENCY_WIDE`;
- **Portal**: display the PRIME-HRM link in Employee Portal.

Access is required for every other action. An ordinary submitter cannot approve their own submission. An administrator can override that separation only with an explicit reason, which is audited. Hiding a button is not the only control; the backend also enforces authorization.

Recommended role separation:

- competency administrator: maintains categories, scales, and competency definitions;
- profile submitter: creates and submits Position Profiles;
- profile approver: independently returns or approves submissions;
- system administrator: configures permissions and uses override only for an authorized exception.

## 3. Competency foundation

Position Profiles can use only published, effective competency definitions and their exact published proficiency levels.

### Categories

Use **Categories** to group related competencies. Create a draft with a unique code, name, description, display order, and effectivity. Publish the completed draft. Use **New version** for later changes; do not alter published history.

### Proficiency scales and levels

Use **Proficiency Scales** to define an agency scale and its levels. A level contains a code, name, description, display order, and active state. Effectivity belongs to the scale version, not each individual level. Publish only after the complete level structure is correct.

### Competencies and behavioral indicators

Use **Competencies** to create a definition linked to a published category and scale. Add behavioral indicators for the applicable levels. Publish after validation. Published definitions are immutable; use **New version** for revisions.

## 4. Position Competency Profiles

Open **Position Profiles** to search, create, review, compare, and resolve profiles.

### Filters

- **Status** filters DRAFT, SUBMITTED, ACTIVE, or archived records.
- **Target type** filters Job Position or Plantilla profiles.
- **Search** matches the profile/target text.
- Select **Load** to apply the filters.

### Create a draft

1. Select **New Draft**.
2. Choose target type:
   - **Job Position** provides the default profile for that position.
   - **Plantilla** provides a narrower profile for one Plantilla item.
3. Search and select the authoritative Administrative target.
4. Enter the profile name, description, effective-from date, and optional effective-to date.
5. Save the draft.

PRIME-HRM stores authoritative IDs and a historical target snapshot. Job Position and Plantilla master data remain owned by Administrative and cannot be edited here.

### Add competency requirements

For each requirement:

1. select a published competency version;
2. select a valid level from that competency's exact scale version;
3. choose **MANDATORY** or **DESIRABLE**;
4. optionally enter an agency criticality code and remarks;
5. set display order and save.

A profile cannot contain the same competency version twice. A complete submission requires an effective-from date and at least one active valid requirement.

### Readiness and source freshness

The details panel shows the stored target snapshot and the current Administrative source:

- **Current / matches** means the authoritative target still matches the snapshot.
- **Changed** means Administrative master data changed after the snapshot. Historical data remains unchanged; review and refresh the draft or create an appropriate successor.
- A dependency error means Administrative could not verify the target. Do not approve until connectivity and source validity are restored.

## 5. Submit, return, resubmit, and approve

Lifecycle:

```text
DRAFT -> SUBMITTED -> ACTIVE
                  -> DRAFT (returned)
```

### Submitter

1. Review completeness and source freshness.
2. Select **Submit**.
3. Confirm the record becomes **SUBMITTED** and an audit entry is present.

Submitted content is locked. If an approver returns it, review the recorded reason, edit the draft, and submit again.

### Approver

1. Open the submitted profile and verify target, effectivity, snapshot, requirements, levels, classification, and criticality.
2. Select **Return to Draft** when correction is required; a reason is mandatory.
3. Select **Approve** when correct.
4. Confirm status **ACTIVE**, approval actor/time, audit history, and any predecessor effective-to closure.

### Administrator override

Use self-approval only as an authorized exception. The system requires a nonblank reason and records an administrator-specific audit action. Ordinary approval should use a separate approver.

## 6. Active versions and successors

ACTIVE profiles are immutable. To revise one:

1. select **New Successor Version**;
2. choose an effective-from date after the predecessor period;
3. update copied requirements as needed;
4. submit and approve through the normal workflow.

On approval, the system closes the predecessor on the day before the successor starts. A successor cannot start on or before its predecessor's effective-from date, and effective periods cannot overlap.

## 7. Compare versions

1. select exactly two profile versions using **Compare** checkboxes;
2. select **Compare selected versions**;
3. review Added, Removed, Changed, and Unchanged counts and row details.

Comparison uses the exact selected historical competency and level versions. It does not substitute the latest definition.

## 8. Resolve an effective profile

In **Resolve Effective Profile**:

1. enter Job Position ID;
2. optionally enter Plantilla ID;
3. choose the **As of** date;
4. select **Resolve**.

When both IDs are supplied, an effective ACTIVE Plantilla profile takes precedence. If none applies, the effective Job Position profile is used. A date outside all effective periods returns a no-effective-profile message and clears any prior result.

## 9. Audit and concurrency

Audit History records create, update, submit, return, approve, publish, archive, and administrator override activity with actor, time, reason, and correlation when available.

If two users edit the same draft, the first valid save wins. A stale second save is rejected with an expected/current record-version message, the stale editor closes, and current server data reloads. Reopen the record and consciously reapply changes; do not bypass the conflict.

## 10. Assessment administration

Open **Assessment Administration** to prepare cycles and tools. This page requires agency-wide assessment-administration access.

1. Create a DRAFT cycle with a unique code, name, effectivity, and instructions.
2. Create a tool under that cycle and select one exact ACTIVE Position Profile.
3. Select one or more supported methods. `SELF_ASSESSMENT` assigns the subject to themselves; every other method requires an explicit employee assessor.
4. Add eligible subjects from HRM. Eligibility requires a current active appointment and an effective Position Profile resolved with Plantilla precedence and Job Position fallback.
5. Add explicit assessors where required. The subject cannot be their own non-self assessor.
6. Publish the complete tool, then open the cycle.
7. Close the cycle only after the intended assessment work is complete.

The HRM/Administrative source panels show the authoritative IDs and snapshots used. A dependency or freshness problem must be resolved before proceeding. PRIME-HRM never infers a supervisor from unrelated approval-workflow or personnel fields.

## 11. My Assessments

Open **My Assessments** to see only self contributions or work explicitly assigned to the signed-in employee.

1. Open an assigned item and read its exact tool instructions and position requirements.
2. Select an attained level from the competency's exact published scale.
3. Add remarks and observable-behavior notes, then select **Save Rating**.
4. When required, add structured evidence with type, official reference/title, evidence date, and description. Phase 3 stores references and text only; it does not upload binary files.
5. Rate every active requirement and satisfy evidence requirements.
6. Select **Submit Contribution** and confirm.

Submitted contributions are read-only. If a validator returns the case, the contribution becomes available for correction and resubmission. Other assessors' confidential contributions are not shown in this inbox.

## 12. Assessment validation

Open **Assessment Validation** to review cases for which every active contribution has been submitted.

1. Open a case and compare each contributor separately. The system does not average ratings or automatically decide the official result.
2. Select a human-validated final level for every competency and enter decision remarks where needed.
3. Set the official profile valid-from date and optional reassessment date.
4. Select **Return for Correction** with a mandatory reason when work is incomplete or unclear.
5. Otherwise select **Validate and Generate Profile** and confirm.

An ordinary validator cannot validate a case in which they contributed. An administrator may use the displayed override only for an authorized exception and must enter a mandatory audited reason. Successful validation atomically creates one immutable official Person Competency Profile version.

## 13. Person Competency Profiles

Open **Person Profiles** to view validated official results.

- `OWN_RECORDS` shows only the signed-in employee's profile.
- `AGENCY_WIDE` allows an authorized HR user to enter another Employee No.
- **Latest valid as of** resolves the profile effective on that date.
- **Immutable Version History** lists every retained official version and its predecessor lineage.

The badge **VALIDATED OFFICIAL PROFILE** distinguishes official human-validated results from self or assessor contributions. Official versions cannot be edited or deleted. A later validated assessment creates a successor and closes the earlier open period when applicable.

## 14. Qualification Standards

Qualification Standards are maintained in **Administrative > Qualification Standards** and belong to the authoritative Job Position master.

1. Select a Job Position.
2. Create a DRAFT containing education, training, experience, eligibility, optional license/statutory requirement, source/legal basis, and effectivity.
3. Review the draft and publish it with the dedicated **Publish** permission.
4. Use **New version** for a later change. Publishing a successor closes an overlapping predecessor; ACTIVE history is not edited in place.

The PrimeHR vacancy workflow will not treat manually retyped requirements as authoritative. It resolves the effective published Qualification Standard and snapshots its exact ID, version, content, fingerprint, and fetch time.

## 15. Recruitment planning and vacancy publication

Open **Recruitment Planning** in PRIME-HRM. The initial workflow requires `AGENCY_WIDE` data scope because authoritative office-assignment responsibility is not yet available.

### Prepare a recruitment plan

1. Create a plan code, title, planning period, and description.
2. Open the plan and find the exact Administrative Plantilla item.
3. Enter the authoritative Business Unit ID and choose:
   - **ACTUAL** only when HRM reports the exact Plantilla is unoccupied;
   - **ANTICIPATED** when it is still occupied, with anticipated date, reason, explanation, and authority/reference.
4. Enter priority, target fill date when known, and justification.
5. Select **Check Readiness**. Resolve every blocker before saving or submitting.
6. Submit each vacancy request, then submit the plan.
7. An independent approver reviews and approves/returns the plan, then authorizes or declines each submitted vacancy.

Readiness is checked from HRM Plantilla occupancy, the current Administrative Qualification Standard, and the effective Position Competency Profile. The browser cannot declare an occupied Plantilla to be an actual vacancy. Duplicate active vacancy requests for the same Plantilla and overlapping period are rejected.

### Prepare and publish a vacancy notice

1. From an **AUTHORIZED** vacancy, select **Create Publication**.
2. Set visibility, opening/closing dates, application instructions, contact/submission guidance, approved notice text, and at least one publication channel/date/reference.
3. Save and review the immutable Qualification Standard, position, organizational, salary, and competency snapshots.
4. Submit the publication for independent approval.
5. The approver may return or approve it. A separately authorized publisher then selects **Publish**.
6. For an **APPROVED** or **PUBLISHED** record, select **Vacancy Notice PDF** to generate the official portable notice.

Publication does not send data to CSC, social media, email, or external job boards. Channels are evidence records only. Phase 5A also does not accept applicants, documents, or applications.

Lifecycle summary:

```text
Plan:        DRAFT/RETURNED -> SUBMITTED -> APPROVED -> ARCHIVED
Vacancy:     DRAFT/RETURNED -> SUBMITTED -> AUTHORIZED | DECLINED
Publication: DRAFT/RETURNED -> SUBMITTED -> APPROVED -> PUBLISHED -> CLOSED
```

Cancellation and return actions require the applicable permission and reason. Material transitions recheck authoritative source freshness. A `409` message means source data or the record version changed; reload and review current facts before retrying.

## 16. Careers and applicant self-service

Careers is a separate public/applicant surface at `/careers`. It does not use an employee account, Employee Portal token, or PrimeHR staff session.

### Browse and register

1. Open **ISOFT HRIS Careers** and review open published vacancies.
2. Select a vacancy to review its position, salary, effectivity, application window, qualification summary, and publication details.
3. Select **Register**, enter the applicant's own email/name/password, read the displayed effective privacy notice, and explicitly accept that exact notice version.
4. Registration signs the applicant into the Careers surface. **Login** can be used for later sessions.

The generic release activates an account without email verification because no delivery provider is configured. Use a unique applicant email and protect the password. An applicant session cannot open employee or PrimeHR management APIs, and employee SSO cannot open applicant-owned APIs.

### Complete the profile

1. Open **My Profile**.
2. Complete contact and declaration fields.
3. Add the supported education, work experience, training, eligibility, licence/credential, or reference entries. Each entry has its own title, organization, dates, description, and display order where applicable.
4. Select **Save profile**.

This profile belongs to the applicant and is separate from the HRM employee PDS. A submission captures an immutable profile snapshot; later edits affect future submissions only.

### Manage private documents

1. Open **Documents**.
2. Select the document type/classification and a permitted file.
3. Select **Upload document**. The server validates the configured size, media type, content signature, checksum, ownership, and private storage settings.
4. Download, replace, or deactivate only the applicant's own active documents.

Replacement creates a new document version. It does not alter the file evidence already captured by a submitted application. File bytes are never public and are streamed only after applicant ownership or staff permission checks.

### Apply and track

1. From an open published vacancy, select **Apply**.
2. Create a draft, select the active supporting documents, and save the selection.
3. Review the declaration/readiness messages, then select **Submit application**.
4. Record the acknowledgment number and use **My Applications** to view the safe status and portal communication history.
5. Where allowed, enter a withdrawal reason and select **Withdraw application**. Withdrawal preserves the submitted evidence and history.

Only an open `PUBLISHED` vacancy accepts an application. A duplicate active application, stale record version, incomplete profile/declaration, missing required document, expired window, or changed vacancy returns a validation/conflict message and does not create a second accepted submission.

## 17. Applicant intake for authorized staff

Administrative permission rules use **PRIME-HRM > Applicant Intake**:

- **Access** permits agency-wide list/detail and authorized evidence reads.
- **Add** permits an informational portal message and is independent from Access.
- no Edit, Delete, Submit, Approve, Publish, screening, qualified/disqualified, scoring, ranking, shortlist, or selection action exists in Phase 5B.

After a permission change, sign in again through Employee Portal so the effective permission snapshot is refreshed. Open **Applicant Intake** in PrimeHR, filter/search the submission queue, select **Details**, review the immutable submitted evidence and communication history, and use **Send message** only for safe informational correspondence. Sensitive evidence downloads and staff messages are audited.

The staff intake page does not determine documentary completeness or qualification. Those actions require the independent Phase 5C Application Screening permission and assignment described below.

### Deployment controls

Before enabling applicant uploads outside a local QA environment:

- configure a durable `local` or private S3-compatible storage provider and root/bucket;
- configure a dedicated applicant JWT secret different from employee JWT signing material;
- publish approved privacy/retention text and file-type/size/required-document policy;
- configure exact deployed CORS origins and never use a wildcard;
- plan email verification/password reset, CAPTCHA/rate limiting, malware scanning, retention/legal hold, backup, and applicant support controls appropriate to the agency.

The application fails closed when applicant/storage functionality is enabled without required secure storage or token configuration.

## 18. Screening policies and application screening

Administrative permission rules use two independent PRIME-HRM rows:

- **Screening Policy**: Access lists/reads versions; Add creates drafts/successors; Edit changes drafts; Publish makes a validated version immutable. Agency-wide scope is required.
- **Application Screening**: Access lists permitted cases; Add opens cases/manages assignments; Edit records assigned findings; Submit sends an assigned screener recommendation; Approve lets the independently assigned validator return/finalize. Agency-wide scope and case assignment are both enforced.

After a permission change, sign out and return through Employee Portal so the effective permission snapshot is refreshed.

### Configure a screening policy

1. Open **Screening Policies** and create a draft code/name/effectivity.
2. Add ordered criteria. Use objective modes only for structured evidence; use **MANUAL_REVIEW** for relevance, equivalence, authenticity, or legal judgment.
3. Mark mandatory/disqualifying behavior and required remarks/evidence explicitly.
4. Add outcome-compatible reason codes with agency-approved applicant-safe wording.
5. Publish the complete version with a reason. Published versions are immutable; use **New version** for a successor.
6. Bind the exact published policy to the vacancy publication before opening its first screening case. An existing case prevents rebinding.

### Screen an application

1. Open **Application Screening** and identify the submitted application/current record version from Applicant Intake.
2. Assign two different, eligible employee numbers as screener and validator. The server verifies the employees and their exact effective permissions.
3. The assigned screener opens the case and reviews the immutable application and policy snapshots.
4. Record every criterion result, required remarks, and evidence declaration. Save each finding.
5. Choose a recommendation. `QUALIFIED` requires every mandatory/disqualifying finding to be supported. `DISQUALIFIED` requires a policy reason code, internal explanation, and applicant-safe reason.
6. Submit for independent validation. The validator either returns with a required reason or finalizes the supported outcome.

An administrator override is an exceptional action requiring administrator authority, Approve permission, and an explicit audited reason. It does not delete findings or history. Final records are immutable; use a controlled correction successor where authorized. A stale `recordVersion` receives HTTP 409 and cannot overwrite the accepted revision.

Careers displays only applicant-safe status and communication. It never exposes findings, internal explanations, policy instructions, staff identities, or audit data. `QUALIFIED`/`NOT QUALIFIED` is a screening result, not selection, appointment, or onboarding.

## 19. Phase 5D evaluation operations

Configure **Evaluation Policy**, **HRMPSB Governance**, and **Candidate Evaluation** independently in Administrative. Access does not imply action authority. Published policies and committees are immutable; create effective successors for future changes.

Authorized staff admit only current qualified applications, validate frozen source versions, schedule examination/interview sessions, record attendance, maintain eligible assignments, and observe candidate-specific conflict/recusal controls. Examination results follow independent submit/validation. Panel ratings remain member-owned until submitted, and lawful reference checks keep applicant-safe communication separate from confidential notes.

Generate comparative evaluation only after all mandatory validated inputs and minimum-rater requirements are satisfied. The calculation preserves policy versions, stage breakdowns, ties, exclusions, and a deterministic fingerprint. HRMPSB deliberation requires eligible attendance and quorum. Its resolution is only a recommendation and does not select or appoint anyone.

Careers exposes only the applicant's progress and approved schedule details. Never disclose scores, rankings, panel identities/ratings, conflicts, reference notes, minutes, evidence, or recommendations through applicant communication.

## 20. Common messages

- **Access denied**: ask an administrator to review the exact feature/action permissions, then sign in again through the portal.
- **Incomplete**: supply effective-from and at least one active, valid requirement.
- **Expected recordVersion ...**: another save changed the record; reopen and review current data.
- **Successor effectiveFrom must be after ...**: correct the successor dates so versions do not overlap.
- **No effective profile**: verify IDs, ACTIVE status, and the As-of date.
- **Administrative source changed**: review the current authoritative target before submitting/approving.
- **Dependency unavailable**: verify the configured service URL, service health, authentication, and CORS.
- **The required assessment action is not permitted**: review Access plus the exact Assess, Submit, Validate, or Finalize permission and data scope, then sign in again.
- **This assessment is not assigned to the current user**: use the account explicitly assigned to that contribution.
- **Every active competency requirement must have a rating**: finish all ratings before submission.
- **A successor validFrom must be after the latest profile validFrom**: choose a later non-overlapping official-profile effectivity date.
- **A validator cannot validate their own contribution**: use an independent validator or an authorized administrator override with an audited reason.
- **No effective Qualification Standard**: publish a version for the vacancy date in Administrative.
- **Plantilla is occupied**: use ANTICIPATED with complete evidence, or wait for HRM to show an actual vacancy.
- **This Plantilla already has an active vacancy request**: open the existing overlapping plan instead of creating a duplicate.
- **Source changed / reload before retrying**: an Administrative, HRM, profile, or optimistic record fingerprint changed; review current data.
- **Publication is not APPROVED or PUBLISHED**: a final vacancy notice cannot be generated yet.
- **No effective privacy notice**: an administrator must publish an active notice effective for the current date before applicant registration.
- **File content does not match its media type**: choose a genuine configured PDF/image/office file; renaming an extension is not accepted.
- **Application already exists**: open the existing application for that vacancy instead of creating a duplicate.
- **Vacancy is not open for applications**: verify that the publication is PUBLISHED and the application window includes the current date.
- **Applicant Intake access denied**: grant the exact Applicant Intake Access permission with agency-wide scope, then sign in again.
- **Screening Policy/Application Screening access denied**: grant the exact independent feature/action and agency-wide scope, then sign in again.
- **Screener and validator must be different**: assign independent eligible employees with the required actions.
- **No screening policy is bound**: publish and bind an effective policy before opening the case.
- **The screening record changed**: reload after another session saved; the stale write was rejected.
- **Reason code required/incompatible**: choose a published reason compatible with `DISQUALIFIED` and complete required internal/safe text.
- **Evaluation source changed**: reopen and review the current vacancy, policy, committee, application, and screening versions.
- **Conflict/recusal blocks this action**: obtain an authorized independent resolution or assign an eligible alternate.
- **Minimum raters/results not complete**: complete all mandatory submitted/validated inputs; missing results are not zero.
- **Quorum is not met**: record eligible attendance before beginning or finalizing deliberation.

## 21. Operational controls

### Phase 6A performance foundation

Administrative permission rules use three independent agency-wide rows:

- **Performance Policy**: Access reads policy history; Add creates drafts/successor revisions; Edit changes drafts; Publish publishes or retires immutable versions.
- **Performance Cycle**: Access reads cycles/calendars; Add creates drafts; Edit changes draft details/calendars; Finalize opens, closes, or cancels with a reason.
- **Performance Management Team**: Access reads PMT history; Add creates drafts; Edit changes draft details/rosters; Publish activates or deactivates with a reason.

After a permission change, sign in again so the effective ruleset is refreshed.

Create and publish the effective SPMS policy first. Then create a cycle tied to the exact published policy version and add its positively ordered required milestones before opening it. Published policy versions and non-draft cycle calendars are immutable; use an audited policy successor or later authorized correction workflow instead of overwriting history.

For PMT governance, enter the mandate/effectivity and an employee-number roster using only the controlled roles shown by the screen. Activation requires exactly one effective chairperson and verifies active employment through the authenticated HRM contract. If HRM is unavailable, no activation change is saved. Active rosters are immutable and deactivation preserves their history.

Phase 6A does not include performance templates, KRA/KPI definitions, OPCR/DPCR/IPCR commitments, monitoring, ratings, calibration, coaching, appeals, or reports. Those later capabilities remain approval-gated.

### Phase 6C performance planning and approval

Administrative permission rules separately control **Strategic Objectives**, **Plan Assignments**, **Office Performance Commitments**, and **Individual Performance Commitments**. Access is required before any action. Add, Edit, Publish, Submit, Approve, and Finalize remain independent; data scope restricts office work to assigned/agency records and IPCR work to own/assigned/agency records.

Use **Strategic Objectives** to create agency, area, or business-unit objectives against exact published policy and indicator versions. Check readiness before publishing. Published versions are immutable; create a successor revision instead of overwriting history.

Use **Plan Assignments** to bind an open cycle, published template, published objectives, authoritative subject, routing business unit, and owner. Review the displayed organization, participant, and route fingerprints before activation.

Use **Office Commitments** or **Individual Commitments** to generate a draft from an active assignment, complete targets and dates, configure valid cascade relationships, check readiness, and submit. Editing is available only for draft, amendment-draft, or returned records. Approval actions follow the stored route in order; the inbox shows only tasks assigned to the signed-in actor. Reload after a stale-write message rather than repeating an old decision.

Employees use **Employee Portal → Employee Self Service → My Performance Commitments** for their own IPCR records. The server derives ownership from the authenticated account; the page does not accept an employee number or ID. Employees may edit and submit only when their exact permission actions and lifecycle state permit it.

Phase 6C does not include accomplishments, monitoring, coaching, ratings, calibration, appeals, or reports. Those remain Phase 6D and require separate approval.

### Phase 6D.1 performance monitoring backend

An approved commitment with a completed approval route can open one monitoring case. The case keeps the exact commitment, policy, cycle, owner, organization, and approval-route fingerprints used when monitoring began. Opening the same commitment again is idempotent and does not create a duplicate case.

Employees record progress and actual accomplishments against an exact commitment item. Drafts may be edited, while submission freezes that revision. A returned update is corrected through a linked successor revision so the submitted history is preserved. The configured operational supervisor may accept an update or return it with visible feedback.

Evidence uploads use secured storage, generated object keys, checksums, MIME and size checks, and per-request download authorization. Storage locations are not returned to the browser. Evidence can be voided with a reason; workflow evidence is not physically deleted.

Final accomplishment submission checks every required commitment item and its evidence requirement. It freezes the accomplishment set for the later rating phase; a supervisor may return it for correction before rating begins. Progress percentages remain informational and do not calculate a rating.

Authorized supervisors and PMT users operate these records from **PrimeHR → Monitoring Inbox**. Employees use **Employee Portal → Employee Self Service → My Performance Monitoring**. The inbox and portal controls appear only when both the exact permission action and current lifecycle permit the operation.

### Phase 6D.2 coaching and mid-cycle backend

The assigned supervisor from the immutable commitment route creates a coaching session against the monitoring case and may link exact commitment items. Employee-visible guidance is stored separately from restricted supervisor notes. Issuing a session freezes that revision; a correction creates a linked successor rather than replacing history.

The employee can acknowledge receipt and add a response. Acknowledgment confirms receipt only and does not imply agreement or remove later review or appeal rights.

Coaching action items identify the accountable employee, due date, progress, completion, and supervisor verification. The accountable employee updates their own item. The assigned supervisor verifies completion or reopens it with a reason. Action items do not automatically create a disciplinary case, PIP, L&D referral, rating, or promotion record.

A mid-cycle review is available only when the exact published policy version requires it and the configured `MID_CYCLE_REVIEW` milestone is current. Its snapshot records monitoring-update count, open action count, and missing required evidence count. The supervisor submits the review and the employee acknowledges receipt. Closing without acknowledgment requires a reason, agency-wide permission, and effective PMT membership. An amendment recommendation points users to the existing formal Phase 6C amendment workflow and never changes a target directly.

Administrative manages `primehr.performance-monitoring` and `primehr.performance-coaching` separately. Access, Add, Edit, Submit, and Approve are independent, and data scope is limited to Own Records, Assigned Records, or Agency Wide. A hidden menu is not authorization; the server also verifies ownership, the immutable supervisor route, PMT process role where required, record state, and record version.

### Phase 6D.3 interface operation

In **Monitoring Inbox**, select a permitted case to review its approved commitment-item snapshot and progress history. Use the lifecycle buttons to accept or return submitted updates, return final accomplishments, issue or void coaching, verify or reopen completed action items, and submit or close a mid-cycle review. A stale-write response means another user changed the record; reload before deciding again.

In **My Performance Monitoring**, employees can work only on the case returned for their authenticated account. Create or revise drafts, submit progress, attach allowed evidence, correct returned work through a successor revision, and submit final accomplishments when required items and evidence are ready. Employees can see only employee-visible coaching text, may acknowledge receipt with a response, maintain their own assigned action items, and acknowledge a submitted mid-cycle review. There is no employee selector.

Monitoring percentages and accomplishments are informational inputs only. These screens do not calculate, approve, or publish a rating and do not create calibration, appeal, PIP, L&D, R&R, or promotion records. Those remain outside Phase 6D.

### Phase 6E.1 rating foundation backend

After the employee submits a complete accomplishment set, the exact assigned supervisor may open one rating case for that accomplishment revision during the configured `RATING_DUE` window. Opening snapshots the approved commitment, selected latest submitted/accepted item updates, evidence checksums, exact indicator/rubric versions, section/item weights, rating scale, policy, route, and organization fingerprints. The monitoring case becomes `READY_FOR_RATING`; later definition changes cannot alter the snapshot.

When the exact policy requires self-assessment, the employee may create and submit only their own `SELF` assessment. The stored supervisor may create and submit only the `SUPERVISOR` assessment, and supervisor submission is blocked until the required self-assessment is submitted. Drafts remain private to their author. A submitted correction creates a successor revision and preserves the original.

Users provide only a numeric actual for threshold rubrics or an exact permitted band for manual rubrics. The backend maps each dimension to exactly one rubric level, applies dimension, item, and section weights, rounds with the immutable rating-scale rule, and resolves exactly one overall band. Calculated scores, statuses, formula version, and fingerprints are server-owned. Phase 6E.1 does not expose a frontend yet and does not calibrate or publish a final rating.

### Phase 6E.2 PMT calibration and finalization backend

When the exact published policy requires calibration, an authorized effective PMT member opens a calibration case against the submitted supervisor assessment. The case snapshots the effective PMT roster and its revision. The rating subject, assigned supervisor, secretariat, technical-support, and non-voting members remain visible for traceability but cannot vote.

Calibration proposals change exact item/dimension actuals and require a rationale. They cannot type an overall score: the backend reruns the Phase 6E.1 calculation and preserves the original self/supervisor assessments beside the calibrated proposal. A strict majority of eligible, non-conflicted, independent voters approves or rejects the proposal; an otherwise eligible proposal author is excluded from that electorate. Every decision advances the optimistic record version, and retries with the same idempotency key return the existing result.

Finalization is allowed only in the configured `FINALIZATION_DUE` window and by an effective, non-conflicted voting PMT member with the exact Finalize permission. The proposal author cannot finalize their own calibrated result. Policies that do not require calibration use the submitted supervisor assessment directly. The resulting rating is immutable and retains its calculation fingerprint, exact source assessment, calibration lineage where applicable, finalizer, and time. Phase 6E.2 has no frontend yet and does not acknowledge, appeal, create an intervention/PIP, or feed other modules.

### Phase 6E.3 acknowledgment, appeal, and intervention backend

An employee may acknowledge receipt of their exact latest final-rating revision during the configured `ACKNOWLEDGMENT_DUE` window. Acknowledgment records receipt only and does not mean agreement. Closing without employee acknowledgment requires a reason, agency-wide Finalize authority, and an effective PMT member.

The authenticated employee may create and submit an appeal only for their own latest final rating during `APPEAL_DUE`. Evidence is stored securely and downloaded only after authorization. An effective, voting, non-conflicted PMT member with agency-wide approval authority decides the appeal; the subject and assigned supervisor cannot decide it. A modified decision reruns the authoritative server calculation and creates a linked successor final-rating revision without overwriting the original.

Intervention rules bind exact policy, scale, and rating-band versions to `NONE`, `ALERT`, `PIP_RECOMMENDED`, or `PIP_REQUIRED`. Finalization and modified appeals evaluate that mapping idempotently. PIP records objectives, support, accountable employee, dates, checkpoints, employee receipt, completion, and closure. Employee views receive employee-visible feedback but not restricted PMT notes. These backend operations do not create discipline, change CoreHR, or feed payroll, timekeeping, recruitment, L&D, R&R, or reporting.

### Phase 5E operational readiness

- Phase 5E.1-5E.4 workflows and UI are available only through their exact authorization boundaries.
- Publish one effective onboarding template before PrimeHR submits a selected candidate handoff. Receipt and case creation fail together when no effective template exists.
- Required evidence must be submitted and independently verified by a different user before appointment approval.
- HRM must explicitly link an exact existing employee or review unique employee/biometric numbers for a new employee. Applicant passwords and PDS data are never copied.
- Appointment creation re-resolves Plantilla, Job Position, Business Unit, Nature of Appointment, effective Salary Schedule, and qualification source. Daily salary uses `monthly x 12 / 365`.
- New employees receive a 72-hour one-time activation invitation by default; only its hash is stored. Existing employees keep their current credential.
- The prior 49 duplicate-active-Plantilla warning in `hrisof` was valid and was resolved on 2026-09-07. HumanResource V2 is applied, no appointment history was deleted, and the reviewed before/action snapshot remains in `dbo.phase5e_active_appointment_remediation_20260907` for audit or recovery review.

- Configure permissions in Administrative and reauthenticate after changing a ruleset.
- Maintain Job Position and Plantilla only in Administrative.
- Use published/effective competency versions; never try to rewrite historical ACTIVE data.
- Require meaningful return and administrator-override reasons.
- Review audit history before approval.
- Back up and migrate through reviewed provider-specific Flyway migrations; do not manually edit an applied migration.
- Validate the application against SQL Server and PostgreSQL before deploying a provider switch.
