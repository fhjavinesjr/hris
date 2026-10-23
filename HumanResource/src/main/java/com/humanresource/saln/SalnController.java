package com.humanresource.saln;

import com.humanresource.onboarding.HrmPermissionGuard;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/saln")
public class SalnController {
    private static final String REVIEW="hrm.saln.review";
    private static final String HISTORICAL="hrm.saln.historical";
    private static final String AUDIT="hrm.saln.audit";
    private static final String REQUIREMENTS="hrm.saln.requirements";
    private final SalnService service; private final HrmPermissionGuard permissions; private final SalnReportService reports;
    public SalnController(SalnService service,HrmPermissionGuard permissions,SalnReportService reports){this.service=service;this.permissions=permissions;this.reports=reports;}

    @GetMapping("/my") public List<SalnDtos.Summary> my(Authentication a){return service.myList(actor(a));}
    @GetMapping("/my/requirements") public List<SalnDtos.RequirementResponse> myRequirements(Authentication a){return service.myRequirements(actor(a));}
    @GetMapping("/my/{id}") public SalnDtos.Response myGet(Authentication a,@PathVariable Long id){return service.myGet(actor(a),id);}
    @GetMapping("/my/{id}/corrections") public List<SalnCorrection> myCorrections(Authentication a,@PathVariable Long id){return service.myCorrections(actor(a),id);}
    @GetMapping("/my/{id}/versions") public List<SalnVersion> myVersions(Authentication a,@PathVariable Long id){service.myGet(actor(a),id);return service.versions(id);}
    @PostMapping("/my") public SalnDtos.Response create(Authentication a,@Valid @RequestBody SalnDtos.DraftRequest request){return service.createDraft(actor(a),request);}
    @PutMapping("/my/{id}") public SalnDtos.Response update(Authentication a,@PathVariable Long id,@Valid @RequestBody SalnDtos.DraftRequest request){return service.updateDraft(actor(a),id,request);}
    @DeleteMapping("/my/{id}/draft") public void discard(Authentication a,@PathVariable Long id){service.discardDraft(actor(a),id);}
    @PostMapping("/my/{id}/submit") public SalnDtos.Response submit(Authentication a,@PathVariable Long id){return service.submit(actor(a),id,false);}
    @PostMapping("/my/{id}/resubmit") public SalnDtos.Response resubmit(Authentication a,@PathVariable Long id){return service.submit(actor(a),id,true);}
    @GetMapping(value="/my/{id}/pdf",produces=MediaType.APPLICATION_PDF_VALUE) public void myPdf(Authentication a,@PathVariable Long id,HttpServletResponse response)throws Exception{pdfHeaders(response,id);reports.generateForEmployee(actor(a),id,response.getOutputStream());}

    @GetMapping("/admin") public List<SalnDtos.Summary> adminList(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,
            @RequestParam(required=false)Integer year,@RequestParam(required=false)SalnTypes.FilingType filingType,
            @RequestParam(required=false)SalnTypes.Status status,@RequestParam(required=false)String employee){permissions.require(REVIEW,HrmPermissionGuard.Action.ACCESS,token);return service.adminList(year,filingType,status,employee);}
    @GetMapping("/admin/dashboard") public SalnDtos.Dashboard dashboard(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestParam(required=false)Integer year,@RequestParam(required=false)SalnTypes.FilingType filingType){permissions.require(REVIEW,HrmPermissionGuard.Action.ACCESS,token);return service.dashboard(year,filingType);}
    @GetMapping(value="/admin/report/compliance",produces="text/csv") public ResponseEntity<byte[]> complianceReport(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestParam(required=false)Integer year,@RequestParam(required=false)SalnTypes.FilingType filingType,@RequestParam(required=false)SalnTypes.Status status,@RequestParam(required=false)String employee){permissions.require(REVIEW,HrmPermissionGuard.Action.ACCESS,token);byte[] body=service.complianceCsv(year,filingType,status,employee).getBytes(StandardCharsets.UTF_8);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"SALN_Compliance.csv\"").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(body);}
    @GetMapping("/admin/{id}") public SalnDtos.Response adminGet(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id){permissions.require(REVIEW,HrmPermissionGuard.Action.ACCESS,token);return service.adminGet(id);}
    @PostMapping("/admin/{id}/start-review") public SalnDtos.Response startReview(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id){permissions.require(REVIEW,HrmPermissionGuard.Action.EDIT,token);return service.startReview(id,actor(a));}
    @PostMapping("/admin/{id}/return-for-correction") public SalnDtos.Response returnForCorrection(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id,@Valid @RequestBody SalnDtos.CorrectionRequest request){permissions.require(REVIEW,HrmPermissionGuard.Action.EDIT,token);return service.returnForCorrection(id,actor(a),request);}
    @PostMapping("/admin/{id}/mark-compliant") public SalnDtos.Response compliant(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id,@Valid @RequestBody SalnDtos.RemarksRequest request){permissions.require(REVIEW,HrmPermissionGuard.Action.APPROVE,token);return service.markCompliant(id,actor(a),request.remarks());}
    @PostMapping("/admin/{id}/lock") public SalnDtos.Response lock(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id,@Valid @RequestBody SalnDtos.RemarksRequest request){permissions.require(REVIEW,HrmPermissionGuard.Action.FINALIZE,token);return service.lock(id,actor(a),request.remarks());}
    @PostMapping("/admin/{id}/repository-submission") public SalnDtos.Response repository(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id,@Valid @RequestBody SalnDtos.RepositoryRequest request){permissions.require(REVIEW,HrmPermissionGuard.Action.FINALIZE,token);return service.repositorySubmission(id,actor(a),request);}
    @PostMapping("/admin/{id}/void") public SalnDtos.Response voidSaln(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id,@Valid @RequestBody SalnDtos.VoidRequest request){permissions.require(REVIEW,HrmPermissionGuard.Action.DELETE,token);return service.voidSaln(id,actor(a),request);}
    @PostMapping("/admin/historical") public SalnDtos.Response historical(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@Valid @RequestBody SalnDtos.HistoricalRequest request){permissions.require(HISTORICAL,HrmPermissionGuard.Action.ADD,token);return service.encodeHistorical(actor(a),request);}
    @GetMapping("/admin/{id}/audit") public List<SalnAudit> audit(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id){permissions.require(AUDIT,HrmPermissionGuard.Action.ACCESS,token);return service.audit(id);}
    @GetMapping("/admin/{id}/versions") public List<SalnVersion> versions(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id){permissions.require(AUDIT,HrmPermissionGuard.Action.ACCESS,token);return service.versions(id);}
    @GetMapping(value="/admin/{id}/pdf",produces=MediaType.APPLICATION_PDF_VALUE) public void adminPdf(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@PathVariable Long id,HttpServletResponse response)throws Exception{permissions.require(REVIEW,HrmPermissionGuard.Action.ACCESS,token);pdfHeaders(response,id);reports.generateForAdmin(id,actor(a),response.getOutputStream());}
    @GetMapping("/admin/requirements/all") public List<SalnDtos.RequirementResponse> requirements(@RequestHeader(HttpHeaders.AUTHORIZATION)String token){permissions.require(REQUIREMENTS,HrmPermissionGuard.Action.ACCESS,token);return service.requirements();}
    @PostMapping("/admin/requirements") public SalnDtos.RequirementResponse requirement(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@Valid @RequestBody SalnDtos.RequirementRequest request){permissions.require(REQUIREMENTS,HrmPermissionGuard.Action.ADD,token);return service.createRequirement(actor(a),request);}
    @GetMapping("/admin/requirements/annual-preview") public SalnDtos.AnnualBulkRequirementResult annualRequirementPreview(@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@RequestParam Integer year){permissions.require(REQUIREMENTS,HrmPermissionGuard.Action.ADD,token);return service.previewAnnualRequirements(year);}
    @PostMapping("/admin/requirements/annual-generate") public SalnDtos.AnnualBulkRequirementResult generateAnnualRequirements(Authentication a,@RequestHeader(HttpHeaders.AUTHORIZATION)String token,@Valid @RequestBody SalnDtos.AnnualBulkRequirementRequest request){permissions.require(REQUIREMENTS,HrmPermissionGuard.Action.ADD,token);return service.generateAnnualRequirements(actor(a),request.salnYear());}
    private String actor(Authentication a){return a==null||a.getName()==null?"":a.getName();}
    private void pdfHeaders(HttpServletResponse response,Long id){response.setContentType(MediaType.APPLICATION_PDF_VALUE);response.setHeader(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"SALN_"+id+".pdf\"");}
}
