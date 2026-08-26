# ISOFT PRIME-HRM Phase 2.3 - Position Profile UI

Status: Complete; manual and repeatable Playwright browser acceptance passed

Updated: 2026-08-26

## Scope and boundary

Phase 2.3 implements only the user-approved Administrative permission controls and standalone PrimeHR Position Competency Profile UI. It consumes the completed Phase 2.1/2.2 contracts without changing backend behavior.

Employee Portal changes, person profiles, assessments, competency gaps, reports, notifications, HRISApp assembly, deployment, commit, and push are excluded.

## Administrative permission controls

`administrative-software/src/app/administrative/permission/Permission.tsx` now exposes `primehr.position-profile` as **Position Competency Profiles** with independent Access, Add, Edit, Delete, Submit, and Approve controls.

- Access is required for every action.
- Turning Access off clears every action in the editor.
- Turning any action on also turns Access on.
- Missing legacy Submit/Approve values remain false.
- Existing administrator fill behavior grants all supported actions.
- Existing permission JSON remains readable; invalid JSON fails closed.

The user's pre-existing `administrative-software/.env` change was not edited or included in the Phase 2.3 source scope.

## PrimeHR UI

The new route is `/prime-hr/position-profiles`. It provides:

- exact permission-derived visibility for create, edit, archive, submit, return, approve, and successor actions;
- current Administrative Job Position and Plantilla search through the authenticated typed integration contract;
- draft profile creation/editing and dynamic competency requirements;
- published competency and scale paging without a first-100-record truncation;
- effective-date filtering for competency and proficiency-level choices, with backend validation still authoritative;
- completeness guidance before submission;
- stored target snapshot details plus a live Administrative fingerprint freshness check;
- lifecycle, immutable ACTIVE state, lineage, submission/approval metadata, and append-only audit history;
- exact two-version comparison with added, removed, changed, and unchanged classifications;
- effective profile resolution showing Plantilla override or Job Position fallback;
- actionable dependency, validation, authorization, and stale-record conflict messages;
- SSO/local-storage compatibility and Employee Portal return navigation.

## Files changed

### `administrative-software`

- Modified `src/app/administrative/permission/Permission.tsx` - adds and safely normalizes Position Profile actions.

### `prime-hr-software`

- Modified `src/app/prime-hr/competencies/CompetencyManager.tsx` - adds Position Profiles navigation.
- Modified `src/lib/auth.ts` - adds typed Position Profile permission evaluation and authenticated Administrative requests.
- Created `src/lib/positionProfiles.ts` - typed Phase 2 REST client contracts and actionable API errors.
- Created `src/app/prime-hr/position-profiles/page.tsx` - route entry.
- Created `src/app/prime-hr/position-profiles/PositionProfileManager.tsx` - management, lifecycle, resolution, comparison, source-freshness, and audit UI.
- Created `src/app/prime-hr/position-profiles/PositionProfileManager.module.scss` - responsive SCSS-module presentation.
- Modified `.gitignore`, `package.json`, and `package-lock.json` - Playwright commands/dependency and ignored reports/results.
- Created `.env.e2e.example` and `playwright.config.ts` - secret-free local configuration and managed local services.
- Created `e2e/support/primeHrTestSupport.ts` and five Phase 2.3 specification files - authenticated SSO contexts, controlled fixtures, RBAC, lifecycle, conflict, comparison, resolution, and precedence coverage.
- Created `docs/PRIME_HRM_E2E_TESTING.md` - repeatable setup, execution, safety, and troubleshooting runbook.

No backend Java, migration, OpenAPI, Employee Portal, HRM, Timekeeping, Payroll, Jasper, or deployed frontend file was changed by Phase 2.3.

## Automated verification

### PrimeHR frontend

```text
npm run typecheck
PASS - strict TypeScript, zero errors

npm run lint
PASS - eslint ., zero findings

npm run build
PASS - Next.js 16.2.12 production build
Compiled, type-checked, and statically generated /prime-hr/position-profiles

npm run e2e
PASS - 8 Playwright tests in 32.1 seconds against local SQL Server services
```

### Administrative frontend

```text
npx eslint src/app/administrative/permission/Permission.tsx
PASS - zero findings

npm run build
PASS - Next.js 16.2.6 production build
Compiled, type-checked, and statically generated /administrative/permission
```

`npm run lint` in Administrative does not execute a lint run because its pre-existing script is `next lint`, which Next.js 16 interprets as an invalid project directory. The exact failure is:

```text
Invalid project directory provided, no such directory: ...\administrative-software\lint
```

This was not hidden or changed as an incidental Phase 2.3 behavior/configuration edit. The changed Administrative file passed direct ESLint, and the full production build passed TypeScript.

The Playwright suite was run once while developing the stable fixtures and then rerun in full after the database already contained those fixtures. The final repeatability run passed all eight tests. Live PostgreSQL was not used for this browser run by user direction; Phase 2 backend portability evidence remains recorded separately.

The Administrative build retains a pre-existing `images.domains` deprecation warning. It does not fail the build and is outside this phase.

## Repository and boundary audit

- `git diff --check`: passed in both frontend repositories; only Git line-ending conversion warnings were emitted.
- Untracked Phase 2.3 files: only the four listed PrimeHR source/style files.
- Generated `.next` and TypeScript build-info output remains ignored and is not in Git status.
- Credential-pattern scan: no password, secret, API key, private key, datasource URL, or connection string in the Phase 2.3 source.
- Strict typing scan: no explicit `any` in the Phase 2.3 PrimeHR source.
- Phase boundary scan: no person profile, assessment, gap, RSP, SPMS, L&D, report, notification, employee assignment, or Phase 3+ implementation.

## Browser acceptance matrix

The user manually confirmed the applicable submit, return, resubmit, independent approval, administrator override, denied-access, immutability, conflict, successor, comparison, audit, and resolution behaviors. Playwright now automates the repeatable subset below. Immutable approval-success fixtures are reused instead of creating an unlimited ACTIVE version chain on every run.

Run Administrative API/UI and PrimeHR API/UI. Use two ordinary employee accounts with separate rulesets (submitter and approver) plus the established administrator account. Restart/re-authenticate after changing a ruleset so the SSO exchange refreshes `permissionData`.

1. **Denied access**
   - Remove Position Competency Profiles Access from an ordinary account.
   - Open `/prime-hr/position-profiles` after a fresh SSO.
   - Expected: Access denied; no profile data or actions. A direct API request remains backend-denied.

2. **Independent CRUD actions**
   - Grant Access/Add/Edit/Delete but not Submit/Approve.
   - Expected: create/edit/archive draft controls follow their individual flags; Submit, Return, and Approve are absent.
   - Turn Access off in Administrative.
   - Expected: all six action checkboxes clear.

3. **Submitter path and validation**
   - Grant the submitter Access/Add/Edit/Delete/Submit, not Approve.
   - Create a Job Position or Plantilla draft.
   - Try submission without an effective-from date or active requirement.
   - Expected: visible INCOMPLETE guidance and server validation error without partial lifecycle/audit mutation.
   - Complete the draft and submit it.
   - Expected: status SUBMITTED, content controls disappear, audit records submission, and Approve is absent.

4. **Return and resubmit**
   - Sign in as the separate approver with Access/Approve, not Submit.
   - Return the submission; cancel once, then supply a reason.
   - Expected: reason is required, status returns to DRAFT, audit contains actor/reason.
   - Resubmit as the submitter.

5. **Independent approval and immutability**
   - Approve as the separate approver.
   - Expected: status ACTIVE; approved actor/time and target snapshot are visible; edit/archive/submit/return/approve controls are absent; New Successor is present only with Add.

6. **Administrator override**
   - As administrator, create and submit a complete test draft, then attempt self-approval without a reason.
   - Expected: backend rejects it. Approve with an explicit reason.
   - Expected: ACTIVE plus an audited administrator-override reason.

7. **Optimistic conflict**
   - Open the same DRAFT in two tabs. Save an edit in tab A, then save the stale revision in tab B.
   - Expected: actionable 409/conflict message and automatic list/detail reload; tab A data is not overwritten.

8. **Source freshness**
   - View details of a draft/active version.
   - Expected: current snapshot match is shown. Change the authoritative target in Administrative and reload details.
   - Expected: CHANGED is shown while the historical stored snapshot remains unchanged.

9. **Successor and comparison**
   - Create a successor from ACTIVE, set a later effective-from date, change at least one level/classification/criticality, add one requirement, archive another, submit, and approve.
   - Compare the two versions.
   - Expected: correct Added/Removed/Changed/Unchanged counts and exact selected version values.

10. **Resolution precedence**
    - With an effective Job Position profile only, resolve by Job Position/date.
    - Expected: Job Position fallback.
    - Add and approve an effective Plantilla profile for the same position/item and resolve with both IDs.
    - Expected: Plantilla override. A date outside effectivity returns an actionable no-effective-profile error.

11. **Legacy permission compatibility**
    - Use a ruleset whose stored Position Profile entry lacks `canSubmit`/`canApprove`.
    - Expected: both actions remain denied; CRUD does not substitute for either lifecycle action.

## Defect found and corrected

The two-tab test confirmed that the backend rejected a stale update, but the UI retained the stale edit form after reloading the current server revision. A user could click Save again and unintentionally apply old form content as a new update. The 409 handler now closes and clears the editor before reloading current list/detail data. The Playwright conflict test verifies both rejection and preservation of the first accepted value.

## Acceptance disposition

Phase 2.3 acceptance is complete on the approved local SQL Server environment. The exact commands, fixtures, and limitations are in `prime-hr-software/docs/PRIME_HRM_E2E_TESTING.md`. No Phase 3 behavior was implemented. Commit, push, and deployment remain under the user's control.
