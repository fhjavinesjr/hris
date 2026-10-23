ALTER TABLE IF EXISTS previous_employer_tax_data
    ALTER COLUMN createdAt TYPE TIMESTAMP(6);
ALTER TABLE IF EXISTS previous_employer_tax_data
    ALTER COLUMN updatedAt TYPE TIMESTAMP(6);

ALTER TABLE IF EXISTS annualization_snapshot
    ALTER COLUMN finalizedAt TYPE TIMESTAMP(6);
ALTER TABLE IF EXISTS annualization_snapshot
    ALTER COLUMN reopenedAt TYPE TIMESTAMP(6);
ALTER TABLE IF EXISTS annualization_snapshot
    ALTER COLUMN adjustmentAppliedAt TYPE TIMESTAMP(6);

ALTER TABLE IF EXISTS annualization_audit_log
    ALTER COLUMN performedAt TYPE TIMESTAMP(6);
