CREATE TABLE IF NOT EXISTS annual_pera_tax_credit (
    id BIGSERIAL PRIMARY KEY,
    employeeNo VARCHAR(50) NOT NULL,
    taxYear INTEGER NOT NULL,
    contributionAmount NUMERIC(19,2) NOT NULL,
    certifiedTaxCredit NUMERIC(19,2) NOT NULL,
    certificateNo VARCHAR(100) NOT NULL,
    administratorName VARCHAR(200) NOT NULL,
    certificateIssuedDate DATE NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    updatedAt TIMESTAMP NOT NULL,
    createdBy VARCHAR(100) NOT NULL,
    updatedBy VARCHAR(100) NOT NULL,
    CONSTRAINT uq_annual_pera_employee_year UNIQUE (employeeNo, taxYear)
);

CREATE INDEX IF NOT EXISTS idx_annual_pera_lookup
    ON annual_pera_tax_credit (employeeNo, taxYear);

ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS peraTaxCredit NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS peraContributionAmount NUMERIC(19,2);
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS peraCertifiedTaxCredit NUMERIC(19,2);
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS peraCertificateNo VARCHAR(100);
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS peraAdministratorName VARCHAR(200);
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS peraCertificateIssuedDate DATE;
