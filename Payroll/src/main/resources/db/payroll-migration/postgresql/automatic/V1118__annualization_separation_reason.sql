ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS employeeBirSeparationReasonCode VARCHAR(2);
