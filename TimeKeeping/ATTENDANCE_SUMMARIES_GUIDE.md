# Timekeeping attendance summaries

Timekeeping > Reports contains four independent calendar-month PDFs: Tardiness, Undertime, Overtime, and Night Differential Work. Choose the month, Area, optional Business Unit, and employee signatories, then print the selected summary. Each PDF lists only employee dates with positive recorded minutes and shows per-employee and report totals. An empty month produces a clear no-recorded-minutes PDF. Current-month output stops at today and is marked draft.

Each report has a separate access permission: `tk.reports.tardiness`, `tk.reports.undertime`, `tk.reports.overtime`, and `tk.reports.nightDifferential`. MRA has `tk.reports.mra`. Grant these independently in Administrative Permissions. The backend checks the same key before exporting a PDF.

Tardiness, distinct undertime, and overtime minutes come from the same DTR-derived daily projection used by MRA. The CSC DTR UNDERTIME display combines late and distinct undertime; the two summaries separate them. The overtime summary shows whether the DTR date has an approved overtime request, but computed minutes alone are not a payment approval.

Night Differential Work is **attendance evidence, not a payable NSDP calculation**. It sums complete DTR segment punch intervals overlapping 6 p.m. to 6 a.m., subtracting complete recorded breaks. Segments with an incomplete punch or break pair contribute no minutes. Officers must verify the official shift, authorization, employee eligibility, and applicable agency rate before any payment. The [CSC RA 11701 implementing rules](https://www.csc.gov.ph/downloads/category/384-ra-11701?download=2638%3Airr-ra-11701) define the 6 p.m.–6 a.m. window and distinguish regular night work from ordinary day-schedule overtime. This report does not calculate money or assert eligibility.

As with MRA, historical Area/Business Unit membership follows the current `manage_personnel` assignment because effective-dated assignment history does not yet exist. Review transfers before certification. The Jasper template queries a short-lived, request-scoped daily dataset; Java supplies only report parameters and prepares DTR-derived data.
