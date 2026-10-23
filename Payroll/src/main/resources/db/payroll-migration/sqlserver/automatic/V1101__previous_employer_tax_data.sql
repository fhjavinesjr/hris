IF OBJECT_ID('previous_employer_tax_data', 'U') IS NULL
BEGIN
    CREATE TABLE previous_employer_tax_data (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        employeeNo VARCHAR(50) NOT NULL,
        taxYear INT NOT NULL,
        employerName VARCHAR(200) NOT NULL,
        employerTin VARCHAR(30) NOT NULL,
        employmentFrom DATE NOT NULL,
        employmentTo DATE NOT NULL,
        grossCompensation DECIMAL(19, 2) NOT NULL,
        nonTaxableCompensation DECIMAL(19, 2) NOT NULL,
        taxableCompensation DECIMAL(19, 2) NOT NULL,
        taxWithheld DECIMAL(19, 2) NOT NULL,
        createdAt DATETIME2 NOT NULL,
        updatedAt DATETIME2 NOT NULL,
        createdBy VARCHAR(100) NOT NULL,
        updatedBy VARCHAR(100) NOT NULL
    );

    CREATE INDEX idx_petd_employee_year
        ON previous_employer_tax_data (employeeNo, taxYear);
END;
