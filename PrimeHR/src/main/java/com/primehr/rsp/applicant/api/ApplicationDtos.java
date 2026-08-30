package com.primehr.rsp.applicant.api;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ApplicationDtos {
    private ApplicationDtos() {}

    public record Create(@NotBlank String publicationId) {}
    public record Save(@NotEmpty List<@NotBlank String> documentIds, @NotNull Long recordVersion) {}
    public record Submit(@NotNull Long recordVersion) {}
    public record Withdraw(@NotBlank @Size(max = 1000) String reason, @NotNull Long recordVersion) {}
    public record StaffMessage(@NotBlank @Size(max = 300) String subject,
                               @NotBlank @Size(max = 4000) String body) {}

    public record DocumentEvidence(String id, String applicantDocumentId, String documentType,
                                   String originalFilename, String mediaType, long byteSize,
                                   String checksum, String classification, int displayOrder) {}
    public record Communication(String id, String direction, String channel, String subject, String body,
                                String actor, Instant occurredAt, Instant readAt, String correlationId) {}
    public record Application(String id, String applicantId, String publicationId, String vacancyTitle,
                              String placeOfAssignment, LocalDate openingDate, LocalDate closingDate,
                              int applicationVersion, String status, String safeStatus,
                              String acknowledgmentNumber, Instant submittedAt, Instant withdrawnAt,
                              String withdrawalReason, long recordVersion,
                              List<DocumentEvidence> documents) {}
}
