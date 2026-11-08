package com.administrative.dtos;

public final class PrimeHrReportSourceDtos {
    private PrimeHrReportSourceDtos() { }
    public record Branding(String companyName,String address,String sourceFingerprint) { }
    public record Position(Long plantillaId,String plantillaName,Long jobPositionId,String jobPositionName,
            String sourceFingerprint) { }
}
