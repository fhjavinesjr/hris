package com.humanresource.onboarding;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AdministrativeAppointmentSourceClient {
    record Source(Long plantillaId,String plantillaName,Long jobPositionId,String jobPositionName,Long businessUnitId,
            String businessUnitCode,String businessUnitName,Long natureOfAppointmentId,String natureCode,String natureName,
            Long salaryScheduleId,Long salaryGrade,Long salaryStep,BigDecimal monthlySalary,BigDecimal annualSalary,
            BigDecimal dailySalary,String recruitmentFingerprint,String sourceFingerprint){}
    Source resolve(String bearerToken,long plantillaId,long businessUnitId,long natureId,LocalDateTime assumptionDate);
}
