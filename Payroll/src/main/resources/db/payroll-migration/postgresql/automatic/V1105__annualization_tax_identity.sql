ALTER TABLE IF EXISTS previous_employer_tax_data
    ADD COLUMN IF NOT EXISTS "employerAddress" VARCHAR(500);
ALTER TABLE IF EXISTS previous_employer_tax_data
    ADD COLUMN IF NOT EXISTS "employerZipCode" VARCHAR(10);

ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employerName" VARCHAR(200);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employerTin" VARCHAR(30);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employerRdoCode" VARCHAR(10);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employerAddress" VARCHAR(500);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employerZipCode" VARCHAR(10);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employeeTin" VARCHAR(30);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employeeRdoCode" VARCHAR(10);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employeeAddress" VARCHAR(500);
ALTER TABLE IF EXISTS annualization_snapshot
    ADD COLUMN IF NOT EXISTS "employeeZipCode" VARCHAR(10);

ALTER TABLE IF EXISTS annualization_snapshot_previous
    ADD COLUMN IF NOT EXISTS "employerAddress" VARCHAR(500);
ALTER TABLE IF EXISTS annualization_snapshot_previous
    ADD COLUMN IF NOT EXISTS "employerZipCode" VARCHAR(10);
