# ISOFT PRIME-HRM User Guide

This guide covers the delivered standalone PRIME-HRM functions through Phase 5A: competency administration, Position and Person Competency Profiles, assessments, competency gaps and manual L&D referrals, and vacancy/recruitment planning through controlled vacancy-notice publication. Applicant intake and later RSP stages are not yet implemented.

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

The staff page does not determine documentary completeness or qualification. Those actions require a separately approved Phase 5C design.

### Deployment controls

Before enabling applicant uploads outside a local QA environment:

- configure a durable `local` or private S3-compatible storage provider and root/bucket;
- configure a dedicated applicant JWT secret different from employee JWT signing material;
- publish approved privacy/retention text and file-type/size/required-document policy;
- configure exact deployed CORS origins and never use a wildcard;
- plan email verification/password reset, CAPTCHA/rate limiting, malware scanning, retention/legal hold, backup, and applicant support controls appropriate to the agency.

The application fails closed when applicant/storage functionality is enabled without required secure storage or token configuration.

## 18. Common messages

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

## 19. Operational controls

- Configure permissions in Administrative and reauthenticate after changing a ruleset.
- Maintain Job Position and Plantilla only in Administrative.
- Use published/effective competency versions; never try to rewrite historical ACTIVE data.
- Require meaningful return and administrator-override reasons.
- Review audit history before approval.
- Back up and migrate through reviewed provider-specific Flyway migrations; do not manually edit an applied migration.
- Validate the application against SQL Server and PostgreSQL before deploying a provider switch.
