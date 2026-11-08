# Phase 7 - Competency-Based Learning and Development Scope Approval

Prepared: 2026-10-25  
Status: Approved; Phase 7D.1 complete and stopped before Phase 7D.2

## 1. Objective and boundary

Phase 7 will implement the Learning and Development (L&D) domain owned by PrimeHR. It will
translate verified competency, performance, organizational, and employee development needs into
approved individual and organizational plans, designed interventions, scheduled offerings,
authorized participation, verified delivery, evaluation, and auditable outcomes.

Phase 7 does not replace HumanResource Personal Data/PDS training history. It does not automatically
change a competency level, performance rating, appointment, payroll result, work schedule, reward,
promotion decision, or PDS record. Those remain owned by their authoritative domains and require a
separate controlled handoff or human decision.

## 2. Evidence established during discovery

### Current ISOFT HRIS

- PrimeHR already owns the Phase 4 manual `prime_ld_referral` and `prime_ld_referral_item` records.
  `REFERRED` means queued for future L&D intake only; it is not an accepted IDP, training request,
  funding decision, enrollment, or completion.
- HumanResource `learninganddevelopment` stores Personal Data/PDS history fields: program name,
  inclusive dates, hours, type, and conducting organization. It has no plan, nomination, delivery,
  evaluation, or competency-outcome lifecycle.
- The HRM Personal Data screen directly maintains those historical entries. PrimeHR must not write
  that table or use it as its operational L&D aggregate.
- PrimeHR already provides reusable foundations for immutable source snapshots, authorization and
  data scope, audit, optimistic locking, idempotency, secured evidence storage, Jasper bean reports,
  dual-provider Flyway migrations, OpenAPI, and packaged-frontend Playwright.
- V36 is the latest PrimeHR schema migration; Phase 7 migrations therefore begin at V37.

### Legacy HRBliz

The legacy application contains an employee training record, a small training-name master, group
assignment by section, portal-to-HRM approval of self-declared training, lecturer/support-staff lists,
and a Hospital Training Order report.

Useful concepts to retain:

- title, inclusive dates, hours, intervention type, sponsor/provider;
- individual and group participants;
- lecturers/resource persons and implementation support staff;
- employee confirmation and HR validation of external completion evidence; and
- an agency training/office order generated for one approved activity.

Legacy behavior that is not authoritative and will not be copied:

- concatenating master name and remarks into one mutable text value;
- using a nullable `isNewTraining` flag as the workflow;
- writing planned attendance directly into permanent PDS history;
- hard deletion of training history;
- lecturer/support rows that are global rather than attached to an activity revision;
- string-concatenated searches and weak/no foreign-key relationships;
- manual report inputs that are not persisted with the approved activity; and
- hard-coded ZCMC order number, venue/time, contact details, and developer web paths in JRXML.

## 3. Philippine government baseline

The implementation baseline is configurable agency policy aligned with current official guidance:

- CSC Competency-Based L&D describes a lifecycle from competency-based learning-needs assessment,
  through design and delivery, to evaluation. It treats L&D as broader than classroom training and
  includes coaching, job rotation, self-development, scholarships, special projects, and similar
  interventions: <https://www.csc.gov.ph/programs/competency-based-learning-and-development-program>
- The CSC CBLD Management System links competency and performance results to IDPs, consolidates IDPs
  into office/agency plans, requires evaluation to be designed with the intervention, and describes
  Reaction, Learning, Behavior, Results, and Return-of-Expectation evaluation levels:
  <https://www.csc.gov.ph/phocadownload/userupload/csi/CBLDP/cbldms.pdf>
- The 2025 ORAOHRA defines training broadly as formal/non-formal HRD interventions tied to an IDP or
  Career Development Plan, calls for at least one planned intervention per employee per year, and
  specifies at least 40 hours of supervisory/management L&D annually for applicable incumbents:
  <https://www.csc.gov.ph/phocadownload/userupload/hrpso/ORAOHRA/CSC%20Resolution%20No.%202500358%20ORAOHRA.pdf>
- CSC Resolution No. 2600005 provides the current competency-based HRMS direction and sample
  competency artifacts: <https://www.csc.gov.ph/downloads/category/562-csc-reso-2600005>
- PRIME-HRM accreditation activity was temporarily deferred in July 2026, and CSC has since posted
  MC No. 11, s. 2026 and PRIME-ESG implementing materials. Phase 7 must keep maturity labels,
  evidence checklists, and agency workflows configurable rather than hard-code a superseded
  accreditation checklist: <https://csc.gov.ph/downloads/category/600-mc-no-11-s-2026>

The CSC documents are a policy/design baseline. Agency-approved L&D policy, forms, delegation,
funding rules, signatories, and later CSC/PRIME-ESG guidance remain authoritative configuration.

## 4. Source ownership and integration

| Information | Authority | Phase 7 treatment |
|---|---|---|
| Employee identity, appointment, position, office | HumanResource | authenticated read and immutable source snapshot |
| Area, Business Unit, Job Position, Plantilla, settings, branding | Administrative | authenticated read; never duplicated as editable masters |
| Permission rules and approval workflow configuration | Administrative | exact feature/action/data-scope enforcement and route snapshot |
| Competency profiles, assessments, gaps and referrals | PrimeHR Competency | exact immutable references; referral intake is an explicit decision |
| Final performance outcomes/development plans | PrimeHR Performance | read-only source reference; never automatic L&D approval |
| L&D policy, IDP, plan, program, offering, participation and evaluation | PrimeHR L&D | owned, versioned, audited and provider-neutral |
| PDS L&D/training history | HumanResource | remains CoreHR-owned; no direct PrimeHR table write |
| Work schedule/attendance | TimeKeeping | later authorized read/handoff only; no Phase 7 schedule mutation |
| Payment, reimbursement and accounting | Payroll/Finance outside PrimeHR | excluded from Phase 7 |

Cross-domain access uses authenticated typed APIs only. PrimeHR never joins or updates another
module's tables. Every mutable-source reference needed for history is frozen as an ID/version,
human-readable snapshot, fingerprint, and capture time.

## 5. Planned domain lifecycle

### Needs and Individual Development Plan

Allowed need sources are explicit: competency gap/referral, performance-development outcome,
organizational/office need, supervisor observation, employee career-development goal, statutory or
professional requirement, and other reasoned manual need. No algorithm automatically accepts a
source or prescribes an intervention.

A Phase 4 referral intake records `ACCEPTED`, `RETURNED`, or `DECLINED` with actor, reason, exact
referral version, and optional linked IDP item. It does not rewrite the immutable Phase 4 referral.

Proposed IDP lifecycle:

```text
DRAFT -> SUBMITTED -> RECOMMENDED -> APPROVED
                    -> RETURNED -> SUBMITTED
DRAFT/RETURNED -> CANCELLED
APPROVED -> successor amendment; never overwrite approved history
```

Each item identifies the development objective, source need, targeted competency where applicable,
current/target state, intervention category, success measure, target date, employee and supervisor
commitment, priority, and plan status. The configured approval route is snapshotted at submission.

### Consolidated plans and intervention design

Approved IDP needs and authorized organizational needs may be consolidated into office and agency
L&D plans. Plans are versioned by planning period and retain source coverage, priorities, intended
participants, target outcomes, delivery strategy, schedule window, estimated resources/cost, funding
source, accountable implementer, and approval history.

An intervention catalog is versioned and may represent classroom, online/blended, coaching,
mentoring, job rotation, job shadowing, self-development, scholarship, special project, knowledge
sharing, or another agency-configured category. A catalog item maps intended outcomes to exact
competency versions where applicable; it is not a mutable free-text training master.

An offering is a scheduled approved instance with sessions, modality, venue/platform, capacity,
resource persons, support staff, provider, budget/cost facts, evaluation plan, requirements, and
implementation responsibility. Draft design is editable; approved/published revisions are immutable.

### Nomination, participation and delivery

Employees, supervisors, or authorized L&D staff may initiate a nomination according to policy. A
nomination must identify its IDP/plan or justified source, offering, employee snapshot, schedule,
estimated cost, and route. Approval does not mean attendance or completion.

Participation separately records invitation/confirmation, substitutions or cancellation with reason,
session attendance, credited hours, required outputs, evidence, completion decision, certificate
metadata, and verification. Capacity, duplicate enrollment, overlapping offerings, inactive
employment, stale appointment, and lifecycle conflicts fail before persistence.

The legacy Hospital Training Order becomes an immutable report of one approved offering and its
authorized participants/resource persons. Official-time or official-business consequences are not
silently written to TimeKeeping.

### Evaluation and outcomes

The intervention design selects required evaluation levels and timing. Phase 7 supports controlled
Reaction, Learning, Behavior/Application, Results, and Return-of-Expectation instruments without an
arbitrary executable formula engine. At minimum, evidence and scoring rules are versioned and frozen
with the offering.

Learning Action Plan/REAP/SAP-type follow-through is stored separately from attendance. Supervisor
behavior/application evaluation is scheduled according to the approved policy and records the
evaluator's process role and evidence.

Verified completion may expose a versioned, audited read-only outcome feed and an HRM-review packet.
It cannot directly insert PDS history, award a competency level, close a performance intervention,
grant a reward, or qualify an employee for promotion. Those require the owning domain's authorized
validation workflow.

## 6. Controlled implementation units

| Unit | Included | Required stop boundary |
|---|---|---|
| 7A.1 | L&D governance policy, intervention taxonomy/catalog, provider/resource-person registry, evaluation framework, V37 paired migrations, permissions, audit and OpenAPI | No IDP, plan, offering, nomination, UI or report |
| 7A.2 | Learning-needs intake, explicit Phase 4 referral disposition/link, IDP lifecycle/route/source snapshots, V38 paired migrations | No automatic referral acceptance, consolidation, offering or completion |
| 7B | Versioned office/agency L&D plans, source consolidation, program design and scheduled offerings, sessions/resources/capacity/budget facts, V39 paired migrations | No nomination, attendance or completion |
| 7C | Employee/supervisor nomination, configured approval route, enrollment/confirmation/substitution/cancellation, approved Training Order source, V40 paired migrations | No attendance, PDS write, competency update or evaluation result |
| 7D.1 | Session attendance, credited hours, outputs, secured certificate/evidence, verification and immutable completion, V41 paired migrations | No automatic downstream mutation |
| 7D.2 | Versioned evaluation instruments/results, LAP/follow-through, delayed supervisor evaluation, intervention/provider effectiveness, controlled read-only completion feed | No reward, promotion, competency re-rating or CoreHR write |
| 7E | Administrative controls, PrimeHR L&D workspaces, Employee Portal My Learning, bean-driven government/agency reports, privacy analytics, Playwright and documentation | Stop before Rewards and Recognition or Succession |

Each backend unit requires focused tests, full PrimeHR regression/package, OpenAPI/audit checks,
PostgreSQL/SQL Server migration parity, populated upgrade preservation, and fresh migration/Hibernate
validation before the next unit begins. Phase 7E requires all affected frontend lint, strict
TypeScript, production builds/packages, and packaged-frontend Playwright acceptance.

## 7. Proposed permission keys

| Feature key | Actions | Data scope |
|---|---|---|
| `primehr.ld-governance` | Access, Add, Edit, Delete/archive, Publish | Agency Wide |
| `primehr.ld-needs` | Access, Add, Edit, Submit, Approve | Own, Assigned, Agency |
| `primehr.ld-individual-plan` | Access, Add, Edit, Submit, Approve, Finalize | Own, Assigned, Agency |
| `primehr.ld-agency-plan` | Access, Add, Edit, Submit, Approve, Publish | Assigned, Agency |
| `primehr.ld-program` | Access, Add, Edit, Submit, Approve, Publish, Delete/cancel | Assigned, Agency |
| `primehr.ld-nomination` | Access, Add, Edit, Submit, Approve, Delete/cancel | Own, Assigned, Agency |
| `primehr.ld-delivery` | Access, Add, Edit, Submit, Approve, Finalize | Assigned, Agency |
| `primehr.ld-evaluation` | Access, Add, Edit, Submit, Approve | Own, Assigned, Agency |
| `primehr.ld-report` | Access | Own, Assigned, Agency according to report kind |
| `primehr.ld-analytics` | Access | Agency Wide |
| `primehr.ld-completion-feed` | Access | Agency Wide/service integration only |

The existing `primehr.ld-referral` permission remains the Phase 4 referral-creation authority. It
does not grant Phase 7 intake, IDP, program, nomination, or completion authority.

Backend enforcement combines exact action, data scope, authenticated employee, immutable route,
process role, lifecycle, source ownership, agency, and optimistic record version. Sidebar visibility
is never treated as authorization.

## 8. Planned interfaces and reports

PrimeHR workspaces are expected for L&D governance/catalog, Needs and IDPs, L&D Plans, Programs and
Offerings, Nomination Inbox, Delivery, Evaluation, and Reports/Analytics. Employee Portal **My
Learning** will expose only authenticated-own needs/IDPs, nominations, schedules, required outputs,
evaluation tasks, completion records, and eligible PDFs; it will have no employee selector.

Candidate bean-driven reports, finalized only after form/source comparison:

- Individual Development Plan;
- Office and Agency L&D Plan/implementation matrix;
- Training Activity Plan/Design Matrix;
- Hospital/Office Training Order;
- participant directory and attendance sheet;
- completion/certificate register and employee L&D record packet;
- L&D documentation/accomplishment report;
- evaluation summary and delayed application/follow-through report; and
- privacy-controlled plan, participation, completion, hours, cost, equity/access, provider, and
  effectiveness analytics.

Before each JRXML is implemented, compare the legacy template with current CSC/Philippine government
guidance and the agency-approved form. JRXML will contain layout only, use packaged resources and
typed bean data, and never contain provider-specific SQL or developer-machine paths.

## 9. Explicit exclusions

Phase 7 excludes:

- a general-purpose LMS, SCORM/xAPI course player, video conferencing, or course-content authoring;
- procurement, accounting, cash advance, travel order, reimbursement, payroll, or scholarship-bond
  accounting;
- automatic TimeKeeping schedules, official-business entries, or DTR changes;
- direct HumanResource/PDS training-history writes;
- automatic competency-profile changes or reassessment results;
- automatic performance, disciplinary, reward, promotion, RSP, or succession decisions;
- unreviewed demographic inference or employee ranking;
- AI-generated needs, nominations, ratings, or completion decisions;
- email/SMS/push notification infrastructure; and
- Rewards and Recognition, Succession Planning, or legacy-data migration beyond separately approved
  read-only analysis/remediation.

## 10. Gate to implementation

Phase 7 discovery and scope approval are complete. Phase 7A.1 delivered the L&D governance,
catalog, provider/resource-person, evaluation-framework, V37 migration, permission, audit, OpenAPI,
and backend verification foundation. Phase 7A.2 delivered learning-needs intake, explicit Phase 4
referral disposition and IDP-item linkage, the versioned IDP lifecycle and immutable submission-route
snapshots, V38 paired migrations, permissions, audit, OpenAPI, and backend verification. Phase 7B
delivered versioned office/agency plans, exact source consolidation, program design, scheduled offerings,
sessions/resources/capacity/budget facts, controlled approval/publication, and paired V39 migrations.
Phase 7C delivered routed nominations, controlled enrollment and immutable approved Training Order
sources with paired V40 migrations. Phase 7D.1 delivered Training Order-bound session attendance,
bounded credited hours, required outputs, secured evidence and certificate metadata, independent
verification, immutable completion, and paired V41 migrations. The implementation is stopped at its
verification gate before Phase 7D.2 evaluation results and controlled outcome feeds.
