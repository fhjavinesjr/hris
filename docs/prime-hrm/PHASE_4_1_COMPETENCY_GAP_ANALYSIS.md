# Phase 4.1 - Priority Configuration and Transparent Gap Analysis

Status: Complete on 2026-08-28.

Phase 4.1 adds the three Administrative feature keys, provider-equivalent V9 migrations, versioned development-priority schemes, and immutable competency-gap analyses. Generation resolves the current HRM appointment fingerprint, Plantilla-before-Job-Position profile, latest valid Person Profile, and effective priority policy on the server. Results retain exact required/attained levels and calculate `required order - attained order`; missing or incompatible results remain `NOT_ASSESSED` rather than zero.

Authorization supports own-record reads and agency-wide reads, while generation and priority administration require agency-wide scope. Repeated request/source tuples are idempotent. Audit records generation and priority lifecycle actions.

Verification passed focused tests, PostgreSQL-mode V1-V9 Flyway/Hibernate validation, real SQL Server fresh V1-V9 and populated V8-V9 upgrades, and the full affected Maven test/package gate. The affected full gate ran Common 3, Administrative 34, and PrimeHR 103 tests, all with zero failures, errors, or skips. A live PostgreSQL instance was not run under the approved SQL Server-primary policy.

No referral, UI, report, Playwright, IDP, training, or later-phase behavior was included.

