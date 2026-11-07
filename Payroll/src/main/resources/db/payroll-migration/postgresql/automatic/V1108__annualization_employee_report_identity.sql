ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeLocalAddress VARCHAR(500);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeLocalZipCode VARCHAR(10);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeBirthDate DATE;

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeContactNo VARCHAR(50);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeePosition VARCHAR(200);
