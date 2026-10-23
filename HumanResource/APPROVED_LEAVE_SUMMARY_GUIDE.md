# Approved Leave Summary

Grant `hrm.reports.leaveSummary` with `AGENCY_WIDE` data scope in Administrative permissions. Open **HR Management > Approved Leave Summary**. Select a date range (at most one year), Area, optional Business Unit, leave type from Administrative master data, and pay treatment (both, with pay, or without pay). Print the PDF.

Only leave applications whose workflow status **and** final approval status are `Approved` appear. Disapproved and pending applications are excluded. A legacy null `withPay` value is treated as paid, consistent with the leave model. The filter includes filings that overlap the selected range; the PDF displays each filing's **full approved dates and days**, not a prorated number. It is not a leave-balance ledger or payroll deduction report.

The report uses current Area/Business Unit assignments; verify historical transfers when reporting an earlier period. No schema migration is required.
