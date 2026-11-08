package com.administrative.controllers;

import com.administrative.dtos.PrimeHrReportSourceDtos.*;
import com.administrative.services.PrimeHrReportSourceService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/integration/v1/primehr/report-sources")
public class PrimeHrReportSourceIntegrationController {
    private final PrimeHrReportSourceService service;
    public PrimeHrReportSourceIntegrationController(PrimeHrReportSourceService service){this.service=service;}
    @GetMapping("/branding") public Branding branding(Authentication authentication){authenticated(authentication);return service.branding();}
    @GetMapping("/positions/{plantillaId}") public Position position(Authentication authentication,@PathVariable Long plantillaId){authenticated(authentication);return service.position(plantillaId);}
    private static void authenticated(Authentication value){if(value==null||!value.isAuthenticated())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Authentication is required");}
}
