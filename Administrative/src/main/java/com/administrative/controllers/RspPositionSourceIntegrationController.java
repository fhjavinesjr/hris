package com.administrative.controllers;

import com.administrative.dtos.*;
import com.administrative.entitymodels.*;
import com.administrative.impl.EffectiveAuthorizationServiceImpl;
import com.administrative.repositories.*;
import com.administrative.services.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.HexFormat;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController @RequestMapping("/api/integration/v1/primehr/rsp/position-sources")
public class RspPositionSourceIntegrationController {
    private final PlantillaRepository plantillas; private final JobPositionRepository positions;
    private final BusinessUnitsRepository units; private final QualificationStandardService standards;
    private final EffectiveAuthorizationService authorization;
    public RspPositionSourceIntegrationController(PlantillaRepository p,JobPositionRepository j,BusinessUnitsRepository u,QualificationStandardService q,EffectiveAuthorizationService a){plantillas=p;positions=j;units=u;standards=q;authorization=a;}
    @GetMapping("/{plantillaId}") public RspPositionSourceResponse get(Authentication auth,@PathVariable Long plantillaId,@RequestParam Long businessUnitId,@RequestParam(required=false) LocalDate asOf){
        require(auth);Plantilla pl=plantillas.findById(plantillaId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Plantilla was not found"));
        JobPosition job=positions.findById(pl.getJobPositionId()).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Plantilla Job Position was not found"));
        BusinessUnits unit=units.findById(businessUnitId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Business Unit was not found"));QualificationStandardDtos.Response qs=standards.effective(job.getJobPositionId(),asOf);
        Instant at=Instant.now();String raw=String.join("|",String.valueOf(pl.getPlantillaId()),pl.getPlantillaName(),String.valueOf(job.getJobPositionId()),job.getJobPositionName(),String.valueOf(job.getSalaryGrade()),String.valueOf(job.getSalaryStep()),String.valueOf(unit.getBusinessUnitsId()),String.valueOf(unit.getBusinessUnitsCode()),unit.getBusinessUnitsName(),qs.sourceFingerprint());
        return new RspPositionSourceResponse(pl.getPlantillaId(),pl.getPlantillaName(),job.getJobPositionId(),job.getJobPositionName(),job.getSalaryGrade(),job.getSalaryStep(),unit.getBusinessUnitsId(),unit.getBusinessUnitsCode(),unit.getBusinessUnitsName(),qs.id(),qs.definitionVersion(),qs.education(),qs.training(),qs.experience(),qs.eligibility(),qs.licenseRequirement(),qs.sourceBasis(),qs.effectiveFrom(),qs.effectiveTo(),sha(raw),at);}
    private void require(Authentication a){if(a==null||!a.isAuthenticated())throw new AccessDeniedException("Authentication is required");String role=a.getAuthorities().stream().findFirst().map(x->x.getAuthority()).orElse("");EffectiveFeaturePermissionResponse p=authorization.resolve(a.getName(),role,EffectiveAuthorizationServiceImpl.PRIMEHR_RSP_RECRUITMENT_PLANNING);if(!p.administrator()&&(!p.canAccess()||p.dataScope()!=PermissionDataScope.AGENCY_WIDE))throw new AccessDeniedException("RSP recruitment planning access is not permitted");}
    private static String sha(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
