IF COL_LENGTH('natureofseparation', 'birSeparationReasonCode') IS NULL
    ALTER TABLE natureofseparation ADD birSeparationReasonCode VARCHAR(2) NULL;
