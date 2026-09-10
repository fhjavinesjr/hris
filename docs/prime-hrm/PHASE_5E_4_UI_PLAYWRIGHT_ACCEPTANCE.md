# Phase 5E.4 UI and Playwright Acceptance

## Delivered surfaces

- Administrative permission rows use only the canonical keys and applicable actions:
  - `primehr.rsp-selection-decision`
  - `primehr.rsp-appointment-handoff`
  - `hrm.appointment-intake`
  - `hrm.onboarding-configuration`
  - `hrm.onboarding`
- PrimeHR staff can prepare a documented selection outcome, record variance basis, submit, return, approve, finalize applicant-safe notices, and create/retry/reconcile an appointment handoff.
- Careers shows only the signed-in applicant's released notice, offer deadline/status, accept/decline action, and coarse accepted-offer progress.
- HRM staff receive an appointment-intake inbox, explicit existing/new identity resolution, independently attributed evidence verification, server-authoritative appointment creation, stable retry result, onboarding completion, and one-time activation-link handoff.
- HRM configuration creates versioned/effective onboarding templates and publishes immutable versions.
- Employee Portal exposes an unauthenticated one-time password-activation page. Legacy employee self-registration now presents a disabled informational boundary.

No Phase 5F Jasper/report behavior is included.

## Operational sequence

1. Configure the five Administrative permission rows with `AGENCY_WIDE` data scope. Missing, malformed, or inaccessible rules fail closed in the new helpers.
2. Finalize the evaluation and HRMPSB evidence before creating a selection case.
3. Record the proposed outcome. Supply both variance basis and variance reason only when selecting a non-endorsed candidate.
4. Submit, independently approve, and finalize. Finalization releases safe notices; it does not create an employee.
5. The selected applicant accepts or declines. A decline can produce a controlled successor case. An accepted offer is not silently superseded.
6. Publish an effective onboarding template before delivering the handoff.
7. Create and submit the handoff. Retry only a retryable failure; use reconciliation for an acknowledged receipt after response loss.
8. In HRM, begin review, resolve identity, submit evidence references/fingerprints, and use a different authenticated staff member for independent verification.
9. Approve the intake for appointment. The atomic creation endpoint re-resolves Administrative Plantilla, position, business unit, salary, nature, and source fingerprints.
10. Transfer a newly issued activation link through the approved secure channel. The link expires and can be consumed once.

## Deployment remediation and migration result

The populated SQL Server `hrisof` warning was not a false alarm. On 2026-09-07, all 49 duplicate-active-Plantilla groups were reviewed under the approved fixture-data criteria. Every affected active occupant was a synthetic `EMP-*` record, each group resolved to one Job Position, and no group had a latest-assumption-date tie. The reviewed remediation retained the latest authoritative active appointment per Plantilla and deactivated 4,844 older appointments without deleting history. A 4,893-row before/action snapshot is preserved in `dbo.phase5e_active_appointment_remediation_20260907`; the rerunnable guarded script is `docs/prime-hrm/sql/PHASE_5E_HRISOF_ACTIVE_PLANTILLA_REMEDIATION_20260907.sql`.

HumanResource V2 then applied through Flyway. Post-migration checks show 5,004 employees, 5,006 appointments, 54 active appointments, 54 active Plantillas, and zero duplicate-active employee or Plantilla groups. The isolated fresh SQL Server migration also passed. Live PostgreSQL remains unverified under the approved non-blocking provider policy.

## Acceptance commands

```text
Administrative: npm run typecheck && npm run lint && npm run build
PrimeHR:        npm run typecheck && npm run lint && npm run build
HRM:            npm run typecheck; focused ESLint on Phase 5E files; npm run build
Employee Portal:npm run typecheck && npm run lint && npm run build
Playwright:     npx playwright test
```

Final real-service execution on 2026-09-07 passed all 37 tests in 4.6 minutes with zero skips. This includes Phase 5E selection, decline/successor, accepted offer, signed handoff, independent evidence verification, atomic employee/appointment creation, one-time activation, replay rejection, and the full Phase 1-5D regression set. Acceptance fixtures increased the final totals to 5,006 employees, 5,008 appointments, and 56 active unique Plantillas; duplicate-active employee and Plantilla groups remain zero. The failed diagnostic attempt's unconsumed invitation was revoked because its test token was retained in the failure trace.
