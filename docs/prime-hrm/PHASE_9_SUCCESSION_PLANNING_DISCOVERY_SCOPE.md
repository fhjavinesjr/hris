# Phase 9 - Career and Succession Planning Discovery and Proposed Scope

Reviewed: 2026-11-04  
Status: Discovery complete; proposed implementation scope awaiting user approval

## 1. Decision

The next proposed PrimeHR capability is **Phase 9 - Career and Succession Planning**.

Succession Planning is not a fifth CSC core HR system. It is a confidential, competency-based,
cross-core capability that supports organizational continuity, Recruitment/Selection/Placement
(RSP), and Learning and Development (L&D). It consumes authoritative facts from the four completed
HR cores but owns its policy, critical-position planning, candidate-pool, readiness-review, and
development-plan decisions inside a separate `com.primehr.succession` boundary.

The product remains **ISOFT HRIS** and the module remains **PrimeHR**. Phase 9 must not claim PRIME
accreditation or automate an appointment, promotion, salary change, award, or RSP selection.

## 2. Current policy findings

The design is anchored to the current policy position as of this review:

- CSC Resolution No. 2600005 (2026) states that competency-based position and person profiles are
  compared to determine fitness for work. It specifically identifies determining readiness for
  higher responsibilities for succession management and developing competencies for higher
  responsibilities as applications of the competency system.
- The same resolution requires an agency-approved policy governing integration of competencies
  into organizational practices. Therefore readiness rules, roles, scales, and career pathways
  must be versioned agency policy, not constants in source code.
- Competency evidence supports a human decision; it does not establish entitlement to promotion.
  Appointment and merit-selection rules remain authoritative in CoreHR/RSP.
- CSC Resolution No. 2600988 and Memorandum Circular No. 11, s. 2026 introduced PRIME-ESG. The
  redesigned framework took effect on 2026-10-18 and supersedes inconsistent legacy PRIME-HRM
  instruments. Existing maturity/accreditation labels must not be embedded into Phase 9 logic.
- PRIME-ESG rollout and future instruments may change governance/reporting requirements. Phase 9
  must therefore preserve exact policy/legal-basis versions and configurable report templates.

Official references:

- https://www.csc.gov.ph/phocadownload/userupload/irmo/policy%20resolutions/2026/CSC%20Resolution%20No.%202600005%20-%20Guidelines%20on%20the%20Development%20of%20Agency%20Competency-Based%20Human%20Resource%20Management%20System.pdf
- https://www.csc.gov.ph/downloads/category/600-mc-no-11-s-2026
- https://www.csc.gov.ph/csc-adopts-prime-esg-as-framework-for-modernizing-government-hr-systems

## 3. Repository and legacy findings

### Current ISOFT HRIS

- No succession package, tables, migrations, permissions, APIs, frontend routes, reports, or tests
  currently exist.
- Phase 2 provides immutable, effective Position Profiles and exact competency requirements.
- Phase 3 provides human-validated, versioned Person Competency Profiles.
- Phase 4 provides transparent position/person competency gap calculations, but a gap is not a
  readiness or promotion decision.
- Phase 5 provides governed RSP and appointment handoff. Succession must refer candidates to RSP;
  it cannot bypass vacancy publication, qualification, screening, comparative evaluation,
  appointing-authority decision, or HRM appointment controls.
- Phase 6 provides immutable final performance outcomes through a controlled read-only feed.
- Phase 7 provides approved IDPs, verified completions, follow-through results, and a controlled
  completion feed.
- Phase 8 provides immutable recognition facts. Awards may be documentary evidence only and must
  not automatically place an employee in a pool or increase readiness.
- Administrative exposes Job Position, Plantilla, Area, Business Unit, Qualification Standard,
  organization membership, approval-route, permission, and report-branding sources.
- HRM exposes active assessment/performance participants and Plantilla occupancy. These contracts
  do not yet provide complete service history, employment-status history, retirement eligibility,
  temporary designation, or separation-risk facts needed for an automatic vacancy-risk model.
- Supervisor/head data remains partial. Phase 9 must use an explicit reviewed authority contract
  for planning and approval roles instead of inferring authority from a job title.

### Legacy application

No meaningful Succession Planning implementation, table family, workflow, user interface, or JRXML
was found. There is no legacy data model to migrate and no legacy form that can be treated as an
approved succession template.

This means Phase 9 is a new bounded implementation. It must not manufacture historical candidate
pools or readiness decisions from old employee, performance, training, or award records.

## 4. Ownership and integration boundaries

| Concern | Authority | Phase 9 treatment |
|---|---|---|
| Area, Business Unit, Job Position, Plantilla, Qualification Standard, permissions and branding | Administrative/CoreHR | Authenticated read-only references and immutable snapshots |
| Employee identity, active appointment, employment status, service history and position occupancy | HRM/CoreHR | Authenticated read-only facts; no direct table access |
| Position competency requirements | PrimeHR Competency | Exact effective Position Profile version through an application port |
| Employee attained competencies | PrimeHR Competency | Exact validated Person Profile version through an application port |
| Final performance outcomes | PrimeHR Performance | Supporting evidence through the controlled outcome application port; never automatic ranking |
| Approved IDPs, verified completions and effectiveness | PrimeHR L&D | Supporting development evidence through application ports |
| Vacancy and merit-selection process | PrimeHR RSP | Authoritative competitive selection; succession can refer but never select or appoint |
| Recognition facts | PrimeHR R&R | Optional evidence only; never automatic readiness or pool membership |
| Succession policy, career paths, critical positions, pools, readiness decisions and development reviews | PrimeHR Succession | New Phase 9 owner; versioned, audited, privacy restricted |
| Appointment, promotion, salary and payroll execution | HRM/Administrative/Payroll | Explicitly excluded from Phase 9 |

One PrimeHR domain calls another domain's application interface, never its repository. External
CoreHR facts use configurable authenticated HTTP contracts. Commands fail closed when required
authority, appointment, position, or policy facts cannot be verified or their fingerprints are
stale.

## 5. Proposed staged delivery

| Unit | Scope | Proposed migration | Stop boundary |
|---|---|---|---|
| 9A.1 | Versioned succession governance policy, configurable readiness scale, planning/review committee charter, effective roster, conflict declarations, audit and permissions | V47 PostgreSQL/SQL Server | No critical-position or employee pool decisions |
| 9A.2 | Versioned career families/pathways, allowed source-to-target position relationships, critical-position criteria, human-approved critical-position designation and immutable source snapshots | V48 PostgreSQL/SQL Server | No employee candidate intake or readiness scoring |
| 9B | Succession cycle, position continuity/risk review, candidate nomination/intake, notice/consent, withdrawal, eligibility review, secured evidence and frozen target-position snapshots | V49 PostgreSQL/SQL Server | No readiness result, ranking, development plan, RSP referral or appointment |
| 9C.1 | Conflict-controlled panel assignments, configurable criterion findings, exact competency-gap calculation, human readiness recommendation and authorized final pool decision | V50 PostgreSQL/SQL Server | No automatic rank, promotion, appointment or RSP outcome |
| 9C.2 | Target-position succession development plan, approved IDP/L&D links, milestones, employee acknowledgment, periodic readiness review, correction/expiry/removal lineage | V51 PostgreSQL/SQL Server | No automatic L&D enrollment or CoreHR mutation |
| 9D | Position continuity plan, confidential bench coverage, authorized RSP referral packet, referral receipt/status, vacancy linkage and audit | V52 PostgreSQL/SQL Server | RSP remains authoritative; no automatic application, shortlist, selection or appointment |
| 9E | Administrative controls, PrimeHR workspaces, authenticated-own Employee Portal My Career view, bean-driven reports, privacy analytics, documentation, production builds and packaged Playwright | None unless a reviewed reporting need requires it | Stop before workforce-planning automation or any CoreHR/Payroll mutation |

Each unit requires focused approval before implementation. Migration numbers are reserved proposals
and must be reconfirmed against the checked-out migration head before code is added.

## 6. Proposed domain model

### Governance

- `SuccessionPolicy` and immutable effective versions;
- configurable readiness levels and criterion definitions;
- committee charter, effective roster, voting role and designation source;
- conflict declaration, recusal and authorized replacement; and
- policy/legal basis, approval reference, effectivity and successor lineage.

### Career and critical-position planning

- career family and position-path relationship, including lateral/developmental links;
- exact Administrative Job Position/Plantilla snapshots;
- human-owned criticality assessment against configured criteria;
- critical-position designation with effective dates and review/expiry date;
- position continuity risk review with evidence and reason; and
- no inference from employee age, health, gender, disability, leave, payroll, family status, or
  another protected/sensitive PDS attribute.

### Candidate pool and readiness

- individual candidate nomination by an authorized actor or permitted self-expression of interest;
- employee notice, consent/acknowledgment, withdrawal, and correction history;
- exact target position/profile and current appointment snapshots;
- criterion-level eligibility/readiness findings with source version/fingerprint;
- panel assignment, conflict/recusal, recommendation and final decision;
- readiness category, review date, expiry, rationale and development conditions; and
- append-only pool membership history. Pool inclusion is not a promise of promotion.

### Development and continuity

- target-position development plan with measurable outcomes and due dates;
- references to approved L&D IDPs/interventions/completions rather than duplicate L&D execution;
- milestone evidence, supervisor/mentor feedback, employee acknowledgment and periodic review;
- controlled removal, expiry, reinstatement and successor decision lineage;
- critical-position coverage and confidential bench-strength facts; and
- an authorized RSP referral packet that starts no selection or appointment by itself.

## 7. Calculation and decision rules

- The server may calculate transparent competency gaps and policy-defined criterion totals from
  exact source versions. It must show the formula, source date, missing evidence, and stale facts.
- A calculated gap/total is decision support only. An authorized, non-conflicted human body owns
  eligibility, readiness, pool membership, development conditions, and removal decisions.
- Readiness labels are configurable and versioned. Do not hard-code `READY_NOW`, `READY_SOON`, or a
  time horizon as universal government rules.
- No employee-wide leaderboard or forced rank is created.
- A nine-box grid is excluded from the initial implementation. It may be proposed later only when
  the agency supplies approved axes, definitions, due-process/privacy rules, and evidence that it
  does not replace merit selection.
- Missing or stale Position Profile, Person Profile, appointment, policy, or authority data blocks
  a final decision. Historical records retain their original snapshots and display stale warnings.
- Reassessment creates a linked successor. Final decisions and evidence are not overwritten.

## 8. Proposed permissions

| Feature key | Actions | Data scope |
|---|---|---|
| `primehr.succession-governance` | Access, Add, Edit, Publish, Delete/archive | Agency Wide |
| `primehr.succession-career-path` | Access, Add, Edit, Publish, Delete/archive | Agency Wide |
| `primehr.succession-critical-position` | Access, Add, Edit, Submit, Approve, Finalize | Assigned, Agency Wide |
| `primehr.succession-pool` | Access, Add, Edit, Submit, Approve, Delete/withdraw | Own, Assigned, Agency Wide |
| `primehr.succession-readiness` | Access, Edit, Submit, Approve, Finalize | Own-safe view, Assigned, Agency Wide |
| `primehr.succession-development` | Access, Add, Edit, Submit, Approve | Own, Assigned, Agency Wide |
| `primehr.succession-continuity` | Access, Add, Edit, Submit, Approve, Finalize | Assigned, Agency Wide |
| `primehr.succession-report` | Access | Own, Assigned, Agency Wide according to report |
| `primehr.succession-analytics` | Access | Agency Wide |

Backend enforcement combines action, data scope, authenticated identity, source ownership,
effective committee/panel role, conflict/recusal, lifecycle, source freshness and optimistic record
version. Sidebar visibility is not authorization.

## 9. Privacy, fairness, and due process

- Candidate pools, readiness deliberations, restricted evidence, panel notes, conflict details and
  comparative material are confidential HR records.
- Employee Portal exposes only the authenticated employee's notice/consent, own development plan,
  employee-visible findings, acknowledgment/response, and publication-safe status. It has no
  employee selector and never shows another candidate or peer rank.
- Employees receive a reasoned employee-visible outcome and a controlled correction/review path.
  Restricted panel notes remain separate.
- Analytics requires Agency Wide authority and configured minimum-cohort suppression. It reports
  position coverage and aggregate readiness, not named rankings or protected characteristics.
- Do not use age, sex, civil status, disability, health, religion, ethnicity, leave, salary,
  deductions, loans, biometric attendance, or family/PDS details as readiness proxies.
- Attendance, performance, awards, or training cannot automatically include, exclude, score, or
  rank an employee.
- Every sensitive read, export, decision, correction, removal, referral and denied access is audited.
- Evidence storage follows the existing private-object, checksum, safe-name, MIME/size validation,
  authorization, no-store, retention, and deletion-hold patterns.

## 10. Proposed reports and interfaces

Candidate operational reports, subject to agency template approval:

- critical-position and continuity plan;
- position-based succession plan and confidential bench coverage;
- candidate readiness assessment summary;
- succession development plan and review history;
- authorized RSP referral packet; and
- privacy-controlled aggregate coverage/readiness dashboard.

No existing legacy JRXML is available to preserve. New reports should use typed DTOs and
`JRBeanCollectionDataSource`, with JRXML responsible for layout only. They must not be described as
CSC-prescribed forms unless a current official or agency-approved template is supplied and reviewed.

Expected PrimeHR workspaces:

- Succession Governance;
- Career Paths and Critical Positions;
- Succession Cycles and Candidate Pools;
- Readiness Review;
- Development and Continuity; and
- Succession Reports.

Employee Portal should eventually expose **My Career and Development** only for the authenticated
employee. The name must not imply entitlement to promotion.

## 11. Acceptance requirements

- Equivalent PostgreSQL and SQL Server migrations, structural parity, fresh-schema and populated
  previous-version upgrade tests, plus Hibernate validation.
- Domain tests for policy versioning, critical-position decisions, source freshness, candidate
  consent, conflicts/recusal, readiness calculations, final decisions, expiry/removal, and RSP
  referral idempotency.
- Controller/security tests for every action and data scope, including own-safe response mapping.
- OpenAPI contracts and integration-client failure/stale-source tests.
- Report compilation, representative PDFs, pagination, null/empty behavior, and privacy checks.
- Strict TypeScript, lint, production package builds, and packaged Playwright covering permission,
  direct-route denial, own-only portal access, lifecycle, stale writes, privacy suppression, and
  the non-automatic RSP boundary.
- Live PostgreSQL and SQL Server validation when configured; otherwise disclose the limitation.

## 12. Explicit exclusions

Phase 9 excludes:

- automatic promotion, appointment, acting designation, salary/step increase, incentive or payroll;
- automatic RSP application, qualification, shortlist, rank, selection, offer or handoff;
- automatic candidate discovery or pool inclusion from performance, competency, L&D, attendance,
  service length, age, award, PDS, payroll or demographic facts;
- predictive attrition, retirement, resignation, health, disciplinary or behavioral-risk scoring;
- employee ranking, popularity voting, AI recommendations, psychometric inference, or black-box
  potential scoring;
- an unapproved nine-box grid;
- replacement of agency MSP, SPMS, L&D, PRAISE, CBHRMS, PRIME-ESG, appointment or qualification
  rules;
- public candidate/readiness lists or unrestricted data exports;
- direct cross-domain table access;
- email/SMS/push infrastructure; and
- historical succession decisions fabricated from legacy records.

## 13. Implementation gate

Discovery is complete. No Succession Planning functional code, schema, permission, API, UI, report,
or migration was added during this unit.

Implementation may begin with **Phase 9A.1 - succession governance policy, readiness scale, and
committee foundation** only after the user approves this scope. Before that unit starts, the agency
should provide or identify its current approved succession/career-development policy, readiness
definitions, committee/approval authority, confidentiality rules, retention period, and report
templates. Missing agency artifacts may be represented as configurable draft foundations, but the
software must not claim that a generic draft is the agency's approved policy.
