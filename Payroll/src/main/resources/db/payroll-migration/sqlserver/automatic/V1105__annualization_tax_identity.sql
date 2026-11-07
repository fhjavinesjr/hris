IF OBJECT_ID('previous_employer_tax_data', 'U') IS NOT NULL AND COL_LENGTH('previous_employer_tax_data', 'employerAddress') IS NULL
    ALTER TABLE previous_employer_tax_data ADD employerAddress VARCHAR(500) NULL;
IF OBJECT_ID('previous_employer_tax_data', 'U') IS NOT NULL AND COL_LENGTH('previous_employer_tax_data', 'employerZipCode') IS NULL
    ALTER TABLE previous_employer_tax_data ADD employerZipCode VARCHAR(10) NULL;

IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employerName') IS NULL
    ALTER TABLE annualization_snapshot ADD employerName VARCHAR(200) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employerTin') IS NULL
    ALTER TABLE annualization_snapshot ADD employerTin VARCHAR(30) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employerRdoCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employerRdoCode VARCHAR(10) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employerAddress') IS NULL
    ALTER TABLE annualization_snapshot ADD employerAddress VARCHAR(500) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employerZipCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employerZipCode VARCHAR(10) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employeeTin') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeTin VARCHAR(30) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employeeRdoCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeRdoCode VARCHAR(10) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employeeAddress') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeAddress VARCHAR(500) NULL;
IF OBJECT_ID('annualization_snapshot', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot', 'employeeZipCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeZipCode VARCHAR(10) NULL;

IF OBJECT_ID('annualization_snapshot_previous', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot_previous', 'employerAddress') IS NULL
    ALTER TABLE annualization_snapshot_previous ADD employerAddress VARCHAR(500) NULL;
IF OBJECT_ID('annualization_snapshot_previous', 'U') IS NOT NULL AND COL_LENGTH('annualization_snapshot_previous', 'employerZipCode') IS NULL
    ALTER TABLE annualization_snapshot_previous ADD employerZipCode VARCHAR(10) NULL;
