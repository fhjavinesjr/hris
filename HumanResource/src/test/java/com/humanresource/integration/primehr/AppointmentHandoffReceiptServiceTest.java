package com.humanresource.integration.primehr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class AppointmentHandoffReceiptServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AppointmentHandoffReceiptStore store = mock(AppointmentHandoffReceiptStore.class);
    private final AppointmentHandoffReceiptService service = new AppointmentHandoffReceiptService(store, mapper);

    @Test
    void persistsAReceiptWithoutCreatingAnEmployeeOrAppointment() throws Exception {
        var request = request("handoff-1", payload("handoff-1"));
        when(store.findByHandoff("agency-1", "handoff-1")).thenReturn(Optional.empty());
        when(store.findBySelection("agency-1", "selection-1")).thenReturn(Optional.empty());
        when(store.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.receive(request);

        assertEquals("RECEIVED", response.state());
        assertEquals("selection-1", response.selectionId());
        verify(store).insert(any());
    }

    @Test
    void returnsTheExistingReceiptForAnIdenticalRetry() throws Exception {
        var request = request("handoff-1", payload("handoff-1"));
        var first = stored(request);
        when(store.findByHandoff("agency-1", "handoff-1")).thenReturn(Optional.of(first));

        assertEquals(first.id(), service.receive(request).receiptId());
    }

    @Test
    void rejectsReuseOfAHandoffIdWithDifferentContent() throws Exception {
        var request = request("handoff-1", payload("handoff-1"));
        var changed = new AppointmentHandoffReceiptRecord("receipt-1", "agency-1", "handoff-1", 1,
                "different", "selection-1", "application-1", "applicant-1", "RECEIVED", "{}",
                java.time.Instant.now(), null, "actor-1", 0, java.time.Instant.now());
        when(store.findByHandoff("agency-1", "handoff-1")).thenReturn(Optional.of(changed));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.receive(request));
        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void rejectsAFingerprintThatDoesNotMatchTheSnapshot() throws Exception {
        var valid = request("handoff-1", payload("handoff-1"));
        var invalid = new AppointmentHandoffDtos.Request(valid.schemaVersion(), valid.handoffId(), valid.agencyId(),
                valid.selectionId(), valid.applicationId(), valid.applicantId(), "0".repeat(64),
                valid.payloadSnapshot(), valid.correlationId(), valid.sourceActor());

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.receive(invalid));
        assertEquals(400, error.getStatusCode().value());
        verify(store, never()).insert(any());
    }

    @Test
    void rejectsASnapshotWithoutFinalSelectionAndAcceptedOfferEvidence() throws Exception {
        JsonNode payload=payload("handoff-1");
        ((com.fasterxml.jackson.databind.node.ObjectNode)payload.path("offer")).put("status","DECLINED");
        var request=request("handoff-1",payload);
        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->service.receive(request));
        assertEquals(400,error.getStatusCode().value());
    }

    private AppointmentHandoffDtos.Request request(String handoffId, JsonNode payload) throws Exception {
        String canonical = mapper.writeValueAsString(payload);
        String fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        return new AppointmentHandoffDtos.Request(1, handoffId, "agency-1", "selection-1", "application-1",
                "applicant-1", fingerprint, payload, "correlation-1", "actor-1");
    }

    private JsonNode payload(String handoffId) throws Exception {
        return mapper.readTree("""
                {"schemaVersion":1,"handoffId":"%s","agencyId":"agency-1",
                 "selection":{"id":"selection-1","status":"FINALIZED","outcome":"SELECTED","sourceFingerprint":"%s","finalizedAt":"2026-09-01T00:00:00Z"},
                 "offer":{"status":"ACCEPTED","respondedAt":"2026-09-01T00:00:00Z"},
                 "applicant":{"applicationId":"application-1","applicantId":"applicant-1","givenName":"Ada","familyName":"Lovelace","email":"ada@example.test"},
                 "position":{"publicationId":"publication-1","vacancyRequestId":"vacancy-1","plantillaId":1,"jobPositionId":2,"businessUnitId":3,"administrativeFingerprint":"%s","hrmFingerprint":"%s"},
                 "consent":{"privacyNoticeId":"notice-1","privacyNoticeVersion":1,"applicationSubmittedAt":"2026-08-01T00:00:00Z"}}
                """.formatted(handoffId,"a".repeat(64),"b".repeat(64),"c".repeat(64)));
    }

    private AppointmentHandoffReceiptRecord stored(AppointmentHandoffDtos.Request request) throws Exception {
        var now = java.time.Instant.now();
        return new AppointmentHandoffReceiptRecord("receipt-1", request.agencyId(), request.handoffId(),
                request.schemaVersion(), request.sourceFingerprint(), request.selectionId(), request.applicationId(),
                request.applicantId(), "RECEIVED", mapper.writeValueAsString(request.payloadSnapshot()), now,
                request.correlationId(), request.sourceActor(), 0, now);
    }
}
