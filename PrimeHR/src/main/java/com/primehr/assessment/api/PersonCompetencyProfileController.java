package com.primehr.assessment.api;
import com.primehr.assessment.api.AssessmentValidationDtos.PersonProfileResponse;
import com.primehr.assessment.application.AssessmentValidationService;
import com.primehr.integration.administrative.EffectiveFeaturePermission;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController @RequestMapping("/api/primehr/v1/person-profiles")
public class PersonCompetencyProfileController {
 private final AssessmentValidationService service;private final AssessmentPermissionGuard permission;private final AgencyScopeResolver agency;
 public PersonCompetencyProfileController(AssessmentValidationService service,AssessmentPermissionGuard permission,AgencyScopeResolver agency){this.service=service;this.permission=permission;this.agency=agency;}
 @GetMapping("/me") public PersonProfileResponse me(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestParam(name="asOf",required=false)LocalDate asOf){EffectiveFeaturePermission p=permission.require(AssessmentPermissionGuard.PERSON_PROFILE,PrimeHrAction.ACCESS,token);return service.latest(agency.resolveAgencyId(auth),auth.getName(),asOf,auth.getName(),p.dataScope());}
 @GetMapping("/me/history") public PageResponse<PersonProfileResponse> myHistory(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size){EffectiveFeaturePermission p=permission.require(AssessmentPermissionGuard.PERSON_PROFILE,PrimeHrAction.ACCESS,token);return service.history(agency.resolveAgencyId(auth),auth.getName(),page,size,auth.getName(),p.dataScope());}
 @GetMapping("/employees/{employeeNo}") public PersonProfileResponse employee(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable("employeeNo") String employeeNo,@RequestParam(name="asOf",required=false)LocalDate asOf){EffectiveFeaturePermission p=permission.require(AssessmentPermissionGuard.PERSON_PROFILE,PrimeHrAction.ACCESS,token);return service.latest(agency.resolveAgencyId(auth),employeeNo,asOf,auth.getName(),p.dataScope());}
 @GetMapping("/employees/{employeeNo}/history") public PageResponse<PersonProfileResponse> history(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable("employeeNo") String employeeNo,@RequestParam(name="page",defaultValue="0")int page,@RequestParam(name="size",defaultValue="20")int size){EffectiveFeaturePermission p=permission.require(AssessmentPermissionGuard.PERSON_PROFILE,PrimeHrAction.ACCESS,token);return service.history(agency.resolveAgencyId(auth),employeeNo,page,size,auth.getName(),p.dataScope());}
 @GetMapping("/versions/{profileVersionId}") public PersonProfileResponse version(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable("profileVersionId") String profileVersionId){EffectiveFeaturePermission p=permission.require(AssessmentPermissionGuard.PERSON_PROFILE,PrimeHrAction.ACCESS,token);return service.version(agency.resolveAgencyId(auth),profileVersionId,auth.getName(),p.dataScope());}
}
