# Annualization benefit-classification simulation (2026)

This checklist continues the existing **Verify earning classifications** step.
Use a test employee and finalized **test** payroll periods. Do not finalize or
export a production employee until the agency's payroll/tax officer has checked
the actual BIR treatment and prior-employer evidence.
If an employee's annualization was already finalized before this correction,
an authorized officer must **reopen** it, complete the benefit review, and
finalize a new snapshot. The existing finalized snapshot is not silently
rewritten by an Administrative profile change.

## 1. Deploy/restart the separate services

Deploy the updated **Administrative** and **Payroll** JARs and their frontends.
Their normal Flyway startup migrations create the profile/review tables for SQL
Server or PostgreSQL and seed four 2026 guidance profiles. No manual DB script
is needed. HRISApp is not the production deployment target.

## 2. Verify Administrative profiles and earning links

Open **Administrative → Earning Types**. In **Annual benefit tax profiles**,
select **2026** and verify:

| Profile | Guidance ceiling | Exempt BIR item | Taxable BIR item |
| --- | --- | --- | --- |
| BONUS_POOL | PHP 90,000 per tax year, shared | 34 | 48 |
| CLOTHING | PHP 8,000 per year | 35 | officer chooses if excess |
| LAUNDRY | PHP 400 per month | 35 | officer chooses if excess |
| CBA_PRODUCTIVITY | PHP 12,000 per year, combined, only if qualifying | 35 | officer chooses if excess |

Check the **Annual tax profile** column for each Earning Type. The first
profile migration may have prefilled a few links using common display names;
these are starting suggestions, **not verified classifications**. Confirm or
clear each one using the Earning Type's actual saved settings and agency rule.
Annualization now resolves a saved profile only through the earning's ID or
accounting code, never its display name. Older finalized payroll lines that
contain only a name may need manual review through **Show all earnings**, or
recomputation from the corrected Earning Type settings where appropriate.
The profile setting does not change taxable flags, Form 2316 items, earnings,
payroll results, or already finalized annualization snapshots.

Do **not** link CNA or PEI to CBA_PRODUCTIVITY without evidence that the
agency's actual scheme qualifies. DOH Allowance and PhilHealth Honorarium also
need their specific legal/payment basis. Do not infer treatment from names.

## 3. Review one test employee in Payroll

Open **Payroll → Annualization**, select **2026**, load the year, and open the
test employee. In **Benefit classification review**, click **How this review
works**. Assigned benefits show as **Required** until an officer saves a split.
Use **Show all earnings** if a benefit was not linked to a profile in
Administrative and choose the applicable profile in its Review dialog.

For each paid benefit, select the profile and enter:

- The de minimis or primary exempt amount and item (for example, clothing item
  35), if it qualifies.
- Any additional exempt amount that belongs in the shared other-benefits pool;
  this optional amount uses Form 2316 item 34.
- The taxable remainder and its correct item (typically item 48 for qualifying
  bonus excess). Explain the basis and save.

The three signed portions must equal the **unchanged** paid earning amount.
HRIS never allocates a ceiling on its own. It shows the ceiling and BIR source
as guidance. Review all qualifying benefits together, including the prior
employer's Form 2316, before using any shared exemption. A prior-employer
record in HRIS currently has aggregate amounts only; the officer must check
the actual Form 2316 for its benefit breakdown.

### Simple practice cases (assuming no other qualifying benefits)

- PHP 100,000 qualifying bonus: item 34 PHP 90,000; item 48 PHP 10,000.
- PHP 9,000 qualifying clothing allowance: item 35 PHP 8,000; the PHP 1,000
  excess may be item 34 **only if** shared-exemption capacity remains;
  otherwise classify the taxable remainder appropriately.
- A PHP 500 qualifying monthly laundry payment: item 35 PHP 400; assess the
  PHP 100 excess against the shared benefits pool or taxable remainder.

These are demonstrations, not automatic classifications or tax advice for an
employee. Annual, monthly, shared-pool, qualifying-payment and previous-
employer facts can change the correct split.

## 4. Confirm before finalization

Reload the employee and check that gross compensation did not change, while
taxable/non-taxable totals and BIR items reflect the saved split. A missing or
stale required review keeps the record **For review**. Correct all other
readiness issues, then finalize, preview Form 2316 and the Alphalist, export
only after the preview has no unresolved records, and validate the DAT with
the official BIR validation module.

## Official references

- BIR RR 11-2018: https://bir-cdn.bir.gov.ph/local/pdf/RR%20No.%2011-2018.pdf
- BIR RR 29-2025 digest: https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025%20digest%20FINAL.pdf
- BIR Form 2316: https://bir-cdn.bir.gov.ph/local/pdf/2316%20Sep%202021%20ENCS_Final_corrected.pdf
