# Phase 7D.1 - L&D Attendance, Evidence, and Completion Review

Reviewed: 2026-11-02  
Status: Complete and verified; stopped before Phase 7D.2

## Delivered scope

Phase 7D.1 adds a separate delivery-completion aggregate after Phase 7C participation approval:

- one completion case can be opened only for a confirmed enrollment included in the immutable,
  approved Training Order participant roster;
- offering sessions are snapshotted into per-participant attendance rows with scheduled, attended,
  and credited hours;
- attendance hours cannot exceed scheduled hours, and credited hours cannot exceed either attended
  or scheduled hours;
- required completion outputs are case-scoped and must be supported by secured evidence before
  verification, or explicitly waived with a reason;
- completion evidence supports output, certificate, attendance, and other evidence classifications;
- certificate number, issuer, and issue date are mandatory for certificate evidence; and
- the lifecycle is `DRAFT -> FOR_VERIFICATION -> COMPLETED | NOT_COMPLETED`, with terminal records
  immutable and a deterministic completion fingerprint.

This aggregate deliberately separates nomination/enrollment approval from proof that delivery was
actually attended and completed. It preserves the approved Training Order and offering snapshots
instead of rewriting them.

## Authorization and integrity

- Delivery staff with Assigned Records scope can prepare only programs for which they are the
  snapshotted accountable employee; Agency Wide authority can work across the agency.
- Participant endpoints force the authenticated employee identity for own-case reads, uploads, and
  downloads. Browser-supplied employee identifiers are not trusted.
- Only an Agency Wide verifier can return or finalize a submitted case, and the submitter cannot
  verify the same case.
- Every mutation is lifecycle-checked, optimistically versioned, transactional, and audited.
- Evidence uploads enforce configured storage enablement, maximum size, allowed media type, PDF/PNG/
  JPEG content signature, safe filenames, checksum capture, and storage cleanup on persistence error.
- Evidence downloads are authorized, audited, attachment-only, `no-store`, and `nosniff`.
- Final credited hours are derived from the attendance records. A completed decision requires
  supporting evidence and a completion date on or after the final session and not in the future.

## Database and contract

Paired PostgreSQL and SQL Server V41 migrations add:

- `ld_completion_case`;
- `ld_session_attendance`;
- `ld_completion_output`; and
- `ld_completion_evidence`.

The migrations include provider-neutral foreign keys, lifecycle/value checks, unique enrollment,
session, and output-code rules, and permission-query indexes. The OpenAPI contract is versioned as
`7.5.0-phase-7d1` and covers staff and participant reads, case opening, attendance, outputs, secured
evidence, submission, return, and finalization.

## Verification

- Focused Phase 7D.1 domain, service integration, OpenAPI, migration parity, and populated
  V40-to-V41 upgrade suite: **83/83 passed**.
- Fresh V1-to-V41 migration plus Hibernate schema validation: **9/9 passed**.
- Full clean affected reactor: Common **3/3**, Administrative **74/74**, and PrimeHR **394/394**
  passed with zero failures, errors, or skips; Administrative and PrimeHR production JARs were built.
- V41 was exercised in H2 PostgreSQL compatibility mode for fresh and populated upgrades. No
  configured live PostgreSQL or SQL Server endpoint was available in this workspace.

## Stop boundary

No versioned evaluation instrument/result, LAP/follow-through, delayed supervisor evaluation,
intervention/provider effectiveness, completion outcome feed, report rendering, frontend route,
Playwright behavior, CoreHR/PDS write, notification, automatic competency/profile update, or
performance/reward/promotion decision was introduced. Phase 7E remains the frontend, reporting, and
browser-acceptance gate. The next gated backend unit is Phase 7D.2.
