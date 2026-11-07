# Monthly Report of Attendance (MRA)

The Timekeeping **Reports → Monthly Report of Attendance** page prints a calendar-month PDF for the selected Area and optional Business Unit. The format is based on published Philippine government agency MRA practice (for example, the [NCMH HRMO MRA](https://ncmh.gov.ph/images/pdf/forms/HRMO-Monthly-Report-of-Attendance-REV-3.pdf)); it is **not** presented as a CSC-prescribed form.

## Officer workflow

1. Check the employee DTRs, Work Schedules, approved leave and other approved requests for the month. Resolve missing or conflicting entries before certification.
2. Open **Timekeeping → Reports → Monthly Report of Attendance**.
3. Select the calendar month and Area. Select a Business Unit or leave **All Business Units**.
4. Choose **Prepared by** and **Certified by** from the employee autocomplete. **Verified by** is optional. HRIS resolves their current appointment positions when printing.
5. Click **Print MRA**. The PDF opens in a new browser tab; use the browser's print or save control.
6. Check the PDF against DTRs and approval evidence, then obtain the required signatures. Typed names and position titles do not replace signatures.

The report shows one dated line per employee per day. Its short-lived, request-scoped database snapshot comes from the same DTR report rows used by the DTR PDF; Jasper queries that snapshot to render the report. The snapshot is deleted after export, and abandoned snapshots are purged after one hour. A calendar month still in progress is marked **DRAFT THROUGH** its generation date; future dates are not printed.

**Review markers are not findings.** A scheduled day without DTR may appear as `ABSENT / FOR REVIEW`; a day without a Work Schedule or DTR appears for review without counted absence minutes. `AWOL` is shown only when explicitly recorded in DTR data. Approved leave and worked/absent conflicts are marked for review. Overtime minutes reflect DTR computation; the `[OT approved]` remark indicates an approved overtime request.

The PDF shows a red **REVIEW REQUIRED** notice and an employee-level review-day count whenever a daily row needs checking. This count includes missing schedule/DTR evidence, possible absences, unfinished DTR entries, a `Present` DTR with zero worked minutes, and work recorded on a rest day. It is separate from **Absence for review**: an unknown day must not be silently counted as absence or as a clean day. Resolve or explain every review row before an officer certifies the report. Printing is allowed so officers can use the draft for reconciliation; printing does not certify attendance.

**Late and undertime are separate in the MRA.** The CSC DTR's UNDERTIME display combines late plus distinct undertime. For comparison, `MRA Late min + MRA UT min` should equal the DTR UNDERTIME minutes for the same date. An `IN PROGRESS` or `PENDING FINALIZATION` DTR is marked for review even if it has provisional worked minutes.

**Historical organizational assignment limitation:** the existing `manage_personnel` table does not retain effective-dated Area/Business Unit history. A past-month MRA uses today's employee assignments; officers must check employees transferred since that month. Historical assignment accuracy requires a separate effective-dated personnel-assignment feature.

The report requires the independent `tk.reports.mra` access permission. Existing `tk.dtr` permission no longer grants report access by itself. An administrator must grant the new report permission in Administrative Permissions to non-administrator officers. SQL Server and PostgreSQL migration `V1301__mra_print_snapshot.sql` creates the request-scoped report table; `V1302__attendance_summary_metric.sql` adds a metric column shared with the separate summaries.
