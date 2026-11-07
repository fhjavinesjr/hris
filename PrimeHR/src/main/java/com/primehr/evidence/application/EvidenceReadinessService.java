package com.primehr.evidence.application;

import com.primehr.evidence.api.EvidenceReadinessDtos.SignOffInput;
import com.primehr.evidence.application.EvidenceReadinessData.*;
import com.primehr.integration.administrative.PermissionDataScope;

import java.time.LocalDate;
import java.util.List;

public interface EvidenceReadinessService {
    Calculation calculate(String agencyId, String caseId, LocalDate asOf, LocalDate expiringThrough,
            String actor, PermissionDataScope scope);
    List<Snapshot> snapshots(String agencyId, String caseId, String actor, PermissionDataScope scope);
    Snapshot signOff(String agencyId, String caseId, SignOffInput input, String actor,
            PermissionDataScope scope, String correlationId);
    ExceptionView exceptions(String agencyId, LocalDate asOf, LocalDate expiringThrough,
            String actor, PermissionDataScope scope);
    byte[] evidenceIndex(String agencyId, String caseId, String actor, PermissionDataScope scope);
    byte[] internalAssessment(String agencyId, String snapshotId, String actor, PermissionDataScope scope);
    Analytics analytics(String agencyId, LocalDate from, LocalDate to, String authorization,
            String actor, String correlationId);
}
