IF COL_LENGTH('overtime_request', 'groupRequestId') IS NULL
    ALTER TABLE overtime_request ADD groupRequestId varchar(36) NULL;
IF COL_LENGTH('overtime_request', 'filedByEmployeeId') IS NULL
    ALTER TABLE overtime_request ADD filedByEmployeeId bigint NULL;
IF COL_LENGTH('overtime_request', 'businessUnitId') IS NULL
    ALTER TABLE overtime_request ADD businessUnitId bigint NULL;
IF COL_LENGTH('overtime_request', 'supervisorFiled') IS NULL
BEGIN
    ALTER TABLE overtime_request ADD supervisorFiled bit NOT NULL
        CONSTRAINT DF_overtime_request_supervisorFiled DEFAULT 0;
END;
IF COL_LENGTH('overtime_request', 'expectedOutput') IS NULL
    ALTER TABLE overtime_request ADD expectedOutput varchar(500) NULL;
IF COL_LENGTH('overtime_request', 'discrepancyRemarks') IS NULL
    ALTER TABLE overtime_request ADD discrepancyRemarks varchar(500) NULL;
IF COL_LENGTH('overtime_request', 'discrepancyReportedAt') IS NULL
    ALTER TABLE overtime_request ADD discrepancyReportedAt datetime2 NULL;

UPDATE overtime_request
SET filedByEmployeeId = employeeId
WHERE filedByEmployeeId IS NULL;
