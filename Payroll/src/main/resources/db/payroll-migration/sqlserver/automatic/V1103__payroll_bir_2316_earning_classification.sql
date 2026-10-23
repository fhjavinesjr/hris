IF OBJECT_ID('payroll_detail_earning', 'U') IS NOT NULL
    AND COL_LENGTH('payroll_detail_earning', 'bir2316ItemNo') IS NULL
    ALTER TABLE payroll_detail_earning ADD bir2316ItemNo VARCHAR(4) NULL;
