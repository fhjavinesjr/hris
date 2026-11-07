IF OBJECT_ID('bir_1604c_officer_review', 'U') IS NULL
    CREATE TABLE bir_1604c_officer_review (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        taxYear INT NOT NULL,
        contactNumber VARCHAR(30) NULL,
        emailAddress VARCHAR(100) NULL,
        sheetsAttached INT NULL,
        refundsReleased VARCHAR(3) NULL,
        refundDate DATE NULL,
        overremittanceAmount DECIMAL(19,2) NULL,
        firstCreditingMonth INT NULL,
        evidenceReference VARCHAR(255) NULL,
        signatoryName VARCHAR(100) NULL,
        signatoryTitle VARCHAR(100) NULL,
        signatoryTin VARCHAR(30) NULL,
        remarks VARCHAR(500) NULL,
        basis1601CWithheld DECIMAL(19,2) NULL,
        basisAlphalistWithheld DECIMAL(19,2) NULL,
        basisPaidRefunds DECIMAL(19,2) NULL,
        status VARCHAR(20) NOT NULL,
        updatedBy VARCHAR(100) NOT NULL,
        updatedAt DATETIME2(6) NOT NULL,
        CONSTRAINT uk_1604c_review_year UNIQUE (taxYear),
        CONSTRAINT ck_1604c_credit_month CHECK (firstCreditingMonth BETWEEN 1 AND 12)
    );

IF OBJECT_ID('bir_1604c_officer_review_audit', 'U') IS NULL
    CREATE TABLE bir_1604c_officer_review_audit (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        taxYear INT NOT NULL,
        action VARCHAR(20) NOT NULL,
        previousValues VARCHAR(2000) NULL,
        newValues VARCHAR(2000) NOT NULL,
        performedBy VARCHAR(100) NOT NULL,
        performedAt DATETIME2(6) NOT NULL
    );
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_1604c_review_audit_year' AND object_id = OBJECT_ID('bir_1604c_officer_review_audit'))
    CREATE INDEX idx_1604c_review_audit_year ON bir_1604c_officer_review_audit (taxYear, performedAt);
