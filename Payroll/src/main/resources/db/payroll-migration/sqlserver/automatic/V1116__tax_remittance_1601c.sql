IF OBJECT_ID('tax_remittance_1601c', 'U') IS NULL
    CREATE TABLE tax_remittance_1601c (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        taxYear INT NOT NULL,
        taxMonth INT NOT NULL,
        taxesWithheld DECIMAL(19,2) NOT NULL,
        adjustment DECIMAL(19,2) NOT NULL,
        penalties DECIMAL(19,2) NOT NULL,
        amountRemitted DECIMAL(19,2) NOT NULL,
        filingDate DATE NULL,
        remittanceDate DATE NULL,
        remittanceChannel VARCHAR(100) NULL,
        referenceNo VARCHAR(100) NULL,
        remarks VARCHAR(500) NULL,
        evidenceReference VARCHAR(255) NULL,
        status VARCHAR(20) NOT NULL,
        recordedBy VARCHAR(100) NOT NULL,
        recordedAt DATETIME2(6) NOT NULL,
        updatedBy VARCHAR(100) NOT NULL,
        updatedAt DATETIME2(6) NOT NULL,
        CONSTRAINT uk_1601c_year_month UNIQUE (taxYear, taxMonth),
        CONSTRAINT ck_1601c_month CHECK (taxMonth BETWEEN 1 AND 12)
    );

IF OBJECT_ID('tax_remittance_1601c_audit', 'U') IS NULL
    CREATE TABLE tax_remittance_1601c_audit (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        taxYear INT NOT NULL,
        taxMonth INT NOT NULL,
        action VARCHAR(20) NOT NULL,
        previousValues VARCHAR(2000) NULL,
        newValues VARCHAR(2000) NOT NULL,
        performedBy VARCHAR(100) NOT NULL,
        performedAt DATETIME2(6) NOT NULL
    );
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_1601c_audit_period' AND object_id = OBJECT_ID('tax_remittance_1601c_audit'))
    CREATE INDEX idx_1601c_audit_period ON tax_remittance_1601c_audit (taxYear, taxMonth, performedAt);
