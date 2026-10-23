IF COL_LENGTH('annualization_snapshot', 'employeeLastName') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeLastName VARCHAR(100) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeFirstName') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeFirstName VARCHAR(100) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeMiddleName') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeMiddleName VARCHAR(100) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeExtensionName') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeExtensionName VARCHAR(50) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeNationality') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeNationality VARCHAR(100) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeEmploymentStatusCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeEmploymentStatusCode VARCHAR(50) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeEmploymentStatusName') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeEmploymentStatusName VARCHAR(200) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeEmploymentFrom') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeEmploymentFrom DATE NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeEmploymentTo') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeEmploymentTo DATE NULL;
