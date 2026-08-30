package com.administrative.dtos;

import java.time.Instant;
import java.time.LocalDate;

public record RspPositionSourceResponse(Long plantillaId,String plantillaName,Long jobPositionId,
        String jobPositionName,Long salaryGrade,Long salaryStep,Long businessUnitId,String businessUnitCode,
        String businessUnitName,Long qualificationStandardId,int qualificationStandardVersion,
        String education,String training,String experience,String eligibility,String licenseRequirement,
        String sourceBasis,LocalDate qualificationEffectiveFrom,LocalDate qualificationEffectiveTo,
        String sourceFingerprint,Instant fetchedAt) {}
