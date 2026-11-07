# Phase 10B - Evidence Assessment Workflow Review

Reviewed: 2026-11-05  
Status: Complete

## Delivered scope

Phase 10B delivers the controlled internal evidence-assessment workflow defined by the approved
Phase 10 scope. It provides:

- versioned assessment cycles bound to an exact published framework definition, content revision,
  and fingerprint;
- organization-scoped assessment cases with authoritative owner, validator, appointment, and
  organization snapshots;
- separate owner and validator responsibilities, including validator recusal and reasoned
  reassignment;
- exact-version live evidence links to competency profiles, final performance outcomes, and L&D
  completion records, with the source module's own read permission enforced during verification;
- secured uploaded evidence with classification, visibility, retention tag, evidence date, review
  due date, content signature validation, checksum, supersession, and controlled download;
- human requirement findings of `MET`, `PARTIALLY_MET`, `NOT_MET`, `NOT_APPLICABLE`, or
  `NEEDS_REVIEW`, with authority required for an allowed not-applicable determination;
- current/superseded finding lineage across submission revisions;
- return, resubmit, withdraw, and validator-only finalization controls;
- finding-linked evidence gaps and responsible-person corrective actions with independent
  plan/start/submit/verify/return/cancel and resolve/reopen lifecycles;
- append-only case, evidence, finding, gap, action, recusal, reassignment, and transition history;
- optimistic-lock checks and exact-source re-verification before evidence may support a finalized
  finding;
- independent Administrative permissions for assessment, validation, and corrective-action work;
- versioned REST/OpenAPI contracts; and
- equivalent PostgreSQL and SQL Server V54 migrations.

Finalization remains a human internal assessment decision. Every framework requirement must have a
current finding; `NEEDS_REVIEW` cannot be finalized; `MET` and `PARTIALLY_MET` require current,
verified, non-expired evidence; and `PARTIALLY_MET` and `NOT_MET` require a recorded gap.

## Primary implementation

- `PrimeHR/src/main/java/com/primehr/evidence/**`
- `PrimeHR/src/main/java/com/primehr/security/EvidenceWorkflowPermissionGuard.java`
- `PrimeHR/src/main/resources/db/migration/postgresql/V54__evidence_assessment_workflow.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V54__evidence_assessment_workflow.sql`
- `Administrative/src/main/java/com/administrative/impl/EffectiveAuthorizationServiceImpl.java`
- `contracts/openapi/primehr-v1.yaml`

## Authorization and privacy

The three features are `primehr.evidence-assessment`, `primehr.evidence-validation`, and
`primehr.evidence-action`. Permissions fail closed and independently enforce access, add, edit,
delete/withdraw, submit, approve, and finalize as applicable. Only Assigned Records or Agency Wide
data scope is accepted; cycle administration requires Agency Wide scope. Case reads and mutations
also enforce process role and assignment, so link visibility is not treated as authorization.

Uploaded evidence remains private document content. Download requires a permitted case relationship,
returns `no-store`, and applies the evidence visibility rule. Live links retain exact source identity
and do not copy or mutate the authoritative competency, performance, or learning record.

## Database and provider review

V54 adds the assessment cycle, assessment case, evidence artifact, finding, gap, corrective action,
and assessment event tables. PostgreSQL and SQL Server scripts passed structural parity and populated
V53-to-V54 upgrade-preservation tests. Fresh PostgreSQL-compatible migration through V54 and
Hibernate validation are covered by the complete PrimeHR regression.

Live PostgreSQL and SQL Server endpoints were not configured for this unit. Provider assurance is
therefore based on paired migration parity, populated upgrade preservation, and fresh-schema
provider-compatible validation; live-provider execution remains a deployment validation item.

## Verification

- PrimeHR affected-module compilation passed;
- Phase 10B domain, permission, migration parity, populated upgrade, OpenAPI, and full service
  lifecycle tests passed 57/57;
- Administrative effective-authorization regression passed 39/39; and
- the clean complete PrimeHR regression produced 507/507 passing tests with zero failures, errors,
  or skips.

The integration workflow covers published-framework binding, cycle opening, owner submission,
validator finding, gap creation, corrective-action assignment/submission/independent verification,
case finalization, and event history.

## Explicit exclusions

Phase 10B does not calculate a readiness score or Governance Level, create an official CSC
determination, produce immutable readiness snapshots, generate evidence/readiness PDFs, expose
aggregate analytics or user interfaces, submit data externally, or mutate CoreHR, Timekeeping, or
Payroll records.

## Next gated unit

Phase 10C is the deterministic internal-readiness, reporting, and privacy-analytics unit. It may add
definition-bound completeness/coverage calculations, human internal sign-off, immutable readiness
snapshots, missing/expiring evidence and overdue-action views, bean-driven internal PDFs, and
privacy-controlled aggregate analytics. It must retain the non-official boundary and may not rank
employees or determine an official CSC score or Governance Level.
