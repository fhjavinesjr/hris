# Phase 5F.2 HumanResource Appointment Documents Review

Date: 2026-09-07  
Status: Passed

## Adjudicated source forms

- Oath of Office is a separate legal document, not the appointment form. The legacy ZCMC source is `web/reports/zcmc/OathOfficeReport.jrxml`; the implemented wording and roles were reconciled to CSC Form No. 32, Revised 2025, Annex B of the 2025 ORAOHRA.
- Certification of Assumption to Duty is also separate from the appointment. The legacy source is `web/reports/zcmc/certification/CertAssumptionToDuty.jrxml`; the implemented contract follows CSC Form No. 4, Revised 2025, Annex L.
- Oath signatories are the appointee and officer administering the oath. Assumption signatories are the Head of Office/Department/Unit and the attesting HRMO. The assumption date must equal the authoritative appointment assumption-to-duty date.

## Delivered contract

- Paired PostgreSQL and SQL Server HumanResource V3 migrations add immutable appointment-document revisions and audit events, linked to authoritative appointment/onboarding records and employee-backed signatories.
- Draft, finalization, supersession, correction, optimistic version, source snapshot, fingerprint, form code/version, event dates, venue, signatory snapshots, and audit metadata are explicit.
- Final PDFs are bean-driven and SQL-free: CSC Oath of Office 2025, CSC Certification of Assumption to Duty 2025, and the onboarding completion record. Draft legal documents cannot be rendered.
- Existing Personnel Action output remains under its existing HRM appointment contract, with independent `hrm.appointment-report` authorization.
- Reports never persist generated PDFs, browser tokens, signatures, storage paths, or appointment-document content in audit logs.

## Gates

- HumanResource clean suite: 71 tests, zero failures/errors/skips.
- Administrative clean suite: 51 tests, zero failures/errors/skips.
- Fresh and populated SQL Server V1-V3 migrations passed with appointment/employee counts preserved; paired migration parity and PostgreSQL-mode tests passed. No live PostgreSQL instance was available.
- All three JRXML files compiled and representative PDFs were generated and visually inspected for layout, pagination, signatories, null handling, and repeated headers.
- HRISApp combined reactor package passed after the Phase 5F.2 implementation.

Phase 5F.2 cleared the approved gate and allowed Phase 5F.3 to start. Phase 5F.4 and Phase 6 remained out of scope at that checkpoint.
