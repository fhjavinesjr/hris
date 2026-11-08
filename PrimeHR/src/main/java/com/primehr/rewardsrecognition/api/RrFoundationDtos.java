package com.primehr.rewardsrecognition.api;

import com.primehr.rewardsrecognition.domain.RrAwardCategoryVersion;
import com.primehr.rewardsrecognition.domain.RrCommitteeCharterVersion;
import com.primehr.rewardsrecognition.domain.RrCommitteeMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class RrFoundationDtos {
    private RrFoundationDtos() {
    }

    public record DefinitionTransition(@NotNull Long recordVersion,
                                       @NotBlank @Size(max = 1000) String reason) { }

    public record PublishTransition(@NotNull Long recordVersion, @NotNull LocalDate effectiveFrom,
                                    LocalDate effectiveTo,
                                    @NotBlank @Size(max = 1000) String reason) { }

    public record PolicyInput(@NotBlank @Size(max = 80) String code,
                              @NotBlank @Size(max = 200) String title,
                              @Size(max = 2000) String description,
                              @NotBlank @Size(max = 2000) String legalBasis,
                              @NotBlank @Size(max = 250) String policyReference,
                              boolean committeeRequired,
                              boolean allowsIndividualAwards,
                              boolean allowsGroupAwards,
                              boolean allowsOrganizationAwards,
                              boolean allowsMonetaryIncentives,
                              boolean allowsNonmonetaryIncentives,
                              boolean requiresAgencyHeadApproval,
                              boolean annualReportRequired,
                              Long recordVersion) { }

    public record PolicyResponse(String id, String policyId, String code, String title,
                                 String description, String legalBasis, String policyReference,
                                 boolean committeeRequired, boolean allowsIndividualAwards,
                                 boolean allowsGroupAwards, boolean allowsOrganizationAwards,
                                 boolean allowsMonetaryIncentives, boolean allowsNonmonetaryIncentives,
                                 boolean requiresAgencyHeadApproval, boolean annualReportRequired,
                                 int definitionVersion, String supersedesId, String status,
                                 LocalDate effectiveFrom, LocalDate effectiveTo, String publishedBy,
                                 Instant publishedAt, String retiredBy, Instant retiredAt,
                                 String retirementReason, long recordVersion) { }

    public record CriterionInput(@NotBlank @Size(max = 80) String code,
                                 @NotBlank @Size(max = 200) String title,
                                 @Size(max = 2000) String description,
                                 @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal weightPercent,
                                 @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal maximumScore,
                                 @NotBlank @Size(max = 3000) String evidenceGuidance,
                                 @Min(1) int displayOrder) { }

    public record CriteriaInput(@NotNull Long recordVersion,
                                @NotEmpty List<@Valid CriterionInput> criteria) { }

    public record CategoryInput(@NotBlank @Size(max = 80) String code,
                                @NotBlank @Size(max = 200) String title,
                                @Size(max = 2000) String description,
                                @NotNull RrAwardCategoryVersion.NomineeType nomineeType,
                                @NotNull RrAwardCategoryVersion.IncentiveType incentiveType,
                                @NotBlank @Size(max = 4000) String eligibilityGuidance,
                                @NotBlank @Size(max = 4000) String evidenceRequirements,
                                @DecimalMin("0") BigDecimal minimumScore,
                                Long recordVersion) { }

    public record CriterionResponse(String id, String code, String title, String description,
                                    BigDecimal weightPercent, BigDecimal maximumScore,
                                    String evidenceGuidance, int displayOrder) { }

    public record CategoryResponse(String id, String categoryId, String code, String title,
                                   String description, String nomineeType, String incentiveType,
                                   String eligibilityGuidance, String evidenceRequirements,
                                   BigDecimal minimumScore, int criteriaRevision,
                                   int definitionVersion, String supersedesId, String status,
                                   LocalDate effectiveFrom, LocalDate effectiveTo, String publishedBy,
                                   Instant publishedAt, String retiredBy, Instant retiredAt,
                                   String retirementReason, long recordVersion,
                                   List<CriterionResponse> criteria) { }

    public record CommitteeMemberInput(@NotBlank @Size(max = 100) String employeeNo,
                                       @NotBlank @Size(max = 250) String displayName,
                                       @NotBlank @Size(max = 250) String positionTitle,
                                       @NotNull RrCommitteeMember.Role memberRole,
                                       boolean votingMember,
                                       @NotNull LocalDate effectiveFrom,
                                       LocalDate effectiveTo,
                                       @Size(max = 120) String sourceVersion) { }

    public record CommitteeRosterInput(@NotNull Long recordVersion,
                                       @NotEmpty List<@Valid CommitteeMemberInput> members) { }

    public record CommitteeCharterInput(@NotBlank @Size(max = 80) String code,
                                        @NotBlank @Size(max = 200) String title,
                                        @Size(max = 2000) String description,
                                        @NotBlank @Size(max = 2000) String legalBasis,
                                        @NotNull RrCommitteeCharterVersion.QuorumType quorumType,
                                        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quorumValue,
                                        boolean requiresSecretariat,
                                        boolean requiresAgencyHeadDecision,
                                        boolean conflictDeclarationRequired,
                                        Long recordVersion) { }

    public record CommitteeMemberResponse(String id, String employeeNo, String displayName,
                                          String positionTitle, String memberRole, boolean votingMember,
                                          LocalDate effectiveFrom, LocalDate effectiveTo,
                                          String sourceVersion) { }

    public record CommitteeCharterResponse(String id, String charterId, String code, String title,
                                           String description, String legalBasis, String quorumType,
                                           BigDecimal quorumValue, boolean requiresSecretariat,
                                           boolean requiresAgencyHeadDecision,
                                           boolean conflictDeclarationRequired, int rosterRevision,
                                           int definitionVersion, String supersedesId, String status,
                                           LocalDate effectiveFrom, LocalDate effectiveTo,
                                           String publishedBy, Instant publishedAt, String retiredBy,
                                           Instant retiredAt, String retirementReason, long recordVersion,
                                           List<CommitteeMemberResponse> members) { }
}
