# ISOFT PRIME-HRM Phase 1B - Competency Draft Administration

Status: Implemented; final commit-readiness review pending
Completed: 2026-08-03
Approved scope: `PHASE_1B_SCOPE_APPROVAL.md`

## Delivered behavior

Phase 1B adds audited administration for competency categories, proficiency scales and levels, competencies, and behavioral indicators. New definitions begin as `DRAFT`. Drafts can be edited with optimistic `recordVersion` checks or archived with a reason. Existing `ACTIVE` roots are immutable and can only produce a versioned successor draft. No publishing, activation, hard deletion, assessment, position profile, or later-domain feature was added.

The business `definitionVersion` is separate from JPA `recordVersion`. Successor roots retain `supersedesId`; scale successors clone levels into new child IDs and competency successors clone indicators. Every successful command creates one append-only audit event in the same transaction. Stale and illegal lifecycle operations create no audit event.

## Authorization and trusted scope

- Canonical feature: `primehr.competency`.
- Portal module: `primeHr`; SSO target: `primehr`.
- `canAccess`, `canAdd`, `canEdit`, and `canDelete` map to read, create/version, edit/child edit, and archive.
- Administrative resolves effective permission from the authenticated JWT identity and persisted ruleset.
- PrimeHR forwards the bearer token to Administrative and fails closed on denial, timeout, invalid response, or dependency failure.
- Role `1`, an administrator ruleset, and install user `admin` retain unrestricted behavior; all commands remain audited.
- Agency is resolved only from required server configuration `PRIMEHR_AGENCY_ID`; request input cannot override it.

## Persistence

Provider-specific Flyway V2 migrations add lifecycle status, business version, predecessor lineage, version-aware uniqueness, checks, self references, audit storage, and audit indexes. Existing Phase 1A definitions migrate to `ACTIVE`, version 1. Hibernate remains `ddl-auto=validate`.

Validated isolated schemas retained for review:

- PostgreSQL fresh: `primehr_phase1b_20260803_pg`
- PostgreSQL populated upgrade: `primehr_phase1b_20260803_pg_upgrade`
- SQL Server fresh: `primehr_phase1b_20260803_mssql`
- SQL Server populated upgrade: `primehr_phase1b_20260803_mssql_upgrade`

No cleanup is automated because schema removal is destructive.

## API and UI

The admin contract is under `/api/primehr/v1/admin` and is documented in `contracts/openapi/primehr-v1.yaml`. It supports filtered/paged lists, draft create/update, successor creation, archive, nested level/indicator commands, and audit history. No `DELETE` or `PATCH` operation is exposed.

The new standalone `prime-hr-software` application provides strict-TypeScript SSO bootstrap, permission-aware category/scale/competency screens, nested level/indicator management, lifecycle/version actions, error/empty/loading states, audit history, and a return-to-portal link. Administrative can configure both the feature flags and `primeHr` portal visibility. Employee Portal launches the new SSO target through System Config destinations.

## Verification evidence

- Affected backend clean test: 34 tests, 0 failures, 0 errors, 0 skipped.
- Expanded SSO test: 10 tests, 0 failures, 0 errors, 0 skipped.
- Full nine-module `mvn clean package`: BUILD SUCCESS.
- Real Neon PostgreSQL 17.10 fresh V1+V2: 6 tests passed.
- Real SQL Server 2017 (14.0) fresh V1+V2: 6 tests passed.
- Real populated V1-to-V2 upgrade: 1 test passed on each provider.
- Administrative UI production build: passed.
- Employee Portal production build: passed.
- Standalone PrimeHR: lint passed, strict type-check passed, production build passed.

The Common module still has zero tests, a pre-existing gap. Flyway 9.22.3 still warns that PostgreSQL 17.10 is newer than its tested PostgreSQL 15 maximum. The new UI was advanced from Next.js 16.2.6 to patched 16.2.12. The final `npm audit --omit=dev` reports zero vulnerabilities with the committed lockfile (`postcss` 8.4.31 and `sharp` 0.34.5 transitively resolved).

## Deferred work

Publishing/activation and `canPublish`; approval workflows; dynamic multi-agency identity; position/plantilla profiles; assessments; applicants; reports; documents; messaging; analytics; HRM/Timekeeping/Payroll integration; HRISApp assembly. These require a separately approved next phase.
