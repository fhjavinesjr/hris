# Incomplete DTR Review

Grant `tk.reports.incompleteDtr` in Administrative permissions, then open **Timekeeping > Timekeeping Reports > Incomplete DTR Review**. Select a date range (at most one year), Area, optional Business Unit, and Prepared/Certified signatories from the employee autocomplete before printing.

The PDF flags raw DTR segments missing an arrival/departure or one side of a recorded break; scheduled absences without a DTR; AWOL and DTR/leave conflicts; pending finalization; unusual undertime of at least 500 minutes; and recorded DTRs without a work schedule. It does **not** infer absence from a blank day with no schedule. The report stops at yesterday so an ongoing shift is not prematurely flagged.

Every finding is an officer review item, not a final determination of absence or misconduct. Check the source DTR, cross-midnight shift, approved leave, official engagements, CTO, corrections, rest day, and appointment coverage. Employee membership uses the **current** Area/Business Unit assignment; confirm historical transfers when reviewing an earlier period. No schema migration is required.
