package com.humanresource.integration.primehr;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/integration/v1/primehr/plantilla-occupancy")
public class PlantillaOccupancyIntegrationController {
    private final PlantillaOccupancyIntegrationService service;private final RspPlanningAuthorization authorization;
    public PlantillaOccupancyIntegrationController(PlantillaOccupancyIntegrationService s,RspPlanningAuthorization a){service=s;authorization=a;}
    @GetMapping("/{plantillaId}") public PlantillaOccupancyResponse get(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,@PathVariable Long plantillaId){authorization.requireAgencyWideAccess(token);return service.get(plantillaId);}
}
