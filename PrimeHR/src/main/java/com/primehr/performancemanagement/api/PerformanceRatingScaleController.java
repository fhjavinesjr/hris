package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.api.PerformanceRatingScaleDtos.*;
import com.primehr.performancemanagement.application.PerformanceRatingScaleService;
import com.primehr.security.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/primehr/v1/performance-management/rating-scales")
public class PerformanceRatingScaleController {
 private final PerformanceRatingScaleService service; private final PerformanceRatingScalePermissionGuard permission; private final AgencyScopeResolver agency;
 public PerformanceRatingScaleController(PerformanceRatingScaleService service,PerformanceRatingScalePermissionGuard permission,AgencyScopeResolver agency){this.service=service;this.permission=permission;this.agency=agency;}
 @GetMapping public List<ScaleResponse> list(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token){permission.require(PrimeHrAction.ACCESS,token);return service.list(agency.resolveAgencyId(a));}
 @GetMapping("/{id}") public ScaleResponse get(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable String id){permission.require(PrimeHrAction.ACCESS,token);return service.get(agency.resolveAgencyId(a),id);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public ScaleResponse create(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@Valid @RequestBody ScaleInput input){permission.require(PrimeHrAction.ADD,token);return service.create(agency.resolveAgencyId(a),input,correlation);}
 @PutMapping("/{id}") public ScaleResponse update(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id,@Valid @RequestBody ScaleInput input){permission.require(PrimeHrAction.EDIT,token);return service.update(agency.resolveAgencyId(a),id,input,correlation);}
 @PutMapping("/{id}/bands") public ScaleResponse bands(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id,@Valid @RequestBody BandsInput input){permission.require(PrimeHrAction.EDIT,token);return service.bands(agency.resolveAgencyId(a),id,input,correlation);}
 @PostMapping("/{id}/revisions") @ResponseStatus(HttpStatus.CREATED) public ScaleResponse revision(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id,@Valid @RequestBody Transition input){permission.require(PrimeHrAction.ADD,token);return service.revision(agency.resolveAgencyId(a),id,input,correlation);}
 @PostMapping("/{id}/publish") public ScaleResponse publish(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id,@Valid @RequestBody PublishTransition input){permission.require(PrimeHrAction.PUBLISH,token);return service.publish(agency.resolveAgencyId(a),id,input,correlation);}
 @PostMapping("/{id}/retire") public ScaleResponse retire(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id,@Valid @RequestBody Transition input){permission.require(PrimeHrAction.PUBLISH,token);return service.retire(agency.resolveAgencyId(a),id,input,correlation);}
 @PostMapping("/{id}/preview") public PreviewResponse preview(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable String id,@Valid @RequestBody PreviewInput input){permission.require(PrimeHrAction.ACCESS,token);return service.preview(agency.resolveAgencyId(a),id,input);}
}
