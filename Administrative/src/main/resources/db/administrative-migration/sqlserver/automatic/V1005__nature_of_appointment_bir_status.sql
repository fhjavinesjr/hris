IF COL_LENGTH('natureofappointment', 'birEmploymentStatusCode') IS NULL
BEGIN
    ALTER TABLE natureofappointment
        ADD birEmploymentStatusCode VARCHAR(2) NULL;
END;
