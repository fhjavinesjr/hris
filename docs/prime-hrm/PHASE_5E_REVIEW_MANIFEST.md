# Phase 5E Review Manifest

## Result

Phase 5E.1 selection and offer, Phase 5E.2 durable appointment handoff, Phase 5E.3 onboarding/atomic appointment/activation backend, and the approved Phase 5E.4 controls and UI are implemented. Phase 5F has not started.

## Contract and security evidence

- Selection remains a human decision derived from finalized comparative and HRMPSB evidence; no automatic candidate selection exists.
- Applicant notices are separate safe records. Careers cannot read scores, ranks, panel identities, conflicts, reference notes, minutes, evidence, employee IDs, or onboarding internals.
- Handoff payloads are immutable, fingerprinted, service-authenticated, receipt-validated, retryable, and reconcilable.
- Employee/appointment creation occurs once inside HRM after explicit identity and evidence gates. Stable retry returns the same IDs and never issues a second activation token.
- The one-time activation token is hashed at rest, expiring, and single-use.
- All five new staff features use backend authorization and fail-closed frontend helpers. Sidebar visibility is not the security boundary.
- Legacy employee registration no longer offers self-registration behavior.

## Automated evidence

- Administrative focused authorization test: 16 passed.
- Administrative, PrimeHR, HRM, and Employee Portal strict TypeScript: passed.
- Administrative lint: zero errors, one pre-existing Sidebar hook warning.
- PrimeHR lint: passed.
- Employee Portal lint: passed.
- HRM Phase 5E focused lint: passed. Full repository lint remains blocked by 25 pre-existing unused-variable errors and 11 warnings in unrelated legacy screens.
- All four production Next builds compiled and packaged. PrimeHR packaging requires `NEXT_PUBLIC_API_BASE_URL_HRM`; the gate passed with the local acceptance endpoint supplied.
- Playwright compile/discovery: 37 tests across 11 files, including two Phase 5E real-service scenarios, zero explicit skips.
- Final real-service execution on 2026-09-07: 37/37 passed in 4.6 minutes with zero skips, including the complete Phase 5E selection-to-activation lifecycle and all earlier regressions.
- Prior Phase 5E.1-5E.3 backend gate: Common 3, Administrative 48, HumanResource 66, PrimeHR 224, plus HRISApp package.
- HumanResource isolated fresh SQL Server V2 migration: passed.
- HumanResource clean runtime package after the SQL Server activation conversion: passed. The focused populated-SQL-Server V2 integration test also passed 1/1 when run at the HumanResource module boundary; an earlier reactor-wide `-am` invocation exposed the repository's existing mixed Common/HumanResource Spring dependency classpath and was not used as acceptance evidence.

## Populated SQL Server adjudication

The original 49-group warning was valid. Review confirmed all 4,893 affected active rows belonged to synthetic `EMP-*` fixture employees, with one Job Position per group and no latest-assumption-date ties. The guarded remediation retained 49 latest active appointments and deactivated 4,844 older rows; it deleted nothing and preserved the full decision snapshot in `dbo.phase5e_active_appointment_remediation_20260907`. HumanResource V2 then applied successfully through Flyway. Immediate post-remediation invariants were 5,004 employees, 5,006 appointments, 54 active appointments/Plantillas, and zero duplicate-active employee or Plantilla groups. The two acceptance attempts intentionally added two synthetic employee/appointment fixtures; the final database has 5,006 employees, 5,008 appointments, 56 active unique Plantillas, and still zero duplicate-active groups. The failed attempt's trace-exposed activation invitation was revoked; the passing attempt's invitation was consumed.

## Boundary

No Phase 5F report, Jasper template, formal RSP reporting, or later workflow has been implemented. Phase 5E acceptance is complete; proceeding to Phase 5F still requires separate approval.
