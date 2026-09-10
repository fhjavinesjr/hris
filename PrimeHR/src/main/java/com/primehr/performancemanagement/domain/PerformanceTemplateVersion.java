package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.time.*;
import java.util.Objects;

@Entity @Table(name="spms_template_version",uniqueConstraints=@UniqueConstraint(name="uk_spms_template_version",columnNames={"agency_id","template_id","definition_version"}))
public class PerformanceTemplateVersion extends RspAuditedEntity {
    public enum FormType { OPCR,DPCR,IPCR,CUSTOM } public enum Status { DRAFT,PUBLISHED,RETIRED }
    @Column(name="template_id",nullable=false,length=36) private String templateId;
    @Column(name="policy_version_id",nullable=false,length=36) private String policyVersionId;
    @Column(name="rating_scale_version_id",nullable=false,length=36) private String ratingScaleVersionId;
    @Column(name="definition_version",nullable=false) private int definitionVersion;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Column(nullable=false,length=200) private String title; @Column(length=2000) private String description;
    @Enumerated(EnumType.STRING) @Column(name="form_type",nullable=false,length=20) private FormType formType;
    @Column(name="form_label",nullable=false,length=200) private String formLabel;
    @Column(name="legal_basis",nullable=false,length=1000) private String legalBasis;
    @Column(name="coverage_scope",nullable=false,length=30) private String coverageScope;
    @Column(nullable=false,length=30) private String aggregation;
    @Column(name="missing_value_policy",nullable=false,length=20) private String missingValuePolicy;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="structure_revision",nullable=false) private int structureRevision;
    @Column(name="effective_from") private LocalDate effectiveFrom; @Column(name="effective_to") private LocalDate effectiveTo;
    @Column(name="published_by",length=100) private String publishedBy; @Column(name="published_at") private Instant publishedAt;
    @Column(name="retired_by",length=100) private String retiredBy; @Column(name="retired_at") private Instant retiredAt;
    @Column(name="retirement_reason",length=1000) private String retirementReason;
    protected PerformanceTemplateVersion() {}
    public PerformanceTemplateVersion(String agency,String root,String policy,String scale,int number,String prior,
            String title,String description,FormType type,String formLabel,String legalBasis){super(agency);templateId=requiredText(root,"templateId");if(number<1)throw new IllegalArgumentException("definitionVersion must be positive");definitionVersion=number;supersedesId=optionalText(prior);coverageScope="AGENCY_WIDE";aggregation="WEIGHTED_AVERAGE";missingValuePolicy="ERROR";status=Status.DRAFT;update(policy,scale,title,description,type,formLabel,legalBasis);}
    public PerformanceTemplateVersion(String agency,String root,String policy,String scale,int number,String prior,String title,String description,FormType type){this(agency,root,policy,scale,number,prior,title,description,type,title,"Not specified");}
    public void update(String policy,String scale,String title,String description,FormType type,String formLabel,String legalBasis){draft();policyVersionId=requiredText(policy,"policyVersionId");ratingScaleVersionId=requiredText(scale,"ratingScaleVersionId");this.title=requiredText(title,"title");this.description=optionalText(description);formType=Objects.requireNonNull(type,"formType");this.formLabel=requiredText(formLabel,"formLabel");this.legalBasis=requiredText(legalBasis,"legalBasis");}
    public void structureChanged(){draft();structureRevision++;}
    public void publish(LocalDate from,LocalDate to,String actor,Instant at){draft();if(from==null)throw new IllegalArgumentException("effectiveFrom is required");if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");effectiveFrom=from;effectiveTo=to;publishedBy=requiredText(actor,"actor");publishedAt=Objects.requireNonNull(at);status=Status.PUBLISHED;}
    public void supersede(LocalDate successorFrom,String actor,Instant at){if(status!=Status.PUBLISHED)throw new IllegalLifecycleTransitionException("Only a published template may be superseded");if(successorFrom==null||!successorFrom.isAfter(effectiveFrom))throw new IllegalArgumentException("A successor must become effective after its predecessor");effectiveTo=successorFrom.minusDays(1);retiredBy=requiredText(actor,"actor");retirementReason="Superseded by a published revision";retiredAt=Objects.requireNonNull(at);status=Status.RETIRED;}
    public void retire(String actor,String reason,Instant at){if(status!=Status.PUBLISHED)throw new IllegalLifecycleTransitionException("Only a published template may be retired");retiredBy=requiredText(actor,"actor");retirementReason=requiredText(reason,"reason");retiredAt=Objects.requireNonNull(at);LocalDate day=at.atZone(ZoneId.of("Asia/Manila")).toLocalDate();LocalDate end=day.isBefore(effectiveFrom)?effectiveFrom:day;if(effectiveTo==null||effectiveTo.isAfter(end))effectiveTo=end;status=Status.RETIRED;}
    private void draft(){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Published template versions are immutable; create a revision");}
    public String getTemplateId(){return templateId;} public String getPolicyVersionId(){return policyVersionId;} public String getRatingScaleVersionId(){return ratingScaleVersionId;} public int getDefinitionVersion(){return definitionVersion;} public String getSupersedesId(){return supersedesId;} public String getTitle(){return title;} public String getDescription(){return description;} public FormType getFormType(){return formType;} public String getFormLabel(){return formLabel;} public String getLegalBasis(){return legalBasis;} public String getCoverageScope(){return coverageScope;} public String getAggregation(){return aggregation;} public String getMissingValuePolicy(){return missingValuePolicy;} public Status getStatus(){return status;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;} public String getPublishedBy(){return publishedBy;} public Instant getPublishedAt(){return publishedAt;} public String getRetiredBy(){return retiredBy;} public Instant getRetiredAt(){return retiredAt;} public String getRetirementReason(){return retirementReason;}
}
