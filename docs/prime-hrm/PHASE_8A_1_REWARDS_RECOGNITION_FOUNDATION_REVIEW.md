# Phase 8A.1 - Rewards and Recognition Foundation Review

Reviewed: 2026-11-02  
Status: Complete; stopped before Phase 8A.2

## Outcome

Phase 8A.1 establishes a provider-portable, agency-scoped foundation for PRAISE governance,
configurable award categories and criteria, and the PRAISE Committee. It does not nominate,
evaluate, recommend, confer, or fulfill an award.

## Delivered behavior

- Versioned agency PRAISE policies capture legal basis, policy reference, supported nominee and
  incentive types, committee/head-of-agency controls, annual-report requirement, and effectivity.
- Award categories support individual, group, and organization-unit nominees, monetary,
  non-monetary, or mixed incentives, eligibility/evidence guidance, minimum score, and ordered
  criteria whose weights must total exactly 100 percent.
- Committee charters support count- or percentage-based quorum and effective-dated employee roster
  snapshots. Each valid roster has exactly one chair, a secretariat, and voting membership; a
  secretariat cannot vote.
- Draft definitions may be edited. Published definitions are immutable and can only be retired or
  superseded by a new draft revision. Stale writes are rejected through optimistic versions.
- All writes are agency-scoped, transactional, actor/correlation-aware, and auditable.

## Authorization and API contract

- `primehr.rr-governance` independently controls PRAISE policy, award-category, and criteria access.
- `primehr.rr-committee` independently controls committee-charter and roster access.
- PrimeHR requires the exact action and Agency Wide data scope; Administrative recognizes both
  keys in effective authorization. No permission is automatically granted or seeded.
- Authenticated REST endpoints live below `/api/primehr/v1/rewards-recognition` and are documented
  in `contracts/openapi/primehr-v1.yaml`.

## Persistence and portability

- Paired V43 migrations create policy/version, category/version/criterion, and
  charter/version/member tables for PostgreSQL and SQL Server.
- Application queries use Spring Data/provider-neutral persistence. The migration parity and
  populated V42-to-V43 upgrade tests passed, and fresh PostgreSQL-compatible Flyway/Hibernate
  validation includes the new tables and indexes.
- No live PostgreSQL or SQL Server endpoint was configured for this run, so live-provider execution
  remains a deployment validation item.

## Verification

- Focused R&R integration, controller, permission, OpenAPI, migration parity, and populated-upgrade
  tests passed.
- `mvn -pl Administrative,PrimeHR -am clean verify` passed:
  - Common: 3 tests;
  - Administrative: 76 tests;
  - PrimeHR: 414 tests;
  - zero failures, errors, or skips; production JARs built.
- A first incremental verification exposed an obsolete compiled test left after an earlier package
  move. Clean verification removed the generated stale class and confirmed the current source suite
  is green; no production change was needed for it.

## Explicit boundary

Phase 8A.1 contains no cycle/window, nomination, nominee snapshot, evidence intake, eligibility
decision, evaluator assignment, conflict/recusal, score, deliberation, recommendation, final award,
incentive fulfillment, PDF/report, frontend, Playwright, CoreHR write, payroll/timekeeping action,
promotion, appointment, or succession behavior.

Playwright remains scheduled for Phase 8D, when user interfaces exist.

## Next gated unit

Phase 8A.2 may begin only after user authorization. Its bounded scope is award cycles/windows;
individual, group/team, and organization-unit nominations; authenticated nominators; authoritative
nominee snapshots; secured evidence; and read-only links to exact competency, performance, and L&D
facts. It must stop before automatic eligibility, evaluation/scoring, committee recommendation, or
award creation.
