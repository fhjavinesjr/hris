package com.administrative.dtos;

import java.time.LocalDate;
import java.util.List;

public record SupervisedBusinessUnitDTO(
        Long businessUnitId,
        String businessUnitName,
        String role,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<Long> personnelEmployeeIds
) {}
