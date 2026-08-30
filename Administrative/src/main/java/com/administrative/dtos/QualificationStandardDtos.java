package com.administrative.dtos;

import com.administrative.entitymodels.QualificationStandardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public final class QualificationStandardDtos {
    private QualificationStandardDtos() {}
    public record Save(@NotNull Long jobPositionId,@NotBlank String education,@NotBlank String training,
            @NotBlank String experience,@NotBlank String eligibility,String licenseRequirement,String sourceBasis,
            LocalDate effectiveFrom,LocalDate effectiveTo,Long recordVersion) {}
    public record Transition(@NotNull Long recordVersion,String reason,LocalDate effectiveFrom) {}
    public record Response(Long id,Long jobPositionId,String jobPositionName,int definitionVersion,Long supersedesId,
            QualificationStandardStatus status,String education,String training,String experience,String eligibility,
            String licenseRequirement,String sourceBasis,LocalDate effectiveFrom,LocalDate effectiveTo,
            String publishedBy,Instant publishedAt,long recordVersion,String sourceFingerprint,Instant fetchedAt) {}
}
