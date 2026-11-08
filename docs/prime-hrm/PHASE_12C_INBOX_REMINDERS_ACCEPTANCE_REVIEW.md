# Phase 12C - Inboxes, Configurable Reminders, and Acceptance Review

Date: 2026-11-07

## Delivered

- A PrimeHR notification bell on every management shell and a dedicated **My Notifications** page.
  The interface uses the authenticated-own feed, unread count, all/unread filters, refresh, read,
  read-all, and informational-dismiss APIs. Action notices cannot be dismissed as workflow actions.
- An Employee Portal notification bell and dedicated **My Notifications** page backed by the same
  authenticated employee feed. The previous unsupported announcement, leave, payslip, and memo mock
  claims were removed. Existing real account-setup warnings and staff-overtime discrepancies remain.
- Controlled target routing. Every Open action first marks the user's own notice read and then calls
  the authoritative target-revalidation endpoint. Missing, completed, reassigned, or unauthorized
  work stays on the inbox with a safe unavailable message. The browser never accepts a URL from the
  notification payload.
- PrimeHR destinations for the activated Performance commitment and Evidence case/action source
  types. The Employee Portal opens only employee-safe Performance commitments. Evidence work has no
  approved portal destination and explicitly directs the employee to the protected PrimeHR module.
- A bounded reminder service over authoritative active action assignments with due dates. Candidate
  reads are limited by agency, lead window, and maximum batch size; every source is revalidated;
  stale actions are closed; retries replay the deterministic source-notification/due-time event key.
- Disabled-by-default scheduling. Deployment operators must explicitly set
  `PRIMEHR_NOTIFICATION_REMINDERS_ENABLED=true`; lead hours, fixed delay, and maximum batch size have
  bounded defaults/configuration. No secret or employee-specific configuration is introduced.
- Reminder metrics use only `issued`, `replayed`, and `stale` outcome labels. They contain no
  employee number, source identifier, route, title, message, or reason.
- OpenAPI version `12.3.0-phase-12c` and a packaged Playwright suite that starts both standalone
  production artifacts.

## Authoritative behavior and privacy

- Notification reads remain authenticated-own. Neither UI accepts an employee number or agency from
  the browser.
- A notification remains a projection, never authority. Target revalidation remains mandatory and
  source workflow commands remain in their existing protected modules.
- Safe typed titles/messages only are rendered. No source payload, private coaching text, evidence,
  attachment, arbitrary HTML, token, or arbitrary URL is stored or displayed.
- Only the Phase 12B Performance commitment and Evidence assessment/corrective-action catalogue is
  active. L&D, Rewards and Recognition, Succession, RSP staff, Operations, applicants, and CoreHR
  remain outside the notification catalogue.
- There is no schema change beyond V57 and no automatic retention deletion.

## Verification

- Focused PrimeHR notification, migration, security, reminder, service, projection, controller, and
  OpenAPI tests: **75 passed**, zero failures/errors/skips.
- PrimeHR strict TypeScript: passed.
- PrimeHR full ESLint: passed.
- Employee Portal strict TypeScript: passed.
- Employee Portal source ESLint: passed.
- PrimeHR production build and standalone package: passed.
- Employee Portal production build and standalone package: passed. The existing Next.js
  `images.domains` deprecation warning remains unrelated to Phase 12C.
- Packaged Chromium Playwright: **3 passed**, covering stale target rejection, controlled PrimeHR
  navigation, authenticated API calls, informational dismissal, portal-safe evidence handling, and
  absence of the unsupported legacy mock claims.
- Focused suite including the full application smoke context: **76 passed**, confirming repository
  JPQL validation and disabled-by-default scheduler startup.
- Clean full PrimeHR regression: **571 passed**, zero failures/errors/skips.
- PrimeHR production JAR packaging: passed.

The packaged Employee Portal acceptance used deployment fallback configuration because no live
centralized runtime-config service was started; API behavior was isolated with authenticated route
mocks. This is expected for repeatable frontend acceptance and is not a product failure.

## Boundary

Phase 12C adds no applicant notifications, CoreHR leave/overtime/DTR/payroll/payslip catalogue,
broadcasts, administrator browsing of personal feeds, source-workflow mutation, arbitrary routes or
HTML, attachments, browser/mobile push, WebSocket/SSE, email/SMS, third-party delivery, broker,
outbox, cross-service event transport, or retention purge.

## Next gate

Phase 12 is complete. There is no approved Phase 13. Any continuation must begin with repository
discovery and an explicit scope table before code changes, while preserving all Phase 12 exclusions
unless the user separately approves a bounded extension.
