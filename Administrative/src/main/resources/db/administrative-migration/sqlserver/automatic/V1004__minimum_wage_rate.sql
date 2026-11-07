IF OBJECT_ID('minimum_wage_rate', 'U') IS NULL
BEGIN
    CREATE TABLE minimum_wage_rate (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        regionCode VARCHAR(30) NOT NULL,
        regionName VARCHAR(150) NOT NULL,
        effectiveFrom DATE NOT NULL,
        effectiveTo DATE NULL,
        dailyRate DECIMAL(19,2) NOT NULL,
        monthlyRate DECIMAL(19,2) NOT NULL,
        legalBasis VARCHAR(200) NOT NULL,
        remarks VARCHAR(500) NULL,
        CONSTRAINT uq_minimum_wage_rate_region_from UNIQUE (regionCode, effectiveFrom)
    );
    CREATE INDEX idx_minimum_wage_rate_effective
        ON minimum_wage_rate (regionCode, effectiveFrom, effectiveTo);
END;
