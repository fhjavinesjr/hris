CREATE TABLE IF NOT EXISTS annualization_snapshot_item (
    id BIGSERIAL PRIMARY KEY,
    snapshotId BIGINT NOT NULL,
    itemNo VARCHAR(4) NOT NULL,
    description VARCHAR(500),
    amount NUMERIC(19,2) NOT NULL,
    CONSTRAINT uq_annualization_snapshot_item UNIQUE (snapshotId, itemNo),
    CONSTRAINT fk_annualization_item_snapshot
        FOREIGN KEY (snapshotId) REFERENCES annualization_snapshot(id)
);

CREATE INDEX IF NOT EXISTS idx_annualization_item_snapshot
    ON annualization_snapshot_item (snapshotId);
