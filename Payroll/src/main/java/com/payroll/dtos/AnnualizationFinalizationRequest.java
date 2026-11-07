package com.payroll.dtos;

public record AnnualizationFinalizationRequest(
        String remarks,
        Boolean substitutedFilingQualified,
        String substitutedFilingReason) {
}
