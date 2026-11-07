IF COL_LENGTH('annualization_snapshot', 'employeeBirSeparationReasonCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeBirSeparationReasonCode VARCHAR(2) NULL;
