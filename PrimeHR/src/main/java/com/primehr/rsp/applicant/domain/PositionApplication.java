package com.primehr.rsp.applicant.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "rsp_position_application", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rsp_application_version",
                columnNames = {"agency_id", "applicant_id", "vacancy_publication_id", "application_version"})
})
public class PositionApplication extends RspAuditedEntity {
    public enum Status { DRAFT, SUBMITTED, WITHDRAWN }

    @Column(name = "applicant_id", nullable = false, length = 36) private String applicantId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_publication_id", nullable = false) private VacancyPublication publication;
    @Column(name = "application_version", nullable = false) private int applicationVersion;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status;
    @Column(name = "safe_status", nullable = false, length = 30) private String safeStatus;
    @Column(name = "acknowledgment_number", length = 50) private String acknowledgmentNumber;
    @Column(name = "privacy_notice_id", length = 36) private String privacyNoticeId;
    @Column(name = "privacy_notice_version") private Integer privacyNoticeVersion;
    @Nationalized @Column(name = "vacancy_snapshot", length = 32000) private String vacancySnapshot;
    @Nationalized @Column(name = "qualification_snapshot", length = 32000) private String qualificationSnapshot;
    @Nationalized @Column(name = "competency_snapshot", length = 32000) private String competencySnapshot;
    @Nationalized @Column(name = "profile_snapshot", length = 32000) private String profileSnapshot;
    @Column(name = "draft_updated_at", nullable = false) private Instant draftUpdatedAt;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "withdrawn_at") private Instant withdrawnAt;
    @Column(name = "withdrawal_reason", length = 1000) private String withdrawalReason;

    protected PositionApplication() {}

    public PositionApplication(String agencyId, String applicantId, VacancyPublication publication,
                               int applicationVersion) {
        super(agencyId);
        this.applicantId = requiredText(applicantId, "applicantId");
        this.publication = Objects.requireNonNull(publication, "publication");
        if (applicationVersion < 1) throw new IllegalArgumentException("applicationVersion must be positive");
        this.applicationVersion = applicationVersion;
        this.status = Status.DRAFT;
        this.safeStatus = Status.DRAFT.name();
        this.draftUpdatedAt = Instant.now();
    }

    public void touchDraft(Instant at) { require(Status.DRAFT); draftUpdatedAt = Objects.requireNonNull(at, "at"); }

    public void submit(String acknowledgmentNumber, String privacyNoticeId, int privacyNoticeVersion,
                       String vacancySnapshot, String qualificationSnapshot, String competencySnapshot,
                       String profileSnapshot, Instant submittedAt) {
        require(Status.DRAFT);
        this.acknowledgmentNumber = requiredText(acknowledgmentNumber, "acknowledgmentNumber");
        this.privacyNoticeId = requiredText(privacyNoticeId, "privacyNoticeId");
        this.privacyNoticeVersion = privacyNoticeVersion;
        this.vacancySnapshot = requiredText(vacancySnapshot, "vacancySnapshot");
        this.qualificationSnapshot = requiredText(qualificationSnapshot, "qualificationSnapshot");
        this.competencySnapshot = requiredText(competencySnapshot, "competencySnapshot");
        this.profileSnapshot = requiredText(profileSnapshot, "profileSnapshot");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
        status = Status.SUBMITTED;
        safeStatus = Status.SUBMITTED.name();
    }

    public void withdraw(String reason, Instant at) {
        require(Status.SUBMITTED);
        withdrawalReason = requiredText(reason, "withdrawalReason");
        withdrawnAt = Objects.requireNonNull(at, "withdrawnAt");
        status = Status.WITHDRAWN;
        safeStatus = Status.WITHDRAWN.name();
    }

    private void require(Status expected) {
        if (status != expected) throw new IllegalLifecycleTransitionException(
                "Only " + expected + " applications may perform this action");
    }

    public String getApplicantId() { return applicantId; }
    public VacancyPublication getPublication() { return publication; }
    public int getApplicationVersion() { return applicationVersion; }
    public Status getStatus() { return status; }
    public String getSafeStatus() { return safeStatus; }
    public String getAcknowledgmentNumber() { return acknowledgmentNumber; }
    public String getPrivacyNoticeId() { return privacyNoticeId; }
    public Integer getPrivacyNoticeVersion() { return privacyNoticeVersion; }
    public String getVacancySnapshot() { return vacancySnapshot; }
    public String getQualificationSnapshot() { return qualificationSnapshot; }
    public String getCompetencySnapshot() { return competencySnapshot; }
    public String getProfileSnapshot() { return profileSnapshot; }
    public Instant getDraftUpdatedAt() { return draftUpdatedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getWithdrawnAt() { return withdrawnAt; }
    public String getWithdrawalReason() { return withdrawalReason; }
}
