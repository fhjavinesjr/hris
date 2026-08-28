package com.primehr.assessment.domain;

import com.primehr.positionprofile.domain.PositionProfile;
import com.primehr.positionprofile.domain.PositionProfileStatus;
import com.primehr.shared.audit.AgencyAuditableEntity;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.Instant;

@Entity
@Table(name = "prime_assessment_tool", uniqueConstraints =
        @UniqueConstraint(name = "uk_prime_assessment_tool_name", columnNames = {"cycle_id", "name"}))
public class AssessmentTool extends AgencyAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false, updatable = false)
    private AssessmentCycle cycle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_profile_id", nullable = false, updatable = false)
    private PositionProfile positionProfile;
    @Nationalized
    @Column(name = "name", length = 200, nullable = false)
    private String name;
    @Nationalized
    @Column(name = "instructions", length = 4000)
    private String instructions;
    @Column(name = "status", length = 30, nullable = false)
    private String status;
    @Column(name = "profile_definition_version", nullable = false, updatable = false)
    private int profileDefinitionVersion;
    @Column(name = "profile_content_revision", nullable = false, updatable = false)
    private long profileContentRevision;
    @Column(name = "profile_target_key", length = 100, nullable = false, updatable = false)
    private String profileTargetKey;
    @Nationalized
    @Column(name = "profile_name", length = 200, nullable = false, updatable = false)
    private String profileName;
    @Column(name = "profile_source_fingerprint", length = 64, nullable = false, updatable = false)
    private String profileSourceFingerprint;
    @Column(name = "published_by", length = 100)
    private String publishedBy;
    @Column(name = "published_at")
    private Instant publishedAt;

    protected AssessmentTool() { }

    public AssessmentTool(String agencyId, AssessmentCycle cycle, PositionProfile profile,
                          String name, String instructions) {
        super(agencyId, false, 0, cycle.getEffectiveFrom(), cycle.getEffectiveTo());
        if (!agencyId.equals(cycle.getAgencyId()) || !agencyId.equals(profile.getAgencyId())) {
            throw new IllegalArgumentException("Cycle and profile must use the same agency");
        }
        cycle.requireDraft();
        if (profile.getStatus() != PositionProfileStatus.ACTIVE || !profile.isActive()) {
            throw new IllegalArgumentException("Assessment tools require an ACTIVE position profile");
        }
        this.cycle = cycle;
        this.positionProfile = profile;
        this.name = requireText(name, "name");
        this.instructions = normalize(instructions);
        this.status = AssessmentToolStatus.DRAFT.name();
        this.profileDefinitionVersion = profile.getDefinitionVersion();
        this.profileContentRevision = profile.getContentRevision();
        this.profileTargetKey = profile.getTargetKey();
        this.profileName = profile.getName();
        this.profileSourceFingerprint = profile.getSourceFingerprint();
    }

    public void updateDraft(String name, String instructions) {
        requireDraft();
        this.name = requireText(name, "name");
        this.instructions = normalize(instructions);
    }

    public void archiveDraft() {
        requireDraft();
        status = AssessmentToolStatus.ARCHIVED.name();
        setDefinitionActive(false);
    }

    public void publish(String actor, Instant at) {
        requireDraft();
        status = AssessmentToolStatus.PUBLISHED.name();
        setDefinitionActive(true);
        publishedBy = requireText(actor, "publisher");
        publishedAt = java.util.Objects.requireNonNull(at, "publishedAt");
    }

    public void requirePublishedForExecution() {
        if (getStatus() != AssessmentToolStatus.PUBLISHED
                || cycle.getStatus() != AssessmentCycleStatus.OPEN) {
            throw new IllegalLifecycleTransitionException(
                    "Assessment work requires a PUBLISHED tool in an OPEN cycle");
        }
    }

    public void requireDraft() {
        cycle.requireDraft();
        if (getStatus() != AssessmentToolStatus.DRAFT) throw new IllegalLifecycleTransitionException(
                "Only DRAFT assessment tools may be changed in Phase 3.1");
    }

    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    public AssessmentCycle getCycle() { return cycle; }
    public PositionProfile getPositionProfile() { return positionProfile; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public AssessmentToolStatus getStatus() { return AssessmentToolStatus.valueOf(status); }
    public int getProfileDefinitionVersion() { return profileDefinitionVersion; }
    public long getProfileContentRevision() { return profileContentRevision; }
    public String getProfileTargetKey() { return profileTargetKey; }
    public String getProfileName() { return profileName; }
    public String getProfileSourceFingerprint() { return profileSourceFingerprint; }
    public String getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
}
