package com.administrative.controllers;

import com.administrative.dtos.*;
import com.administrative.entitymodels.*;
import com.administrative.repositories.*;
import com.administrative.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HrmAppointmentSourceIntegrationControllerTest {
    @Test void resolvesAuthoritativeSalaryAndUsesTheAdministrative365DayEquivalent(){
        PlantillaRepository plantillas=mock(PlantillaRepository.class);JobPositionRepository jobs=mock(JobPositionRepository.class);
        BusinessUnitsRepository units=mock(BusinessUnitsRepository.class);NatureOfAppointmentRepository natures=mock(NatureOfAppointmentRepository.class);
        SalaryScheduleRepository salaries=mock(SalaryScheduleRepository.class);QualificationStandardService standards=mock(QualificationStandardService.class);
        EffectiveAuthorizationService auth=mock(EffectiveAuthorizationService.class);
        Plantilla plantilla=new Plantilla(10L,"P-10",20L);JobPosition job=new JobPosition("HR Officer",12L,1L);job.setJobPositionId(20L);
        BusinessUnits unit=new BusinessUnits();unit.setBusinessUnitsId(30L);unit.setBusinessUnitsCode("HR");unit.setBusinessUnitsName("Human Resource");
        NatureOfAppointment nature=new NatureOfAppointment(40L,"P","Permanent");SalarySchedule salary=new SalarySchedule();salary.setSalaryScheduleId(50L);
        salary.setEffectivityDate(LocalDateTime.of(2026,1,1,0,0));salary.setSalaryGrade(12L);salary.setSalaryStep(1L);salary.setMonthlySalary(new BigDecimal("30000.00"));
        QualificationStandardDtos.Response qs=mock(QualificationStandardDtos.Response.class);when(qs.sourceFingerprint()).thenReturn("q".repeat(64));
        when(plantillas.findById(10L)).thenReturn(Optional.of(plantilla));when(jobs.findById(20L)).thenReturn(Optional.of(job));when(units.findById(30L)).thenReturn(Optional.of(unit));when(natures.findById(40L)).thenReturn(Optional.of(nature));when(salaries.findFirstByEffectivityDateLessThanEqualAndSalaryGradeAndSalaryStepOrderByEffectivityDateDesc(any(),eq(12L),eq(1L))).thenReturn(salary);when(standards.effective(eq(20L),any())).thenReturn(qs);when(auth.resolve(any(),any(),any())).thenReturn(EffectiveFeaturePermissionResponse.administrator("hrm.appointment-intake"));
        var authentication=new UsernamePasswordAuthenticationToken("admin",null,List.of(new SimpleGrantedAuthority("1")));
        var result=new HrmAppointmentSourceIntegrationController(plantillas,jobs,units,natures,salaries,standards,auth).get(authentication,10L,30L,40L,LocalDateTime.of(2026,9,1,0,0));
        assertEquals(new BigDecimal("360000.00"),result.annualSalary());assertEquals(new BigDecimal("986.30"),result.dailySalary());assertEquals(64,result.recruitmentFingerprint().length());assertEquals(64,result.sourceFingerprint().length());
    }
}
