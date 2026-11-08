package com.primehr.integration.administrative;

public interface AdministrativePerformanceReportSourceClient {
    record Branding(String companyName,String address,String sourceFingerprint) { }
    record Position(Long plantillaId,String plantillaName,Long jobPositionId,String jobPositionName,String sourceFingerprint) { }
    Branding branding(String token);
    Position position(Long plantillaId,String token);
}
