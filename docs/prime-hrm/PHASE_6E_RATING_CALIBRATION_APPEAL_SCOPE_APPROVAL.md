# Phase 6E Rating, Calibration, Acknowledgment, Appeal, and Intervention Scope Approval

Prepared: 2026-09-24

Status: Approved; Phase 6E.3 complete and verified, stopped before Phase 6E.4

Authority: ISOFT PRIME-HRM Master Plan V2 Phase 6E, the completed Phase 6B rating definitions, Phase 6C approved commitments, and Phase 6D monitoring/coaching contracts

## 1. Outcome and controlled delivery sequence

Phase 6E turns a frozen Phase 6D accomplishment set into an auditable performance rating. It must reuse the exact published rating scale, success-indicator dimensions/rubrics, template weights, approved commitment version, monitoring evidence, stored supervisor route, and policy/cycle milestones. It must never accept a browser-calculated item or overall score as authoritative.

Delivery is split into four gated runs:

1. **6E.1 rating foundation and deterministic computation:** rating-ready accomplishment acceptance, exact source snapshot, optional self-assessment, assigned-supervisor assessment, Q/E/T or agency-defined dimension evaluation, weighted calculation, corrections, permissions, audit, OpenAPI, and paired V33 migrations.
2. **6E.2 PMT calibration and finalization:** policy-gated calibration cases, PMT segregation of duties, explicit item-level calibration proposals, recomputation, final-rating approval, immutable final result, and paired V34 migrations.
3. **6E.3 acknowledgment, appeal, and intervention:** receipt acknowledgment, authorized non-acknowledgment closure, policy/window-gated appeals and decisions, successor final-rating revisions, configurable low-rating intervention rules, and non-disciplinary PIP follow-through with paired migrations.
4. **6E.4 controls, interfaces, and acceptance:** Administrative permissions, PrimeHR rating/calibration/appeal/intervention workspaces, Employee Portal self-assessment/final-rating acknowledgment/appeal/PIP views, and Playwright acceptance.

Each backend run must pass focused, full regression, OpenAPI, audit, and PostgreSQL/SQL Server migration gates before the next begins. Phase 6E.4 remains separately approval-gated after the backend runs. Stop before Phase 6F reporting and downstream feeds.

## 2. Investigation findings and adjudications

- The immutable Phase 6B scale, bands, indicator dimensions, rubric levels, section/item weights, rounding scale, and rounding mode are suitable as the only calculation vocabulary. Existing preview services are definition-time tools; Phase 6E needs a persisted transaction calculator that resolves those exact server-side versions.
- A Phase 6D monitoring case currently reaches `ACCOMPLISHMENT_SUBMITTED` but has no explicit supervisor acceptance method that creates a rating-source snapshot. Phase 6E.1 must add this transition and atomically freeze the exact active update/evidence lineage used for rating.
- Monitoring updates carry accomplishment narrative/value and progress, but they do not contain separate Quality, Efficiency, Timeliness, or agency-defined dimension actuals. Phase 6E rating assessments must capture actual values or manual-rubric selections per configured dimension. Progress percentage must never be reused as a score.
- Multiple periodic updates may exist for one commitment item. Rating opening must deterministically snapshot the latest non-superseded submitted/accepted update for each required item by reporting date and creation order, expose the selection for review, and reject an ambiguous or incomplete set. The assigned supervisor's acceptance confirms that exact set.
- Self-assessment is created only when the exact policy requires it. It is an employee input, never the authoritative final rating, and becomes visible to the assigned supervisor only after submission.
- The assigned supervisor from the immutable commitment route is the rater. Agency-wide permission alone does not silently replace the stored rater; an exceptional reassignment requires a separately audited reason and authority.
- Calibration, acknowledgment, appeal, and low-rating intervention are controlled by the exact policy and cycle milestones. No label text or hard-coded numeric cutoff may decide that a rating is low.
- Existing rating bands do not identify intervention behavior. Phase 6E.3 therefore needs an immutable intervention rule bound to exact policy/rating-scale/band IDs instead of inferring from labels such as “Unsatisfactory.”

## 3. Phase 6E.1 — rating foundation and computation

### 3.1 Rating case and source snapshot

- One active rating case is opened idempotently per monitoring case/accomplishment revision.
- Opening requires an approved commitment, completed approval route, `ACCOMPLISHMENT_SUBMITTED` monitoring case, complete required evidence, and the configured `RATING_DUE` window.
- The assigned supervisor accepts the exact accomplishment set and moves monitoring to `READY_FOR_RATING` atomically with rating-case creation.
- The case snapshots the commitment, policy, template, rating scale, organization, owner, supervisor, route/content fingerprints, accomplishment revision, selected update IDs, evidence checksums, and calculation-definition fingerprints.
- Later definition retirement or successor publication cannot change an existing rating calculation.

### 3.2 Self and supervisor assessments

- Assessment types are `SELF` and `SUPERVISOR`; only one current revision of each type exists per rating case.
- The employee may create/submit `SELF` only when the exact policy requires self-assessment and the feature permission grants the action on own records.
- The stored assigned supervisor may create/submit `SUPERVISOR` on assigned records. A supervisor cannot submit the employee's self-assessment.
- Draft assessments are editable by their author. Submitted assessments are immutable; corrections create a linked successor revision and preserve the prior calculation.
- Each assessment provides one actual per configured indicator dimension: numeric/date/boolean actual where the rubric is threshold-based, or an exact permitted band selection where it is manual-rubric based.
- Narratives and evidence references support the assessment but never replace required numeric/rubric inputs.

### 3.3 Authoritative calculation

- The backend maps every dimension actual to exactly one rubric level and rating band.
- Dimension contribution is `band score × dimension weight ÷ 100`.
- Item score is the sum of its dimension contributions, rounded only according to the exact rating-scale rules.
- Section and overall scores use the exact immutable template/commitment weights and the established weighted-average formula.
- Missing, duplicate, out-of-range, unmatched, or multiply matched values fail without persistence.
- The result stores raw actuals, matched rubric/band IDs, unrounded contributions, rounded item/section/overall scores, adjectival band, formula version, and fingerprints. The client cannot supply any calculated score or status.
- No supervisor override, calibration adjustment, final rating, acknowledgment, appeal, alert, or PIP exists in 6E.1.

## 4. Phase 6E.2 — PMT calibration and finalization

- Calibration is required only when the exact policy says so; otherwise an authorized finalizer may advance the submitted supervisor assessment directly through the configured finalization workflow.
- A calibration case snapshots the submitted supervisor assessment and effective PMT roster.
- Only effective PMT members with exact calibration permission participate. Secretariat/technical-support roles remain non-voting; a conflicted subject/rater cannot decide their own case.
- Calibration cannot type an overall score. Any proposed change is item/dimension-specific, selects a valid rubric result or corrected actual, requires rationale, and triggers full backend recomputation.
- Original supervisor and self-assessment results remain visible and immutable beside the calibrated proposal.
- Final approval requires the configured `CALIBRATION_DUE`/`FINALIZATION_DUE` windows, quorum/decision rules defined by the effective policy/PMT contract, optimistic version, and idempotency key.
- The final rating is an immutable snapshot with rating band, decision actors, calculation lineage, and audit history. Reopening requires a controlled successor, never an in-place edit.

## 5. Phase 6E.3 — acknowledgment, appeal, and intervention

### 5.1 Acknowledgment

- The employee sees only their authenticated final rating and may acknowledge receipt with an optional response when required by policy.
- Acknowledgment means receipt, not agreement, waiver, or acceptance of the result.
- Closing without acknowledgment requires agency-wide permission, effective PMT authority, a reason, and audit evidence.

### 5.2 Appeal

- Appeal filing exists only when policy requires it and the `APPEAL_DUE` window is open.
- The employee files against an exact final-rating version with grounds, requested remedy, and secured evidence. The browser cannot select another employee.
- Appeal review uses effective, non-conflicted PMT authority and controlled outcomes such as `UPHELD`, `MODIFIED`, `REMANDED`, or `DISMISSED` with reasons.
- A modified result creates a successor final-rating revision and reruns the authoritative calculation. The original result and acknowledgment remain historical.

### 5.3 Low-rating intervention and PIP

- Immutable intervention rules map exact rating-band IDs to no action, alert, recommended PIP, or required PIP. Labels and numeric constants are never used as hidden cutoffs.
- Finalization evaluates the exact rule idempotently. It may create an auditable alert/PIP draft but never a disciplinary record, payroll effect, appointment action, promotion block, or automatic L&D/R&R/RSP decision.
- A PIP records objectives, support, accountable parties, dates, checkpoints, employee receipt, completion/closure, and immutable correction history. It reuses monitoring/coaching evidence where authorized but does not overwrite it.

## 6. Authority, scope, privacy, and segregation

| Feature key | Actions | Allowed data scopes |
|---|---|---|
| `primehr.performance-rating` | Access, Add, Edit, Submit, Approve | Own Records, Assigned Records, Agency Wide |
| `primehr.performance-calibration` | Access, Add, Edit, Submit, Approve, Finalize | Agency Wide plus effective PMT process role |
| `primehr.performance-appeal` | Access, Add, Edit, Submit, Approve, Finalize | Own Records, Assigned Records, Agency Wide |
| `primehr.performance-intervention` | Access, Add, Edit, Submit, Approve, Finalize | Own Records, Assigned Records, Agency Wide |

- Employee Portal forces `OWN_RECORDS` and derives identity from authentication.
- Supervisor authority comes from the immutable Phase 6C/6D route snapshot, not a client employee number.
- PMT authority requires both the exact action permission and effective process membership.
- Authors cannot approve/finalize their own exceptional override, calibration adjustment, appeal decision, or PIP closure where segregation is required.
- Private calibration deliberation and restricted intervention notes are filtered from employee responses; employee-visible reasons are stored separately.
- Every write uses optimistic locking, idempotency where transitions may be retried, and a complete audit event.

## 7. Persistence, portability, and API direction

- Paired PostgreSQL/SQL Server forward migrations begin at V33 and use equivalent constraints, indexes, precision, temporal types, status vocabularies, and tenant keys.
- Proposed 6E.1 persistence: rating case, source item snapshot, assessment revision, dimension actual/result, item/section/overall result, and rating action history.
- Proposed 6E.2 persistence: calibration case, proposal/result, participant/decision snapshot, and immutable final-rating revision.
- Proposed 6E.3 persistence: acknowledgment, appeal/evidence/decision, intervention rule, alert, PIP, checkpoint, and action history.
- No provider-specific shared Java query, cross-domain database access, destructive migration, or `ddl-auto` mutation is allowed.
- REST stays under `/api/primehr/v1/performance-management`; DTOs expose safe calculation lineage, never JPA entities, storage paths, private deliberation, or client-controlled authority/status/score.

## 8. Acceptance gates

### 6E.1 gates before 6E.2

1. Rating opening accepts and snapshots one complete exact accomplishment revision idempotently.
2. Policy-gated self assessment and assigned-supervisor assessment preserve author, ownership, correction lineage, and immutable submissions.
3. Q/E/T/agency dimensions and manual rubrics calculate deterministically from server definitions with exact rounding and no browser-supplied scores.
4. Permission, scope, route, window override, audit, concurrency, idempotency, OpenAPI, V33 parity/fresh/upgrade, focused/full tests, and package gates pass.
5. No calibration, final rating, acknowledgment, appeal, intervention, report, or Phase 6F behavior exists.

### 6E.2 gates before 6E.3

1. Policy-gated calibration, effective PMT roles, conflicts, quorum/decision evidence, and item-specific recomputation pass.
2. Non-calibration and calibration-required paths both produce immutable final ratings only through authorized finalization.
3. Original/self/supervisor/calibrated/final histories remain distinct and reproducible.
4. V34 parity/fresh/upgrade, OpenAPI, audit, concurrency, focused/full tests, and package gates pass.
5. No acknowledgment, appeal, PIP, downstream feed, report, or Phase 6F behavior exists.

### 6E.3 gates before 6E.4

1. Receipt acknowledgment and authorized non-acknowledgment closure preserve semantics and history.
2. Policy/window-gated own-record appeal, non-conflicted decision, secured evidence, and successor final-rating revision pass.
3. Exact-band intervention rules and idempotent alert/PIP creation work without hidden thresholds or CoreHR side effects.
4. Provider, OpenAPI, audit, privacy, concurrency, focused/full tests, and package gates pass.
5. No Jasper/analytics or L&D/R&R/RSP/Payroll/Timekeeping/HRM mutation exists.

### 6E.4 final gates

1. Administrative, PrimeHR, and Employee Portal controls exactly match backend action, scope, ownership, process-role, lifecycle, and privacy enforcement.
2. Type-check, lint, production builds/packages, focused Phase 6E Playwright, and complete feasible browser regression pass.
3. Rating math, source/result comparison, history, stale conflict, and no-client-score behavior are inspectable in the UI.
4. Deployment/rollback, live-provider limitations, user guide, and Phase 6F stop boundary are current.

## 9. Explicit exclusions and stop boundary

Phase 6E does not implement Jasper/PDF or analytics reports; organization-wide performance dashboards; competency/L&D/R&R/RSP/promotion feeds; payroll, timekeeping, appointment, or disciplinary effects; AI-generated scoring; arbitrary scripts/formulas; electronic signatures; notifications; legacy transaction import; or Phase 7 behavior. Those require Phase 6F or a separately approved scope.

## 10. Approval semantics

Approval progressed through Phase 6E.3 after the Phase 6E.1 and 6E.2 gates passed. Stop before Phase 6E.4 until Administrative controls, PrimeHR/Employee Portal interfaces, and Playwright acceptance receive separate approval. Stop before Phase 6F.
