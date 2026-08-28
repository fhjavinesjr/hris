package com.primehr.assessment.domain;

import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.ProficiencyLevel;
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
@Table(name = "prime_assessment_rating", uniqueConstraints = @UniqueConstraint(
        name = "uk_prime_assessment_rating", columnNames = {"assessor_assignment_id", "competency_id"}))
public class AssessmentRating {
    @Id @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "agency_id", length = 64, nullable = false, updatable = false)
    private String agencyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessor_assignment_id", nullable = false, updatable = false)
    private AssessorAssignment assignment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competency_id", nullable = false, updatable = false)
    private Competency competency;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attained_proficiency_level_id", nullable = false)
    private ProficiencyLevel attainedLevel;
    @Nationalized @Column(name = "remarks", length = 2000)
    private String remarks;
    @Nationalized @Column(name = "behavioral_notes", length = 4000)
    private String behavioralNotes;
    @Column(name = "active", nullable = false)
    private boolean active;
    @Version @Column(name = "record_version", nullable = false)
    private long version;
    @CreatedBy @Column(name = "created_by", length = 100, nullable = false, updatable = false)
    private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedBy @Column(name = "updated_by", length = 100, nullable = false)
    private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AssessmentRating() { }

    public AssessmentRating(String agencyId, AssessorAssignment assignment, Competency competency,
                            ProficiencyLevel attainedLevel, String remarks, String behavioralNotes,
                            String actor) {
        this.agencyId = requireText(agencyId, "agencyId");
        this.assignment = java.util.Objects.requireNonNull(assignment, "assignment");
        this.competency = java.util.Objects.requireNonNull(competency, "competency");
        assignment.requireActor(actor);
        requireCompatible(attainedLevel);
        this.attainedLevel = attainedLevel;
        this.remarks = normalize(remarks);
        this.behavioralNotes = normalize(behavioralNotes);
        this.active = true;
    }

    public void update(ProficiencyLevel level, String remarks, String behavioralNotes,
                       long expectedVersion, String actor) {
        if (version != expectedVersion) throw new com.primehr.shared.exception.OptimisticConflictException(
                "Expected recordVersion " + expectedVersion + " but current version is " + version);
        assignment.requireActor(actor);
        requireCompatible(level);
        this.attainedLevel = level;
        this.remarks = normalize(remarks);
        this.behavioralNotes = normalize(behavioralNotes);
    }

    public void requireMutableBy(String actor) {
        assignment.requireActor(actor);
        assignment.getAssessmentCase().getTool().requirePublishedForExecution();
        if (assignment.getStatus() != AssessorAssignmentStatus.IN_PROGRESS) {
            throw new com.primehr.shared.exception.IllegalLifecycleTransitionException(
                    "Assessment content may be changed only while the assignment is IN_PROGRESS");
        }
        if (!active) throw new IllegalStateException("Archived ratings cannot be changed");
    }

    private void requireCompatible(ProficiencyLevel level) {
        if (!agencyId.equals(assignment.getAgencyId()) || !agencyId.equals(competency.getAgencyId())
                || !agencyId.equals(level.getAgencyId())) {
            throw new IllegalArgumentException("Rating references must use the same agency");
        }
        if (!competency.getProficiencyScale().getId().equals(level.getScale().getId()) || !level.isActive()) {
            throw new IllegalArgumentException("The attained level must belong to the competency's exact scale");
        }
    }

    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID().toString(); }
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public String getId() { return id; }
    public String getAgencyId() { return agencyId; }
    public AssessorAssignment getAssignment() { return assignment; }
    public Competency getCompetency() { return competency; }
    public ProficiencyLevel getAttainedLevel() { return attainedLevel; }
    public String getRemarks() { return remarks; }
    public String getBehavioralNotes() { return behavioralNotes; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
}
