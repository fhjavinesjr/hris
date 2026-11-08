# Phase 7A.1 - Learning and Development Foundation Review

Reviewed: 2026-10-25  
Status: Complete; stopped at the Phase 7A.2 approval boundary

## Delivered scope

Phase 7A.1 establishes the provider-neutral, agency-scoped foundation for Competency-Based
Learning and Development:

- effective-dated and immutable-version L&D governance policies;
- effective-dated and immutable-version intervention definitions;
- provider and resource-person registries with controlled active/inactive state;
- effective-dated evaluation frameworks and ordered evaluation levels;
- draft, publish, revise, and retire lifecycle rules with optimistic locking;
- audit events and correlation identifiers for state-changing operations;
- Administrative authorization for `primehr.ld-governance`, `primehr.ld-program`, and
  `primehr.ld-evaluation` with independent access/add/edit/publish enforcement;
- authenticated REST endpoints and the corresponding PrimeHR OpenAPI contract; and
- equivalent PostgreSQL and SQL Server V37 migrations.

The implementation is under `com.primehr.learningdevelopment` and follows the existing
controller-service-repository boundary. Published definitions cannot be edited in place. Revisions
create successors, published effective periods cannot overlap, agency scope is mandatory, and stale
record versions are rejected.

## Schema and portability

V37 creates these nine tables on both providers:

1. `ld_governance_policy`
2. `ld_governance_policy_version`
3. `ld_intervention`
4. `ld_intervention_version`
5. `ld_provider`
6. `ld_resource_person`
7. `ld_evaluation_framework`
8. `ld_evaluation_framework_version`
9. `ld_evaluation_level`

The paired migrations use provider-appropriate data types while preserving equivalent keys,
constraints, indexes, lifecycle values, and version semantics. Migration parity, fresh-schema
validation, and the V36-to-V37 forward upgrade path are covered by automated tests. No native
provider-specific application query was introduced.

## Verification

Executed from the backend Maven root:

```text
mvn -pl PrimeHR -am clean verify
Tests run: 361, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
PrimeHR-1.0-SNAPSHOT.jar built

mvn -pl Administrative -am clean verify
Common: 3 tests passed
Administrative: 72 tests passed
Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Administrative-1.0-SNAPSHOT.jar and Administrative-1.0-SNAPSHOT-exec.jar built

git diff --check
Passed
```

The PrimeHR suite covers the new domain/service behavior, permission guard, OpenAPI boundary,
PostgreSQL/SQL Server migration parity, V36-to-V37 upgrade, fresh migration/Hibernate validation,
application context, and the existing PrimeHR regression set. No configured live PostgreSQL or SQL
Server endpoint was used in this unit; actual-engine acceptance remains a disclosed deployment gate.

## Boundary held

This unit intentionally does not add:

- needs intake, referral acceptance, or an Individual Development Plan;
- annual/consolidated L&D plans, programs, offerings, budgets, or schedules;
- nomination, approval routing, enrollment, attendance, completion, evaluation results, or outcome
  handoff;
- Administrative permission screens, PrimeHR pages, Employee Portal pages, reports, or Playwright;
- HumanResource PDS mutation, TimeKeeping/Payroll mutation, competency-level change, performance
  decision, or Rewards and Recognition behavior.

Frontend controls, interfaces, reports, and Playwright remain assigned to Phase 7E. The next eligible
implementation unit is Phase 7A.2 only after its scope is confirmed.
