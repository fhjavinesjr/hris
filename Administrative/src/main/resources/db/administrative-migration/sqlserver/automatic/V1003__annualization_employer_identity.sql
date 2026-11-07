IF OBJECT_ID('settings', 'U') IS NOT NULL
   AND COL_LENGTH('settings', 'rdoCode') IS NULL
    ALTER TABLE settings ADD rdoCode VARCHAR(10) NULL;
