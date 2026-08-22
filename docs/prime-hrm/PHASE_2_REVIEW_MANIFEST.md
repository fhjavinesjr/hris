# ISOFT PRIME-HRM Phase 2 Review Manifest

Status: Backend and UI implementation complete; Phase 2.3 browser acceptance pending

Prepared: 2026-08-13

## 1. Approved scope mapping

This manifest covers only Master Plan V2 Phase 2 and the approved scope in `PHASE_2_POSITION_COMPETENCY_PROFILES_SCOPE_APPROVAL.md`:

- Phase 2.1: authoritative Administrative position targets and PrimeHR draft/version requirement foundation;
- Phase 2.2: Submit/Approve lifecycle, separation of duties, snapshots, resolution, comparison, and audit;
- Phase 2.3: Administrative permission controls and standalone PrimeHR management UI.

No person profile, assessment, gap analysis, RSP, SPMS, L&D, R&R, report, notification, HRISApp assembly, or direct cross-domain database access is included.

## 2. Repository changes

### Backend `hris` - modified

- `Administrative/src/main/java/com/administrative/dtos/EffectiveFeaturePermissionResponse.java`
- `Administrative/src/main/java/com/administrative/impl/EffectiveAuthorizationServiceImpl.java`
- `Administrative/src/main/java/com/administrative/repositories/JobPositionRepository.java`
- `Administrative/src/main/java/com/administrative/repositories/PlantillaRepository.java`
- `Administrative/src/test/java/com/administrative/impl/EffectiveAuthorizationServiceImplTest.java`
- `PrimeHR/src/main/java/com/primehr/integration/administrative/AdministrativeAuthorizationClient.java`
- `PrimeHR/src/main/java/com/primehr/integration/administrative/EffectiveFeaturePermission.java`
- `PrimeHR/src/main/java/com/primehr/security/PrimeHrAction.java`
- `PrimeHR/src/main/java/com/primehr/security/PrimeHrPermissionGuard.java`
- `PrimeHR/src/main/java/com/primehr/shared/exception/PrimeHrExceptionHandler.java`
- `PrimeHR/src/test/java/com/primehr/migration/AbstractPrimeHrProviderIntegration.java`
- `PrimeHR/src/test/java/com/primehr/migration/CompetencyMigrationParityTest.java`
- `PrimeHR/src/test/java/com/primehr/security/PrimeHrPermissionGuardTest.java`
- `contracts/openapi/primehr-v1.yaml`
- `docs/prime-hrm/PHASE_2_POSITION_COMPETENCY_PROFILES_SCOPE_APPROVAL.md`
- `docs/prime-hrm/PRIME_HRM_PROGRESS.md`

### Backend `hris` - created

Administrative integration:

- `Administrative/src/main/java/com/administrative/controllers/PositionTargetIntegrationController.java`
- `Administrative/src/main/java/com/administrative/dtos/PositionTargetPageResponse.java`
- `Administrative/src/main/java/com/administrative/dtos/PositionTargetResponse.java`
- `Administrative/src/main/java/com/administrative/dtos/PositionTargetType.java`
- `Administrative/src/main/java/com/administrative/impl/PositionTargetServiceImpl.java`
- `Administrative/src/main/java/com/administrative/services/PositionTargetService.java`
- `Administrative/src/test/java/com/administrative/controllers/PositionTargetIntegrationControllerTest.java`
- `Administrative/src/test/java/com/administrative/impl/PositionTargetServiceImplTest.java`

PrimeHR integration/security:

- `PrimeHR/src/main/java/com/primehr/integration/administrative/AdministrativePositionTarget.java`
- `PrimeHR/src/main/java/com/primehr/integration/administrative/AdministrativePositionTargetClient.java`
- `PrimeHR/src/main/java/com/primehr/integration/administrative/PositionTargetDependencyException.java`
- `PrimeHR/src/main/java/com/primehr/security/PositionProfilePermissionGuard.java`

PrimeHR position-profile API/application/domain/infrastructure:

- `PrimeHR/src/main/java/com/primehr/positionprofile/api/ApprovePositionProfileRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/CreatePositionProfileRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/CreatePositionRequirementRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileAdminController.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileComparisonChange.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileComparisonItemResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileComparisonResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileResolutionResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileSummaryResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionProfileTransitionRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionRequirementResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionRequirementTransitionRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/PositionTargetSnapshotResponse.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/SubmitPositionProfileRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/UpdatePositionProfileRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/api/UpdatePositionRequirementRequest.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/application/PositionProfileAdminService.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/application/PositionProfileAdminServiceImpl.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/domain/PositionProfile.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/domain/PositionProfileRequirement.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/domain/PositionProfileStatus.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/domain/PositionTargetSnapshot.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/domain/PositionTargetType.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/domain/RequirementClassification.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/infrastructure/PositionProfileRepository.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/infrastructure/PositionProfileRequirementRepository.java`
- `PrimeHR/src/main/java/com/primehr/positionprofile/infrastructure/PositionProfileSpecifications.java`

Migrations, tests, contract, and phase records:

- `PrimeHR/src/main/resources/db/migration/postgresql/V4__position_competency_profiles.sql`
- `PrimeHR/src/main/resources/db/migration/postgresql/V5__position_profile_approval_lifecycle.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V4__position_competency_profiles.sql`
- `PrimeHR/src/main/resources/db/migration/sqlserver/V5__position_profile_approval_lifecycle.sql`
- `PrimeHR/src/test/java/com/primehr/contract/PrimeHrOpenApiContractTest.java`
- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV3ToV4UpgradeIT.java`
- `PrimeHR/src/test/java/com/primehr/migration/PrimeHrV4ToV5UpgradeIT.java`
- `PrimeHR/src/test/java/com/primehr/positionprofile/api/PositionProfileAdminControllerTest.java`
- `PrimeHR/src/test/java/com/primehr/positionprofile/application/PositionProfileAdminServiceIntegrationTest.java`
- `PrimeHR/src/test/java/com/primehr/positionprofile/domain/PositionProfileDomainTest.java`
- `PrimeHR/src/test/java/com/primehr/security/PositionProfilePermissionGuardTest.java`
- `contracts/openapi/administrative-primehr-integration-v1.yaml`
- `docs/prime-hrm/PHASE_2_1_POSITION_PROFILE_DRAFT_FOUNDATION.md`
- `docs/prime-hrm/PHASE_2_2_POSITION_PROFILE_APPROVAL.md`
- `docs/prime-hrm/PHASE_2_3_POSITION_PROFILE_UI.md`
- `docs/prime-hrm/PHASE_2_REVIEW_MANIFEST.md`

### Administrative frontend

- Modified `administrative-software/src/app/administrative/permission/Permission.tsx`.
- The repository's separate `.env` modification predates/is unrelated to Phase 2.3 and must be reviewed or excluded independently.

### PrimeHR frontend

- Modified `prime-hr-software/src/app/prime-hr/competencies/CompetencyManager.tsx`.
- Modified `prime-hr-software/src/lib/auth.ts`.
- Created `prime-hr-software/src/lib/positionProfiles.ts`.
- Created `prime-hr-software/src/app/prime-hr/position-profiles/page.tsx`.
- Created `prime-hr-software/src/app/prime-hr/position-profiles/PositionProfileManager.tsx`.
- Created `prime-hr-software/src/app/prime-hr/position-profiles/PositionProfileManager.module.scss`.

## 3. Persistence, relationships, and portability

V4 creates `prime_position_profile` and `prime_position_profile_requirement`. Requirements reference the exact PrimeHR competency definition and exact required proficiency level. The root retains authoritative Job Position/Plantilla IDs and immutable snapshots without copying the Administrative position master. V5 adds submission/approval metadata, lifecycle consistency, and effective-resolution indexing.

Equivalent reviewed PostgreSQL and SQL Server migrations exist. Shared application persistence uses JPA/JPQL/Specifications/Pageable and no provider-specific shared query. Live SQL Server fresh and populated upgrades passed. Live PostgreSQL execution was not run by user direction; parity and PostgreSQL-mode harnesses passed, but live PostgreSQL remains explicitly unverified for Phase 2.

## 4. REST and authorization

Administrative authenticated read-only integration:

- `GET /api/integration/v1/primehr/position-targets`
- `GET /api/integration/v1/primehr/position-targets/{type}/{id}`

PrimeHR profile administration under `/api/primehr/v1/admin/position-profiles`:

- paginated list/details;
- create/update/archive draft roots;
- add/update/archive requirements;
- submit, return, approve, and successor creation;
- audit history;
- effective resolution;
- exact-version comparison.

Every action requires Access. CRUD, Submit, and Approve are independently enforced server-side from Administrative permissions. Ordinary self-approval is denied; administrator self-approval requires an explicit audited reason. Missing legacy Submit/Approve flags fail closed.

## 5. Verification evidence

Backend evidence is detailed in the Phase 2.1 and Phase 2.2 records:

```text
.\mvnw.cmd -pl Administrative,PrimeHR -am test -DskipTests=false
BUILD SUCCESS
Administrative: 26 tests; PrimeHR: 73 tests; 0 failures/errors/skips
Common: 0 tests (pre-existing coverage gap)

.\mvnw.cmd -pl Administrative,PrimeHR -am package -DskipTests=false
BUILD SUCCESS

SQL Server fresh V1-V5: PASS, 9 tests
SQL Server populated V4-to-V5: PASS, 1 test
PostgreSQL live Phase 2 run: NOT RUN by user direction
```

Phase 2.3 frontend:

```text
prime-hr-software: npm run typecheck - PASS
prime-hr-software: npm run lint - PASS
prime-hr-software: npm run build - PASS
administrative-software: npx eslint src/app/administrative/permission/Permission.tsx - PASS
administrative-software: npm run build - PASS
```

Administrative `npm run lint` is a pre-existing invalid Next.js 16 `next lint` script and did not run. Neither frontend has a UI test script. No frontend tests were skipped because none are configured; automated UI behavior remains unverified beyond lint/type/build.

## 6. Audits and unresolved items

- Frontend `git diff --check`: pass, with line-ending conversion warnings only.
- Phase 2.3 credential and explicit-`any` scans: pass.
- Generated/IDE artifact scan: pass; only intended source/docs are untracked.
- Phase-boundary scan: pass; no Phase 3+ functionality found.
- Backend Phase 2.1/2.2 secret/generated/boundary audits are recorded in their detail documents.
- The exact Phase 2.3 browser matrix in `PHASE_2_3_POSITION_PROFILE_UI.md` is pending user execution.
- No frontend was committed, pushed, or deployed by Codex.
- No database-provider behavior was added by Phase 2.3; it is a typed REST UI over the provider-neutral backend.

## 7. Review disposition

The Phase 2 implementation is ready for independent review and manual browser acceptance. Do not declare Phase 2 fully accepted or start Phase 3 implementation until the applicable Phase 2.3 manual matrix is confirmed and the user explicitly approves a separately prepared Phase 3 scope.
