IF COL_LENGTH('annualization_snapshot', 'employerBirBranchCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employerBirBranchCode VARCHAR(4) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeBirBranchCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeBirBranchCode VARCHAR(4) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeBirRegionCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeBirRegionCode VARCHAR(4) NULL;
