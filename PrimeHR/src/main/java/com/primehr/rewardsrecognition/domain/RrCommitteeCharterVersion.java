package com.primehr.rewardsrecognition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "rr_committee_charter_version", uniqueConstraints = @UniqueConstraint(
        name = "uk_rr_charter_version", columnNames = {"agency_id", "charter_id", "definition_version"}))
public class RrCommitteeCharterVersion extends RrVersionedDefinition {
    public enum QuorumType { COUNT, PERCENTAGE }

    @Column(name = "charter_id", nullable = false, length = 36)
    private String charterId;
    @Column(name = "legal_basis", nullable = false, length = 2000)
    private String legalBasis;
    @Enumerated(EnumType.STRING)
    @Column(name = "quorum_type", nullable = false, length = 20)
    private QuorumType quorumType;
    @Column(name = "quorum_value", nullable = false, precision = 7, scale = 2)
    private BigDecimal quorumValue;
    @Column(name = "requires_secretariat", nullable = false)
    private boolean requiresSecretariat;
    @Column(name = "requires_agency_head_decision", nullable = false)
    private boolean requiresAgencyHeadDecision;
    @Column(name = "conflict_declaration_required", nullable = false)
    private boolean conflictDeclarationRequired;
    @Column(name = "roster_revision", nullable = false)
    private int rosterRevision;

    protected RrCommitteeCharterVersion() {
    }

    public RrCommitteeCharterVersion(String agencyId, String charterId, int definitionVersion,
                                     String supersedesId, String title, String description,
                                     String legalBasis, QuorumType quorumType, BigDecimal quorumValue,
                                     boolean requiresSecretariat, boolean requiresAgencyHeadDecision,
                                     boolean conflictDeclarationRequired) {
        super(agencyId, definitionVersion, supersedesId, title, description);
        this.charterId = requiredText(charterId, "charterId");
        apply(legalBasis, quorumType, quorumValue, requiresSecretariat,
                requiresAgencyHeadDecision, conflictDeclarationRequired);
    }

    public void update(String title, String description, String legalBasis, QuorumType quorumType,
                       BigDecimal quorumValue, boolean requiresSecretariat,
                       boolean requiresAgencyHeadDecision, boolean conflictDeclarationRequired) {
        changeDefinition(title, description);
        apply(legalBasis, quorumType, quorumValue, requiresSecretariat,
                requiresAgencyHeadDecision, conflictDeclarationRequired);
    }

    public void rosterChanged() {
        requireDraft();
        rosterRevision++;
    }

    private void apply(String legalBasis, QuorumType quorumType, BigDecimal quorumValue,
                       boolean requiresSecretariat, boolean requiresAgencyHeadDecision,
                       boolean conflictDeclarationRequired) {
        if (quorumType == null) throw new IllegalArgumentException("quorumType is required");
        if (quorumValue == null || quorumValue.signum() <= 0) throw new IllegalArgumentException("quorumValue must be positive");
        if (quorumType == QuorumType.PERCENTAGE && quorumValue.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage quorum cannot exceed 100");
        }
        if (quorumType == QuorumType.COUNT && quorumValue.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Count quorum must be a whole number");
        }
        this.legalBasis = requiredText(legalBasis, "legalBasis");
        this.quorumType = quorumType;
        this.quorumValue = quorumValue;
        this.requiresSecretariat = requiresSecretariat;
        this.requiresAgencyHeadDecision = requiresAgencyHeadDecision;
        this.conflictDeclarationRequired = conflictDeclarationRequired;
    }

    public String getCharterId() { return charterId; }
    public String getLegalBasis() { return legalBasis; }
    public QuorumType getQuorumType() { return quorumType; }
    public BigDecimal getQuorumValue() { return quorumValue; }
    public boolean isRequiresSecretariat() { return requiresSecretariat; }
    public boolean isRequiresAgencyHeadDecision() { return requiresAgencyHeadDecision; }
    public boolean isConflictDeclarationRequired() { return conflictDeclarationRequired; }
    public int getRosterRevision() { return rosterRevision; }
}
