# ISOFT PRIME-HRM Phase 1B - Competency Draft Administration Approval Scope

Status: Approved by user and implemented; see `PHASE_1B_COMPETENCY_DRAFT_ADMINISTRATION.md`
Prepared: 2026-08-03
Depends on: committed Phase 1A and Phase 1A.1 checkpoint `52d2e13`

## 1. Objective

Deliver a usable, standalone, permission-controlled administration experience for draft competency definitions while preserving the Phase 1A architecture and every existing HRIS runtime contract.

Phase 1B is limited to the same five competency concepts:

1. Competency categories
2. Competencies
3. Proficiency scales
4. Proficiency levels
5. Behavioral indicators

It adds audited draft creation/editing/archive and successor-draft creation. It does not publish or activate definitions and does not implement any later PRIME-HRM domain.

## 2. Delivery checkpoints under one approval

### Phase 1B.1 - Backend, lifecycle, audit, and Administrative authorization

- Extend the PrimeHR schema through provider-specific Flyway V2 migrations.
- Add explicit draft lifecycle and business-version lineage.
- Add write DTOs, command services, repositories, controllers, validation, audit, and conflict handling.
- Add an Administrative authoritative permission-resolution endpoint.
- Add the `primehr` SSO target, runtime URL settings, portal module access, and Competency permission entry.
- Replace configured PrimeHR write-role decisions with a fail-closed Administrative authorization adapter.
- Retain the required server-side single-agency resolver until an authoritative employee-to-agency relationship exists.
- Update OpenAPI and run real PostgreSQL and SQL Server migration/integration tests.

Phase 1B.2 starts only after Phase 1B.1 builds and both provider gates pass.

### Phase 1B.2 - Standalone PrimeHR management UI and SSO

- Create the standalone `prime-hr-software` Next.js application if it is still absent.
- Add SSO exchange/bootstrap and Employee Portal launch integration for target `primehr`.
- Add typed list/detail/create/edit/version/archive experiences for the same five concepts.
- Enforce UI visibility/action states using canonical permissions while retaining backend enforcement.
- Run strict type checking, lint/tests where configured, and a production build.

If Phase 1B.1 reveals an unsafe authorization, migration, or lifecycle issue, implementation stops before Phase 1B.2 and the blocker is reported.

## 3. Exact lifecycle and versioning rules

Aggregate roots are Category, Proficiency Scale, and Competency. Levels belong to a Scale version; Indicators belong to a Competency version.

### States

- `DRAFT`: mutable by an authorized editor.
- `ACTIVE`: immutable. Existing active definitions remain readable and may be cloned into a successor draft.
- `ARCHIVED`: immutable and excluded from ordinary active reads.

### Allowed Phase 1B operations

- Create a new DRAFT at business version 1.
- Edit a DRAFT when the supplied optimistic `recordVersion` matches.
- Archive a DRAFT with a required reason.
- Clone an ACTIVE aggregate into a new DRAFT with incremented business version and `supersedesId` lineage.
- Add/edit/archive Levels only while their owning Scale is DRAFT.
- Add/edit/archive Indicators only while their owning Competency is DRAFT.

### Explicitly disallowed

- Editing or archiving ACTIVE definitions directly.
- Editing ARCHIVED definitions.
- DRAFT to ACTIVE publishing or approval.
- Reusing `record_version` as the business definition version.
- Hard deletion through repository or HTTP DELETE.
- Changing agency ownership through a request.
- Client-supplied actor, agency, audit timestamp, lifecycle status, or business version.

Publishing/activation requires a separate future approval because it needs a `canPublish` action, governance rules, and—if required—an approval workflow. Phase 1B will not silently map publishing to ordinary `canEdit`.

## 4. Persistence and migration scope

Provider-specific `V2__competency_draft_administration.sql` migrations will be added for PostgreSQL and SQL Server.

Planned model:

- Category, Scale, and Competency gain `lifecycle_status`, `definition_version`, and nullable `supersedes_id`.
- Existing Phase 1A rows migrate deterministically to `ACTIVE`, business version 1, with no predecessor.
- Existing unique constraints that prevent multiple business versions are replaced by version-aware equivalents.
- Only one non-archived draft for a lineage/code is permitted.
- Scale successor creation clones its Levels into new IDs owned by the successor Scale.
- Competency successor creation clones its Indicators into new IDs owned by the successor Competency, mapped to Levels in the selected successor/current Scale.
- Provider-equivalent constraints and indexes enforce status values, positive business versions, valid self-reference lineage, agency boundaries where SQL can enforce them, lookup performance, and uniqueness.
- Hibernate remains `ddl-auto=validate`; it does not create or update production schema.

An append-only `prime_audit_event` table records:

- agency, actor, action, aggregate type and ID;
- business version and optimistic record version;
- timestamp;
- previous-state and new-state JSON text;
- required reason where applicable;
- source module;
- request/correlation ID when available.

Audit insertion occurs in the same transaction as every successful command. Application code exposes no update/delete repository operation for audit events.

## 5. Authorization and trusted scope

### Administrative permission keys

- Portal module key: `primeHr`
- SSO target: `primehr`
- Permission feature key: `primehr.competency`
- Existing flags used in Phase 1B: `canAccess`, `canAdd`, `canEdit`, `canDelete`

Action mapping:

| Operation | Required flag |
|---|---|
| Read active and permitted draft administration data | `canAccess` |
| Create a new draft or successor draft | `canAdd` |
| Edit a draft or add/edit its child records | `canEdit` |
| Archive a draft or draft child | `canDelete` |

Role `1`, an Administrative `isAdministrator` ruleset, and the established install administrator retain their existing unrestricted behavior, but their commands are still audited.

### Authoritative backend check

Administrative will expose a narrow authenticated effective-permission endpoint. It derives employee identity and role from the validated JWT/security context, resolves the persisted Permission Ruleset, and returns only the requested PrimeHR feature flags and administrator state. It will not trust employee number, role, permission JSON, or agency supplied in the request body/query.

PrimeHR will forward the caller's bearer token to that endpoint through a typed authorization client. It fails closed on 401, 403, invalid responses, timeout, or Administrative unavailability. It will not read the Administrative database and will not trust frontend local storage.

The Administrative base URL is bootstrap environment/profile configuration because PrimeHR cannot fetch centralized configuration without first locating Administrative. Public UI/API destinations remain authoritative System Config values.

Agency scope continues to come only from required `PRIMEHR_AGENCY_ID`. No JWT agency claim or employee-agency relationship will be invented. Multi-agency identity resolution remains deferred.

## 6. Proposed REST contract

Existing Phase 1A read endpoints remain backward compatible. Draft administration uses `/api/primehr/v1/admin` and never accepts `agencyId`.

### Categories

- `POST /admin/competency-categories`
- `PUT /admin/competency-categories/{categoryId}`
- `POST /admin/competency-categories/{categoryId}/versions`
- `POST /admin/competency-categories/{categoryId}/archive`

### Proficiency scales and levels

- `POST /admin/proficiency-scales`
- `PUT /admin/proficiency-scales/{scaleId}`
- `POST /admin/proficiency-scales/{scaleId}/versions`
- `POST /admin/proficiency-scales/{scaleId}/archive`
- `POST /admin/proficiency-scales/{scaleId}/levels`
- `PUT /admin/proficiency-scales/{scaleId}/levels/{levelId}`
- `POST /admin/proficiency-scales/{scaleId}/levels/{levelId}/archive`

### Competencies and behavioral indicators

- `POST /admin/competencies`
- `PUT /admin/competencies/{competencyId}`
- `POST /admin/competencies/{competencyId}/versions`
- `POST /admin/competencies/{competencyId}/archive`
- `POST /admin/competencies/{competencyId}/indicators`
- `PUT /admin/competencies/{competencyId}/indicators/{indicatorId}`
- `POST /admin/competencies/{competencyId}/indicators/{indicatorId}/archive`

### Administrative reads and audit

- `GET /admin/competency-categories` with status/search/effectivity pagination filters
- `GET /admin/proficiency-scales` with status/search/effectivity pagination filters
- `GET /admin/competencies` with status/category/search/effectivity pagination filters
- `GET /admin/audit-events` with aggregate type/ID and pagination filters

All write requests require Bean Validation, `recordVersion` for updates/archive, and `reason` for archive/version creation. Responses use DTOs and the established PrimeHR error envelope. Optimistic-lock conflicts return HTTP 409 with a stable machine-readable error code. No HTTP DELETE endpoint is introduced.

## 7. UI scope

The standalone UI is limited to:

- authenticated SSO callback/bootstrap;
- Competency dashboard/list;
- category list and draft form;
- proficiency scale list and draft form with dynamic Levels;
- competency list/detail and draft form with ordered Indicators by Level;
- successor-version action for ACTIVE records;
- archive confirmation requiring a reason;
- read-only presentation for ACTIVE/ARCHIVED records;
- lifecycle badges, audit-history view, pagination/search/filtering;
- loading, empty, 401, 403, validation, conflict, dependency-unavailable, and server-error states;
- Employee Portal PrimeHR launch item controlled by `portalModuleAccess.primeHr`;
- link back to Employee Portal.

The UI uses strict TypeScript, React/Next.js conventions already present in ISOFT HRIS, SCSS modules, typed API helpers, SweetAlert2, configurable runtime URLs, and no `any`. No public applicant route is created.

## 8. Expected repository impact

Exact filenames will be confirmed immediately before coding, but the approved boundary is:

### `hris`

- `PrimeHR/src/main/java/com/primehr/competency/{api,application,domain,infrastructure}/**`
- `PrimeHR/src/main/java/com/primehr/security/**`
- `PrimeHR/src/main/java/com/primehr/integration/administrative/**`
- `PrimeHR/src/main/java/com/primehr/shared/audit/**`
- `PrimeHR/src/main/java/com/primehr/shared/exception/**`
- `PrimeHR/src/main/resources/application*.properties`
- `PrimeHR/src/main/resources/db/migration/{postgresql,sqlserver}/V2__competency_draft_administration.sql`
- `PrimeHR/src/test/**`
- `contracts/openapi/primehr-v1.yaml`
- Administrative authorization DTO/service/controller, SSO target, System Config seed, and focused tests
- Phase/progress documentation

### `administrative-software`

- Permission catalog/type updates for `app.primehr` and `primehr.competency`
- PrimeHR portal-module checkbox and System Config-compatible typing only

### `employee-portal-UI`

- PrimeHR portal-module type/default/parser
- PrimeHR SSO target/launch destination/sidebar item

### New or existing `prime-hr-software`

- standalone Next.js configuration, shell, SSO callback, typed services, Competency pages/components/styles/tests

No other repository is in scope.

## 9. Tests and acceptance gates

### Backend behavior

- 401 without JWT; 403 without exact action permission.
- Administrator compatibility and audit coverage.
- Public input cannot override employee, role, permission, agency, state, version, or audit actor.
- Administrative permission lookup is authoritative and fails closed.
- DRAFT updates succeed only with matching optimistic version.
- Stale updates return 409 and do not create an audit event.
- ACTIVE and ARCHIVED records reject mutation.
- ACTIVE clone creates a new DRAFT lineage/version without modifying the source.
- Duplicate code/version, order, effectivity, cross-agency, wrong-scale Level, and illegal transition checks are enforced.
- Every successful command creates exactly one append-only audit event with redacted-safe content.
- Existing Phase 1A read contract and health behavior remain valid.

### Provider validation

- Fast unit/repository/security/controller tests.
- Flyway-enabled H2 fidelity path remains supplemental only.
- Real PostgreSQL migration from V1 to V2 plus Hibernate validation and command/repository tests.
- Real SQL Server migration from V1 to V2 plus the same verification.
- Fresh-database V1+V2 and upgrade-from-V1 paths are both tested.
- No credentials or test database addresses are committed.

### Frontend and ecosystem

- PrimeHR UI lint, strict type check, configured tests, and production build pass.
- Administrative, Employee Portal, and full backend affected builds/tests pass.
- SSO success, expired/used/wrong-target ticket, unauthorized module, and administrator paths are tested.
- Manual verification covers CRUD-equivalent draft actions, version conflict, active immutability, audit display, portal launch, and return-to-portal.
- `git diff --check`, secret/generated-file audit, and Phase-boundary audit pass.

## 10. Explicit exclusions

Phase 1B will not implement:

- DRAFT publishing/activation, approval, recommendation, or workflow;
- hard deletion;
- position or plantilla competency profiles;
- job-family inheritance or overrides;
- person competency profiles;
- assessments, assessment cycles, evidence, or gap analysis;
- RSP, SPMS, L&D, R&R, Evidence Center, applicants, public routes, or reports;
- documents, uploads, object storage, notifications, RabbitMQ, outbox, analytics, or reporting service;
- HRM, Timekeeping, or Payroll integration;
- combined HRISApp assembly or shared legacy datasource access;
- dynamic multi-agency resolution or a new JWT agency claim;
- broad cleanup of existing JWT, permission, bootstrap, CORS, secret, or frontend architecture outside touched PrimeHR contracts;
- destructive cleanup of Phase 1A.1 validation schemas.

## 11. Known risks and decisions accepted by approval

Approval of this scope accepts these design decisions:

1. Phase 1B includes both backend administration and the minimal standalone PrimeHR management UI, delivered through gated checkpoints.
2. Publishing/activation is deferred; only draft administration and successor-draft creation are allowed.
3. The current deployment remains single-agency through required server configuration.
4. Administrative is synchronously required for protected PrimeHR administration actions; outages fail closed.
5. `canDelete` means archive permission, never physical deletion.
6. Business definition version and JPA optimistic record version remain separate.
7. Category, Scale, and Competency are versioned aggregate roots; Levels and Indicators are versioned through cloned ownership by their parent aggregate.

## 12. Stop condition

After both checkpoints pass, Codex will update the implementation document and progress ledger, provide the exact commit-readiness review, recommend the next phase, and stop. No Phase 1C/2 or publishing functionality begins without a new explicit approval.
