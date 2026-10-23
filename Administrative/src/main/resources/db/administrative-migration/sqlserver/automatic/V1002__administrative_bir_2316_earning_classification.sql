IF OBJECT_ID('earning_type', 'U') IS NOT NULL
    AND COL_LENGTH('earning_type', 'bir2316ItemNo') IS NULL
    ALTER TABLE earning_type ADD bir2316ItemNo VARCHAR(4) NULL;
