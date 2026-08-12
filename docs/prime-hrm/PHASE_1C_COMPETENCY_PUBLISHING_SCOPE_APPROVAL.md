# ISOFT PRIME-HRM Phase 1C - Controlled Competency Publishing Approval Scope

Status: Approved and implemented; final manual acceptance pending

Prepared: 2026-08-03

Depends on: committed Phase 1B backend checkpoint `12eb6ee` and the locally tested Phase 1B frontend changes

Implementation review: [PHASE_1C_REVIEW_MANIFEST.md](./PHASE_1C_REVIEW_MANIFEST.md)

## 1. Objective

Add controlled direct publishing for competency categories, proficiency scales with levels, and competencies with behavioral indicators. Publishing is a privileged, transactional, fully audited operation. It converts a complete `DRAFT` aggregate into an immutable published `ACTIVE` version without introducing a multi-step approval workflow.

Phase 1C completes the publishing prerequisite for Master Plan Phase 2. It does not start position competency profiles or any other Phase 2 domain.

## 2. Approved governance decision to encode

- Publishing is performed directly by a user with the dedicated `canPublish` permission.
- `canAdd`, `canEdit`, `canDelete`, and administrator UI visibility do not silently grant publishing to ordinary users.
- Established administrator behavior remains compatible: role `1`, an Administrative `isAdministrator` ruleset, and install user `admin` receive effective `canPublish=true` and remain audited.
- Publishing requires a reason and the current optimistic `recordVersion`.
- The backend remains authoritative. Frontend permission state only controls presentation.
- Multi-step submit/review/recommend/approve workflow is deferred. Adding it later must not rewrite published history.

## 3. Lifecycle and version invariants

No new lifecycle status is introduced in Phase 1C:

- `DRAFT`: mutable and unpublished.
- `ACTIVE`: published and content-immutable. An ACTIVE version may be currently effective, historically effective, or future-effective according to its date range.
- `ARCHIVED`: an unpublished draft withdrawn from use.

`active=true` continues to identify a published definition. `isEffectiveOn(asOf)` continues to combine publication with `effectiveFrom`/`effectiveTo`. This preserves the Phase 1A read contract and allows historical published versions to be resolved by date.

### Publish rules

1. Only a `DRAFT` aggregate root can be published.
2. `effectiveFrom` is mandatory before publication. `effectiveTo`, when supplied, must not precede it.
3. A new code at business version 1 may publish only when no other published version of that agency/code overlaps its effective range.
4. A successor may publish only when `supersedesId` identifies the latest published version for the same agency/code, its `definitionVersion` is exactly predecessor version + 1, no other draft exists for the same agency/code, and its `effectiveFrom` is later than the predecessor's `effectiveFrom` when the predecessor has one.
5. Publishing a successor closes only the predecessor's effective period when necessary: if the predecessor is open-ended or overlaps the successor, its `effectiveTo` becomes one day before the successor's `effectiveFrom`. A pre-existing earlier end date is preserved, so an intentional gap is allowed.
6. The predecessor remains `ACTIVE` and otherwise immutable. Its exact ID and content remain available for historical references.
7. Published effective ranges for the same agency/code must never overlap. The service checks the complete version chain inside the publication transaction.
8. Publishing never changes IDs, code, business version, lineage, agency, child ownership, or content supplied by a draft-edit operation.
9. An ACTIVE definition cannot be edited, manually archived, unpublished, or republished. Corrections use a successor draft.
10. A failed or stale publication changes neither the draft nor predecessor and creates no audit event.

The controlled predecessor `effectiveTo` adjustment is the only permitted mutation of a previously published root. It is part of the successor publication transaction and receives its own before/after audit event.

## 4. Aggregate completeness rules

### Competency category

- Required code, name, effectivity, and existing ordering validations must pass.
- A category can publish without a competency assigned to it.

### Proficiency scale

- At least one non-archived level is required; the number of levels remains dynamic and is never fixed at four.
- Enabled levels must have unique codes and positive, unique `levelOrder` values within the scale.
- Every enabled level's effective range must cover the scale's publication start and must not end before the scale starts.
- Disabled/archived draft levels are retained for audit/history but are not part of the usable published scale.
- Levels publish atomically with their owning scale; there is no child-level publish endpoint.

### Competency

- Its referenced category and proficiency scale must already be published and must be effective on the competency's `effectiveFrom` date.
- At least one enabled indicator is required for every enabled level in the selected scale. Additional indicators per level remain allowed.
- Every enabled indicator must reference an enabled level belonging to the competency's exact scale version.
- Every enabled indicator's effective range must cover the competency's publication start and must not end before the competency starts.
- Indicators publish atomically with their owning competency; there is no child-indicator publish endpoint.

Published competencies retain exact category and scale version IDs. Later publication of a category or scale successor does not rewrite an existing competency reference.

## 5. Concurrency and transaction boundary

- The publish service loads the agency/code version chain with a provider-neutral JPA pessimistic write lock.
- It rechecks draft status, optimistic `recordVersion`, latest predecessor, business version, effectivity, dependencies, and completeness after acquiring the lock.
- Draft activation, optional predecessor effectivity closure, publication metadata, and audit inserts occur in one transaction.
- Concurrent publication or stale record conflicts return HTTP 409 with a stable machine-readable error code such as `PUBLICATION_CONFLICT`.
- Validation failures return the existing structured 400 response; missing records remain 404; permission/dependency failures remain 401/403/503 as appropriate.
- No native provider-specific query is introduced for application behavior. If a provider-specific lock/index is required, it remains isolated in the matching migration/profile with equivalent behavior on both databases.

## 6. Persistence and migration scope

Add provider-specific Flyway migrations:

- `PrimeHR/src/main/resources/db/migration/postgresql/V3__competency_controlled_publishing.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V3__competency_controlled_publishing.sql`

Each aggregate root gains nullable `published_at` and `published_by` metadata. Existing Phase 1A rows migrated as ACTIVE remain valid legacy published versions with null publication metadata; history must not be fabricated. New publications populate both fields from the authenticated server context and current server instant.

Add equivalent indexes supporting agency/code/status/effectivity/version-chain lookup. Existing IDs, V1/V2 checks, unique constraints, relationships, and audit data are preserved. Hibernate remains `ddl-auto=validate`. Applied V1/V2 files must never be edited; deployment uses forward V3 migrations only.

Date-range non-overlap is enforced transactionally because PostgreSQL exclusion constraints and SQL Server-specific range logic cannot be placed in shared application behavior. Database uniqueness continues to protect agency/code/business-version identity.

## 7. Authorization contract

Administrative permission JSON for `primehr.competency` gains additive boolean `canPublish`.

- Missing `canPublish` in an older ruleset resolves to `false`.
- Effective `canPublish` is true for an ordinary ruleset only when both stored `canAccess` and stored `canPublish` are true.
- Administrator responses return all five flags as true.
- PrimeHR's typed `EffectiveFeaturePermission`, `PrimeHrAction`, client, and guard gain `PUBLISH`/`canPublish`.
- Publish endpoints check `PUBLISH`; they do not substitute `ADD`, `EDIT`, or `DELETE`.
- Administrative continues deriving identity and role from validated authentication. PrimeHR continues forwarding the bearer token and failing closed when Administrative cannot authorize it.
- Agency remains exclusively server-resolved through the existing single-agency configuration. Requests cannot supply actor, role, permission, agency, status, version number, publication timestamp, or publisher.

Administrative Permission UI adds a Publish checkbox only for features declaring `hasPublish`, initially `primehr.competency`. Enabling Publish also requires/enables Access; clearing Access clears Publish. Existing permission rows remain backward compatible.

## 8. REST and OpenAPI contract

Add one endpoint per aggregate root under the existing admin base path:

- `POST /api/primehr/v1/admin/competency-categories/{categoryId}/publish`
- `POST /api/primehr/v1/admin/proficiency-scales/{scaleId}/publish`
- `POST /api/primehr/v1/admin/competencies/{competencyId}/publish`

The request DTO contains only required non-negative optimistic `recordVersion` and a required trimmed publication `reason`. The response is the corresponding existing admin response DTO extended additively with nullable `publishedAt` and `publishedBy`.

Existing list, detail, create, edit, version, archive, audit, and Phase 1A read endpoints remain backward compatible. OpenAPI documents the new permission, request, response metadata, 400/401/403/404/409/503 outcomes, and lifecycle guarantees. No `DELETE`, `PATCH`, bulk-publish, unpublish, approve, or workflow endpoint is introduced.

## 9. Audit requirements

- Successful root activation writes `PUBLISH_DRAFT` with actor, agency, reason, business/record versions, before/after state, source module, correlation ID, and timestamp.
- A predecessor date closure writes a second `CLOSE_PUBLISHED_EFFECTIVITY` event for that predecessor in the same transaction and with the same reason/correlation ID.
- Administrator publishing is audited identically.
- Validation, denial, dependency failure, optimistic conflict, or transaction rollback writes no success audit event.
- Published metadata and audit actor always come from authenticated server context, never request data.
- Existing append-only audit storage and endpoint are reused; no mutable audit API is added.

## 10. UI scope

### `administrative-software`

- Extend the permission model/table with optional `hasPublish` and persisted `canPublish`.
- Show the Publish permission only for `primehr.competency` in this phase.
- Preserve existing CRUD flags and old ruleset parsing.

### `prime-hr-software`

- Extend typed competency permission parsing with `canPublish`, defaulting missing values to false and preserving administrator behavior.
- Show Publish only for a DRAFT root when the user has `canPublish`.
- Present a confirmation summary containing type, code/name, definition version, effectivity, predecessor, and completeness result.
- Require a non-empty reason and explicitly warn that published content becomes immutable.
- Display actionable validation, 403, 409, and authorization-dependency errors.
- Refresh list/detail/audit state after success and display publisher/time when available.
- Label ACTIVE records as Published while retaining the exact backend status value.
- Preserve loading, empty, unauthorized, and return-to-Employee-Portal behavior.

No `employee-portal-UI` code change is expected because SSO, module visibility, and opaque permission JSON transport already exist. Its currently local Phase 1B changes remain a separate deployment decision.

## 11. Expected repository impact

Exact files must be reconfirmed immediately before implementation.

### `hris`

- `PrimeHR/src/main/java/com/primehr/competency/{api,application,domain,infrastructure}/**`
- `PrimeHR/src/main/java/com/primehr/security/**`
- `PrimeHR/src/main/java/com/primehr/integration/administrative/**`
- `PrimeHR/src/main/java/com/primehr/shared/{audit,exception}/**` only if additive mapping is needed
- PostgreSQL and SQL Server V3 migrations and focused PrimeHR tests
- `Administrative` effective authorization DTO/service and focused tests
- `contracts/openapi/primehr-v1.yaml`
- Phase 1C implementation/review and progress documents

### `administrative-software`

- `src/app/administrative/permission/Permission.tsx`

### `prime-hr-software`

- typed auth permission model and competency administration component/styles as required

### Explicitly untouched

- `employee-portal-UI`, HRM UI, Timekeeping UI, and Payroll UI;
- HRIS `HumanResource`, `TimeKeeping`, `Payroll`, `EmployeePortal`, and Jasper reports;
- HRISApp assembly/runtime;
- existing `.env` files and deployed frontend branches.

## 12. Implementation checkpoints and gates

### Phase 1C.1 - Backend, permission contract, and provider gates

Implement backend lifecycle, V3 migrations, `canPublish`, authorization, audit, OpenAPI, and focused tests first. Phase 1C.2 must not begin until all of these pass:

- no JWT returns 401; no `canPublish` returns 403;
- `canEdit`/`canAdd` without `canPublish` cannot publish;
- role `1`, ruleset administrator, and install administrator can publish and are audited;
- old rulesets missing `canPublish` fail closed for publication;
- only a complete DRAFT publishes;
- stale version and concurrent publish return 409 without partial state or audit;
- successor publication closes an overlapping/open predecessor period atomically and preserves predecessor content/status/history;
- non-overlapping historical/current/future `asOf` reads return the correct published version;
- incomplete scale/competency, inactive or wrong-version dependency, duplicate or overlapping version, and illegal transition are rejected;
- every successful publish creates exactly the expected one or two audit events;
- existing Phase 1A/1B APIs and tests remain green;
- fresh V1+V2+V3 and populated V2-to-V3 migrations plus Hibernate validation pass on real PostgreSQL and real SQL Server;
- affected Maven clean tests and full reactor package pass with skipped/zero-test modules reported.

### Phase 1C.2 - Permission and publishing UI

Only after Phase 1C.1 passes:

- implement the Administrative Publish permission control and PrimeHR publishing experience;
- run Administrative and PrimeHR UI lint, type-check, tests where configured, and production builds;
- verify old permission JSON, administrator behavior, denied action visibility, success, validation, 409, audit refresh, and immutable published UI manually;
- run repository status/stat/whitespace, secret, generated-file, and phase-boundary audits.

Frontend changes remain local and must not be pushed to Vercel-connected branches without the user's separate deployment approval.

## 13. Explicit exclusions

Phase 1C does not implement:

- submit/review/recommend/approve workflow, queues, assignments, separation of duties, or notifications;
- unpublish, rollback, manual edit/archive of published content, or hard deletion;
- bulk publishing or coordinated multi-aggregate release bundles;
- position/plantilla competency profiles, job-family inheritance, or Qualification Standards;
- person profiles, assessments, evidence, gaps, RSP, SPMS, L&D, R&R, applicants, reports, documents, analytics, messaging, or outbox;
- dynamic multi-agency identity, new JWT claims, gateway, shared frontend package, or HRISApp combined datasource/runtime;
- frontend deployment, Git commit, Git push, or modification of user `.env` files;
- cleanup of retained validation schemas.

## 14. Known risks and review decisions

Approval of this scope accepts these decisions:

1. `ACTIVE` continues to mean published; effective dates determine whether a published version is current.
2. Publishing a successor may shorten only its predecessor's `effectiveTo`; this exception is transactional and separately audited.
3. Published versions and exact dependency IDs remain immutable and historically queryable.
4. Future-effective publication is supported through non-overlapping date ranges; gaps are allowed, overlaps are not.
5. Competency publication requires at least one enabled behavioral indicator for every enabled level in its exact published scale version.
6. Direct publishing is intentionally independent from ordinary CRUD permissions through `canPublish`.
7. Multi-step governance remains a compatible later enhancement, not an implicit part of Phase 1C.
8. The current server-configured single-agency boundary remains unchanged.

Implementation risk is higher than Phase 1B because publishing affects immutable history, concurrent version chains, authorization, and two database providers. A GPT-5.6 Sol High reasoning pass is recommended when implementation begins and again for the final lifecycle/provider review; Medium is sufficient for this scope-definition turn.

## 15. Approval record

The user explicitly approved this scope and authorized Phase 1C.1, followed by Phase 1C.2 only after the Phase 1C.1 gates passed. That condition was satisfied. The remaining closure gate is the focused manual acceptance matrix and selective commit-readiness review recorded in `PHASE_1C_REVIEW_MANIFEST.md`.
