ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS substitutedFilingQualified BOOLEAN;
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS substitutedFilingReason VARCHAR(500);
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS substitutedFilingReviewedAt TIMESTAMP;
ALTER TABLE annualization_snapshot
    ADD COLUMN IF NOT EXISTS substitutedFilingReviewedBy VARCHAR(100);
