IF OBJECT_ID('annualization_refund_voucher', 'U') IS NULL
    CREATE TABLE annualization_refund_voucher (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        snapshotId BIGINT NOT NULL,
        amount DECIMAL(19,2) NOT NULL,
        voucherReference VARCHAR(100) NOT NULL,
        paidDate DATE NOT NULL,
        recordedBy VARCHAR(100) NOT NULL,
        recordedAt DATETIME2(6) NOT NULL,
        voidedAt DATETIME2(6) NULL,
        voidedBy VARCHAR(100) NULL,
        voidReason VARCHAR(500) NULL,
        CONSTRAINT fk_annual_refund_snapshot FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
    );
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_annual_refund_snapshot' AND object_id = OBJECT_ID('annualization_refund_voucher'))
    CREATE INDEX idx_annual_refund_snapshot ON annualization_refund_voucher (snapshotId);
