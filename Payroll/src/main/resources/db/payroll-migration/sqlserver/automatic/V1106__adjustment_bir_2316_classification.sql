IF OBJECT_ID('payroll_adjustment_line', 'U') IS NOT NULL
        AND COL_LENGTH('payroll_adjustment_line', 'bir2316ItemNo') IS NULL
    ALTER TABLE payroll_adjustment_line ADD bir2316ItemNo VARCHAR(4) NULL;
