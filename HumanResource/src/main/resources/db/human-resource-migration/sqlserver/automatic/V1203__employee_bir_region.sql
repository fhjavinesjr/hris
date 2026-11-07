IF OBJECT_ID('personaldata', 'U') IS NOT NULL
   AND COL_LENGTH('personaldata', 'birRegionCode') IS NULL
    ALTER TABLE personaldata ADD birRegionCode VARCHAR(4) NULL;

IF OBJECT_ID('personaldata', 'U') IS NOT NULL
   AND COL_LENGTH('personaldata', 'birBranchCode') IS NULL
    ALTER TABLE personaldata ADD birBranchCode VARCHAR(4) NULL;
