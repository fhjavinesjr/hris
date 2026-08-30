package com.primehr.rsp.domain;

import com.primehr.positionprofile.api.PositionRequirementResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rsp_vacancy_publication_requirement")
public class VacancyPublicationRequirementSnapshot extends RspAuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_id", nullable = false)
    private VacancyPublication publication;

    @Column(name = "competency_version_id", nullable = false, length = 36) private String competencyVersionId;
    @Column(name = "competency_code", nullable = false, length = 100) private String competencyCode;
    @Column(name = "competency_name", nullable = false, length = 200) private String competencyName;
    @Column(name = "competency_definition_version", nullable = false) private int competencyDefinitionVersion;
    @Column(name = "required_level_id", nullable = false, length = 36) private String requiredLevelId;
    @Column(name = "required_level_code", nullable = false, length = 100) private String requiredLevelCode;
    @Column(name = "required_level_label", nullable = false, length = 200) private String requiredLevelLabel;
    @Column(name = "classification", nullable = false, length = 30) private String classification;
    @Column(name = "criticality_code", length = 100) private String criticalityCode;
    @Column(name = "remarks", length = 1000) private String remarks;
    @Column(name = "display_order", nullable = false) private int displayOrder;

    protected VacancyPublicationRequirementSnapshot() {
    }

    public VacancyPublicationRequirementSnapshot(String agencyId, VacancyPublication publication,
                                                  PositionRequirementResponse source) {
        super(agencyId);
        this.publication = publication;
        competencyVersionId = text(source.competencyVersionId(), "competencyVersionId");
        competencyCode = text(source.competencyCode(), "competencyCode");
        competencyName = text(source.competencyName(), "competencyName");
        competencyDefinitionVersion = source.competencyDefinitionVersion();
        requiredLevelId = text(source.requiredProficiencyLevelId(), "requiredLevelId");
        requiredLevelCode = text(source.requiredProficiencyLevelCode(), "requiredLevelCode");
        requiredLevelLabel = text(source.requiredProficiencyLevelLabel(), "requiredLevelLabel");
        classification = source.classification().name();
        criticalityCode = optional(source.criticalityCode());
        remarks = optional(source.remarks());
        displayOrder = source.displayOrder();
    }

    public VacancyPublication getPublication() { return publication; }
    public String getCompetencyVersionId() { return competencyVersionId; }
    public String getCompetencyCode() { return competencyCode; }
    public String getCompetencyName() { return competencyName; }
    public int getCompetencyDefinitionVersion() { return competencyDefinitionVersion; }
    public String getRequiredLevelId() { return requiredLevelId; }
    public String getRequiredLevelCode() { return requiredLevelCode; }
    public String getRequiredLevelLabel() { return requiredLevelLabel; }
    public String getClassification() { return classification; }
    public String getCriticalityCode() { return criticalityCode; }
    public String getRemarks() { return remarks; }
    public int getDisplayOrder() { return displayOrder; }
}
