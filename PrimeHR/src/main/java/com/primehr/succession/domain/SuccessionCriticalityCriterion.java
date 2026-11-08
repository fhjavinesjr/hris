package com.primehr.succession.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;

@Entity
@Table(name="succession_criticality_criterion", uniqueConstraints={@UniqueConstraint(name="uk_succ_criterion_code",columnNames={"agency_id","framework_version_id","normalized_code"}),@UniqueConstraint(name="uk_succ_criterion_order",columnNames={"agency_id","framework_version_id","display_order"})})
public class SuccessionCriticalityCriterion extends RspAuditedEntity {
    @Column(name="framework_version_id",nullable=false,length=36) private String frameworkVersionId;
    @Column(nullable=false,length=80) private String code;
    @Column(name="normalized_code",nullable=false,length=80) private String normalizedCode;
    @Column(nullable=false,length=200) private String label;
    @Column(nullable=false,length=2000) private String description;
    @Column(name="evidence_guidance",nullable=false,length=4000) private String evidenceGuidance;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected SuccessionCriticalityCriterion() { }
    public SuccessionCriticalityCriterion(String agencyId,String frameworkVersionId,String code,String label,String description,String evidenceGuidance,int displayOrder){super(agencyId);this.frameworkVersionId=requiredText(frameworkVersionId,"frameworkVersionId");this.code=requiredText(code,"code");this.normalizedCode=this.code.toUpperCase(java.util.Locale.ROOT);this.label=requiredText(label,"label");this.description=requiredText(description,"description");this.evidenceGuidance=requiredText(evidenceGuidance,"evidenceGuidance");if(displayOrder<1)throw new IllegalArgumentException("displayOrder must be positive");this.displayOrder=displayOrder;}
    public String getFrameworkVersionId(){return frameworkVersionId;} public String getCode(){return code;} public String getLabel(){return label;} public String getDescription(){return description;} public String getEvidenceGuidance(){return evidenceGuidance;} public int getDisplayOrder(){return displayOrder;}
}
