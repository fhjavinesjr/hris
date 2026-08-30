# ISOFT PRIME-HRM Phase 5A.3 - UI, Vacancy Notice, and Browser Acceptance

Completed: 2026-08-29

Status: Complete. All approved Phase 5A.3 gates passed against the local SQL Server environment. Work is stopped before Phase 5B.

## Delivered scope

- Administrative permission rows and a versioned Qualification Standard page linked to authoritative Job Positions.
- PRIME-HRM Recruitment Planning navigation and a typed management page for plans, vacancy readiness, authority-to-fill decisions, publications, immutable source snapshots, lifecycle history, and conflicts.
- Official vacancy-notice PDF for APPROVED and PUBLISHED publications.
- Repeatable Playwright fixtures and acceptance coverage for denied access, submission, independent approval/authorization, publication, immutable snapshots, PDF download, and the full earlier-phase regression.
- Updated E2E operator documentation and PRIME-HRM user guide.

## Authorization

The UI consumes the exact Administrative permissions and the backend remains authoritative:

- `administrative.qualification-standard`: Access, Add, Edit, Delete/archive, Publish.
- `primehr.rsp-recruitment-planning`: Access, Add, Edit, Delete/cancel/archive, Submit, Approve.
- `primehr.rsp-vacancy-publication`: Access, Add, Edit, Delete/cancel/close, Submit, Approve, Publish.

An inaccessible Position Planning route renders an explicit denied page. Hiding navigation is supplementary; controller guards and service invariants enforce authorization, agency scope, lifecycle transitions, separation of duties, administrator override reasons, and optimistic conflicts.

## Portable report design

`vacancy_notice.jrxml` contains layout and field expressions only. The service maps the immutable publication response to typed parameters and `VacancyNoticeReportRow` beans and fills the report with `JRBeanCollectionDataSource`. It has no SQL query, datasource connection, vendor function, or machine-specific path. This makes the report independent of SQL Server/PostgreSQL selection.

The official endpoint is:

```http
GET /api/primehr/v1/rsp/vacancy-publications/{id}/notice.pdf
```

It requires publication Access and permits only APPROVED or PUBLISHED records. It returns `application/pdf` with a safe attachment filename.

## Defects found and corrected by acceptance

1. Spring request binding failed when the runtime lacked implicit Java parameter-name metadata. Request parameters now have explicit names and PrimeHR compiles with `-parameters`.
2. New form inputs lacked stable accessible labels. Exact `aria-label` values were added and are used by Playwright.
3. The first fixture could select a Plantilla already reserved by another non-final vacancy. Fixture selection now excludes reserved Plantilla records.
4. The fixture could attempt to create a duplicate Position Profile chain. It now reuses an existing chain or prepares an eligible source without violating uniqueness.
5. Strict/format-sensitive Playwright locators were corrected without weakening business assertions.

## Verification

| Command | Result |
|---|---|
| Administrative `npm run typecheck` | Passed |
| Administrative `npm run lint` | Passed with one pre-existing Sidebar hook-dependency warning and zero errors |
| Administrative `npm run build` | Passed |
| PRIME-HRM `npm run typecheck` | Passed |
| PRIME-HRM `npm run lint` | Passed, zero errors |
| PRIME-HRM production `npm run build` | Passed; `/prime-hr/recruitment-planning` packaged |
| `mvn -pl PrimeHR test` | Passed: 144 tests, zero failures/errors/skips |
| `mvn -pl PrimeHR package -DskipTests` | Passed on the same final source after the 144-test run; this packaging invocation deliberately skipped the redundant test execution |
| Earlier affected `mvn -pl Administrative,HumanResource,PrimeHR -am clean package` | Passed: 234 tests, zero failures/errors/skips |
| `npx playwright test e2e/phase5a.spec.ts` | Passed: 5/5 on local SQL Server |
| `npx playwright test` | Passed: 20/20 on local SQL Server in 4.6 minutes, zero skipped |

The report test compiles the JRXML and generates representative and 70-row multi-page PDF output. Playwright also downloads the real PDF and validates its signature and non-empty content. Live PostgreSQL was not run by user direction; dual migrations, PostgreSQL-mode validation, parity checks, provider-neutral Java/JPA, and SQL-free Jasper are retained as portability evidence.

## Phase boundary

No public careers page, applicant account, consent, PDS/application intake, document upload, screening, examination, ranking, selection, appointment handoff, or onboarding behavior was added. Phase 5B requires an exact scope and separate explicit approval.
