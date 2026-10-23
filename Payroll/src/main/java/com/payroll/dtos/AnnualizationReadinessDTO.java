package com.payroll.dtos;

import java.util.List;

public record AnnualizationReadinessDTO(boolean ready, List<String> issues) {
    public AnnualizationReadinessDTO {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }
}
