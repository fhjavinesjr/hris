# Phase 9D - Continuity Planning and Controlled RSP Referral Review

Reviewed: 2026-11-05

Status: Complete

## Delivered boundary

Phase 9D adds the position-level continuity and controlled recruitment referral layer:

- a governed continuity plan created only from a finalized position continuity-risk review;
- independent draft, submit, approve, and finalize transitions;
- critical functions, interim arrangements, continuity actions, activation triggers, recovery target,
  and review-date facts;
- a confidential immutable bench snapshot containing only candidates with an active, current,
  unexpired pool-membership event for the same cycle and critical-position designation;
- an explicit `NO_ACTIVE_BENCH` coverage outcome when a finalized plan has no current candidate;
- a controlled RSP referral packet created from one frozen bench entry;
- an optional draft vacancy link that becomes mandatory before authorization/dispatch and must match
  the target Job Position and, when applicable, Plantilla;
- independent referral authorization, dispatch, RSP receipt, under-review, closed, declined, and
  withdrawal states; and
- append-only employee-visible and restricted referral-event history.

The bench array is ordered only for deterministic display and is not a candidate rank. A referral
packet does not create an applicant account, application, shortlist, screening case, evaluation,
selection, offer, appointment, promotion, salary change, or CoreHR/Payroll mutation.

## Authorization, privacy, and integrity

- `primehr.succession-continuity` is an independent Administrative feature permission.
- Access requires Assigned Records or Agency Wide scope; Own Records is rejected because continuity
  plans and named bench coverage are confidential HR planning records.
- Assigned access additionally requires an effective member of the frozen succession committee.
- Plan submission, approval, and finalization preserve separation of duties.
- Plan creation and finalization recheck the approved critical-position designation and exact frozen
  position fingerprint.
- Bench finalization includes only eligible candidates with an included decision and the exact latest
  active, in-effect, unexpired membership event.
- Referral creation, authorization, and dispatch recheck the exact current membership event.
- A candidate cannot create or decide their own referral. Active committee conflicts in either
  direction block referral action.
- Referral authorization, receipt, and RSP status changes require Agency Wide authority.
- Vacancy reads cross the RSP application port; Succession does not read RSP repositories directly.
- Dispatch rechecks the frozen vacancy record version and source fingerprint and fails closed when it
  changed or is no longer approved/published.
- All mutations are optimistic-lock protected and audited.

## Database and contracts

Paired PostgreSQL and SQL Server V52 migrations add:

- `succession_continuity_plan`
- `succession_bench_coverage_entry`
- `succession_rsp_referral_packet`
- `succession_rsp_referral_event`

The OpenAPI contract exposes permission-controlled continuity plan lifecycle, frozen bench coverage,
referral creation/update, exact vacancy linkage, authorization, dispatch, receipt, status, withdrawal,
and event history. It exposes no application, shortlist, selection, appointment, or promotion route.

## Verification

- Focused Phase 9D domain, service, permission, OpenAPI, V52 migration parity, and populated V51-to-V52
  upgrade checks passed: 60/60.
- Fresh V1-to-V52 Flyway, repository bootstrap, and Hibernate validation passed: 9/9; 226 JPA
  repositories were discovered.
- Administrative effective-authorization regression passed: 36/36.
- Complete clean PrimeHR regression passed: 488/488 with zero failures, errors, or skips.
- Common, Administrative, and PrimeHR package verification passed after implementation.
- Live PostgreSQL and SQL Server endpoints were not configured. Paired migration parity/upgrade checks
  and the PostgreSQL-compatible H2 provider gate passed; live-provider validation remains a deployment gate.

## Next gated unit

Phase 9E may add Administrative controls, PrimeHR succession workspaces, authenticated-own Employee
Portal My Career and Development, bean-driven reports, privacy-threshold analytics, documentation,
production frontend builds, and packaged Playwright acceptance. It must stop before workforce-planning
automation or any RSP merit-selection, CoreHR, salary, appointment, or Payroll mutation.
