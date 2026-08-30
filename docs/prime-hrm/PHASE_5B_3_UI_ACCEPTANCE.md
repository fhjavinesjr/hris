# ISOFT PRIME-HRM Phase 5B.3 UI Acceptance

Prepared: 2026-08-30

Status: Passed against local SQL Server. Phase 5B is complete and work stopped before Phase 5C.

## Delivered surfaces

- Administrative **Applicant Intake** permission row with independent Access/Add and agency-wide data scope.
- Separate `/careers` public/applicant layout and applicant-only session keys.
- Public vacancy list/detail and applicant registration/login.
- Applicant profile, exact privacy consent, private document upload/download/replace/deactivate, application draft/document selection/submit, acknowledgment, history, communication, and withdrawal.
- Staff `/prime-hr/applicant-intake` list/detail, immutable evidence download, communication history, and informational messaging.
- No staff screening, completeness, qualified/disqualified, score, rank, shortlist, selection, employee creation, appointment, or onboarding action.

## Automated browser matrix

The focused matrix ran serially against local Administrative, HumanResource, PrimeHR, PrimeHR UI, and real SQL Server services:

| Test | Result |
|---|---|
| Public Careers registration, exact effective consent, isolated applicant storage, applicant/employee token cross-rejection | Passed |
| Profile entry/declaration save, invalid content-signature rejection, valid private document upload | Passed |
| Application draft/selection/submit/acknowledgment, duplicate rejection, immutable evidence after replacement, cross-applicant non-disclosure | Passed |
| Denied staff page/API, allowed staff read/evidence/message, absence of Phase 5C decision controls | Passed |
| Applicant communication read and withdrawal while evidence remains unchanged | Passed |

```text
npx playwright test e2e/phase5b.spec.ts --reporter=line
5 passed (1.1m)

npx playwright test --reporter=line
25 passed (2.6m)
```

No Playwright test was skipped. The full suite includes all Phase 2, 3, 4, 5A, and 5B browser tests.

## Build and backend gates

```text
Administrative UI
npm run typecheck                 PASS
npm run lint                      PASS with one pre-existing Sidebar hook warning
npm exec next build               PASS; 41 app routes generated

PrimeHR UI
npm run typecheck                 PASS
npm run lint                      PASS
npm exec next build               PASS; 19 routes generated

Administrative backend
mvn -pl Administrative test       40 tests, 0 failures/errors/skips; BUILD SUCCESS

PrimeHR backend
mvn -pl PrimeHR -am clean verify  164 tests, 0 failures/errors/skips; BUILD SUCCESS; JAR packaged
```

`npm run build` in PrimeHR includes an optional environment packager that requires `NEXT_PUBLIC_API_BASE_URL_HRM` in the user's local `.env`. The actual production framework gate, `npm exec next build`, passed. No user `.env` was changed to conceal this local packaging prerequisite.

## Defects found and corrected

1. A same-clock-tick draft save could leave `recordVersion` unchanged on Windows. Applicant draft update time is now strictly monotonic, so JPA dirty checking and optimistic versioning are deterministic.
2. The Playwright API helper forced JSON content type on multipart document replacement. The default header was removed so Playwright generates the proper multipart boundary.
3. Applicant error parsing now displays the first backend validation detail when present, making upload failures actionable.
4. Careers initial client state no longer reads local storage during server render, eliminating hydration mismatch warnings.

An initial local upload failure was sandbox filesystem denial for a temporary test-storage directory, not an application defect. The rerun used an authorized temporary location.

## Provider and boundary evidence

- Browser acceptance used real local SQL Server.
- Live PostgreSQL was not run by user direction and is not represented as tested.
- PostgreSQL V13/V14 migrations, PostgreSQL-mode Flyway/Hibernate validation, migration parity, provider-neutral Spring Data/JPA business logic, and storage abstraction remain the portability evidence.
- The only SQL Server-specific statement in Phase 5B.3 is in Playwright fixture support to ensure an active privacy notice in the explicitly local SQL Server QA database. It is not production application code.
- No Phase 5C or later mutation, route, decision status, report, or UI control was implemented.

## Operational limitations

Before production use, the agency must provide approved privacy/retention text, durable local or private S3-compatible storage, dedicated secrets, exact CORS origins, file policy, backups, and applicant support procedures. Email verification/password reset delivery, CAPTCHA, malware scanning provider, and multi-agency public routing remain explicit hardening work.
