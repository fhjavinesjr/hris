package com.primehr.rsp.applicant.api;

import com.primehr.rsp.applicant.application.ApplicantFoundationService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController @RequestMapping("/api/primehr/public/v1") @ConditionalOnProperty(name="primehr.applicant.enabled",havingValue="true")
public class PublicApplicantController {
    private final ApplicantFoundationService service; public PublicApplicantController(ApplicantFoundationService service){this.service=service;}
    @GetMapping("/privacy-notices/current") public ApplicantDtos.Notice notice(){return service.currentNotice();}
    @GetMapping("/vacancies") public List<ApplicantDtos.PublicVacancy> vacancies(){return service.publicVacancies();}
    @GetMapping("/vacancies/{id}") public ApplicantDtos.PublicVacancy vacancy(@PathVariable("id") String id){return service.publicVacancy(id);}
    @PostMapping("/applicant-accounts/register") @ResponseStatus(HttpStatus.CREATED) public ApplicantDtos.Session register(@Valid @RequestBody ApplicantDtos.Register request,HttpServletRequest http){return service.register(request,http.getRemoteAddr(),http.getHeader("User-Agent"));}
    @PostMapping("/applicant-sessions") public ApplicantDtos.Session login(@Valid @RequestBody ApplicantDtos.Login request){return service.login(request);}
}
