# ISOFT PRIME-HRM User Guide

This guide covers the Phase 1 competency foundation and Phase 2 Position Competency Profiles available in the standalone PRIME-HRM application. It does not cover Phase 3 person profiles, assessments, or gap analysis because those features have not been implemented.

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

## 10. Common messages

- **Access denied**: ask an administrator to review the exact feature/action permissions, then sign in again through the portal.
- **Incomplete**: supply effective-from and at least one active, valid requirement.
- **Expected recordVersion ...**: another save changed the record; reopen and review current data.
- **Successor effectiveFrom must be after ...**: correct the successor dates so versions do not overlap.
- **No effective profile**: verify IDs, ACTIVE status, and the As-of date.
- **Administrative source changed**: review the current authoritative target before submitting/approving.
- **Dependency unavailable**: verify the configured service URL, service health, authentication, and CORS.

## 11. Operational controls

- Configure permissions in Administrative and reauthenticate after changing a ruleset.
- Maintain Job Position and Plantilla only in Administrative.
- Use published/effective competency versions; never try to rewrite historical ACTIVE data.
- Require meaningful return and administrator-override reasons.
- Review audit history before approval.
- Back up and migrate through reviewed provider-specific Flyway migrations; do not manually edit an applied migration.
- Validate the application against SQL Server and PostgreSQL before deploying a provider switch.
