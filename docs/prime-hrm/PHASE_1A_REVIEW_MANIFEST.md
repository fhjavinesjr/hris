# ISOFT PRIME-HRM Phase 1A Review Manifest

Prepared: 2026-08-03

Review scope: Phase 1A — Standalone Competency Foundation

Repository: `hris`

Status: historical Phase 1A review baseline; superseded for security/provider findings by Phase 1A.1

> Phase 1A.1 subsequently resolved the arbitrary-agency override, read-permission, migration-fidelity, and real-provider-validation findings recorded in this manifest. See `PHASE_1A_1_HARDENING.md` and `PRIME_HRM_PROGRESS.md`. This manifest intentionally preserves the original independent-review snapshot instead of rewriting historical command results.

## Scope and source comparison

This manifest compares the working tree against:

- `.codex/ISOFT_PRIME_HRM_CODEX_MASTER_PLAN_V2.md`
- `docs/prime-hrm/PHASE_0_ARCHITECTURE_DISCOVERY.md`
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md`
- `docs/prime-hrm/PHASE_1A_COMPETENCY_FOUNDATION.md`

The request used `docs/prime-hr`, but the repository's established canonical directory is `docs/prime-hrm`; this manifest is stored with the other PRIME-HRM documents to avoid a duplicate documentation tree.

The explicit Phase 1A boundary and the Phase 0 architecture decision control where the older master plan is ambiguous: this delivery is a standalone backend foundation with read-only APIs, no frontend, no write API, no HRISApp assembly, and no Phase 1B workflow. The master plan's references to draft management/UI are therefore not treated as authorization to implement Phase 1B.

## 1–3. Created and modified files, with reasons

### Created files

#### Module and bootstrapping

| File | Reason |
|---|---|
| `PrimeHR/pom.xml` | Defines the standalone Spring Boot module, runtime drivers for both database providers, Flyway, security, validation, JPA, Actuator, and test dependencies. |
| `PrimeHR/src/main/java/com/primehr/PrimeHRApplication.java` | Provides the isolated PrimeHR application entry point and configuration-properties/JPA-auditing setup. |

#### Competency API DTOs and controller

| File | Reason |
|---|---|
| `PrimeHR/src/main/java/com/primehr/competency/api/BehavioralIndicatorResponse.java` | Typed read model for an indicator and its proficiency-level/effectivity metadata. |
| `PrimeHR/src/main/java/com/primehr/competency/api/CompetencyCategoryResponse.java` | Typed read model for a competency category. |
| `PrimeHR/src/main/java/com/primehr/competency/api/CompetencyDetailResponse.java` | Groups competency summary, scale, levels, and ordered indicators for detail reads. |
| `PrimeHR/src/main/java/com/primehr/competency/api/CompetencyQueryController.java` | Exposes the four Phase 1A authenticated GET endpoints only. |
| `PrimeHR/src/main/java/com/primehr/competency/api/CompetencySummaryResponse.java` | Typed competency list/detail summary without exposing JPA entities. |
| `PrimeHR/src/main/java/com/primehr/competency/api/ProficiencyLevelResponse.java` | Typed read model for an agency-defined proficiency level. |
| `PrimeHR/src/main/java/com/primehr/competency/api/ProficiencyScaleResponse.java` | Typed read model for a proficiency scale and its ordered levels. |

#### Application service

| File | Reason |
|---|---|
| `PrimeHR/src/main/java/com/primehr/competency/application/CompetencyQueryService.java` | Defines the read-only competency query boundary. |
| `PrimeHR/src/main/java/com/primehr/competency/application/CompetencyQueryServiceImpl.java` | Applies agency, active/effective-date, search, ordering, pagination, and entity-to-DTO mapping rules. |

#### Domain entities

| File | Reason |
|---|---|
| `PrimeHR/src/main/java/com/primehr/competency/domain/BehavioralIndicator.java` | Models observable behavior/evidence guidance by competency and proficiency level. |
| `PrimeHR/src/main/java/com/primehr/competency/domain/Competency.java` | Models the agency competency dictionary item, category, scale, status, ordering, and effectivity. |
| `PrimeHR/src/main/java/com/primehr/competency/domain/CompetencyCategory.java` | Models agency-configurable competency categories. |
| `PrimeHR/src/main/java/com/primehr/competency/domain/ProficiencyLevel.java` | Models ordered, non-fixed levels within a scale. |
| `PrimeHR/src/main/java/com/primehr/competency/domain/ProficiencyScale.java` | Models an agency-defined proficiency scale. |

#### Persistence

| File | Reason |
|---|---|
| `PrimeHR/src/main/java/com/primehr/competency/infrastructure/BehavioralIndicatorRepository.java` | Provides agency/competency/level-scoped indicator reads. |
| `PrimeHR/src/main/java/com/primehr/competency/infrastructure/CompetencyCategoryRepository.java` | Provides agency-scoped category reads. |
| `PrimeHR/src/main/java/com/primehr/competency/infrastructure/CompetencyRepository.java` | Provides agency-scoped competency lookup and specification execution. |
| `PrimeHR/src/main/java/com/primehr/competency/infrastructure/CompetencySpecifications.java` | Builds provider-neutral JPA Criteria filters for category, active/effectivity, and search. |
| `PrimeHR/src/main/java/com/primehr/competency/infrastructure/ProficiencyLevelRepository.java` | Provides ordered agency/scale-scoped proficiency-level reads. |
| `PrimeHR/src/main/java/com/primehr/competency/infrastructure/ProficiencyScaleRepository.java` | Provides agency-scoped proficiency-scale reads. |

#### Shared configuration, security, API, auditing, and exceptions

| File | Reason |
|---|---|
| `PrimeHR/src/main/java/com/primehr/config/AuditConfiguration.java` | Supplies Spring Data auditing values from the authenticated principal. |
| `PrimeHR/src/main/java/com/primehr/config/PrimeHrProperties.java` | Binds mandatory JWT secret and configurable CORS settings. |
| `PrimeHR/src/main/java/com/primehr/security/PrimeHrJwtAuthenticationFilter.java` | Validates compatible ISOFT employee HMAC JWTs without changing legacy Common behavior. |
| `PrimeHR/src/main/java/com/primehr/security/PrimeHrSecurityConfiguration.java` | Makes health public, requires authentication for GET APIs, denies all other requests, and configures stateless CORS/security errors. |
| `PrimeHR/src/main/java/com/primehr/shared/api/ApiErrorResponse.java` | Defines the consistent API error payload. |
| `PrimeHR/src/main/java/com/primehr/shared/api/PageResponse.java` | Defines the typed provider-neutral pagination response. |
| `PrimeHR/src/main/java/com/primehr/shared/audit/AgencyAuditableEntity.java` | Centralizes agency scope, active/effectivity, ordering, optimistic version, and audit columns. |
| `PrimeHR/src/main/java/com/primehr/shared/exception/PrimeHrExceptionHandler.java` | Converts validation, not-found, and request errors to stable responses. |
| `PrimeHR/src/main/java/com/primehr/shared/exception/ResourceNotFoundException.java` | Represents a scoped competency resource that cannot be found. |

#### Runtime configuration and migrations

| File | Reason |
|---|---|
| `PrimeHR/src/main/resources/application.properties` | Defines the standalone port/profile, schema validation, Flyway, health, JWT, and CORS configuration. |
| `PrimeHR/src/main/resources/application-postgresql.properties` | Selects the PostgreSQL datasource driver and PostgreSQL Flyway location through environment variables. |
| `PrimeHR/src/main/resources/application-sqlserver.properties` | Selects the SQL Server datasource driver and SQL Server Flyway location through environment variables. |
| `PrimeHR/src/main/resources/db/migration/postgresql/V1__competency_foundation.sql` | Creates the Phase 1A PostgreSQL schema, constraints, foreign keys, and indexes without seed data. |
| `PrimeHR/src/main/resources/db/migration/sqlserver/V1__competency_foundation.sql` | Creates the equivalent Phase 1A SQL Server schema, constraints, foreign keys, and indexes without seed data. |

#### Tests

| File | Reason |
|---|---|
| `PrimeHR/src/test/java/com/primehr/PrimeHrApplicationSmokeTest.java` | Starts the isolated test application context and verifies the public health endpoint. |
| `PrimeHR/src/test/java/com/primehr/competency/api/CompetencyQueryControllerTest.java` | Verifies missing-token rejection, compatible JWT read access, required agency validation, and denial of writes. |
| `PrimeHR/src/test/java/com/primehr/competency/application/CompetencyQueryServiceImplTest.java` | Verifies inactive filtering, expired-indicator filtering/order, and pagination mapping with mocked repositories. |
| `PrimeHR/src/test/java/com/primehr/competency/domain/CompetencyDomainConstraintTest.java` | Verifies configurable scales, effectivity/order validation, uppercase/agency rules, and indicator-to-scale consistency. |
| `PrimeHR/src/test/java/com/primehr/competency/infrastructure/CompetencyRepositoryTest.java` | Verifies agency/category/search/effectivity queries, uniqueness behavior, cross-agency code reuse, audit population, and indicator ordering on H2. |
| `PrimeHR/src/test/java/com/primehr/migration/CompetencyMigrationParityTest.java` | Textually verifies PostgreSQL/SQL Server table and named-constraint parity and absence of seed/cross-domain SQL. |
| `PrimeHR/src/test/resources/application-test.properties` | Configures an isolated H2 PostgreSQL-mode test datasource and test JWT/CORS values; Flyway is disabled in this profile. |

#### Contract and documentation

| File | Reason |
|---|---|
| `contracts/openapi/primehr-v1.yaml` | Records the read-only Phase 1A REST contract, parameters, responses, schemas, and bearer authentication. |
| `docs/prime-hrm/PHASE_1A_COMPETENCY_FOUNDATION.md` | Documents scope, design, configuration, migration commands, API behavior, verification, and limitations. |
| `docs/prime-hrm/PHASE_1A_REVIEW_MANIFEST.md` | This independent-review inventory and verification record; it changes no runtime behavior. |

### Modified files

| File | Why modified |
|---|---|
| `pom.xml` | Adds `PrimeHR` to the root Maven reactor. It does not add PrimeHR to `HRISApp`. |
| `docs/prime-hrm/PRIME_HRM_PROGRESS.md` | Advances the ledger from Phase 0 to completed-local Phase 1A and records boundaries, validation limits, and Phase 1B as not started. |
| `.idea/compiler.xml` | IntelliJ module metadata now recognizes PrimeHR annotation processing. This is IDE metadata, not application behavior. |
| `.idea/encodings.xml` | IntelliJ marks PrimeHR Java/resources as UTF-8. This is IDE metadata, not application behavior. |

No existing Java source, runtime properties, JRXML, frontend project, HRISApp assembly, or existing module POM was modified for Phase 1A.

## 4. Phase 1A requirement mapping

| Requirement | Implementing files | Review status |
|---|---|---|
| Standalone Maven/Spring Boot module | root `pom.xml`; `PrimeHR/pom.xml`; `PrimeHRApplication.java` | Implemented; not assembled into HRISApp. |
| Independent provider-selectable datasource | `application.properties`; `application-postgresql.properties`; `application-sqlserver.properties` | Implemented by profiles/environment variables; real providers not exercised. |
| Flyway-controlled schema for both providers | both `V1__competency_foundation.sql` files | Implemented; structural parity tested only. |
| Agency-configurable category dictionary | `CompetencyCategory`; repository; response; query service/controller | Implemented read-only. |
| Competency dictionary with category, definition, status, ordering, and effectivity | `Competency`; specifications/repository; summary/detail DTOs; service/controller | Implemented read-only. |
| Agency-defined scales with arbitrary ordered levels | `ProficiencyScale`; `ProficiencyLevel`; repositories/DTOs/service | Implemented; no fixed five-level assumption. |
| Behavioral indicators by competency and level | `BehavioralIndicator`; repository/DTO/service | Implemented read-only and ordered. |
| Effective dating, active flag, optimistic version, audit fields | `AgencyAuditableEntity`; all entities; `AuditConfiguration`; migrations | Implemented foundation. No write workflow exercises optimistic locking. |
| Provider-neutral query logic | Spring Data repositories; `CompetencySpecifications`; query service | Implemented without shared native query SQL. |
| Read APIs with filters/search/pagination | `CompetencyQueryController`; query service; repositories; `PageResponse` | Implemented. |
| Typed contract | response records; `ApiErrorResponse`; `PageResponse`; OpenAPI file | Implemented. |
| JWT and deny-by-default security | JWT filter; security configuration; properties; controller tests | Authentication implemented. Granular Administrative PrimeHR permission and principal-to-agency authorization are not implemented. |
| Public operational health | Actuator dependency/config/security; smoke test | Implemented at `/actuator/health`. |
| No seeded government/agency labels | migration files; migration parity test | Implemented; migrations contain no `INSERT`. |
| Tests and handoff documentation | six test classes; test properties; Phase 1A document; progress ledger; this manifest | Implemented, subject to real-provider gaps. |
| Phase 1A read-only/no UI boundary | security configuration; controller; OpenAPI; absence of frontend changes | Implemented. |

## 5. Package and module structure

```text
hris/
├── PrimeHR/                         standalone Spring Boot jar
│   └── src/
│       ├── main/
│       │   ├── java/com/primehr/
│       │   │   ├── competency/
│       │   │   │   ├── api/         REST controller and response DTOs
│       │   │   │   ├── application/ query-service boundary and implementation
│       │   │   │   ├── domain/      five competency-foundation entities
│       │   │   │   └── infrastructure/ repositories and JPA specifications
│       │   │   ├── config/          auditing and configuration binding
│       │   │   ├── security/        JWT and Spring Security
│       │   │   └── shared/          API, audit, and exception primitives
│       │   └── resources/
│       │       └── db/migration/{postgresql,sqlserver}/
│       └── test/                     API, service, domain, repository, migration, smoke tests
├── contracts/openapi/primehr-v1.yaml
└── docs/prime-hrm/
```

The module has no dependency on Administrative, HumanResource, TimeKeeping, EmployeePortal, Payroll, or HRISApp, and the combined HRISApp has not been changed to scan or start PrimeHR.

## 6. Database tables, constraints, indexes, and relationships

### Tables

| Table | Purpose |
|---|---|
| `prime_competency_category` | Agency-owned category dictionary. |
| `prime_proficiency_scale` | Agency-owned proficiency scale. |
| `prime_proficiency_level` | Ordered levels within a scale. |
| `prime_competency` | Competency dictionary linked to a category and scale. |
| `prime_behavioral_indicator` | Observable behavior/evidence linked to a competency and one of its scale levels. |

Every table uses a string UUID-compatible primary key and carries `agency_id`, `active`, `display_order`, nullable `effective_from`/`effective_to`, `record_version`, `created_at`, `created_by`, `updated_at`, and `updated_by` foundation columns.

### Unique constraints

- `uk_prime_category_agency_code (agency_id, code)`
- `uk_prime_scale_agency_code (agency_id, code)`
- `uk_prime_level_scale_code (scale_id, code)`
- `uk_prime_level_scale_order (scale_id, level_order)`
- `uk_prime_competency_agency_code (agency_id, code)`
- `uk_prime_indicator_order (competency_id, proficiency_level_id, display_order)`

### Check constraints

- Codes are constrained to uppercase for categories, scales, levels, and competencies.
- Shared display order is non-negative.
- Proficiency `level_order` is at least 1.
- An effectivity end date cannot precede its start date.

### Foreign keys and relationships

- `prime_proficiency_level.scale_id` → `prime_proficiency_scale.id` (many levels to one scale).
- `prime_competency.category_id` → `prime_competency_category.id` (many competencies to one category).
- `prime_competency.proficiency_scale_id` → `prime_proficiency_scale.id` (many competencies may use a scale).
- `prime_behavioral_indicator.competency_id` → `prime_competency.id`.
- `prime_behavioral_indicator.proficiency_level_id` → `prime_proficiency_level.id`.

The Java domain additionally validates agency consistency and that an indicator's level belongs to the competency's assigned scale. Those cross-row rules are not expressed as a single database constraint.

### Indexes

- `ix_prime_category_agency_active`
- `ix_prime_scale_agency_active`
- `ix_prime_level_agency_scale`
- `ix_prime_competency_filter`
- `ix_prime_indicator_lookup`

## 7–8. Provider migrations

### PostgreSQL

- `PrimeHR/src/main/resources/db/migration/postgresql/V1__competency_foundation.sql`
- Uses PostgreSQL-compatible types and DDL for all five tables, named checks/uniques/foreign keys, and five indexes.
- Selected only by the `postgresql` Spring profile.

### SQL Server

- `PrimeHR/src/main/resources/db/migration/sqlserver/V1__competency_foundation.sql`
- Provides equivalent SQL Server types and DDL with the same logical tables, named constraints, relationships, and indexes.
- Selected only by the `sqlserver` Spring profile.

Neither migration includes seed data. Migration equivalence was checked by `CompetencyMigrationParityTest`, but neither script was executed against its real database engine during this review.

## 9. REST endpoints and authorization

Base path: `/api/primehr/v1`

| Method and path | Purpose | Authorization currently enforced |
|---|---|---|
| `GET /competency-categories` | Lists agency categories with optional `active` and `asOf`. | Valid bearer JWT required. |
| `GET /competencies` | Agency-scoped search/filter/page of competencies. | Valid bearer JWT required. |
| `GET /competencies/{competencyId}` | Returns scoped competency, scale/levels, and indicators. | Valid bearer JWT required. |
| `GET /proficiency-scales` | Lists agency scales and ordered levels. | Valid bearer JWT required. |
| `GET /actuator/health` | Minimal service health. | Public. |

All non-GET and all otherwise unmatched requests are denied. CORS permits only configured origins/patterns and `GET, OPTIONS`.

Authorization limitation: the implementation proves authentication only. It does not check `canAccess`/PrimeHR feature permission, role-specific action permissions, or bind the caller's JWT identity to the requested `agencyId`. An authenticated user can submit another agency identifier unless an upstream gateway supplies that control. This is a material prerequisite before production exposure or Phase 1B writes.

## 10. DTOs, entities, repositories, services, and controllers

- DTOs: `BehavioralIndicatorResponse`, `CompetencyCategoryResponse`, `CompetencyDetailResponse`, `CompetencySummaryResponse`, `ProficiencyLevelResponse`, `ProficiencyScaleResponse`, `ApiErrorResponse`, and generic `PageResponse`.
- Entities: `CompetencyCategory`, `Competency`, `ProficiencyScale`, `ProficiencyLevel`, and `BehavioralIndicator`, all based on `AgencyAuditableEntity`.
- Repositories: one Spring Data repository for each entity; `CompetencySpecifications` supplies provider-neutral dynamic filters.
- Services: `CompetencyQueryService` and `CompetencyQueryServiceImpl`; there is intentionally no command/write service.
- Controllers: `CompetencyQueryController`; there is intentionally no command/write controller.
- Shared runtime support: `AuditConfiguration`, `PrimeHrProperties`, JWT filter, security configuration, exception handler, and not-found exception.

## 11. Test classes and verified behavior

| Test class | Tests | What it verifies |
|---|---:|---|
| `CompetencyDomainConstraintTest` | 4 | Arbitrary scale size; invalid effectivity/order rejection; uppercase and cross-agency rules; indicator level/scale consistency. |
| `CompetencyRepositoryTest` | 3 | Agency/category/search/effectivity filtering; same-agency uniqueness and cross-agency code reuse; auditing and ordered indicators on H2. |
| `CompetencyQueryServiceImplTest` | 3 | Inactive data filtering; expired indicator filtering/order; page conversion. Repositories are mocked. |
| `CompetencyQueryControllerTest` | 3 | Unauthenticated 401; compatible signed JWT read; missing agency 400; write denied 403. Service is mocked. |
| `CompetencyMigrationParityTest` | 2 | Both SQL files declare corresponding tables/constraints and contain no seed/cross-domain statements. It does not execute DDL. |
| `PrimeHrApplicationSmokeTest` | 1 | H2-backed application context starts and public health responds successfully. |

PrimeHR total: 16 tests, 0 failures, 0 errors, 0 skipped.

## 12. Exact commands and results

Commands were run from the backend Maven root on 2026-08-03 (Asia/Manila).

### Git inspection after this manifest

```text
> git status --short
 M .idea/compiler.xml
 M .idea/encodings.xml
 M docs/prime-hrm/PRIME_HRM_PROGRESS.md
 M pom.xml
?? PrimeHR/
?? contracts/
?? docs/prime-hrm/PHASE_1A_COMPETENCY_FOUNDATION.md
?? docs/prime-hrm/PHASE_1A_REVIEW_MANIFEST.md
```

```text
> git diff --stat
 .idea/compiler.xml                   |  1 +
 .idea/encodings.xml                  |  2 ++
 docs/prime-hrm/PRIME_HRM_PROGRESS.md | 52 +++++++++++++++++++++---------------
 pom.xml                              |  1 +
 4 files changed, 34 insertions(+), 22 deletions(-)
```

`git diff --stat` reports tracked-file differences only; it omits all 46 untracked files, including this manifest.

```text
> git diff --check
(no output)
```

Exit code: 0. Git emitted line-ending warnings that LF will be replaced by CRLF for `pom.xml` and `PRIME_HRM_PROGRESS.md`; no whitespace error was reported.

### Maven verification

```text
> .\mvnw.cmd clean test
Reactor: 9/9 SUCCESS
BUILD SUCCESS
Total time: 01:58 min
Exit code: 0
```

```text
> .\mvnw.cmd clean package
Reactor: 9/9 SUCCESS
BUILD SUCCESS
Total time: 02:24 min
Exit code: 0
PrimeHR artifact: PrimeHR/target/PrimeHR-1.0-SNAPSHOT.jar
```

No `-DskipTests`, `-Dmaven.test.skip`, or equivalent option was used. The package command reran the tests.

Surefire XML totals after `mvn clean package`:

| Module | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| Common | 0 | 0 | 0 | 0 |
| TimeKeeping | 7 | 0 | 0 | 0 |
| Administrative | 9 | 0 | 0 | 0 |
| HumanResource | 34 | 0 | 0 | 0 |
| EmployeePortal | 0 | 0 | 0 | 0 |
| Payroll | 45 | 0 | 0 | 0 |
| HRISApp | 0 | 0 | 0 | 0 |
| PrimeHR | 16 | 0 | 0 | 0 |
| **Total** | **111** | **0** | **0** | **0** |

No test was reported skipped. Common explicitly reported `Tests run: 0`; EmployeePortal and HRISApp produced no test suites. The reactor still executed 111 tests overall, and the affected PrimeHR module executed 16.

## 13–15. Real database validation, mocked/assumed/skipped/unverified behavior

### PostgreSQL

Not tested against a real PostgreSQL instance. H2 was run in PostgreSQL compatibility mode for repository/context tests. This does not prove Flyway DDL, index creation, driver behavior, collation, timestamp semantics, or production Neon connectivity.

### SQL Server

Not tested against a real SQL Server instance. The SQL Server migration was inspected and structurally compared to the PostgreSQL migration, but it was not executed. Driver behavior, DDL execution, Unicode/collation, timestamp behavior, and connectivity remain unverified.

### Mocked, assumed, skipped, or otherwise unverified

- Controller tests mock the query service; service tests mock repositories.
- Repository and smoke tests use H2 with Hibernate-created schema; the test profile disables Flyway.
- Migration parity tests parse SQL text; they do not start either provider or execute migrations.
- JWT compatibility is tested with a synthetic HMAC token and configured test secret. Compatibility with every deployed token variant and secret configuration is assumed, not integration-tested with a live Administrative login.
- Authentication is verified, but PrimeHR feature RBAC and authenticated principal-to-agency scoping are absent.
- No frontend/browser, real CORS deployment, gateway, Render/Vercel, or live employee identity integration was tested.
- No production data, concurrency race, optimistic-lock write, or audit update flow was exercised because Phase 1A exposes no writes.
- No migration rollback/down script exists; production rollback after migration is explicitly a reviewed forward migration.
- No tests were intentionally skipped, but Common, EmployeePortal, and HRISApp execute zero test cases as described above.

## 16. Existing HRIS files modified

Existing tracked HRIS files changed are exactly:

1. `pom.xml` — reactor registration only.
2. `docs/prime-hrm/PRIME_HRM_PROGRESS.md` — documentation ledger only.
3. `.idea/compiler.xml` — IDE module metadata only.
4. `.idea/encodings.xml` — IDE encoding metadata only.

The `.idea` changes are not required for runtime behavior and should receive an explicit reviewer decision on whether repository policy permits committing IDE metadata. They were not used to claim application functionality.

## 17. Phase 1B or later functionality audit

No Phase 1B or later functionality was found in the Phase 1A change set:

- no POST, PUT, PATCH, or DELETE controller mappings;
- no command service or administrative CRUD workflow;
- no frontend route, page, form, or navigation change;
- no position profile, assessment, gap analysis, learning, succession, recruitment, performance, analytics, notification, storage, Jasper, or messaging implementation;
- no HRISApp assembly/deployment change;
- no cross-domain table or seed data in the migrations.

The model's `status`, audit, effectivity, and optimistic-version columns are Phase 1A foundation fields. They do not constitute a Phase 1B draft-management workflow because there are no mutation endpoints or services.

## 18. Known risks and unresolved decisions

1. **Authorization:** granular Administrative permission checks and trusted agency scope are not implemented. Authentication alone is insufficient for production multi-agency exposure.
2. **Real-provider proof:** neither Flyway migration has run against a real PostgreSQL/Neon or SQL Server instance.
3. **Test schema fidelity:** H2/Hibernate tests can pass while provider-specific DDL fails; real-provider migration/integration tests remain required.
4. **JWT integration:** PrimeHR intentionally does not reuse Common's hardcoded/logging JWT implementation; final secret rotation/distribution and live SSO compatibility must be validated operationally.
5. **Ownership decisions:** supervisor hierarchy and complete Qualification Standards ownership remain unresolved from Phase 0 and are outside Phase 1A.
6. **Deployment topology:** PrimeHR remains standalone. Any later HRISApp inclusion requires isolated multi-datasource configuration and explicit approval.
7. **Database rollback:** there is no destructive automatic rollback. Environments that have applied V1 require reviewed forward migrations.
8. **IDE metadata:** reviewers should decide whether `.idea/compiler.xml` and `.idea/encodings.xml` belong in the commit.
9. **Line endings:** Git reported LF-to-CRLF conversion warnings for two tracked files; `git diff --check` still passed.
10. **Master-plan ambiguity:** older Phase 1 language includes draft management/UI, while the explicit Phase 1A contract prohibits Phase 1B. The implementation follows the narrower explicit authorization.

## Independent-review conclusion

The Phase 1A change set compiles, packages, and executes its 16 PrimeHR tests successfully within the full 111-test reactor. It remains suitable for code review, not yet for a claim of production database acceptance. Independent approval should be withheld from production deployment until real PostgreSQL and SQL Server migration/integration tests and the agency/RBAC authorization contract are completed or explicitly risk-accepted. No additional feature implementation was performed while preparing this manifest.
