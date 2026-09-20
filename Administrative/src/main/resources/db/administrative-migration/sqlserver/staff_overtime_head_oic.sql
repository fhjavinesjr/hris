IF COL_LENGTH('manage_personnel', 'oicEffectiveFrom') IS NULL
    ALTER TABLE manage_personnel ADD oicEffectiveFrom date NULL;
IF COL_LENGTH('manage_personnel', 'oicEffectiveTo') IS NULL
    ALTER TABLE manage_personnel ADD oicEffectiveTo date NULL;

UPDATE manage_personnel
SET otherStatus = NULL, oicEffectiveFrom = NULL, oicEffectiveTo = NULL
WHERE otherStatus IS NOT NULL AND UPPER(LTRIM(RTRIM(otherStatus))) <> 'OIC';
