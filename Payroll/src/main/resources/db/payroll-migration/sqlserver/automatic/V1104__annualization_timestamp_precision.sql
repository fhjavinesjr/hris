IF OBJECT_ID('previous_employer_tax_data', 'U') IS NOT NULL
BEGIN
    ALTER TABLE previous_employer_tax_data ALTER COLUMN createdAt DATETIME2(6) NOT NULL;
    ALTER TABLE previous_employer_tax_data ALTER COLUMN updatedAt DATETIME2(6) NOT NULL;
END;

IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL
BEGIN
    ALTER TABLE annualization_snapshot ALTER COLUMN finalizedAt DATETIME2(6) NOT NULL;
    ALTER TABLE annualization_snapshot ALTER COLUMN reopenedAt DATETIME2(6) NULL;
    ALTER TABLE annualization_snapshot ALTER COLUMN adjustmentAppliedAt DATETIME2(6) NULL;
END;

IF OBJECT_ID('annualization_audit_log', 'U') IS NOT NULL
BEGIN
    IF EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE name = 'idx_annualization_audit_lookup'
          AND object_id = OBJECT_ID('annualization_audit_log')
    )
        DROP INDEX idx_annualization_audit_lookup ON annualization_audit_log;

    ALTER TABLE annualization_audit_log ALTER COLUMN performedAt DATETIME2(6) NOT NULL;

    CREATE INDEX idx_annualization_audit_lookup
        ON annualization_audit_log (employeeNo, taxYear, performedAt);
END;
