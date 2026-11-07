# Phase 7E - L&D Controls, Interfaces, Reports, and Acceptance Review

Reviewed: 2026-11-02  
Status: Complete and verified; Phase 7 is complete

## Delivered scope

Phase 7E exposes the immutable Phase 7A-7D learning-and-development records through exact
Administrative permissions and permission-aware operational surfaces:

- Administrative contains independent rows for governance, needs, IDPs, plans, programs and
  offerings, nominations, delivery/completion, evaluation/follow-through, formal reports,
  aggregate analytics, and the API-only completion feed.
- PrimeHR contains separate Governance & Catalog, Needs & IDPs, Plans & Programs, Nominations,
  Delivery, Evaluation, and Reports workspaces. Each route fails closed without its exact feature
  permission, and lifecycle controls also require the corresponding action and process role.
- Employee Portal **My Learning** derives the employee from the authenticated account and exposes
  only that employee's needs, IDPs, nominations, schedules, evaluation tasks, action plans,
  completion records, and lifecycle-eligible PDFs. It has no employee selector.
- Formal PDF endpoints cover an approved Individual Development Plan, an approved Training Order,
  a completed employee L&D record packet, and a completed evaluation/follow-through summary.
- Effectiveness aggregates require the independent agency-wide `primehr.ld-analytics` permission;
  groups below the configured distinct-employee cohort are suppressed.

The completion feed remains an API integration contract and is not rendered in either frontend.

## Form and source comparison

The report implementation was adjudicated against:

- legacy `web/reports/hospitalTrainingOrder.jrxml`, which supplied the agency Training Order
  concepts of subject, attendance directive, dress code, participant roster, lecturer/support
  roles, and agency branding;
- legacy `web/reports/pcmc/spms/Individual_Performance_Development_Plan.jrxml`, which supplied
  development target, objective, activity, support, planned completion, accomplishment, supervisor,
  and employee/office identity concepts;
- the Civil Service Commission Competency-Based Learning and Development Management System,
  including its IDP and Learning Application Plan annexes; and
- the CSC Competency-Based Learning and Development Program and L&D Plan guidance, which tie
  interventions to competency gaps, approved IDPs, office plans, transfer of learning, and
  supervisor follow-through.

The new report is deliberately an **agency operational report**, not a claim that ISOFT's generic
layout is a prescribed CSC form. It preserves authoritative lifecycle and source facts while
removing legacy absolute paths, SQL-coupled fields, hard-coded agency URLs/accreditation text, and
fragile subreport dependencies. An agency may approve a branded layout successor later without
changing the report service contract or historical source snapshots.

The JRXML is SQL-free and packaged with the application. Java prepares typed report data and fills
the template through a bean/map collection data source. Empty and optional values are handled by
the service, and only eligible approved/completed records can be printed.

Official references reviewed:

- https://csc.gov.ph/downloads/learning-and-development?download=2617%3Acbldms
- https://www.csc.gov.ph/programs/competency-based-learning-and-development-program
- https://csc.gov.ph/phocadownload/userupload/csi/cscldplan.pdf

## Authorization and privacy

- Backend report access intersects `primehr.ld-report` with the exact underlying workflow
  permission and record scope; a visible button is never treated as authorization.
- Employee report endpoints resolve ownership from the authenticated principal.
- Analytics is not implied by evaluation or report access. It requires a separate agency-wide
  `primehr.ld-analytics` grant.
- Evaluation results, action-plan narratives, evidence locations, and private evaluator content are
  not exposed in aggregates or the completion feed.
- Browser routes fail closed and unauthorized Employee Portal direct URLs return to Dashboard.

## Verification

- Clean focused report/OpenAPI/controller-permission test gate: **39/39 passed**.
- Full PrimeHR regression and package: **404/404 passed**, zero failures/errors/skips, and the
  production JAR was rebuilt.
- Administrative, PrimeHR, and Employee Portal strict TypeScript/build gates passed; PrimeHR and
  Employee Portal lint passed. The changed Administrative permission page passes focused lint.
- PrimeHR and Employee Portal production packages were created successfully.
- Packaged Chromium Phase 7E acceptance: **3/3 passed**, covering fail-closed direct reporting,
  agency-wide privacy analytics, authenticated-own My Learning, and lifecycle-eligible print
  actions.
- The Jasper renderer test compiled and filled `ld_formal_report.jrxml` and produced a non-empty
  representative PDF.

The full Administrative lint command still reports pre-existing unused helpers in
`SystemSetup.tsx` and unrelated warnings; Phase 7E did not modify those files. The packaged
Employee Portal server also logs an optional Windows Sharp native-module warning when an optimized
image is requested, but the Phase 7E browser scenarios pass.

## Provider and boundary statement

Phase 7E adds no schema migration. The underlying V37-V42 PostgreSQL/SQL Server structures retain
their prior parity, fresh-migration, populated-upgrade, and Hibernate-validation gates. No
configured live PostgreSQL or SQL Server endpoint was exercised during this interface-only unit.

No CoreHR/PDS write, competency re-rating, performance-intervention closure, reward, promotion,
payroll, timekeeping, notification infrastructure, or other downstream mutation was introduced.
Phase 7 is complete. Rewards and Recognition and Succession Planning remain outside this phase and
require separate discovery and scope approval.
