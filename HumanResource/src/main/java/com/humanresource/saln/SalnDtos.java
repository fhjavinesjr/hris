package com.humanresource.saln;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class SalnDtos {
    private SalnDtos() {}

    public record DraftRequest(
            @NotNull SalnTypes.FilingType filingType,
            @NotNull @Min(1900) @Max(2200) Integer salnYear,
            @NotNull LocalDate referenceDate,
            String declarantMiddleInitial, String declarantPosition,
            String declarantAgencyOffice, String declarantOfficeAddress,
            String spouseFullName, String spousePosition, String spouseAgencyOffice, String spouseOfficeAddress,
            @NotNull SalnTypes.FilingMode filingMode, String multipleSpouses,
            boolean businessInterestsNone, boolean relativesInGovernmentNone,
            boolean certificationAccepted, String governmentIdType, String governmentIdNo, LocalDate governmentIdDateIssued,
            @Valid List<DependentItem> dependents, @Valid List<RealPropertyItem> realProperties,
            @Valid List<PersonalPropertyItem> personalProperties, @Valid List<LiabilityItem> liabilities,
            @Valid List<BusinessInterestItem> businessInterests, @Valid List<GovernmentRelativeItem> governmentRelatives,
            String remarks) {
        public DraftRequest {
            declarantAgencyOffice = declarantAgencyOffice == null ? "" : declarantAgencyOffice;
            declarantOfficeAddress = declarantOfficeAddress == null ? "" : declarantOfficeAddress;
        }
    }

    public record DependentItem(@NotBlank String name,@NotBlank String relationship,@NotNull @Min(0) @Max(17) Integer age) {}
    public record RealPropertyItem(@NotNull SalnTypes.OwnerType ownerType,String ownerName,@NotBlank String description,
            @NotBlank String kind,@NotBlank String exactLocation,@PositiveOrZero BigDecimal assessedValue,
            @PositiveOrZero BigDecimal fairMarketValue,@Min(1000) @Max(2200) Integer acquisitionYear,
            String acquisitionMode,@NotNull @PositiveOrZero BigDecimal acquisitionCost) {}
    public record PersonalPropertyItem(@NotNull SalnTypes.OwnerType ownerType,String ownerName,@NotBlank String description,
            @Min(1000) @Max(2200) Integer acquisitionYear,@NotNull @PositiveOrZero BigDecimal acquisitionCost) {}
    public record LiabilityItem(@NotNull SalnTypes.OwnerType ownerType,String ownerName,@NotBlank String nature,
            @NotBlank String creditorName,@NotNull @PositiveOrZero BigDecimal outstandingBalance) {}
    public record BusinessInterestItem(@NotNull SalnTypes.OwnerType ownerType,String ownerName,@NotBlank String entityName,
            @NotBlank String businessAddress,@NotBlank String nature,LocalDate dateAcquired) {}
    public record GovernmentRelativeItem(@NotBlank String name,@NotBlank String relationship,@NotBlank String position,
            @NotBlank String agencyOfficeAddress) {}

    public record Totals(BigDecimal realProperties,BigDecimal personalProperties,BigDecimal totalAssets,
            BigDecimal totalLiabilities,BigDecimal netWorth) {}
    public record Response(Long id,Long employeeId,String employeeNo,SalnTypes.FilingType filingType,Integer salnYear,
            LocalDate referenceDate,LocalDate asOfDate,LocalDate dueDate,LocalDate submissionDate,SalnTypes.Status status,
            SalnTypes.SourceType sourceType,int versionNo,String declarantFamilyName,String declarantFirstName,
            String declarantMiddleInitial,String declarantPosition,String declarantAgencyOffice,String declarantOfficeAddress,
            String spouseFullName,String spousePosition,String spouseAgencyOffice,String spouseOfficeAddress,
            SalnTypes.FilingMode filingMode,String multipleSpouses,boolean businessInterestsNone,
            boolean relativesInGovernmentNone,boolean certificationAccepted,String governmentIdType,String governmentIdNo,
            LocalDate governmentIdDateIssued,List<DependentItem> dependents,List<RealPropertyItem> realProperties,
            List<PersonalPropertyItem> personalProperties,List<LiabilityItem> liabilities,
            List<BusinessInterestItem> businessInterests,List<GovernmentRelativeItem> governmentRelatives,
            Totals totals,String remarks,String sourceDocumentReference,String repositoryAgency,
            LocalDateTime repositorySubmittedAt,String repositoryReferenceNo,String repositoryRemarks,
            LocalDateTime createdAt,LocalDateTime updatedAt,LocalDateTime submittedAt,String reviewedBy,
            LocalDateTime reviewedAt,String compliantBy,LocalDateTime compliantAt,LocalDateTime lockedAt) {}
    public record Summary(Long id,Long employeeId,String employeeNo,String employeeName,SalnTypes.FilingType filingType,
            Integer salnYear,LocalDate dueDate,LocalDate submissionDate,SalnTypes.Status status,SalnTypes.SourceType sourceType,
            int versionNo,BigDecimal totalAssets,BigDecimal totalLiabilities,BigDecimal netWorth,String reviewedBy,
            LocalDateTime compliantAt) {}
    public record CorrectionItem(String section,String field,@NotBlank @Size(max=2000) String message) {}
    public record CorrectionRequest(@NotEmpty @Valid List<CorrectionItem> items) {}
    public record RemarksRequest(@NotBlank @Size(max=2000) String remarks) {}
    public record VoidRequest(@NotBlank @Size(max=1000) String reason,Long replacementSalnId) {}
    public record RepositoryRequest(@NotBlank String repositoryAgency,@NotNull LocalDateTime submittedAt,
            @NotBlank String referenceNo,String remarks) {}
    public record HistoricalRequest(@NotNull Long employeeId,@NotNull SalnTypes.SourceType sourceType,
            @NotNull LocalDate originalFilingDate,String sourceDocumentReference,@NotNull @Valid DraftRequest declaration) {}
    public record RequirementRequest(@NotNull Long employeeId,@NotNull SalnTypes.FilingType filingType,@NotNull LocalDate referenceDate) {}
    public record RequirementResponse(Long id,Long employeeId,String employeeNo,SalnTypes.FilingType filingType,
            LocalDate referenceDate,int salnYear,LocalDate dueDate,SalnTypes.RequirementStatus status,Long linkedSalnId) {}
    public record AnnualBulkRequirementRequest(@NotNull @Min(1900) @Max(2200) Integer salnYear) {}
    public record AnnualBulkRequirementResult(Integer salnYear,LocalDate referenceDate,LocalDate dueDate,
            long eligibleEmployees,long requirementsCreated,long alreadyExisting,long excludedEmployees) {}
    public record Dashboard(long totalRequired,long filed,long notFiled,long draft,long submitted,long underReview,
            long forCorrection,long compliant,long overdue) {}
}
