IF OBJECT_ID('personaldata', 'U') IS NOT NULL
   AND COL_LENGTH('personaldata', 'rdoCode') IS NULL
    ALTER TABLE personaldata ADD rdoCode VARCHAR(10) NULL;
