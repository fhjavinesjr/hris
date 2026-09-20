ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "groupRequestId" varchar(36) NULL;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "filedByEmployeeId" bigint NULL;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "businessUnitId" bigint NULL;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "supervisorFiled" boolean NOT NULL DEFAULT false;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "expectedOutput" varchar(500) NULL;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "discrepancyRemarks" varchar(500) NULL;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS "discrepancyReportedAt" timestamp NULL;

UPDATE overtime_request
SET "filedByEmployeeId" = "employeeId"
WHERE "filedByEmployeeId" IS NULL;
