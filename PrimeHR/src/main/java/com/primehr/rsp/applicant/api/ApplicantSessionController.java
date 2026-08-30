package com.primehr.rsp.applicant.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Applicant JWTs are stateless; logout acknowledges that the client must discard its token. */
@RestController
@RequestMapping("/api/primehr/applicant/v1/session")
@ConditionalOnProperty(name="primehr.applicant.enabled",havingValue="true")
public class ApplicantSessionController {
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // Token revocation is intentionally not claimed by the Phase 5B stateless session contract.
    }
}
