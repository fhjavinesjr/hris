# Phase 10A - Evidence Framework Foundation Review

Reviewed: 2026-11-05  
Status: Complete

## Delivered scope

Phase 10A establishes the agency-controlled definition layer for internal PRIME-ESG evidence
readiness work. It delivers:

- a stable framework identity with versioned draft, published and retired revisions;
- legal-basis, effectivity, successor and retirement lineage;
- ordered pillars, reference-only Governance Level labels, indicators and evidence requirements;
- controlled requirement types for live transactions, reports, documents, policies and other
  evidence, with optional recurrence and freshness rules;
- publication completeness, effectivity-overlap, stale-write and published-immutability guards;
- an Agency Wide `primehr.evidence-framework` permission with independent access, add, edit,
  publish and archive actions;
- full PrimeHR mutation audit records;
- versioned REST/OpenAPI contracts; and
- equivalent PostgreSQL and SQL Server V53 migrations.

Governance Level labels are descriptive reference data only. The implementation does not calculate,
award, certify or imply an official CSC PRIME-ESG Governance Level.

## Primary implementation

- `PrimeHR/src/main/java/com/primehr/evidence/**`
- `PrimeHR/src/main/java/com/primehr/security/EvidenceFrameworkPermissionGuard.java`
- `PrimeHR/src/main/resources/db/migration/postgresql/V53__evidence_framework_foundation.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V53__evidence_framework_foundation.sql`
- `Administrative/src/main/java/com/administrative/impl/EffectiveAuthorizationServiceImpl.java`
- `contracts/openapi/primehr-v1.yaml`

## Verification

- affected-module compilation passed;
- framework domain and permission tests passed;
- PostgreSQL/SQL Server migration parity and populated V52-to-V53 upgrade tests passed;
- fresh H2/PostgreSQL-compatible Flyway migration through V53 and Hibernate validation passed;
- framework draft/content/publish/effective/revision integration workflow passed;
- Administrative effective-authorization regression passed; and
- the clean complete PrimeHR regression suite passed 498/498 with zero failures, errors or skips.

An initial non-clean regression run discovered one obsolete compiled test class left under `target`
after an earlier package move. Its current source replacement passed. `mvn clean test` removed the
stale bytecode and produced the authoritative 498/498 result above.

Live PostgreSQL and SQL Server endpoints were not configured for this unit. Provider assurance is
therefore based on paired migration parity, upgrade preservation, and the PostgreSQL-compatible fresh
schema/Hibernate gate; live-provider execution remains an explicit deployment validation item.

## Explicit exclusions

Phase 10A adds no evidence submission, assessment case, requirement finding, readiness score,
gap/corrective action, report, analytics, Administrative UI, PrimeHR UI, Employee Portal behavior,
official CSC determination, or CoreHR/Timekeeping/Payroll mutation.

## Next gated unit

Phase 10B is the controlled evidence and internal-assessment workflow. It remains separately gated and
must preserve source ownership, confidentiality, human judgment, exact-version lineage and the
non-official readiness boundary.
