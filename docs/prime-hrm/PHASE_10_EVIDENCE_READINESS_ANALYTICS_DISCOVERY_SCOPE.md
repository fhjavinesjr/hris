# Phase 10 - PRIME-ESG Evidence, Internal Readiness, and Cross-Module Analytics Discovery

Reviewed: 2026-11-05  
Status: Complete; Phases 10A-10D implemented and verified

## 1. Decision and numbering

The next proposed PrimeHR capability is **Phase 10 - PRIME-ESG Evidence, Internal Readiness, and
Cross-Module Analytics**.

The original master plan called this capability Phase 9 and called Integration Hardening Phase 10.
The delivered project later used Phase 9 for the separately approved Career and Succession Planning
domain. To preserve the repository's immutable delivery history, the remaining master-plan phases are
renumbered as follows:

| Master-plan label | Repository delivery label |
|---|---|
| Phase 9 - Evidence Center and Analytics | Phase 10 - Evidence, Internal Readiness, and Analytics |
| Phase 10 - Integration Hardening | Phase 11 - Integration Hardening |

Phase 10 is a cross-domain governance capability, not a seventh transactional PrimeHR core. It owns
configurable framework definitions, internal evidence assessments, gaps/actions, readiness snapshots,
and privacy-safe consolidated views. Competency, RSP, Performance, L&D, R&R, and Succession remain the
authoritative owners of their operational transactions.

The system must describe its result as **internal readiness/self-assessment support**. It must not
calculate, award, certify, or imply an official CSC PRIME-ESG Governance Level.

## 2. Current policy findings

- CSC Resolution No. 2600988, operationalized by Memorandum Circular No. 11, s. 2026, adopted
  PRIME-ESG and took effect on 2026-10-18.
- The current framework replaces the former periodic PRIME-HRM documentary
  assessment/validation/accreditation/award cycle where the older instruments are inconsistent with
  PRIME-ESG. The software therefore must not hard-code former maturity levels or old indicators as
  current official rules.
- PRIME-ESG emphasizes continuous governance, verified data, integrated digital systems,
  accountability, and evidence-based leadership. A configurable, versioned internal evidence center
  is consistent with that direction, but CSC remains the authority for official instruments and
  determinations.
- Historical PRIME-HRM records may be retained as clearly labeled historical/transition evidence.
  They must not be silently reclassified as a current PRIME-ESG result.

Official references:

- https://www.csc.gov.ph/downloads/category/600-mc-no-11-s-2026
- https://www.csc.gov.ph/csc-adopts-prime-esg-as-framework-for-modernizing-government-hr-systems
- https://csc.gov.ph/regional-leaders-step-up-to-spearhead-nationwide-csc-prime-esg-rollout

Until the agency supplies the approved current PRIME-ESG instruments applicable to it, definitions
may be created only as configurable drafts. No generic seed is to be labeled agency-approved or CSC
official.

## 3. Repository and legacy findings

### Current ISOFT HRIS

- No generic evidence-center, framework registry, indicator registry, internal-assessment, gap/action,
  or consolidated-analytics package currently exists.
- Each PrimeHR domain already owns appropriate operational evidence and reports. Examples include
  competency assessment evidence, RSP screening/evaluation evidence, performance monitoring and
  appeal facts, L&D completion evidence, R&R nomination evidence, and confidential succession
  evidence.
- RSP, Performance, L&D, R&R, and Succession already provide bounded reports or privacy-threshold
  analytics. Performance and L&D also expose controlled, read-only outcome/completion feeds.
- The shared `prime_audit_event` model records actor, action, aggregate, versions, before/after state,
  reason, correlation, and timestamp and can audit the new domain without creating a second audit
  framework.
- Private document storage has local and S3-compatible implementations behind `DocumentStorage`, but
  the interface currently resides under the RSP applicant package. Phase 10 may consume that boundary;
  moving it to a shared package is architectural cleanup and must not be mixed into the first
  transactional unit without a separate compatibility review.
- There is no broker, transactional outbox, integration-event framework, or notification service.
  Existing direct application interfaces and authenticated REST contracts remain the safe integration
  pattern.
- Existing analytics use the configurable `primehr.reporting.minimum-aggregate-cohort` threshold,
  currently defaulting to 5. Consolidated analytics must preserve independent suppression by
  dimension and must not reconstruct suppressed small groups.
- HRM contracts still lack the complete service-history, employment-status, retirement-eligibility,
  temporary-designation, and separation-risk facts required for automated workforce-risk models.

### Legacy application

No generic PRIME evidence center, framework/indicator registry, readiness workflow, or consolidated
PrimeHR analytics implementation was found. The legacy Personnel Turnover Report is a CoreHR report;
it is not a PrimeHR evidence/readiness engine and is not migrated into this phase.

There is no legacy official form to preserve. New PDFs require an agency-approved layout or must be
clearly labeled internal working reports.

## 4. Ownership and integration boundaries

| Concern | Authoritative owner | Phase 10 treatment |
|---|---|---|
| Framework/version, pillar, indicator and requirement definitions used internally | PrimeHR Evidence Center | Versioned agency configuration with legal basis and draft/published/retired lifecycle |
| Administrative structure, permissions, branding and system settings | Administrative/CoreHR | Authenticated read-only references and snapshots |
| Employee and appointment facts | HRM/CoreHR | Read only when an assigned owner/validator must be identified; no PDS replication |
| Competency, RSP, Performance, L&D, R&R and Succession transactions/evidence | Owning PrimeHR domain | Typed link plus source identifier/version/fingerprint; no copied master transaction |
| Evidence assessment, internal finding, gap, corrective action and readiness snapshot | PrimeHR Evidence Center | New Phase 10 owner |
| Official CSC framework content and Governance Level determination | CSC / authorized external process | Never calculated or represented as an official result by ISOFT HRIS |
| Payroll, Timekeeping and CoreHR operational analytics | Owning CoreHR module | Excluded until a reviewed privacy-safe aggregate contract exists |

One PrimeHR domain calls another domain's application interface, never its repository. Evidence links
record the source domain, aggregate type/id, source version/fingerprint, link purpose, visibility,
freshness result, and last verification time. Commands fail closed when a required source cannot be
verified. Historical assessments keep their original snapshots.

## 5. Proposed staged delivery

| Unit | Precise scope | Proposed migration | Stop boundary |
|---|---|---|---|
| 10A | Versioned framework registry; pillars/domains; configurable Governance Level labels without official determination logic; indicator/requirement definitions; legal basis, effectivity, successor/retirement; permissions, audit, REST/OpenAPI | V53 PostgreSQL/SQL Server | No evidence collection, assessment case, gap/action, score, report, UI or analytics |
| 10B | Internal assessment cycle/case; assigned evidence owner and validator; typed links to live PrimeHR sources; optional secured upload; evidence classification/retention/due date; human validation, finding, gap and corrective-action lifecycle; history/corrections | V54 PostgreSQL/SQL Server | No automatic readiness level, official determination, consolidated analytics or external submission |
| 10C | Deterministic completeness/coverage calculations from the exact published definition; human internal sign-off; immutable readiness snapshots; missing/expiring evidence and overdue-action views; evidence index and internal-assessment PDFs; privacy-controlled cross-module aggregate analytics through application ports | V55 PostgreSQL/SQL Server | No employee ranking, predictive analytics, official CSC score/level, cross-database report join, broker or CoreHR mutation |
| 10D | Administrative permission controls; PrimeHR framework, assessment, validation/action and reporting workspaces; production builds; provider/report verification; packaged Playwright acceptance and documentation | None unless a reviewed acceptance need requires it | Stop before Phase 11 integration hardening and before Employee Portal exposure |

Each unit is separately gated. Migration numbers are reserved proposals and must be reconfirmed
against the checked-out migration head immediately before implementation.

## 6. Proposed domain model

### Framework configuration

- `EvidenceFramework` with code, name, issuing body, legal basis, effectivity, status, definition
  version, predecessor/successor, and fingerprint;
- `EvidencePillar` or domain grouping with ordered display metadata;
- configurable `GovernanceLevelDefinition` labels for internal mapping/reference only;
- `EvidenceIndicator` and `EvidenceRequirement` with guidance, required evidence types, recurrence,
  due/freshness rules, applicability, weight or completion rule, and display order; and
- immutable published versions; changes create a successor rather than rewriting history.

### Internal assessment and evidence

- assessment cycle/case bound to one exact framework version and assessment period;
- owner/validator assignments from authoritative employee/appointment facts;
- evidence link to an approved live transaction/report, or an optional secured uploaded artifact;
- source version/fingerprint, classification, visibility, retention tag, evidence date, review/due
  date, last verification, stale/broken status, and replacement lineage;
- human validation outcome, remarks, conflict/recusal, return/rework and correction history; and
- no evidence copy when an authorized stable link can preserve provenance.

### Gaps, actions and snapshots

- reasoned internal finding per requirement: met, partially met, not met, not applicable, or needs
  review, where the exact vocabulary comes from the published framework definition;
- gap/action item, responsible employee/unit, target date, progress, completion evidence, verifier and
  closure/reopening history;
- server-calculated completeness/coverage facts with transparent numerator, denominator, exclusions,
  missing inputs and definition fingerprint; and
- immutable human-signed internal readiness snapshot with clear non-official disclaimer.

## 7. Calculation and decision rules

- The server may calculate only transparent completion, coverage, overdue, and configuration-defined
  aggregate facts from exact source versions.
- A calculation is decision support. A non-conflicted authorized human validator owns each finding
  and internal sign-off.
- Missing, inaccessible, stale, superseded, or checksum-mismatched evidence cannot silently satisfy a
  requirement. The response must explain the blocking source.
- `Not applicable` requires a reason and authority defined by the published framework.
- Finalized snapshots are immutable. Correction creates a linked successor and retains the original.
- No formula may infer an employee's potential, merit, performance, risk, suitability, or official
  PRIME-ESG Governance Level.
- Old PRIME-HRM maturity labels may appear only as historical metadata, never as the current outcome.

## 8. Proposed permissions

| Feature key | Actions | Data scope |
|---|---|---|
| `primehr.evidence-framework` | Access, Add, Edit, Publish, Delete/archive | Agency Wide |
| `primehr.evidence-assessment` | Access, Add, Edit, Submit, Delete/withdraw | Assigned, Agency Wide |
| `primehr.evidence-validation` | Access, Edit, Submit, Approve, Finalize | Assigned, Agency Wide |
| `primehr.evidence-action` | Access, Add, Edit, Submit, Approve | Assigned, Agency Wide |
| `primehr.evidence-report` | Access | Assigned or Agency Wide according to report |
| `primehr.evidence-analytics` | Access | Agency Wide |

Backend enforcement combines the exact action, data scope, authenticated identity, effective
assignment/process role, conflict/recusal, lifecycle, source visibility, source-domain permission,
source freshness, and optimistic record version. A link to a source does not grant permission to read
that source. Sidebar visibility is not authorization.

## 9. Privacy, retention, and analytics architecture

- Evidence may contain confidential personnel, applicant, performance, award, succession, or policy
  information. List/detail/export/download permissions are independently enforced and audited.
- Consolidated analytics consumes privacy-safe domain application ports. It does not query another
  domain's repositories or join service databases in Jasper.
- The dedicated evidence-analytics permission does not bypass source-domain restrictions. A metric is
  included only through an approved aggregate contract, with the strictest applicable data scope and
  cohort suppression.
- Suppression is applied independently to totals and every breakdown. The API omits suppressed cells
  and does not expose enough complementary totals to derive them.
- Initial dashboards may compose bounded live aggregate services. A generic employee-level analytics
  cube/read model is deferred until measured latency, scale, freshness, and reconciliation needs
  justify it.
- If a read model is later justified, it must record source, source version, fetched/sync time,
  freshness, reconciliation state, and deletion/retention behavior and remain non-authoritative.
- Broker/outbox/event activation is deferred to Phase 11 discovery. It is not required merely to join
  modules already running in the same PrimeHR application.
- Employee Portal exposure is excluded from the initial phase. This is agency governance work, not an
  employee self-service workflow. A later own-action view requires a separate privacy/use-case review.

## 10. Proposed reports and interfaces

Candidate internal reports, subject to agency template approval:

- framework/version and indicator register;
- evidence index with provenance, freshness and validation status;
- missing/expiring evidence register;
- gap and corrective-action register with overdue status;
- internal self-assessment summary with non-official disclaimer; and
- privacy-controlled cross-module aggregate dashboard/export.

New reports use typed DTOs and `JRBeanCollectionDataSource`. JRXML owns layout only. There are no
cross-database Jasper joins and no report may be labeled a CSC-prescribed form unless a current
official template is supplied and reviewed.

Expected PrimeHR workspaces:

- Framework Registry;
- Evidence Assessments;
- Validation and Corrective Actions; and
- Evidence Reports and Analytics.

## 11. Acceptance requirements

- Equivalent PostgreSQL and SQL Server migrations, structural parity, fresh-schema and populated
  previous-version upgrade tests, and Hibernate validation.
- Domain tests for versioning, effectivity, publication, assignments, conflicts, evidence provenance,
  source failure/staleness, validation, gaps/actions, corrections and immutable snapshots.
- Controller/security tests for every action/data scope and for source-permission intersection.
- OpenAPI contracts and integration application-port failure tests.
- Privacy tests for cohort suppression, complementary-total inference, confidential evidence, export,
  and direct-object access.
- JRXML compilation, representative PDF generation, pagination, null/empty/stale behavior, provenance,
  and disclaimer inspection.
- Strict TypeScript, lint, production builds, and packaged Playwright for permission/direct-route
  denial, lifecycle, stale writes, separation of duty, source failure, report privacy and suppression.
- Live PostgreSQL and SQL Server validation when configured; otherwise disclose the limitation.

## 12. Explicit exclusions

Phase 10 excludes:

- official CSC PRIME-ESG assessment, validation, certification, accreditation, award, Governance Level
  calculation or external submission;
- hard-coded former PRIME-HRM maturity levels, indicators, documentary checklists or scoring rules;
- automatic finding, sign-off, gap closure or evidence acceptance;
- employee ranking, leaderboard, nine-box, promotion/potential score, predictive attrition,
  retirement/separation risk or AI recommendation;
- automatic recruitment, appointment, promotion, salary, incentive, payroll, attendance or CoreHR
  mutation;
- copying operational transactions into a second system of record;
- direct cross-domain repository/database access or cross-database Jasper joins;
- a generic employee-level analytics warehouse before it is justified;
- RabbitMQ, transactional outbox, gateway, notification infrastructure, email/SMS/push;
- unrestricted evidence download/export or source-permission bypass; and
- Employee Portal UI in the initial delivery.

## 13. Implementation gate

Discovery is complete and the user approved the staged Phase 10 scope. Phases 10A through 10D now
deliver the versioned framework, controlled assessment workflow, internal readiness/reporting
backend, Administrative controls, PrimeHR interfaces, and browser acceptance described above.

Implementation may begin with **Phase 10A - versioned framework, pillar, level-label, indicator and
requirement registry**. Before an agency treats a published definition as its authoritative working
instrument,
the agency must provide or identify its approved current PRIME-ESG framework/instruments, legal basis,
internal governance authority, confidentiality/retention rules, and report templates. Missing agency
artifacts may be represented as drafts, but ISOFT HRIS must not label a draft as CSC official or
agency-approved.
