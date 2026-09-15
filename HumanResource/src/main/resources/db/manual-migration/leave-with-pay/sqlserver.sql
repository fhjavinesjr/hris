IF COL_LENGTH('leave_application', 'withPay') IS NULL
BEGIN
    ALTER TABLE leave_application
        ADD withPay bit NOT NULL
            CONSTRAINT DF_leave_application_withPay DEFAULT (1) WITH VALUES;
END;
