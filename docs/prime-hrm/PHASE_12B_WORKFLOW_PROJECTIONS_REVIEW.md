# Phase 12B - Authoritative Workflow Projections and Action Lifecycle Review

Date: 2026-11-07

## Delivered

- A transactional projection service for typed action assignments, returns, outcomes, reassignment,
  and authoritative closure. Projection retries use deterministic recipient-scoped event keys and do
  not embed raw employee numbers in those keys.
- Exact-source action closure for active action-required notifications. Route advancement,
  reassignment, return, approval, rejection, withdrawal, voiding, and finalization close obsolete
  work instead of leaving stale actionable rows.
- Authenticated-own target resolution at
  `GET /api/primehr/v1/notifications/me/{id}/target`. Resolution revalidates the authoritative source
  record and current recipient before returning the controlled route/type/id. Unsupported, missing,
  completed, reassigned, or otherwise unavailable sources fail closed; stale active actions are
  closed and audited.
- Performance commitment projections for submit/resubmit, sequential recommendation and approval,
  return, reject, withdraw, void, and route rebase. Only the current exact route actor receives an
  action, while released return/final outcomes go to the commitment owner.
- Evidence Center projections for assessment-case validator assignment/reassignment/recusal,
  return/finalization, and corrective-action responsible/validator handoff, return, verification,
  and cancellation. Corrective-action due dates come from the authoritative target date in the
  approved Asia/Manila business timezone.
- Source-specific revalidators for Performance commitments, Evidence assessment cases, and Evidence
  corrective actions. They validate agency, source type/id/version context, lifecycle state, exact
  owner/current actor, and recusal state without treating the notification itself as authority.
- OpenAPI 12.2.0 coverage and controller/service/integration tests for ownership, stale resolution,
  deterministic publication, exact reviewer handoff, automatic closure, and owner outcomes.

## Catalogue decision

The Phase 12 discovery explicitly allows the event catalogue to narrow when a source transition lacks
an unambiguous recipient or safe release rule. Phase 12B therefore activates only:

| Domain | Activated source | Action recipient | Released informational recipient |
|---|---|---|---|
| Performance | `SPMS_COMMITMENT_VERSION` | exact current approval-route actor | commitment owner on return/final outcome |
| Evidence | `EVIDENCE_ASSESSMENT_CASE` | exact non-recused validator | case owner on return/finalization |
| Evidence | `EVIDENCE_CORRECTIVE_ACTION` | responsible employee, then exact case validator | responsible employee on return/final outcome |

L&D, Rewards and Recognition, Succession, RSP staff, and Operations projections remain inactive.
Their workflows were not guessed or connected merely to satisfy breadth: restricted release rules,
recipient semantics, or an explicit assignee are not uniformly safe at the current transition
boundaries. Applicant notifications remain excluded by design. A later scoped catalogue extension
must add an authoritative source validator and lifecycle acceptance tests before activating any of
those sources.

## Safety boundary

Phase 12B adds no reminder scheduler, frontend interface, browser notification transport, applicant
notice, CoreHR leave/payroll/timekeeping notice, broadcast, broker/outbox, email/SMS/push delivery,
workflow mutation through a notification endpoint, confidential source text, or retention purge.
No schema migration was needed beyond the Phase 12A V57 foundation.

## Verification

- Focused notification, Performance workflow, Evidence workflow, and OpenAPI suite passed **80/80**.
- Clean complete PrimeHR regression passed **569/569** with zero failures, errors, or skips.
- PrimeHR production executable JAR packaging passed.
- Existing V57 PostgreSQL/SQL Server parity and migration coverage remained unchanged because Phase
  12B introduced no schema change. No approved live PostgreSQL or SQL Server target was available;
  live-provider deployment validation remains a release gate.

## Next gate

The next proposed unit is **Phase 12C - inboxes, configurable reminders, and acceptance**. It must
implement the PrimeHR and Employee Portal notification interfaces, remove unsupported portal mock
rows, keep reminders disabled by default and bounded to authoritative due dates, and complete strict
frontend/build/Playwright acceptance. It must retain the Phase 12 external-delivery, applicant,
CoreHR-notification, broadcast, and destructive-retention exclusions.
