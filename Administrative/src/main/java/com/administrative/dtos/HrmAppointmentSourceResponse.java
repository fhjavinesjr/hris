package com.administrative.dtos;

import java.math.BigDecimal;
import java.time.Instant;

public record HrmAppointmentSourceResponse(Long plantillaId,String plantillaName,Long jobPositionId,
        String jobPositionName,Long businessUnitId,String businessUnitCode,String businessUnitName,
        Long natureOfAppointmentId,String natureCode,String natureName,Long salaryScheduleId,
        Long salaryGrade,Long salaryStep,BigDecimal monthlySalary,BigDecimal annualSalary,
        BigDecimal dailySalary,String recruitmentFingerprint,String sourceFingerprint,Instant fetchedAt) {}
