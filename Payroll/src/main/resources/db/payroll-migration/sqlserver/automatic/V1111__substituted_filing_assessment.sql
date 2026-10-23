IF COL_LENGTH('annualization_snapshot', 'substitutedFilingQualified') IS NULL
    ALTER TABLE annualization_snapshot ADD substitutedFilingQualified BIT NULL;
IF COL_LENGTH('annualization_snapshot', 'substitutedFilingReason') IS NULL
    ALTER TABLE annualization_snapshot ADD substitutedFilingReason VARCHAR(500) NULL;
IF COL_LENGTH('annualization_snapshot', 'substitutedFilingReviewedAt') IS NULL
    ALTER TABLE annualization_snapshot ADD substitutedFilingReviewedAt DATETIME2 NULL;
IF COL_LENGTH('annualization_snapshot', 'substitutedFilingReviewedBy') IS NULL
    ALTER TABLE annualization_snapshot ADD substitutedFilingReviewedBy VARCHAR(100) NULL;
