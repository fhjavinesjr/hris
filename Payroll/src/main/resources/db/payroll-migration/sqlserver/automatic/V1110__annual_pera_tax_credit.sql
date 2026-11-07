IF OBJECT_ID('annual_pera_tax_credit', 'U') IS NULL
BEGIN
    CREATE TABLE annual_pera_tax_credit (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        employeeNo VARCHAR(50) NOT NULL,
        taxYear INT NOT NULL,
        contributionAmount DECIMAL(19,2) NOT NULL,
        certifiedTaxCredit DECIMAL(19,2) NOT NULL,
        certificateNo VARCHAR(100) NOT NULL,
        administratorName VARCHAR(200) NOT NULL,
        certificateIssuedDate DATE NOT NULL,
        createdAt DATETIME2 NOT NULL,
        updatedAt DATETIME2 NOT NULL,
        createdBy VARCHAR(100) NOT NULL,
        updatedBy VARCHAR(100) NOT NULL,
        CONSTRAINT uq_annual_pera_employee_year UNIQUE (employeeNo, taxYear)
    );
    CREATE INDEX idx_annual_pera_lookup ON annual_pera_tax_credit (employeeNo, taxYear);
END;

IF COL_LENGTH('annualization_snapshot', 'peraTaxCredit') IS NULL
    ALTER TABLE annualization_snapshot ADD peraTaxCredit DECIMAL(19,2) NOT NULL
        CONSTRAINT df_annualization_snapshot_pera_tax_credit DEFAULT 0;
IF COL_LENGTH('annualization_snapshot', 'peraContributionAmount') IS NULL
    ALTER TABLE annualization_snapshot ADD peraContributionAmount DECIMAL(19,2) NULL;
IF COL_LENGTH('annualization_snapshot', 'peraCertifiedTaxCredit') IS NULL
    ALTER TABLE annualization_snapshot ADD peraCertifiedTaxCredit DECIMAL(19,2) NULL;
IF COL_LENGTH('annualization_snapshot', 'peraCertificateNo') IS NULL
    ALTER TABLE annualization_snapshot ADD peraCertificateNo VARCHAR(100) NULL;
IF COL_LENGTH('annualization_snapshot', 'peraAdministratorName') IS NULL
    ALTER TABLE annualization_snapshot ADD peraAdministratorName VARCHAR(200) NULL;
IF COL_LENGTH('annualization_snapshot', 'peraCertificateIssuedDate') IS NULL
    ALTER TABLE annualization_snapshot ADD peraCertificateIssuedDate DATE NULL;
