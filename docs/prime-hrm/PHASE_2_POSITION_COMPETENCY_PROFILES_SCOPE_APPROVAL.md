# ISOFT PRIME-HRM Phase 2 - Position Competency Profiles Approval Scope

Status: Proposed; awaiting explicit user approval

Prepared: 2026-08-12

Depends on: completed Master Plan Phase 1 competency foundation through Phase 1C

## 1. Objective

Implement versioned position competency profiles that reference authoritative Administrative Job Position and Plantilla masters and exact published PrimeHR competency/proficiency versions. A profile defines the competencies and required levels for a position without copying or replacing the existing position master.

Phase 2 implements only Master Plan V2 Phase 2. It does not create person competency profiles, assessments, gap analysis, recruitment, SPMS, L&D, R&R, evidence files, reports, notifications, or a generic workflow engine.

## 2. Verified repository facts

- Administrative owns `JobPosition` (`jobPositionId`, name, salary grade/step) and `Plantilla` (`plantillaId`, name, `jobPositionId`).
- Existing Administrative endpoints expose whole lists but have no PrimeHR-specific paginated reference contract, source version, snapshot, or integration authorization.
- HRM appointments reference job position and plantilla, but HRM is not needed to define a position profile. Employee/appointment resolution remains Phase 3 or a later employee-facing integration.
- PrimeHR owns published competencies, their exact category/scale versions, proficiency levels, effectivity, lifecycle, authorization guard, and append-only audit.
- PrimeHR remains standalone with its own database. It must not query Administrative tables or create editable copies of Job Position or Plantilla.
- Current organization hierarchy and complete Qualification Standards remain incomplete and are not prerequisites for this phase.

## 3. Target and precedence model

A profile targets one authoritative position grain:

- `JOB_POSITION`: applies to all appointments/items using that Job Position unless a narrower Plantilla profile exists.
- `PLANTILLA`: applies only to that exact Plantilla item and records its authoritative parent Job Position.

For future profile resolution, an effective approved Plantilla profile takes precedence over the effective approved Job Position profile. Phase 2 exposes and tests this resolution rule but does not assign profiles to employees.

Each profile stores source identifiers plus an immutable target snapshot when approved:

- target type and source ID;
- Job Position ID/name and salary grade/step;
- optional Plantilla ID/name;
- source fingerprint and snapshot timestamp.

The snapshot is historical evidence, not an editable position master. Draft screens refresh current reference data from Administrative and clearly indicate when it differs from a stored snapshot.

## 4. Profile and requirement model

### Position profile root

- UUID/string ID generated in PrimeHR;
- agency scope (`DEFAULT` remains the current neutral single-agency partition unless configured otherwise);
- target type and authoritative target IDs;
- display name/description;
- business `definitionVersion`, `supersedesId`, optimistic `recordVersion`;
- `effectiveFrom` and optional `effectiveTo`;
- lifecycle and approval metadata;
- source target snapshot/fingerprint;
- standard created/updated/published/audit metadata.

### Competency requirement child

- exact published competency version ID;
- exact required proficiency-level ID from that competency's published scale version;
- classification exactly `MANDATORY` or `DESIRABLE`;
- optional agency-defined `criticalityCode` and explanatory remarks;
- display order and optimistic record version.

The same exact competency version may appear only once in a profile. Required proficiency must belong to that competency's exact published scale. Requirements are mutable only while the profile is a draft.

## 5. Lifecycle and governance recommendation

Because a position profile becomes an authoritative input to later assessment, gap, RSP, and L&D decisions, Phase 2 should use a small two-stage approval lifecycle rather than treating ordinary CRUD as approval:

```text
DRAFT -> SUBMITTED -> ACTIVE (approved)
                  -> DRAFT (returned with reason)
```

- `DRAFT`: editable and may be archived before submission.
- `SUBMITTED`: content locked while awaiting a decision.
- `ACTIVE`: approved/published, effective-dated, immutable, and historically queryable.
- Returning a submission to DRAFT requires a reason and audit; it is not a destructive rejection.
- Corrections to ACTIVE profiles use a successor draft.
- Self-approval is prohibited for ordinary users. Established administrators may override this only with an explicit reason and are audited.
- Approval closes an overlapping/open predecessor effective period atomically, using the Phase 1C historical-version pattern.

Administrative permission feature `primehr.position-profile` gains independent `canSubmit` and `canApprove` actions in addition to CRUD/access. Missing flags fail closed. Administrator behavior remains compatible and audited. This is a controlled domain workflow, not a general BPMN/workflow engine.

Approval of this scope accepts the two-stage lifecycle and ordinary-user separation of duties. If the organization instead wants direct publishing, revise this section before implementation.

## 6. Administrative reference contract

Add an authenticated, read-only integration endpoint owned by Administrative:

```text
GET /api/integration/v1/primehr/position-targets?type=&search=&page=&size=
GET /api/integration/v1/primehr/position-targets/{type}/{id}
```

Responses are typed and contain only the fields required in section 3. PrimeHR forwards the authenticated bearer token. Administrative enforces `primehr.position-profile.canAccess`; dependency failures fail closed for commands and return actionable 503 responses. No PrimeHR write reaches Administrative.

Existing Job Position/Plantilla CRUD endpoints and response shapes remain unchanged.

## 7. PrimeHR REST contract

Under `/api/primehr/v1/admin/position-profiles`:

- `GET /position-profiles` - paginated/filterable list.
- `GET /position-profiles/{id}` - details, requirements, snapshot, and lifecycle metadata.
- `POST /position-profiles` - create draft.
- `PUT /position-profiles/{id}` - edit draft root.
- `POST /position-profiles/{id}/requirements` - add draft requirement.
- `PUT /position-profiles/{id}/requirements/{requirementId}` - edit draft requirement.
- `POST /position-profiles/{id}/requirements/{requirementId}/archive` - archive draft requirement with reason.
- `POST /position-profiles/{id}/submit` - submit complete draft.
- `POST /position-profiles/{id}/return` - approver returns submission to draft with reason.
- `POST /position-profiles/{id}/approve` - approve/publish submitted version.
- `POST /position-profiles/{id}/versions` - create successor draft from ACTIVE version.
- `POST /position-profiles/{id}/archive` - archive an unsubmitted draft with reason.
- `GET /position-profiles/resolve?jobPositionId=&plantillaId=&asOf=` - apply exact Plantilla-then-Job Position precedence.
- `GET /position-profiles/compare?leftProfileId=&rightProfileId=` - compare two profile versions without person data.

No hard DELETE, bulk approval, unpublish, person assignment, or employee assessment endpoint is added. OpenAPI remains the authoritative contract.

## 8. Validation and historical rules

- Target must resolve from Administrative at create, refresh, submit, and approval time.
- A submitted profile requires `effectiveFrom` and at least one active competency requirement.
- Every competency must be published and effective on the profile start date.
- Required level must belong to the exact competency's published scale and be effective on the profile start date.
- Target/code/version uniqueness and effective ranges may not overlap at the same target grain.
- A Plantilla target's parent Job Position must match the Administrative response.
- Approval uses pessimistic chain locking plus optimistic record versions and is atomic with snapshot, predecessor closure, and audit events.
- Completed versions retain exact competency/level IDs and target snapshots even after source masters or competency successors change.
- Comparison uses exact selected versions; it never silently substitutes current competency definitions.

## 9. Persistence and portability

Add equivalent forward V4 migrations:

```text
PrimeHR/src/main/resources/db/migration/postgresql/V4__position_competency_profiles.sql
PrimeHR/src/main/resources/db/migration/sqlserver/V4__position_competency_profiles.sql
```

Expected tables:

- `prime_position_profile`
- `prime_position_profile_requirement`
- lifecycle/audit data continues using the shared append-only audit table

Migrations define equivalent primary keys, foreign keys to PrimeHR competency/level records, target/version/effectivity indexes, uniqueness constraints, lengths, booleans, and timestamp types. Shared Java code uses JPA/JPQL and no provider-specific application query.

Fresh V1-V4 and populated V3-to-V4 upgrades plus Hibernate validation must run against real PostgreSQL and SQL Server. Because configuration is now one physical properties file, provider validation commands must explicitly override the complete datasource, driver, schema, and Flyway location together; both paths must be demonstrated before Phase 2 backend acceptance.

## 10. UI scope

### Administrative UI

- Add `Position Competency Profiles` permission under PRIME-HRM.
- Support Access/Add/Edit/Delete/Submit/Approve flags, with Access required by every action.
- Preserve old permission JSON and administrator behavior.

### PrimeHR UI

- Add `Position Profiles` navigation and management page.
- Search/select current Job Position or Plantilla targets through the typed Administrative endpoint.
- Create/edit drafts and dynamic competency requirements.
- Select only published competencies and valid levels.
- Show completeness, source freshness, target snapshot, version lineage, status, audit, submit/return/approve controls, and immutable ACTIVE state.
- Provide side-by-side profile comparison showing added, removed, changed level, classification, and criticality requirements.
- Preserve SSO, Employee Portal return, strict TypeScript, SCSS modules, loading/empty/unauthorized/dependency/error states, and actionable conflict messages.

No Employee Portal change is included because Phase 2 defines positions, not employee/person profiles.

## 11. Execution checkpoints

### Phase 2.1 - Reference contract and draft foundation

- Administrative typed position-target reads and authorization;
- PrimeHR V4 schema/entities/DTOs/repositories;
- draft profile and requirement CRUD/archive/version rules;
- OpenAPI and focused tests;
- PostgreSQL/SQL Server fresh/upgrade gates.

Stop if any provider, authorization, source-ownership, or historical-version gate fails.

### Phase 2.2 - Submission, approval, resolution, and comparison

- independent Submit/Approve permissions;
- DRAFT/SUBMITTED/ACTIVE transitions and separation of duties;
- snapshot/fingerprint, predecessor closure, target precedence resolution, comparison, conflicts, and complete audit;
- focused and full backend/provider gates.

Do not begin Phase 2.3 until every Phase 2.2 backend gate passes.

### Phase 2.3 - Administrative and PrimeHR UI

- permission controls and position-profile management/comparison UI;
- lint, strict type-check, production builds, and manual allowed/denied/submit/return/approve/conflict/history acceptance;
- final repository/secret/generated/phase-boundary audit and review manifest.

Frontend pushing/deployment remains separately controlled by the user.

## 12. Expected repository impact

### `hris`

- focused Administrative integration DTO/controller/service and authorization tests;
- `PrimeHR/.../positionprofile/{api,application,domain,infrastructure}/**`;
- additive PrimeHR authorization/integration/audit/exception changes;
- PostgreSQL and SQL Server V4 migrations and provider tests;
- `contracts/openapi/primehr-v1.yaml`;
- Phase 2 documentation and progress ledger.

### `administrative-software`

- `src/app/administrative/permission/Permission.tsx` only as needed for the new action flags/feature.

### `prime-hr-software`

- typed permission/auth additions;
- Position Profiles page/component/SCSS/navigation/runtime client additions.

### Explicitly untouched

- Employee Portal, HRM UI, Timekeeping UI, Payroll UI;
- HRIS HumanResource, TimeKeeping, Payroll, EmployeePortal business behavior;
- Jasper reports, messaging, files, analytics, HRISApp combined datasource/runtime;
- deployed frontend branches and user `.env` files.

## 13. Test and acceptance gates

- old permission JSON fails closed for Submit/Approve;
- Access/CRUD cannot substitute for Submit/Approve;
- ordinary submitter cannot approve their own submission;
- authorized approver and administrator override are correctly audited;
- invalid target, stale reference, missing requirement, wrong competency/level version, overlap, stale record, and illegal transitions are rejected without partial state/audit;
- Plantilla resolution overrides Job Position only when an effective approved Plantilla profile exists;
- exact historical target and competency snapshots remain unchanged;
- comparison correctly classifies added/removed/changed requirements;
- existing Phase 1 endpoints/tests remain green;
- fresh V1-V4 and populated V3-to-V4 migrations pass on real PostgreSQL and SQL Server;
- full affected Maven tests/package and frontend production builds pass, with zero/skipped tests reported;
- manual allowed/denied/submit/return/approve/conflict/immutability/history behavior passes.

## 14. Explicit exclusions

- person profiles, subjects, assessors, ratings, evidence, competency gaps, IDPs, and employee matching;
- Qualification Standards expansion or duplication;
- vacancy/RSP, SPMS, L&D, R&R, succession, Evidence Center, reports, documents, notifications, messaging, analytics, outbox, or gateway;
- generic configurable workflow designer, committee assignment, or multi-step agency workflow;
- direct PrimeHR access to Administrative/HRM tables;
- hard deletion, unpublish, bulk approval, and retroactive mutation of approved history;
- dynamic multi-agency identity and combined HRISApp datasource integration.

## 15. Decisions accepted by approving this scope

Approval accepts:

1. both Job Position and Plantilla targets, with exact Plantilla precedence over Job Position;
2. immutable source snapshots plus live Administrative validation;
3. exact published competency/level version references;
4. a two-stage submit/approve lifecycle with independent permissions and ordinary-user separation of duties;
5. administrator override only with reason and complete audit;
6. no employee/person assessment or gap functionality in Phase 2;
7. execution in Phase 2.1, 2.2, and 2.3 checkpoints with gates between them.

## 16. Stop condition

After creating this scope, stop. Do not create V4 migrations, position-profile entities, Administrative reference endpoints, permissions, APIs, or UI until the user explicitly approves this exact Phase 2 scope.
