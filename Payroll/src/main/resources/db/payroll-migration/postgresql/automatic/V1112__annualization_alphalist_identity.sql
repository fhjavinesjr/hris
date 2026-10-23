ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeLastName VARCHAR(100);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeFirstName VARCHAR(100);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeMiddleName VARCHAR(100);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeExtensionName VARCHAR(50);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeNationality VARCHAR(100);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeEmploymentStatusCode VARCHAR(50);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeEmploymentStatusName VARCHAR(200);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeEmploymentFrom DATE;

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeEmploymentTo DATE;
