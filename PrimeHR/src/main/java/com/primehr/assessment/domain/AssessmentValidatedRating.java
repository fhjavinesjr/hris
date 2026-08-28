package com.primehr.assessment.domain;

import com.primehr.competency.domain.*;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prime_assessment_validated_rating", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_validated_rating", columnNames = {"validation_id", "competency_id"}))
public class AssessmentValidatedRating {
    @Id @Column(length = 36, nullable = false, updatable = false) private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false) private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "validation_id", nullable = false, updatable = false) private AssessmentValidation validation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "competency_id", nullable = false, updatable = false) private Competency competency;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "final_proficiency_level_id", nullable = false, updatable = false) private ProficiencyLevel finalLevel;
    @Nationalized @Column(name = "validation_remarks", length = 2000, updatable = false) private String validationRemarks;
    @Column(name = "contributing_assignment_ids", length = 4000, nullable = false, updatable = false) private String contributingAssignmentIds;
    @Column(nullable = false, updatable = false) private boolean active;
    @Version @Column(name = "record_version", nullable = false) private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false) private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    protected AssessmentValidatedRating() { }
    public AssessmentValidatedRating(String agencyId, AssessmentValidation validation, Competency competency,
                                     ProficiencyLevel finalLevel, String remarks, String contributingIds) {
        if (!agencyId.equals(validation.getAgencyId()) || !agencyId.equals(competency.getAgencyId()) || !agencyId.equals(finalLevel.getAgencyId())) throw new IllegalArgumentException("Validated rating agency does not match");
        if (!competency.getProficiencyScale().getId().equals(finalLevel.getScale().getId()) || !finalLevel.isActive()) throw new IllegalArgumentException("Final level must belong to the competency's exact scale");
        if (contributingIds == null || contributingIds.isBlank()) throw new IllegalArgumentException("Contributing assignment IDs are required");
        this.agencyId=agencyId; this.validation=validation; this.competency=competency; this.finalLevel=finalLevel;
        this.validationRemarks=remarks==null||remarks.isBlank()?null:remarks.trim(); this.contributingAssignmentIds=contributingIds; this.active=true;
    }
    @PrePersist void assignId(){if(id==null)id=UUID.randomUUID().toString();}
    public String getId(){return id;} public AssessmentValidation getValidation(){return validation;}
    public Competency getCompetency(){return competency;} public ProficiencyLevel getFinalLevel(){return finalLevel;}
    public String getValidationRemarks(){return validationRemarks;} public String getContributingAssignmentIds(){return contributingAssignmentIds;}
}
