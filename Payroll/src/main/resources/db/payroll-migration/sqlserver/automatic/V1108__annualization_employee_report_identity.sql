IF COL_LENGTH('annualization_snapshot', 'employeeLocalAddress') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeLocalAddress VARCHAR(500) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeLocalZipCode') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeLocalZipCode VARCHAR(10) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeBirthDate') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeBirthDate DATE NULL;

IF COL_LENGTH('annualization_snapshot', 'employeeContactNo') IS NULL
    ALTER TABLE annualization_snapshot ADD employeeContactNo VARCHAR(50) NULL;

IF COL_LENGTH('annualization_snapshot', 'employeePosition') IS NULL
    ALTER TABLE annualization_snapshot ADD employeePosition VARCHAR(200) NULL;
