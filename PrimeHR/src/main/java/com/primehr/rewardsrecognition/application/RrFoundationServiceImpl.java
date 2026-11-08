package com.primehr.rewardsrecognition.application;

import com.primehr.rewardsrecognition.api.RrFoundationDtos.*;
import com.primehr.rewardsrecognition.domain.*;
import com.primehr.rewardsrecognition.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.IllegalLifecycleTransitionException;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional
public class RrFoundationServiceImpl implements RrFoundationService {
    private final RrGovernancePolicyRepository policies;
    private final RrGovernancePolicyVersionRepository policyVersions;
    private final RrAwardCategoryRepository categories;
    private final RrAwardCategoryVersionRepository categoryVersions;
    private final RrAwardCriterionRepository criteria;
    private final RrCommitteeCharterRepository charters;
    private final RrCommitteeCharterVersionRepository charterVersions;
    private final RrCommitteeMemberRepository members;
    private final PrimeHrAuditService audit;

    public RrFoundationServiceImpl(RrGovernancePolicyRepository policies,
                                   RrGovernancePolicyVersionRepository policyVersions,
                                   RrAwardCategoryRepository categories,
                                   RrAwardCategoryVersionRepository categoryVersions,
                                   RrAwardCriterionRepository criteria,
                                   RrCommitteeCharterRepository charters,
                                   RrCommitteeCharterVersionRepository charterVersions,
                                   RrCommitteeMemberRepository members,
                                   PrimeHrAuditService audit) {
        this.policies = policies;
        this.policyVersions = policyVersions;
        this.categories = categories;
        this.categoryVersions = categoryVersions;
        this.criteria = criteria;
        this.charters = charters;
        this.charterVersions = charterVersions;
        this.members = members;
        this.audit = audit;
    }

    @Override @Transactional(readOnly = true)
    public List<PolicyResponse> policies(String agencyId) {
        return policyVersions.findByAgencyIdOrderByCreatedAtDesc(agencyId).stream()
                .map(value -> policyResponse(agencyId, value)).toList();
    }

    @Override @Transactional(readOnly = true)
    public PolicyResponse policy(String agencyId, String id) {
        return policyResponse(agencyId, policyVersion(agencyId, id));
    }

    @Override @Transactional(readOnly = true)
    public PolicyResponse effectivePolicy(String agencyId, LocalDate on) {
        return policyVersions.findByAgencyIdAndStatus(agencyId, RrDefinitionStatus.PUBLISHED).stream()
                .filter(value -> value.effectiveOn(on)).findFirst()
                .map(value -> policyResponse(agencyId, value))
                .orElseThrow(() -> new ResourceNotFoundException("No PRAISE policy is effective on the requested date"));
    }

    @Override
    public PolicyResponse createPolicy(String agencyId, PolicyInput input, String correlationId) {
        requireNew(input.recordVersion());
        String normalized = normalize(input.code());
        if (policies.existsByAgencyIdAndNormalizedCode(agencyId, normalized)) {
            throw new IllegalArgumentException("PRAISE policy code already exists");
        }
        RrGovernancePolicy root = policies.saveAndFlush(new RrGovernancePolicy(agencyId, input.code()));
        RrGovernancePolicyVersion version = policyVersions.saveAndFlush(newPolicyVersion(
                agencyId, root.getId(), 1, null, input));
        PolicyResponse after = policyResponse(agencyId, version);
        record(agencyId, "CREATE_RR_POLICY_DRAFT", "RR_GOVERNANCE_POLICY_VERSION", version, null, after, null, correlationId);
        return after;
    }

    @Override
    public PolicyResponse updatePolicy(String agencyId, String id, PolicyInput input, String correlationId) {
        RrGovernancePolicyVersion version = policyVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        requireStableCode(policyRoot(agencyId, version.getPolicyId()).getCode(), input.code());
        PolicyResponse before = policyResponse(agencyId, version);
        version.update(input.title(), input.description(), input.legalBasis(), input.policyReference(),
                input.committeeRequired(), input.allowsIndividualAwards(), input.allowsGroupAwards(),
                input.allowsOrganizationAwards(), input.allowsMonetaryIncentives(),
                input.allowsNonmonetaryIncentives(), input.requiresAgencyHeadApproval(),
                input.annualReportRequired());
        version = policyVersions.saveAndFlush(version);
        PolicyResponse after = policyResponse(agencyId, version);
        record(agencyId, "UPDATE_RR_POLICY_DRAFT", "RR_GOVERNANCE_POLICY_VERSION", version, before, after, null, correlationId);
        return after;
    }

    @Override
    public PolicyResponse revisePolicy(String agencyId, String id, DefinitionTransition input, String correlationId) {
        RrGovernancePolicyVersion prior = policyVersion(agencyId, id);
        requireVersion(prior.getVersion(), input.recordVersion());
        requirePublished(prior.getStatus());
        int next = policyVersions.findByAgencyIdAndPolicyIdOrderByDefinitionVersionDesc(agencyId, prior.getPolicyId())
                .stream().mapToInt(RrGovernancePolicyVersion::getDefinitionVersion).max().orElse(0) + 1;
        RrGovernancePolicyVersion version = policyVersions.saveAndFlush(new RrGovernancePolicyVersion(
                agencyId, prior.getPolicyId(), next, prior.getId(), prior.getTitle(), prior.getDescription(),
                prior.getLegalBasis(), prior.getPolicyReference(), prior.isCommitteeRequired(),
                prior.isAllowsIndividualAwards(), prior.isAllowsGroupAwards(), prior.isAllowsOrganizationAwards(),
                prior.isAllowsMonetaryIncentives(), prior.isAllowsNonmonetaryIncentives(),
                prior.isRequiresAgencyHeadApproval(), prior.isAnnualReportRequired()));
        PolicyResponse after = policyResponse(agencyId, version);
        record(agencyId, "CREATE_RR_POLICY_REVISION", "RR_GOVERNANCE_POLICY_VERSION", version, null, after, input.reason(), correlationId);
        return after;
    }

    @Override
    public PolicyResponse publishPolicy(String agencyId, String id, PublishTransition input, String correlationId) {
        RrGovernancePolicyVersion version = policyVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        RrGovernancePolicyVersion prior = version.getSupersedesId() == null ? null : policyVersion(agencyId, version.getSupersedesId());
        validatePublish(input, prior, policyVersions.findByAgencyIdAndStatus(agencyId, RrDefinitionStatus.PUBLISHED), version.getId());
        PolicyResponse before = policyResponse(agencyId, version);
        Instant now = Instant.now();
        version.publish(input.effectiveFrom(), input.effectiveTo(), audit.currentActor(), now);
        version = policyVersions.saveAndFlush(version);
        if (prior != null) {
            prior.supersede(input.effectiveFrom(), audit.currentActor(), now);
            policyVersions.saveAndFlush(prior);
        }
        PolicyResponse after = policyResponse(agencyId, version);
        record(agencyId, "PUBLISH_RR_POLICY", "RR_GOVERNANCE_POLICY_VERSION", version, before, after, input.reason(), correlationId);
        return after;
    }

    @Override
    public PolicyResponse retirePolicy(String agencyId, String id, DefinitionTransition input, String correlationId) {
        RrGovernancePolicyVersion version = policyVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        PolicyResponse before = policyResponse(agencyId, version);
        version.retire(audit.currentActor(), input.reason(), Instant.now());
        version = policyVersions.saveAndFlush(version);
        PolicyResponse after = policyResponse(agencyId, version);
        record(agencyId, "RETIRE_RR_POLICY", "RR_GOVERNANCE_POLICY_VERSION", version, before, after, input.reason(), correlationId);
        return after;
    }

    @Override @Transactional(readOnly = true)
    public List<CategoryResponse> categories(String agencyId) {
        return categoryVersions.findByAgencyIdOrderByCreatedAtDesc(agencyId).stream()
                .map(value -> categoryResponse(agencyId, value)).toList();
    }

    @Override @Transactional(readOnly = true)
    public CategoryResponse category(String agencyId, String id) {
        return categoryResponse(agencyId, categoryVersion(agencyId, id));
    }

    @Override
    public CategoryResponse createCategory(String agencyId, CategoryInput input, String correlationId) {
        requireNew(input.recordVersion());
        if (categories.existsByAgencyIdAndNormalizedCode(agencyId, normalize(input.code()))) {
            throw new IllegalArgumentException("Award category code already exists");
        }
        RrAwardCategory root = categories.saveAndFlush(new RrAwardCategory(agencyId, input.code()));
        RrAwardCategoryVersion version = categoryVersions.saveAndFlush(newCategoryVersion(
                agencyId, root.getId(), 1, null, input));
        CategoryResponse after = categoryResponse(agencyId, version);
        record(agencyId, "CREATE_RR_CATEGORY_DRAFT", "RR_AWARD_CATEGORY_VERSION", version, null, after, null, correlationId);
        return after;
    }

    @Override
    public CategoryResponse updateCategory(String agencyId, String id, CategoryInput input, String correlationId) {
        RrAwardCategoryVersion version = categoryVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        requireStableCode(categoryRoot(agencyId, version.getCategoryId()).getCode(), input.code());
        CategoryResponse before = categoryResponse(agencyId, version);
        version.update(input.title(), input.description(), input.nomineeType(), input.incentiveType(),
                input.eligibilityGuidance(), input.evidenceRequirements(), input.minimumScore());
        version = categoryVersions.saveAndFlush(version);
        CategoryResponse after = categoryResponse(agencyId, version);
        record(agencyId, "UPDATE_RR_CATEGORY_DRAFT", "RR_AWARD_CATEGORY_VERSION", version, before, after, null, correlationId);
        return after;
    }

    @Override
    public CategoryResponse replaceCriteria(String agencyId, String id, CriteriaInput input, String correlationId) {
        RrAwardCategoryVersion version = categoryVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        if (version.getStatus() != RrDefinitionStatus.DRAFT) {
            throw new IllegalLifecycleTransitionException("Published award categories are immutable; create a revision");
        }
        validateCriteria(input.criteria());
        CategoryResponse before = categoryResponse(agencyId, version);
        criteria.deleteByAgencyIdAndCategoryVersionId(agencyId, id);
        criteria.flush();
        criteria.saveAll(input.criteria().stream().map(value -> new RrAwardCriterion(
                agencyId, id, value.code(), value.title(), value.description(), value.weightPercent(),
                value.maximumScore(), value.evidenceGuidance(), value.displayOrder())).toList());
        criteria.flush();
        version.criteriaChanged();
        version = categoryVersions.saveAndFlush(version);
        CategoryResponse after = categoryResponse(agencyId, version);
        record(agencyId, "REPLACE_RR_CATEGORY_CRITERIA", "RR_AWARD_CATEGORY_VERSION", version, before, after, null, correlationId);
        return after;
    }

    @Override
    public CategoryResponse reviseCategory(String agencyId, String id, DefinitionTransition input, String correlationId) {
        RrAwardCategoryVersion prior = categoryVersion(agencyId, id);
        requireVersion(prior.getVersion(), input.recordVersion());
        requirePublished(prior.getStatus());
        int next = categoryVersions.findByAgencyIdAndCategoryIdOrderByDefinitionVersionDesc(agencyId, prior.getCategoryId())
                .stream().mapToInt(RrAwardCategoryVersion::getDefinitionVersion).max().orElse(0) + 1;
        RrAwardCategoryVersion version = categoryVersions.saveAndFlush(new RrAwardCategoryVersion(
                agencyId, prior.getCategoryId(), next, prior.getId(), prior.getTitle(), prior.getDescription(),
                prior.getNomineeType(), prior.getIncentiveType(), prior.getEligibilityGuidance(),
                prior.getEvidenceRequirements(), prior.getMinimumScore()));
        String revisionId = version.getId();
        List<RrAwardCriterion> copied = criteria.findByAgencyIdAndCategoryVersionIdOrderByDisplayOrderAsc(agencyId, id)
                .stream().map(value -> new RrAwardCriterion(agencyId, revisionId, value.getCode(),
                        value.getTitle(), value.getDescription(), value.getWeightPercent(), value.getMaximumScore(),
                        value.getEvidenceGuidance(), value.getDisplayOrder())).toList();
        criteria.saveAll(copied);
        if (!copied.isEmpty()) {
            version.criteriaChanged();
            version = categoryVersions.saveAndFlush(version);
        }
        CategoryResponse after = categoryResponse(agencyId, version);
        record(agencyId, "CREATE_RR_CATEGORY_REVISION", "RR_AWARD_CATEGORY_VERSION", version, null, after, input.reason(), correlationId);
        return after;
    }

    @Override
    public CategoryResponse publishCategory(String agencyId, String id, PublishTransition input, String correlationId) {
        RrAwardCategoryVersion version = categoryVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        if (criteria.findByAgencyIdAndCategoryVersionIdOrderByDisplayOrderAsc(agencyId, id).isEmpty()) {
            throw new IllegalLifecycleTransitionException("At least one criterion is required before publication");
        }
        RrAwardCategoryVersion prior = version.getSupersedesId() == null ? null : categoryVersion(agencyId, version.getSupersedesId());
        String categoryId = version.getCategoryId();
        List<RrAwardCategoryVersion> sameRoot = categoryVersions.findByAgencyIdAndStatus(agencyId, RrDefinitionStatus.PUBLISHED)
                .stream().filter(value -> value.getCategoryId().equals(categoryId)).toList();
        validatePublish(input, prior, sameRoot, version.getId());
        CategoryResponse before = categoryResponse(agencyId, version);
        Instant now = Instant.now();
        version.publish(input.effectiveFrom(), input.effectiveTo(), audit.currentActor(), now);
        version = categoryVersions.saveAndFlush(version);
        if (prior != null) {
            prior.supersede(input.effectiveFrom(), audit.currentActor(), now);
            categoryVersions.saveAndFlush(prior);
        }
        CategoryResponse after = categoryResponse(agencyId, version);
        record(agencyId, "PUBLISH_RR_CATEGORY", "RR_AWARD_CATEGORY_VERSION", version, before, after, input.reason(), correlationId);
        return after;
    }

    @Override
    public CategoryResponse retireCategory(String agencyId, String id, DefinitionTransition input, String correlationId) {
        RrAwardCategoryVersion version = categoryVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        CategoryResponse before = categoryResponse(agencyId, version);
        version.retire(audit.currentActor(), input.reason(), Instant.now());
        version = categoryVersions.saveAndFlush(version);
        CategoryResponse after = categoryResponse(agencyId, version);
        record(agencyId, "RETIRE_RR_CATEGORY", "RR_AWARD_CATEGORY_VERSION", version, before, after, input.reason(), correlationId);
        return after;
    }

    @Override @Transactional(readOnly = true)
    public List<CommitteeCharterResponse> charters(String agencyId) {
        return charterVersions.findByAgencyIdOrderByCreatedAtDesc(agencyId).stream()
                .map(value -> charterResponse(agencyId, value)).toList();
    }

    @Override @Transactional(readOnly = true)
    public CommitteeCharterResponse charter(String agencyId, String id) {
        return charterResponse(agencyId, charterVersion(agencyId, id));
    }

    @Override
    public CommitteeCharterResponse createCharter(String agencyId, CommitteeCharterInput input, String correlationId) {
        requireNew(input.recordVersion());
        if (charters.existsByAgencyIdAndNormalizedCode(agencyId, normalize(input.code()))) {
            throw new IllegalArgumentException("PRAISE committee charter code already exists");
        }
        RrCommitteeCharter root = charters.saveAndFlush(new RrCommitteeCharter(agencyId, input.code()));
        RrCommitteeCharterVersion version = charterVersions.saveAndFlush(newCharterVersion(
                agencyId, root.getId(), 1, null, input));
        CommitteeCharterResponse after = charterResponse(agencyId, version);
        record(agencyId, "CREATE_RR_CHARTER_DRAFT", "RR_COMMITTEE_CHARTER_VERSION", version, null, after, null, correlationId);
        return after;
    }

    @Override
    public CommitteeCharterResponse updateCharter(String agencyId, String id, CommitteeCharterInput input, String correlationId) {
        RrCommitteeCharterVersion version = charterVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        requireStableCode(charterRoot(agencyId, version.getCharterId()).getCode(), input.code());
        CommitteeCharterResponse before = charterResponse(agencyId, version);
        version.update(input.title(), input.description(), input.legalBasis(), input.quorumType(),
                input.quorumValue(), input.requiresSecretariat(), input.requiresAgencyHeadDecision(),
                input.conflictDeclarationRequired());
        version = charterVersions.saveAndFlush(version);
        CommitteeCharterResponse after = charterResponse(agencyId, version);
        record(agencyId, "UPDATE_RR_CHARTER_DRAFT", "RR_COMMITTEE_CHARTER_VERSION", version, before, after, null, correlationId);
        return after;
    }

    @Override
    public CommitteeCharterResponse replaceRoster(String agencyId, String id, CommitteeRosterInput input, String correlationId) {
        RrCommitteeCharterVersion version = charterVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        if (version.getStatus() != RrDefinitionStatus.DRAFT) {
            throw new IllegalLifecycleTransitionException("Published committee charters are immutable; create a revision");
        }
        validateRoster(version, input.members());
        CommitteeCharterResponse before = charterResponse(agencyId, version);
        members.deleteByAgencyIdAndCharterVersionId(agencyId, id);
        members.flush();
        members.saveAll(input.members().stream().map(value -> new RrCommitteeMember(
                agencyId, id, value.employeeNo(), value.displayName(), value.positionTitle(),
                value.memberRole(), value.votingMember(), value.effectiveFrom(), value.effectiveTo(),
                value.sourceVersion())).toList());
        members.flush();
        version.rosterChanged();
        version = charterVersions.saveAndFlush(version);
        CommitteeCharterResponse after = charterResponse(agencyId, version);
        record(agencyId, "REPLACE_RR_COMMITTEE_ROSTER", "RR_COMMITTEE_CHARTER_VERSION", version, before, after, null, correlationId);
        return after;
    }

    @Override
    public CommitteeCharterResponse reviseCharter(String agencyId, String id, DefinitionTransition input, String correlationId) {
        RrCommitteeCharterVersion prior = charterVersion(agencyId, id);
        requireVersion(prior.getVersion(), input.recordVersion());
        requirePublished(prior.getStatus());
        int next = charterVersions.findByAgencyIdAndCharterIdOrderByDefinitionVersionDesc(agencyId, prior.getCharterId())
                .stream().mapToInt(RrCommitteeCharterVersion::getDefinitionVersion).max().orElse(0) + 1;
        RrCommitteeCharterVersion version = charterVersions.saveAndFlush(new RrCommitteeCharterVersion(
                agencyId, prior.getCharterId(), next, prior.getId(), prior.getTitle(), prior.getDescription(),
                prior.getLegalBasis(), prior.getQuorumType(), prior.getQuorumValue(),
                prior.isRequiresSecretariat(), prior.isRequiresAgencyHeadDecision(),
                prior.isConflictDeclarationRequired()));
        String revisionId = version.getId();
        List<RrCommitteeMember> copied = members.findByAgencyIdAndCharterVersionIdOrderByMemberRoleAscDisplayNameAsc(agencyId, id)
                .stream().map(value -> new RrCommitteeMember(agencyId, revisionId, value.getEmployeeNo(),
                        value.getDisplayName(), value.getPositionTitle(), value.getMemberRole(), value.isVotingMember(),
                        value.getEffectiveFrom(), value.getEffectiveTo(), value.getSourceVersion())).toList();
        members.saveAll(copied);
        if (!copied.isEmpty()) {
            version.rosterChanged();
            version = charterVersions.saveAndFlush(version);
        }
        CommitteeCharterResponse after = charterResponse(agencyId, version);
        record(agencyId, "CREATE_RR_CHARTER_REVISION", "RR_COMMITTEE_CHARTER_VERSION", version, null, after, input.reason(), correlationId);
        return after;
    }

    @Override
    public CommitteeCharterResponse publishCharter(String agencyId, String id, PublishTransition input, String correlationId) {
        RrCommitteeCharterVersion version = charterVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        List<RrCommitteeMember> roster = members.findByAgencyIdAndCharterVersionIdOrderByMemberRoleAscDisplayNameAsc(agencyId, id);
        validateRoster(version, roster.stream().map(value -> new CommitteeMemberInput(
                value.getEmployeeNo(), value.getDisplayName(), value.getPositionTitle(), value.getMemberRole(),
                value.isVotingMember(), value.getEffectiveFrom(), value.getEffectiveTo(), value.getSourceVersion())).toList());
        RrCommitteeCharterVersion prior = version.getSupersedesId() == null ? null : charterVersion(agencyId, version.getSupersedesId());
        String charterId = version.getCharterId();
        List<RrCommitteeCharterVersion> sameRoot = charterVersions.findByAgencyIdAndStatus(agencyId, RrDefinitionStatus.PUBLISHED)
                .stream().filter(value -> value.getCharterId().equals(charterId)).toList();
        validatePublish(input, prior, sameRoot, version.getId());
        CommitteeCharterResponse before = charterResponse(agencyId, version);
        Instant now = Instant.now();
        version.publish(input.effectiveFrom(), input.effectiveTo(), audit.currentActor(), now);
        version = charterVersions.saveAndFlush(version);
        if (prior != null) {
            prior.supersede(input.effectiveFrom(), audit.currentActor(), now);
            charterVersions.saveAndFlush(prior);
        }
        CommitteeCharterResponse after = charterResponse(agencyId, version);
        record(agencyId, "PUBLISH_RR_CHARTER", "RR_COMMITTEE_CHARTER_VERSION", version, before, after, input.reason(), correlationId);
        return after;
    }

    @Override
    public CommitteeCharterResponse retireCharter(String agencyId, String id, DefinitionTransition input, String correlationId) {
        RrCommitteeCharterVersion version = charterVersion(agencyId, id);
        requireVersion(version.getVersion(), input.recordVersion());
        CommitteeCharterResponse before = charterResponse(agencyId, version);
        version.retire(audit.currentActor(), input.reason(), Instant.now());
        version = charterVersions.saveAndFlush(version);
        CommitteeCharterResponse after = charterResponse(agencyId, version);
        record(agencyId, "RETIRE_RR_CHARTER", "RR_COMMITTEE_CHARTER_VERSION", version, before, after, input.reason(), correlationId);
        return after;
    }

    private RrGovernancePolicyVersion newPolicyVersion(String agencyId, String rootId, int number,
                                                       String supersedesId, PolicyInput input) {
        return new RrGovernancePolicyVersion(agencyId, rootId, number, supersedesId, input.title(),
                input.description(), input.legalBasis(), input.policyReference(), input.committeeRequired(),
                input.allowsIndividualAwards(), input.allowsGroupAwards(), input.allowsOrganizationAwards(),
                input.allowsMonetaryIncentives(), input.allowsNonmonetaryIncentives(),
                input.requiresAgencyHeadApproval(), input.annualReportRequired());
    }

    private RrAwardCategoryVersion newCategoryVersion(String agencyId, String rootId, int number,
                                                      String supersedesId, CategoryInput input) {
        return new RrAwardCategoryVersion(agencyId, rootId, number, supersedesId, input.title(),
                input.description(), input.nomineeType(), input.incentiveType(), input.eligibilityGuidance(),
                input.evidenceRequirements(), input.minimumScore());
    }

    private RrCommitteeCharterVersion newCharterVersion(String agencyId, String rootId, int number,
                                                        String supersedesId, CommitteeCharterInput input) {
        return new RrCommitteeCharterVersion(agencyId, rootId, number, supersedesId, input.title(),
                input.description(), input.legalBasis(), input.quorumType(), input.quorumValue(),
                input.requiresSecretariat(), input.requiresAgencyHeadDecision(),
                input.conflictDeclarationRequired());
    }

    private PolicyResponse policyResponse(String agencyId, RrGovernancePolicyVersion value) {
        RrGovernancePolicy root = policyRoot(agencyId, value.getPolicyId());
        return new PolicyResponse(value.getId(), value.getPolicyId(), root.getCode(), value.getTitle(),
                value.getDescription(), value.getLegalBasis(), value.getPolicyReference(),
                value.isCommitteeRequired(), value.isAllowsIndividualAwards(), value.isAllowsGroupAwards(),
                value.isAllowsOrganizationAwards(), value.isAllowsMonetaryIncentives(),
                value.isAllowsNonmonetaryIncentives(), value.isRequiresAgencyHeadApproval(),
                value.isAnnualReportRequired(), value.getDefinitionVersion(), value.getSupersedesId(),
                value.getStatus().name(), value.getEffectiveFrom(), value.getEffectiveTo(),
                value.getPublishedBy(), value.getPublishedAt(), value.getRetiredBy(), value.getRetiredAt(),
                value.getRetirementReason(), value.getVersion());
    }

    private CategoryResponse categoryResponse(String agencyId, RrAwardCategoryVersion value) {
        RrAwardCategory root = categoryRoot(agencyId, value.getCategoryId());
        List<CriterionResponse> child = criteria.findByAgencyIdAndCategoryVersionIdOrderByDisplayOrderAsc(agencyId, value.getId())
                .stream().map(item -> new CriterionResponse(item.getId(), item.getCode(), item.getTitle(),
                        item.getDescription(), item.getWeightPercent(), item.getMaximumScore(),
                        item.getEvidenceGuidance(), item.getDisplayOrder())).toList();
        return new CategoryResponse(value.getId(), value.getCategoryId(), root.getCode(), value.getTitle(),
                value.getDescription(), value.getNomineeType().name(), value.getIncentiveType().name(),
                value.getEligibilityGuidance(), value.getEvidenceRequirements(), value.getMinimumScore(),
                value.getCriteriaRevision(), value.getDefinitionVersion(), value.getSupersedesId(),
                value.getStatus().name(), value.getEffectiveFrom(), value.getEffectiveTo(),
                value.getPublishedBy(), value.getPublishedAt(), value.getRetiredBy(), value.getRetiredAt(),
                value.getRetirementReason(), value.getVersion(), child);
    }

    private CommitteeCharterResponse charterResponse(String agencyId, RrCommitteeCharterVersion value) {
        RrCommitteeCharter root = charterRoot(agencyId, value.getCharterId());
        List<CommitteeMemberResponse> roster = members.findByAgencyIdAndCharterVersionIdOrderByMemberRoleAscDisplayNameAsc(agencyId, value.getId())
                .stream().map(item -> new CommitteeMemberResponse(item.getId(), item.getEmployeeNo(),
                        item.getDisplayName(), item.getPositionTitle(), item.getMemberRole().name(),
                        item.isVotingMember(), item.getEffectiveFrom(), item.getEffectiveTo(),
                        item.getSourceVersion())).toList();
        return new CommitteeCharterResponse(value.getId(), value.getCharterId(), root.getCode(),
                value.getTitle(), value.getDescription(), value.getLegalBasis(), value.getQuorumType().name(),
                value.getQuorumValue(), value.isRequiresSecretariat(), value.isRequiresAgencyHeadDecision(),
                value.isConflictDeclarationRequired(), value.getRosterRevision(), value.getDefinitionVersion(),
                value.getSupersedesId(), value.getStatus().name(), value.getEffectiveFrom(), value.getEffectiveTo(),
                value.getPublishedBy(), value.getPublishedAt(), value.getRetiredBy(), value.getRetiredAt(),
                value.getRetirementReason(), value.getVersion(), roster);
    }

    private void validateCriteria(List<CriterionInput> values) {
        Set<String> codes = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CriterionInput value : values) {
            if (!codes.add(normalize(value.code()))) throw new IllegalArgumentException("Criterion codes must be unique");
            if (!orders.add(value.displayOrder())) throw new IllegalArgumentException("Criterion display order must be unique");
            total = total.add(value.weightPercent());
        }
        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("Criterion weights must total exactly 100 percent");
        }
    }

    private void validateRoster(RrCommitteeCharterVersion charter, List<CommitteeMemberInput> values) {
        Set<String> employeeNos = new HashSet<>();
        long chairs = 0;
        long secretariat = 0;
        long voting = 0;
        for (CommitteeMemberInput value : values) {
            if (!employeeNos.add(normalize(value.employeeNo()))) throw new IllegalArgumentException("Committee employees must be unique");
            if (value.memberRole() == RrCommitteeMember.Role.CHAIR) chairs++;
            if (value.memberRole() == RrCommitteeMember.Role.SECRETARIAT) secretariat++;
            if (value.votingMember()) voting++;
        }
        if (chairs != 1) throw new IllegalArgumentException("Committee roster must contain exactly one chair");
        if (charter.isRequiresSecretariat() && secretariat < 1) throw new IllegalArgumentException("Committee roster requires a secretariat member");
        if (voting < 1) throw new IllegalArgumentException("Committee roster requires at least one voting member");
        if (charter.getQuorumType() == RrCommitteeCharterVersion.QuorumType.COUNT
                && charter.getQuorumValue().compareTo(BigDecimal.valueOf(voting)) > 0) {
            throw new IllegalArgumentException("Count quorum cannot exceed voting membership");
        }
    }

    private <T extends RrVersionedDefinition> void validatePublish(PublishTransition input, T prior,
                                                                   List<T> published, String currentId) {
        if (prior == null && published.stream().anyMatch(value -> !value.getId().equals(currentId))) {
            throw new IllegalLifecycleTransitionException("A published definition already exists; create a revision");
        }
        if (prior != null && !input.effectiveFrom().isAfter(prior.getEffectiveFrom())) {
            throw new IllegalArgumentException("A revision must become effective after its predecessor");
        }
    }

    private RrGovernancePolicyVersion policyVersion(String agencyId, String id) {
        return policyVersions.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("PRAISE policy version not found"));
    }

    private RrGovernancePolicy policyRoot(String agencyId, String id) {
        return policies.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("PRAISE policy not found"));
    }

    private RrAwardCategoryVersion categoryVersion(String agencyId, String id) {
        return categoryVersions.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Award category version not found"));
    }

    private RrAwardCategory categoryRoot(String agencyId, String id) {
        return categories.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Award category not found"));
    }

    private RrCommitteeCharterVersion charterVersion(String agencyId, String id) {
        return charterVersions.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee charter version not found"));
    }

    private RrCommitteeCharter charterRoot(String agencyId, String id) {
        return charters.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee charter not found"));
    }

    private static void requireNew(Long version) {
        if (version != null) throw new IllegalArgumentException("recordVersion must be omitted for a new record");
    }

    private static void requireVersion(long current, Long supplied) {
        if (supplied == null || current != supplied) throw new OptimisticConflictException("Record version is stale");
    }

    private static void requireStableCode(String current, String supplied) {
        if (!normalize(current).equals(normalize(supplied))) throw new IllegalArgumentException("Definition code cannot change");
    }

    private static void requirePublished(RrDefinitionStatus status) {
        if (status != RrDefinitionStatus.PUBLISHED) throw new IllegalLifecycleTransitionException("Only a published definition may be revised");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private void record(String agencyId, String action, String aggregateType, RrVersionedDefinition aggregate,
                        Object before, Object after, String reason, String correlationId) {
        audit.record(agencyId, action, aggregateType, aggregate.getId(), aggregate.getDefinitionVersion(),
                aggregate.getVersion(), before, after, reason, correlationId);
    }
}
