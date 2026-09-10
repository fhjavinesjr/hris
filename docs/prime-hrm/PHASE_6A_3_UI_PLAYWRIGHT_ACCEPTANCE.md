# Phase 6A.3 Controls, Performance UI, and Playwright Acceptance

Date: 2026-09-08  
Status: Passed; Phase 6A complete; stopped before Phase 6B

## Delivered controls

Administrative exposes three independent, agency-wide permission rows:

| Feature key | Actions |
|---|---|
| `primehr.performance-policy` | Access, Add, Edit, Publish |
| `primehr.performance-cycle` | Access, Add, Edit, Finalize |
| `primehr.performance-pmt` | Access, Add, Edit, Publish |

Administrator role `"1"` retains the established full-access behavior. A non-administrator requires the exact action and `AGENCY_WIDE` scope. Missing, malformed, narrower, or stale rules fail closed. Navigation and page controls are usability boundaries; the Phase 6A backend guards remain authoritative.

## PrimeHR workspaces

- **Performance Policies** creates and edits drafts, publishes immutable effective versions, creates reasoned successor revisions, retires versions without deleting history, and explains stale lifecycle conflicts.
- **Performance Cycles and Calendar** binds a cycle to an exact published policy version, atomically replaces a positively ordered draft milestone calendar, and performs reasoned open, close, and cancel transitions. Opened and closed definitions are read-only.
- **Performance Management Teams** maintains effective employee-backed rosters with controlled roles, displays authoritative employee/position/Plantilla snapshots, and performs reasoned activation/deactivation. Activation fails closed with no saved changes if the authenticated HRM eligibility source is unavailable.

All API calls use the configured PrimeHR base URL and send the JWT only through the Authorization header. The UI includes loading, empty, denied, validation, history, immutable lifecycle, stale-write, and dependency-unavailable states.

## Operator guide

1. In Administrative Permission, grant only the required performance row/actions with Agency-wide scope. Have the user sign in again to refresh their ruleset.
2. In **Performance Policies**, create a complete draft and publish it with effectivity dates and an approval reason. Published versions cannot be edited; use **Create revision** for a successor.
3. In **Performance Cycles**, reference the exact published policy version ID and enter the cycle period/timezone. Save at least the policy-required milestones with unique positive `displayOrder` values and ISO-8601 instants, then open the cycle with a reason. Closing/cancelling preserves history.
4. In **Performance Management Teams**, enter the mandate and effective dates, then save an effective roster using employee numbers and controlled roles. Activation requires exactly one effective chairperson and live authoritative HRM eligibility. An active roster is immutable.
5. On HTTP 409, reload and review the accepted version before retrying. On HRM dependency unavailable, restore the configured service/authentication path; do not bypass eligibility through direct database edits.

## Verification

| Gate | Result |
|---|---|
| Administrative lint/build/package | Passed; one unrelated pre-existing Sidebar hook warning remains |
| PrimeHR strict TypeScript/lint/build/package | Passed |
| Focused `phase6a.spec.ts` | 4/4 passed |
| Complete Playwright regression | 45/45 passed with zero skips |
| Provider evidence | Live SQL Server integrated browser startup/regression passed; paired migration parity and PostgreSQL-compatible Flyway/Hibernate gates remain passed from 6A.1/6A.2; live PostgreSQL was not configured |

Browser acceptance covers exact permissions and denied direct routes, policy publication/revision/immutability/stale writes, positive calendar ordering and reasoned open/close, PMT historical snapshots and HRM-unavailable rollback, successful activation, Authorization-header transport, and absence of Phase 6B surfaces.

## Deployment and rollback

Deploy the Administrative and PrimeHR frontend packages together with backend artifacts containing PrimeHR V23/V24 and the Phase 6A API. Apply the matching provider migration through Flyway before enabling the routes, verify the three ruleset mappings, and smoke-test one permitted and one denied non-administrator account.

Frontend rollback restores the prior frontend artifacts and permission catalogue; persisted JSON rules may retain unknown keys harmlessly. Do not destructively roll back an applied V23/V24 schema or delete published policies, cycles, PMTs, memberships, or audits. Correct production schema/data with a reviewed forward migration.

## Boundary

Employee Portal, Careers, HRM, Timekeeping, and Payroll UI are unchanged. Phase 6A.3 does not implement templates, KRA/KPI definitions, success indicators, formulas, OPCR/DPCR/IPCR commitments, monitoring, ratings, calibration, coaching, appeals, or reports. Phase 6B has not started.
