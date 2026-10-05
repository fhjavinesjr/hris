# Phase 6E.3 Acknowledgment, Appeal, and Intervention Review

Prepared: 2026-09-25

Status: Complete and verified; stopped before Phase 6E.4

## Delivered outcome

Phase 6E.3 adds the backend workflow after an immutable final rating: employee receipt acknowledgment, authorized closure when acknowledgment is not obtained, employee-owned appeals with secured evidence, non-conflicted PMT decisions, successor final-rating revisions for modified outcomes, and exact-band intervention/PIP follow-through.

The implementation preserves every original final rating. A modified appeal reruns the authoritative Phase 6E.1 server calculation and creates a linked successor final-rating revision; it never overwrites or accepts a browser-calculated score.

## Authority, lifecycle, and privacy controls

- `primehr.performance-appeal` and `primehr.performance-intervention` enforce independent actions and Own Records, Assigned Records, or Agency Wide scope.
- Employee identity is derived from authentication; an employee cannot select another employee's rating, appeal, acknowledgment, or intervention.
- Acknowledgment confirms receipt only. Authorized closure without acknowledgment requires agency-wide Finalize authority, effective PMT membership, and a reason.
- Appeals are tied to the exact latest final-rating revision and governed by the configured `APPEAL_DUE` window.
- Appeal evidence uses secured document storage, generated object keys, MIME/magic/size/checksum validation, and never exposes its storage key.
- Appeal decisions require agency-wide authority and an effective, voting, non-conflicted PMT member. The subject and assigned rater cannot decide the appeal.
- A modified decision creates a successor final rating linked through `supersedes_id` and re-evaluates intervention rules against that exact result.
- Intervention rules bind an exact policy version, rating-scale version, and rating-band ID to `NONE`, `ALERT`, `PIP_RECOMMENDED`, or `PIP_REQUIRED`; labels and hidden numeric thresholds are not used.
- PIP lifecycle and checkpoints preserve employee-visible feedback separately from restricted PMT notes. Own-record responses redact PMT-private checkpoint notes.
- Retried transitions use the established rating-action idempotency ledger, optimistic versions, and audit events.

## Persistence and contract

Paired V35 PostgreSQL and SQL Server migrations add final-rating successor linkage plus:

- `spms_rating_acknowledgment`
- `spms_rating_appeal`
- `spms_rating_appeal_evidence`
- `spms_intervention_rule`
- `spms_performance_intervention`
- `spms_intervention_checkpoint`

REST remains under `/api/primehr/v1/performance-management` and exposes acknowledgment, appeal/evidence/decision, intervention-rule, intervention, checkpoint, and PIP lifecycle operations. The OpenAPI contract documents the Phase 6E.3 routes and safe DTOs.

## Verification evidence

- Focused domain, permission, OpenAPI, migration-parity, and V34-to-V35 upgrade gates passed.
- The PostgreSQL-compatible Flyway V1-to-V35 chain, Hibernate validation, provider-profile expectations, and repository startup passed.
- The complete clean PrimeHR test/package regression passed 330/330 with zero failures, errors, or skips and produced the executable JAR.
- Acknowledgment/closure, own-record appeal, modified successor rating, exact-band intervention creation, privacy filtering, process-role restrictions, idempotency, and stale-state behavior are covered by focused tests.

Live PostgreSQL and SQL Server endpoints were not configured for this run. Paired structural parity, PostgreSQL-mode Flyway/Hibernate validation, and provider-neutral Java/JPA were verified; live-provider execution remains an explicit deployment gate.

## Boundary confirmation

No Administrative permission seed, PrimeHR or Employee Portal interface, Playwright workflow, Jasper/analytics report, downstream competency/L&D/R&R/RSP feed, CoreHR mutation, payroll/timekeeping effect, disciplinary action, or Phase 6F behavior was introduced.

## Next approval gate

Phase 6E.4 Administrative controls, PrimeHR and Employee Portal interfaces, and Playwright acceptance is the next planned unit and requires explicit approval. Stop before Phase 6F reporting and downstream feeds.
