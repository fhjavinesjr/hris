package com.primehr.evidence.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class EvidenceReadinessDtos {
    private EvidenceReadinessDtos() { }

    public record SignOffInput(@NotNull Long caseRecordVersion, String supersedesSnapshotId,
            Long supersedesRecordVersion, @NotBlank @Size(max = 3000) String reason,
            LocalDate expiringThrough) { }
}
