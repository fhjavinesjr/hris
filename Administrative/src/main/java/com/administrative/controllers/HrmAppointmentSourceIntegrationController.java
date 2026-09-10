package com.administrative.controllers;

import com.administrative.dtos.*;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.repositories.*;
import com.administrative.services.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.HexFormat;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/integration/v1/hrm/appointment-sources")
public class HrmAppointmentSourceIntegrationController {
    private final PlantillaRepository plantillas; private final JobPositionRepository positions;
    private final BusinessUnitsRepository units; private final NatureOfAppointmentRepository natures;
    private final SalaryScheduleRepository salaries; private final QualificationStandardService standards;
    private final EffectiveAuthorizationService authorization;

    public HrmAppointmentSourceIntegrationController(PlantillaRepository p,JobPositionRepository j,
            BusinessUnitsRepository u,NatureOfAppointmentRepository n,SalaryScheduleRepository s,
            QualificationStandardService q,EffectiveAuthorizationService a){plantillas=p;positions=j;units=u;natures=n;salaries=s;standards=q;authorization=a;}

    @GetMapping("/{plantillaId}")
    public HrmAppointmentSourceResponse get(Authentication auth,@PathVariable Long plantillaId,
            @RequestParam Long businessUnitId,@RequestParam Long natureOfAppointmentId,@RequestParam LocalDateTime assumptionDate){
        require(auth);
        var plantilla=plantillas.findById(plantillaId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Plantilla was not found"));
        var job=positions.findById(plantilla.getJobPositionId()).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Job Position was not found"));
        var unit=units.findById(businessUnitId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Business Unit was not found"));
        var nature=natures.findById(natureOfAppointmentId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Nature of Appointment was not found"));
        var salary=salaries.findFirstByEffectivityDateLessThanEqualAndSalaryGradeAndSalaryStepOrderByEffectivityDateDesc(assumptionDate,job.getSalaryGrade(),job.getSalaryStep());
        if(salary==null)throw new ResponseStatusException(CONFLICT,"No effective Salary Schedule exists for the assumption date");
        var qs=standards.effective(job.getJobPositionId(),assumptionDate.toLocalDate());
        BigDecimal monthly=salary.getMonthlySalary().setScale(2,RoundingMode.HALF_UP);
        BigDecimal annual=monthly.multiply(BigDecimal.valueOf(12)).setScale(2,RoundingMode.HALF_UP);
        BigDecimal daily=annual.divide(BigDecimal.valueOf(365),2,RoundingMode.HALF_UP);
        String recruitmentRaw=String.join("|",plantilla.getPlantillaId().toString(),plantilla.getPlantillaName(),job.getJobPositionId().toString(),job.getJobPositionName(),job.getSalaryGrade().toString(),job.getSalaryStep().toString(),unit.getBusinessUnitsId().toString(),String.valueOf(unit.getBusinessUnitsCode()),unit.getBusinessUnitsName(),qs.sourceFingerprint());
        String appointmentRaw=String.join("|",recruitmentRaw,nature.getNatureOfAppointmentId().toString(),nature.getCode(),nature.getNature(),salary.getSalaryScheduleId().toString(),salary.getEffectivityDate().toString(),monthly.toPlainString());
        return new HrmAppointmentSourceResponse(plantilla.getPlantillaId(),plantilla.getPlantillaName(),job.getJobPositionId(),job.getJobPositionName(),unit.getBusinessUnitsId(),unit.getBusinessUnitsCode(),unit.getBusinessUnitsName(),nature.getNatureOfAppointmentId(),nature.getCode(),nature.getNature(),salary.getSalaryScheduleId(),job.getSalaryGrade(),job.getSalaryStep(),monthly,annual,daily,sha(recruitmentRaw),sha(appointmentRaw),Instant.now());
    }

    private void require(Authentication auth){if(auth==null||!auth.isAuthenticated())throw new AccessDeniedException("Authentication is required");String role=auth.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");EffectiveFeaturePermissionResponse p=authorization.resolve(auth.getName(),role,EffectiveAuthorizationServiceImpl.HRM_APPOINTMENT_INTAKE);if(!p.administrator()&&(!p.canAccess()||!p.canFinalize()||p.dataScope()!=PermissionDataScope.AGENCY_WIDE))throw new AccessDeniedException("Agency-wide appointment intake finalization is required");}
    private static String sha(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
