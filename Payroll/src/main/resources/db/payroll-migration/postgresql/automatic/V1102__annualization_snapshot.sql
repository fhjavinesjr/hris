CREATE TABLE IF NOT EXISTS annualization_snapshot (
    id BIGSERIAL PRIMARY KEY, employeeNo VARCHAR(50) NOT NULL, employeeName VARCHAR(200) NOT NULL,
    department VARCHAR(200), taxYear INTEGER NOT NULL, version INTEGER NOT NULL,
    annualizationType VARCHAR(20) NOT NULL, status VARCHAR(20) NOT NULL,
    presentGrossCompensation NUMERIC(19,2) NOT NULL, presentNonTaxableCompensation NUMERIC(19,2) NOT NULL,
    presentTaxableCompensation NUMERIC(19,2) NOT NULL, presentTaxWithheld NUMERIC(19,2) NOT NULL,
    previousGrossCompensation NUMERIC(19,2) NOT NULL, previousNonTaxableCompensation NUMERIC(19,2) NOT NULL,
    previousTaxableCompensation NUMERIC(19,2) NOT NULL, previousTaxWithheld NUMERIC(19,2) NOT NULL,
    grossCompensation NUMERIC(19,2) NOT NULL, nonTaxableCompensation NUMERIC(19,2) NOT NULL,
    taxableCompensation NUMERIC(19,2) NOT NULL, gsisContribution NUMERIC(19,2) NOT NULL,
    philHealthContribution NUMERIC(19,2) NOT NULL, pagIbigContribution NUMERIC(19,2) NOT NULL,
    taxWithheld NUMERIC(19,2) NOT NULL, annualTaxDue NUMERIC(19,2) NOT NULL,
    adjustmentAmount NUMERIC(19,2) NOT NULL, adjustmentType VARCHAR(30) NOT NULL,
    finalizedAt TIMESTAMP NOT NULL, finalizedBy VARCHAR(100) NOT NULL, finalizationRemarks VARCHAR(500),
    reopenedAt TIMESTAMP, reopenedBy VARCHAR(100), reopenReason VARCHAR(500),
    CONSTRAINT uq_annualization_snapshot_version UNIQUE (employeeNo, taxYear, version)
);
CREATE INDEX IF NOT EXISTS idx_annualization_snapshot_lookup
    ON annualization_snapshot (employeeNo, taxYear, status);

ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS adjustmentHeaderId BIGINT;
ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS adjustmentSalaryPeriodKey VARCHAR(50);
ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS adjustmentAppliedAt TIMESTAMP;
ALTER TABLE annualization_snapshot ADD COLUMN IF NOT EXISTS adjustmentAppliedBy VARCHAR(100);

CREATE TABLE IF NOT EXISTS annualization_snapshot_period (
    id BIGSERIAL PRIMARY KEY, snapshotId BIGINT NOT NULL,
    salaryPeriodKey VARCHAR(50) NOT NULL, salaryDate DATE NOT NULL,
    grossCompensation NUMERIC(19,2) NOT NULL, nonTaxableCompensation NUMERIC(19,2) NOT NULL,
    taxableCompensation NUMERIC(19,2) NOT NULL, gsisContribution NUMERIC(19,2) NOT NULL,
    philHealthContribution NUMERIC(19,2) NOT NULL, pagIbigContribution NUMERIC(19,2) NOT NULL,
    taxWithheld NUMERIC(19,2) NOT NULL,
    CONSTRAINT fk_annualization_period_snapshot FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
);
CREATE INDEX IF NOT EXISTS idx_annualization_period_snapshot ON annualization_snapshot_period (snapshotId);

CREATE TABLE IF NOT EXISTS annualization_snapshot_previous (
    id BIGSERIAL PRIMARY KEY, snapshotId BIGINT NOT NULL,
    employerName VARCHAR(200) NOT NULL, employerTin VARCHAR(30) NOT NULL,
    employmentFrom DATE NOT NULL, employmentTo DATE NOT NULL,
    grossCompensation NUMERIC(19,2) NOT NULL, nonTaxableCompensation NUMERIC(19,2) NOT NULL,
    taxableCompensation NUMERIC(19,2) NOT NULL, taxWithheld NUMERIC(19,2) NOT NULL,
    CONSTRAINT fk_annualization_previous_snapshot FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
);
CREATE INDEX IF NOT EXISTS idx_annualization_previous_snapshot ON annualization_snapshot_previous (snapshotId);

CREATE TABLE IF NOT EXISTS annualization_audit_log (
    id BIGSERIAL PRIMARY KEY, employeeNo VARCHAR(50) NOT NULL, taxYear INTEGER NOT NULL,
    action VARCHAR(40) NOT NULL, oldStatus VARCHAR(20), newStatus VARCHAR(20) NOT NULL,
    remarks VARCHAR(500), performedBy VARCHAR(100) NOT NULL, performedAt TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_annualization_audit_lookup
    ON annualization_audit_log (employeeNo, taxYear, performedAt);
