# Phase 4.3 - Administrative Controls, PrimeHR UI, Gap PDF, and Acceptance

Status: Complete on 2026-08-29. Work stopped before Phase 5.

## Delivered behavior

- Administrative exposes separate permission rows for development-priority configuration, competency-gap analysis, and manual L&D referrals. Existing action columns and data-scope values are reused.
- PrimeHR exposes `/prime-hr/competency-gaps` with permission-aware Gap Analyses, Priority Configuration, and L&D Referrals panels.
- Gap generation uses the authoritative HRM current-appointment fingerprint and displays immutable Position Profile, Person Profile, and priority-scheme versions.
- The UI explains `required order - attained order`, preserves `NOT_ASSESSED` reasons, and never represents missing evidence as zero.
- Manual referrals select only `BELOW` or `NOT_ASSESSED` items. `REFERRED` is explicitly described as intake for later review, not an approved IDP, training request, funding decision, enrollment, or completion.
- `GET /api/primehr/v1/competency-gaps/{analysisId}/report.pdf` produces an authorized bean-driven PDF.

## Jasper architecture

`competency_gap_report.jrxml` contains no SQL or database-provider expression. `CompetencyGapReportServiceImpl` maps the immutable analysis DTO to standard JavaBean rows and fills the report with `JRBeanCollectionDataSource`.

The report includes employee and position snapshots, analysis date, exact source versions, the formula legend, required and attained levels, classification, missing-evidence reason, priority and explanation, generator, repeated headers, page numbers, and the no-approved-IDP disclaimer. Null attained levels and multi-page data are covered by automated PDF tests.

## Defects found and corrected during acceptance

1. Phase 4 GET requests were initially captured by the older global competency-catalog read-authority matcher. The security configuration now authenticates competency-gap and referral GET routes before their controller-level Phase 4 action/data-scope guards run. Catalog reads retain their existing authority.
2. The Playwright fixture used a UTC date while the business/runtime timezone was Asia/Manila. Phase 4 fixture effectivity now uses an explicit Manila date.
3. Existing validated employees could legitimately have no actionable gap. The suite now creates a controlled published competency and audited Position Profile successor through supported APIs when required, producing a real `NOT_ASSESSED` result without direct database edits.
4. Reruns cannot claim the same immutable gap item in another active referral. The suite safely reuses an existing DRAFT/REFERRED fixture and verifies/submits it as appropriate; clean databases still exercise referral creation.

## Verification evidence

SQL Server was the blocking live provider under the approved policy.

```text
mvn -pl Administrative,PrimeHR -am clean package
Common: 3 tests
Administrative: 34 tests
PrimeHR: 118 tests
Total: 155 tests, 0 failures, 0 errors, 0 skipped
BUILD SUCCESS

npx playwright test e2e/phase4.spec.ts
4 passed, 0 skipped

npm run e2e
15 passed, 0 skipped

Administrative: npm run lint; npm run build
PASS (one pre-existing Sidebar hook warning during lint)

PrimeHR UI: npx tsc --noEmit --incremental false; npm run lint; npm run build
PASS
```

`CompetencyGapReportServiceTest` generated a representative PDF containing calculated and not-assessed rows and a 70-row multi-page PDF with repeated headers. The Playwright suite also requested the live SQL Server-backed report endpoint and verified HTTP success, PDF content type, and `%PDF` bytes.

Equivalent PostgreSQL V9/V10 migrations, migration parity, PostgreSQL-mode Flyway/Hibernate validation, provider-neutral JPA/service logic, and SQL-free Jasper design remain in place. A live PostgreSQL instance was not run by user direction and is not claimed.

## Boundary

No Phase 5 vacancy, recruitment, applicant, screening, HRMPSB, appointment-handoff, or onboarding behavior was implemented. Phase 4 also does not create approved IDPs, training programs, enrollments, budgets, notifications, payroll rows, or cross-domain database writes.
