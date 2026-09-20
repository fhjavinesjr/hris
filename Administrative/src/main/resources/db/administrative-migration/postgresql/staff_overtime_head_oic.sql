ALTER TABLE manage_personnel ADD COLUMN IF NOT EXISTS "oicEffectiveFrom" date NULL;
ALTER TABLE manage_personnel ADD COLUMN IF NOT EXISTS "oicEffectiveTo" date NULL;

UPDATE manage_personnel
SET "otherStatus" = NULL, "oicEffectiveFrom" = NULL, "oicEffectiveTo" = NULL
WHERE "otherStatus" IS NOT NULL AND UPPER(BTRIM("otherStatus")) <> 'OIC';
