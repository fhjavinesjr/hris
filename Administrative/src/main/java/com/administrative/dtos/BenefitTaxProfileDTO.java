package com.administrative.dtos;

import java.math.BigDecimal;

public record BenefitTaxProfileDTO(Long id, String profileCode, Integer taxYear,
        String label, BigDecimal guidanceCeiling, String ceilingPeriod,
        String sharedPoolCode, String exemptBirItem, String taxableBirItem,
        String legalBasis, String sourceUrl) {}
