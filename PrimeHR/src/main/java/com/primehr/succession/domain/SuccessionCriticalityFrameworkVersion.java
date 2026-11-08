package com.primehr.succession.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "succession_criticality_framework_version", uniqueConstraints = @UniqueConstraint(name = "uk_succ_criticality_version", columnNames = {"agency_id", "framework_id", "definition_version"}))
public class SuccessionCriticalityFrameworkVersion extends SuccessionVersionedDefinition {
    @Column(name="framework_id", nullable=false, length=36) private String frameworkId;
    @Column(name="legal_basis", nullable=false, length=2000) private String legalBasis;
    @Column(name="decision_guidance", nullable=false, length=4000) private String decisionGuidance;
    @Column(name="criteria_revision", nullable=false) private int criteriaRevision;
    protected SuccessionCriticalityFrameworkVersion() { }
    public SuccessionCriticalityFrameworkVersion(String agencyId,String frameworkId,int definitionVersion,String supersedesId,String title,String description,String legalBasis,String decisionGuidance){super(agencyId,definitionVersion,supersedesId,title,description);this.frameworkId=requiredText(frameworkId,"frameworkId");apply(legalBasis,decisionGuidance);}
    public void update(String title,String description,String legalBasis,String decisionGuidance){changeDefinition(title,description);apply(legalBasis,decisionGuidance);}
    public void criteriaChanged(){requireDraft();criteriaRevision++;}
    private void apply(String legalBasis,String decisionGuidance){this.legalBasis=requiredText(legalBasis,"legalBasis");this.decisionGuidance=requiredText(decisionGuidance,"decisionGuidance");}
    public String getFrameworkId(){return frameworkId;} public String getLegalBasis(){return legalBasis;} public String getDecisionGuidance(){return decisionGuidance;} public int getCriteriaRevision(){return criteriaRevision;}
}
