# ISOFT PRIME-HRM Phase 3.4 - UI and Playwright Acceptance

Status: Implemented and accepted by automated local SQL Server gates on 2026-08-28

Boundary: Phase 3.4 only. Phase 4 competency gap analysis, L&D referrals, notifications, reports, applicant assessment, and other later-domain behavior were not implemented.

## Delivered behavior

### Administrative permission controls

The permission editor now persists and displays four Phase 3 feature keys:

- `primehr.assessment-administration`
- `primehr.competency-assessment`
- `primehr.assessment-validation`
- `primehr.person-profile`

It supports independent Assess, Validate, and Finalize actions plus `NONE`, `OWN_RECORDS`, `ASSIGNED_RECORDS`, and `AGENCY_WIDE` data scopes. Missing legacy JSON fields fail closed. Disabling Access clears actions and scope; selecting an action/scope enables Access. Administrator rulesets retain full supported actions and agency-wide scope.

### PRIME-HRM UI

The standalone SSO application adds:

- `/prime-hr/assessment-administration`
- `/prime-hr/assessments`
- `/prime-hr/assessment-validation`
- `/prime-hr/person-profiles`

The pages use strict TypeScript, the existing runtime configuration and token, shared SCSS modules, SweetAlert2 confirmation/errors, explicit empty/denied/loading states, responsive tables/forms, and backend-enforced authorization. Form controls used by the acceptance matrix have programmatic labels.

Assessment Administration manages draft cycles/tools, exact Position Profile selection, HRM subject lookup/snapshot checking, explicit assessor assignment, publication, opening, and closure. My Assessments exposes only own/explicit assignments and supports ratings, structured evidence, optimistic conflicts, and submission. Assessment Validation compares contributors without averaging and records explicit human decisions, returns, and audited administrator override. Person Profiles resolves own or agency-wide latest-as-of results and immutable history.

### Runtime/deployment configuration

`NEXT_PUBLIC_API_BASE_URL_HRM` / `api.url.hrm` is included in runtime configuration and production package validation. No service URL, datasource provider, or credential was embedded in application logic.

## Defects found and corrected by browser acceptance

1. Non-admin assessment and validation `POST`/`PUT` routes fell through Spring Security's `denyAll`. Narrow authenticated matchers were added only for `/assessments/**` mutations and `/validation/**` POST actions; feature/action/data-scope guards remain authoritative.
2. Assessment, validation, and Person Profile captions were visually present but not associated with their controls. Stable `id`/`htmlFor` pairs were added.
3. Person Profiles reinitialized Employee No. whenever the controlled value changed, preventing agency-wide lookup. Initialization is now one-time and preserves the user's query.
4. The Phase 3 fixture initially attempted to recreate a browser-created rating and reused an overlapping profile date. It now consumes authoritative contribution state and chooses the day after the latest immutable profile effectivity, preserving repeatability without database deletion.
5. Phase 3 controller path/request parameter names are explicit, avoiding runtime reflection dependence when packaged without parameter metadata.

## Playwright design and data safety

`e2e/phase3.spec.ts` uses the existing ignored `.env.e2e.local` credentials and controlled local SQL Server records. API services run on isolated ports 18082, 18085, and 18086 while the already-running UI at 3086 can be reused. Browser runtime configuration is intercepted only inside the test context.

The suite:

- never drops, truncates, resets, or deletes business data;
- never commits credentials or tokens;
- temporarily grants Phase 3 permissions and restores the exact original ruleset JSON in `afterAll`;
- resolves real eligible HRM/current-appointment and effective Position Profile records;
- creates uniquely named cycles/tools/cases;
- advances immutable Person Profile effectivity rather than overwriting history.

## Acceptance results

Environment: local Windows, local ISOFT HRIS services, real local SQL Server.

```text
npx playwright test e2e/phase3.spec.ts
3 passed (1.4m)

npx playwright test
11 passed (2.0m)

prime-hr-software: npm run lint
PASS, zero warnings/errors

prime-hr-software: npm run typecheck
PASS

prime-hr-software: npm run build
PASS; all Phase 3 routes generated and production package created

administrative-software: npm run typecheck
PASS

administrative-software: npm run lint
PASS with one pre-existing Sidebar exhaustive-deps warning outside the Phase 3 change

administrative-software: npm run build
PASS after rerunning outside the restricted process sandbox; production package created

mvn -pl Administrative,HumanResource,PrimeHR -am clean test
PASS: Common 3, Administrative 33, HumanResource 45, PrimeHR 90;
171 total, zero failures, zero errors, zero skipped
```

The first restricted Administrative build attempt compiled but Windows returned `spawn EPERM`; the elevated rerun passed. An intermediate PrimeHR JAR rebuild used `-DskipTests` only to make the corrected runtime available to Playwright; the subsequent clean test gate executed all 171 tests with zero skips.

## Provider evidence

Phase 3.4 browser acceptance used real SQL Server. No live PostgreSQL browser run was performed under the approved SQL Server-primary policy. Shared UI/API logic contains no provider SQL. Phase 3.1-3.3 already supply equivalent PostgreSQL V6-V8 migrations, PostgreSQL-mode Flyway/Hibernate checks, structural parity tests, and provider-neutral JPA/service logic. Live PostgreSQL remains explicitly unverified and non-blocking by user direction.

## Operational notes

- Configure the HRM, Administrative, and PrimeHR API URLs plus CORS origins for each deployment.
- Reauthenticate through Employee Portal after changing a permission ruleset.
- Do not commit `.env.e2e.local`, `test-results/`, `playwright-report/`, `dist/`, `.next/`, or real credentials.
- See `prime-hr-software/docs/PRIME_HRM_E2E_TESTING.md` for the repeatable runbook.
- See `docs/prime-hrm/PRIME_HRM_USER_GUIDE.md` for end-user operation.

## Phase boundary

Phase 3 is functionally complete once the final clean package and repository audits recorded in the Phase 3 review manifest pass. Phase 4 requires a separate exact scope and explicit user approval.
