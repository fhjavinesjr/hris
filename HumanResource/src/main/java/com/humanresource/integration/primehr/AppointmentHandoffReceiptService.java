package com.humanresource.integration.primehr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanresource.integration.primehr.AppointmentHandoffDtos.Receipt;
import com.humanresource.integration.primehr.AppointmentHandoffDtos.Request;
import com.humanresource.onboarding.AppointmentHandoffAcceptedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.List;

@Service
class AppointmentHandoffReceiptService {
    private final AppointmentHandoffReceiptStore store;
    private final ObjectMapper mapper;
    private final List<AppointmentHandoffAcceptedListener> listeners;

    AppointmentHandoffReceiptService(AppointmentHandoffReceiptStore store, ObjectMapper mapper) {
        this(store,mapper,List.of());
    }

    @Autowired AppointmentHandoffReceiptService(AppointmentHandoffReceiptStore store, ObjectMapper mapper,List<AppointmentHandoffAcceptedListener> listeners) {
        this.store = store;
        this.mapper = mapper;
        this.listeners=listeners;
    }

    @Transactional
    Receipt receive(Request request) {
        requireValidFingerprint(request);
        requireValidSnapshot(request);
        var existing = store.findByHandoff(request.agencyId(), request.handoffId());
        if (existing.isPresent()) {
            Receipt receipt = sameOrConflict(existing.get(), request);
            listeners.forEach(listener -> listener.accepted(existing.get()));
            return receipt;
        }
        store.findBySelection(request.agencyId(), request.selectionId()).ifPresent(receipt -> {
            throw conflict("This selection already has a different appointment handoff");
        });
        Instant now = Instant.now();
        AppointmentHandoffReceiptRecord value = new AppointmentHandoffReceiptRecord(
                UUID.randomUUID().toString(), request.agencyId(), request.handoffId(), request.schemaVersion(),
                request.sourceFingerprint(), request.selectionId(), request.applicationId(), request.applicantId(),
                "RECEIVED", json(request.payloadSnapshot()), now, request.correlationId(), request.sourceActor(), 0, now);
        try {
            AppointmentHandoffReceiptRecord inserted=store.insert(value);
            listeners.forEach(listener->listener.accepted(inserted));
            return response(inserted);
        } catch (DataIntegrityViolationException race) {
            return store.findByHandoff(request.agencyId(), request.handoffId())
                    .map(found -> {Receipt receipt=sameOrConflict(found,request);listeners.forEach(listener->listener.accepted(found));return receipt;})
                    .orElseThrow(() -> race);
        }
    }

    @Transactional(readOnly = true)
    Receipt find(String agencyId, String handoffId) {
        return store.findByHandoff(agencyId, handoffId).map(this::response)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment handoff receipt not found"));
    }

    private Receipt sameOrConflict(AppointmentHandoffReceiptRecord existing, Request request) {
        if (!existing.sourceFingerprint().equals(request.sourceFingerprint())
                || !existing.selectionId().equals(request.selectionId())
                || existing.schemaVersion() != request.schemaVersion()) {
            throw conflict("The handoff id was already used with different content");
        }
        return response(existing);
    }

    private void requireValidFingerprint(Request request) {
        if (!fingerprint(request.payloadSnapshot()).equalsIgnoreCase(request.sourceFingerprint())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceFingerprint does not match payloadSnapshot");
        }
    }

    private void requireValidSnapshot(Request request) {
        JsonNode root=request.payloadSnapshot();
        requireText(root,"handoffId",request.handoffId());requireText(root,"agencyId",request.agencyId());
        if(root.path("schemaVersion").asInt(-1)!=request.schemaVersion())bad("payload schemaVersion does not match the envelope");
        JsonNode selection=requireObject(root,"selection");requireText(selection,"id",request.selectionId());
        requireText(selection,"status","FINALIZED");requireText(selection,"outcome","SELECTED");requirePresent(selection,"sourceFingerprint");requirePresent(selection,"finalizedAt");
        JsonNode offer=requireObject(root,"offer");requireText(offer,"status","ACCEPTED");requirePresent(offer,"respondedAt");
        JsonNode applicant=requireObject(root,"applicant");requireText(applicant,"applicationId",request.applicationId());requireText(applicant,"applicantId",request.applicantId());requirePresent(applicant,"givenName");requirePresent(applicant,"familyName");requirePresent(applicant,"email");
        JsonNode position=requireObject(root,"position");for(String field:new String[]{"publicationId","vacancyRequestId","plantillaId","jobPositionId","businessUnitId","administrativeFingerprint","hrmFingerprint"})requirePresent(position,field);
        JsonNode consent=requireObject(root,"consent");requirePresent(consent,"privacyNoticeId");requirePresent(consent,"privacyNoticeVersion");requirePresent(consent,"applicationSubmittedAt");
    }

    private JsonNode requireObject(JsonNode root,String field){JsonNode value=root.path(field);if(!value.isObject())bad(field+" snapshot is required");return value;}
    private void requireText(JsonNode root,String field,String expected){if(!expected.equals(root.path(field).asText(null)))bad(field+" does not match the handoff envelope or required source state");}
    private void requirePresent(JsonNode root,String field){JsonNode value=root.get(field);if(value==null||value.isNull()||(value.isTextual()&&value.asText().isBlank()))bad(field+" is required in the handoff snapshot");}
    private void bad(String message){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}

    private String fingerprint(JsonNode payload) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json(payload).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String json(JsonNode value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("payloadSnapshot is not valid JSON", exception);
        }
    }

    private Receipt response(AppointmentHandoffReceiptRecord value) {
        try {
            return new Receipt(value.id(), value.handoffId(), value.agencyId(), value.schemaVersion(),
                    value.sourceFingerprint(), value.selectionId(), value.applicationId(), value.applicantId(),
                    value.state(), mapper.readTree(value.payloadSnapshot()), value.receivedAt(), value.correlationId(),
                    value.sourceActor(), value.recordVersion(), value.updatedAt());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored appointment handoff payload is invalid", exception);
        }
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
