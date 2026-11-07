# Phase 12A - Notification Foundation and Authenticated-Own Feed Review

Date: 2026-11-06

## Delivered

- A provider-neutral PrimeHR notification aggregate with an exact agency and employee recipient,
  immutable source lineage/version, recipient-scoped idempotency key, controlled domain/event/route codes,
  bounded safe title/message definitions, optional due date, action state, read/dismiss state, audit metadata,
  optimistic locking, and a retention classification.
- Paired PostgreSQL and SQL Server V57 migrations with recipient/event uniqueness, controlled-value checks,
  unread/feed indexes, and no destructive migration statement.
- An internal typed publisher whose definitions reject unsupported domain/event combinations and never accept
  arbitrary URLs, HTML, notification text, source payloads, or browser-supplied recipients.
- Authenticated-own APIs for a bounded pageable feed, unread count, mark-read, mark-all-read, and dismiss.
  Employee identity is derived from the authenticated principal; another employee or agency cannot be selected.
- Informational notifications may be dismissed; action-required notifications cannot be dismissed or resolved
  through the notification API. Read/dismiss retries are idempotent, while real competing changes remain
  optimistic-lock protected. Mark-all-read advances audit metadata and record versions.
- OpenAPI, domain, service, controller, migration parity, populated V56-to-V57 upgrade, fresh-schema, Hibernate,
  and application-context coverage.

## Safety boundary

Phase 12A does not activate any domain workflow producer. It adds no reminder scheduler, PrimeHR/Employee Portal
interface, applicant notification, administrator feed browsing, broadcast, arbitrary route/content, workflow
mutation, CoreHR notice, broker/outbox, WebSocket/SSE, email/SMS/push delivery, or retention purge.

## Verification

- Focused notification/OpenAPI/migration tests passed.
- Flyway fresh-schema, V56-to-V57 upgrade, migration parity, Hibernate schema validation, and PrimeHR application
  context tests passed.
- PrimeHR full regression passed **562/562** with zero failures, errors, or skips.
- PrimeHR production JAR packaging passed.

## Provider limitation

No approved live PostgreSQL or SQL Server target was configured for this unit. Portability evidence is the paired
V57 migrations plus structural parity, populated upgrade, fresh-schema, and Hibernate validation tests. A live
provider migration remains a deployment gate.

## Next gate

The next proposed unit is **Phase 12B - authoritative workflow projections and action lifecycle**. It must add
only events with an unambiguous authoritative recipient and safe release rule, and it must stop before reminders
and user interfaces in Phase 12C.
