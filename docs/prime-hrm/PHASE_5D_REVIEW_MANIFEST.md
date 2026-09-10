# Phase 5D Final Review Manifest

## Delivered contracts

- V18: versioned/effective evaluation policy, publication binding, HRMPSB committee governance, qualified-candidate proceeding and immutable source admission.
- V19: sessions/attendance, assignments, candidate conflicts, independently validated results, member-owned panel ratings, lawful reference checks, secured evidence, HRMPSB meetings/attendance/resolutions, comparative evaluation, and applicant-safe schedules.
- V20: provider-correct uniqueness for one current proceeding per vacancy while allowing multiple cancelled/finalized historical rows.
- Administrative: three exact feature rows with independent action flags and agency-wide data scope.
- PrimeHR: five permission-gated staff routes plus applicant-safe Careers progress/schedule display.
- Acceptance: focused Phase 5D Playwright and complete regression suite, frontend gates, backend clean package, OpenAPI and provider checks.

## Security and integrity review

- Employee and applicant JWT boundaries remain separate.
- Page visibility is not treated as authorization; controllers enforce exact action and scope.
- Committee membership/effectivity, assignment ownership, separation of duties, conflict/recusal, minimum raters, quorum and optimistic versions are server enforced.
- Another member's unsubmitted rating remains private.
- Evidence upload/download validates authority, ownership, media signature, classification and retention metadata.
- Applicant responses omit internal deliberation and evaluation material.
- Comparative results preserve policy/source versions and deterministic calculation fingerprints; missing mandatory results are rejected rather than scored as zero.

## Provider and reporting review

SQL Server is the browser-acceptance provider. PostgreSQL has paired V18-V20 migrations, structural/parity checks, and PostgreSQL-compatible Flyway/Hibernate validation; a live PostgreSQL instance was not exercised. Shared Java/JPA remains provider neutral.

No Phase 5D Jasper report was introduced. Formal RSP reporting remains outside this phase.

## Repository boundary

Unrelated Administrative `.env`, PrimeHR `next-env.d.ts`, and backend `.idea/compiler.xml` changes are user-owned and were preserved. Phase 5E is not implemented or authorized.

## Deployment order

1. Back up and apply the reviewed provider-specific V18-V20 Flyway migrations.
2. Deploy Administrative and PrimeHR backend packages.
3. Configure the three exact Administrative permission rows and require affected users to reauthenticate.
4. Deploy both frontend production packages.
5. Run the documented SQL Server smoke/browser acceptance for the target environment.

Applied migrations are forward-only; do not edit V18-V20 after deployment. Use a new reviewed migration for any correction.
