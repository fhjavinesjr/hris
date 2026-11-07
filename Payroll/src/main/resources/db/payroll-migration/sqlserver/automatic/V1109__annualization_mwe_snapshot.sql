IF COL_LENGTH('annualization_snapshot_period', 'minimumWageEarner') IS NULL
    ALTER TABLE annualization_snapshot_period ADD minimumWageEarner BIT NULL;

IF COL_LENGTH('annualization_snapshot_period', 'minimumWageRegionCode') IS NULL
    ALTER TABLE annualization_snapshot_period ADD minimumWageRegionCode VARCHAR(30) NULL;

IF COL_LENGTH('annualization_snapshot_period', 'minimumWageDailyRate') IS NULL
    ALTER TABLE annualization_snapshot_period ADD minimumWageDailyRate DECIMAL(19,2) NULL;

IF COL_LENGTH('annualization_snapshot_period', 'minimumWageMonthlyRate') IS NULL
    ALTER TABLE annualization_snapshot_period ADD minimumWageMonthlyRate DECIMAL(19,2) NULL;

IF COL_LENGTH('annualization_snapshot_period', 'minimumWageLegalBasis') IS NULL
    ALTER TABLE annualization_snapshot_period ADD minimumWageLegalBasis VARCHAR(200) NULL;
