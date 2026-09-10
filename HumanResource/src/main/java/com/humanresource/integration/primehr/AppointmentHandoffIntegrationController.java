package com.humanresource.integration.primehr;

import com.humanresource.integration.primehr.AppointmentHandoffDtos.Receipt;
import com.humanresource.integration.primehr.AppointmentHandoffDtos.Request;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@ConditionalOnProperty(name="primehr.handoff.enabled", havingValue="true")
@RequestMapping("/api/integration/v1/primehr/appointment-handoffs")
class AppointmentHandoffIntegrationController {
    private final PrimeHrHandoffServiceTokenVerifier tokens;
    private final AppointmentHandoffReceiptService receipts;

    AppointmentHandoffIntegrationController(PrimeHrHandoffServiceTokenVerifier tokens,
                                             AppointmentHandoffReceiptService receipts) {
        this.tokens = tokens;
        this.receipts = receipts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    Receipt receive(@RequestHeader("Authorization") String authorization,
                    @RequestHeader("Idempotency-Key") String idempotencyKey,
                    @Valid @RequestBody Request request) {
        var principal = tokens.verify(authorization);
        requireAgency(principal, request.agencyId());
        if (!request.handoffId().equals(idempotencyKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key must equal handoffId");
        }
        return receipts.receive(request);
    }

    @GetMapping("/{handoffId}")
    Receipt find(@RequestHeader("Authorization") String authorization,
                 @PathVariable String handoffId) {
        var principal = tokens.verify(authorization);
        return receipts.find(principal.agencyId(), handoffId);
    }

    private void requireAgency(PrimeHrHandoffServiceTokenVerifier.ServicePrincipal principal, String agencyId) {
        if (!principal.agencyId().equals(agencyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Service token agency does not match the request");
        }
    }
}
