package com.humanresource.dtos;

import java.time.LocalDate;

public record EmployeeAnnualizationIdentityDTO(
        String employeeNo,
        String tin,
        String rdoCode,
        String birBranchCode,
        String birRegionCode,
        String address,
        String zipCode,
        String localAddress,
        String localZipCode,
        LocalDate birthDate,
        String contactNo,
        Long jobPositionId,
        String lastName,
        String firstName,
        String middleName,
        String extensionName,
        String nationality,
        Long natureOfAppointmentId,
        Integer natureOfSeparationId,
        LocalDate employmentFrom,
        LocalDate employmentTo) {
}
