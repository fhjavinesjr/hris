# Payroll Annualization Roadmap

This note records the implementation order and prevents later BIR/report work from
silently changing the payroll source-of-truth rules.

## Implemented foundation

- Annualization reads finalized salary periods identified by `payroll_period_lock`.
- Cancelled payroll details and non-posted adjustments are excluded.
- Present-employer amounts come from persisted payroll detail earning/deduction rows.
- Annual tax tables are maintained by tax year in Administrative.
- Previous-employer BIR 2316 totals are stored separately and included in combined
  taxable compensation, tax withheld, annual tax due, and adjustment.
- Finalization stores versioned summary, period, and previous-employer snapshots.
- Reopening voids the active snapshot without deleting its historical values.
- Finalize, reopen, and previous-employer changes create metadata-only audit events.
- A finalized non-zero result can create one pending `WTX_ANNUALIZATION` payroll
  adjustment in an open computed period from the same tax year.
- Existing pending adjustment lines are preserved when annualization creates a new
  version; an existing posted adjustment is never superseded.
- The finalized snapshot records the target period and adjustment header, preventing
  duplicate application and unsafe reopening after downstream payroll creation.
- All money used by the annualization service and previous-employer storage uses
  `BigDecimal` with two-decimal output rounding.
- Persistence access uses Spring Data derived queries/JPQL. Provider-specific DDL is
  isolated in separate PostgreSQL and SQL Server migration scripts.
- Each production microservice owns its versioned schema upgrades: Administrative,
  Human Resource, and Payroll record migrations in their own
  `hris_administrative_schema_history`, `hris_human_resource_schema_history`, and
  `hris_payroll_schema_history` tables. SQL Server or PostgreSQL scripts are selected
  from the live datasource, so deployment and installation do not require manual
  execution. HRISApp retains the combined paths only for integration testing.
- The official BIR Form 2316 baseline is the September 2021 ENCS form.
- Administrative Earning Types can be assigned an authoritative BIR Form 2316 item.
  The selected item is validated against the earning's taxable setting and copied to
  computed payroll earning lines so later configuration changes do not rewrite history.
- Existing and newly created earning types may remain `Not classified`; no BIR item is
  guessed from the earning name or flags.
- Employer and employee TIN, RDO, and address information is read from Administrative
  Settings and Human Resource Personal Data through lightweight bulk endpoints.
- Previous-employer identity includes the employer address and ZIP code.
- Annualization lists exact missing identity and persisted earning-classification fields,
  marks the result `FOR_REVIEW`, and prevents finalization until they are complete.
- Finalization snapshots employer and employee tax identity so later master-data edits do
  not rewrite a completed annualization version.
- Earning adjustment lines snapshot the authoritative BIR item from the original computed
  earning or configured Administrative Earning Type. Unclassified earnings cannot be
  posted, and older posted unclassified lines keep Annualization under `FOR_REVIEW`.
- A typed September 2021 ENCS item 29-52 compensation dataset is calculated from finalized
  payroll earnings and posted adjustments. Item 36 uses actual employee-share mandatory
  contributions and union dues, while items 38 and 52 are reconciled totals.
- Finalization persists every BIR compensation item in `annualization_snapshot_item`, so
  report values do not change when earning configuration is edited later.
- Employee birth date, contact number, registered/local address, and the position resolved
  from the active HR appointment and Administrative Job Position master are included in
  Annualization readiness and copied to the finalization snapshot.
- Administrative owns effective-dated statutory minimum-wage rates by region, including
  daily/monthly amounts and legal basis. Human Resource owns explicit effective-dated
  employee MWE status by region. Both reject overlapping date ranges and are maintained
  through permission-protected APIs, without inferring status from salary.
- Every annualized payroll period resolves the employee's explicit MWE status and, when
  applicable, the regional statutory rate effective on its salary date. Missing, incomplete,
  or overlapping coverage is reported against the exact payroll period and blocks finalization.
- Taxable flags do not override the statutory exemption for persisted BIR items 29-33 when
  the employee is explicitly MWE for that period. The resolved status, region, daily/monthly
  rates, and legal basis are stored on each finalization snapshot period.
- The Annualization detail shows the MWE basis per payroll period. A missing-status warning
  offers a compact permission-controlled setup action with an explicit classification,
  authoritative configured region, and user-selected effectivity dates.
- A bean-backed, provider-neutral BIR Form 2316 PDF is generated only from finalized
  snapshots. The one-page September 2021 ENCS template contains items 1-52, renders the
  authorized substituted-filing assessment while leaving physical signatures blank, records a generation
  audit event, and is available from the employee detail without adding screen clutter.
- Form 2316 generation rejects multiple previous-employer records because the official
  certificate has one previous-employer identity section; it will not choose or merge
  employer identities silently.
- Personal Equity and Retirement Account (PERA) evidence is maintained separately from
  the government PERA allowance. Payroll stores one verified annual certificate reference,
  contribution amount, accredited administrator, and issue date per employee/tax year;
  the server calculates the statutory 5% credit and snapshots both the evidence and the
  applied amount at finalization. The applied amount is capped at remaining tax so it
  cannot create or increase a refund.
- Finalization requires an authorized substituted-filing assessment and stored basis/reason.
  A qualified result is rejected when previous-employer compensation exists or reconciled
  tax due does not equal total withholding after applicable tax credits. The reviewer must
  still verify the employee's pure-compensation and one-employer declarations because those
  facts cannot be inferred safely from payroll data alone. The result, reason, reviewer, and
  review time are frozen in the final snapshot and rendered conditionally on Form 2316.
- A read-only 1604-C/Alphalist review preview uses only finalized snapshots. It shows
  employer identity, employee TIN, finalized payroll coverage, compensation, actual tax
  withheld, tax due, and substituted-filing status, with row-level and dataset-level
  blockers. Export is rejected server-side whenever any record or dataset blocker remains.
- The permission-controlled export action downloads an ASCII, CRLF-terminated BIR v7.4
  `1604C.DAT` containing the official header, Schedule 1/2 detail records, and reconciled
  control records. The combined representative file passed the official Alphalist
  Validation Module v7.4 with `No Errors Encountered` for both schedules.
- Employer/employee branch codes and the employee BIR region are explicit master-data
  fields. Human Resource presents the official region choices, and Administrative Nature
  of Appointment records expose the official `R`, `C`, `CP`, `S`, `P`, and `AL` mapping.
  Obvious existing appointment names are migrated automatically; ambiguous names remain
  unassigned for deliberate Administrative review.
- Human Resource supplies tax-year-specific structured legal names and nationality from
  Personal Data, plus employment coverage derived from appointment and separation history.
  Payroll resolves the appointment nature through Administrative master data and freezes
  these values in the finalized snapshot. Payroll-period coverage remains separately visible
  so it cannot be mistaken for legal employment coverage.

## Source mapping

| Annualization value | Current source |
| --- | --- |
| Present gross compensation | `payroll_detail.grossAmount` plus posted earning adjustments |
| Present taxable earnings | Taxable `payroll_detail_earning` plus taxable posted earning adjustments |
| GSIS / PhilHealth / Pag-IBIG | Actual finalized deduction lines and posted deduction adjustments |
| Present taxable compensation | Taxable earnings less actual mandatory contributions |
| Present tax withheld | Actual WTX deduction lines and posted WTX adjustments |
| Previous-employer totals | `previous_employer_tax_data`, encoded from prior BIR Form 2316 |
| Annual tax due | Tax-year-specific Administrative annual tax brackets |
| Adjustment | Annual tax due less combined present/previous tax withheld and applied PERA credit |
| Finalization status | Versioned `annualization_snapshot` record |
| BIR compensation item | `payroll_detail_earning.bir2316ItemNo`, copied from Administrative Earning Type |
| Adjustment BIR compensation item | `payroll_adjustment_line.bir2316ItemNo`, resolved server-side from the original earning or Administrative Earning Type |
| BIR present-employer items 29-52 | Typed `Bir2316CompensationItemsDTO`, persisted in `annualization_snapshot_item` at finalization |
| Employer TIN/RDO/address snapshot | Administrative Settings, copied to `annualization_snapshot` at finalization |
| Employee TIN/RDO/address snapshot | Human Resource Personal Data, copied to `annualization_snapshot` at finalization |
| Previous-employer identity | `previous_employer_tax_data`, copied to the annualization snapshot |
| Employee birth/contact/address snapshot | Human Resource Personal Data, copied to `annualization_snapshot` at finalization |
| Employee position snapshot | Active Human Resource appointment resolved through Administrative Job Position, copied to `annualization_snapshot` at finalization |
| Minimum-wage statutory rates | Administrative `minimum_wage_rate`, effective-dated by region |
| Employee minimum-wage-earner status | Human Resource `employee_mwe_status`, explicitly effective-dated by employee |
| Finalized period MWE basis | `annualization_snapshot_period`, including status, region, daily/monthly rates, and legal basis |
| BIR Form 2316 items 1-52 | Finalized identity, summary, period, previous-employer, and typed compensation snapshots |
| PERA 5% tax credit (Form 2316 item 27) | Verified `annual_pera_tax_credit` evidence; 5% calculated server-side and capped at remaining tax |
| Substituted-filing result/reason | Authorized finalization assessment, validated against employer history and tax reconciliation, then stored in `annualization_snapshot` |
| 1604-C/Alphalist review preview | Finalized annualization summary and period snapshots; no live recomputation and no provider-specific SQL |
| Structured legal name / nationality | Human Resource Personal Data, copied to the annualization snapshot at finalization |
| Employment coverage | Tax-year-bounded Human Resource appointment start and separation date, copied to the annualization snapshot |
| BIR employment status | Human Resource appointment ID resolved through the explicit Administrative Nature of Appointment mapping (`R`, `C`, `CP`, `S`, `P`, or `AL`), copied to the snapshot |
| 1604-C schedule and MWE region | Derived from finalized period MWE status; Schedule 2 uses the single authoritative wage-region code already frozen per period |
| 1604-C year-end adjustment | Snapshot prior/present withholding plus PERA credit, December/last-salary additional withholding, refund, and adjusted withholding reconciled to annual tax due |
| BIR v7.4 `1604C.DAT` | Finalized Alphalist preview snapshots written as `H1604C`, `D1`/`C1`, and `D2`/`C2` records; export is blocked unless the preview is clean |

## Required next phases

0. **Government benefit tax treatment - officer-reviewed correction implemented**
   - Administrative now stores tax-year benefit profiles and BIR ceilings as
     guidance. The 2026 references are seeded by automatic migration. Exact
     clothing, laundry and bonus earning names are linked without changing
     their payroll tax flags or historical amounts.
   - Annualization now requires an officer-reviewed split for linked benefits:
     primary exempt, optional shared-pool exempt, and taxable portions. It
     reconciles the paid amount and Form 2316 items, audits the review, blocks
     unresolved/stale reviews, and preserves locked payroll and snapshots.
   - Automatic benefit allocation and ceiling enforcement are intentionally
     **not** part of this workflow. Officers must check shared annual/monthly
     amounts, qualifying evidence and previous-employer Form 2316 data.
   - Keep grant eligibility/amounts separate from BIR tax exemption thresholds.
     A statutory permission to pay a benefit does not establish its tax exemption.
   - Research, confirmed local mappings, unresolved agency facts, and manual
     simulation steps are recorded in `GOVERNMENT_BENEFIT_CLASSIFICATION_REVIEW.md`
     and `ANNUALIZATION_BENEFIT_REVIEW_SIMULATION.md`.

1. **Remaining BIR Form 2316 control**
   - Decide the reviewed handling for employees with more than one previous employer.

2. **Separation annualization**
   - Reuse the same engine with a separation cutoff date.
   - Read authoritative HR separation data; do not approve separation in Payroll.

3. **Scale and acceptance testing**
   - Replace full-year detail loading with database aggregation if production volumes
     show it is needed.
   - Exercise SQL Server and PostgreSQL profiles, role permissions, multiple tax years,
     previous employers, reopen behavior, Jasper output, and BIR export validation.

## Classification rule

Do not infer BIR categories such as de minimis or 13th-month/other benefits from an
earning name. The authoritative item is maintained on the earning configuration and
snapshotted on each computed payroll earning line. Item 36 (mandatory contributions and
union dues) comes from finalized deduction data rather than Earning Type configuration.
