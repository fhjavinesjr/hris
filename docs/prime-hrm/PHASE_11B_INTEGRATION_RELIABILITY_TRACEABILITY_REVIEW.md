# Phase 11B — Synchronous Integration Reliability and Traceability Review

Date: 2026-11-06

## Outcome

Phase 11B centralizes PRIME-HRM's synchronous Administrative and HumanResource REST transport
without changing endpoint DTOs, source ownership, or workflow semantics. All browser-user scoped
reads continue to forward the caller's authorization. Appointment handoff continues to use its
short-lived, narrowly scoped service JWT and established idempotency key.

No database migration is introduced. PRIME-HRM remains a standalone service and database; this
unit adds no direct CoreHR database access, generic service impersonation, broker, or outbox.

## Shared client policy

| Client | Authorization context | Retry policy |
|---|---|---|
| Administrative authorization, organization, position target, RSP source, performance planning, and performance report sources | calling user's bearer token | GET/HEAD only, at most the configured 1–3 attempts |
| HR assessment subject, performance participant, employee directory, and Plantilla occupancy | calling user's bearer token | GET/HEAD only, at most the configured 1–3 attempts |
| HR appointment handoff delivery | short-lived service JWT plus idempotency key | no automatic POST retry |
| HR appointment handoff receipt reconciliation | short-lived service JWT | one transport attempt in this client; workflow-controlled reconciliation remains authoritative |

Both dependency clients use their configured connect and read timeouts. Safe-read attempts default
to two with a 100 ms delay and may be configured through `PRIMEHR_SAFE_READ_MAX_ATTEMPTS` and
`PRIMEHR_SAFE_READ_RETRY_DELAY_MILLIS`. The maximum is clamped to three. Retry logging records only
the dependency, attempt, and status/failure category; it does not print a URL, token, employee
identifier, or response body.

## Error contract

| Downstream result | PRIME-HRM behavior |
|---|---|
| `401` or `403` | fail closed as access denied |
| `404` | source not found |
| `409` | stable application/source conflict |
| `429`, `502`, `503`, or `504` on a safe read | bounded retry, then dependency unavailable |
| other `5xx` | dependency unavailable |
| timeout/transport failure | bounded safe-read retry, then the client's stable dependency-unavailable error |
| other `4xx` | stable application conflict/rejection |

Dependency-unavailable API responses use HTTP `503` and a stable dependency code. Errors do not
return downstream bodies, stack traces, tokens, SQL, or personal data. Existing specialized
dependency codes remain backward compatible.

## Correlation flow

Every inbound request now has exactly one normalized `X-Correlation-Id`. A valid caller value is
preserved; blank, oversized, or unsafe values are replaced. The value is:

1. available to controllers through the request header;
2. returned in the response header;
3. installed in logging MDC for the request and removed afterward;
4. propagated to outbound REST calls unless an explicit workflow correlation is already supplied;
5. returned as `correlationId` in JSON API and Spring Security error bodies.

The additive `ApiError.correlationId` OpenAPI field lets users provide one safe identifier to
support without exposing technical diagnostics.

## Verification

- Shared-client compilation passed.
- Focused contract, correlation, retry, mutation-safety, status-classification, and application
  smoke acceptance passed **59/59**.
- Safe reads retry transient HTTP and I/O failures; POST is executed once; authorization failures
  are not retried.
- Valid ingress correlation is preserved through response and security error; unsafe correlation is
  replaced; outbound propagation preserves an explicit handoff correlation.
- OpenAPI requires the additive API-error correlation field.
- Clean complete PRIME-HRM regression passed **533/533** with zero failures, errors, or skips.
- Production JAR packaging passed.

No live Administrative/HumanResource outage endpoint was available. The transport behavior is
verified deterministically at the interceptor/status-contract boundary and through the complete
Spring context; an integrated topology outage/recovery check remains a deployment acceptance step.

## Boundary

Phase 11B does not add reconciliation records, retention policies, legal holds, backup/restore
automation, storage consistency scanning, operations UI, metrics dashboards, alert vendors, or
destructive maintenance. Those concerns remain gated behind Phase 11C and 11D.
