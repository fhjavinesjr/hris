package com.primehr.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PrimeHrAuditService {
    private final PrimeHrAuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public PrimeHrAuditService(PrimeHrAuditEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper.copy();
        this.objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    public void record(String agencyId, String action, String aggregateType, String aggregateId,
                       Integer businessVersion, Long recordVersion, Object before, Object after,
                       String reason, String correlationId) {
        repository.save(new PrimeHrAuditEvent(agencyId, currentActor(), action, aggregateType, aggregateId,
                businessVersion, recordVersion, json(before), json(after), reason, correlationId));
    }

    public String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !authentication.isAuthenticated()
                ? "system" : authentication.getName();
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Audit state could not be serialized", exception);
        }
    }
}
