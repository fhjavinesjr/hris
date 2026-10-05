package com.humanresource.saln;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saln", indexes = {
        @Index(name = "ix_saln_employee_year", columnList = "employee_id,saln_year"),
        @Index(name = "ix_saln_status_due", columnList = "status,due_date")
})
public class Saln {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saln_id")
    private Long id;
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    @Column(name = "employee_no", nullable = false, length = 100)
    private String employeeNo;
    @Enumerated(EnumType.STRING) @Column(name = "filing_type", nullable = false, length = 20)
    private SalnTypes.FilingType filingType;
    @Column(name = "saln_year", nullable = false)
    private Integer salnYear;
    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;
    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(name = "submission_date")
    private LocalDate submissionDate;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 24)
    private SalnTypes.Status status = SalnTypes.Status.DRAFT;
    @Enumerated(EnumType.STRING) @Column(name = "source_type", nullable = false, length = 24)
    private SalnTypes.SourceType sourceType = SalnTypes.SourceType.ONLINE;
    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "declarant_family_name", nullable = false, length = 150)
    private String declarantFamilyName;
    @Column(name = "declarant_first_name", nullable = false, length = 150)
    private String declarantFirstName;
    @Column(name = "declarant_middle_initial", length = 20)
    private String declarantMiddleInitial;
    @Column(name = "declarant_position", length = 250)
    private String declarantPosition;
    @Column(name = "declarant_agency_office", length = 300)
    private String declarantAgencyOffice;
    @Column(name = "declarant_office_address", length = 500)
    private String declarantOfficeAddress;
    @Column(name = "spouse_full_name", length = 350)
    private String spouseFullName;
    @Column(name = "spouse_position", length = 250)
    private String spousePosition;
    @Column(name = "spouse_agency_office", length = 300)
    private String spouseAgencyOffice;
    @Column(name = "spouse_office_address", length = 500)
    private String spouseOfficeAddress;
    @Enumerated(EnumType.STRING) @Column(name = "filing_mode", nullable = false, length = 24)
    private SalnTypes.FilingMode filingMode = SalnTypes.FilingMode.NOT_APPLICABLE;
    @Column(name = "multiple_spouses", length = 1000)
    private String multipleSpouses;
    @Column(name = "business_interests_none", nullable = false)
    private boolean businessInterestsNone;
    @Column(name = "relatives_in_government_none", nullable = false)
    private boolean relativesInGovernmentNone;
    @Column(name = "certification_accepted", nullable = false)
    private boolean certificationAccepted;
    @Column(name = "government_id_type", length = 100)
    private String governmentIdType;
    @Column(name = "government_id_no", length = 150)
    private String governmentIdNo;
    @Column(name = "government_id_date_issued")
    private LocalDate governmentIdDateIssued;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;
    @Column(name = "submitted_at") private LocalDateTime submittedAt;
    @Column(name = "submitted_by", length = 100) private String submittedBy;
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
    @Column(name = "reviewed_by", length = 100) private String reviewedBy;
    @Column(name = "compliant_at") private LocalDateTime compliantAt;
    @Column(name = "compliant_by", length = 100) private String compliantBy;
    @Column(name = "locked_at") private LocalDateTime lockedAt;
    @Column(name = "locked_by", length = 100) private String lockedBy;
    @Column(name = "voided_at") private LocalDateTime voidedAt;
    @Column(name = "voided_by", length = 100) private String voidedBy;
    @Column(name = "void_reason", length = 1000) private String voidReason;
    @Column(name = "replacement_saln_id") private Long replacementSalnId;
    @Column(name = "source_document_reference", length = 500) private String sourceDocumentReference;
    @Column(name = "remarks", length = 2000) private String remarks;
    @Column(name = "repository_agency", length = 300) private String repositoryAgency;
    @Column(name = "repository_submitted_at") private LocalDateTime repositorySubmittedAt;
    @Column(name = "repository_submitted_by", length = 100) private String repositorySubmittedBy;
    @Column(name = "repository_reference_no", length = 200) private String repositoryReferenceNo;
    @Column(name = "repository_remarks", length = 1000) private String repositoryRemarks;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saln_dependent", joinColumns = @JoinColumn(name = "saln_id"))
    @OrderColumn(name = "row_no")
    private List<Dependent> dependents = new ArrayList<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saln_real_property", joinColumns = @JoinColumn(name = "saln_id"))
    @OrderColumn(name = "row_no")
    private List<RealProperty> realProperties = new ArrayList<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saln_personal_property", joinColumns = @JoinColumn(name = "saln_id"))
    @OrderColumn(name = "row_no")
    private List<PersonalProperty> personalProperties = new ArrayList<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saln_liability", joinColumns = @JoinColumn(name = "saln_id"))
    @OrderColumn(name = "row_no")
    private List<Liability> liabilities = new ArrayList<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saln_business_interest", joinColumns = @JoinColumn(name = "saln_id"))
    @OrderColumn(name = "row_no")
    private List<BusinessInterest> businessInterests = new ArrayList<>();
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saln_government_relative", joinColumns = @JoinColumn(name = "saln_id"))
    @OrderColumn(name = "row_no")
    private List<GovernmentRelative> governmentRelatives = new ArrayList<>();

    @Embeddable public static class Dependent {
        @Column(name="dependent_name", nullable=false, length=350) public String name;
        @Column(name="relationship", nullable=false, length=50) public String relationship;
        @Column(name="age") public Integer age;
    }
    @Embeddable public static class RealProperty {
        @Enumerated(EnumType.STRING) @Column(name="owner_type", nullable=false, length=20) public SalnTypes.OwnerType ownerType;
        @Column(name="owner_name", length=350) public String ownerName;
        @Column(name="description", nullable=false, length=500) public String description;
        @Column(name="property_kind", nullable=false, length=150) public String kind;
        @Column(name="exact_location", nullable=false, length=500) public String exactLocation;
        @Column(name="assessed_value", precision=19, scale=2) public BigDecimal assessedValue;
        @Column(name="fair_market_value", precision=19, scale=2) public BigDecimal fairMarketValue;
        @Column(name="acquisition_year") public Integer acquisitionYear;
        @Column(name="acquisition_mode", length=150) public String acquisitionMode;
        @Column(name="acquisition_cost", nullable=false, precision=19, scale=2) public BigDecimal acquisitionCost;
    }
    @Embeddable public static class PersonalProperty {
        @Enumerated(EnumType.STRING) @Column(name="owner_type", nullable=false, length=20) public SalnTypes.OwnerType ownerType;
        @Column(name="owner_name", length=350) public String ownerName;
        @Column(name="description", nullable=false, length=500) public String description;
        @Column(name="acquisition_year") public Integer acquisitionYear;
        @Column(name="acquisition_cost", nullable=false, precision=19, scale=2) public BigDecimal acquisitionCost;
    }
    @Embeddable public static class Liability {
        @Enumerated(EnumType.STRING) @Column(name="owner_type", nullable=false, length=20) public SalnTypes.OwnerType ownerType;
        @Column(name="owner_name", length=350) public String ownerName;
        @Column(name="nature", nullable=false, length=500) public String nature;
        @Column(name="creditor_name", nullable=false, length=350) public String creditorName;
        @Column(name="outstanding_balance", nullable=false, precision=19, scale=2) public BigDecimal outstandingBalance;
    }
    @Embeddable public static class BusinessInterest {
        @Enumerated(EnumType.STRING) @Column(name="owner_type", nullable=false, length=20) public SalnTypes.OwnerType ownerType;
        @Column(name="owner_name", length=350) public String ownerName;
        @Column(name="entity_name", nullable=false, length=500) public String entityName;
        @Column(name="business_address", nullable=false, length=500) public String businessAddress;
        @Column(name="nature", nullable=false, length=500) public String nature;
        @Column(name="date_acquired") public LocalDate dateAcquired;
    }
    @Embeddable public static class GovernmentRelative {
        @Column(name="relative_name", nullable=false, length=350) public String name;
        @Column(name="relationship", nullable=false, length=150) public String relationship;
        @Column(name="position_title", nullable=false, length=250) public String position;
        @Column(name="agency_office_address", nullable=false, length=600) public String agencyOfficeAddress;
    }

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getEmployeeId(){return employeeId;} public void setEmployeeId(Long v){employeeId=v;}
    public String getEmployeeNo(){return employeeNo;} public void setEmployeeNo(String v){employeeNo=v;}
    public SalnTypes.FilingType getFilingType(){return filingType;} public void setFilingType(SalnTypes.FilingType v){filingType=v;}
    public Integer getSalnYear(){return salnYear;} public void setSalnYear(Integer v){salnYear=v;}
    public LocalDate getReferenceDate(){return referenceDate;} public void setReferenceDate(LocalDate v){referenceDate=v;}
    public LocalDate getAsOfDate(){return asOfDate;} public void setAsOfDate(LocalDate v){asOfDate=v;}
    public LocalDate getDueDate(){return dueDate;} public void setDueDate(LocalDate v){dueDate=v;}
    public LocalDate getSubmissionDate(){return submissionDate;} public void setSubmissionDate(LocalDate v){submissionDate=v;}
    public SalnTypes.Status getStatus(){return status;} public void setStatus(SalnTypes.Status v){status=v;}
    public SalnTypes.SourceType getSourceType(){return sourceType;} public void setSourceType(SalnTypes.SourceType v){sourceType=v;}
    public int getVersionNo(){return versionNo;} public void setVersionNo(int v){versionNo=v;}
    public String getDeclarantFamilyName(){return declarantFamilyName;} public void setDeclarantFamilyName(String v){declarantFamilyName=v;}
    public String getDeclarantFirstName(){return declarantFirstName;} public void setDeclarantFirstName(String v){declarantFirstName=v;}
    public String getDeclarantMiddleInitial(){return declarantMiddleInitial;} public void setDeclarantMiddleInitial(String v){declarantMiddleInitial=v;}
    public String getDeclarantPosition(){return declarantPosition;} public void setDeclarantPosition(String v){declarantPosition=v;}
    public String getDeclarantAgencyOffice(){return declarantAgencyOffice;} public void setDeclarantAgencyOffice(String v){declarantAgencyOffice=v;}
    public String getDeclarantOfficeAddress(){return declarantOfficeAddress;} public void setDeclarantOfficeAddress(String v){declarantOfficeAddress=v;}
    public String getSpouseFullName(){return spouseFullName;} public void setSpouseFullName(String v){spouseFullName=v;}
    public String getSpousePosition(){return spousePosition;} public void setSpousePosition(String v){spousePosition=v;}
    public String getSpouseAgencyOffice(){return spouseAgencyOffice;} public void setSpouseAgencyOffice(String v){spouseAgencyOffice=v;}
    public String getSpouseOfficeAddress(){return spouseOfficeAddress;} public void setSpouseOfficeAddress(String v){spouseOfficeAddress=v;}
    public SalnTypes.FilingMode getFilingMode(){return filingMode;} public void setFilingMode(SalnTypes.FilingMode v){filingMode=v;}
    public String getMultipleSpouses(){return multipleSpouses;} public void setMultipleSpouses(String v){multipleSpouses=v;}
    public boolean isBusinessInterestsNone(){return businessInterestsNone;} public void setBusinessInterestsNone(boolean v){businessInterestsNone=v;}
    public boolean isRelativesInGovernmentNone(){return relativesInGovernmentNone;} public void setRelativesInGovernmentNone(boolean v){relativesInGovernmentNone=v;}
    public boolean isCertificationAccepted(){return certificationAccepted;} public void setCertificationAccepted(boolean v){certificationAccepted=v;}
    public String getGovernmentIdType(){return governmentIdType;} public void setGovernmentIdType(String v){governmentIdType=v;}
    public String getGovernmentIdNo(){return governmentIdNo;} public void setGovernmentIdNo(String v){governmentIdNo=v;}
    public LocalDate getGovernmentIdDateIssued(){return governmentIdDateIssued;} public void setGovernmentIdDateIssued(LocalDate v){governmentIdDateIssued=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public String getCreatedBy(){return createdBy;} public void setCreatedBy(String v){createdBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
    public String getUpdatedBy(){return updatedBy;} public void setUpdatedBy(String v){updatedBy=v;}
    public LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(LocalDateTime v){submittedAt=v;}
    public String getSubmittedBy(){return submittedBy;} public void setSubmittedBy(String v){submittedBy=v;}
    public LocalDateTime getReviewedAt(){return reviewedAt;} public void setReviewedAt(LocalDateTime v){reviewedAt=v;}
    public String getReviewedBy(){return reviewedBy;} public void setReviewedBy(String v){reviewedBy=v;}
    public LocalDateTime getCompliantAt(){return compliantAt;} public void setCompliantAt(LocalDateTime v){compliantAt=v;}
    public String getCompliantBy(){return compliantBy;} public void setCompliantBy(String v){compliantBy=v;}
    public LocalDateTime getLockedAt(){return lockedAt;} public void setLockedAt(LocalDateTime v){lockedAt=v;}
    public String getLockedBy(){return lockedBy;} public void setLockedBy(String v){lockedBy=v;}
    public LocalDateTime getVoidedAt(){return voidedAt;} public void setVoidedAt(LocalDateTime v){voidedAt=v;}
    public String getVoidedBy(){return voidedBy;} public void setVoidedBy(String v){voidedBy=v;}
    public String getVoidReason(){return voidReason;} public void setVoidReason(String v){voidReason=v;}
    public Long getReplacementSalnId(){return replacementSalnId;} public void setReplacementSalnId(Long v){replacementSalnId=v;}
    public String getSourceDocumentReference(){return sourceDocumentReference;} public void setSourceDocumentReference(String v){sourceDocumentReference=v;}
    public String getRemarks(){return remarks;} public void setRemarks(String v){remarks=v;}
    public String getRepositoryAgency(){return repositoryAgency;} public void setRepositoryAgency(String v){repositoryAgency=v;}
    public LocalDateTime getRepositorySubmittedAt(){return repositorySubmittedAt;} public void setRepositorySubmittedAt(LocalDateTime v){repositorySubmittedAt=v;}
    public String getRepositorySubmittedBy(){return repositorySubmittedBy;} public void setRepositorySubmittedBy(String v){repositorySubmittedBy=v;}
    public String getRepositoryReferenceNo(){return repositoryReferenceNo;} public void setRepositoryReferenceNo(String v){repositoryReferenceNo=v;}
    public String getRepositoryRemarks(){return repositoryRemarks;} public void setRepositoryRemarks(String v){repositoryRemarks=v;}
    public List<Dependent> getDependents(){return dependents;} public void setDependents(List<Dependent> v){dependents=v;}
    public List<RealProperty> getRealProperties(){return realProperties;} public void setRealProperties(List<RealProperty> v){realProperties=v;}
    public List<PersonalProperty> getPersonalProperties(){return personalProperties;} public void setPersonalProperties(List<PersonalProperty> v){personalProperties=v;}
    public List<Liability> getLiabilities(){return liabilities;} public void setLiabilities(List<Liability> v){liabilities=v;}
    public List<BusinessInterest> getBusinessInterests(){return businessInterests;} public void setBusinessInterests(List<BusinessInterest> v){businessInterests=v;}
    public List<GovernmentRelative> getGovernmentRelatives(){return governmentRelatives;} public void setGovernmentRelatives(List<GovernmentRelative> v){governmentRelatives=v;}
}
