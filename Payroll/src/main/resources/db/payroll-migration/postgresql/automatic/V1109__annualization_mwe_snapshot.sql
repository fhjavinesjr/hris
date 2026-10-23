ALTER TABLE annualization_snapshot_period
    ADD COLUMN IF NOT EXISTS minimumWageEarner BOOLEAN;

ALTER TABLE annualization_snapshot_period
    ADD COLUMN IF NOT EXISTS minimumWageRegionCode VARCHAR(30);

ALTER TABLE annualization_snapshot_period
    ADD COLUMN IF NOT EXISTS minimumWageDailyRate NUMERIC(19,2);

ALTER TABLE annualization_snapshot_period
    ADD COLUMN IF NOT EXISTS minimumWageMonthlyRate NUMERIC(19,2);

ALTER TABLE annualization_snapshot_period
    ADD COLUMN IF NOT EXISTS minimumWageLegalBasis VARCHAR(200);
