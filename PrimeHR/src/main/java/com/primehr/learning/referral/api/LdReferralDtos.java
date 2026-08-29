package com.primehr.learning.referral.api;

import com.primehr.gap.domain.*;
import com.primehr.learning.referral.domain.LdReferralStatus;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public final class LdReferralDtos {
    private LdReferralDtos() { }
    public record CreateRequest(@NotBlank String analysisId,
            @NotBlank @Size(max=4000) String developmentNeed,
            @NotBlank @Size(max=4000) String recommendedIntervention,
            LocalDate targetCompletionDate, @Size(max=1000) String referralReason,
            @Size(max=2000) String remarks) { }
    public record UpdateRequest(@NotBlank @Size(max=4000) String developmentNeed,
            @NotBlank @Size(max=4000) String recommendedIntervention,
            LocalDate targetCompletionDate, @Size(max=1000) String referralReason,
            @Size(max=2000) String remarks, @NotNull @PositiveOrZero Long recordVersion) { }
    public record AddItemsRequest(@NotEmpty List<@NotBlank String> gapItemIds,
            @NotNull @PositiveOrZero Long recordVersion) { }
    public record TransitionRequest(@NotNull @PositiveOrZero Long recordVersion,
            @Size(max=1000) String reason) { }
    public record ItemTransitionRequest(@NotNull @PositiveOrZero Long recordVersion) { }
    public record ItemResponse(String id, String gapItemId, String competencyCode, String competencyName,
            GapClassification classification, NotAssessedReason notAssessedReason, Integer gap,
            String priorityCode, String priorityLabel, Integer priorityRank, int displayOrder,
            boolean active, long recordVersion) { }
    public record SummaryResponse(String id, String analysisId, Long employeeId, String employeeNo,
            String employeeName, LocalDate analysisDate, String positionName, LdReferralStatus status,
            LocalDate targetCompletionDate, long activeItemCount, long recordVersion,
            String createdBy, Instant createdAt, String referredBy, Instant referredAt) { }
    public record Response(String id, String analysisId, Long employeeId, String employeeNo,
            String employeeName, LocalDate analysisDate, String positionName, LdReferralStatus status,
            String developmentNeed, String recommendedIntervention, LocalDate targetCompletionDate,
            String referralReason, String remarks, long recordVersion, String createdBy, Instant createdAt,
            String updatedBy, Instant updatedAt, String referredBy, Instant referredAt,
            List<ItemResponse> items) { }
}

