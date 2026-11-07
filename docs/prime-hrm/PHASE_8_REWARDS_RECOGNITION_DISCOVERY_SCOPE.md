# Phase 8 - Rewards and Recognition (PRAISE) Discovery and Proposed Scope

Reviewed: 2026-11-02  
Status: Complete; Phase 8D controls, interfaces, reports, analytics, and acceptance verified

## 1. Decision

The next PRIME-HRM domain should be **Phase 8 - Rewards and Recognition (R&R/PRAISE)**.
It completes the fourth established HR core after RSP, Performance Management, and Learning and
Development.

Succession Planning must remain a separate later phase. Succession uses competency profiles,
position profiles, career paths, critical-position analysis, readiness assessments, and development
plans. It must never be inferred from an award, final performance rating, or L&D completion, and it
must not be mixed into PRAISE deliberation.

The product remains **ISOFT HRIS** and the module remains **PrimeHR**. Policy metadata and
documentation must nevertheless acknowledge the 2026 transition from PRIME-HRM to PRIME-ESG and
must not hard-code obsolete maturity-award behavior.

## 2. Authoritative policy findings

The following current/continuing sources govern the proposed design:

- CSC Memorandum Circular No. 1, s. 2001 requires every agency to establish PRAISE; supports
  individual and group awards, monetary and non-monetary incentives, a PRAISE Committee and HR
  secretariat, continuing identification of contributions, timely recognition, annual reporting,
  and committee resolution of award issues.
- CSC Resolution No. 2600005 (2026) requires competency use across HR systems. For R&R it calls for
  recognition of competency acquisition/application and reward of exemplary competency
  application. Agency PRAISE amendments must be submitted to the CSC Regional Office.
- CSC's Gawad Lingkod Bayani process confirms that external honor-award nominations pass through
  the agency PRAISE Committee and head-of-agency endorsement before CSC submission. National award
  investigation and final adjudication remain outside the agency system.
- The CSC competency-modelling guide describes succession as preparation of potential successors
  for critical positions and development of a ready bench. This supports keeping succession
  independent from recognition.
- CSC Resolution No. 2600988 and MC No. 11, s. 2026 introduce PRIME-ESG and supersede inconsistent
  PRIME-HRM instruments. Therefore the application must version the exact agency policy/legal basis
  and allow controlled successor policies instead of embedding a static PRIME-HRM checklist.

Official references:

- https://www.csc.gov.ph/phocadownload/userupload/irmo/mc/2001/mc1s2001.pdf
- https://www.csc.gov.ph/phocadownload/userupload/irmo/policy%20resolutions/2026/CSC%20Resolution%20No.%202600005%20-%20Guidelines%20on%20the%20Development%20of%20Agency%20Competency-Based%20Human%20Resource%20Management%20System.pdf
- https://www.csc.gov.ph/downloads/category/600-mc-no-11-s-2026
- https://www.csc.gov.ph/programs/honor-awards-program/gawad-lingkod-bayani
- https://csc.gov.ph/phocadownload/userupload/hrpso/guidebook/Guidebook%20on%20Competency%20Modelling.pdf

## 3. Repository findings

### Current system

- PrimeHR has no `rewards` or `succession` implementation, database tables, permissions, API,
  UI, or report contract.
- Existing immutable sources are suitable only as **evidence candidates**:
  - final performance outcomes and history from Phase 6F;
  - validated person-competency profiles from Phase 3;
  - completed L&D outcomes from Phase 7;
  - active employee/appointment/organization facts from HRM and Administrative.
- Those sources do not establish award eligibility by themselves. A versioned agency PRAISE policy,
  nomination, documentary evidence, conflict-controlled committee evaluation, and authorized final
  decision are still required.
- The HRM basic employee directory does not provide a sufficient authoritative service-history
  contract for length-of-service eligibility. A focused read-only HRM integration contract is a
  prerequisite; PrimeHR must not read HRM tables.
- No current source supports automatic monetary payment, salary increase, appointment, promotion,
  scholarship, leave grant, or payroll transaction.

### Legacy system

Legacy Rewards & Recognition consists of:

- a length-of-service PDF assembled directly from employee, appointment, status, and separation
  DAOs;
- Athletic/Sports, Best Organization Unit, Career & Development, and Gawad Kagitingan nomination
  forms;
- manually entered nominee/event/award/license/sponsor/manager fields; and
- static JRXML criteria, agency form codes, signature labels, and attachment notes.

It does **not** implement a durable nomination aggregate, versioned PRAISE policy, committee roster,
eligibility engine, conflict/recusal, scoring, evidence security, deliberation, approval route,
award ledger, incentive fulfillment, issue resolution, annual PRAISE report, or audit trail.

### Legacy adjudication

| Legacy concept | Decision |
|---|---|
| Individual and organization/team nomination | Retain as configurable nominee types |
| Sports, career/development, heroic service and best-unit examples | Import only as agency-configurable award categories, never hard-code as universal categories |
| Event, accomplishment, award/license and sponsor facts | Retain as typed category-specific evidence fields |
| Supporting certificates and governing-body proof | Retain through secured evidence metadata/storage |
| Nominator, supervisor and committee signatories | Retain as authenticated actors and immutable decision snapshots |
| Length-of-service report | Rebuild from a dedicated HRM service-history projection with explicit as-of date |
| Static criteria and form codes inside JRXML | Reject; criteria/forms belong to versioned agency policy/configuration |
| Browser-entered employee position, office and hire date | Reject; snapshot authoritative HRM/Administrative sources |
| Direct DAO/table access across modules | Reject |
| Temporary PDF files and compiled developer-era Jasper dependencies | Reject |
| A generated form being treated as an approved award | Reject; printing is not a workflow decision |

No meaningful legacy Succession Planning implementation was found.

## 4. Ownership and integration boundaries

| Concern | Owner | Phase 8 use |
|---|---|---|
| Agency identity, organization, Job Position/Plantilla, permissions, report branding | Administrative | Authenticated read-only snapshots |
| Employee identity, active appointment, employment status and service history | HRM | Authenticated read-only eligibility/source snapshots |
| Competency definitions and validated person profiles | PrimeHR Competency | Read through an application port; never repository coupling |
| Final performance outcomes | PrimeHR Performance | Read through a controlled application port; no automatic nomination/award |
| Completed L&D outcomes | PrimeHR L&D | Read through a controlled application port; no automatic nomination/award |
| PRAISE policy, committee, cycles, categories, nominations, evaluation, awards and fulfillment | PrimeHR R&R | New Phase 8 owner |
| Payment, salary, appointment or promotion execution | Payroll/HRM/Administrative under later separately approved integration | Explicitly excluded from Phase 8 automatic behavior |
| Career paths, critical positions, successor pools and readiness | Future Succession domain | Excluded |

One PrimeHR domain may call another domain's application interface, never its repository. External
sources use configurable authenticated HTTP contracts and fail closed when authoritative data is
unavailable or stale.

## 5. Proposed Phase 8 delivery plan

| Unit | Scope | Migration | Stop boundary |
|---|---|---|---|
| 8A.1 | **Complete** - versioned agency PRAISE policy, legal basis/effectivity, award-category definitions, eligibility/criteria schema, committee charter and effective-dated roster | V43 paired PostgreSQL/SQL Server | No nomination, scoring or award |
| 8A.2 | **Complete** - award cycles/windows; individual, group/team and organization-unit nominations; authenticated nominator; authoritative nominee snapshots; secured evidence; source links to competency/performance/L&D facts | V44 paired migrations | No automatic eligibility, recommendation or award |
| 8B | **Complete** - eligibility review, configurable criterion evaluation, evaluator assignment, conflict/recusal, independent scoring/comments, quorum, deliberation, return/disqualification/recommendation, head-of-agency decision and issue/reconsideration tracking | V45 paired migrations | No payroll, promotion or incentive fulfillment |
| 8C | **Complete** - immutable award/conferment record, citation/certificate source, monetary/non-monetary incentive authorization, fulfillment ledger, cancellation/correction successor, external HAP endorsement packet and annual PRAISE register/report facts | V46 paired migrations | No automatic payout, appointment, salary or promotion mutation |
| 8D | Administrative controls, PrimeHR workspaces, Employee Portal My Nominations/Recognitions, bean-driven PDFs/registers, privacy analytics, documentation and packaged Playwright acceptance | Complete | Stopped before Succession Planning |

Each database unit requires migration parity, populated-upgrade preservation, fresh migration,
Hibernate validation, focused tests, OpenAPI/audit checks, and full affected backend regression.
Phase 8D requires strict TypeScript, lint, production packages and packaged Playwright acceptance.

## 6. Proposed core aggregates

### Governance

- `RrPolicy` / immutable `RrPolicyVersion`
- `RrAwardCategory` / immutable category version
- ordered category eligibility rules and evaluation criteria
- `RrCommittee` / effective-dated immutable roster version
- `RrCycle` with nomination, evaluation, decision and conferment windows

The policy stores the exact agency-approved title, legal basis, CSC submission/approval reference
where applicable, effectivity, coverage, issue-resolution rules, monetary constraints, and privacy/
publication rules. Published versions are immutable; corrections use successors.

### Nomination and evidence

- nomination with individual, group/team or organization-unit nominee type;
- authenticated nominator and policy-permitted nomination channel;
- exact category, cycle, nominee, appointment/organization and source snapshots;
- contribution/accomplishment narrative separated into employee-visible, committee-restricted and
  publication-safe text;
- member roster for group nominations;
- secured documentary evidence with checksum, media/size validation and authorized download; and
- immutable submission revisions after return/correction.

### Evaluation and decision

- eligibility review with reasoned findings;
- evaluator assignments from the frozen eligible committee roster;
- conflict declaration, recusal and authorized replacement;
- criterion-level scores/findings against the exact category version;
- quorum and separation-of-duty enforcement;
- committee recommendation and head-of-agency final decision; and
- issue/reconsideration record without overwriting the original decision.

No user types a final calculated score, rank, awardee status or incentive directly. The server
derives calculations from the frozen criterion definition.

### Award and fulfillment

- immutable award/conferment record and publication-safe citation;
- incentive authorizations separated by monetary/non-monetary type;
- fulfillment status and evidence, not direct Payroll/HRM mutation;
- successor correction/revocation with reason and audit;
- external CSC/HAP endorsement packet status without pretending PrimeHR controls CSC adjudication;
  and
- annual PRAISE report/register facts.

## 7. Proposed permissions

| Feature key | Actions | Data scope |
|---|---|---|
| `primehr.rr-governance` | Access, Add, Edit, Publish, Delete/archive | Agency Wide |
| `primehr.rr-committee` | Access, Add, Edit, Publish | Agency Wide |
| `primehr.rr-cycle` | Access, Add, Edit, Submit/open, Finalize/close | Agency Wide |
| `primehr.rr-nomination` | Access, Add, Edit, Submit, Delete/withdraw | Own, Assigned, Agency |
| `primehr.rr-eligibility` | Access, Edit, Submit, Approve | Assigned, Agency |
| `primehr.rr-evaluation` | Access, Edit, Submit, Approve | Assigned, Agency |
| `primehr.rr-award` | Access, Approve, Finalize, Publish | Assigned, Agency |
| `primehr.rr-fulfillment` | Access, Add, Edit, Submit, Approve | Assigned, Agency |
| `primehr.rr-report` | Access | Own, Assigned, Agency according to report |
| `primehr.rr-analytics` | Access | Agency Wide |

Backend enforcement combines action, data scope, authenticated identity, nomination ownership,
frozen committee assignment, conflict/recusal, lifecycle, exact source version and optimistic record
version. Hiding a menu is not authorization.

## 8. Safety, privacy, and fairness rules

- No employee is nominated, scored, disqualified, ranked or awarded automatically from a rating,
  competency profile, attendance, length of service, training, leave, payroll or demographic fact.
- Policy-defined eligibility may prefill objective findings, but an authorized human reviewer owns
  the reasoned decision.
- Committee members cannot evaluate or decide their own nomination or a nomination with a declared
  conflict. Quorum excludes recused/ineligible members.
- Draft evaluator notes, conflicts, background checks, restricted evidence and disqualification
  reasons are not employee/public fields.
- Group results must not expose one member's private HR facts to other members.
- Analytics uses privacy thresholds and does not rank employees or infer protected characteristics.
- Monetary incentives record authorization/fulfillment only. They do not post Payroll transactions.
- Awards that may lead to promotion or salary consequences create no appointment or pay change.
  Those require the legally competent CoreHR process and separately approved integration.
- Hard deletion is prohibited after submission. Corrections and revocations preserve lineage.
- No AI-generated nomination, scoring, ranking, citation, eligibility or award decision.

## 9. Reports and UI planned for 8D

Candidate reports, finalized only after comparison with the agency-approved PRAISE and current
official forms:

- nomination form and evidence checklist;
- eligibility/evaluation worksheet;
- committee deliberation/recommendation and decision record;
- award/citation/certificate source;
- incentive authorization and fulfillment record;
- external HAP endorsement packet;
- length-of-service eligibility register; and
- annual PRAISE report and privacy-controlled aggregate dashboard.

Legacy HRD-009-0 through HRD-012-0 layouts are references only. They must not be reproduced as
current approved forms without agency confirmation.

PrimeHR workspaces are expected for PRAISE Governance, Committee and Cycles, Nomination Inbox,
Eligibility/Evaluation, Awards/Fulfillment, and Reports/Analytics. Employee Portal exposes only the
authenticated employee's permitted nominations, return actions, publication-safe outcomes and
eligible certificates/citations.

## 10. Explicit exclusions

Phase 8 excludes:

- Succession Planning, talent pools, nine-box grids, critical-position benches and successor
  readiness;
- automatic promotion, appointment, salary increase, step increment or payroll payment;
- procurement, accounting, tax, disbursement or budget obligation;
- automatic award based on performance rating, competency, service length or L&D completion;
- disciplinary, grievance, administrative-case or background-investigation case management;
- CSC regional/national adjudication, public feedback or clearance processing;
- employee popularity voting or public ranking;
- unrestricted employee/PDS/payroll data exports;
- email/SMS/push infrastructure; and
- unreviewed migration of legacy forms or historical award data.

## 11. Implementation gate

The user approved and completed the staged scope through Phase 8D. Final controls, interfaces,
reports, analytics, and acceptance evidence are recorded in
[PHASE_8D_CONTROLS_INTERFACES_ACCEPTANCE_REVIEW.md](./PHASE_8D_CONTROLS_INTERFACES_ACCEPTANCE_REVIEW.md).
The agency's currently approved PRAISE policy and prescribed forms remain deployment inputs; the
software supplies configurable operational records and does not claim that a generic report is an
agency-approved or current CSC/HAP form. Implementation is stopped before Succession Planning.
