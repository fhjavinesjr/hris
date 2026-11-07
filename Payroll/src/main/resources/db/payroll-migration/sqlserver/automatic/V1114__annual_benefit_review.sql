IF OBJECT_ID('annual_benefit_review', 'U') IS NULL
    CREATE TABLE annual_benefit_review (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        sourceType VARCHAR(30) NOT NULL,
        sourceId BIGINT NOT NULL,
        employeeNo VARCHAR(50) NOT NULL,
        taxYear INT NOT NULL,
        sourceAmount DECIMAL(19,2) NOT NULL,
        exemptAmount DECIMAL(19,2) NOT NULL,
        otherExemptAmount DECIMAL(19,2) NOT NULL,
        taxableAmount DECIMAL(19,2) NOT NULL,
        exemptBirItem VARCHAR(4) NULL,
        otherExemptBirItem VARCHAR(4) NULL,
        taxableBirItem VARCHAR(4) NULL,
        profileCode VARCHAR(50) NOT NULL,
        profileLegalBasis VARCHAR(200) NOT NULL,
        profileGuidanceCeiling DECIMAL(19,2) NULL,
        profileCeilingPeriod VARCHAR(30) NULL,
        reason VARCHAR(500) NOT NULL,
        reviewedBy VARCHAR(100) NOT NULL,
        reviewedAt DATETIME2(6) NOT NULL,
        CONSTRAINT uq_annual_benefit_review_source UNIQUE (sourceType, sourceId)
    );
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_annual_benefit_review_employee' AND object_id = OBJECT_ID('annual_benefit_review'))
    CREATE INDEX idx_annual_benefit_review_employee ON annual_benefit_review (employeeNo, taxYear);
