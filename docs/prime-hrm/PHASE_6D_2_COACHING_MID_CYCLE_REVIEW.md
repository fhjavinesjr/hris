# Phase 6D.2 Coaching and Mid-Cycle Review

Reviewed: 2026-09-24

Status: Complete; stopped for separate approval before Phase 6D.3

Authority: `PHASE_6D_MONITORING_COACHING_SCOPE_APPROVAL.md`

## Delivered outcome

- Assigned supervisors create, edit, issue, correct, and reason-void coaching sessions tied to an exact monitoring case and optional exact commitment items.
- Issued and acknowledged history is immutable. Corrections create successor revisions with root/prior lineage.
- Employee-visible feedback and employee responses are separate from restricted supervisor notes. Own-record responses omit private notes.
- Employee acknowledgment records receipt only and does not imply agreement.
- Action items preserve accountable employee, due date, progress/completion, supervisor verification, and reasoned reopen history.
- The accountable employee alone updates progress; the stored route supervisor verifies or reopens completion.
- Mid-cycle review is available only when the exact policy requires it and the configured milestone window is active.
- Review creation snapshots monitoring updates, unresolved action items, and missing required evidence. Supervisor submission and employee acknowledgment are separate transitions.
- Closure without acknowledgment requires a reason, agency-wide Approve authority, and effective PMT membership.
- Amendment recommendation stores a reason and points to the existing Phase 6C amendment workflow without mutating the approved commitment.
- Optimistic versions, per-case idempotency keys, tenant filters, exact owner/supervisor identity, shared audit, and lifecycle validation apply to every command.

## Persistence and portability

Paired forward-only V32 migrations create:

- `spms_coaching_session`
- `spms_coaching_session_item`
- `spms_coaching_action_item`
- `spms_mid_cycle_review`

PostgreSQL and SQL Server scripts have equivalent tables, constraints, foreign keys, indexes, status vocabularies, and temporal/boolean representations. Fresh migration through V32 and populated V31-to-V32 preservation passed. Live provider endpoints were not configured, so live PostgreSQL and SQL Server execution remains a deployment-stage verification.

## Verification evidence

- Focused coaching, monitoring, lifecycle, authorization, OpenAPI, parity, and upgrade gates passed.
- Complete PrimeHR reactor regression: **306 tests passed**, zero failures/errors/skips.
- Spring startup validates all 115 repositories and exposes the expected controller mappings.
- Tests cover private-note filtering, stored-supervisor authority, issue/acknowledge/correction history, accountable action completion and verification, policy rejection, positive mid-cycle submit/acknowledge, invalid-command non-mutation, V32 parity, and V31 data survival.

## Boundary review

No self/supervisor rating, Q/E/T score, weighted score, calibration, final rating, appeal, report, PIP, L&D/R&R feed, or other Phase 6E/6F behavior was introduced. No Administrative, PrimeHR, Employee Portal, or CoreHR frontend was changed.

## Required stop

Phase 6D.3 remains separately approval-gated. Do not add its Administrative permission rows, PrimeHR/Employee Portal interfaces, or Playwright acceptance until the user explicitly approves that run.
