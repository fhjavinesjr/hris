# ISOFT PRIME-HRM Phase 1A.1 - Foundation Hardening

Status: Complete for independent review
Date: 2026-08-03
Boundary: trusted read scope, read authorization, migration fidelity, and real-provider validation only

## Outcome

Phase 1A.1 keeps PrimeHR standalone, read-only, outside HRISApp, and owner of its own database. Public clients can no longer select `agencyId`. The server resolves one required configured agency and applies it to every competency query. Competency reads require the stable `PRIME_HR_COMPETENCY_ACCESS` authority. Health remains public and every write method remains denied.

No Phase 1B CRUD API, Administrative permission UI, frontend page, HRISApp assembly, or later-domain behavior was added.

## Existing authentication and authorization inspected

The following repository files were inspected:

- `Common/src/main/java/com/hris/common/utilities/JwtUtil.java`
- `Common/src/main/java/com/hris/common/utilities/JwtFilter.java`
- `HumanResource/src/main/java/com/humanresource/entitymodels/Employee.java`
- `HumanResource/src/main/java/com/humanresource/impl/EmployeeServiceImpl.java`
- `HumanResource/src/main/java/com/humanresource/configs/HumanResourceSecurityConfig.java`
- `Administrative/src/main/java/com/administrative/entitymodels/PermissionRuleset.java`
- `Administrative/src/main/java/com/administrative/dtos/PermissionRulesetDTO.java`
- `Administrative/src/main/java/com/administrative/repositories/PermissionRulesetRepository.java`
- `Administrative/src/main/java/com/administrative/services/PermissionRulesetService.java`
- `Administrative/src/main/java/com/administrative/impl/PermissionRulesetImpl.java`
- `Administrative/src/main/java/com/administrative/controllers/PermissionRulesetController.java`
- `Administrative/src/main/java/com/administrative/configs/AdministrativeSecurityConfig.java`
- `Administrative/src/main/java/com/administrative/impl/SsoServiceImpl.java`
- `Administrative/src/main/java/com/administrative/controllers/SsoController.java`
- `Administrative/src/main/java/com/administrative/sso/SsoTarget.java`
- `administrative-software/src/app/administrative/permission/Permission.tsx`
- `administrative-software/src/lib/utils/authConfig.ts`
- `administrative-software/src/lib/utils/localStorageUtil.ts`
- `administrative-software/src/lib/utils/ssoBootstrap.ts`
- `employee-portal-UI/src/lib/utils/authConfig.ts`
- `employee-portal-UI/src/lib/utils/localStorageUtil.ts`
- `employee-portal-UI/src/components/sidebar/Sidebar.tsx`

Verified current contract:

- JWT subject is employee number; the only authorization claim created is `role`.
- No agency, tenant, or granular permission claim exists in the JWT.
- `Employee.userRole` is the persisted role/ruleset reference. The established administrator role string is `"1"`; the install administrator employee number is `admin`.
- Administrative `PermissionRuleset` stores `permissionName`, `isAdministrator`, `portalModuleAccess`, and JSON CRUD flags (`canAccess`, `canAdd`, `canEdit`, `canDelete`) by module key.
- SSO resolves the token role to the Administrative ruleset and returns permission data to the frontend. These frontend values are not sufficient backend authorization.
- Observed browser storage includes `authToken`, `employees`, `employeeNo`, `employeeFullname`, `userRole`, `employeeId`, `biometricNo`, `systemConfig`, `permissionName`, `isAdministrator`, `permissionData`, and portal module access.
- No authoritative employee-to-agency relationship was found. `agencyEmpNo` in PDS data is not tenant identity.
- The shared JWT implementation contains a hardcoded secret and diagnostic authentication logging, so PrimeHR continues to validate the compatible token contract independently with a required environment secret.

## Trusted agency contract

`AgencyScopeResolver` is the extension point. Phase 1A.1 supplies `ConfiguredSingleAgencyScopeResolver` because the current ecosystem has no trustworthy user-to-agency source.

- `PRIMEHR_AGENCY_ID` is required at startup.
- The resolver requires an authenticated security context.
- Controllers obtain agency scope from the resolver and do not declare an `agencyId` request parameter.
- An extra client-supplied `agencyId` is ignored and cannot replace the trusted value.
- Existing service and repository agency predicates remain the internal defense-in-depth boundary.

A future `JwtAgencyScopeResolver` or employee-directory resolver can replace this implementation without changing the competency service API. No unverified JWT claim was invented.

## Read permission contract

`CompetencyReadPermissionResolver` maps the verified ISOFT role contract to the stable authority `PRIME_HR_COMPETENCY_ACCESS`.

- configured reader role/ruleset IDs or names come from `PRIMEHR_COMPETENCY_READER_ROLES`;
- role `1` and the install administrator identity retain established unrestricted administrator behavior;
- unauthenticated reads return 401;
- authenticated identities without the resolved authority return 403;
- authorized competency readers may use the four GET endpoints;
- all POST, PUT, PATCH, and DELETE requests remain denied.

This is intentionally a small adapter, not a replacement for Administrative RBAC. Phase 1B must add an Administrative PrimeHR module key and a live server-to-server permission/scope contract before write authorization is introduced.

## API and configuration changes

The four `/api/primehr/v1` competency GET operations no longer accept `agencyId`. Their other filters and response DTOs are unchanged. The OpenAPI contract documents 401/403 responses and server-resolved scope.

Required runtime values now include:

```text
PRIMEHR_JWT_SECRET
PRIMEHR_AGENCY_ID
PRIMEHR_DB_URL
PRIMEHR_DB_USERNAME
PRIMEHR_DB_PASSWORD
SPRING_PROFILES_ACTIVE=postgresql|sqlserver
```

`PRIMEHR_COMPETENCY_READER_ROLES` is a comma-separated allowlist for non-administrator read roles. `PRIMEHR_DB_SCHEMA` selects the isolated schema (`public`/`dbo` by default). Flyway, its placeholder, and Hibernate use the same schema value.

## Migration fidelity

Fast H2 tests are retained. A Flyway-enabled H2 PostgreSQL-mode profile now runs the actual PostgreSQL V1 migration with Hibernate `ddl-auto=validate`; Hibernate does not create that schema. Repository operations then execute against the migrated tables.

`PrimeHrRealDatabaseIT` is intentionally excluded from ordinary test discovery by its `IT` suffix and is run explicitly. Its six invocations verify:

- Flyway history is at V1;
- all five tables, expected indexes, and foreign keys exist;
- Hibernate startup validation succeeds;
- agency and effectivity filtering and indicator order;
- same-agency duplicate rejection and cross-agency duplicate allowance;
- lowercase code, negative display order, and reversed effectivity check constraints.

The provider migrations schema-qualify every table, foreign-key reference, and index. SQL Server uppercase checks use binary collation because the ordinary `code = UPPER(code)` expression does not reject lowercase values under a case-insensitive database collation.

## Real PostgreSQL result

- Engine: Neon PostgreSQL 17.10.
- Connection: existing approved development datasource configuration; credentials were loaded locally and were not printed or written to documentation.
- Isolated final schema: `primehr_phase1a1_20260803_v3`.
- Command shape:

```powershell
$env:SPRING_PROFILES_ACTIVE='postgresql'
$env:PRIMEHR_DB_SCHEMA='primehr_phase1a1_20260803_v3'
$env:PRIMEHR_AGENCY_ID='VALIDATION-AGENCY'
$env:PRIMEHR_COMPETENCY_READER_ROLES='COMPETENCY_READER'
# SPRING_DATASOURCE_* supplied from the approved local configuration
.\mvnw.cmd -pl PrimeHR -Dtest=PrimeHrRealDatabaseIT test
```

Result: **PASS - 6 tests, 0 failures, 0 errors, 0 skipped**. Flyway created the isolated schema and schema-history table, applied V1, reported current version 1, and Hibernate validation and repository tests passed.

Limitation: Flyway 9.22.3 warns that PostgreSQL 17.10 is newer than its tested maximum PostgreSQL 15. The migration passed, but upgrading Flyway should be evaluated separately.

## Real SQL Server result

- Engine: local Microsoft SQL Server 2017 Express, version 14.0.
- Connection: repository-configured local SQL Server datasource on port 1433 with its existing development certificate policy; credentials were not printed or documented.
- Isolated final schema: `primehr_phase1a1_20260803_v3`.
- Command shape:

```powershell
$env:SPRING_PROFILES_ACTIVE='sqlserver'
$env:PRIMEHR_DB_SCHEMA='primehr_phase1a1_20260803_v3'
$env:PRIMEHR_AGENCY_ID='VALIDATION-AGENCY'
$env:PRIMEHR_COMPETENCY_READER_ROLES='COMPETENCY_READER'
# SPRING_DATASOURCE_* supplied from the approved local configuration
.\mvnw.cmd -pl PrimeHR -Dtest=PrimeHrRealDatabaseIT test
```

Result: **PASS - 6 tests, 0 failures, 0 errors, 0 skipped**. Flyway created the isolated schema and history table, applied V1, reported current version 1, and Hibernate validation and repository tests passed.

Two real defects were found and fixed before the passing run: Flyway needed its SQL Server database-support artifact, and all migration objects needed explicit schema qualification. A further run exposed case-insensitive-collation behavior in uppercase checks; binary comparison now enforces the intended constraint and the regression test passes.

## Test artifacts and cleanup boundary

Validation created isolated schemas named `primehr_phase1a1_20260803`, `_v2`, and `_v3` where the corresponding attempt reached migration. They were deliberately not dropped because schema deletion is destructive. An early SQL Server run before schema qualification may also have created the five `prime_*` tables in `dbo`; review these objects before any cleanup. No cleanup command is embedded in application code.

## Failed attempts disclosed

- An initial Failsafe configuration could not load the repackaged test class; it was removed and the real-provider test is now explicitly selected through Surefire.
- The first PostgreSQL run found unqualified raw verification SQL in the test; it was changed to the validated configured schema.
- SQL Server initially failed due to missing Flyway SQL Server support, then due to unqualified DDL, then demonstrated that a case-insensitive collation defeated the uppercase constraint.
- One SQL Server rerun used the default profile datasource without the repository's local trust configuration and failed TLS certificate validation. Another selected a PostgreSQL datasource with the SQL Server driver while locating the correct local configuration. Neither is represented as a provider or migration failure.

## Repository hygiene and exclusions

The `.idea/compiler.xml` and `.idea/encodings.xml` content changes are reverted. Existing user changes in the PrimeHR work remain preserved. No frontend, Jasper report, legacy module behavior, or runtime integration was modified. Phase 1B remains not started.

