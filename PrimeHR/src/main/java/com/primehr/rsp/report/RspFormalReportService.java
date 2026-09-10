package com.primehr.rsp.report;

public interface RspFormalReportService {
    byte[] comparative(String agencyId, String proceedingId, String actor, String correlationId);
    byte[] selection(String agencyId, String selectionId, String actor, String correlationId);
    byte[] evidenceIndex(String agencyId, String selectionId, String actor, String correlationId);
}
