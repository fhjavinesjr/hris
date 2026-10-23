# ISOFT HRIS Annualization — User Guide

This guide is for authorized HR and payroll officers using **Payroll → Annualization**. It explains the application workflow, not how to file a tax return. Use actual payroll, employee declarations, filed-return acknowledgements, and payment records for production work. A green HRIS reconciliation message means the recorded figures agree; it is **not** proof that a return was filed or tax was paid.

**At a glance:** Check HRM identity and payroll coverage → review each employee's tax calculation → finalize and settle any adjustment in the correct order → review monthly 1601-C evidence → confirm employer 1604-C facts → inspect Form 2316, the 1604-C draft, and the Alphalist DAT. A separation with an open final salary needs its final-pay tax review **before** locking that salary.

## 1. What each part does

| Screen or output | Purpose | What it does **not** do |
| --- | --- | --- |
| Employee Annualization | Combines finalized payroll, eligible previous-employer data, benefit classifications, and tax credits; calculates tax due and the withholding adjustment. | It does not pay a refund or automatically file a return. |
| BIR Form 2316 | Employee certificate preview based on a finalized annualization. | It does not by itself settle an unpaid adjustment. |
| Review BIR 1700 | Incomplete-year or annual individual-return **draft** for employee review. | HRIS does not decide the employee's final filing obligation or file Form 1700. |
| 1601-C Remittance Ledger | Officer-entered monthly record of filed withholding and payment evidence, compared with locked payroll. | Payroll's expected amount is not evidence of filing/remittance. |
| Alphalist DAT Attachment Review / Export BIR DAT | Checks finalized employee records and produces the 1604-C Alphalist DAT attachment. | An exported DAT is not an accepted BIR submission. |
| Preview BIR 1604-C draft / Employer Review | Draft employer annual return and officer-confirmed Items 3 and 9–13, reconciled with the monthly ledger and Alphalist. | The draft/confirmation does not sign or file Form 1604-C. |

The BIR describes 1601-C as a monthly remittance return, 1604-C as an employer annual information return, 2316 as the employee certificate, and 1700 as an individual annual income-tax return. [BIR forms directory](https://www.bir.gov.ph/bir-forms?q=Bir)

## 2. Before opening Annualization

1. Select the correct **tax year**. Do not mix records from two tax years.
2. Complete the employee's HRM identity and employment history: legal name, TIN, BIR branch and RDO codes, addresses, nationality, employment dates, and applicable MWE classification. For a separation, record the HRM separation and map its **BIR separation reason** in **Administrative → Nature of Separation**.
3. Confirm the employer's registered name, TIN, BIR branch/RDO codes, address, ZIP code, and withholding-agent category in authoritative settings.
4. Complete payroll for **every salary period actually worked**. Check earnings, deductions, benefits, and withholding. Lock completed periods in Payroll Register; if the final salary still needs a tax adjustment, keep it open until that adjustment is posted and the payroll is checked. An unlocked computation is not final payroll evidence.
5. Obtain any prior employer's Form 2316, PERA certificate, refund-payment record, filed 1601-C, and remittance acknowledgement **if applicable**. Do not create a record for an event that did not occur.
6. Ensure your account has the appropriate Annualization access/edit/finalize permissions. Only an authorized officer should confirm filing/payment facts or employer review details.

**Important:** A tax year still in progress can be reviewed for training or a genuine separation, but an October-only payroll record is not a completed 12-month year-end result. Check the year coverage before certifying any annual package.

## 3. Review one employee

1. Go to **Payroll → Annualization**, choose the tax year, and select **Load annualization**.
2. Use the status/search controls to find an employee; select **View**.
3. In **Annualization readiness**, resolve missing employer/employee identity and separation information. Follow the banner's specific issue. If MWE status is missing, use **Fix MWE setup** when available and verify its effective dates and region.
4. Compare **Compensation summary**, mandatory contributions, and **Period breakdown** with the locked payroll register. The period count should match the periods actually paid in the tax year; a single period is not a 12-month calculation.
5. If the employee worked for another employer during the same year, open **Previous employer → Add previous 2316 data** and enter the employer, employment dates, gross/non-taxable/taxable compensation, and tax withheld **from that Form 2316**. Do not invent previous-employer data merely to make a calculation work.
6. Review any **Benefit classification** marked Required or stale. Select the correct Administrative tax profile and exempt/taxable split based on the earning and supporting rule. This review changes annual tax classification, not the original paid payroll amount. Optional unassigned items still deserve a check if their treatment affects tax.
7. Add a **PERA certificate** only for a documented Personal Equity and Retirement Account contribution. This is not the ordinary government PERA allowance.
8. Read **Tax reconciliation**: tax already withheld, annual tax due, PERA credit, and the resulting **Refund**, **Additional withholding**, or **No adjustment**. A negative adjustment is a refund; verify all payroll periods and other-employer data before relying on it.

In the employee list, **For review** means source data or setup still needs attention, **Computed** means there is a calculable live result awaiting officer review, and **Finalized** means a frozen officer-reviewed snapshot exists. Finalized does not mean the employee has been paid or a BIR return has been filed.

## 4. Finalize and settle the adjustment in the correct order

| Result | Normal action |
| --- | --- |
| No adjustment | Once all periods, identity, benefits, and declarations have been checked, select **Finalize**. |
| Additional withholding | For an ordinary (non-separation) finalized snapshot, **Create payroll adjustment** can target an eligible open computed period. Post the pending adjustment through the normal Payroll Adjustment workflow, recompute/check that payroll, and lock it. Never treat a pending adjustment as paid or withheld. For separation, use **Prepare separation final pay before finalization**, as described below. |
| Refund | For an ordinary finalized snapshot, an eligible open period may carry a payroll adjustment; post it, check payroll, and lock the period. If the agency instead pays outside HRIS, select **Record paid voucher** only **after** actual payment, entering the paid date and voucher/payment reference. HRIS records evidence; it does not issue the voucher or pay the employee. |

At **Finalize**, choose the substituted-filing assessment, write its basis, confirm that the employee's employer/income declarations were reviewed, and optionally add internal remarks. **Qualified** is offered only when the application finds no previous-employer record and tax due equals total withholding; the officer must still verify the employee has pure compensation income from this employer only. **Not qualified** is an assessment, not an instruction to file Form 1700 automatically. Finalization preserves a frozen snapshot. For ordinary annualization, the **Create payroll adjustment** and **Record paid voucher** actions appear *after* finalization; separation final-pay adjustments are prepared *before* the final salary is locked and annualization finalized. Do not finalize an incomplete set of worked periods just to expose an adjustment button.

After finalization and settlement of any required adjustment, use **View BIR 2316** to inspect the employee certificate. **Review BIR 1700** opens a separate individual-return draft with limitations; refer any filing decision to the employee and authorized tax staff. If a finalized record is wrong, **Reopen** requires a reason and is available only when the screen's adjustment/voucher conditions permit it. Correct an erroneous voucher record with **Void voucher record**; voiding its HRIS evidence does **not** reverse a real payment.

## 5. Separation-time annualization

1. Record the employee's actual separation date and nature in HRM. Confirm the nature maps to a BIR separation reason; the code shown in **Annualization readiness** should not say “Not mapped.”
2. Identify the **last salary/final payment**. Lock earlier computed periods. Leave the final salary period open while its tax is being checked.
3. Select **Prepare separation final pay** and search by employee number or name. Choose the open final salary period. Review provisional taxable compensation, annual tax due, tax already withheld, proposed adjustment, and available net pay.
4. If the preview permits an adjustment, create the pending final-pay tax adjustment, post it, recompute/check the final salary, and lock it. Reload Annualization: the period breakdown and tax result must now include that last payment.
5. If the final salary is already locked and there is **no adjustment**, review the amounts and proceed to finalization. If additional tax remains but there is no open final salary, stop and resolve the payroll/employer-advance exception; do not mark it reconciled merely to continue.
6. Review substituted filing and finalize. If a refund was **actually paid with the last compensation** outside HRIS, now record its paid-voucher evidence using the last salary-payment date. Then inspect Form 2316 and the Alphalist entry. For separation, Form 2316 reflects compensation through the last payment; the Alphalist must show the correct employment end date and BIR separation code.

The **Prepare separation final pay** button cannot create a salary period. “No open computed final salary” means there is no eligible open period; check Payroll Computation/Register or, if payroll is already locked, follow the on-screen guidance.

## 6. Monthly 1601-C evidence

Open **1601-C Remittance Ledger**. **Expected from payroll** comes from locked payroll; **Filed withheld** and **Remitted** are officer-entered facts from the filed return and payment record. For a month with actual filing/payment, select **Review / edit**, enter taxes withheld, adjustment, penalties, total remitted, filing/remittance dates, bank/agency, reference, and evidence reference, then **Confirm from evidence**. A saved **Draft** is not confirmed and may leave a reconciliation issue.

If an earlier test or mistaken record was **never filed and never remitted**, use **Void / supersede entry**, give a reason, and make the explicit attestation. The audit trail remains, while the voided amount is excluded from confirmed totals. Do **not** void a genuinely filed or paid record to make the ledger green; investigate and correct the official evidence instead. A month with ₱0 expected withholding and no recorded return may show **MISSING** while the *amounts* reconcile. That is not a determination of filing compliance.

## 7. Alphalist, employer review, and output checks

1. Select **Review Alphalist DAT**. Resolve every **Dataset issue** and employee **Validation** issue; the export button remains disabled until preflight passes. Check legal name/TIN/branch, BIR schedule and region, employment and payroll coverage, gross/taxable compensation, annual tax due, adjusted withholding, and substituted-filing result.
2. Select **1601-C Remittance Ledger** and reconcile its confirmed figures with locked payroll, without inventing a return or payment.
3. Select **Review 1604-C employer details**. Items 3 and 9–13 come from the employer's actual information and evidence, **not** from the difference card alone:

   | Field | What to check |
   | --- | --- |
   | 3 — sheets attached | Number of sheets actually attached; a DAT download is not automatically a paper sheet. |
   | 9–10 — contact | Employer's contact number and email. |
   | 11 / 11A — employee refunds | Whether refunds were **released**; if Yes, enter the refund date. Compare paid vouchers, but verify the real payment evidence. |
   | 12 — employer overremittance | Amount supported by employer tax/remittance evidence, not automatically the employee-voucher total or the 1601-C/Alphalist gap. |
   | 13 — first crediting month | Select only when an overremittance will be credited and the month is supported. |
   | Evidence/signatory | Reference the source document; enter the authorized signatory's name, title, and TIN. Typed data is not a signature. Explain any discrepancy/correction. |

   Select **Save draft** for incomplete review. Select **Confirm from evidence** only after the officer has verified the values. The employer review must be reconfirmed if underlying withholding or paid-voucher figures subsequently change. [Official BIR 1604-C instructions](https://bir-cdn.bir.gov.ph/local/pdf/1604-C%20Guidelines%20Jan%202018%20Final.pdf)
4. Select **Preview BIR 1604-C draft**. Check employer identity, Items 3 and 9–13, monthly remittance rows, totals, and signatory details. The preview remains a **draft**, including after officer confirmation.
5. Return to **Review Alphalist DAT → Export BIR DAT**. Check the downloaded filename, tax year, employee count, separation dates/reasons, compensation, withholding, and control totals. Keep the PDF, DAT, source documents, and internal review record together for the authorized filing process. HRIS export does not submit the file to BIR or prove acceptance.

## 8. Worked simulation: separated employee, zero withholding

This example mirrors a **training-only** case; the dates/identities are not evidence of a completed real tax year. An employee has one locked October payroll period, gross compensation **₱33,750.00**, non-taxable compensation **₱3,205.00**, taxable compensation **₱30,545.00**, and final tax due/withheld **₱0.00**. HRM records a November 30 separation and a mapped BIR reason. There is no further final-pay amount, previous employer, paid refund, or employer overremittance.

1. Verify that one October period is truly the **entire compensation processed for this simulated separation**, and that its tax withheld is ₱0.00. For a real case, never assume omitted months were unpaid without checking payroll and employment history.
2. Review the benefit classification, identity, employment end date, and period breakdown. The tax reconciliation should say **No adjustment ₱0.00**. Finalize with the supported substituted-filing assessment and reason.
3. If an old October 1601-C test entry represents only a *computed* amount—not a filed return or paid tax—void it with an audit reason. The ledger should then show expected/confirmed withholding **₱0.00**, with the old record marked **VOIDED · Excluded**.
4. In Employer Review, a no-refund/no-overremittance case uses Item 11 **No**, blank 11A, Item 12 **₱0.00**, and Item 13 **None**. Enter the actual number of attached sheets, employer contact, documented simulation evidence reference, and authorized simulation signatory details; then confirm.
5. Preview the 1604-C draft and export the DAT. Confirm one employee, the separation date/reason, gross/taxable figures, ₱0.00 withholding/remittance, and matching control totals. This is a successful **HRIS workflow test**, not a real BIR filing or a full-year acceptance test.

## 9. If something is blocked

| Message or symptom | Check first |
| --- | --- |
| Employee missing or too few periods | Correct tax year; payroll computed **and locked**; salary dates and employee number; periods actually worked. |
| Missing tax bracket / MWE / identity / BIR reason | Administrative tax settings, HRM MWE/identity, employer settings, and Nature of Separation mapping. Correct the source and reload. |
| Additional tax but no open period | Do not post to locked payroll. Review final-pay period, possible authorized reopening, or documented employer-advance exception. |
| Form 2316 unavailable | Finalize first and settle any required tax adjustment/payment evidence. |
| DAT export disabled | Read each Dataset/Employee validation message in **Review Alphalist DAT**. Correct its source; then reload the preview. |
| 1601-C shows Draft/Difference | Compare locked payroll with the actual filed return/payment. Confirm genuine evidence, or void a never-filed mistaken record with a reason. |
| 1604-C review not confirmed | Complete Items 3 and 9–13, evidence reference, signatory data, and any reconciliation explanation; ensure the ledger and Alphalist are ready. |
| Form 1700 says year in progress | Treat it as an incomplete-year employee draft. HRIS neither files it nor determines final individual filing status. |

## 10. Final hand-off checklist

- [ ] Correct year and complete employment/payroll coverage; all relevant salary periods locked.
- [ ] Employee/employer identity, separation mapping, benefit treatment, previous-employer data, and PERA evidence checked as applicable.
- [ ] Adjustment posted in payroll or paid refund voucher recorded **only after payment**; employee annualization finalized and Form 2316 reviewed.
- [ ] Actual monthly 1601-C filing/payment evidence reconciled; any erroneous unfiled entry explicitly voided with an audit reason.
- [ ] Alphalist preflight ready; 1604-C Employer Review confirmed from evidence; draft PDF and DAT checked together.
- [ ] Authorized officer follows the agency's separate BIR filing, signature, payment, and acknowledgement process. HRIS status alone is not proof of BIR acceptance.

For current filing rules and form instructions, consult the [BIR forms directory](https://www.bir.gov.ph/bir-forms?q=Bir) and [1604-C instructions](https://bir-cdn.bir.gov.ph/local/pdf/1604-C%20Guidelines%20Jan%202018%20Final.pdf). This guide describes the HRIS workflow and should be reviewed when the application or BIR requirements change.
