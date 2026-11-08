package com.primehr.rewardsrecognition.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(name = "rr_committee_member", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_member_employee", columnNames = {"agency_id", "charter_version_id", "employee_no"}))
public class RrCommitteeMember extends RspAuditedEntity {
    public enum Role { CHAIR, VICE_CHAIR, MEMBER, SECRETARIAT }

    @Column(name = "charter_version_id", nullable = false, length = 36)
    private String charterVersionId;
    @Column(name = "employee_no", nullable = false, length = 100)
    private String employeeNo;
    @Column(name = "display_name", nullable = false, length = 250)
    private String displayName;
    @Column(name = "position_title", nullable = false, length = 250)
    private String positionTitle;
    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 30)
    private Role memberRole;
    @Column(name = "voting_member", nullable = false)
    private boolean votingMember;
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    @Column(name = "source_version", length = 120)
    private String sourceVersion;

    protected RrCommitteeMember() {
    }

    public RrCommitteeMember(String agencyId, String charterVersionId, String employeeNo,
                             String displayName, String positionTitle, Role memberRole,
                             boolean votingMember, LocalDate effectiveFrom, LocalDate effectiveTo,
                             String sourceVersion) {
        super(agencyId);
        this.charterVersionId = requiredText(charterVersionId, "charterVersionId");
        this.employeeNo = requiredText(employeeNo, "employeeNo");
        this.displayName = requiredText(displayName, "displayName");
        this.positionTitle = requiredText(positionTitle, "positionTitle");
        if (memberRole == null) throw new IllegalArgumentException("memberRole is required");
        if (effectiveFrom == null) throw new IllegalArgumentException("effectiveFrom is required");
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");
        }
        if (memberRole == Role.SECRETARIAT && votingMember) {
            throw new IllegalArgumentException("Secretariat members cannot vote");
        }
        this.memberRole = memberRole;
        this.votingMember = votingMember;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.sourceVersion = optionalText(sourceVersion);
    }

    public String getCharterVersionId() { return charterVersionId; }
    public String getEmployeeNo() { return employeeNo; }
    public String getDisplayName() { return displayName; }
    public String getPositionTitle() { return positionTitle; }
    public Role getMemberRole() { return memberRole; }
    public boolean isVotingMember() { return votingMember; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public String getSourceVersion() { return sourceVersion; }
}
