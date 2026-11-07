IF OBJECT_ID('annualization_snapshot', 'U') IS NULL
BEGIN
    CREATE TABLE annualization_snapshot (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, employeeNo VARCHAR(50) NOT NULL,
        employeeName VARCHAR(200) NOT NULL, department VARCHAR(200), taxYear INT NOT NULL,
        version INT NOT NULL, annualizationType VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL,
        presentGrossCompensation DECIMAL(19,2) NOT NULL, presentNonTaxableCompensation DECIMAL(19,2) NOT NULL,
        presentTaxableCompensation DECIMAL(19,2) NOT NULL, presentTaxWithheld DECIMAL(19,2) NOT NULL,
        previousGrossCompensation DECIMAL(19,2) NOT NULL, previousNonTaxableCompensation DECIMAL(19,2) NOT NULL,
        previousTaxableCompensation DECIMAL(19,2) NOT NULL, previousTaxWithheld DECIMAL(19,2) NOT NULL,
        grossCompensation DECIMAL(19,2) NOT NULL, nonTaxableCompensation DECIMAL(19,2) NOT NULL,
        taxableCompensation DECIMAL(19,2) NOT NULL, gsisContribution DECIMAL(19,2) NOT NULL,
        philHealthContribution DECIMAL(19,2) NOT NULL, pagIbigContribution DECIMAL(19,2) NOT NULL,
        taxWithheld DECIMAL(19,2) NOT NULL, annualTaxDue DECIMAL(19,2) NOT NULL,
        adjustmentAmount DECIMAL(19,2) NOT NULL, adjustmentType VARCHAR(30) NOT NULL,
        finalizedAt DATETIME2 NOT NULL, finalizedBy VARCHAR(100) NOT NULL, finalizationRemarks VARCHAR(500),
        reopenedAt DATETIME2, reopenedBy VARCHAR(100), reopenReason VARCHAR(500),
        CONSTRAINT uq_annualization_snapshot_version UNIQUE (employeeNo, taxYear, version)
    );
    CREATE INDEX idx_annualization_snapshot_lookup ON annualization_snapshot (employeeNo, taxYear, status);
END;

IF COL_LENGTH('annualization_snapshot', 'adjustmentHeaderId') IS NULL
    ALTER TABLE annualization_snapshot ADD adjustmentHeaderId BIGINT NULL;
IF COL_LENGTH('annualization_snapshot', 'adjustmentSalaryPeriodKey') IS NULL
    ALTER TABLE annualization_snapshot ADD adjustmentSalaryPeriodKey VARCHAR(50) NULL;
IF COL_LENGTH('annualization_snapshot', 'adjustmentAppliedAt') IS NULL
    ALTER TABLE annualization_snapshot ADD adjustmentAppliedAt DATETIME2 NULL;
IF COL_LENGTH('annualization_snapshot', 'adjustmentAppliedBy') IS NULL
    ALTER TABLE annualization_snapshot ADD adjustmentAppliedBy VARCHAR(100) NULL;

IF OBJECT_ID('annualization_snapshot_period', 'U') IS NULL
BEGIN
    CREATE TABLE annualization_snapshot_period (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, snapshotId BIGINT NOT NULL,
        salaryPeriodKey VARCHAR(50) NOT NULL, salaryDate DATE NOT NULL,
        grossCompensation DECIMAL(19,2) NOT NULL, nonTaxableCompensation DECIMAL(19,2) NOT NULL,
        taxableCompensation DECIMAL(19,2) NOT NULL, gsisContribution DECIMAL(19,2) NOT NULL,
        philHealthContribution DECIMAL(19,2) NOT NULL, pagIbigContribution DECIMAL(19,2) NOT NULL,
        taxWithheld DECIMAL(19,2) NOT NULL,
        CONSTRAINT fk_annualization_period_snapshot FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
    );
    CREATE INDEX idx_annualization_period_snapshot ON annualization_snapshot_period (snapshotId);
END;

IF OBJECT_ID('annualization_snapshot_previous', 'U') IS NULL
BEGIN
    CREATE TABLE annualization_snapshot_previous (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, snapshotId BIGINT NOT NULL,
        employerName VARCHAR(200) NOT NULL, employerTin VARCHAR(30) NOT NULL,
        employmentFrom DATE NOT NULL, employmentTo DATE NOT NULL,
        grossCompensation DECIMAL(19,2) NOT NULL, nonTaxableCompensation DECIMAL(19,2) NOT NULL,
        taxableCompensation DECIMAL(19,2) NOT NULL, taxWithheld DECIMAL(19,2) NOT NULL,
        CONSTRAINT fk_annualization_previous_snapshot FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
    );
    CREATE INDEX idx_annualization_previous_snapshot ON annualization_snapshot_previous (snapshotId);
END;

IF OBJECT_ID('annualization_audit_log', 'U') IS NULL
BEGIN
    CREATE TABLE annualization_audit_log (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY, employeeNo VARCHAR(50) NOT NULL,
        taxYear INT NOT NULL, action VARCHAR(40) NOT NULL, oldStatus VARCHAR(20),
        newStatus VARCHAR(20) NOT NULL, remarks VARCHAR(500), performedBy VARCHAR(100) NOT NULL,
        performedAt DATETIME2 NOT NULL
    );
    CREATE INDEX idx_annualization_audit_lookup ON annualization_audit_log (employeeNo, taxYear, performedAt);
END;
