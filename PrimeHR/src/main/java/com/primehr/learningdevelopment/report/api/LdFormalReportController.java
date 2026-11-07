package com.primehr.learningdevelopment.report.api;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.learningdevelopment.report.application.LdFormalReportService;
import com.primehr.security.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/learning-development/reports")
public class LdFormalReportController {
    private final LdFormalReportService service; private final AgencyScopeResolver agencies;
    private final LdReportingPermissionGuard reports; private final LdPlanningPermissionGuard planning;
    private final LdParticipationPermissionGuard participation; private final LdEvaluationPermissionGuard evaluation;

    public LdFormalReportController(LdFormalReportService service,AgencyScopeResolver agencies,
            LdReportingPermissionGuard reports,LdPlanningPermissionGuard planning,
            LdParticipationPermissionGuard participation,LdEvaluationPermissionGuard evaluation){this.service=service;this.agencies=agencies;this.reports=reports;this.planning=planning;this.participation=participation;this.evaluation=evaluation;}

    @GetMapping(value="/individual-plans/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> individualPlan(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,
            @RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){
        PermissionDataScope scope=intersect(reports.requireReport(token),planning.require(LdPlanningPermissionGuard.IDP,PrimeHrAction.ACCESS,token));
        return pdf("individual-development-plan-"+id,service.individualPlan(agencies.resolveAgencyId(auth),id,actor(auth),scope,correlation));
    }

    @GetMapping(value="/training-orders/{offeringId}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> trainingOrder(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,
            @RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String offeringId){
        PermissionDataScope scope=intersect(reports.requireReport(token),participation.require(LdParticipationPermissionGuard.DELIVERY,PrimeHrAction.ACCESS,token));
        return pdf("training-order-"+offeringId,service.trainingOrder(agencies.resolveAgencyId(auth),offeringId,actor(auth),scope,correlation));
    }

    @GetMapping(value="/completion-cases/{id}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> completion(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,
            @RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String id){
        return pdf("learning-completion-"+id,service.completionPacket(agencies.resolveAgencyId(auth),id,actor(auth),reports.requireReport(token),correlation));
    }

    @GetMapping(value="/evaluation-summaries/{completionId}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> evaluation(Authentication auth,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,
            @RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String completionId){
        PermissionDataScope scope=intersect(reports.requireReport(token),evaluation.require(LdEvaluationPermissionGuard.EVALUATION,PrimeHrAction.ACCESS,token));
        return pdf("learning-evaluation-"+completionId,service.evaluationSummary(agencies.resolveAgencyId(auth),completionId,actor(auth),scope,correlation));
    }

    private static PermissionDataScope intersect(PermissionDataScope a,PermissionDataScope b){if(a==b)return a;if(a==PermissionDataScope.AGENCY_WIDE)return b;if(b==PermissionDataScope.AGENCY_WIDE)return a;throw new org.springframework.security.access.AccessDeniedException("Workflow and report data scopes do not overlap");}
    private static String actor(Authentication auth){if(auth==null||auth.getName()==null||auth.getName().isBlank())throw new org.springframework.security.access.AccessDeniedException("Authenticated employee identity is required");return auth.getName();}
    private static ResponseEntity<byte[]> pdf(String name,byte[] body){String safe=name.replaceAll("[^A-Za-z0-9._-]","_")+".pdf";return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.inline().filename(safe).build().toString()).body(body);}
}
