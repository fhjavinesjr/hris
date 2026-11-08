# Phase 8A.2 - Award Cycles, Nominations, Sources, and Evidence Review

Reviewed: 2026-11-03  
Status: Complete; stopped before Phase 8B

## Delivered

- Agency-scoped award cycles freeze the exact published PRAISE policy version/fingerprint and enforce chronological nomination, evaluation, decision, and optional conferment windows.
- Cycle lifecycle supports draft maintenance, reasoned opening, closing, and cancellation with optimistic locking and audit.
- Individual, group/team, and organization-unit nominations freeze the authenticated nominator plus authoritative HRM appointment and Administrative organization facts.
- Own, Assigned, and Agency Wide nomination scope is backend-enforced. Assigned group nominations require every member to belong to the nominator's assigned business unit.
- Draft nominations may be changed, submitted nominations are immutable, and draft/submitted nominations may be reasonedly withdrawn without hard deletion.
- Duplicate active individual and organization-unit nominations for the same cycle/category are rejected, including nominee changes during draft updates.
- Optional exact source links resolve through application interfaces, never cross-domain repositories: validated competency profile versions, final performance outcomes, and completed L&D outcomes.
- Performance and L&D links require exact version, fingerprint, and change timestamp. Competency links require the exact profile version and capture a server-derived fingerprint because the existing profile response exposes no fingerprint field.
- Documentary evidence uses the established storage abstraction with configured size/media limits, PDF/PNG/JPEG signature validation, checksum, safe filename, no-store download, and audit.
- Committee-restricted evidence metadata/content is hidden from and denied to nominees; nominators and Agency Wide-authorized staff retain access. Phase 8B will add assignment-based committee access.
- Administrative recognizes independent `primehr.rr-cycle` and `primehr.rr-nomination` feature keys. PrimeHR enforces action and data scope separately.

## Database and contracts

- Paired PostgreSQL and SQL Server `V44__rr_cycles_nominations_evidence.sql` migrations add `rr_award_cycle`, `rr_nomination`, `rr_nomination_member`, `rr_nomination_source`, and `rr_nomination_evidence` with matching constraints/indexes.
- Populated V43-to-V44 upgrade preservation, paired structural parity, fresh V1-to-V44 migration, and Hibernate schema validation passed.
- The unreleased V43 nominee-type check was corrected from `ORG_UNIT` to the Java/API value `ORGANIZATION_UNIT` in both provider scripts before deployment.
- `contracts/openapi/primehr-v1.yaml` now exposes cycle, nomination, submission/withdrawal, and secured evidence contracts with the exact enum values.

## Verification

```text
mvn -pl PrimeHR -am "-Dtest=RrCycleNominationDomainTest,RrNominationControllerTest,RrNominationPermissionGuardTest,PrimeHrV43ToV44UpgradeTest,RrV44MigrationParityTest,PrimeHrOpenApiContractTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0

mvn -pl Administrative -am "-Dtest=EffectiveAuthorizationServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0

mvn -pl PrimeHR -am "-Dtest=RrNominationServiceIntegrationTest" "-Dsurefire.failIfNoSpecifiedTests=false" test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

mvn -pl PrimeHR -am clean verify
Tests run: 423, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS; production JAR built

mvn -pl Administrative -am clean verify
Common tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Administrative tests run: 77, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS; production JARs built
```

No live PostgreSQL or SQL Server endpoint was configured for this run. Provider portability was checked through paired migration parity, populated upgrade, fresh PostgreSQL-compatible migration, and Hibernate validation. No frontend was authorized in 8A.2, so Playwright remains scheduled for Phase 8D.

## Stop boundary and next gate

Phase 8A.2 introduced no automatic eligibility, scoring, evaluator assignment, conflict/recusal, quorum, deliberation, recommendation, final award decision, award/conferment record, incentive fulfillment, report/UI, CoreHR mutation, payment, promotion, or succession behavior.

The next gated unit is Phase 8B: eligibility review, configurable criterion evaluation, evaluator assignment, conflict/recusal, independent scoring/comments, quorum, deliberation, return/disqualification/recommendation, head-of-agency decision, and issue/reconsideration tracking. Phase 8B must stop before award/conferment and fulfillment.
