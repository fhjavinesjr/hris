package com.primehr.rsp.report;

import com.primehr.security.AgencyScopeResolver;
import com.primehr.security.RspFormalReportPermissionGuard;
import com.primehr.shared.audit.PrimeHrAuditService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/primehr/v1/rsp/reports")
public class RspFormalReportController {
    private final RspFormalReportService service;private final RspFormalReportPermissionGuard permission;private final AgencyScopeResolver agency;private final PrimeHrAuditService audit;
    public RspFormalReportController(RspFormalReportService service,RspFormalReportPermissionGuard permission,AgencyScopeResolver agency,PrimeHrAuditService audit){this.service=service;this.permission=permission;this.agency=agency;this.audit=audit;}
    @GetMapping(value="/comparative-evaluations/{proceedingId}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> comparative(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String proceedingId){authorize(RspFormalReportPermissionGuard.COMPARATIVE,token,a,proceedingId,correlation);return pdf("rsp-comparative-"+proceedingId,service.comparative(agency.resolveAgencyId(a),proceedingId,a.getName(),correlation));}
    @GetMapping(value="/selections/{selectionId}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> selection(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String selectionId){authorize(RspFormalReportPermissionGuard.SELECTION,token,a,selectionId,correlation);return pdf("rsp-selection-"+selectionId,service.selection(agency.resolveAgencyId(a),selectionId,a.getName(),correlation));}
    @GetMapping(value="/evidence-index/{selectionId}.pdf",produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> evidence(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestHeader(value="X-Correlation-Id",required=false)String correlation,@PathVariable String selectionId){authorize(RspFormalReportPermissionGuard.EVIDENCE_INDEX,token,a,selectionId,correlation);return pdf("rsp-evidence-index-"+selectionId,service.evidenceIndex(agency.resolveAgencyId(a),selectionId,a.getName(),correlation));}
    private void authorize(String feature,String token,Authentication authentication,String subject,String correlation){try{permission.require(feature,token);}catch(AccessDeniedException denied){String agencyId=agency.resolveAgencyId(authentication);audit.record(agencyId,"DENY_RSP_REPORT","RSP_REPORT_REQUEST",subject,null,null,null,java.util.Map.of("featureKey",feature,"outcome","DENIED"),null,correlation);throw denied;}}
    private static ResponseEntity<byte[]> pdf(String name,byte[] body){String safe=name.replaceAll("[^A-Za-z0-9._-]","_")+".pdf";return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.inline().filename(safe).build().toString()).body(body);}
}
