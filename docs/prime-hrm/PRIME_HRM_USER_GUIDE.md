# ISOFT PRIME-HRM User Guide — Phases 1–12

**Document purpose:** Detailed operating handbook for employees, supervisors, HR/PrimeHR staff,
committee members, approvers, administrators, and deployment/support personnel.

**Coverage:** Completed PRIME-HRM Phases 1–12.

**Product:** ISOFT HRIS — PRIME-HRM.

**Document status:** Operational guide based on the verified Phase 1–12 implementation.

**Last updated:** 7 November 2026.

This guide explains how to perform work in PRIME-HRM, not merely what the menus contain. It covers
competency management, position and person profiles, competency gaps, recruitment and selection,
performance management, learning and development, rewards and recognition, career and succession
planning, the Evidence Center, operational controls, and authenticated notifications.

PRIME-HRM is part of ISOFT HRIS, but it remains a separately governed decision-support and workflow
layer. It reads authoritative employee, appointment, position, Plantilla, organization, and
permission data from CoreHR services. It does not silently rewrite CoreHR, Payroll, or Timekeeping
records. Where an approved workflow must hand information to another module, the handoff remains
explicit, traceable, permission-controlled, and subject to the receiving module's validation.

## How to use this guide

1. Start with **Access and sign-in** and **Permissions and responsibilities**.
2. Use the phase map below to identify the business process you need.
3. Follow the numbered procedure for your role. Do not skip readiness, source-freshness, conflict,
   or approval checks.
4. Read the lifecycle notes before changing a record. Published, approved, finalized, or historical
   records are usually immutable; corrections normally create a successor instead of overwriting
   history.
5. Use **Common messages** and the final troubleshooting checklist when an action is unavailable.

### Phase and capability map

| Phase | Business capability | Main users | Primary result |
|---|---|---|---|
| 1 | Competency foundation | Competency administrators | Published categories, scales, levels, competencies, and indicators |
| 2 | Position Competency Profiles | Profile authors and approvers | Effective Job Position/Plantilla competency requirements |
| 3 | Assessments and Person Competency Profiles | Employees, assessors, validators | Immutable human-validated employee competency profile |
| 4 | Competency gaps and L&D referral | HR/PrimeHR analysts | Explainable gap result and controlled referral input |
| 5 | Recruitment, Selection, and Placement (RSP) | HR recruitment staff, HRMPSB, applicants | Vacancy-to-selection workflow and controlled HR appointment handoff |
| 6 | Strategic Performance Management System (SPMS) | Employees, supervisors, PMT, HR/PrimeHR staff | OPCR/DPCR/IPCR planning, monitoring, rating, appeal, intervention, and reports |
| 7 | Learning and Development (L&D) | Employees, supervisors, L&D staff | Needs, IDPs, plans, offerings, participation, completion, evaluation, and reports |
| 8 | Rewards and Recognition (PRAISE) | Employees, PRAISE committee, HR staff | Governed nominations, decisions, awards, fulfillment, and reports |
| 9 | Career and Succession Planning | Employees, succession committee, HR staff | Career paths, readiness, development, continuity, and controlled RSP referrals |
| 10 | Evidence Center and readiness analytics | Framework owners, validators, action owners | Versioned evidence framework, assessments, corrective actions, and privacy-controlled analytics |
| 11 | Integration and operational hardening | System administrators and support staff | Secure deployment, traceability, reconciliation, retention, legal holds, and recovery controls |
| 12 | Action inbox and notifications | All authenticated users | Authenticated-own notifications, safe target routing, and bounded reminders |

### Roles used throughout the guide

| Role | Typical responsibility | Important limitation |
|---|---|---|
| Employee | Complete own assessments, commitments, learning, acknowledgments, nominations, and assigned actions | Cannot select another employee or approve own work unless an explicitly authorized exception exists |
| Supervisor/owner | Prepare or review assigned office/employee work and provide coaching or ratings | Authority comes from the stored assignment/route, not merely a job title |
| Author/processor | Create drafts and submit operational records | Submission does not imply approval |
| Approver/validator | Independently return, approve, validate, or finalize assigned work | Must satisfy permission, route, data scope, lifecycle, and conflict rules |
| Committee/PMT member | Perform frozen-roster deliberation, calibration, evaluation, or decision work | Membership alone does not bypass conflict, voting eligibility, or feature permission |
| PRIME-HRM administrator | Maintain governed definitions and administer authorized exceptions | Administrator override requires an explicit reason and is audited |
| Technical operator | Deploy, monitor, reconcile, retain, and support the service | Operations controls do not permit editing business outcomes or purging records |

### Rules that apply to every module

- **Access is not authority to act.** Add, Edit, Delete, Submit, Approve, Publish, Finalize, Assess,
  Validate, and data scope are enforced independently where applicable.
- **The backend is authoritative.** A visible button does not grant permission, and a hidden button
  is not the only security control.
- **Identity comes from authentication.** Employee Portal pages derive the employee from the signed-in
  account and do not trust an employee number supplied by the browser.
- **Drafts are working records.** Submitted records are normally locked while awaiting action.
- **Approved history is preserved.** Published, active, finalized, or completed records are normally
  corrected through a successor, revision, reconsideration, or formally authorized reversal.
- **Reasons matter.** Return, rejection, override, cancellation, revocation, and closure actions
  require meaningful reasons when prompted.
- **Reload after a conflict.** A stale `recordVersion`, source fingerprint, or HTTP 409 means another
  user or source changed the record. Reload and review; do not repeatedly submit stale information.
- **Source freshness matters.** If Administrative or HRM data changed, review the current position,
  Plantilla, organization, appointment, employee, or qualification source before proceeding.
- **Reports follow lifecycle.** A print action appears only when the source record and the user's
  report/underlying workflow permissions allow it.
- **Privacy is intentional.** Employee views omit restricted committee narratives, other employees'
  data, confidential evidence, and small-cohort analytics.

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

## 3. Competency foundation (Phase 1)

Position Profiles can use only published, effective competency definitions and their exact published proficiency levels.

### Categories

Use **Categories** to group related competencies. Create a draft with a unique code, name, description, display order, and effectivity. Publish the completed draft. Use **New version** for later changes; do not alter published history.

### Proficiency scales and levels

Use **Proficiency Scales** to define an agency scale and its levels. A level contains a code, name, description, display order, and active state. Effectivity belongs to the scale version, not each individual level. Publish only after the complete level structure is correct.

### Competencies and behavioral indicators

Use **Competencies** to create a definition linked to a published category and scale. Add behavioral indicators for the applicable levels. Publish after validation. Published definitions are immutable; use **New version** for revisions.

## 4. Position Competency Profiles (Phase 2)

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

## 10. Assessment administration (Phase 3)

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

## 14. Competency gap analysis and controlled L&D referral (Phase 4)

Open **Competency Gaps** after an employee has a current active appointment, an effective Position
Competency Profile, and at least one validated Person Competency Profile. The calculation is
transparent and uses the exact historical source versions selected by the server.

### Generate or review a gap analysis

1. Confirm the employee's current HRM appointment and effective Job Position/Plantilla assignment.
2. Confirm that an ACTIVE Position Competency Profile resolves for the analysis date. A Plantilla
   profile takes precedence; otherwise the Job Position profile is used.
3. Confirm that the employee has a validated Person Competency Profile effective on the date.
4. Open **Competency Gaps**, select or enter the permitted employee, and choose the analysis date.
5. Select **Generate Analysis**. Repeated generation with the same employee and source versions is
   idempotent and returns the existing analysis rather than duplicating it.
6. Review every row:
   - `MEETS` means the attained ordered level equals the required level;
   - `ABOVE` means the attained level is higher than required;
   - `BELOW` means the required level is higher than attained;
   - `NOT_ASSESSED` means compatible validated evidence was not available. It is never treated as
     level zero.
7. Review the displayed formula, priority, explanation, source versions, appointment fingerprint,
   and generation time. Print the gap PDF only when the exact report permission and record scope
   allow it.

### Maintain development-priority rules

Agency-wide authorized staff may maintain versioned priority schemes. Define how gap magnitude,
mandatory/desirable classification, criticality, and not-assessed conditions map to a development
priority. Publish only after review. A later rule change creates a successor; it does not recalculate
or overwrite an earlier immutable analysis.

### Prepare a manual L&D referral

1. Open an immutable gap analysis and select **Create L&D Referral**.
2. Select only actionable `BELOW` or `NOT_ASSESSED` rows.
3. Add the permitted referral remarks and save the draft.
4. Remove an item only while the referral remains a draft.
5. Review that at least one active actionable item remains, then select **Submit Referral**.

`REFERRED` means the competency-gap recommendation is ready for L&D intake. It is not an approved
IDP, training request, budget authorization, nomination, enrollment, or completion. Phase 7 staff
must still review and disposition the need through the governed L&D workflow. The same immutable gap
item cannot be claimed by multiple active draft/referred records.

## 15. Qualification Standards (Phase 5 RSP foundation)

Qualification Standards are maintained in **Administrative > Qualification Standards** and belong to the authoritative Job Position master.

1. Select a Job Position.
2. Create a DRAFT containing education, training, experience, eligibility, optional license/statutory requirement, source/legal basis, and effectivity.
3. Review the draft and publish it with the dedicated **Publish** permission.
4. Use **New version** for a later change. Publishing a successor closes an overlapping predecessor; ACTIVE history is not edited in place.

The PrimeHR vacancy workflow will not treat manually retyped requirements as authoritative. It resolves the effective published Qualification Standard and snapshots its exact ID, version, content, fingerprint, and fetch time.

## 16. Recruitment planning and vacancy publication

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

## 17. Careers and applicant self-service

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

## 18. Applicant intake for authorized staff

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

## 19. Screening policies and application screening

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

## 20. Examination, interview, HRMPSB, selection, and appointment handoff

Configure **Evaluation Policy**, **HRMPSB Governance**, and **Candidate Evaluation** independently in Administrative. Access does not imply action authority. Published policies and committees are immutable; create effective successors for future changes.

Authorized staff admit only current qualified applications, validate frozen source versions, schedule examination/interview sessions, record attendance, maintain eligible assignments, and observe candidate-specific conflict/recusal controls. Examination results follow independent submit/validation. Panel ratings remain member-owned until submitted, and lawful reference checks keep applicant-safe communication separate from confidential notes.

Generate comparative evaluation only after all mandatory validated inputs and minimum-rater requirements are satisfied. The calculation preserves policy versions, stage breakdowns, ties, exclusions, and a deterministic fingerprint. HRMPSB deliberation requires eligible attendance and quorum. Its resolution is only a recommendation and does not select or appoint anyone.

Careers exposes only the applicant's progress and approved schedule details. Never disclose scores, rankings, panel identities/ratings, conflicts, reference notes, minutes, evidence, or recommendations through applicant communication.

### Configure the evaluation foundation

1. Create a draft **Evaluation Policy** that defines stages, criteria, weights, maximum and passing
   scores, evidence requirements, minimum raters, rounding, and tie treatment.
2. Validate that stage and criterion weights are complete. Publish the policy with its effectivity.
3. Create the HRMPSB committee/roster with the correct effective members and controlled roles.
4. Publish the roster. Use a successor for future membership changes; never rewrite the frozen
   roster used by an existing proceeding.

### Conduct candidate evaluation

1. Admit only applications with a finalized qualified screening result.
2. Open the proceeding and confirm the frozen vacancy, publication, qualification, policy, committee,
   application, and screening snapshots.
3. Schedule each required examination, interview, or other stage. Publish only applicant-safe
   schedule details to Careers.
4. Record attendance. Assign only eligible, non-conflicted evaluators.
5. Record examination results and submit them for independent validation when the stage requires it.
6. Each panel member records and submits their own ratings. A member cannot edit another member's
   rating or view confidential peer work before the lifecycle permits it.
7. Record lawful reference-check facts separately from applicant-safe communication.
8. When every mandatory validated input and minimum-rater rule is satisfied, generate the comparative
   evaluation. Review stage totals, exclusions, ties, and the deterministic calculation fingerprint.
9. Record eligible HRMPSB attendance, verify quorum, conduct deliberation, and finalize the committee
   recommendation with its required reason/minutes reference.

### Selection and offer

The HRMPSB recommendation is advisory. An authorized appointing/selection workflow must make the
selection decision independently. Record the selected candidate, lawful reason, source version,
offer events, acceptance/decline/expiry outcome, and any controlled successor action. Do not mark a
candidate appointed merely because they ranked first or received an HRMPSB recommendation.

### Controlled HRM appointment handoff and onboarding

1. Submit only a finalized selection with an accepted offer and complete required evidence.
2. PrimeHR sends an idempotent handoff to HRM. Repeating the same key returns the existing receipt;
   it must not create duplicate employee or appointment rows.
3. HRM explicitly links an existing employee or validates unique employee and biometric numbers for
   a new employee. Applicant passwords and applicant-profile data are not copied as a PDS.
4. HRM re-resolves authoritative Plantilla, Job Position, Business Unit, Nature of Appointment,
   Salary Schedule, and qualification sources before appointment creation.
5. Complete onboarding checklist items and independently verify required evidence.
6. Create/finalize Personnel Action, Oath, Assumption, and onboarding-completion documents only when
   their source lifecycle permits it. A finalized legal document is immutable; correction creates a
   linked successor.
7. For a new employee, issue the configured one-time account activation invitation. Existing
   employees retain their current credentials.

### RSP reports and privacy

Use **RSP Reports** for finalized Comparative Evaluation, Selection and Appointment Process Record,
Confidential Evidence Index, paginated RSP Register, and process analytics. Use HRM's Appointment and
Onboarding reports for Personnel Action, completed onboarding, and finalized Oath/Assumption outputs.
The general register intentionally excludes candidate identity. The Evidence Index exposes metadata,
not private file bytes, paths, storage keys, credentials, or download tokens.

## 21. Common messages

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

## 22. Phase 6–12 detailed operating procedures

### Phase 6A performance foundation

Administrative permission rules use three independent agency-wide rows:

- **Performance Policy**: Access reads policy history; Add creates drafts/successor revisions; Edit changes drafts; Publish publishes or retires immutable versions.
- **Performance Cycle**: Access reads cycles/calendars; Add creates drafts; Edit changes draft details/calendars; Finalize opens, closes, or cancels with a reason.
- **Performance Management Team**: Access reads PMT history; Add creates drafts; Edit changes draft details/rosters; Publish activates or deactivates with a reason.

After a permission change, sign in again so the effective ruleset is refreshed.

Create and publish the effective SPMS policy first. Then create a cycle tied to the exact published policy version and add its positively ordered required milestones before opening it. Published policy versions and non-draft cycle calendars are immutable; use an audited policy successor or later authorized correction workflow instead of overwriting history.

For PMT governance, enter the mandate/effectivity and an employee-number roster using only the controlled roles shown by the screen. Activation requires exactly one effective chairperson and verifies active employment through the authenticated HRM contract. If HRM is unavailable, no activation change is saved. Active rosters are immutable and deactivation preserves their history.

Complete these foundation records before using the definition, planning, monitoring, rating, and
reporting procedures below.

### Phase 6B performance definitions and templates

Administrative independently controls **Performance Rating Scales**, **Success Indicators**, and
**Performance Templates**. These are versioned definitions; published versions are immutable.

#### Create a rating scale

1. Open **Performance Rating Scales** and create a draft code, title, description, rounding rule,
   effectivity, and legal/policy basis.
2. Add ordered rating bands with non-overlapping score boundaries and labels.
3. Preview representative scores and confirm every permitted result maps to exactly one band.
4. Check readiness and publish. Use a successor for future changes.

#### Create success indicators

1. Open **Success Indicators** and create the indicator definition.
2. Choose the measure type and direction and define quality, efficiency, timeliness, or other
   permitted dimensions.
3. Configure exact thresholds/bands, weights, evidence expectation, calculation guidance, and
   effectivity.
4. Preview boundary values. Ensure there are no gaps, overlaps, or ambiguous mappings.
5. Publish only when complete; later corrections use a successor.

#### Create OPCR, DPCR, or IPCR templates

1. Open **Performance Templates** and create a draft for `OPCR`, `DPCR`, `IPCR`, or an authorized
   custom form.
2. Bind the exact published policy and rating-scale versions.
3. Add ordered sections such as Strategic, Core, Support, or Other.
4. Set section weights to total 100%.
5. Add published success indicators to each section, configure item weights, required flags, label
   overrides, and evidence overrides. Confirm each section's items form a complete weight structure.
6. Select **Check publication readiness** and resolve every error.
7. Use **Preview template score** with representative values to verify the formula and rating band.
8. Publish with effectivity and an approval reason. Use **Create revision** for future changes or
   retire an obsolete version with a reason.

The system stores the exact policy, scale, indicator, and template versions in downstream
commitments. Publishing a new definition does not recalculate historical commitments or ratings.

### Phase 6C performance planning and approval

Administrative permission rules separately control **Strategic Objectives**, **Plan Assignments**, **Office Performance Commitments**, and **Individual Performance Commitments**. Access is required before any action. Add, Edit, Publish, Submit, Approve, and Finalize remain independent; data scope restricts office work to assigned/agency records and IPCR work to own/assigned/agency records.

Use **Strategic Objectives** to create agency, area, or business-unit objectives against exact published policy and indicator versions. Check readiness before publishing. Published versions are immutable; create a successor revision instead of overwriting history.

Use **Plan Assignments** to bind an open cycle, published template, published objectives, authoritative subject, routing business unit, and owner. Review the displayed organization, participant, and route fingerprints before activation.

Use **Office Commitments** or **Individual Commitments** to generate a draft from an active assignment, complete targets and dates, configure valid cascade relationships, check readiness, and submit. Editing is available only for draft, amendment-draft, or returned records. Approval actions follow the stored route in order; the inbox shows only tasks assigned to the signed-in actor. Reload after a stale-write message rather than repeating an old decision.

Employees use **Employee Portal → Employee Self Service → My Performance Commitments** for their own IPCR records. The server derives ownership from the authenticated account; the page does not accept an employee number or ID. Employees may edit and submit only when their exact permission actions and lifecycle state permit it.

After a commitment is approved, continue with the Phase 6D monitoring/coaching and Phase 6E
rating/calibration procedures below. Planning approval alone does not create an accomplishment or
final rating.

### Phase 6D.1 performance monitoring

An approved commitment with a completed approval route can open one monitoring case. The case keeps the exact commitment, policy, cycle, owner, organization, and approval-route fingerprints used when monitoring began. Opening the same commitment again is idempotent and does not create a duplicate case.

Employees record progress and actual accomplishments against an exact commitment item. Drafts may be edited, while submission freezes that revision. A returned update is corrected through a linked successor revision so the submitted history is preserved. The configured operational supervisor may accept an update or return it with visible feedback.

Evidence uploads use secured storage, generated object keys, checksums, MIME and size checks, and per-request download authorization. Storage locations are not returned to the browser. Evidence can be voided with a reason; workflow evidence is not physically deleted.

Final accomplishment submission checks every required commitment item and its evidence requirement. It freezes the accomplishment set for the later rating phase; a supervisor may return it for correction before rating begins. Progress percentages remain informational and do not calculate a rating.

Authorized supervisors and PMT users operate these records from **PrimeHR → Monitoring Inbox**. Employees use **Employee Portal → Employee Self Service → My Performance Monitoring**. The inbox and portal controls appear only when both the exact permission action and current lifecycle permit the operation.

### Phase 6D.2 coaching and mid-cycle review

The assigned supervisor from the immutable commitment route creates a coaching session against the monitoring case and may link exact commitment items. Employee-visible guidance is stored separately from restricted supervisor notes. Issuing a session freezes that revision; a correction creates a linked successor rather than replacing history.

The employee can acknowledge receipt and add a response. Acknowledgment confirms receipt only and does not imply agreement or remove later review or appeal rights.

Coaching action items identify the accountable employee, due date, progress, completion, and supervisor verification. The accountable employee updates their own item. The assigned supervisor verifies completion or reopens it with a reason. Action items do not automatically create a disciplinary case, PIP, L&D referral, rating, or promotion record.

A mid-cycle review is available only when the exact published policy version requires it and the configured `MID_CYCLE_REVIEW` milestone is current. Its snapshot records monitoring-update count, open action count, and missing required evidence count. The supervisor submits the review and the employee acknowledges receipt. Closing without acknowledgment requires a reason, agency-wide permission, and effective PMT membership. An amendment recommendation points users to the existing formal Phase 6C amendment workflow and never changes a target directly.

Administrative manages `primehr.performance-monitoring` and `primehr.performance-coaching` separately. Access, Add, Edit, Submit, and Approve are independent, and data scope is limited to Own Records, Assigned Records, or Agency Wide. A hidden menu is not authorization; the server also verifies ownership, the immutable supervisor route, PMT process role where required, record state, and record version.

### Phase 6D.3 employee and supervisor operation

In **Monitoring Inbox**, select a permitted case to review its approved commitment-item snapshot and progress history. Use the lifecycle buttons to accept or return submitted updates, return final accomplishments, issue or void coaching, verify or reopen completed action items, and submit or close a mid-cycle review. A stale-write response means another user changed the record; reload before deciding again.

In **My Performance Monitoring**, employees can work only on the case returned for their authenticated account. Create or revise drafts, submit progress, attach allowed evidence, correct returned work through a successor revision, and submit final accomplishments when required items and evidence are ready. Employees can see only employee-visible coaching text, may acknowledge receipt with a response, maintain their own assigned action items, and acknowledge a submitted mid-cycle review. There is no employee selector.

Monitoring percentages and accomplishments are informational inputs only. These screens do not calculate, approve, or publish a rating and do not create calibration, appeal, PIP, L&D, R&R, or promotion records. Those remain outside Phase 6D.

### Phase 6E.1 employee and supervisor ratings

After the employee submits a complete accomplishment set, the exact assigned supervisor may open one rating case for that accomplishment revision during the configured `RATING_DUE` window. Opening snapshots the approved commitment, selected latest submitted/accepted item updates, evidence checksums, exact indicator/rubric versions, section/item weights, rating scale, policy, route, and organization fingerprints. The monitoring case becomes `READY_FOR_RATING`; later definition changes cannot alter the snapshot.

When the exact policy requires self-assessment, the employee may create and submit only their own `SELF` assessment. The stored supervisor may create and submit only the `SUPERVISOR` assessment, and supervisor submission is blocked until the required self-assessment is submitted. Drafts remain private to their author. A submitted correction creates a successor revision and preserves the original.

Users provide only a numeric actual for threshold rubrics or an exact permitted band for manual rubrics. The backend maps each dimension to exactly one rubric level, applies dimension, item, and section weights, rounds with the immutable rating-scale rule, and resolves exactly one overall band. Calculated scores, statuses, formula version, and fingerprints are server-owned.

Employees complete required self-assessment from **Employee Portal → My Performance Ratings**.
Supervisors open **Performance Ratings**, select the assigned rating case, review the immutable source
snapshot, enter only permitted actuals/bands and remarks, then submit. Neither user types the overall
score or final band.

### Phase 6E.2 PMT calibration and finalization

When the exact published policy requires calibration, an authorized effective PMT member opens a calibration case against the submitted supervisor assessment. The case snapshots the effective PMT roster and its revision. The rating subject, assigned supervisor, secretariat, technical-support, and non-voting members remain visible for traceability but cannot vote.

Calibration proposals change exact item/dimension actuals and require a rationale. They cannot type an overall score: the backend reruns the Phase 6E.1 calculation and preserves the original self/supervisor assessments beside the calibrated proposal. A strict majority of eligible, non-conflicted, independent voters approves or rejects the proposal; an otherwise eligible proposal author is excluded from that electorate. Every decision advances the optimistic record version, and retries with the same idempotency key return the existing result.

Finalization is allowed only in the configured `FINALIZATION_DUE` window and by an effective, non-conflicted voting PMT member with the exact Finalize permission. The proposal author cannot finalize their own calibrated result. Policies that do not require calibration use the submitted supervisor assessment directly. The resulting rating is immutable and retains its calculation fingerprint, exact source assessment, calibration lineage where applicable, finalizer, and time.

Authorized PMT members perform this work in **Performance Calibration**. Review original self and
supervisor assessments beside the proposal, record independent votes, verify majority/quorum and
conflicts, then use the lifecycle-eligible finalization action. The interface never accepts a typed
overall score.

### Phase 6E.3 acknowledgment, appeal, and intervention

An employee may acknowledge receipt of their exact latest final-rating revision during the configured `ACKNOWLEDGMENT_DUE` window. Acknowledgment records receipt only and does not mean agreement. Closing without employee acknowledgment requires a reason, agency-wide Finalize authority, and an effective PMT member.

The authenticated employee may create and submit an appeal only for their own latest final rating during `APPEAL_DUE`. Evidence is stored securely and downloaded only after authorization. An effective, voting, non-conflicted PMT member with agency-wide approval authority decides the appeal; the subject and assigned supervisor cannot decide it. A modified decision reruns the authoritative server calculation and creates a linked successor final-rating revision without overwriting the original.

Intervention rules bind exact policy, scale, and rating-band versions to `NONE`, `ALERT`, `PIP_RECOMMENDED`, or `PIP_REQUIRED`. Finalization and modified appeals evaluate that mapping idempotently. PIP records objectives, support, accountable employee, dates, checkpoints, employee receipt, completion, and closure. Employee views receive employee-visible feedback but not restricted PMT notes.

Employees acknowledge, appeal, upload permitted appeal evidence, and review employee-visible PIP
details in **My Performance Ratings**. Authorized PMT staff use **Performance Appeals** and
**Performance Interventions** for assigned decisions and lifecycle work. These workflows do not
create discipline, change CoreHR, or feed Payroll, Timekeeping, recruitment, L&D, R&R, or promotion.

### Phase 6F performance reports

Administrative manages five independent reporting permissions: **Performance Planning and Rating
Forms**, **Performance Monitoring and Development Reports**, **Performance Register Report**,
**Performance Aggregate Analytics**, and **Performance Outcome Feed**. Grant only the required data
scope and have the user sign in again after changing a ruleset. Register, analytics, and the outcome
feed require Agency Wide scope at the backend.

Authorized staff use **PrimeHR → Performance Reports** to generate lifecycle-eligible planning,
rating, monitoring, coaching, development-plan, and intervention PDFs. The same workspace provides
the paginated final-rating register and privacy-threshold aggregate analytics when their exact
permissions allow them. A hidden card or menu is not authorization; the server also checks the
underlying workflow permission, record scope, ownership or assignment, and source lifecycle.

Employees use **Employee Portal → Employee Self Service → My Performance Reports**. The page lists
only records returned by authenticated `/mine` endpoints, never accepts an employee number, and
opens only reports owned by that account. Unauthorized direct URLs redirect to Dashboard. The
performance-outcome feed is an API-only read contract and is not shown in either frontend.

### Cross-module operational readiness reminders

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

### Phase 7 — Learning and Development operation

Administrative exposes separate permission rows for L&D Governance, Needs, Individual Development
Plans, Office/Agency Plans, Programs/Offerings, Nominations, Delivery/Completion,
Evaluation/Follow-through, Formal Reports, Aggregate Analytics, and the API-only Completion Feed.
Grant only the required action and data scope, then have the user sign in again.

Authorized staff use the seven **PrimeHR → L&D** workspaces to review in-scope records and perform
only lifecycle actions assigned to them. Approved and published source versions are immutable.
Returned work is corrected through the workflow; stale record-version messages require a reload
before retrying. Aggregate effectiveness is available only in **L&D Reports** to a user with the
independent Agency Wide analytics permission.

Employees use **Employee Portal → Employee Self Service → My Learning**. The server derives the
employee from the authenticated account and the page has no employee selector. Employees see only
their own needs, IDPs, nominations, schedules, completion records, evaluation tasks, and learning
action plans. IDP printing is enabled only for an approved plan, while completion/evaluation
packets require a completed record.

The generated IDP, Training Order, completion packet, and evaluation summary are authoritative
agency operational reports based on immutable source records. They are not represented as a
prescribed CSC form unless the agency separately approves a matching branded layout. L&D completion
does not automatically write PDS training history, change competencies, close a performance
intervention, grant a reward, or create a promotion/succession decision.

#### 7.1 Configure governance and catalog records

1. Open **L&D Governance & Catalog**.
2. Create the governing policy version, learning intervention/catalog entries, evaluation framework,
   provider/resource-person records, and required effectivity information.
3. Review classification, delivery mode, evidence, evaluation, and follow-through requirements.
4. Publish the approved version. Published definitions are immutable; use an effective successor
   for later changes.

Do this before processing needs or planning offerings. A draft or expired definition cannot serve as
an authoritative source for a new approved downstream record.

#### 7.2 Record and disposition a learning need

1. Open **L&D Needs & IDPs** and select the employee within your permitted data scope.
2. Create a need from an authorized source such as a competency-gap referral, performance finding,
   succession-development requirement, or supported manual assessment.
3. Preserve the source reference and explain the learning/development requirement. Do not turn an
   unverified recommendation directly into an enrollment.
4. Review and disposition the need. Return incomplete work with a reason, or accept it for IDP
   development according to the configured process.

#### 7.3 Prepare and approve an Individual Development Plan

1. Create the IDP against the employee and accepted needs.
2. Add development items with objective, activity/intervention, target competency or outcome,
   support/resources, accountable parties, planned dates, and success/follow-through measures.
3. Save the draft and review it with the employee and supervisor.
4. Submit the plan. The configured route follows `DRAFT → SUBMITTED → RECOMMENDED → APPROVED`, with
   supported return/resubmission and cancellation.
5. The recommender verifies relevance and support; the final approver verifies authority, feasibility,
   and source integrity.
6. Print the IDP only after it reaches **APPROVED**.

An approved IDP does not itself reserve funding, nominate the employee, confirm a seat, or prove
completion.

#### 7.4 Consolidate office and agency plans

1. Open **L&D Plans & Programs**.
2. Create an office plan from approved needs/IDPs and authoritative organizational scope.
3. Consolidate supported office demand into the agency plan without exposing restricted personal
   narrative.
4. Submit, approve, and publish the plan. Plan lifecycle is
   `DRAFT → SUBMITTED → APPROVED → PUBLISHED`.
5. Create a program design only from a published plan. Define objectives, audience, learning methods,
   provider/resource requirements, evaluation design, and expected outputs.
6. Submit and approve the program design.
7. Create scheduled offerings with venue/mode, dates, capacity, resource persons, and delivery
   details. Submit, approve, then publish the offering. A published offering may be explicitly
   cancelled with a reason; it is not edited in place.

#### 7.5 Nominate, approve, enroll, and issue a Training Order

1. Open **L&D Nominations** and choose a published offering.
2. Nominate an employee from an approved need/IDP or another permitted source.
3. Verify eligibility, schedule, capacity, funding/authority reference, and absence of a duplicate
   active nomination.
4. Submit through the configured approval route.
5. On approval, confirm enrollment. Use substitution or cancellation only through the controlled
   lifecycle, retaining the original participant history and reason.
6. Generate the Training Order only from the approved authoritative source. Review subject, schedule,
   participants, resource/support roles, dress/attendance instructions, and agency details before
   printing.

#### 7.6 Record delivery, attendance, output, and completion

1. Open **L&D Delivery** for the scheduled offering.
2. Record session delivery and participant attendance using the approved roster.
3. Record required outputs/evidence references. Do not mark a participant complete solely because
   they were enrolled.
4. Prepare the completion record. Lifecycle is
   `DRAFT → FOR_VERIFICATION → COMPLETED` or `NOT_COMPLETED`.
5. An independent authorized verifier confirms attendance, outputs, and completion requirements.
   Terminal results are retained as history.

#### 7.7 Evaluate effectiveness and follow through

1. Open **L&D Evaluation** and complete the assigned immediate or delayed evaluation task.
2. Record authorized evaluation results and supporting remarks/evidence.
3. Submit the task for the required review/approval.
4. Create and monitor a Learning Application/Action Plan where transfer of learning is required.
5. Supervisors verify follow-through outcomes; incomplete or unsupported claims are returned rather
   than counted as successful transfer.

#### 7.8 Employee Portal: My Learning

Employees open **Employee Self Service → My Learning** to view only their own needs, approved IDPs,
nominations, schedules, completion records, assigned evaluation tasks, and action plans. The page has
no employee selector. Employees should review schedule changes, complete assigned evaluation/action
work, and use print actions only when the underlying record is eligible.

#### 7.9 L&D reports and analytics

Use **L&D Reports** for approved IDP, approved Training Order, completed participant packet, and
completed evaluation/follow-through summary. Aggregate effectiveness requires a separate Agency
Wide analytics permission. Groups below the configured privacy cohort are suppressed. The
completion feed is read-only and API-only; it is not a user-facing page and does not write a PDS.

### Phase 8 — Rewards and Recognition (PRAISE) operation

Administrative exposes separate permission rows for PRAISE Governance, Committee, Cycle,
Nomination, Eligibility, Evaluation, Award, Fulfillment, Reports, and Aggregate Analytics. Grant
only the needed action and data scope, then have the user sign in again. Aggregate analytics always
requires the independent Agency Wide analytics permission.

Authorized staff use the six **PrimeHR -> PRAISE** workspaces for governance; committee and cycles;
nominations; eligibility and evaluation; awards and fulfillment; and reports and analytics. Menu
visibility does not grant authority: the backend also validates action, data scope, nomination
ownership or frozen committee assignment, lifecycle, conflicts, and record version. Reload before
retrying a stale-write response.

Employees use **Employee Portal -> Employee Self Service -> My Nominations & Recognitions**. The
server derives identity from the authenticated account and the page has no employee selector.
Employees see only permitted nominations and publication-safe recognition results. Withdrawal is
available only for an eligible own nomination, while PDF actions appear only when the underlying
record and report permission allow them.

PRAISE operational PDFs use immutable source data and SQL-free bean-driven Jasper input. The
generic ISOFT nomination, evaluation, award/citation-source, and annual-register layouts are not
represented as prescribed CSC/HAP forms. Use an agency-approved template when the current PRAISE or
Honor Awards Program cycle requires one. Aggregate metrics are suppressed below the configured
minimum cohort and never rank employees or infer protected characteristics. Rewards records do not
post Payroll, change an appointment or salary, grant promotion, or create a succession decision.

#### 8.1 Establish PRAISE governance

1. Open **PRAISE Governance**.
2. Create the policy/program version, legal basis, effectivity, categories, eligibility rules,
   evaluation framework, decision rules, evidence requirements, and fulfillment expectations.
3. Review the configuration for the intended award cycle, then publish it.
4. Create an effective successor for later policy changes. Existing nominations and awards continue
   to reference their exact frozen version.

#### 8.2 Configure committee and award cycle

1. Open **PRAISE Committee & Cycles**.
2. Create the committee roster and controlled roles. Confirm active employment and conflict rules.
3. Activate/publish the roster with effectivity.
4. Create the award cycle, bind the exact governance/category versions, nomination window,
   evaluation period, and expected decision dates.
5. Open the cycle only after the roster, categories, evidence rules, and dates are complete.

#### 8.3 Prepare and submit a nomination

1. Open **PRAISE Nominations** or, for an employee-authorized pathway, **My Nominations &
   Recognitions** in Employee Portal.
2. Choose the open cycle/category and eligible nominee type.
3. Enter the nomination narrative and accomplishment facts. Attach/reference only permitted evidence.
4. Review declarations, consent/authority, deadlines, and category-specific completeness.
5. Submit the nomination. It becomes locked for normal editing.
6. Withdraw only an eligible own nomination while the lifecycle allows it; the reason and history
   are retained.

Submission is not an award. The system must still determine eligibility, conduct independent
evaluation, record the committee recommendation, and issue a final authorized decision.

#### 8.4 Eligibility, conflicts, evaluation, and decision

1. Open **PRAISE Eligibility & Evaluation** and initialize/review the case.
2. Validate nominee, category, cycle, evidence, and policy source versions.
3. Record eligibility findings. An ineligible result requires the configured reason and explanation.
4. Declare committee conflicts or recusals. Replace an ineligible/conflicted assignee through the
   controlled workflow; never share another evaluator's account.
5. Each assigned evaluator records and submits their own scores/findings against the frozen criteria.
6. Generate the supported recommendation only after required evaluations are complete.
7. The independent authorized decision maker/committee records the final decision with the required
   reason. Reconsideration follows the controlled successor/review path and does not overwrite the
   original decision.

#### 8.5 Confer award and record fulfillment

1. Open **PRAISE Awards & Fulfillment** after an approved decision.
2. Confer the award, preserving the exact nominee, cycle, category, decision, citation source, and
   authority.
3. Record incentive authorization separately from fulfillment. An award record does not itself post
   a Payroll transaction.
4. Record fulfillment evidence such as ceremony, certificate/citation, non-cash incentive, or other
   approved delivery facts.
5. Use successor correction or revocation only with the authorized action and reason; retain the
   original award history.
6. For external Honor Awards Program processing, record only the packet event lifecycle:
   `PREPARED → ENDORSED → SUBMITTED → ACKNOWLEDGED/RETURNED`, or `WITHDRAWN`. PRIME-HRM does not claim
   to perform CSC adjudication.

#### 8.6 Employee Portal and reports

Employees use **My Nominations & Recognitions** to view only permitted own nominations and
publication-safe recognition results. Internal eligibility notes, evaluator identities/scores,
committee deliberation, and other nominees are not exposed.

Use **PRAISE Reports** for lifecycle-eligible nomination, evaluation, award/citation-source, and
annual-register outputs. Confirm whether the agency requires a cycle-specific official HAP form;
the generic ISOFT PDF is an operational record, not automatically a prescribed CSC form. Aggregate
analytics is independently permissioned, suppresses small cohorts, and must not be used as an
employee leaderboard.

### Phase 9 — Career and Succession Planning operation

Administrative exposes separate permission entries for Succession Governance, Succession Committee,
Career Paths, Critical Positions, Candidate Pools, Readiness, Development, Continuity, Reports, and
Aggregate Analytics. Grant only the required actions and data scope, then have the user sign in again.
Aggregate analytics requires its independent Agency Wide permission.

Authorized staff use the seven **PrimeHR -> Succession** workspaces for governance; career paths and
critical positions; candidate pools; readiness; development and membership lineage; continuity and
controlled RSP referrals; and reports and analytics. Menu visibility does not grant authority. The
backend also checks the exact action, Own/Assigned/Agency scope, frozen committee or panel role,
conflict status, lifecycle, source freshness, and record version.

Employees use **Employee Portal -> Employee Self Service -> My Career and Development**. Identity is
derived from the authenticated account, and the page has no employee selector. Employees see only
their candidate-safe notice and findings, expressly employee-visible readiness outcome/reason, and
their own development milestones, reviews and membership history. Internal panel narrative,
restricted deliberation, named confidential bench coverage and other employees' records are not
shown. Eligible own readiness and development PDFs may be printed from that page.

Succession PDFs are ISOFT operational records, not appointment, promotion, or prescribed merit-
selection forms. Coverage analytics suppress cohorts below the configured privacy minimum and do
not rank employees. A succession pool decision or RSP referral never automatically creates an RSP
application, shortlist, selection, appointment, promotion, salary change, or Payroll transaction.

#### 9.1 Configure governance and committee

1. Open **Succession Governance**.
2. Define the policy, readiness model, confidentiality rules, review periods, and permitted decision
   outcomes. Publish the effective version.
3. Create and activate the succession committee/panel roster with controlled roles and effectivity.
4. Use successors for later policy or roster changes. Cases preserve the exact frozen versions used.

#### 9.2 Define career paths and critical positions

1. Open **Career Paths & Critical Positions**.
2. Create career-path nodes and transitions using authoritative position references. Describe lawful
   readiness/development prerequisites without promising automatic promotion.
3. Create a critical-position assessment with business-continuity rationale, impact, vacancy/retirement
   considerations, and supporting evidence.
4. Submit it for independent review. The lifecycle is
   `DRAFT → SUBMITTED → APPROVED` or `REJECTED`; an approved record may later be retired.
5. Do not edit an approved critical-position decision in place.

#### 9.3 Open a succession cycle and candidate pool

1. Open **Succession Cycles & Candidate Pools**.
2. Create a cycle using the exact governance, committee, career-path, and critical-position sources.
3. Define the target position, intake window, eligibility/source basis, and confidential handling.
4. Admit a candidate only through an authorized source and preserve candidate consent/notice where
   required. Intake is not a promotion or selection.
5. Freeze the pool/assignment information required for independent readiness assessment.

#### 9.4 Assess readiness and decide pool membership

1. Open **Succession Readiness** and initialize the candidate's case.
2. Review authoritative current appointment, target position profile, person profile, competency gap,
   evidence, and frozen panel facts.
3. Record conflicts/recusals and assign eligible replacements through the controlled workflow.
4. Each assigned assessor records their own findings. Restricted narrative remains panel-confidential;
   explicitly employee-visible findings/reasons are stored separately.
5. Complete the required review and final readiness decision. Never infer a decision solely from a
   numeric score or competency gap.
6. Record pool membership through the approved decision, preserving start/end and successor lineage.
   A pool membership is development/continuity planning data, not a guarantee of appointment.

#### 9.5 Create and monitor a succession development plan

1. Open **Succession Development**.
2. Create the plan against the candidate, target position, readiness result, and current pool
   membership.
3. Add milestones, actions, accountable persons, target dates, evidence expectations, and employee-
   visible guidance.
4. Submit/approve according to the configured lifecycle.
5. Record milestone progress and review outcomes. Preserve membership history and plan successors;
   do not erase a previous readiness or pool decision.

#### 9.6 Maintain continuity coverage and controlled RSP referral

1. Open **Succession Continuity & RSP Referrals**.
2. Create the continuity plan for an approved critical position.
3. Record confidential bench coverage from eligible finalized membership/readiness sources. Do not
   publish named bench members in aggregate reports.
4. Review, approve/finalize, and periodically reassess the plan against current sources.
5. Where authorized, prepare a controlled RSP referral from an eligible succession source. The
   referral is an input only; RSP must still apply vacancy, qualification, screening, merit-selection,
   conflict, and appointment rules.

#### 9.7 Employee Portal and reports

Employees use **My Career and Development** to view their candidate-safe notice, expressly
employee-visible readiness outcome/reason, own development milestones/reviews, and own membership
history. They cannot see restricted panel narrative, confidential bench coverage, or other
employees' records.

Use **Succession Reports** for readiness, development, continuity, controlled RSP referral, and
coverage register outputs. Aggregate coverage includes only finalized sources and suppresses cohorts
below the configured privacy minimum. It must not be used to rank employees or bypass merit selection.

### Phase 10 — PRIME-ESG Evidence Center operation

Administrative exposes separate permission entries for Evidence Framework, Assessment, Validation,
Corrective Actions, Reports, and Aggregate Analytics. Framework administration and analytics require
Agency Wide scope. Assessment, validation, action, and report access may use Assigned Records or
Agency Wide scope according to the exact permission. Have the user sign in again after changing a
ruleset.

Authorized staff use four **PrimeHR -> Evidence** workspaces. Framework Registry displays exact
definition versions and permits publication only with publish authority. Evidence Assessments shows
the user's assigned or agency-visible cases and permits only lifecycle-valid submission. Validation
and Actions supports reasoned return/finalization and human readiness sign-off. Reports and Analytics
shows missing/expiring evidence, overdue actions, and privacy-controlled cross-module aggregates.

Reload before retrying a stale record-version response. A readiness correction must supersede the
latest immutable snapshot. Evidence and analytics permissions never bypass the underlying source
module's permission, confidentiality, freshness, or cohort suppression. Suppressed cells remain
omitted rather than displaying derivable complementary totals.

All Evidence Center calculations, snapshots, and PDFs are internal decision-support records. They
are not an official CSC PRIME-ESG assessment, validation, certification, accreditation, award,
score, or Governance Level. The phase does not expose evidence work in Employee Portal and does not
rank employees, submit externally, or change CoreHR, Timekeeping, or Payroll records.

#### 10.1 Configure the evidence framework

1. Open **Evidence Framework**.
2. Create the framework/version and define domains, indicators/requirements, evidence types,
   validity/expiry rules, scoring or readiness semantics, and corrective-action expectations.
3. Link only approved source-module definitions and record their exact versions.
4. Run readiness/completeness checks and publish the framework with the required reason/effectivity.
5. Use a successor for future changes. Existing assessments retain their frozen framework.

#### 10.2 Conduct an evidence assessment

1. Open **Evidence Assessments**.
2. Create or open an assigned case using the permitted organization/source scope.
3. Review the exact framework and source snapshots before recording findings.
4. For each requirement, record evidence metadata, validity, sufficiency, expiry, and authorized
   narrative. Do not store credentials, arbitrary HTML, or unrestricted source payloads.
5. Submit the assessment when every required finding is complete. Submitted evidence is reviewed
   under the frozen source/framework versions.

#### 10.3 Validate, return, finalize, and correct readiness

1. Open **Evidence Validation & Corrective Actions**.
2. Review the submitted case, source freshness, evidence exceptions, and conflicts.
3. Return incomplete/unsupported work with a meaningful reason, or finalize the human readiness
   decision when authorized.
4. If the decision produces gaps, create corrective actions with an owner, due date, required output,
   and follow-up status.
5. The action owner records progress/evidence; the validator verifies closure or reopens it with a
   reason.
6. A readiness correction must reference the latest snapshot ID and record version and creates a
   successor. It never silently edits the finalized snapshot.

#### 10.4 Reports, exceptions, and privacy analytics

Use **Evidence Reports** to review missing/expiring evidence, overdue corrective actions, evidence
index output, internal assessment output, and privacy-controlled aggregates. Report access does not
automatically grant access to the underlying source module. Aggregate analytics is separately
permissioned and applies both minimum-cohort suppression and source-module suppression. Suppressed
cells remain omitted so complementary totals cannot be used to reconstruct protected data.

There is intentionally no Employee Portal Evidence Center. Notifications may tell an employee that
protected Evidence work exists, but the user must open the authorized PrimeHR destination and pass
its permission checks.

### Phase 11 — Deployment, integration, reconciliation, retention, and recovery

#### 11.1 Deployment operation

Deploy PRIME-HRM as a standalone service with its own database. Select exactly one explicit
production profile: `production-postgresql` for cloud PostgreSQL or `production-sqlserver` for
on-premise SQL Server. Production startup fails closed when mandatory secrets, agency identity,
database credentials, CoreHR service URLs, or CORS origins are missing, local, or inconsistent with
the selected provider. Never point PRIME-HRM at a CoreHR schema.

Use `/actuator/health/liveness` for process health and `/actuator/health/readiness` for traffic
readiness, including database availability. Both public responses intentionally omit component
details. `/actuator/info` requires a valid staff token and should show the promoted release version.
The complete environment-variable inventory, container command, promotion checks, and rollback
procedure are recorded in
[PHASE_11A_DEPLOYMENT_CONFIGURATION_SECURITY_REVIEW.md](./PHASE_11A_DEPLOYMENT_CONFIGURATION_SECURITY_REVIEW.md).

#### 11.2 Integration and support operation

Every PRIME-HRM response now includes `X-Correlation-Id`. When reporting an integration or API
failure, provide that identifier, the time, and the action attempted; do not send the user's token or
sensitive screen data. JSON errors also expose the same value as `correlationId`.

Administrative and HumanResource lookups use configured connection/read timeouts. PRIME-HRM may
retry a transient read failure a small bounded number of times, but it never automatically retries
workflow POST/PUT commands. Appointment handoff retains its idempotency key and dedicated service
credential; staff browser credentials are not replaced with a generic service identity.

Downstream access denial remains denied. A missing source remains not found, a stale/conflicting
source remains a conflict, and a dependency outage returns a stable service-unavailable response.
Wait for the dependency to recover before retrying a command; never work around an outage by
granting broader permissions. The exact transport and error matrix is recorded in
[PHASE_11B_INTEGRATION_RELIABILITY_TRACEABILITY_REVIEW.md](./PHASE_11B_INTEGRATION_RELIABILITY_TRACEABILITY_REVIEW.md).

#### 11.3 Operations workspace

Only an agency-wide user with the exact `primehr.operations` permission may open **Operations**.
Access, Add, Edit, and Delete-style administrative actions remain independent. In this workspace,
"Delete" does not expose a purge control; legal and retention safety boundaries remain authoritative.

Use the workspace for:

- sanitized application/dependency state;
- manual read-only reconciliation runs;
- finding acknowledgment and resolution notes;
- versioned retention-policy administration;
- legal-hold administration; and
- report-only retention eligibility.

The dependency panel deliberately shows only a bounded dependency name, status, HTTP status, and
duration. It must never display configured URLs, database details, credentials, tokens, employee
identifiers, filenames, storage keys, or document contents.

#### 11.4 Run and resolve reconciliation

1. Open **Operations** and review dependency readiness. If a required dependency is unavailable,
   restore it before executing a business command.
2. Select the permitted reconciliation scope and start a manual run. Scheduled reconciliation is
   disabled unless explicitly configured by deployment operators.
3. Review each finding category:
   - source drift: the immutable snapshot differs from the current authorized source;
   - missing source: the stored source can no longer be resolved;
   - dependency unavailable: current comparison could not be completed safely;
   - object metadata inconsistency: stored document metadata does not match local/S3 object metadata.
4. Acknowledge the finding with an operational note when investigation begins.
5. Correct the source problem through its owning module or approved operational process. Reconciliation
   itself never rewrites the snapshot, opens document content, or mutates CoreHR.
6. Resolve the finding only after verification. The run, finding, actor, correlation ID, and notes
   remain auditable.

#### 11.5 Retention policies and legal holds

1. Create a draft retention-policy version for an evidence family that carries retention tags.
2. Define the lawful basis, periods, effectivity, and review metadata.
3. Activate the reviewed version. Use a successor for future changes.
4. Create/release a legal hold only with the exact authority and case/reference information.
5. Use the eligibility report to identify records for review.

PRIME-HRM provides **report-only eligibility**. It has no deletion or purge endpoint. A retention
policy or eligibility row is never permission to remove data manually from the database or storage.

#### 11.6 Backup, restore, and release acceptance

Before promotion:

1. back up the PRIME-HRM database and durable document storage;
2. apply the provider-appropriate reviewed Flyway migrations—PostgreSQL or SQL Server;
3. run liveness/readiness, protected release info, authenticated permission, workflow, and report
   smoke checks;
4. verify Administrative and HRM dependencies and exact deployed frontend runtime configuration;
5. execute a restore drill in an approved isolated target and complete/sign the restore-verification
   manifest; and
6. retain the previous application artifacts for application rollback.

Never perform a destructive migration rollback or delete immutable workflow history to roll back an
application release. Correct database changes through a reviewed forward migration.

### Phase 12 — Action inbox, notifications, and reminders

Phase 12 provides one authenticated-own notification feed in the PrimeHR management shell and
Employee Portal. A notification is a safe projection of work; it is never authority to approve,
submit, edit, or finalize the underlying record.

#### 12.1 Use the notification bell

1. Sign in normally and locate the bell in the application header.
2. The badge shows the current unread count returned for the authenticated employee.
3. Select the bell or open **My Notifications**.
4. Use **All** or **Unread** to filter the feed and **Refresh** to obtain current notices.
5. Select **Mark read** for one notice or **Mark all read** for the current user's feed.
6. Dismiss only an informational notice when **Dismiss** is available. An action notice cannot be
   dismissed as though its underlying work were complete.

Neither interface accepts an employee number or agency from the browser. Administrators do not gain
a general screen for browsing another employee's personal notification feed.

#### 12.2 Open a notification safely

1. Select **Open** on the notice.
2. The system marks that notice read for the signed-in employee.
3. PRIME-HRM revalidates the authoritative target, current assignment, lifecycle, and permission.
4. If the work still exists and remains assigned/authorized, the application navigates to a
   controlled destination.
5. If the work was completed, reassigned, removed, or is no longer authorized, the user remains in
   the inbox and receives a safe unavailable message.

The browser never follows a URL supplied by notification data. Destinations come from a controlled
server-side source-type catalogue. This prevents an old or malicious notice from bypassing route and
workflow security.

#### 12.3 Supported notification sources

The completed Phase 12 catalogue activates:

- Performance commitment action assignments for authorized PrimeHR users;
- employee-safe Performance commitment destinations in Employee Portal;
- Evidence assessment/corrective-action assignments in protected PrimeHR; and
- a portal-safe instruction when Evidence work exists but no Employee Portal Evidence destination
  is approved.

L&D, PRAISE, Succession, RSP staff work, Operations, applicants, CoreHR leave/overtime/DTR, Payroll,
payslips, broadcasts, email, SMS, browser/mobile push, and real-time WebSocket/SSE delivery are not
part of the Phase 12 catalogue. Existing Employee Portal account-setup and overtime-discrepancy
notices remain separate supported portal behavior.

#### 12.4 Reminder operation

The reminder service reads authoritative active assignments with due dates. It bounds candidates by
agency, lead window, and maximum batch size; revalidates every source; closes stale actions; and uses
a deterministic event key so a retry replays instead of creating duplicate notices.

Scheduling is disabled by default. Deployment operators explicitly enable it with
`PRIMEHR_NOTIFICATION_REMINDERS_ENABLED=true` and configure bounded lead hours, fixed delay, and
batch size. Reminder metrics expose only `issued`, `replayed`, and `stale` counts—never employee
numbers, source IDs, routes, titles, messages, or reasons.

#### 12.5 Notification privacy and troubleshooting

- Notifications contain safe typed titles/messages only. They do not store private coaching text,
  evidence, attachment bytes, arbitrary HTML, authentication tokens, or arbitrary URLs.
- If the unread count is unavailable, refresh after confirming PrimeHR API health and authentication.
- If **Open** reports unavailable, return to the owning module/inbox and check whether the task was
  completed, reassigned, or removed from your permission scope.
- Marking a notice read does not complete the work. Complete the authoritative workflow action in the
  destination module.
- A reminder may be replayed safely; it must not create a second workflow task.

## 23. Lifecycle and status reference

Status names vary by business module, but these meanings are consistent:

| Status concept | Meaning | Normal user action |
|---|---|---|
| DRAFT | Working record, not yet formally submitted | Complete, save, check readiness, then submit |
| RETURNED | Reviewer sent the record back with a reason | Read the reason, correct the draft, resubmit |
| SUBMITTED | Formally sent to the next assigned actor | Wait for/monitor assigned review; normal editing is locked |
| RECOMMENDED | Intermediate authority endorsed the record | Await final approval; recommendation is not final approval |
| APPROVED/AUTHORIZED | Required authority accepted the record | Perform only the allowed next lifecycle step |
| PUBLISHED/ACTIVE | Version is effective and available as an authoritative source | Do not edit; create a successor/revision for change |
| FINALIZED/COMPLETED | Workflow result is terminal and auditable | Print/report or use an authorized correction/reconsideration path |
| REJECTED/DECLINED/DISAPPROVED | Authority did not accept the request | Review reason; create a new/corrected record only when allowed |
| CANCELLED/WITHDRAWN | Request was intentionally stopped with retained history | Do not reuse as an active source |
| ARCHIVED/RETIRED | Historical definition/record is no longer active | Retain for audit; use its successor for current work |
| SUPERSEDED | A later linked version replaced it prospectively | Review lineage; never delete the predecessor |

Never infer a status from button visibility alone. Open record details and audit/lifecycle history,
especially where `RECOMMENDED` and `APPROVED` are separate route stages.

## 24. Reports and printing guide

Before printing any PRIME-HRM report:

1. confirm the source record is in the required approved/final/completed state;
2. confirm the signed-in user has both the exact report permission and underlying workflow scope;
3. review employee, organization, dates, version, authority/signatory, and report parameters;
4. generate the PDF through the application action so authentication remains in the Authorization
   header and not in a URL;
5. inspect the PDF before official use, especially multipage tables, repeated headers, totals,
   optional images/signatures, blank values, and continuation pages; and
6. store/distribute the output according to agency privacy and records-management policy.

| Area | Typical outputs | Minimum lifecycle expectation |
|---|---|---|
| Competency | Competency Gap Analysis | Immutable generated analysis and authorized scope |
| RSP | Vacancy Notice, Comparative Evaluation, Selection Record, Evidence Index, registers | Approved/published/finalized source as defined by each report |
| SPMS | OPCR/DPCR/IPCR, monitoring, coaching, development/PIP, rating and registers | Approved commitment or lifecycle-eligible finalized workflow source |
| L&D | IDP, Training Order, completion packet, evaluation summary | Approved IDP/order; completed participation/evaluation source |
| PRAISE | Nomination, evaluation, award/citation, annual register | Lifecycle-eligible nomination/evaluation/final award source |
| Succession | Readiness, development, continuity, RSP referral, coverage register | Finalized or approved source with privacy restrictions |
| Evidence | Evidence index/internal assessment and exception reporting | Authorized framework/assessment source and underlying module access |

ISOFT operational reports must not be represented as prescribed CSC/HAP or other official forms
unless the agency has approved the matching template/version.

## 25. Administrator setup checklist

Before allowing production users into a workflow:

- publish the required policy, scale, definition, template, committee/PMT roster, and effectivity;
- confirm authoritative Administrative Job Position, Plantilla, organization, permission, and
  technical settings;
- confirm authoritative HRM employee, active appointment, and employee-account mapping;
- assign exact feature actions and data scope—avoid broad Agency Wide access when Own/Assigned is
  sufficient;
- require users to sign out and sign in again after permission changes;
- verify configured CoreHR service URLs, CORS origins, JWT/service credentials, private storage, and
  runtime configuration without exposing secrets;
- test one allowed user, one denied user, one returned record, one stale-write conflict, and one
  lifecycle-eligible PDF;
- verify small-cohort privacy suppression where analytics is enabled;
- back up data/storage and verify provider-appropriate migration/recovery procedures; and
- keep administrator override exceptional, reasoned, and auditable.

## 26. End-user and approver checklist

### Before submitting

- Confirm you are using the correct employee, office, position, Plantilla, cycle, and effective date.
- Complete every required row, declaration, evidence reference, and reason.
- Review readiness/source-freshness messages.
- Save the draft and reopen/review important totals or generated calculations.
- Submit once and wait for the response; do not create a duplicate because a page is slow.

### Before recommending, approving, validating, or finalizing

- Confirm the task is assigned to you and you have no unresolved conflict.
- Review the complete record, immutable source versions, earlier decisions, and return history.
- Verify that `RECOMMENDED` is not being mistaken for final approval.
- Return unsupported work with a specific, useful reason.
- Use override only when the authorized exception is genuine and enter the complete reason.
- Reopen the record after success and confirm status, actor, time, and audit history.

### After approval/finalization

- Perform the next explicit handoff; do not assume another module was automatically updated.
- Print only lifecycle-eligible reports.
- Protect confidential employee, applicant, committee, evaluation, and evidence information.
- Use a successor/correction/reconsideration path when an immutable result must be corrected.

## 27. Support information to provide

When reporting a problem, provide:

- module/page and action attempted;
- date/time and employee account used, without sharing the password or token;
- record code/ID and displayed status, if non-sensitive;
- exact safe error message;
- `X-Correlation-Id`/`correlationId` from the response when available;
- whether the problem affects one user or several users; and
- whether Administrative, HRM, PrimeHR, and the database were healthy.

Do not send screenshots containing full PDS, applicant evidence, payroll information, tokens,
passwords, restricted committee narrative, or private document locations unless the approved secure
support process specifically requires and protects them.
