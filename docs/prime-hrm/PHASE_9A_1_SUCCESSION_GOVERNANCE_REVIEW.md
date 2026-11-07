# Phase 9A.1 — Succession Governance Foundation Review

## Delivered boundary

Phase 9A.1 establishes governance only. It adds versioned, effective-dated succession policies;
agency-configurable readiness scales and levels; committee charters; HRM-resolved committee roster
snapshots; and standing conflict declarations/resolution. It does not rank employees or create a
candidate pool.

## Controls

- Published definitions are immutable. Revisions preserve predecessor lineage and non-overlapping
  effectivity.
- Readiness labels and development guidance are agency data. No `READY_NOW` value or numeric
  employee-ranking threshold is hardcoded.
- Readiness publication requires at least two unique levels and must distinguish pool-eligible from
  not-yet-eligible outcomes.
- Committee roster requests accept employee numbers and governance attributes only. Employee name,
  identity, appointment, job-position, plantilla and source fingerprint are resolved from the
  authoritative Human Resource integration endpoint.
- A roster has exactly one chair, at least one voter, any required secretariat, and a feasible quorum.
- Conflict declarations require a current roster member, reject self-conflicts and active duplicates,
  and preserve an audited resolution.
- Optimistic record versions protect every mutable operation. Mutations create PrimeHR audit events.
- `primehr.succession-governance` and `primehr.succession-committee` are independent Agency Wide
  permissions; API routes are authenticated.

## Persistence and portability

V47 adds nine tables for policy, scale/levels, committee/roster and conflicts. PostgreSQL and SQL
Server migrations use equivalent provider-appropriate boolean, timestamp and decimal types. The
migration is additive and contains no CoreHR, Payroll, Timekeeping, RSP or employee-record writes.

## Verification

- Java compilation passed.
- Phase 9A.1 domain, permission, OpenAPI, migration parity and V46-to-V47 upgrade tests passed.
- PostgreSQL-compatible H2 migration through V47 passed and preserved populated V46 data.
- Clean affected reactor passed Common 3/3, Administrative 80/80 and PrimeHR 443/443 with zero
  failures, errors or skips.
- Live PostgreSQL and SQL Server endpoints were not configured for this workspace and were not run.

## Next gated unit

Phase 9A.2 may add versioned career pathways and critical-position governance. It must stop before
employee pools, nominations, readiness assessments, development plans, continuity actions or RSP
handoffs.
