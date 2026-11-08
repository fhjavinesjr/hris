package com.primehr.evidence.application;

import com.primehr.evidence.application.EvidenceReadinessData.ModuleAggregate;

import java.time.LocalDate;
import java.util.List;

public interface EvidenceAggregateSourcePort {
    List<ModuleAggregate> load(String agencyId, LocalDate from, LocalDate to, String actor,
            String authorization, String correlationId);
}
