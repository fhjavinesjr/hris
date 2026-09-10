# Phase 5F.4 Controls, Reporting UI, and Playwright Acceptance

Date: 2026-09-08  
Status: Passed; Phase 5 complete through 5F; stopped before Phase 6 implementation

## Review manifest

- Administrative exposes the eight approved Phase 5F feature rows. Seven rows have Access only; `hrm.appointment-documents` has Access, Add, Edit, and Finalize only. Every row uses `AGENCY_WIDE` scope.
- PrimeHR exposes one permission-aware RSP Reports workspace for the three finalized formal reports, the paged/current-or-history RSP register, and process analytics.
- HRM exposes a report-only Appointment and Onboarding workspace for Personnel Action, completed onboarding, and the immutable Oath/Assumption document lifecycle.
- Navigation visibility and direct-route/action controls fail closed. Backend authorization remains the security boundary.
- PDF clients send JWTs only in the Authorization header, require `application/pdf`, use sanitized server filenames, and open/download temporary browser blob URLs.
- Careers and Employee Portal received no Phase 5F report route or report permission.

## Report catalogue and data dictionary

| Output | Owner | Input | State gate | UI-visible data boundary |
|---|---|---|---|---|
| Comparative Evaluation | PrimeHR | proceeding ID | finalized, fingerprint-consistent proceeding | validated results, rank/tie/gate and recommendation evidence |
| Selection and Appointment Process Record | PrimeHR | selection ID | finalized, fingerprint-consistent selection | selection/offer/handoff record; no HRM appointment outcome |
| Confidential Evidence Index | PrimeHR | selection ID | finalized, fingerprint-consistent selection | evidence metadata only; no bytes, paths, storage keys, tokens, or signed URLs |
| RSP Register | PrimeHR | from/to, one of five explicit date bases, exact workflow/position/organization filters, history flag, page/size | bounded query | vacancy and aggregate workflow outcomes; no candidate identity |
| RSP Process Analytics | PrimeHR | same bounded date/filter contract plus explicit history flag | bounded query | documented channel/reason/funnel/outcome/handoff counts and stage durations; missing milestones are N/A |
| Personnel Action | HumanResource | employee appointment ID | authoritative appointment exists | existing compatible Personnel Action output |
| Onboarding Completion | HumanResource | intake ID | onboarding status is COMPLETED | checklist status/attribution and evidence identifiers, never evidence contents |
| Oath / Assumption | HumanResource | appointment and document IDs | finalized immutable legal-document snapshot | approved form text and authoritative appointment/signatory facts |

The register and analytics support `PUBLICATION_DATE`, `PUBLICATION_CLOSING_DATE`, `PROCEEDING_FINALIZATION_DATE`, `SELECTION_FINALIZATION_DATE`, and `HANDOFF_ACKNOWLEDGMENT_DATE`. The UI sends the selected basis, exact workflow/position/organization filters, and explicit history choice unchanged to JSON and PDF endpoints.

## Permission matrix

| Feature key | Actions | Scope |
|---|---|---|
| `primehr.rsp-comparative-report` | Access | Agency-wide |
| `primehr.rsp-selection-report` | Access | Agency-wide |
| `primehr.rsp-evidence-index-report` | Access | Agency-wide |
| `primehr.rsp-register-report` | Access | Agency-wide |
| `primehr.rsp-process-analytics` | Access | Agency-wide |
| `hrm.appointment-report` | Access | Agency-wide |
| `hrm.onboarding-report` | Access | Agency-wide |
| `hrm.appointment-documents` | Access, Add, Edit, Finalize | Agency-wide |

Administrator role `"1"` retains the established full-access behavior. Non-administrator access requires both the exact action and agency-wide scope; `OWN_RECORDS`, `ASSIGNED_RECORDS`, absent, malformed, or stale rules fail closed.

## Operator guide

1. In Administrative Permission, grant only the exact report rows required by the staff role and retain Agency-wide scope. Users sign in again to obtain the updated ruleset.
2. In PrimeHR, open **RSP Reports**. Enter a finalized proceeding or selection ID for formal records. For register/analytics, select From, To, and the displayed date basis. Enable revision history only for an authorized historical review.
3. In HRM, open **Appointment & Onboarding Reports**. Personnel Action uses the employee appointment ID; Onboarding Completion uses a completed intake ID.
4. For an Oath or Assumption document, enter the authoritative appointment, legal dates, venue, and employee-backed signatory IDs. Save a draft, review it, then finalize with a reason. Finalized records cannot be edited. A correction creates a successor linked through the superseded document ID.
5. A stale/final-state conflict means the authoritative workflow or fingerprint changed. Reopen and verify the source record; never bypass it by editing the database.

## Retention and privacy

Generated PDF bytes remain transient and are not stored by Phase 5F. Existing source retention and Administrative policy remain authoritative. Logs and browser URLs must not contain JWTs, applicant PII lists, evidence contents/paths, activation tokens, passwords, or PDF bytes. The general register deliberately excludes candidate identity. The Evidence Index is separately permissioned and contains metadata only. No demographic attribute is collected, inferred, or reported.

## Verification

| Gate | Result |
|---|---|
| Administrative strict type-check/lint/build/package | Passed |
| PrimeHR strict type-check/lint/build/package | Passed; packaging requires only the Administrative runtime-config bootstrap and resolves the HRM API from `system_config.api.url.hrm` |
| Central runtime configuration | Passed; live SQL Server `hrisof.dbo.system_config` contains `api.url.hrm`, and the focused Common/Administrative resolver tests passed 6/6 |
| HRM strict type-check/build/package and Phase 5F focused lint | Passed; repository-wide lint retains 25 unrelated pre-existing errors and 11 warnings in older Pass Slip, Time Correction, Personal Data, and Separation screens |
| Focused Playwright `phase5f.spec.ts` | 4/4 passed |
| Complete Playwright regression | 41/41 passed in 5.6 minutes; zero skips |
| Clean nine-project Maven reactor | Passed in 4:34; Common 3, TimeKeeping 7, Administrative 52, HumanResource 71, Payroll 45, PrimeHR 238 tests passed |

The browser scenarios cover exact filter/history propagation, general-register privacy, deny-by-default navigation and direct routes, stale/final conflict messaging, explicit no-data analytics, Authorization-header/no-token-URL transport, HRM report isolation, and disabled mutation after legal-document finalization. Backend suites retain the final/fingerprint, secure response header, Personnel Action compatibility, provider-parity, Jasper, and immutable lifecycle coverage from Phases 5F.1-5F.3.

## Deployment and rollback

Deploy the Administrative, PrimeHR, and HRM frontend packages together with backend artifacts containing Phase 5F.1-5F.3. Configure the PrimeHR package with `HRIS_CONFIG_BOOTSTRAP_URL` or the compatible `NEXT_PUBLIC_API_BASE_URL_ADMINISTRATIVE` fallback; Administrative then supplies `api.url.hrm` and the other public runtime values from `system_config`. Apply HumanResource V3 through Flyway before enabling legal-document UI. Verify the eight feature mappings in a non-administrator ruleset, then smoke-test one allowed and one denied route.

Frontend rollback is restoration of the prior frontend artifacts and permission UI definition; persisted JSON rules may retain unknown keys harmlessly. HumanResource V3 is not rolled back destructively after application. Use a reviewed forward migration for schema/data correction. Finalized legal-document records and audits must never be deleted to perform rollback.

Master Plan V2 contains no Phase 5G. No Phase 6 behavior was implemented as part of Phase 5F.
