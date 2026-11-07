IF OBJECT_ID('annualization_snapshot_item', 'U') IS NULL
BEGIN
    CREATE TABLE annualization_snapshot_item (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        snapshotId BIGINT NOT NULL,
        itemNo VARCHAR(4) NOT NULL,
        description VARCHAR(500) NULL,
        amount DECIMAL(19,2) NOT NULL,
        CONSTRAINT uq_annualization_snapshot_item UNIQUE (snapshotId, itemNo),
        CONSTRAINT fk_annualization_item_snapshot
            FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
    );
    CREATE INDEX idx_annualization_item_snapshot
        ON annualization_snapshot_item (snapshotId);
END;
