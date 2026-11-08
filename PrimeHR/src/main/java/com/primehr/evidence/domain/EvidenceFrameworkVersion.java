package com.primehr.evidence.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.time.*;
import java.util.Objects;

@Entity
@Table(name="evidence_framework_version", uniqueConstraints=@UniqueConstraint(name="uk_evidence_framework_version", columnNames={"agency_id","framework_id","definition_version"}))
public class EvidenceFrameworkVersion extends RspAuditedEntity {
    @Column(name="framework_id",nullable=false,length=36) private String frameworkId;
    @Column(name="definition_version",nullable=false) private int definitionVersion;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Column(nullable=false,length=200) private String title;
    @Column(length=2000) private String description;
    @Column(name="issuing_body",nullable=false,length=250) private String issuingBody;
    @Column(name="legal_basis",nullable=false,length=2000) private String legalBasis;
    @Column(name="authority_reference",nullable=false,length=250) private String authorityReference;
    @Column(name="internal_use_disclaimer",nullable=false,length=2000) private String internalUseDisclaimer;
    @Column(name="content_revision",nullable=false) private int contentRevision;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private EvidenceDefinitionStatus status;
    @Column(name="effective_from") private LocalDate effectiveFrom;
    @Column(name="effective_to") private LocalDate effectiveTo;
    @Column(name="published_by",length=100) private String publishedBy;
    @Column(name="published_at") private Instant publishedAt;
    @Column(name="retired_by",length=100) private String retiredBy;
    @Column(name="retired_at") private Instant retiredAt;
    @Column(name="retirement_reason",length=1000) private String retirementReason;
    protected EvidenceFrameworkVersion() { }
    public EvidenceFrameworkVersion(String agencyId,String frameworkId,int definitionVersion,String supersedesId,String title,String description,String issuingBody,String legalBasis,String authorityReference,String disclaimer){
        super(agencyId);if(definitionVersion<1)throw new IllegalArgumentException("definitionVersion must be positive");this.frameworkId=requiredText(frameworkId,"frameworkId");this.definitionVersion=definitionVersion;this.supersedesId=optionalText(supersedesId);status=EvidenceDefinitionStatus.DRAFT;apply(title,description,issuingBody,legalBasis,authorityReference,disclaimer);
    }
    public void update(String title,String description,String issuingBody,String legalBasis,String authorityReference,String disclaimer){requireDraft();apply(title,description,issuingBody,legalBasis,authorityReference,disclaimer);}
    private void apply(String title,String description,String issuingBody,String legalBasis,String authorityReference,String disclaimer){this.title=requiredText(title,"title");this.description=optionalText(description);this.issuingBody=requiredText(issuingBody,"issuingBody");this.legalBasis=requiredText(legalBasis,"legalBasis");this.authorityReference=requiredText(authorityReference,"authorityReference");this.internalUseDisclaimer=requiredText(disclaimer,"internalUseDisclaimer");}
    public void contentChanged(){requireDraft();contentRevision++;}
    public void publish(LocalDate from,LocalDate to,String actor,Instant at){requireDraft();if(from==null)throw new IllegalArgumentException("effectiveFrom is required");if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");effectiveFrom=from;effectiveTo=to;publishedBy=requiredText(actor,"actor");publishedAt=Objects.requireNonNull(at);status=EvidenceDefinitionStatus.PUBLISHED;}
    public void supersede(LocalDate successorFrom,String actor,Instant at){if(status!=EvidenceDefinitionStatus.PUBLISHED)throw new IllegalLifecycleTransitionException("Only a published framework may be superseded");if(successorFrom==null||!successorFrom.isAfter(effectiveFrom))throw new IllegalArgumentException("A successor must become effective after its predecessor");effectiveTo=successorFrom.minusDays(1);retiredBy=requiredText(actor,"actor");retiredAt=Objects.requireNonNull(at);retirementReason="Superseded by a published revision";status=EvidenceDefinitionStatus.RETIRED;}
    public void retire(String actor,String reason,Instant at){if(status!=EvidenceDefinitionStatus.PUBLISHED)throw new IllegalLifecycleTransitionException("Only a published framework may be retired");retiredBy=requiredText(actor,"actor");retirementReason=requiredText(reason,"reason");retiredAt=Objects.requireNonNull(at);LocalDate day=at.atZone(ZoneId.of("Asia/Manila")).toLocalDate();if(effectiveTo==null||effectiveTo.isAfter(day))effectiveTo=day.isBefore(effectiveFrom)?effectiveFrom:day;status=EvidenceDefinitionStatus.RETIRED;}
    private void requireDraft(){if(status!=EvidenceDefinitionStatus.DRAFT)throw new IllegalLifecycleTransitionException("Published frameworks are immutable; create a revision");}
    public boolean effectiveOn(LocalDate day){return status==EvidenceDefinitionStatus.PUBLISHED&&!day.isBefore(effectiveFrom)&&(effectiveTo==null||!day.isAfter(effectiveTo));}
    public String getFrameworkId(){return frameworkId;} public int getDefinitionVersion(){return definitionVersion;} public String getSupersedesId(){return supersedesId;} public String getTitle(){return title;} public String getDescription(){return description;} public String getIssuingBody(){return issuingBody;} public String getLegalBasis(){return legalBasis;} public String getAuthorityReference(){return authorityReference;} public String getInternalUseDisclaimer(){return internalUseDisclaimer;} public int getContentRevision(){return contentRevision;} public EvidenceDefinitionStatus getStatus(){return status;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public String getPublishedBy(){return publishedBy;} public Instant getPublishedAt(){return publishedAt;}
}
