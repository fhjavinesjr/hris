# Phase 5F RSP Reporting, Appointment Documents, and Analytics Scope Approval

## 1. Purpose and approval boundary

This document defines Phase 5F only. The user approved this scope on 2026-09-07 with the required sequential gates and separately approved Phase 5F.4 on 2026-09-08. Phase 5F.1-5F.4 are implemented and verified. The Oath of Office and assumption-to-duty gate was resolved from the legacy ZCMC templates and the current official CSC 2025 forms/signatory contract. Phase 5F is complete. Master Plan V2 has no Phase 5G, so the requested stop boundary is reconciled to Phase 6.

Phase 5F will turn the completed Phase 5A-5E records into permission-controlled, reproducible reports without changing any recruitment, evaluation, selection, appointment, onboarding, or employee-activation decision. PrimeHR owns RSP reports. HumanResource continues to own employee, appointment, Personnel Action, Oath of Office, assumption-to-duty, and onboarding outputs.

Scope approval authorizes only the gated sequence described in this document. It does not waive the authoritative-form gate in Phase 5F.2 or the separate controls/UI/Playwright gate in Phase 5F.4.

## 2. Repository findings

### Existing reporting contracts

- PrimeHR currently packages only `vacancy_notice.jrxml` and `competency_gap_report.jrxml`. Both are compiled from classpath resources and filled from typed Java rows using `JRBeanCollectionDataSource`; neither performs a database query inside JRXML.
- The vacancy notice is available only for `APPROVED` or `PUBLISHED` publications. Its controller returns `application/pdf` with a controlled content disposition.
- HumanResource already owns `personnel_action.jrxml` and `GET /api/employeeAppointment/report/{employeeAppointmentId}`. The loader resolves the authoritative appointment history, organization fields, agency name/address, and logos. Phase 5F must reuse this report rather than copy it into PrimeHR.
- Existing report tests compile JRXML, generate representative PDFs, and exercise multi-page output. This remains the required pattern.
- There is no separate reporting service or warehouse. The architecture decision remains service DTOs plus packaged Jasper resources in the owning module.

### Existing RSP source contracts

- Recruitment planning and publication preserve Job Position, Plantilla, Qualification Standard, position-profile versions, publication periods/channels, authority, approval, and immutable vacancy snapshots.
- Applications preserve the submitted applicant/profile/document snapshot and consent basis. Applicant document binaries remain in private storage behind authorized download endpoints.
- Screening preserves policy/version, assignments, findings, evidence references, final outcome, reasons, correction history, and audit.
- Evaluation preserves the effective policy and committee version, proceeding, sessions, attendance, conflicts, validated stage results, individual panel ratings, reference checks, meeting/minutes/resolution metadata, comparative scores/ranks/ties/exclusions, evidence metadata, and final recommendation.
- Selection preserves the exact comparative fingerprint, revision/supersession chain, candidate decisions, appointing-authority decision and variance basis, offer status, notices, approval/finalization actors, and timestamps.
- PrimeHR handoff preserves its immutable payload fingerprint, correlation/retry state, and HumanResource receipt. It does not own employee, appointment, or onboarding completion state.

### Existing HumanResource source contracts

- HumanResource owns the durable handoff receipt, onboarding template/version, onboarding case/items/evidence references, identity decision, employee record, appointment, appointment provenance, and one-time activation invitation.
- Onboarding becomes `COMPLETED` only after the appointment exists and every required item is verified.
- The current onboarding response exposes intake, selection/application/applicant references, status, resolved employee/appointment IDs, and item completion/verification. It must never expose an activation token in a report.
- The repositories do not contain an approved Oath of Office template, assumption-to-duty template, authoritative legal text/version, venue, administering officer, or certifying officer contract. Those values must not be invented or hardcoded.

### Analytics data limitation

The applicant profile currently contains name, birth date, contact/address, citizenship, declaration, and profile entries. It does not contain a governed sex/gender, disability, indigenous-community, or other equal-opportunity monitoring contract. Phase 5F must not infer protected characteristics from names, photographs, documents, addresses, or free text. Initial analytics therefore cover process and outcome measures only. Demographic reporting is deferred until a separately approved privacy notice, lawful purpose, field definitions, collection/consent rules, retention, minimum-cell suppression, and disclosure policy exist.

## 3. Ownership and integration boundary

| Record/output | Owner | Phase 5F rule |
|---|---|---|
| RSP case record, comparative result, evidence index, register, process analytics | PrimeHR | Query PrimeHR repositories/services only; no cross-database SQL |
| applicant document/evaluation evidence binary | PrimeHR private storage boundary | Never embed or bulk-export; index metadata and protected record IDs only |
| handoff delivery/receipt | PrimeHR and HumanResource, respectively | Each reports its own durable state; receipt ID/fingerprint links them |
| Personnel Action, appointment, Oath, assumption-to-duty, onboarding completion | HumanResource | Generated only by HumanResource from authoritative HR records |
| agency branding and report-signatory assignments | Administrative | Read through typed/configured contracts; snapshot into finalized legal documents |
| permissions | Administrative | Exact independent feature/action/data-scope rules, enforced again in each backend |

PrimeHR must not read HumanResource tables or render HumanResource legal documents. HumanResource must not read PrimeHR tables. Browser tokens must not be forwarded as service credentials. Phase 5F introduces no cross-module write beyond any already-approved service contract, and no Jasper template may contain a cross-database query.

## 4. Exact report catalogue

### 4.1 Formal Comparative Evaluation Report

One confidential PDF per finalized proceeding. It contains:

- agency, vacancy/publication, Plantilla, position, assignment, and source-version identifiers;
- evaluation policy/version, criteria/stage weights, rounding/ranking/tie rules, and source fingerprint;
- HRMPSB committee version, quorum result, meeting/resolution references, and finalization actors/times;
- qualified candidates, validated stage subtotals, total score, rank/tie, exclusion/gate result, and HRMPSB recommendation;
- clear markings for superseded/corrected source revisions and report generation metadata.

It excludes applicant contact/address/birth/citizenship, raw document binaries, individual panel comments, reference-check narrative, conflict narrative, hidden internal notes, passwords, tokens, and onboarding data. It is available only when the comparative evaluation and proceeding are final and their fingerprints still match. A corrected proceeding produces a report from the new current revision; older revisions remain auditable and may be downloaded only from explicit history.

### 4.2 Selection and Appointment Process Record

One confidential PrimeHR PDF per finalized selection case. It contains the vacancy/publication and comparative references; selection revision chain; candidate disposition; selected candidate; appointing-authority decision, approved variance basis when applicable, final offer disposition, notice release state, and handoff/receipt identifiers and state.

This report ends at HumanResource receipt. It must not claim that an employee, appointment, or onboarding completion exists. It excludes applicant-safe notice text for other candidates, applicant credentials, employee IDs/numbers, activation data, raw evidence, and HRM-only fields.

### 4.3 RSP Evidence Index

One confidential PDF per terminal proceeding/selection revision. It is an index, not an evidence archive. It lists each source record's type, record ID, business/record version, classification, retention tag, checksum/fingerprint where available, custodian module, status, and relevant event timestamp. Protected links use opaque record IDs and still require normal backend authorization.

The index never embeds document/evidence binaries, file-system paths, storage keys, signed URLs, access tokens, reference-check narrative, panel notes, minutes text, or applicant credentials. Phase 5F does not create a ZIP/package downloader.

### 4.4 RSP Register

An agency-wide paged JSON view plus PDF export covering vacancies and their current RSP outcome. Exact filters are `fromDate`, `toDate`, event-date basis, vacancy/publication/proceeding/selection status, Job Position, Plantilla, Area, Business Unit, and outcome. The event-date basis must be selected explicitly from publication date, application closing date, proceeding finalization date, selection finalization date, or handoff acknowledgment date; the UI must not silently mix date meanings.

Rows contain vacancy identity, position/Plantilla, publication window, counts by workflow stage, current proceeding/selection revision and status, selected/no-selection/deferred outcome, offer disposition, and handoff state. Candidate names appear only in the separately permissioned confidential detail report, not in the general register export.

### 4.5 RSP Process Analytics

An agency-wide JSON view plus PDF export with the same explicit filters/date basis. Initial measures are:

- vacancy counts by status/outcome and publication channel;
- application, withdrawn, screened, qualified, disqualified, evaluated, selected, accepted, declined/expired, and acknowledged-handoff counts;
- conversion rates with numerator, denominator, and formula shown;
- elapsed calendar days for publication-to-close, close-to-screening-final, screening-to-proceeding-final, proceeding-to-selection-final, selection-to-offer-response, and accepted-offer-to-handoff acknowledgment;
- disqualification counts by controlled reason code and no-selection/deferred counts by controlled outcome.

Missing milestones remain `N/A`; they are never treated as zero duration. Corrections/successors count only the current revision in operational totals while historical revision counts are separately labelled. The report carries an as-of instant, timezone, filters, formula version, and source fingerprint. No target, quota, league table, employee-performance judgment, demographic breakout, inferred attribute, or automatic selection recommendation is included.

### 4.6 HumanResource appointment/onboarding outputs

HumanResource exposes these separately from PrimeHR:

1. Existing Personnel Action PDF, using the existing appointment endpoint and data loader. Phase 5F adds exact backend report authorization and may add a permission-safe link from the HRM intake page, but does not duplicate or redesign the template incidentally.
2. Onboarding Completion Record PDF, available only for a `COMPLETED` onboarding case. It shows the authoritative intake/receipt, employee/appointment reference, template/version, required checklist items and statuses, completion/verification actors, evidence-reference identifiers (not contents or storage paths), completion time, agency branding, and report metadata.
3. Oath of Office PDF and Certificate of Assumption to Duty PDF, available only after their official agency/CSC templates, exact legal text, field definitions, signatory roles, and print specifications are supplied and approved. They use HumanResource appointment/employee facts plus finalized document metadata; no browser-only values or hardcoded signatory are allowed.

Oath and assumption documents are not considered delivered merely because a generic draft PDF can be generated. Their Phase 5F.2 gate requires representative approved source forms and pixel/data inspection.

## 5. Permission model

All new permissions are deny-by-default, independently assignable, backend-enforced, and `AGENCY_WIDE` only. `OWN_RECORD` and organization-unit scope are not valid because these reports combine candidates and the current identity model has no authoritative actor-to-unit responsibility mapping.

| Feature key | Applicable actions | Protected capability |
|---|---|---|
| `primehr.rsp-comparative-report` | `canAccess` | list/read/download comparative reports, including explicit history |
| `primehr.rsp-selection-report` | `canAccess` | list/read/download Selection and Appointment Process Records |
| `primehr.rsp-evidence-index-report` | `canAccess` | read/download the confidential evidence index |
| `primehr.rsp-register-report` | `canAccess` | query/download the general RSP register |
| `primehr.rsp-process-analytics` | `canAccess` | query/download process analytics |
| `hrm.appointment-report` | `canAccess` | generate/download the existing Personnel Action report |
| `hrm.onboarding-report` | `canAccess` | read/download completed onboarding records |
| `hrm.appointment-documents` | `canAccess`, `canAdd`, `canEdit`, `canFinalize` | view; create draft Oath/assumption metadata; edit draft; finalize an immutable document snapshot |

`canAccess` is sufficient for deterministic generation/download of a report from an already final source; report generation is not falsely modeled as record creation. `canAdd`, `canEdit`, and `canFinalize` apply only to the new HumanResource legal-document record. `canDelete`, `canPublish`, `canSubmit`, `canApprove`, `canAssess`, and `canValidate` do not apply to these feature rows. Administrator behavior retains the existing role `"1"` contract. A hidden link or disabled button is never the authorization boundary.

## 6. API and response boundary

Proposed versioned staff endpoints:

```text
GET /api/primehr/v1/rsp/reports/comparative-evaluations/{proceedingId}.pdf
GET /api/primehr/v1/rsp/reports/selections/{selectionId}.pdf
GET /api/primehr/v1/rsp/reports/evidence-index/{selectionId}.pdf
GET /api/primehr/v1/rsp/reports/register
GET /api/primehr/v1/rsp/reports/register.pdf
GET /api/primehr/v1/rsp/reports/process-analytics
GET /api/primehr/v1/rsp/reports/process-analytics.pdf

GET /api/hrm/v1/appointment-intakes/{intakeId}/onboarding-completion.pdf
POST /api/hrm/v1/appointments/{appointmentId}/documents
PUT /api/hrm/v1/appointments/{appointmentId}/documents/{documentId}
POST /api/hrm/v1/appointments/{appointmentId}/documents/{documentId}/finalize
GET /api/hrm/v1/appointments/{appointmentId}/documents/{documentId}.pdf
```

The JSON register/analytics response is typed and paged where rows can grow. Date ranges have a configured safe maximum; requested page sizes are capped. PDF responses use `application/pdf`, safe quoted filenames, `nosniff`, private/no-store cache headers, and no sensitive values in URLs. Invalid state, stale fingerprint, missing official template, missing signatory, denied permission, and no-data responses are explicit and tested.

No applicant-facing or Employee Portal report endpoint is added. The existing public vacancy notice and applicant-safe status/notices remain unchanged.

## 7. Jasper and data rules

- JRXML is layout-only: no SQL query, datasource connection, repository lookup, scriptlet, provider branch, local absolute path, or remote URL.
- Services load typed immutable report DTOs through repositories/JPQL and fill `JRBeanCollectionDataSource`.
- Agency branding and finalized signatory facts come from authoritative Administrative/HumanResource records and are snapshotted where a legal document must remain reproducible.
- Every PDF prints report kind, template/form version, source revision/fingerprint, generated-at instant, timezone, generated-by employee number, confidentiality label, and page `x of y` where appropriate.
- Templates and subreports are packaged in module resources. Fonts and logos must work in the packaged JAR on Windows/on-premise and Linux/cloud.
- Null/empty data, long names/reasons, Unicode, multiple pages, repeated headers, ties, no selection, superseded history, and missing optional logos/signatures are explicit tests.
- Server-side source fingerprint validation happens immediately before rendering. A report never silently combines stale revisions.

## 8. Audit, privacy, and retention

Every successful or denied sensitive report request is security-auditable without logging report contents. Successful generation records actor, agency, feature/report kind, subject ID/revision, filters/date basis, template/form version, source fingerprint, output checksum, generated-at, correlation ID, and outcome. PrimeHR uses its existing `prime_audit_event` service; it does not need a new export-history table. HumanResource uses the onboarding audit for intake-owned completion reports and the Phase 5F.2 legal-document audit records for Oath/assumption documents.

PDF bytes are generated on demand and are not stored in the database by default. Existing immutable source records plus template version and checksum provide reproducibility. If policy later requires retaining signed/scanned issued forms, that is a separate private document-storage and electronic-signature scope.

Retention periods remain Administrative policy/configuration, not hardcoded Java/JRXML. Reports do not extend the retention of their sources. Logs must not contain candidate lists, ratings, applicant PII, document paths, activation tokens, passwords, JWTs, or PDF bytes.

## 9. Migration plan

### PrimeHR and Administrative

No PrimeHR schema migration is required for Phase 5F.1 or 5F.3. Reports are projections of existing V1-V22 immutable/versioned records, and generation metadata fits the existing `prime_audit_event` contract. Administrative adds supported feature keys and permission UI mappings, but no database schema change is required because rulesets persist versioned JSON.

If implementation discovers that an exact required fingerprint or terminal timestamp is absent, work must stop and the scope must be amended; Phase 5F must not hide a workflow-schema change inside a report migration.

### HumanResource V3

Phase 5F.2 adds paired PostgreSQL and SQL Server `V3__appointment_legal_documents.sql` migrations under the existing `primehr-intake` Flyway locations. It creates:

- `hrm_appointment_document`: agency, appointment/intake IDs, document kind, status, official template code/version/checksum, source fingerprint, issue/assumption/oath dates, venue, administering/certifying employee references plus immutable name/position snapshots, record version, finalized/superseded actors/times, and audit timestamps;
- `hrm_appointment_document_audit_event`: append-only before/after metadata, actor, action, reason, correlation ID, and occurrence time;
- provider-equivalent foreign keys, current-document uniqueness, lookup indexes, and optimistic-version constraints.

Document kinds are exactly `OATH_OF_OFFICE` and `ASSUMPTION_TO_DUTY` in this migration. The existing appointment and Personnel Action records are not copied. Finalized documents are immutable; correction creates a successor and preserves the predecessor. No PDF blob, signature image, password, activation token, applicant document, or PrimeHR data is stored.

The migration must pass fresh and populated V1-V3 HumanResource paths without changing/deleting employee, appointment, provenance, onboarding, or activation rows. PostgreSQL/SQL Server parity and Hibernate validation are mandatory; a live PostgreSQL run remains accurately disclosed if unavailable under the standing policy.

## 10. Controlled implementation slices

### Phase 5F.1 - Formal RSP reports

Implement the Comparative Evaluation Report, Selection and Appointment Process Record, and Evidence Index in PrimeHR, including typed DTO/loaders, exact status/fingerprint gates, backend permissions, audit, OpenAPI, packaged JRXML, PDF tests, dual-provider query tests, and no-cross-domain boundary tests.

Phase 5F.1 must pass completely before Phase 5F.2 starts.

### Phase 5F.2 - HumanResource appointment and onboarding documents

First obtain and record approval of the official Oath of Office and assumption-to-duty source forms, legal text, required fields, signatory roles, and print specifications. Then implement HumanResource V3, immutable document lifecycle/API, Onboarding Completion Record, Oath and assumption PDFs, reuse/link the existing Personnel Action report, backend permissions, audits, and representative visual/data checks.

If the official forms are not supplied/approved, Phase 5F.2 is blocked at this gate; it must not substitute an invented template. Phase 5F.2 must pass completely before Phase 5F.3 starts.

### Phase 5F.3 - RSP register and process analytics

Implement the typed paged register, explicit date-basis filters, process funnel/duration calculations, no-data/current-versus-history rules, PDF exports, permissions, audit, OpenAPI, provider-neutral repository tests, and aggregate accuracy tests. Demographic analytics remain excluded.

Phase 5F.3 must pass completely before Phase 5F.4 starts.

### Phase 5F.4 - Administrative controls, UI, and Playwright acceptance

This slice requires separate explicit approval after 5F.1-5F.3 pass. It adds the eight canonical Administrative permission rows, PrimeHR report/register/analytics screens, HumanResource appointment/onboarding report controls, safe PDF opening/downloading, denied/empty/error/stale states, user/operator documentation, focused Playwright, and the complete regression suite.

No Careers/applicant or Employee Portal report UI is added.

## 11. Acceptance gates

### Phase 5F.1 gates

1. Only final, fingerprint-consistent current or explicitly selected historical revisions render.
2. Comparative calculations exactly reproduce stored validated results, ranks, ties, gates, and recommendations; Jasper performs no calculation.
3. Selection reports stop at handoff receipt and never assert an HRM appointment/onboarding result.
4. Evidence index contains metadata only and every protected link remains independently authorized.
5. Each permission denial fails closed in the backend, including direct URL calls.
6. Representative, no-selection, tie, corrected-history, Unicode, null-logo, and multi-page PDFs compile and are inspected.
7. PrimeHR clean package, PostgreSQL-mode migration/Hibernate/query tests, real SQL Server tests, OpenAPI validation, and scope audits pass.

### Phase 5F.2 gates

1. Official Oath and assumption forms, legal wording, fields, signatory roles, and layout are explicitly approved before implementation.
2. Existing Personnel Action behavior remains compatible and authoritative.
3. Onboarding Completion renders only for `COMPLETED`; Oath/assumption render only from valid appointment and finalized document records.
4. Missing/stale appointment, incomplete onboarding, invalid chronology, missing signatory/template, duplicate current document, and replay are rejected atomically.
5. Finalization snapshots authoritative employee/position/signatory/form data; correction preserves immutable history.
6. Activation token/password and private evidence contents never enter API responses, audit state, logs, or PDFs.
7. HumanResource fresh/populated V1-V3 SQL Server gates, PostgreSQL-mode/parity gates, clean package, HRISApp wiring, representative PDF generation, and visual/data inspection pass.

### Phase 5F.3 gates

1. Every count/duration has a documented numerator, denominator, event date, timezone, null rule, revision rule, and reproducible fixture.
2. Register and analytics totals reconcile to independently queried fixture records, including withdrawn, corrected, deferred, no-selection, declined, expired, and failed/acknowledged handoffs.
3. Current operational totals never double-count superseded revisions; history is explicitly labelled.
4. Filter/page/range limits prevent unbounded exports and invalid mixed date semantics.
5. No demographic inference, candidate performance league table, target/quota, or automated recommendation exists.
6. Permission, audit, PDF, provider-neutral query, no-cross-domain access, clean package, and regression gates pass.

### Phase 5F.4 gates

1. Administrative controls persist and resolve all eight feature keys with only applicable actions and agency-wide scope.
2. PrimeHR and HRM pages hide unauthorized navigation but also display fail-closed denial for direct routes/actions.
3. PDFs open/download with correct media type, filename, cache/security headers, and no token-bearing URL.
4. Playwright covers each allowed/denied report, final/stale/no-data/history states, filter reconciliation, privacy exclusions, existing Personnel Action compatibility, and finalized legal-document immutability.
5. All affected strict type-check, focused/full lint according to each repository baseline, production builds, backend clean packages, focused Playwright, and the complete existing regression suite pass with no unexplained skips.
6. Review manifest, report catalogue/data dictionary, permission matrix, operator guide, retention/privacy disclosure, and deployment/rollback instructions are complete.

## 12. Explicit exclusions

- any automatic screening, ranking, recommendation, selection, appointment, employee creation, onboarding completion, or account activation;
- changing stored scores, ranks, selection decisions, appointments, checklist results, or evidence through a report endpoint;
- applicant-facing comparative/selection/evidence/register/analytics reports or Employee Portal access;
- demographic/equal-opportunity reporting until its separate governed data contract is approved;
- inferred protected characteristics, predictive hiring, applicant risk scores, AI summaries, résumé parsing, facial/emotion analysis, or performance league tables;
- evidence ZIPs, bulk document download, embedded evidence binaries, public/signed storage URLs, electronic signatures, signed-form storage, or external government submission;
- redesigning the existing Personnel Action form, PDS, Service Record, or unrelated Jasper reports;
- direct cross-domain database access, Jasper SQL, a reporting database/warehouse, BI platform, generic report builder, or separate reporting microservice;
- CSV/Excel export in the initial scope; adding it requires formula-injection/privacy acceptance and separate approval;
- organization-unit data scope until authoritative actor-to-unit responsibility exists;
- hardcoded agency legal text, logos, signatories, form versions, retention, thresholds, URLs, credentials, or provider behavior;
- Phase 6 or any later behavior.

## 13. Approval semantics and recommended next command

Approval of this document authorizes implementation only in the gated sequence 5F.1, 5F.2, and 5F.3. Phase 5F.2 cannot begin document-template work until the authoritative forms/signatory contract is supplied and approved. Phase 5F.4 remains separately gated even if the backend slices pass.

Recommended approval wording:

The original approval wording said “Stop before Phase 5G.” Because Master Plan V2 ends Phase 5 at 5F, this is recorded as a stop before Phase 6, preserving the intended major-phase approval boundary.
