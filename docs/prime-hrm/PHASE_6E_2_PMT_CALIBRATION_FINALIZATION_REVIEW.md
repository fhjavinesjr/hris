# Phase 6E.2 PMT Calibration and Finalization Review

Prepared: 2026-09-25

Status: Complete and verified; stopped before Phase 6E.3

## Delivered outcome

Phase 6E.2 adds the backend workflow that converts a submitted supervisor assessment into an immutable final rating. It supports both exact-policy paths:

- policy requires PMT calibration: snapshot the supervisor assessment and effective PMT roster, propose item/dimension corrections, recompute on the server, reach a strict-majority decision, then finalize;
- policy does not require calibration: an effective, non-conflicted voting PMT finalizer uses the submitted supervisor assessment directly.

The implementation preserves self, supervisor, calibrated, and final histories as separate records. It does not accept a browser-calculated score.

## Authority and lifecycle controls

- `primehr.performance-calibration` enforces exact Access, Add, Submit, Approve, and Finalize actions with agency-wide scope.
- Effective PMT membership is required in addition to permission authority.
- Rating subjects, assigned raters, secretariat, technical-support, and non-voting members cannot decide.
- PMT roster and revision are snapshotted when calibration opens.
- Quorum is a deterministic strict majority of eligible independent voters; an otherwise eligible proposal author is removed from the persisted decision electorate.
- Quorum approval advances the rating to finalization readiness; quorum rejection closes the proposal as rejected.
- The proposal author cannot decide or finalize that calibrated result.
- `CALIBRATION_DUE` and `FINALIZATION_DUE` windows, reasoned overrides, optimistic versions, idempotency keys, and audit events are enforced.
- Repeated successful writes return the existing aggregate/final result rather than duplicating it.

## Persistence and contract

Paired V34 PostgreSQL and SQL Server migrations add:

- `spms_calibration_case`
- `spms_calibration_participant`
- `spms_calibration_decision`
- `spms_final_rating`

The migration also extends rating-case and assessment vocabularies for calibrated/final states. Provider-equivalent constraints protect one calibration per rating, one decision per snapshotted participant, valid decisions/statuses, quorum, and immutable final-rating revisions.

REST remains under `/api/primehr/v1/performance-management` and exposes calibration open/read, proposal, PMT decision, and finalization operations. The OpenAPI contract version is `6.12.0-phase-6e.2`.

## Verification evidence

- Focused gate: 74 tests, zero failures, errors, or skips.
- Clean complete PrimeHR test/package regression: 322 tests, zero failures, errors, or skips; executable JAR packaged.
- V34 structural parity and populated V33-to-V34 upgrade passed.
- Fresh provider-profile migration expectations now cover V1 through V34.
- OpenAPI and calibration permission-guard tests passed.
- Calibration integration verifies authoritative recomputation, roster/conflict snapshot, quorum approval, and immutable final rating.
- Domain tests verify strict-majority quorum, decision revision state, terminal rejection, and approved-only finalization.

Live PostgreSQL and SQL Server endpoints were not configured for this run. Paired migration parity, PostgreSQL-mode Flyway upgrade, provider-profile expectations, and provider-neutral JPA were verified; live-provider execution remains an explicit deployment gate.

## Boundary confirmation

No acknowledgment, appeal, intervention/PIP, frontend permission seed, user interface, report, downstream competency/L&D/R&R/RSP feed, CoreHR mutation, or Phase 6F behavior was introduced.

## Next approval gate

Phase 6E.3 acknowledgment, appeal, and intervention is the next planned backend unit. It must not begin without explicit user approval. Phase 6E.4 controls, interfaces, and Playwright acceptance remains separately gated after 6E.3.
