# Phase 12 - Action Inbox and In-App Notifications Discovery and Proposed Scope

Date: 2026-11-06  
Status: Complete; Phases 12A-12C delivered and verified  
Authority: completed repository Phases 1-11, the current checked-out repositories, and the
repository's established ownership, privacy, permission, and integration rules

## 1. Decision and purpose

The repository master-plan work is complete through Phase 11. No Phase 12 was previously approved.
The next recommended capability is **Phase 12 - Action Inbox and In-App Notifications**.

This phase does not add a seventh PrimeHR business domain and does not replace any workflow. It gives
authenticated users one bounded view of actions and notices already produced by Competency, RSP,
Performance Management, L&D, Rewards and Recognition, Succession, Evidence Center, and Operations.
The source workflow remains authoritative for permission, assignment, lifecycle, due date, and the
action itself.

The recommendation is evidence-driven:

- completed PrimeHR workflows already contain assignments, returns, approvals, decisions, deadlines,
  acknowledgments, evaluation tasks, corrective actions, and operational findings;
- management users currently visit separate domain inboxes to find those tasks;
- the Employee Portal notification bell has real setup/overtime warnings but still contains mock
  announcement, leave, payslip, and memo rows;
- PrimeHR has no notification entity, notification API, outbox, broker, email/SMS integration, or
  shared notification preference model;
- Phase 11 now supplies the deployment, reliability, reconciliation, observability, and support
  foundation required for a controlled in-application capability.

This document records the approved staged implementation. Phases 12A-12C are complete.

## 2. Repository evidence inspected

Discovery covered:

- the completed domain and operations APIs, workflow states, assignment/roster snapshots, due dates,
  permissions, audit, idempotency, and frontend inbox routes;
- PrimeHR and Employee Portal authentication, runtime configuration, direct-route protection, and
  authenticated-own endpoint patterns;
- the Employee Portal notification component and its current mix of real warnings and mock data;
- the absence of a backend notification/outbox/broker/WebSocket/SSE implementation;
- Phase 0 deferred-event rules, Phase 11 integration boundaries, paired migration patterns through
  V56, and PostgreSQL/SQL Server acceptance requirements;
- existing privacy boundaries for applicants, succession candidates, nominations, private coaching
  notes, evidence, appeals, deliberations, and restricted operational details.

The working trees contain the user's uncommitted completed PrimeHR phases. Discovery did not reset,
commit, reformat, or overwrite that work.

## 3. Current-state findings

| Priority | Finding | Consequence | Required treatment |
|---|---|---|---|
| High | There is no consolidated assigned-work inbox | Users must discover work by opening each domain workspace | Build a recipient-scoped read model whose links return to the authoritative workflow |
| High | There is no persisted PrimeHR notification model | Read/unread state, idempotence, reminders, and lifecycle closure cannot be reliable | Add paired provider-neutral persistence with immutable source lineage and mutable recipient state |
| High | Employee Portal notification content is partly mock data | The bell can imply leave, payslip, announcement, and memo events that were not produced by a backend | Remove mock rows when the real PrimeHR feed is introduced; do not claim unsupported CoreHR notifications |
| Critical | Several domains contain confidential or restricted records | Copying free text or broad snapshots into a notification could disclose applicant, succession, appeal, coaching, evidence, or deliberation data | Store a bounded safe title/message code and source reference; fetch details only from the protected source endpoint |
| Critical | A notification is not authority | A stale/deep link must never grant access to its target | Re-run the source endpoint's exact action, scope, process-role, ownership, and lifecycle checks |
| High | Applicant identity is separate from employee identity | A shared recipient table or browser token would cross the security boundary | Exclude applicant notifications from the initial phase |
| Medium | No broker/outbox infrastructure exists | Introducing distributed delivery would expand deployment and recovery risk | Keep Phase 12 inside the PrimeHR transaction/database; no broker or cross-service event transport |
| Medium | Due dates exist but reminder policy is not centralized | Hard-coded reminder intervals would be agency-specific and noisy | Use disabled-by-default/configured reminder rules with idempotent generation and bounded schedules |
| Medium | Notification retention periods are not approved | Automatic deletion could violate records or legal-hold requirements | Tag records for Phase 11 retention reporting; do not purge in Phase 12 |

## 4. Ownership and security model

| Concern | Authoritative owner | Phase 12 rule |
|---|---|---|
| Workflow state, assignee, due date, decision and action | Existing PrimeHR domain | Notification references it; it does not duplicate or modify it |
| Employee identity and appointment | HRM | Use authenticated employee identity and existing versioned reads only |
| Permission and agency scope | Administrative | Existing exact source permission remains mandatory; fail closed |
| Notification/read state | PrimeHR | PrimeHR owns recipient state, source lineage, safe presentation fields and audit |
| Portal identity | Administrative-issued employee JWT/SSO | Authenticated-own reads only; never accept another employee number from the browser |
| Applicant identity | Applicant security boundary | Excluded until separately scoped |
| Email/SMS/push delivery | Not established | Excluded |

Every notification has one exact recipient, agency, source domain, source aggregate type/id/version,
event key, safe presentation code, created timestamp, optional due timestamp, action-state projection,
and read/dismiss state. Event keys are unique per recipient so retries cannot duplicate a notice.

The stored route is a controlled route key plus source identifier. Arbitrary URLs, HTML, scripts,
tokens, filenames, private notes, report parameters, and source payload JSON are forbidden.

## 5. Proposed delivery units

| Unit | Precise implementation scope | Migration | Verification gate | Stop boundary |
|---|---|---|---|---|
| **12A - Notification foundation and authenticated-own feed** | Provider-neutral notification and recipient-state model; source/event idempotency key; safe route registry; unread count; pageable own feed; mark-read, mark-all-read and dismiss actions; audit/correlation; retention tag/legal-hold compatibility; management-user and Employee Portal authenticated-own API contracts | Paired **V57** PostgreSQL/SQL Server migrations | Domain/unit/controller/security tests; V56-to-V57 populated upgrade, parity, fresh-schema and Hibernate validation; another employee/agency denied; duplicate event idempotent; unsafe route/content rejected; full regression/package | No workflow producers, reminders, UI, applicant notification, broadcast, broker, email/SMS/push, or purge |
| **12B - Authoritative workflow projections and action lifecycle** | Transactional creation/resolution from approved source transitions; source permission/assignment revalidation; action-required versus informational classification; due-date update; automatic closure when source action is no longer pending; bounded safe text by event type; initial coverage for management assignments and employee-visible outcomes across Performance, L&D, R&R, Succession, Evidence, RSP employee/staff workflows, and Operations | None expected beyond V57 unless a constrained mapping registry is approved | Each event maps to one intended recipient and source revision; rollback leaves no orphan notice; retry creates no duplicate; reassignment closes/reissues correctly; confidential fields absent; source denial remains denial; full regression | No workflow mutation through the notification endpoint, applicant notices, CoreHR leave/payroll/timekeeping notices, confidential deliberation text, or cross-service eventing |
| **12C - Inboxes, configurable reminders, and acceptance** | PrimeHR action-inbox/bell UI; Employee Portal My Notifications/bell integration; unread badge, filters, accessible empty/error/loading states, safe deep links and stale-action handling; disabled-by-default bounded due reminder scheduler; protected operations metrics without recipient labels; Administrative configuration only for non-secret reminder timing/enablement if authoritative settings are approved; Playwright and production package acceptance | None expected | Exact direct-route and own-recipient tests; unread/read/dismiss; action resolution; safe stale link; reminder idempotence/time boundary; confidential-notice redaction; no mock portal rows; lint/type/build/package; packaged Playwright; clean backend regression | No browser push, WebSocket/SSE requirement, email/SMS, administrator browsing of personal feeds, broadcast announcements, notification templates containing free HTML, or automatic retention deletion |

## 6. Initial event catalogue

The exact catalogue must be implemented as typed codes, not arbitrary free-text events. Phase 12B
starts with actions/outcomes whose recipient already exists in an immutable assignment, route, roster,
or authenticated-own record.

| Domain | Action-required examples | Informational examples | Privacy rule |
|---|---|---|---|
| Performance | plan/commitment review, monitoring return, supervisor assessment, calibration/finalization, appeal review | commitment approved/returned, final rating available, appeal/intervention outcome | Never copy private coaching notes, appeal evidence, PMT deliberation, or scores not yet released |
| L&D | IDP/nomination approval, enrollment confirmation, evaluation task, supervisor follow-through | nomination/enrollment/completion/evaluation outcome | Employee sees only own released facts; no evaluator-private narrative |
| Rewards and Recognition | eligibility/evaluation/deliberation/final-decision assignment | nominee-visible released outcome or fulfillment update | No confidential nomination source, score sheet, recusal reason, or deliberation text |
| Succession | committee/readiness/development/continuity/referral assignment | employee-visible development or membership notice only when the source workflow permits it | Candidate status, bench, readiness and restricted rationale remain confidential unless explicitly released |
| Evidence Center | owner/validator assignment, returned case, corrective-action due/verification | finalized internal-assessment outcome for authorized participants | No evidence content, filenames, restricted findings, or official governance-level claim |
| RSP | employee/staff screening, evaluation, selection, handoff assignment | employee/staff workflow outcome already authorized for that identity | Applicant notices remain in the separate applicant boundary and are excluded |
| Operations | unresolved reconciliation finding or dependency/recovery action | finding resolution/recovery status for authorized operations staff | No dependency URL, token, employee/source payload, filename, or document content |

The final catalogue is allowed to narrow during 12B implementation when a source transition lacks an
unambiguous recipient or safe release rule. It must not guess a supervisor or recipient.

## 7. API and behavior requirements

- Own-feed APIs derive the recipient from the authenticated principal and agency resolver.
- The client cannot submit `employeeNo`, agency, permission, source status, title, body, route, due
  date, or read owner when creating a notification.
- Page size is bounded and ordering is deterministic by created timestamp and identifier.
- Unread count uses an indexed recipient/status query and does not load the complete feed.
- Mark-read/dismiss is idempotent, recipient-owned, optimistic-lock safe, and audited without copying
  sensitive message content into logs.
- Dismiss hides an informational notice from the user's active feed; it does not resolve, approve,
  reject, withdraw, or delete the source workflow.
- Action-required notices close only from an authoritative source transition or reconciliation, not
  because a user marked or dismissed them.
- A deep link re-fetches the source record. Missing, reassigned, completed, or unauthorized sources
  show a safe stale/unavailable result and cannot be acted on.
- Reminder generation is bounded, idempotent, observable, and disabled unless configured. It uses
  authoritative due dates and never invents a deadline.
- Metrics use only bounded domain/event/outcome labels. Employee numbers, aggregate IDs, names,
  routes, titles, message text, and reasons are prohibited labels.

## 8. Explicit exclusions

Phase 12 excludes:

- RabbitMQ/Kafka or another broker, transactional integration outbox, cross-service event replay,
  dead-letter infrastructure, gateway, WebSocket, or SSE requirement;
- email, SMS, mobile push, browser push, or third-party notification vendors;
- applicant notifications or reuse of employee JWTs for applicants;
- CoreHR leave, overtime, DTR, payroll, payslip, announcement, or memo notification implementation;
- administrator impersonation or browsing of personal notification feeds;
- free-form broadcast, arbitrary URL, HTML notification templates, attachments, or document content;
- changing approval routes, assignees, due dates, decisions, balances, appointments, payroll, or any
  source workflow through notification APIs;
- copying confidential succession, applicant, appeal, coaching, evidence, nomination, evaluation,
  deliberation, operational, or personal payloads into notification records;
- automatic deletion or destructive retention processing;
- claiming official CSC notification, submission, accreditation, or PRIME-ESG determination.

## 9. Risks and controls

| Risk | Control |
|---|---|
| Stale or misleading action | Revalidate source assignment/state on read and click; close from authoritative transitions |
| Duplicate notices | Unique source event key per recipient plus transactional/idempotent producer |
| Confidentiality leak | Typed safe-text catalogue, no source payload/free text, own-recipient query, source reauthorization |
| Notification becomes authorization | Notification APIs never execute source commands; target endpoint enforces its existing guard |
| Excessive reminders | Disabled/configured bounded schedule, idempotency, due-date requirement and operations metrics |
| Cross-database inconsistency | Initial producers are PrimeHR-internal and commit in the same database transaction |
| Retention/legal hold conflict | Phase 11 retention tags/eligibility only; no purge |
| Scope explosion into CoreHR | Remove unsupported mock claims but do not implement CoreHR notifications in this phase |

## 10. Proposed approval

The recommended approval is:

1. treat Phase 12 as a PrimeHR-owned in-app action inbox/notification capability, not a new HR domain;
2. keep delivery inside the PrimeHR database and existing synchronous APIs;
3. begin with **12A notification foundation and authenticated-own feed**;
4. activate only events with an unambiguous recipient and safe release rule in 12B;
5. expose management and Employee Portal interfaces only after backend ownership/privacy gates pass;
6. retain broker/outbox, applicant, external delivery, CoreHR notification, broadcast, and purge work
   behind separate future approvals.

Phases 12A-12C were approved and implemented. Phase 12B activated only the authoritative Performance
commitment and Evidence assessment/corrective-action catalogue documented in its review. Phase 12C
added authenticated-own PrimeHR and Employee Portal interfaces, removed unsupported portal mock
claims, and supplied bounded reminders that remain disabled unless deployment configuration enables
them. The exclusions in this scope remain in force.
