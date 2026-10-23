IF OBJECT_ID('employee_mwe_status', 'U') IS NULL
BEGIN
    CREATE TABLE employee_mwe_status (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        employeeId BIGINT NOT NULL,
        effectiveFrom DATE NOT NULL,
        effectiveTo DATE NULL,
        minimumWageEarner BIT NOT NULL,
        regionCode VARCHAR(30) NULL,
        remarks VARCHAR(500) NULL,
        createdBy VARCHAR(100) NOT NULL,
        createdAt DATETIME2 NOT NULL,
        updatedBy VARCHAR(100) NOT NULL,
        updatedAt DATETIME2 NOT NULL,
        CONSTRAINT uq_employee_mwe_status_from UNIQUE (employeeId, effectiveFrom),
        CONSTRAINT fk_employee_mwe_status_employee FOREIGN KEY (employeeId)
            REFERENCES employee (employeeId)
    );
    CREATE INDEX idx_employee_mwe_status_effective
        ON employee_mwe_status (employeeId, effectiveFrom, effectiveTo);
END;
