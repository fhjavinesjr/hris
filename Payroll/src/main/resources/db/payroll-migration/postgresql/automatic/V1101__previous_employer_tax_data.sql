CREATE TABLE IF NOT EXISTS previous_employer_tax_data (
    id BIGSERIAL PRIMARY KEY,
    employeeNo VARCHAR(50) NOT NULL,
    taxYear INTEGER NOT NULL,
    employerName VARCHAR(200) NOT NULL,
    employerTin VARCHAR(30) NOT NULL,
    employmentFrom DATE NOT NULL,
    employmentTo DATE NOT NULL,
    grossCompensation NUMERIC(19, 2) NOT NULL,
    nonTaxableCompensation NUMERIC(19, 2) NOT NULL,
    taxableCompensation NUMERIC(19, 2) NOT NULL,
    taxWithheld NUMERIC(19, 2) NOT NULL,
    createdAt TIMESTAMP NOT NULL,
    updatedAt TIMESTAMP NOT NULL,
    createdBy VARCHAR(100) NOT NULL,
    updatedBy VARCHAR(100) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_petd_employee_year
    ON previous_employer_tax_data (employeeNo, taxYear);
