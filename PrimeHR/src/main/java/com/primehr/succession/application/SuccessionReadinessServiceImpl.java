package com.primehr.succession.application;

import com.primehr.gap.application.TargetCompetencyGapService;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.integration.humanresource.HumanResourceAssessmentSubjectClient;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import com.primehr.succession.api.SuccessionReadinessDtos.*;
import com.primehr.succession.domain.*;
import com.primehr.succession.infrastructure.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class SuccessionReadinessServiceImpl implements SuccessionReadinessService {
    private static final ZoneId MANILA = ZoneId.of("Asia/Manila");
    private final SuccessionReadinessAssessmentRepository assessments;
    private final SuccessionReadinessGapItemRepository gaps;
    private final SuccessionReadinessPanelAssignmentRepository assignments;
    private final SuccessionReadinessCriterionFindingRepository findings;
    private final SuccessionPoolDecisionRepository decisions;
    private final SuccessionCandidateRepository candidates;
    private final SuccessionCycleRepository cycles;
    private final SuccessionGovernancePolicyVersionRepository policies;
    private final SuccessionCommitteeCharterVersionRepository charters;
    private final SuccessionCommitteeMemberRepository members;
    private final SuccessionCommitteeConflictRepository standingConflicts;
    private final SuccessionReadinessScaleVersionRepository scales;
    private final SuccessionReadinessLevelRepository levels;
    private final TargetCompetencyGapService targetGaps;
    private final HumanResourceAssessmentSubjectClient hr;
    private final PrimeHrAuditService audit;

    public SuccessionReadinessServiceImpl(SuccessionReadinessAssessmentRepository assessments,
            SuccessionReadinessGapItemRepository gaps, SuccessionReadinessPanelAssignmentRepository assignments,
            SuccessionReadinessCriterionFindingRepository findings, SuccessionPoolDecisionRepository decisions,
            SuccessionCandidateRepository candidates, SuccessionCycleRepository cycles,
            SuccessionGovernancePolicyVersionRepository policies,
            SuccessionCommitteeCharterVersionRepository charters, SuccessionCommitteeMemberRepository members,
            SuccessionCommitteeConflictRepository standingConflicts,
            SuccessionReadinessScaleVersionRepository scales, SuccessionReadinessLevelRepository levels,
            TargetCompetencyGapService targetGaps, HumanResourceAssessmentSubjectClient hr,
            PrimeHrAuditService audit) {
        this.assessments=assessments;this.gaps=gaps;this.assignments=assignments;this.findings=findings;
        this.decisions=decisions;this.candidates=candidates;this.cycles=cycles;this.policies=policies;
        this.charters=charters;this.members=members;this.standingConflicts=standingConflicts;
        this.scales=scales;this.levels=levels;this.targetGaps=targetGaps;this.hr=hr;this.audit=audit;
    }

    @Override @Transactional(readOnly=true)
    public List<AssessmentResponse> list(String agency,String actor,PermissionDataScope scope){
        return assessments.findByAgencyIdOrderByCreatedAtDesc(agency).stream()
                .filter(v->visible(agency,v,actor,scope)).map(v->response(agency,v,actor,scope)).toList();
    }

    @Override @Transactional(readOnly=true)
    public AssessmentResponse get(String agency,String id,String actor,PermissionDataScope scope){
        var value=assessment(agency,id);access(agency,value,actor,scope);return response(agency,value,actor,scope);
    }

    @Override
    public AssessmentResponse initialize(String agency,String candidateId,InitializeInput input,String token,
                                         String actor,PermissionDataScope scope,String correlation){
        var candidate=candidate(agency,candidateId);panelAuthority(agency,candidate,actor,scope);
        check(candidate.getVersion(),input.candidateRecordVersion());
        if(candidate.getStatus()!=SuccessionCandidate.Status.ELIGIBLE)throw new IllegalStateException(
                "Only an eligible candidate may enter readiness review");
        if(assessments.findByAgencyIdAndCandidateId(agency,candidateId).isPresent())throw new OptimisticConflictException(
                "A readiness assessment already exists for this candidate");
        var current=hr.get(candidate.getEmployeeId(),token);
        if(!candidate.getAppointmentId().equals(current.appointmentId())||
                !candidate.getEmployeeFingerprint().equals(current.sourceFingerprint()))throw new OptimisticConflictException(
                "Candidate appointment changed; refresh candidate intake before readiness review");
        LocalDate today=LocalDate.now(MANILA);var gap=targetGaps.calculate(agency,
                PositionTargetType.valueOf(candidate.getTargetType()),candidate.getTargetJobPositionId(),
                candidate.getTargetPlantillaId(),candidate.getEmployeeNo(),today);
        var scale=effectiveScale(agency,today);var cycle=cycle(agency,candidate.getCycleId());
        var value=assessments.saveAndFlush(new SuccessionReadinessAssessment(agency,candidate,
                cycle.getCharterVersionId(),scale,gap));
        gaps.saveAll(gap.items().stream().map(i->new SuccessionReadinessGapItem(agency,value.getId(),i)).toList());
        var roster=members.findByAgencyIdAndCharterVersionIdOrderByMemberRole(agency,cycle.getCharterVersionId())
                .stream().filter(m->effective(m,today)).toList();
        if(roster.stream().noneMatch(SuccessionCommitteeMember::isVotingMember))throw new IllegalStateException(
                "The frozen readiness panel has no voting member");
        var conflicts=standingConflicts.findByAgencyIdAndCharterVersionIdOrderByDeclaredAtDesc(
                agency,cycle.getCharterVersionId());
        var frozen=new ArrayList<SuccessionReadinessPanelAssignment>();
        for(var member:roster){var row=new SuccessionReadinessPanelAssignment(agency,value.getId(),member);
            if(member.getEmployeeNo().equalsIgnoreCase(candidate.getEmployeeNo()))row.recuse("SELF",
                    "Panel member is the candidate");
            else conflicts.stream().filter(x->x.getStatus()==SuccessionCommitteeConflict.Status.ACTIVE)
                    .filter(x->related(x,member.getEmployeeNo(),candidate.getEmployeeNo())).findFirst()
                    .ifPresent(x->row.recuse("STANDING_CONFLICT",x.getConflictType()+": "+x.getDetails()));
            frozen.add(row);}
        assignments.saveAll(frozen);assignments.flush();
        var out=response(agency,value,actor,scope);record(agency,"INITIALIZE_SUCCESSION_READINESS",
                "SUCCESSION_READINESS_ASSESSMENT",value,null,out,null,correlation);return out;
    }

    @Override
    public AssessmentResponse declareConflict(String agency,String id,String assignmentId,ConflictInput input,
                                              String actor,PermissionDataScope scope,String correlation){
        var value=assessment(agency,id);var row=assignment(agency,id,assignmentId);access(agency,value,actor,scope);
        denyCandidateAction(agency,value,actor);
        if(scope!=PermissionDataScope.AGENCY_WIDE&&!row.getEmployeeNo().equalsIgnoreCase(actor))throw new AccessDeniedException(
                "Only the assigned panel member or Agency Wide authority may declare this conflict");
        check(row.getVersion(),input.assignmentRecordVersion());row.recuse("DECLARED",input.reason());
        assignments.saveAndFlush(row);var out=response(agency,value,actor,scope);record(agency,
                "DECLARE_SUCCESSION_READINESS_CONFLICT","SUCCESSION_READINESS_PANEL_ASSIGNMENT",row,null,out,
                input.reason(),correlation);return out;
    }

    @Override
    public AssessmentResponse replace(String agency,String id,String assignmentId,ReplacementInput input,
                                      String actor,PermissionDataScope scope,String correlation){
        if(scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException(
                "Panel replacement requires Agency Wide authority");
        var value=assessment(agency,id);denyCandidateAction(agency,value,actor);var recused=assignment(agency,id,assignmentId);
        var replacement=assignment(agency,id,input.replacementAssignmentId());check(recused.getVersion(),input.recusedRecordVersion());
        if(replacement.getStatus()!=SuccessionReadinessPanelAssignment.Status.ASSIGNED||
                replacement.isConflictDeclared())throw new IllegalStateException(
                "Replacement must be a non-conflicted active frozen panel member");
        recused.replaceWith(replacement.getId());assignments.saveAndFlush(recused);
        var out=response(agency,value,actor,scope);record(agency,"REPLACE_SUCCESSION_READINESS_PANEL_MEMBER",
                "SUCCESSION_READINESS_PANEL_ASSIGNMENT",recused,null,out,"Authorized frozen-roster replacement",
                correlation);return out;
    }

    @Override
    public AssessmentResponse findings(String agency,String id,FindingsInput input,String actor,
                                       PermissionDataScope scope,String correlation){
        var value=assessment(agency,id);panelAction(agency,value,actor,scope);check(value.getVersion(),input.assessmentRecordVersion());
        if(value.getStatus()!=SuccessionReadinessAssessment.Status.PANEL_REVIEW)throw new IllegalStateException(
                "Criterion findings are locked after recommendation");
        var codes=new HashSet<String>();var orders=new HashSet<Integer>();
        input.findings().forEach(f->{if(!codes.add(f.criterionCode().trim().toUpperCase(Locale.ROOT))||
                !orders.add(f.displayOrder()))throw new IllegalArgumentException("Criterion codes and display order must be unique");});
        findings.deleteByAgencyIdAndAssessmentId(agency,id);findings.flush();
        findings.saveAll(input.findings().stream().map(f->new SuccessionReadinessCriterionFinding(agency,id,
                f.criterionCode(),f.criterionLabel(),f.outcome(),f.rationale(),f.sourceReference(),
                f.employeeVisible(),f.displayOrder())).toList());
        var out=response(agency,value,actor,scope);record(agency,"RECORD_SUCCESSION_READINESS_FINDINGS",
                "SUCCESSION_READINESS_ASSESSMENT",value,null,out,null,correlation);return out;
    }

    @Override
    public AssessmentResponse recommend(String agency,String id,RecommendationInput input,String actor,
                                        PermissionDataScope scope,String correlation){
        var value=assessment(agency,id);var actorAssignment=panelAction(agency,value,actor,scope);
        if(scope!=PermissionDataScope.AGENCY_WIDE&&(!actorAssignment.isVotingMember()||
                actorAssignment.getStatus()!=SuccessionReadinessPanelAssignment.Status.ASSIGNED))throw new AccessDeniedException(
                "Only a non-conflicted voting panel member may submit the recommendation");
        check(value.getVersion(),input.recordVersion());var rows=findings.findByAgencyIdAndAssessmentIdOrderByDisplayOrder(agency,id);
        if(rows.isEmpty())throw new IllegalStateException("Readiness criterion findings are required");
        if(rows.stream().anyMatch(f->f.getOutcome()==SuccessionReadinessCriterionFinding.Outcome.NEEDS_EVIDENCE)||
                value.getNotAssessedCount()>0)throw new IllegalStateException(
                "Missing competency or criterion evidence blocks readiness recommendation");
        requireQuorum(agency,value);var level=level(agency,value,input.readinessLevelId());
        value.recommend(level,input.recommendPoolMembership(),input.narrative(),actor,Instant.now());
        assessments.saveAndFlush(value);var out=response(agency,value,actor,scope);record(agency,
                "RECOMMEND_SUCCESSION_READINESS","SUCCESSION_READINESS_ASSESSMENT",value,null,out,
                input.narrative(),correlation);return out;
    }

    @Override
    public AssessmentResponse decide(String agency,String id,DecisionInput input,String token,String actor,
                                     PermissionDataScope scope,String correlation){
        if(scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException(
                "Final pool decision requires Agency Wide authority");
        var value=assessment(agency,id);check(value.getVersion(),input.recordVersion());var candidate=candidate(agency,value.getCandidateId());
        if(candidate.getEmployeeNo().equalsIgnoreCase(actor))throw new AccessDeniedException(
                "Candidate cannot finalize their own pool decision");
        if(decisions.findByAgencyIdAndAssessmentId(agency,id).isPresent())throw new OptimisticConflictException(
                "A final pool decision already exists");
        var current=hr.get(candidate.getEmployeeId(),token);
        if(!candidate.getAppointmentId().equals(current.appointmentId())||
                !candidate.getEmployeeFingerprint().equals(current.sourceFingerprint()))throw new OptimisticConflictException(
                "Candidate appointment changed; readiness decision is stale");
        var fresh=targetGaps.calculate(agency,PositionTargetType.valueOf(candidate.getTargetType()),
                candidate.getTargetJobPositionId(),candidate.getTargetPlantillaId(),candidate.getEmployeeNo(),LocalDate.now(MANILA));
        if(!value.getPositionProfileId().equals(fresh.positionProfileId())||
                value.getPositionProfileDefinitionVersion()!=fresh.positionProfileVersion()||
                value.getPositionProfileContentRevision()!=fresh.positionProfileRevision()||
                !value.getPositionProfileFingerprint().equals(fresh.positionProfileFingerprint())||
                !value.getPersonProfileId().equals(fresh.personProfileId())||
                value.getPersonProfileVersion()!=fresh.personProfileVersion())throw new OptimisticConflictException(
                "Competency sources changed; create a successor readiness review");
        var level=level(agency,value,input.readinessLevelId());var policy=policy(agency,cycle(agency,value.getCycleId()).getPolicyVersionId());
        var decision=decisions.saveAndFlush(new SuccessionPoolDecision(agency,value,level,input.outcome(),
                input.employeeVisibleReason(),input.restrictedRationale(),actor,Instant.now(),policy.getMembershipValidityMonths()));
        value.finalizeDecision(actor,decision.getDecidedAt());assessments.saveAndFlush(value);
        var out=response(agency,value,actor,scope);record(agency,"FINALIZE_SUCCESSION_POOL_DECISION",
                "SUCCESSION_POOL_DECISION",decision,null,out,input.restrictedRationale(),correlation);return out;
    }

    private SuccessionReadinessPanelAssignment panelAction(String agency,SuccessionReadinessAssessment value,
                                                            String actor,PermissionDataScope scope){
        denyCandidateAction(agency,value,actor);
        if(scope==PermissionDataScope.AGENCY_WIDE)return assignments.findByAgencyIdAndAssessmentIdOrderByMemberRole(
                agency,value.getId()).stream().filter(x->x.getEmployeeNo().equalsIgnoreCase(actor)).findFirst().orElse(null);
        if(scope!=PermissionDataScope.ASSIGNED_RECORDS)throw new AccessDeniedException("Readiness review is not assigned");
        var row=assignments.findByAgencyIdAndAssessmentIdOrderByMemberRole(agency,value.getId()).stream()
                .filter(x->x.getEmployeeNo().equalsIgnoreCase(actor)).findFirst().orElseThrow(()->new AccessDeniedException(
                        "Readiness review is not assigned to the current panel member"));
        if(row.getStatus()!=SuccessionReadinessPanelAssignment.Status.ASSIGNED)throw new AccessDeniedException(
                "A recused or replaced panel member cannot act on this review");return row;
    }

    private void requireQuorum(String agency,SuccessionReadinessAssessment value){
        var charter=charters.findByIdAndAgencyId(value.getCharterVersionId(),agency).orElseThrow(()->new ResourceNotFoundException("Committee charter not found"));
        var rows=assignments.findByAgencyIdAndAssessmentIdOrderByMemberRole(agency,value.getId());
        long total=rows.stream().filter(SuccessionReadinessPanelAssignment::isVotingMember).count();
        long active=rows.stream().filter(SuccessionReadinessPanelAssignment::isVotingMember)
                .filter(x->x.getStatus()==SuccessionReadinessPanelAssignment.Status.ASSIGNED).count();
        long required=charter.getQuorumType()==SuccessionCommitteeCharterVersion.QuorumType.COUNT
                ?charter.getQuorumValue().setScale(0,RoundingMode.CEILING).longValueExact()
                :(long)Math.ceil(total*charter.getQuorumValue().doubleValue()/100d);
        if(active<required)throw new IllegalStateException("Non-conflicted voting panel does not satisfy quorum");
    }

    private boolean visible(String agency,SuccessionReadinessAssessment value,String actor,PermissionDataScope scope){
        var candidate=candidate(agency,value.getCandidateId());if(candidate.getEmployeeNo().equalsIgnoreCase(actor))return true;
        if(scope==PermissionDataScope.AGENCY_WIDE)return true;if(scope!=PermissionDataScope.ASSIGNED_RECORDS)return false;
        return assignments.findByAgencyIdAndAssessmentIdOrderByMemberRole(agency,value.getId()).stream()
                .anyMatch(x->x.getEmployeeNo().equalsIgnoreCase(actor)&&x.getStatus()==SuccessionReadinessPanelAssignment.Status.ASSIGNED);
    }
    private void access(String agency,SuccessionReadinessAssessment value,String actor,PermissionDataScope scope){if(!visible(agency,value,actor,scope))throw new AccessDeniedException("Readiness review is outside the permitted data scope");}
    private void panelAuthority(String agency,SuccessionCandidate candidate,String actor,PermissionDataScope scope){
        if(candidate.getEmployeeNo().equalsIgnoreCase(actor))throw new AccessDeniedException(
                "Candidate cannot perform panel action on their own readiness review");
        if(scope==PermissionDataScope.AGENCY_WIDE)return;if(scope!=PermissionDataScope.ASSIGNED_RECORDS)throw new AccessDeniedException("Readiness initialization requires panel authority");
        var cycle=cycle(agency,candidate.getCycleId());LocalDate today=LocalDate.now(MANILA);
        boolean member=members.findByAgencyIdAndCharterVersionIdOrderByMemberRole(agency,cycle.getCharterVersionId()).stream()
                .anyMatch(m->m.getEmployeeNo().equalsIgnoreCase(actor)&&effective(m,today));
        if(!member)throw new AccessDeniedException("Candidate is not assigned to the current panel member");
    }

    private AssessmentResponse response(String agency,SuccessionReadinessAssessment value,String actor,PermissionDataScope scope){
        var candidate=candidate(agency,value.getCandidateId());boolean own=candidate.getEmployeeNo().equalsIgnoreCase(actor);
        boolean privileged=!own&&(scope==PermissionDataScope.AGENCY_WIDE||assignments.findByAgencyIdAndAssessmentIdOrderByMemberRole(agency,value.getId()).stream().anyMatch(x->x.getEmployeeNo().equalsIgnoreCase(actor)&&x.getStatus()==SuccessionReadinessPanelAssignment.Status.ASSIGNED));
        var gapRows=gaps.findByAgencyIdAndAssessmentIdOrderByDisplayOrder(agency,value.getId()).stream().map(x->new GapItemResponse(x.getId(),x.getCompetencyCode(),x.getCompetencyName(),x.getCompetencyDefinitionVersion(),x.getRequiredLevelCode(),x.getRequiredLevelLabel(),x.getAttainedLevelOrder(),x.getAttainedLevelCode(),x.getAttainedLevelLabel(),x.getGapValue(),x.getGapClassification(),x.getNotAssessedReason(),x.getRequirementClassification(),x.getCriticalityCode(),x.getDisplayOrder())).toList();
        var panel=privileged?assignments.findByAgencyIdAndAssessmentIdOrderByMemberRole(agency,value.getId()).stream().map(x->new AssignmentResponse(x.getId(),x.getVersion(),x.getEmployeeNo(),x.getDisplayName(),x.getMemberRole(),x.isVotingMember(),x.getStatus().name(),x.isConflictDeclared(),x.getConflictSource(),x.getConflictReason(),x.getReplacementAssignmentId())).toList():List.<AssignmentResponse>of();
        var fs=findings.findByAgencyIdAndAssessmentIdOrderByDisplayOrder(agency,value.getId()).stream().filter(x->privileged||x.isEmployeeVisible()).map(x->new FindingResponse(x.getId(),x.getCriterionCode(),x.getCriterionLabel(),x.getOutcome().name(),x.getRationale(),x.getSourceReference(),x.isEmployeeVisible(),x.getDisplayOrder())).toList();
        var d=decisions.findByAgencyIdAndAssessmentId(agency,value.getId()).map(x->new DecisionResponse(x.getId(),x.getOutcome().name(),x.getReadinessLevelCode(),x.getReadinessLevelLabel(),x.getEmployeeVisibleReason(),privileged?x.getRestrictedRationale():null,privileged?x.getDecidedBy():null,x.getDecidedAt(),x.getEffectiveFrom(),x.getExpiresOn())).orElse(null);
        boolean revealRecommendation=privileged||value.getStatus()==SuccessionReadinessAssessment.Status.FINALIZED;
        return new AssessmentResponse(value.getId(),value.getVersion(),value.getCandidateId(),value.getCycleId(),candidate.getEmployeeNo(),candidate.getEmployeeName(),candidate.getTargetPositionName(),value.getStatus().name(),value.getReadinessScaleVersionId(),value.getReadinessScaleDefinitionVersion(),value.getPositionProfileId(),value.getPositionProfileDefinitionVersion(),value.getPositionProfileContentRevision(),value.getPersonProfileId(),value.getPersonProfileVersion(),value.getGapCalculatedAt(),value.getBelowCount(),value.getMeetsCount(),value.getExceedsCount(),value.getNotAssessedCount(),revealRecommendation?value.getRecommendedLevelCode():null,revealRecommendation?value.getRecommendedLevelLabel():null,revealRecommendation?value.getPoolRecommended():null,revealRecommendation?value.getRecommendationNarrative():null,revealRecommendation?value.getRecommendedBy():null,revealRecommendation?value.getRecommendedAt():null,privileged?value.getFinalizedBy():null,value.getFinalizedAt(),gapRows,panel,fs,d);
    }

    private SuccessionReadinessAssessment assessment(String a,String id){return assessments.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Readiness assessment not found"));}
    private SuccessionReadinessPanelAssignment assignment(String a,String assessment,String id){return assignments.findByIdAndAgencyIdAndAssessmentId(id,a,assessment).orElseThrow(()->new ResourceNotFoundException("Panel assignment not found"));}
    private SuccessionCandidate candidate(String a,String id){return candidates.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Succession candidate not found"));}
    private SuccessionCycle cycle(String a,String id){return cycles.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Succession cycle not found"));}
    private SuccessionGovernancePolicyVersion policy(String a,String id){return policies.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Succession policy not found"));}
    private SuccessionReadinessLevel level(String a,SuccessionReadinessAssessment v,String id){return levels.findByIdAndAgencyIdAndScaleVersionId(id,a,v.getReadinessScaleVersionId()).orElseThrow(()->new ResourceNotFoundException("Readiness level not found in frozen scale"));}
    private SuccessionReadinessScaleVersion effectiveScale(String a,LocalDate day){var found=scales.findByAgencyIdAndStatus(a,SuccessionDefinitionStatus.PUBLISHED).stream().filter(x->x.effectiveOn(day)).toList();if(found.isEmpty())throw new ResourceNotFoundException("No readiness scale is effective");if(found.size()>1)throw new PublicationConflictException("More than one readiness scale is effective");if(levels.findByAgencyIdAndScaleVersionIdOrderByDisplayOrder(a,found.get(0).getId()).isEmpty())throw new PublicationConflictException("Effective readiness scale has no levels");return found.get(0);}
    private static boolean effective(SuccessionCommitteeMember m,LocalDate day){return !day.isBefore(m.getEffectiveFrom())&&(m.getEffectiveTo()==null||!day.isAfter(m.getEffectiveTo()));}
    private static boolean related(SuccessionCommitteeConflict x,String member,String candidate){return x.getDeclaringEmployeeNo().equalsIgnoreCase(member)&&x.getRelatedEmployeeNo().equalsIgnoreCase(candidate)||x.getRelatedEmployeeNo().equalsIgnoreCase(member)&&x.getDeclaringEmployeeNo().equalsIgnoreCase(candidate);}
    private void denyCandidateAction(String agency,SuccessionReadinessAssessment value,String actor){if(candidate(agency,value.getCandidateId()).getEmployeeNo().equalsIgnoreCase(actor))throw new AccessDeniedException("Candidate cannot perform panel action on their own readiness review");}
    private static void check(long actual,Long expected){if(expected==null||actual!=expected)throw new OptimisticConflictException("Record changed; reload and retry");}
    private void record(String a,String action,String type,com.primehr.rsp.domain.RspAuditedEntity entity,Object before,Object after,String reason,String correlation){audit.record(a,action,type,entity.getId(),null,entity.getVersion(),before,after,reason,correlation);}
}
