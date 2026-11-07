# Government benefit classification review - 2026-10-12

## Status

The reusable 2026 guidance profiles and explicit annual benefit review are now
implemented. Automatic exemption allocation and automatic ceiling enforcement
are intentionally not implemented. Passing the DAT format validator does not
prove the underlying benefit tax treatment is correct.

The user confirmed that PERA is government Personnel Economic Relief Allowance
and the representation/transportation entries are government RATA. DOH Allowance
is agency-specific. No universal DOH exemption is assumed.

## Local master-data changes

Six previously NULL BIR item values were set in the configured local SQL Server
database, in one transaction with ID, code, existing taxable flag, and NULL-value
preconditions. Six affected rows were verified before commit. No taxable flag,
amount, other earning setting, or payroll history was changed.

| Existing entry ID | Accounting code | Existing taxable flag | BIR item assigned |
| --- | --- | --- | --- |
| 4 | Longevity | true | 44A |
| 9 | Salary Adjustment | true | 39 |
| 12 | PERA | false | 37 |
| 13 | Representation Allowance | false | 37 |
| 14 | Transportation Allowance | false | 37 |
| 15 | Hazard Pay | true | 49 |

Basic (39), Cell Card Allowance (44A), and Clothing Allowance (35) already had
user-entered mappings and were preserved. Clothing still requires ceiling checks.
The hazard-pay mapping follows the existing taxable non-MWE treatment; it does
not establish MWE classification or override a supported special-law exemption.

No agency-specific mapping was embedded in a universal installation migration.
For recovery, these six original BIR values were NULL; any restoration must first
check for subsequent user edits and preserve already computed payroll snapshots.

## Verified rules and implementation requirements

- Government PERA and qualifying GAA RATA: non-taxable under RR 11-2018;
  use item 37 in this application's compensation classification. Government
  PERA allowance is separate from the retirement-account PERA tax credit.
- Bonuses and qualifying other benefits: shared PHP 90,000 exemption per
  employee/tax year, not per earning code. Item 34 holds the exempt portion;
  taxable bonus excess belongs in item 48. Current mid-year/year-end taxable
  flags alone do not implement this allocation.
- RR 29-2025 revised de minimis thresholds include clothing PHP 8,000/year,
  laundry PHP 400/month, and qualifying overtime/night-shift meal allowance up
  to 30% of the regional basic minimum wage. Use effective dates, qualifying
  payment evidence, and actual amounts. These are tax ceilings, not grant rates.
- The same regulation provides a PHP 12,000 combined annual ceiling for
  qualifying CBA/productivity-incentive benefits. Confirm applicability of the
  actual CNA/PEI scheme; annual payment frequency alone is insufficient.
- Excess de minimis benefits enter the other-benefits pool, with the remaining
  PHP 90,000 exemption considered before determining taxable excess.
- DBM's civilian U/CA baseline is PHP 7,000/year; PEI baseline PHP 5,000 subject
  to eligibility and applicable yearly issuances. These do not replace BIR limits.
- Public-health-worker subsistence/laundry entitlement depends on RA 7305 and
  implementing rules. Entitlement alone does not establish non-taxability.
- Ordinary fixed meals, qualifying overtime meals, and on-duty subsistence need
  distinct explicit categories. The earning names cannot establish their facts.
- DOH Allowance requires the agency's actual benefit/legal basis. PhilHealth
  Honorarium requires confirmation of employee compensation versus professional
  fees. Leave both unclassified until those facts are established.

## Required verification before production acceptance

- Boundary and excess tests for each effective-dated ceiling; shared-pool tests
  across earning codes, periods and employee changes of employer.
- Monthly laundry and qualifying-meal evidence tests; reversals and adjustments.
- Preserve actual gross payments/withholding, show allocation and reconciliation,
  and snapshot the applicable rule/basis at annualization finalization.
- Existing finalized earning lines retain their saved classifications. Editing
  master data does not backfill them; correction needs an explicit audited workflow.
- SQL Server/PostgreSQL persistence and separate production JAR verification.

## Correction design agreed for implementation

The existing earning master has one taxable flag and one BIR 2316 item. Those
values are copied to each finalized payroll earning, and annualization sums the
copies. Changing the master later cannot correct historical lines. A partly
exempt payment (for example, a bonus above the shared exemption) therefore
cannot be represented correctly by changing only the earning-type dropdown.

The correction belongs in an explicit **annual benefit-classification review**:

1. Show each affected finalized earning and posted earning adjustment, its
   payroll amount, current BIR item and current taxable treatment. Keep the
   original payroll amount, withholding, and locked payroll untouched.
2. Let an authorized payroll officer enter the non-taxable and taxable amounts
   and their respective BIR items, plus a reason/legal basis. Never derive the
   split from an earning name or silently allocate it by a ceiling.
3. Require the two amounts to equal the signed source amount exactly, use
   decimal arithmetic, and reject a missing/invalid BIR item. If a source is
   changed or reversed after review, invalidate the review and require it again.
4. Apply the reviewed split only to annualization/2316 itemization and tax-due
   calculation. Audit author, time, old/new values and reason; prevent changes
   while the employee's annualization is finalized. Existing snapshots stay
   frozen until explicitly reopened/refinalized.
5. Configure reference ceilings by tax year, effective dates, benefit category,
   period (annual/monthly/per qualifying event), shared-pool group, amount and
   legal basis. These are officer-visible review references, not automatic
   allocation or automatic ceiling enforcement. Do not prepopulate agency-
   specific entitlement amounts as BIR tax ceilings.
6. An unresolved or stale required review blocks finalization/export. A
   completed review must reconcile source, period and annual Form 2316 totals.

Items needing agency evidence before any tax default is assigned: the DOH
allowance, PhilHealth honorarium, whether the actual CNA/PEI scheme qualifies
for a de minimis category, and whether a meal payment is qualifying overtime/
night-shift food rather than an ordinary allowance. Previous-employer benefit
breakdown is unavailable in the current annualization model, so a shared
exemption involving previous-employer benefits must remain an explicit officer
review instead of an inferred remainder.

The review now accepts a primary exempt portion, an optional other-benefits
exempt portion (Form 2316 item 34), and a taxable portion. Source reconciliation,
valid BIR items, a review reason, and an assigned-profile review are enforced
before finalization. The profile's legal basis and guidance ceiling are copied
into the review for audit. See ANNUALIZATION_BENEFIT_REVIEW_SIMULATION.md for
the officer's manual procedure. Remaining limitations include no automatic
ceiling enforcement and no structured prior-employer benefit itemization.

## Primary references

- BIR RR 11-2018: https://bir-cdn.bir.gov.ph/local/pdf/RR%20No.%2011-2018.pdf
- BIR RR 29-2025: https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025.pdf
- BIR RR 29-2025 digest: https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025%20digest%20FINAL.pdf
- BIR Form 2316: https://bir-cdn.bir.gov.ph/local/pdf/2316%20Sep%202021%20ENCS_Final_corrected.pdf
- DBM NBM 156 (budget preparation baseline, not a blanket grant authority): https://www.dbm.gov.ph/wp-content/uploads/Issuances/2026/National-Budget-Memorandum/NATIONAL-BUDGET-MEMORANDUM-NO-156.pdf
- DBM PEI guidance: https://www.dbm.gov.ph/index.php/central-office?catid=196&id=960&view=article
- DBM FY2025 CNA circular (not automatic FY2026 authority): https://www.dbm.gov.ph/index.php?catid=386&id=3716%3Abudget-circular-no-2025-2&view=article
- RA 7305: https://lawphil.net/statutes/repacts/ra1992/ra_7305_1992.html
