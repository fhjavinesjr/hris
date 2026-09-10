/*
 * One-time reviewed remediation for the local SQL Server hrisof acceptance database.
 * No appointment or employee row is deleted. Every affected active row is preserved
 * in dbo.phase5e_active_appointment_remediation_20260907 before older occupants are
 * marked inactive.
 *
 * Adjudication evidence captured on 2026-09-07:
 * - 49 duplicate-active Plantilla groups;
 * - 4,893 affected active appointment rows, all owned by synthetic EMP-* employees;
 * - zero non-synthetic active occupants in those groups;
 * - one Job Position per group;
 * - no tie for latest assumptionToDutyDate in any group;
 * - 4,844 older occupants to deactivate and 49 latest occupants to retain.
 */
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID('dbo.phase5e_active_appointment_remediation_20260907', 'U') IS NOT NULL
    THROW 51100, 'Phase 5E remediation audit table already exists; refusing a repeat run', 1;

IF (SELECT COUNT(*) FROM dbo.employee) <> 5004
    OR (SELECT COUNT(*) FROM dbo.employeeappointment) <> 5006
    THROW 51101, 'Phase 5E remediation source row counts changed; review again before running', 1;

IF (SELECT COUNT(*) FROM (
        SELECT plantillaId FROM dbo.employeeappointment
        WHERE activeAppointment = 1 GROUP BY plantillaId HAVING COUNT(*) > 1
    ) duplicate_groups) <> 49
    THROW 51102, 'Phase 5E remediation expected exactly 49 duplicate Plantilla groups', 1;

IF EXISTS (
    SELECT 1
    FROM dbo.employeeappointment appointment
    JOIN dbo.employee employee ON employee.employeeId = appointment.employeeId
    JOIN (
        SELECT plantillaId FROM dbo.employeeappointment
        WHERE activeAppointment = 1 GROUP BY plantillaId HAVING COUNT(*) > 1
    ) duplicate_group ON duplicate_group.plantillaId = appointment.plantillaId
    WHERE appointment.activeAppointment = 1 AND employee.employeeNo NOT LIKE 'EMP-%'
)
    THROW 51103, 'Phase 5E remediation found a non-synthetic occupant; manual HR adjudication is required', 1;

IF EXISTS (
    SELECT 1 FROM (
        SELECT plantillaId, COUNT(DISTINCT jobPositionId) job_count
        FROM dbo.employeeappointment WHERE activeAppointment = 1
        GROUP BY plantillaId HAVING COUNT(*) > 1
    ) grouped WHERE job_count <> 1
)
    THROW 51104, 'Phase 5E remediation found mixed Job Positions in one Plantilla group', 1;

IF EXISTS (
    SELECT 1 FROM (
        SELECT plantillaId, assumptionToDutyDate,
               COUNT(*) same_date_count,
               DENSE_RANK() OVER (PARTITION BY plantillaId ORDER BY assumptionToDutyDate DESC) date_rank
        FROM dbo.employeeappointment WHERE activeAppointment = 1
        GROUP BY plantillaId, assumptionToDutyDate
    ) ranked_dates WHERE date_rank = 1 AND same_date_count > 1
)
    THROW 51105, 'Phase 5E remediation found an ambiguous latest assumption date', 1;

WITH ranked AS (
    SELECT appointment.*,
           employee.employeeNo,
           ROW_NUMBER() OVER (
               PARTITION BY appointment.plantillaId
               ORDER BY appointment.assumptionToDutyDate DESC,
                        appointment.appointmentIssuedDate DESC,
                        appointment.employeeAppointmentId DESC
           ) adjudication_rank,
           COUNT(*) OVER (PARTITION BY appointment.plantillaId) occupant_count
    FROM dbo.employeeappointment appointment
    JOIN dbo.employee employee ON employee.employeeId = appointment.employeeId
    WHERE appointment.activeAppointment = 1
)
SELECT *,
       CAST(CASE WHEN adjudication_rank = 1 THEN 'RETAIN_LATEST' ELSE 'DEACTIVATE_OLDER' END AS VARCHAR(30)) adjudication_action,
       CAST(SYSUTCDATETIME() AS DATETIME2) captured_at_utc
INTO dbo.phase5e_active_appointment_remediation_20260907
FROM ranked
WHERE occupant_count > 1;

IF (SELECT COUNT(*) FROM dbo.phase5e_active_appointment_remediation_20260907) <> 4893
    OR (SELECT COUNT(*) FROM dbo.phase5e_active_appointment_remediation_20260907 WHERE adjudication_action = 'DEACTIVATE_OLDER') <> 4844
    THROW 51106, 'Phase 5E remediation audit snapshot did not match the reviewed classification', 1;

UPDATE appointment
SET activeAppointment = 0
FROM dbo.employeeappointment appointment
JOIN dbo.phase5e_active_appointment_remediation_20260907 audit
  ON audit.employeeAppointmentId = appointment.employeeAppointmentId
WHERE audit.adjudication_action = 'DEACTIVATE_OLDER'
  AND appointment.activeAppointment = 1;

IF @@ROWCOUNT <> 4844
    THROW 51107, 'Phase 5E remediation did not update the reviewed number of rows', 1;

IF EXISTS (SELECT 1 FROM dbo.employeeappointment WHERE activeAppointment = 1 GROUP BY employeeId HAVING COUNT(*) > 1)
    THROW 51108, 'Phase 5E remediation left duplicate active appointments for an employee', 1;

IF EXISTS (SELECT 1 FROM dbo.employeeappointment WHERE activeAppointment = 1 GROUP BY plantillaId HAVING COUNT(*) > 1)
    THROW 51109, 'Phase 5E remediation left duplicate active Plantilla occupants', 1;

IF (SELECT COUNT(*) FROM dbo.employeeappointment) <> 5006
    OR (SELECT COUNT(*) FROM dbo.employeeappointment WHERE activeAppointment = 1) <> 54
    THROW 51110, 'Phase 5E remediation post-condition failed', 1;

COMMIT TRANSACTION;
