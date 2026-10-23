package com.payroll.dtos;

import java.time.LocalDate;

public record AnnualizationTaxIdentityDTO(
        String employerName,
        String employerTin,
        String employerBirBranchCode,
        String employerRdoCode,
        String employerAddress,
        String employerZipCode,
        String employeeTin,
        String employeeBirBranchCode,
        String employeeBirRegionCode,
        String employeeRdoCode,
        String employeeAddress,
        String employeeZipCode,
        String employeeLocalAddress,
        String employeeLocalZipCode,
        LocalDate employeeBirthDate,
        String employeeContactNo,
        String employeePosition,
        String employeeLastName,
        String employeeFirstName,
        String employeeMiddleName,
        String employeeExtensionName,
        String employeeNationality,
        String employeeEmploymentStatusCode,
        String employeeEmploymentStatusName,
        LocalDate employeeEmploymentFrom,
        LocalDate employeeEmploymentTo,
        String employeeBirSeparationReasonCode) {
}
