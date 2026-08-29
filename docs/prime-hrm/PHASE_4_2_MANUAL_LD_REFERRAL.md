# Phase 4.2 - Manual L&D Referral

Status: Complete on 2026-08-28; stopped before Phase 4.3.

Phase 4.2 adds provider-equivalent V10 migrations for `prime_ld_referral` and `prime_ld_referral_item`. An authorized agency-wide user can create and update a draft against one immutable gap analysis, select only `BELOW` or `NOT_ASSESSED` items, archive draft items, submit the referral, or archive the draft. `REFERRED` records are immutable and mean only that the recommendation is ready for a future L&D intake process.

The service serializes item claims by locking the immutable analysis and rejects a gap item already selected by another active DRAFT/REFERRED referral. Root and child mutations validate optimistic record versions. Submission requires at least one active actionable item and is transactional and audited.

REST endpoints are under `/api/primehr/v1/ld-referrals`. Administrative feature `primehr.ld-referral` enforces Access, Add, Edit, Delete/archive, and Submit independently with agency-wide scope; administrator compatibility remains intact.

Focused verification passed 35 tests with no failures, errors, or skips. PostgreSQL-mode Flyway migrated V1-V10 and Hibernate validated all entities. Real SQL Server passed fresh V1-V10 (9 tests) and populated V9-V10 upgrade (1 test) in retained isolated schemas `primehr_phase42_fresh_20260828` and `primehr_phase42_upgrade_20260828`. Live PostgreSQL was not run under the approved SQL Server-primary policy.

The affected clean test passed Common 3, Administrative 34, and PrimeHR 113 tests (150 total). After adding explicit controller authorization coverage, the final-source package gate passed Common 3, Administrative 34, and PrimeHR 116 tests (153 total). Both had zero failures, errors, or skips. Diff/whitespace, generated-file, credential, provider-neutral query, and phase-boundary audits passed.

The implementation has no dependency, table, route, event, or write for IDP, training request, enrollment, annual plan, HRM L&D, notification, payroll, UI, Jasper, or Playwright. Those boundaries remain outside Phase 4.2.
