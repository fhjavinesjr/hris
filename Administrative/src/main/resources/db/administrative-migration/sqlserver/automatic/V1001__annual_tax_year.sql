IF OBJECT_ID('wtaxcontribution', 'U') IS NOT NULL
    AND COL_LENGTH('wtaxcontribution', 'taxYear') IS NULL
BEGIN
    ALTER TABLE wtaxcontribution ADD taxYear INT NULL;
END;
