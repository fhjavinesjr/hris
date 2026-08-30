package com.primehr.rsp.applicant.api;

import com.primehr.rsp.applicant.domain.ApplicantProfileEntry;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import java.time.*;import java.util.List;

public final class ApplicantDtos { private ApplicantDtos(){}
    public record Register(@Email @NotBlank @Size(max=320) String email,@NotBlank @Size(min=12,max=100) String password,@NotBlank @Size(max=100) String givenName,@NotBlank @Size(max=100) String familyName,@NotBlank String privacyNoticeId,@AssertTrue boolean consentAccepted){}
    public record Login(@Email @NotBlank String email,@NotBlank String password){}
    public record Session(String token,String tokenType,long expiresInSeconds,Account account){}
    public record Account(String id,String email,String displayName,String status,Instant createdAt,long recordVersion){}
    public record UpdateAccount(@Email @NotBlank @Size(max=320) String email,@NotBlank @Size(max=200) String displayName,@NotNull Long recordVersion){}
    public record Notice(String id,String title,String body,String retentionSummary,int definitionVersion,LocalDate effectiveFrom,LocalDate effectiveTo){}
    public record Entry(@NotNull ApplicantProfileEntry.Type type,@NotBlank @Size(max=300) String title,@Size(max=300) String organizationName,LocalDate dateFrom,LocalDate dateTo,@Size(max=2000) String details,@Min(0) int displayOrder){}
    public record SaveProfile(@NotBlank @Size(max=100) String givenName,@Size(max=100) String middleName,@NotBlank @Size(max=100) String familyName,@Size(max=30) String suffix,LocalDate birthDate,@Size(max=50) String mobileNumber,@Size(max=500) String addressLine,@Size(max=100) String city,@Size(max=100) String province,@Size(max=20) String postalCode,@Size(max=100) String citizenship,boolean declarationAccepted,@Valid List<Entry> entries,@NotNull Long recordVersion){}
    public record Profile(String id,String applicantId,String givenName,String middleName,String familyName,String suffix,LocalDate birthDate,String mobileNumber,String addressLine,String city,String province,String postalCode,String citizenship,boolean declarationAccepted,long recordVersion,List<Entry> entries){}
    public record Document(String id,String documentType,String originalFilename,String mediaType,long byteSize,String checksum,String classification,String scanStatus,Instant uploadedAt,boolean active,long recordVersion){}
    public record PublicVacancy(String id,String jobPositionName,String plantillaName,String businessUnitName,String placeOfAssignment,Long salaryGrade,Long salaryStep,LocalDate openingDate,LocalDate closingDate,String noticeText,String instructions,String contactGuidance,int qualificationStandardVersion,int positionProfileDefinitionVersion){}
}
