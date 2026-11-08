package com.primehr.rewardsrecognition.api;
import com.primehr.rewardsrecognition.domain.*;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.*;import java.util.List;
public final class RrAwardDtos{private RrAwardDtos(){}
 public record ConferInput(@NotBlank String certificateTitle,@NotBlank String publicationCitation,@NotNull LocalDate conferredOn,@NotBlank String reason){}
 public record CorrectInput(@NotNull Long recordVersion,@NotBlank String certificateTitle,@NotBlank String publicationCitation,@NotNull LocalDate conferredOn,@NotBlank String reason){}
 public record RevokeInput(@NotNull Long recordVersion,@NotBlank String reason){}
 public record IncentiveInput(@NotNull RrIncentiveAuthorization.Type type,@NotBlank String description,@Positive BigDecimal authorizedAmount,String currencyCode,@NotBlank String authorizationReference){}
 public record FulfillmentInput(@NotNull RrFulfillmentEvent.Status status,@Positive BigDecimal fulfilledAmount,@NotBlank String details,String evidenceReference,String evidenceChecksum){}
 public record HapInput(@NotNull RrHapEndorsementEvent.Status status,String externalReference,@NotBlank String details){}
 public record FulfillmentResponse(String id,String status,BigDecimal fulfilledAmount,String details,String evidenceReference,String evidenceChecksum,String recordedBy,Instant recordedAt){}
 public record IncentiveResponse(String id,String type,String description,BigDecimal authorizedAmount,String currencyCode,String authorizationReference,String authorizedBy,Instant authorizedAt,List<FulfillmentResponse>fulfillmentEvents){}
 public record HapResponse(String id,String status,String externalReference,String details,String recordedBy,Instant recordedAt){}
 public record AwardResponse(String id,long recordVersion,String evaluationCaseId,String nominationId,String cycleId,String categoryVersionId,String awardCode,int revisionNo,String rootAwardId,String supersedesAwardId,String recordType,String nomineeType,String nomineeDisplayName,String categoryTitle,String certificateTitle,String publicationCitation,LocalDate conferredOn,String changeReason,String authorizedBy,Instant authorizedAt,String sourceFingerprint,boolean current,boolean revoked,List<IncentiveResponse>incentives,List<HapResponse>hapEvents){}
 public record AnnualRegisterRow(String rootAwardId,String awardCode,int revisionNo,String nomineeType,String nomineeDisplayName,String categoryTitle,LocalDate conferredOn,String status,BigDecimal monetaryAuthorized,BigDecimal monetaryFulfilled,long nonMonetaryAuthorizations,long fulfilledAuthorizations){}
 public record AnnualRegisterResponse(int year,long totalAwards,long revokedAwards,BigDecimal monetaryAuthorized,BigDecimal monetaryFulfilled,List<AnnualRegisterRow>records){}
}
