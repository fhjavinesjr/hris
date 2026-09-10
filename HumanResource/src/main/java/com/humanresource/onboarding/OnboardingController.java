package com.humanresource.onboarding;

import com.humanresource.onboarding.HrmPermissionGuard.Action;
import com.humanresource.onboarding.OnboardingDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/hrm/v1")
public class OnboardingController {
    static final String INTAKE="hrm.appointment-intake",ONBOARDING="hrm.onboarding",CONFIG="hrm.onboarding-configuration";
    private final OnboardingService service;private final HrmPermissionGuard permissions;
    public OnboardingController(OnboardingService s,HrmPermissionGuard p){service=s;permissions=p;}
    @PostMapping("/onboarding-templates")@ResponseStatus(HttpStatus.CREATED) public TemplateResponse createTemplate(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@Valid @RequestBody TemplateCommand command){permissions.require(token,CONFIG,Action.ADD);return service.createTemplate(agency,a.getName(),command);}
    @PostMapping("/onboarding-templates/{id}/publish") public TemplateResponse publishTemplate(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id){permissions.require(token,CONFIG,Action.PUBLISH);return service.publishTemplate(agency,id,a.getName());}
    @GetMapping("/appointment-intakes") public List<IntakeResponse> list(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency){permissions.require(token,INTAKE,Action.ACCESS);return service.list(agency);}
    @GetMapping("/appointment-intakes/{id}") public IntakeResponse get(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id){permissions.require(token,INTAKE,Action.ACCESS);return service.get(agency,id);}
    @PostMapping("/appointment-intakes/{id}/begin-review") public IntakeResponse begin(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@Valid @RequestBody VersionCommand command){permissions.require(token,ONBOARDING,Action.EDIT);return service.begin(agency,id,command.recordVersion(),a.getName());}
    @PostMapping("/appointment-intakes/{id}/return") public IntakeResponse returnCase(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@Valid @RequestBody ReasonCommand command){permissions.require(token,ONBOARDING,Action.EDIT);return service.returnCase(agency,id,command,a.getName());}
    @PutMapping("/appointment-intakes/{id}/identity-resolution") public IntakeResponse identity(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@Valid @RequestBody IdentityCommand command){permissions.require(token,ONBOARDING,Action.EDIT);return service.identity(agency,id,command,a.getName());}
    @PutMapping("/appointment-intakes/{id}/checklist/{itemId}") public IntakeResponse checklist(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@PathVariable String itemId,@Valid @RequestBody ChecklistCommand command){permissions.require(token,ONBOARDING,Action.EDIT);return service.checklist(agency,id,itemId,command,a.getName());}
    @PostMapping("/appointment-intakes/{id}/approve") public IntakeResponse approve(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@Valid @RequestBody VersionCommand command){permissions.require(token,ONBOARDING,Action.APPROVE);return service.approve(agency,id,command.recordVersion(),a.getName());}
    @PostMapping("/appointment-intakes/{id}/create-appointment") public AppointmentResult appointment(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@Valid @RequestBody AppointmentCommand command){permissions.require(token,INTAKE,Action.FINALIZE);return service.createAppointment(agency,id,command,a.getName(),token);}
    @PostMapping("/appointment-intakes/{id}/complete-onboarding") public IntakeResponse complete(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader("X-Agency-Id")String agency,@PathVariable String id,@Valid @RequestBody VersionCommand command){permissions.require(token,ONBOARDING,Action.FINALIZE);return service.complete(agency,id,command.recordVersion(),a.getName());}
}
