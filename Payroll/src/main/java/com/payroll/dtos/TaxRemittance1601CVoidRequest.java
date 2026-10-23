package com.payroll.dtos;

public record TaxRemittance1601CVoidRequest(
        String reason,
        Boolean confirmedNotFiledOrRemitted) {}
