package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

@Entity
@Table(name="succession_criticality_finding",uniqueConstraints=@UniqueConstraint(name="uk_succ_finding_criterion",columnNames={"agency_id","designation_id","criterion_id"}))
public class SuccessionCriticalityFinding extends RspAuditedEntity {
    public enum Outcome{MET,NOT_MET,NOT_APPLICABLE}
    @Column(name="designation_id",nullable=false,length=36) private String designationId; @Column(name="criterion_id",nullable=false,length=36) private String criterionId; @Column(name="criterion_code",nullable=false,length=80) private String criterionCode; @Column(name="criterion_label",nullable=false,length=200) private String criterionLabel; @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Outcome outcome; @Column(nullable=false,length=2000) private String reason; @Column(name="evidence_reference",length=1000) private String evidenceReference; @Column(name="display_order",nullable=false) private int displayOrder;
    protected SuccessionCriticalityFinding() { }
    public SuccessionCriticalityFinding(String agencyId,String designationId,SuccessionCriticalityCriterion criterion,Outcome outcome,String reason,String evidenceReference){super(agencyId);this.designationId=requiredText(designationId,"designationId");if(criterion==null||outcome==null)throw new IllegalArgumentException("criterion and outcome are required");this.criterionId=criterion.getId();this.criterionCode=criterion.getCode();this.criterionLabel=criterion.getLabel();this.displayOrder=criterion.getDisplayOrder();this.outcome=outcome;this.reason=requiredText(reason,"reason");this.evidenceReference=optionalText(evidenceReference);}
    public String getDesignationId(){return designationId;} public String getCriterionId(){return criterionId;} public String getCriterionCode(){return criterionCode;} public String getCriterionLabel(){return criterionLabel;} public Outcome getOutcome(){return outcome;} public String getReason(){return reason;} public String getEvidenceReference(){return evidenceReference;} public int getDisplayOrder(){return displayOrder;}
}
