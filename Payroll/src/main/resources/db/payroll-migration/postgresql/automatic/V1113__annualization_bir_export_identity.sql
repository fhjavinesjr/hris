ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS employerBirBranchCode VARCHAR(4);
ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS employeeBirBranchCode VARCHAR(4);
ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS employeeBirRegionCode VARCHAR(4);
