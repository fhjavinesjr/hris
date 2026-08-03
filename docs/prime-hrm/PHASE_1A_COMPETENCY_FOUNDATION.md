# ISOFT PRIME-HRM Phase 1A — Standalone Competency Foundation

Status: Implemented and hardened by Phase 1A.1; real PostgreSQL and SQL Server validated
Date: 2026-08-03
Boundary: Standalone backend and read-only Competency Management only

## Scope delivered

Phase 1A introduces a standalone `PrimeHR` Spring Boot module with a PrimeHR-owned datasource, environment-selected PostgreSQL or SQL Server configuration, Flyway, JPA validation, Actuator health, configurable CORS, ISOFT-compatible JWT validation, consistent JSON errors, and read-only Competency APIs. Phase 1A.1 adds trusted server-side agency scope, a dedicated competency-read authority, Flyway-created-schema integration tests, and real-provider validation. Full hardening evidence is in `PHASE_1A_1_HARDENING.md`.

It does not add PrimeHR to HRISApp and does not modify Administrative, Common, EmployeePortal, HumanResource, Payroll, TimeKeeping, or any frontend.

## Files created

```text
PrimeHR/pom.xml
PrimeHR/src/main/java/com/primehr/PrimeHRApplication.java
PrimeHR/src/main/java/com/primehr/config/AuditConfiguration.java
PrimeHR/src/main/java/com/primehr/config/PrimeHrProperties.java
PrimeHR/src/main/java/com/primehr/security/PrimeHrJwtAuthenticationFilter.java
PrimeHR/src/main/java/com/primehr/security/PrimeHrSecurityConfiguration.java
PrimeHR/src/main/java/com/primehr/shared/api/ApiErrorResponse.java
PrimeHR/src/main/java/com/primehr/shared/api/PageResponse.java
PrimeHR/src/main/java/com/primehr/shared/audit/AgencyAuditableEntity.java
PrimeHR/src/main/java/com/primehr/shared/exception/PrimeHrExceptionHandler.java
PrimeHR/src/main/java/com/primehr/shared/exception/ResourceNotFoundException.java
PrimeHR/src/main/java/com/primehr/competency/api/*Response.java
PrimeHR/src/main/java/com/primehr/competency/api/CompetencyQueryController.java
PrimeHR/src/main/java/com/primehr/competency/application/CompetencyQueryService.java
PrimeHR/src/main/java/com/primehr/competency/application/CompetencyQueryServiceImpl.java
PrimeHR/src/main/java/com/primehr/competency/domain/BehavioralIndicator.java
PrimeHR/src/main/java/com/primehr/competency/domain/Competency.java
PrimeHR/src/main/java/com/primehr/competency/domain/CompetencyCategory.java
PrimeHR/src/main/java/com/primehr/competency/domain/ProficiencyLevel.java
PrimeHR/src/main/java/com/primehr/competency/domain/ProficiencyScale.java
PrimeHR/src/main/java/com/primehr/competency/infrastructure/*Repository.java
PrimeHR/src/main/java/com/primehr/competency/infrastructure/CompetencySpecifications.java
PrimeHR/src/main/resources/application.properties
PrimeHR/src/main/resources/application-postgresql.properties
PrimeHR/src/main/resources/application-sqlserver.properties
PrimeHR/src/main/resources/db/migration/postgresql/V1__competency_foundation.sql
PrimeHR/src/main/resources/db/migration/sqlserver/V1__competency_foundation.sql
PrimeHR/src/test/java/com/primehr/PrimeHrApplicationSmokeTest.java
PrimeHR/src/test/java/com/primehr/competency/api/CompetencyQueryControllerTest.java
PrimeHR/src/test/java/com/primehr/competency/application/CompetencyQueryServiceImplTest.java
PrimeHR/src/test/java/com/primehr/competency/domain/CompetencyDomainConstraintTest.java
PrimeHR/src/test/java/com/primehr/competency/infrastructure/CompetencyRepositoryTest.java
PrimeHR/src/test/java/com/primehr/migration/CompetencyMigrationParityTest.java
PrimeHR/src/test/resources/application-test.properties
contracts/openapi/primehr-v1.yaml
docs/prime-hrm/PHASE_1A_COMPETENCY_FOUNDATION.md
```

## Files modified

```text
pom.xml                                  adds PrimeHR to the Maven reactor only
docs/prime-hrm/PRIME_HRM_PROGRESS.md     records the Phase 1A checkpoint
```

`HRISApp/pom.xml`, HRISApp component scanning/security, Dockerfile, existing domain modules, and all frontends are intentionally unchanged.

## Package structure

```text
com.primehr
├── config
├── security
├── shared
│   ├── api
│   ├── audit
│   └── exception
└── competency
    ├── api
    ├── application
    ├── domain
    └── infrastructure
```

This is one standalone modular service. No packages for later PrimeHR domains were created.

## Database objects

Both provider migrations create the same five logical tables:

| Table | Purpose | Relationships |
|---|---|---|
| `prime_competency_category` | agency-defined category dictionary | parent of competency |
| `prime_proficiency_scale` | agency-defined scale | parent of levels and competency reference |
| `prime_proficiency_level` | ordered, unlimited scale levels | many-to-one scale |
| `prime_competency` | competency dictionary | many-to-one category and scale |
| `prime_behavioral_indicator` | observable behavior by competency/level | many-to-one competency and level |

Every table has a stable application-generated UUID string ID, `agency_id`, active/effectivity fields, display order, optimistic `record_version`, and created/updated actor/timestamps. Unique constraints protect category, scale, and competency codes in agency scope; level code/order in scale scope; and indicator display order in competency/level scope. Uppercase code checks provide provider-neutral case-normalized uniqueness. No production seed data is included.

The domain additionally rejects cross-agency relationships and indicators whose level is not part of the competency's scale.

## API endpoints

All routes are under `/api/primehr/v1`, accept only GET, require a valid existing-style employee JWT, and return DTOs rather than entities.

| Method and path | Purpose | Filters |
|---|---|---|
| `GET /competency-categories` | ordered category list | server-resolved agency; optional `active`, `asOf` |
| `GET /competencies` | paginated dictionary | server-resolved agency; optional `categoryId`, `active`, `search`, `asOf`, `page`, `size` |
| `GET /competencies/{competencyId}` | competency, scale, levels, ordered indicators | server-resolved agency; optional `includeInactive`, `asOf` |
| `GET /proficiency-scales` | ordered scales and agency-defined levels | server-resolved agency; optional `active`, `asOf` |
| `GET /actuator/health` | public minimal service health | none |

`active=true` means enabled and effective on `asOf`; `active=false` selects disabled, future, or expired records; omission returns all. Page size is limited to 1–100. The stable contract is `contracts/openapi/primehr-v1.yaml`.

No POST, PUT, PATCH, or DELETE endpoint exists. Security denies all non-approved request paths and methods.

## Configuration and database ownership

Required environment:

```text
PRIMEHR_JWT_SECRET
PRIMEHR_DB_URL
PRIMEHR_DB_USERNAME
PRIMEHR_DB_PASSWORD
PRIMEHR_AGENCY_ID
```

Optional environment:

```text
PORT                              default 8086
PRIMEHR_CORS_ALLOWED_ORIGINS      comma-separated exact origins
PRIMEHR_CORS_ALLOWED_ORIGIN_PATTERNS
SPRING_PROFILES_ACTIVE            postgresql or sqlserver
PRIMEHR_COMPETENCY_READER_ROLES   comma-separated role/ruleset allowlist
PRIMEHR_DB_SCHEMA                 public or dbo by default
```

PrimeHR has a single standalone datasource selected by profile. It contains no URL or dependency for Administrative, HRM, Timekeeping, or Payroll. JPA uses `ddl-auto=validate`; Flyway owns schema creation.

## PostgreSQL and SQL Server strategy

- PostgreSQL migration: `db/migration/postgresql/V1__competency_foundation.sql`
- SQL Server migration: `db/migration/sqlserver/V1__competency_foundation.sql`
- profile-specific Flyway location prevents the wrong dialect script from executing;
- JPA mappings remain provider-neutral;
- PostgreSQL uses `BOOLEAN` and `TIMESTAMP WITH TIME ZONE`;
- SQL Server uses `BIT`, `DATETIMEOFFSET`, and Unicode `NVARCHAR` for descriptive text;
- Hibernate `@Nationalized` maps descriptive text appropriately for each dialect;
- IDs are generated in Java and stored as `VARCHAR(36)`, avoiding identity/sequence coupling.

Structural parity tests verify table/constraint presence and absence of seed/cross-domain references. They do not replace execution against real database engines.

### Real PostgreSQL verification command

Prerequisites: reachable empty PostgreSQL database, credentials permitted to create schema objects, Java 17, and the environment values below.

```powershell
$env:SPRING_PROFILES_ACTIVE='postgresql'
$env:PRIMEHR_DB_URL='jdbc:postgresql://HOST:5432/primehr'
$env:PRIMEHR_DB_USERNAME='...'
$env:PRIMEHR_DB_PASSWORD='...'
$env:PRIMEHR_JWT_SECRET='the-same-secret-used-by-ISOFT-auth'
.\mvnw.cmd -pl PrimeHR -am spring-boot:run
```

Verify Flyway V1 success, JPA schema validation, `/actuator/health`, and authenticated GET responses.

### Real SQL Server verification command

Prerequisites: reachable empty SQL Server database, schema-create credentials, Java 17, and a certificate/trust policy appropriate to the environment.

```powershell
$env:SPRING_PROFILES_ACTIVE='sqlserver'
$env:PRIMEHR_DB_URL='jdbc:sqlserver://HOST:1433;databaseName=primehr;encrypt=true;trustServerCertificate=false'
$env:PRIMEHR_DB_USERNAME='...'
$env:PRIMEHR_DB_PASSWORD='...'
$env:PRIMEHR_JWT_SECRET='the-same-secret-used-by-ISOFT-auth'
.\mvnw.cmd -pl PrimeHR -am spring-boot:run
```

Perform the same migration, validation, health, and authenticated-read checks.

## Security integration

The service validates the existing ISOFT HMAC JWT contract: subject is employee number and the `role` claim is retained as an authority. The secret is mandatory environment configuration; no fallback secret is embedded. Tokens and personal data are not logged.

Common's current filter/util implementation was not reused directly because it contains a hardcoded secret and verbose authentication logging. Changing it would alter every existing module, which Phase 1A forbids. The PrimeHR filter is registered only inside its security chain, not twice as a servlet filter.

Health is public and detail-free. Competency reads require the `PRIME_HR_COMPETENCY_ACCESS` authority. The current JWT has no agency claim and the ecosystem has no authoritative employee-to-agency relationship, so `ConfiguredSingleAgencyScopeResolver` supplies a required server-side agency and prevents request override. Role `1` and install administrator `admin` preserve established administrator behavior; configured reader roles receive the read authority. A live Administrative permission/agency adapter remains a Phase 1B prerequisite for writes.

## Tests executed

Final local gates:

```text
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

Both full nine-project reactor commands passed. Surefire XML records **119 tests, 0 failures, 0 errors, 0 skipped**; PrimeHR contributed **24 tests**. `Common`, `EmployeePortal`, and `HRISApp` executed zero tests, which is an existing coverage limitation. The explicitly selected real-provider test contributed six passing invocations on PostgreSQL and six on SQL Server and is intentionally not part of ordinary test discovery.

Covered:

- arbitrary agency-defined proficiency-level count;
- required fields, effectivity ordering, display ordering, cross-agency relations, scale-level consistency;
- case-normalized duplicate competency codes within agency and same code across agencies;
- repository agency/category/search/effectivity filtering;
- behavioral indicator level/display ordering;
- inactive competency and expired indicator handling;
- pagination bounds;
- authenticated and permitted JWT read, unauthenticated 401, unauthorized 403, trusted agency override prevention, administrator compatibility, public health, and denied POST/PUT/PATCH/DELETE;
- PostgreSQL/SQL Server migration object/constraint parity and no seed/cross-domain SQL;
- standalone application context and public health.

Fast repository tests use H2 in PostgreSQL compatibility mode. A separate H2 integration profile enables Flyway, executes the actual PostgreSQL V1 migration, uses Hibernate validation rather than schema creation, and runs repositories against that schema. This remains supplemental to the real-provider results below.

## Provider verification status

| Provider | Status | Evidence/limitation |
|---|---|---|
| PostgreSQL | Passed on Neon PostgreSQL 17.10 | Flyway V1, schema objects, constraints, indexes, Hibernate validation, and repository behavior passed in isolated schema `primehr_phase1a1_20260803_v3` |
| SQL Server | Passed on local SQL Server 2017 Express (14.0) | Flyway V1, schema objects, constraints including case-sensitive uppercase enforcement, indexes, Hibernate validation, and repository behavior passed in isolated schema `primehr_phase1a1_20260803_v3` |

## Unresolved issues and risks

1. Flyway 9.22.3 warns that PostgreSQL 17.10 is newer than its tested PostgreSQL 15 maximum; the real validation passed, but an upgrade review remains prudent.
2. Existing JWT contains no agency or granular PrimeHR permission claims. Phase 1A.1 safely uses required single-agency configuration and a read-role adapter; a dynamic multi-agency identity contract is still required before multi-agency deployment.
3. Administrative SSO target, PrimeHR permission UI/module key, and runtime URL registration are intentionally deferred; standalone API testing currently requires a compatible JWT and configured reader role.
4. The parent reactor has mixed historical Spring Boot dependency declarations. PrimeHR pins its child Boot property to 3.2.2 to match the existing build plugin/starter pattern; an ecosystem-wide dependency cleanup is outside scope.
5. Validation schemas were not destructively removed; see `PHASE_1A_1_HARDENING.md` before cleanup.

## Exact recommended Phase 1B scope

Phase 1B should add draft administration for the same five Competency concepts only:

- Administrative `primehr` module/access/add/edit/delete permission and SSO target integration;
- a PrimeHR permission adapter that validates action and agency scope server-side;
- POST/PUT/archive commands for draft categories, competencies, scales, levels, and indicators;
- optimistic-lock conflict responses and explicit draft/active/archive transition rules;
- active-version immutability and new-version creation instead of destructive editing;
- append-only audit records for every command;
- duplicate/order/effectivity validation at API and database boundaries;
- real PostgreSQL and SQL Server integration tests;
- management UI only if separately approved as part of Phase 1B.

Phase 1B must still exclude position profiles, assessments, gap analysis, RSP, SPMS, L&D, R&R, Evidence Center, applicants, workflows/committees, broker/outbox, object storage, analytics, and HRISApp assembly.

## Stop confirmation

Phase 1A stops here. No frontend, write endpoint, position profile, person assessment, gap analysis, later PrimeHR domain, event infrastructure, or HRISApp integration was implemented.
