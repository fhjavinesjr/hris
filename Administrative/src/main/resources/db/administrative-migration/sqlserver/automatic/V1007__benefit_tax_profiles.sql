IF OBJECT_ID('benefit_tax_profile', 'U') IS NULL
    CREATE TABLE benefit_tax_profile (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        profileCode VARCHAR(50) NOT NULL,
        taxYear INT NOT NULL,
        label VARCHAR(120) NOT NULL,
        guidanceCeiling DECIMAL(19,2) NULL,
        ceilingPeriod VARCHAR(30) NULL,
        sharedPoolCode VARCHAR(50) NULL,
        exemptBirItem VARCHAR(4) NULL,
        taxableBirItem VARCHAR(4) NULL,
        legalBasis VARCHAR(200) NOT NULL,
        sourceUrl VARCHAR(500) NULL,
        CONSTRAINT uq_benefit_tax_profile_year UNIQUE (profileCode, taxYear)
    );
IF OBJECT_ID('earning_type', 'U') IS NOT NULL
   AND COL_LENGTH('earning_type', 'benefitTaxProfileCode') IS NULL
    ALTER TABLE earning_type ADD benefitTaxProfileCode VARCHAR(50) NULL;

IF NOT EXISTS (SELECT 1 FROM benefit_tax_profile WHERE profileCode = 'BONUS_POOL' AND taxYear = 2026)
    INSERT INTO benefit_tax_profile (profileCode, taxYear, label, guidanceCeiling, ceilingPeriod, sharedPoolCode, exemptBirItem, taxableBirItem, legalBasis, sourceUrl)
    VALUES ('BONUS_POOL', 2026, '13th month and qualifying other benefits', 90000.00, 'ANNUAL', 'OTHER_BENEFITS_90K', '34', '48', 'BIR RR 11-2018; review all qualifying benefits together', 'https://bir-cdn.bir.gov.ph/local/pdf/RR%20No.%2011-2018.pdf');
IF NOT EXISTS (SELECT 1 FROM benefit_tax_profile WHERE profileCode = 'CLOTHING' AND taxYear = 2026)
    INSERT INTO benefit_tax_profile (profileCode, taxYear, label, guidanceCeiling, ceilingPeriod, sharedPoolCode, exemptBirItem, taxableBirItem, legalBasis, sourceUrl)
    VALUES ('CLOTHING', 2026, 'Qualifying uniform and clothing allowance', 8000.00, 'ANNUAL', NULL, '35', NULL, 'BIR RR 29-2025; excess requires separate other-benefits review', 'https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025%20digest%20FINAL.pdf');
IF NOT EXISTS (SELECT 1 FROM benefit_tax_profile WHERE profileCode = 'LAUNDRY' AND taxYear = 2026)
    INSERT INTO benefit_tax_profile (profileCode, taxYear, label, guidanceCeiling, ceilingPeriod, sharedPoolCode, exemptBirItem, taxableBirItem, legalBasis, sourceUrl)
    VALUES ('LAUNDRY', 2026, 'Qualifying laundry allowance', 400.00, 'MONTHLY', NULL, '35', NULL, 'BIR RR 29-2025; excess requires separate other-benefits review', 'https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025%20digest%20FINAL.pdf');
IF NOT EXISTS (SELECT 1 FROM benefit_tax_profile WHERE profileCode = 'CBA_PRODUCTIVITY' AND taxYear = 2026)
    INSERT INTO benefit_tax_profile (profileCode, taxYear, label, guidanceCeiling, ceilingPeriod, sharedPoolCode, exemptBirItem, taxableBirItem, legalBasis, sourceUrl)
    VALUES ('CBA_PRODUCTIVITY', 2026, 'Qualifying CBA and productivity incentives combined', 12000.00, 'ANNUAL', 'CBA_PRODUCTIVITY_12K', '35', NULL, 'BIR RR 29-2025; confirm actual scheme qualifies', 'https://bir-cdn.bir.gov.ph/BIR/pdf/RR%20No.%2029-2025%20digest%20FINAL.pdf');

-- These links require an officer review and never alter saved payroll tax flags.
-- SQL Server compiles a batch before executing ALTER TABLE, so these updates
-- are deferred until the new column exists.
EXEC(N'UPDATE earning_type SET benefitTaxProfileCode = ''CLOTHING''
WHERE benefitTaxProfileCode IS NULL AND UPPER(LTRIM(RTRIM(name))) = ''CLOTHING ALLOWANCE''');
EXEC(N'UPDATE earning_type SET benefitTaxProfileCode = ''LAUNDRY''
WHERE benefitTaxProfileCode IS NULL AND UPPER(LTRIM(RTRIM(name))) IN (''LAUNDRY'', ''LAUNDRY ALLOWANCE'')');
EXEC(N'UPDATE earning_type SET benefitTaxProfileCode = ''BONUS_POOL''
WHERE benefitTaxProfileCode IS NULL AND UPPER(LTRIM(RTRIM(name))) IN (''MID-YEAR BONUS'', ''YEAR END BONUS'', ''YEAR-END BONUS'')');
