package com.primehr.performancemanagement.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Objects;

@Entity
@Table(name="spms_success_indicator_version",uniqueConstraints=@UniqueConstraint(name="uk_spms_success_indicator_version",columnNames={"agency_id","success_indicator_id","definition_version"}))
public class PerformanceSuccessIndicatorVersion extends RspAuditedEntity {
    public enum Status { DRAFT, PUBLISHED, RETIRED }
    @Column(name="success_indicator_id",nullable=false,length=36) private String successIndicatorId;
    @Column(name="policy_version_id",nullable=false,length=36) private String policyVersionId;
    @Column(name="rating_scale_version_id",nullable=false,length=36) private String ratingScaleVersionId;
    @Column(name="definition_version",nullable=false) private int definitionVersion;
    @Column(name="supersedes_id",length=36) private String supersedesId;
    @Column(nullable=false,length=200) private String title;
    @Column(length=2000) private String description;
    @Column(name="output_description",nullable=false,length=2000) private String outputDescription;
    @Column(name="key_result_area",nullable=false,length=500) private String keyResultArea;
    @Column(name="performance_indicator",nullable=false,length=1000) private String performanceIndicator;
    @Column(name="success_statement",nullable=false,length=2000) private String successStatement;
    @Enumerated(EnumType.STRING) @Column(name="measure_type",nullable=false,length=30) private PerformanceRatingScaleVersion.MeasureType measureType;
    @Column(name="unit_of_measure",nullable=false,length=100) private String unitOfMeasure;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private PerformanceRatingScaleVersion.Direction direction;
    @Column(name="target_value",precision=19,scale=6) private BigDecimal targetValue;
    @Column(name="target_from",precision=19,scale=6) private BigDecimal targetFrom;
    @Column(name="target_to",precision=19,scale=6) private BigDecimal targetTo;
    @Column(name="requires_evidence",nullable=false) private boolean requiresEvidence;
    @Column(name="evidence_requirements",length=2000) private String evidenceRequirements;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status;
    @Column(name="content_revision",nullable=false) private int contentRevision;
    @Column(name="effective_from") private LocalDate effectiveFrom;
    @Column(name="effective_to") private LocalDate effectiveTo;
    @Column(name="published_by",length=100) private String publishedBy;
    @Column(name="published_at") private Instant publishedAt;
    @Column(name="retired_by",length=100) private String retiredBy;
    @Column(name="retired_at") private Instant retiredAt;
    @Column(name="retirement_reason",length=1000) private String retirementReason;

    protected PerformanceSuccessIndicatorVersion() {}
    public PerformanceSuccessIndicatorVersion(String agency,String indicator,String policy,String scale,int number,
            String prior,String title,String description,String output,String kra,String kpi,String success,
            PerformanceRatingScaleVersion.MeasureType measure,String unit,PerformanceRatingScaleVersion.Direction direction,
            BigDecimal target,BigDecimal from,BigDecimal to,boolean requiresEvidence,String evidence) {
        super(agency); successIndicatorId=requiredText(indicator,"successIndicatorId");
        if(number<1) throw new IllegalArgumentException("definitionVersion must be positive");
        definitionVersion=number; supersedesId=optionalText(prior); status=Status.DRAFT;
        apply(policy,scale,title,description,output,kra,kpi,success,measure,unit,direction,target,from,to,requiresEvidence,evidence);
    }
    public PerformanceSuccessIndicatorVersion(String agency,String indicator,String policy,String scale,int number,
            String prior,String title,String output,String kra,String kpi,String success,
            PerformanceRatingScaleVersion.MeasureType measure,String unit,PerformanceRatingScaleVersion.Direction direction,
            BigDecimal target,BigDecimal from,BigDecimal to,String evidence) {
        this(agency,indicator,policy,scale,number,prior,title,null,output,kra,kpi,success,measure,unit,direction,
                target,from,to,true,evidence);
    }
    public void update(String policy,String scale,String title,String description,String output,String kra,String kpi,
            String success,PerformanceRatingScaleVersion.MeasureType measure,String unit,
            PerformanceRatingScaleVersion.Direction direction,BigDecimal target,BigDecimal from,BigDecimal to,
            boolean requiresEvidence,String evidence) {
        draft(); apply(policy,scale,title,description,output,kra,kpi,success,measure,unit,direction,target,from,to,requiresEvidence,evidence);
    }
    private void apply(String policy,String scale,String title,String description,String output,String kra,String kpi,
            String success,PerformanceRatingScaleVersion.MeasureType measure,String unit,
            PerformanceRatingScaleVersion.Direction direction,BigDecimal target,BigDecimal from,BigDecimal to,
            boolean requiresEvidence,String evidence) {
        policyVersionId=requiredText(policy,"policyVersionId"); ratingScaleVersionId=requiredText(scale,"ratingScaleVersionId");
        this.title=requiredText(title,"title"); this.description=optionalText(description);
        outputDescription=requiredText(output,"outputDescription"); keyResultArea=requiredText(kra,"keyResultArea");
        performanceIndicator=requiredText(kpi,"performanceIndicator"); successStatement=requiredText(success,"successStatement");
        measureType=Objects.requireNonNull(measure,"measureType"); unitOfMeasure=requiredText(unit,"unitOfMeasure");
        this.direction=Objects.requireNonNull(direction,"direction"); targetValue=target; targetFrom=from; targetTo=to;
        this.requiresEvidence=requiresEvidence; evidenceRequirements=optionalText(evidence);
        if(requiresEvidence&&evidenceRequirements==null) throw new IllegalArgumentException("Evidence guidance is required when evidence is required");
        if(direction==PerformanceRatingScaleVersion.Direction.WITHIN_RANGE&&(from==null||to==null||to.compareTo(from)<0))
            throw new IllegalArgumentException("WITHIN_RANGE requires an ordered target range");
        if(direction!=PerformanceRatingScaleVersion.Direction.WITHIN_RANGE&&direction!=PerformanceRatingScaleVersion.Direction.MANUAL_RUBRIC&&target==null)
            throw new IllegalArgumentException("A targetValue is required");
    }
    public void structureChanged(){draft();contentRevision++;}
    public void publish(LocalDate from,LocalDate to,String actor,Instant at){draft();if(from==null)throw new IllegalArgumentException("effectiveFrom is required");if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");effectiveFrom=from;effectiveTo=to;publishedBy=requiredText(actor,"actor");publishedAt=Objects.requireNonNull(at);status=Status.PUBLISHED;}
    public void supersede(LocalDate successorFrom,String actor,Instant at){if(status!=Status.PUBLISHED)throw new IllegalLifecycleTransitionException("Only a published success indicator may be superseded");if(successorFrom==null||!successorFrom.isAfter(effectiveFrom))throw new IllegalArgumentException("A successor must become effective after its predecessor");effectiveTo=successorFrom.minusDays(1);retiredBy=requiredText(actor,"actor");retirementReason="Superseded by a published revision";retiredAt=Objects.requireNonNull(at);status=Status.RETIRED;}
    public void retire(String actor,String reason,Instant at){if(status!=Status.PUBLISHED)throw new IllegalLifecycleTransitionException("Only a published success indicator may be retired");retiredBy=requiredText(actor,"actor");retirementReason=requiredText(reason,"reason");retiredAt=Objects.requireNonNull(at);LocalDate day=at.atZone(ZoneId.of("Asia/Manila")).toLocalDate();LocalDate end=day.isBefore(effectiveFrom)?effectiveFrom:day;if(effectiveTo==null||effectiveTo.isAfter(end))effectiveTo=end;status=Status.RETIRED;}
    private void draft(){if(status!=Status.DRAFT)throw new IllegalLifecycleTransitionException("Published success indicator versions are immutable; create a revision");}
    public String getSuccessIndicatorId(){return successIndicatorId;} public String getPolicyVersionId(){return policyVersionId;}
    public String getRatingScaleVersionId(){return ratingScaleVersionId;} public int getDefinitionVersion(){return definitionVersion;}
    public String getSupersedesId(){return supersedesId;} public String getTitle(){return title;} public String getDescription(){return description;}
    public String getOutputDescription(){return outputDescription;} public String getKeyResultArea(){return keyResultArea;}
    public String getPerformanceIndicator(){return performanceIndicator;} public String getSuccessStatement(){return successStatement;}
    public PerformanceRatingScaleVersion.MeasureType getMeasureType(){return measureType;} public String getUnitOfMeasure(){return unitOfMeasure;}
    public PerformanceRatingScaleVersion.Direction getDirection(){return direction;} public BigDecimal getTargetValue(){return targetValue;}
    public BigDecimal getTargetFrom(){return targetFrom;} public BigDecimal getTargetTo(){return targetTo;}
    public boolean isRequiresEvidence(){return requiresEvidence;} public String getEvidenceRequirements(){return evidenceRequirements;}
    public Status getStatus(){return status;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;}
    public String getPublishedBy(){return publishedBy;} public Instant getPublishedAt(){return publishedAt;}
    public String getRetiredBy(){return retiredBy;} public Instant getRetiredAt(){return retiredAt;} public String getRetirementReason(){return retirementReason;}
}
