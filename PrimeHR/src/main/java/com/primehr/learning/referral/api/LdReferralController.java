package com.primehr.learning.referral.api;

import com.primehr.learning.referral.api.LdReferralDtos.*;
import com.primehr.learning.referral.application.LdReferralService;
import com.primehr.learning.referral.domain.LdReferralStatus;
import com.primehr.security.*;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/primehr/v1/ld-referrals")
public class LdReferralController {
    private final LdReferralService service; private final GapPermissionGuard permission; private final AgencyScopeResolver agency;
    public LdReferralController(LdReferralService service, GapPermissionGuard permission, AgencyScopeResolver agency) {
        this.service=service; this.permission=permission; this.agency=agency;
    }
    @GetMapping public PageResponse<SummaryResponse> list(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name="employeeNo",required=false) String employeeNo,
            @RequestParam(name="status",required=false) LdReferralStatus status,
            @RequestParam(name="page",defaultValue="0") int page,
            @RequestParam(name="size",defaultValue="20") int size) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ACCESS,token);
        return service.list(agency.resolveAgencyId(auth),employeeNo,status,page,size);
    }
    @GetMapping("/{referralId}") public Response get(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,@PathVariable("referralId") String referralId) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ACCESS,token);
        return service.get(agency.resolveAgencyId(auth),referralId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Response create(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value="X-Correlation-Id",required=false) String correlation,
            @Valid @RequestBody CreateRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ADD,token);
        return service.create(agency.resolveAgencyId(auth),request,correlation);
    }
    @PutMapping("/{referralId}") public Response update(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value="X-Correlation-Id",required=false) String correlation,
            @PathVariable("referralId") String referralId,@Valid @RequestBody UpdateRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.EDIT,token);
        return service.update(agency.resolveAgencyId(auth),referralId,request,correlation);
    }
    @PostMapping("/{referralId}/items") public Response addItems(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value="X-Correlation-Id",required=false) String correlation,
            @PathVariable("referralId") String referralId,@Valid @RequestBody AddItemsRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.EDIT,token);
        return service.addItems(agency.resolveAgencyId(auth),referralId,request,correlation);
    }
    @PostMapping("/{referralId}/items/{itemId}/archive") public Response archiveItem(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value="X-Correlation-Id",required=false) String correlation,
            @PathVariable("referralId") String referralId,@PathVariable("itemId") String itemId,@Valid @RequestBody ItemTransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ARCHIVE,token);
        return service.archiveItem(agency.resolveAgencyId(auth),referralId,itemId,request,correlation);
    }
    @PostMapping("/{referralId}/submit") public Response submit(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value="X-Correlation-Id",required=false) String correlation,
            @PathVariable("referralId") String referralId,@Valid @RequestBody TransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.SUBMIT,token);
        return service.submit(agency.resolveAgencyId(auth),referralId,request,correlation);
    }
    @PostMapping("/{referralId}/archive") public Response archive(Authentication auth,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestHeader(value="X-Correlation-Id",required=false) String correlation,
            @PathVariable("referralId") String referralId,@Valid @RequestBody TransitionRequest request) {
        permission.requireAgencyWide(GapPermissionGuard.REFERRAL,PrimeHrAction.ARCHIVE,token);
        return service.archive(agency.resolveAgencyId(auth),referralId,request,correlation);
    }
}
