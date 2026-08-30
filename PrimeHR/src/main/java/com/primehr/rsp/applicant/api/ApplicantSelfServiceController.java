package com.primehr.rsp.applicant.api;

import com.primehr.rsp.applicant.application.ApplicantFoundationService;import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.core.io.InputStreamResource;import org.springframework.http.*;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController @RequestMapping("/api/primehr/applicant/v1/me") @ConditionalOnProperty(name="primehr.applicant.enabled",havingValue="true")
public class ApplicantSelfServiceController {
    private final ApplicantFoundationService service;public ApplicantSelfServiceController(ApplicantFoundationService service){this.service=service;}
    @GetMapping public ApplicantDtos.Account me(Authentication a){return service.me(a.getName());}
    @PutMapping public ApplicantDtos.Account saveAccount(Authentication a,@Valid @RequestBody ApplicantDtos.UpdateAccount r){return service.saveAccount(a.getName(),r);}
    @GetMapping("/profile") public ApplicantDtos.Profile profile(Authentication a){return service.profile(a.getName());}
    @PutMapping("/profile") public ApplicantDtos.Profile save(Authentication a,@Valid @RequestBody ApplicantDtos.SaveProfile r){return service.saveProfile(a.getName(),r);}
    @PostMapping("/consents") @ResponseStatus(HttpStatus.NO_CONTENT) public void consent(Authentication a,HttpServletRequest h){service.acceptCurrentNotice(a.getName(),h.getRemoteAddr(),h.getHeader("User-Agent"));}
    @GetMapping("/documents") public List<ApplicantDtos.Document> documents(Authentication a){return service.documents(a.getName());}
    @PostMapping(value="/documents",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED) public ApplicantDtos.Document upload(Authentication a,@RequestParam("documentType") String type,@RequestParam(defaultValue="SENSITIVE") String classification,@RequestPart("file") MultipartFile file){return service.upload(a.getName(),type,classification,file,null);}
    @PostMapping(value="/documents/{id}/replace",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApplicantDtos.Document replace(Authentication a,@PathVariable String id,@RequestParam("documentType") String type,@RequestParam(defaultValue="SENSITIVE") String classification,@RequestPart("file") MultipartFile file){return service.upload(a.getName(),type,classification,file,id);}
    @GetMapping("/documents/{id}/content") public ResponseEntity<InputStreamResource> download(Authentication a,@PathVariable String id){var d=service.download(a.getName(),id);return ResponseEntity.ok().contentType(MediaType.parseMediaType(d.mediaType())).contentLength(d.size()).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(d.filename()).build().toString()).body(new InputStreamResource(d.stream()));}
    @DeleteMapping("/documents/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(Authentication a,@PathVariable String id,@RequestParam("recordVersion") long version){service.deactivateDocument(a.getName(),id,version);}
}
