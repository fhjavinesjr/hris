# Phase 6F.1 — Report Contract and Immutable Metadata Review

Date: 2026-10-23  
Status: Complete; stop before Phase 6F.2

## Delivered

- Compared legacy OPCR, DPCR, IPCR, monitoring, coaching and development JRXML structures with official CSC and Philippine government SPMS sources.
- Recorded the form adjudication and Phase 6F boundaries in `PHASE_6F_REPORTING_DOWNSTREAM_SCOPE_APPROVAL.md`.
- Added paired PostgreSQL and SQL Server V36 migrations for immutable report metadata.
- Added the report kind/source contract, JPA entity, repository and deterministic SHA-256 capture service.
- Enforced idempotent capture and rejection of conflicting metadata for an already frozen source revision.
- Added migration parity, populated upgrade, fresh migration/Hibernate validation and service tests.

## Verification

```text
.\mvnw.cmd -pl PrimeHR '-Dtest=PerformanceReportMetadataSnapshotServiceTest,CompetencyMigrationParityTest,PrimeHrV35ToV36UpgradeTest' test
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

.\mvnw.cmd -pl PrimeHR '-Dtest=PrimeHrFlywaySchemaIntegrationTest' test
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

.\mvnw.cmd -pl PrimeHR clean package
Tests run: 336, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The first focused run exposed use of `TIMESTAMPTZ`, which the existing portability harness does not accept. V36 was corrected to the established `TIMESTAMP WITH TIME ZONE` form before the successful gates.

## Boundary

No Jasper template, PDF/API endpoint, permission seed, frontend change, analytics result, downstream feed, or consumer-domain mutation was introduced. Phase 6F.2 formal SPMS reports are next.
