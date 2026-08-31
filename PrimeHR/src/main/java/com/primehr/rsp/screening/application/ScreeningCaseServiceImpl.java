package com.primehr.rsp.screening.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.rsp.applicant.domain.*;
import com.primehr.rsp.applicant.infrastructure.*;
import com.primehr.rsp.screening.api.ScreeningCaseDtos.*;
import com.primehr.rsp.screening.domain.*;
import com.primehr.rsp.screening.infrastructure.*;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScreeningCaseServiceImpl implements ScreeningCaseService, ScreeningWithdrawalCoordinator {
    private final ScreeningCaseRepository cases; private final ScreeningAssignmentRepository assignments;
    private final ScreeningFindingRepository findings; private final ScreeningEvidenceLinkRepository evidenceLinks;
    private final ScreeningDecisionRepository decisions; private final PositionApplicationRepository applications;
    private final ApplicationDocumentSnapshotRepository documents; private final PublicationScreeningPolicyRepository bindings;
    private final ScreeningCriterionRepository criteria; private final ScreeningReasonCodeRepository reasons;
    private final ApplicantCommunicationRepository communications; private final PrimeHrAuditService audit;
    private final ObjectMapper json;

    public ScreeningCaseServiceImpl(ScreeningCaseRepository cases, ScreeningAssignmentRepository assignments,
                                    ScreeningFindingRepository findings, ScreeningEvidenceLinkRepository evidenceLinks,
                                    ScreeningDecisionRepository decisions, PositionApplicationRepository applications,
                                    ApplicationDocumentSnapshotRepository documents, PublicationScreeningPolicyRepository bindings,
                                    ScreeningCriterionRepository criteria, ScreeningReasonCodeRepository reasons,
                                    ApplicantCommunicationRepository communications, PrimeHrAuditService audit,
                                    ObjectMapper json) {
        this.cases=cases;this.assignments=assignments;this.findings=findings;this.evidenceLinks=evidenceLinks;
        this.decisions=decisions;this.applications=applications;this.documents=documents;this.bindings=bindings;
        this.criteria=criteria;this.reasons=reasons;this.communications=communications;this.audit=audit;this.json=json;
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<CaseResponse> list(String agency,String actor,boolean administrator,int page,int size){
        Page<ScreeningCase> result=cases.findPermitted(agency,actor,administrator,page(page,size));
        return PageResponse.from(result,this::response);
    }
    @Override @Transactional(readOnly=true)
    public CaseResponse get(String agency,String id,String actor,boolean administrator){return response(requirePermitted(agency,id,actor,administrator));}

    @Override public CaseResponse open(String agency,String applicationId,OpenCase request,String actor,String correlationId){
        PositionApplication application=applications.findByIdAndAgencyId(applicationId,agency)
                .orElseThrow(()->new ResourceNotFoundException("Application was not found"));
        requireVersion(application.getVersion(),request.applicationRecordVersion());
        if(application.getStatus()!=PositionApplication.Status.SUBMITTED)throw new ApplicationConflictException("Only a submitted application may begin screening");
        if(cases.findByAgencyIdAndApplicationIdAndCurrentApplicationKeyIsNotNull(agency,applicationId).isPresent())throw new ApplicationConflictException("This application already has a current screening case");
        if(request.screenerEmployeeNo().equalsIgnoreCase(request.validatorEmployeeNo()))throw new ApplicationConflictException("Screener and validator must be different employees");
        PublicationScreeningPolicy binding=bindings.findByAgencyIdAndPublicationId(agency,application.getPublication().getId())
                .orElseThrow(()->new ApplicationConflictException("The vacancy publication has no screening policy binding"));
        ScreeningPolicy policy=binding.getPolicy();
        if(policy.getStatus()!=ScreeningPolicy.Status.PUBLISHED)throw new ApplicationConflictException("The bound screening policy is not published");
        List<ScreeningCase> history=cases.findByAgencyIdAndApplicationIdOrderByCaseRevisionAsc(agency,applicationId);
        ScreeningCase value=cases.saveAndFlush(new ScreeningCase(agency,application,application.getPublication().getId(),
                policy.getId(),policy.getDefinitionVersion(),history.size()+1,history.isEmpty()?null:history.get(history.size()-1).getId(),
                binding.getPolicySnapshot(),applicationSnapshot(application),actor,Instant.now()));
        List<ScreeningCriterion> ordered=criteria.findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(agency,policy.getId());
        if(ordered.isEmpty())throw new ApplicationConflictException("The bound screening policy has no criteria");
        findings.saveAll(ordered.stream().map(c->new ScreeningFinding(agency,value.getId(),c)).toList());
        assign(agency,value.getId(),request.screenerEmployeeNo(),request.validatorEmployeeNo(),actor);
        application.beginScreening(); applications.saveAndFlush(application);
        audit.record(agency,"OPEN_SCREENING_CASE","RSP_SCREENING_CASE",value.getId(),value.getCaseRevision(),value.getVersion(),null,
                Map.of("applicationId",applicationId,"screener",request.screenerEmployeeNo(),"validator",request.validatorEmployeeNo()),null,correlationId);
        return response(value);
    }

    @Override public CaseResponse assignments(String agency,String id,Assignments request,String actor,String correlationId){
        ScreeningCase value=requireCase(agency,id); requireVersion(value.getVersion(),request.recordVersion()); requireEditable(value);
        if(request.screenerEmployeeNo().equalsIgnoreCase(request.validatorEmployeeNo()))throw new ApplicationConflictException("Screener and validator must be different employees");
        List<ScreeningAssignment> existing=assignments.findByAgencyIdAndCaseIdOrderByRoleAsc(agency,id);
        existing.forEach(ScreeningAssignment::deactivate);
        upsertAssignment(existing,agency,id,request.screenerEmployeeNo(),ScreeningAssignment.Role.SCREENER,actor);
        upsertAssignment(existing,agency,id,request.validatorEmployeeNo(),ScreeningAssignment.Role.VALIDATOR,actor);
        assignments.saveAll(existing);
        audit.record(agency,"ASSIGN_SCREENING_CASE","RSP_SCREENING_CASE",id,value.getCaseRevision(),value.getVersion(),null,
                Map.of("screener",request.screenerEmployeeNo(),"validator",request.validatorEmployeeNo()),null,correlationId);
        return response(value);
    }

    @Override public CaseResponse successor(String agency,String id,Successor request,String actor,String correlationId){
        ScreeningCase predecessor=requireCase(agency,id);requireVersion(predecessor.getVersion(),request.recordVersion());
        if(!isFinal(predecessor)||!predecessor.isCurrent())throw new ApplicationConflictException("Only the current final case may be corrected");
        if(request.screenerEmployeeNo().equalsIgnoreCase(request.validatorEmployeeNo()))throw new ApplicationConflictException("Screener and validator must be different employees");
        PositionApplication application=predecessor.getApplication();predecessor.supersede();cases.saveAndFlush(predecessor);
        ScreeningCase next=cases.saveAndFlush(new ScreeningCase(agency,application,predecessor.getPublicationId(),predecessor.getPolicyId(),predecessor.getPolicyDefinitionVersion(),predecessor.getCaseRevision()+1,predecessor.getId(),predecessor.getPolicySnapshot(),predecessor.getApplicationSnapshot(),actor,Instant.now()));
        List<ScreeningCriterion> ordered=criteria.findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(agency,predecessor.getPolicyId());findings.saveAll(ordered.stream().map(c->new ScreeningFinding(agency,next.getId(),c)).toList());assign(agency,next.getId(),request.screenerEmployeeNo(),request.validatorEmployeeNo(),actor);application.beginScreeningCorrection();applications.saveAndFlush(application);audit.record(agency,"CREATE_SCREENING_CORRECTION","RSP_SCREENING_CASE",next.getId(),next.getCaseRevision(),next.getVersion(),Map.of("supersedesId",predecessor.getId()),Map.of("status","DRAFT"),request.reason(),correlationId);return response(next);
    }

    @Override public CaseResponse finding(String agency,String id,String criterionId,SaveFinding request,String actor,String correlationId){
        ScreeningCase value=requireCase(agency,id); requireVersion(value.getVersion(),request.caseRecordVersion()); requireEditable(value);
        requireAssigned(agency,id,actor,ScreeningAssignment.Role.SCREENER);
        ScreeningFinding finding=findings.findByAgencyIdAndCaseIdAndCriterionId(agency,id,criterionId)
                .orElseThrow(()->new ResourceNotFoundException("Screening criterion was not found"));
        requireVersion(finding.getVersion(),request.findingRecordVersion());
        ScreeningFinding.Result result=parse(ScreeningFinding.Result.class,request.result(),"result");
        validateEvidenceReferences(value,request.evidence());
        finding.record(result,request.remarks(),actor,Instant.now()); findings.saveAndFlush(finding);
        evidenceLinks.deleteByAgencyIdAndFindingId(agency,finding.getId());
        evidenceLinks.saveAll(request.evidence().stream().map(e->new ScreeningEvidenceLink(agency,id,finding.getId(),
                parse(ScreeningEvidenceLink.Type.class,e.type(),"evidence.type"),e.referenceId(),e.label(),e.staffDeclaration())).toList());
        audit.record(agency,"RECORD_SCREENING_FINDING","RSP_SCREENING_CASE",id,value.getCaseRevision(),value.getVersion(),null,
                Map.of("criterionId",criterionId,"result",result.name(),"evidenceCount",request.evidence().size()),request.remarks(),correlationId);
        return response(value);
    }

    @Override public CaseResponse submit(String agency,String id,Submit request,String actor,String correlationId){
        ScreeningCase value=requireCase(agency,id); requireVersion(value.getVersion(),request.recordVersion()); requireAssigned(agency,id,actor,ScreeningAssignment.Role.SCREENER);
        if(value.getStatus()==ScreeningCase.Status.SUBMITTED)return response(value);
        requireEditable(value); ScreeningCase.Outcome outcome=parse(ScreeningCase.Outcome.class,request.recommendation(),"recommendation");
        List<ScreeningFinding> all=findings.findByAgencyIdAndCaseIdOrderByDisplayOrderAsc(agency,id); validateFindings(agency,all,outcome);
        ScreeningReasonCode reason=validateReason(agency,value,outcome,request.reasonCodeId(),request.internalExplanation());
        String safe=outcome==ScreeningCase.Outcome.DISQUALIFIED?reason.getPublicSafeText():request.applicantSafeReason();
        value.submit(outcome,reason==null?null:reason.getId(),reason==null?null:reason.getCode(),request.internalExplanation(),safe,actor,Instant.now());
        cases.saveAndFlush(value); audit.record(agency,"SUBMIT_SCREENING_RECOMMENDATION","RSP_SCREENING_CASE",id,value.getCaseRevision(),value.getVersion(),null,Map.of("outcome",outcome.name()),request.internalExplanation(),correlationId);return response(value);
    }

    @Override public CaseResponse returnCase(String agency,String id,Transition request,String actor,String correlationId){
        ScreeningCase value=requireCase(agency,id); requireVersion(value.getVersion(),request.recordVersion()); requireAssigned(agency,id,actor,ScreeningAssignment.Role.VALIDATOR);requireIndependent(value,actor);
        value.returnToScreener(actor,request.reason(),Instant.now());cases.saveAndFlush(value);audit.record(agency,"RETURN_SCREENING_CASE","RSP_SCREENING_CASE",id,value.getCaseRevision(),value.getVersion(),null,Map.of("status","RETURNED"),request.reason(),correlationId);return response(value);
    }

    @Override public CaseResponse finalizeCase(String agency,String id,Finalize request,String actor,String correlationId){
        ScreeningCase value=requireCase(agency,id);requireVersion(value.getVersion(),request.recordVersion());requireAssigned(agency,id,actor,ScreeningAssignment.Role.VALIDATOR);requireIndependent(value,actor);
        if(isFinal(value))return response(value); ScreeningCase.Outcome outcome=Objects.requireNonNull(value.getRecommendation(),"recommendation");
        value.finalizeDecision(outcome,actor,Instant.now()); saveDecisionAndComplete(value,outcome,actor,false,null,correlationId);return response(value);
    }

    @Override public CaseResponse overrideCase(String agency,String id,AdminOverride request,String actor,String correlationId){
        ScreeningCase value=requireCase(agency,id);requireVersion(value.getVersion(),request.recordVersion());if(isFinal(value))return response(value);
        ScreeningCase.Outcome outcome=parse(ScreeningCase.Outcome.class,request.outcome(),"outcome");ScreeningReasonCode reason=validateReason(agency,value,outcome,request.reasonCodeId(),request.internalExplanation());
        value.overrideDecision(outcome,actor,Instant.now());
        String safe=outcome==ScreeningCase.Outcome.DISQUALIFIED?reason.getPublicSafeText():request.applicantSafeReason();
        decisions.save(new ScreeningDecision(agency,id,outcome,reason==null?null:reason.getId(),reason==null?null:reason.getCode(),request.internalExplanation(),safe,value.getSubmittedBy(),actor,Instant.now(),true,request.overrideReason()));
        completeApplication(value,outcome,actor,safe,correlationId);cases.saveAndFlush(value);
        audit.record(agency,"ADMIN_OVERRIDE_SCREENING","RSP_SCREENING_CASE",id,value.getCaseRevision(),value.getVersion(),Map.of("recommendation",String.valueOf(value.getRecommendation())),Map.of("outcome",outcome.name()),request.overrideReason(),correlationId);return response(value);
    }

    @Override @Transactional(readOnly=true) public PageResponse<CaseResponse> history(String agency,String id,String actor,boolean administrator,int page,int size){
        ScreeningCase selected=requirePermitted(agency,id,actor,administrator);List<ScreeningCase> all=cases.findByAgencyIdAndApplicationIdOrderByCaseRevisionAsc(agency,selected.getApplication().getId());int start=Math.min(Math.max(0,page)*Math.max(1,size),all.size());int end=Math.min(start+Math.max(1,size),all.size());List<CaseResponse> content=all.subList(start,end).stream().map(this::response).toList();int pages=(int)Math.ceil(all.size()/(double)Math.max(1,size));return new PageResponse<>(content,Math.max(0,page),Math.max(1,size),all.size(),pages,start==0,end==all.size());
    }

    @Override public void cancelOpenCase(String agency,String applicationId,String actor,String reason,String correlationId){
        cases.findByAgencyIdAndApplicationIdAndCurrentApplicationKeyIsNotNull(agency,applicationId).ifPresent(value->{if(!isFinal(value)){value.cancel(actor,reason,Instant.now());cases.saveAndFlush(value);audit.record(agency,"CANCEL_SCREENING_CASE","RSP_SCREENING_CASE",value.getId(),value.getCaseRevision(),value.getVersion(),null,Map.of("status","CANCELLED"),reason,correlationId);}});
    }

    private void saveDecisionAndComplete(ScreeningCase value,ScreeningCase.Outcome outcome,String actor,boolean override,String overrideReason,String correlationId){
        decisions.save(new ScreeningDecision(value.getAgencyId(),value.getId(),outcome,value.getRecommendationReasonCodeId(),value.getRecommendationReasonCode(),value.getRecommendationExplanation(),value.getRecommendationSafeReason(),value.getSubmittedBy(),actor,Instant.now(),override,overrideReason));completeApplication(value,outcome,actor,value.getRecommendationSafeReason(),correlationId);cases.saveAndFlush(value);audit.record(value.getAgencyId(),"FINALIZE_SCREENING_DECISION","RSP_SCREENING_CASE",value.getId(),value.getCaseRevision(),value.getVersion(),null,Map.of("outcome",outcome.name()),value.getRecommendationExplanation(),correlationId);
    }
    private void completeApplication(ScreeningCase value,ScreeningCase.Outcome outcome,String actor,String safeReason,String correlationId){
        PositionApplication application=value.getApplication();application.completeScreening(outcome==ScreeningCase.Outcome.QUALIFIED?PositionApplication.Status.QUALIFIED:PositionApplication.Status.DISQUALIFIED);applications.saveAndFlush(application);communications.save(new ApplicantCommunication(value.getAgencyId(),application.getId(),application.getApplicantId(),ApplicantCommunication.Direction.SYSTEM_TO_APPLICANT,"Application status update",safeReason,actor,Instant.now(),correlationId));
    }
    private void validateFindings(String agency,List<ScreeningFinding> all,ScreeningCase.Outcome outcome){
        if(all.isEmpty())throw new ApplicationConflictException("The screening case has no findings");boolean disqualifyingNotMet=false;
        for(ScreeningFinding f:all){if(f.getResult()==null||!f.isHumanConfirmed()||f.getResult()==ScreeningFinding.Result.NEEDS_REVIEW)throw new ApplicationConflictException("Every criterion must be human-confirmed and resolved");if(f.isRequiresEvidence()&&evidenceLinks.findByAgencyIdAndFindingId(agency,f.getId()).isEmpty())throw new ApplicationConflictException("Required evidence is missing for "+f.getCriterionCode());if(f.isDisqualifying()&&f.getResult()==ScreeningFinding.Result.NOT_MET)disqualifyingNotMet=true;if(outcome==ScreeningCase.Outcome.QUALIFIED&&(f.isMandatory()||f.isDisqualifying())&&f.getResult()!=ScreeningFinding.Result.MET&&!(f.getResult()==ScreeningFinding.Result.NOT_APPLICABLE&&f.isAllowsNotApplicable()))throw new ApplicationConflictException("QUALIFIED is inconsistent with mandatory findings");}
        if(outcome==ScreeningCase.Outcome.DISQUALIFIED&&!disqualifyingNotMet)throw new ApplicationConflictException("DISQUALIFIED requires a disqualifying NOT_MET finding");
    }
    private ScreeningReasonCode validateReason(String agency,ScreeningCase value,ScreeningCase.Outcome outcome,String reasonId,String explanation){
        if(outcome==ScreeningCase.Outcome.QUALIFIED){if(reasonId==null||reasonId.isBlank())return null;}
        if(reasonId==null||reasonId.isBlank())throw new ApplicationConflictException("A compatible reason code is required");ScreeningReasonCode reason=reasons.findByAgencyIdAndPolicyIdOrderByDisplayOrderAsc(agency,value.getPolicyId()).stream().filter(r->r.getId().equals(reasonId)).findFirst().orElseThrow(()->new ResourceNotFoundException("Screening reason code was not found"));if(!reason.getOutcomeCompatibility().name().equals(outcome.name()))throw new ApplicationConflictException("Reason code is incompatible with the outcome");if(reason.isRemarksRequired()&&(explanation==null||explanation.isBlank()))throw new ApplicationConflictException("Internal explanation is required for this reason");return reason;
    }
    private void validateEvidenceReferences(ScreeningCase value,List<Evidence> evidence){for(Evidence item:evidence){ScreeningEvidenceLink.Type type=parse(ScreeningEvidenceLink.Type.class,item.type(),"evidence.type");if(type==ScreeningEvidenceLink.Type.APPLICATION_DOCUMENT){documents.findByIdAndAgencyIdAndApplicationId(item.referenceId(),value.getAgencyId(),value.getApplication().getId()).orElseThrow(()->new ApplicationConflictException("Evidence is not part of the immutable application submission"));}else if(type!=ScreeningEvidenceLink.Type.STAFF_DECLARATION&&!item.referenceId().equals(value.getApplication().getId()))throw new ApplicationConflictException("Snapshot evidence must reference the owning application");}}
    private void assign(String agency,String caseId,String screener,String validator,String actor){assignments.saveAll(List.of(new ScreeningAssignment(agency,caseId,screener,ScreeningAssignment.Role.SCREENER,actor,Instant.now()),new ScreeningAssignment(agency,caseId,validator,ScreeningAssignment.Role.VALIDATOR,actor,Instant.now())));}
    private void upsertAssignment(List<ScreeningAssignment> existing,String agency,String caseId,String employee,ScreeningAssignment.Role role,String actor){ScreeningAssignment match=existing.stream().filter(a->a.getEmployeeNo().equalsIgnoreCase(employee)&&a.getRole()==role).findFirst().orElse(null);if(match==null){match=new ScreeningAssignment(agency,caseId,employee,role,actor,Instant.now());existing.add(match);}else match.activate(actor,Instant.now());}
    private ScreeningCase requireCase(String agency,String id){return cases.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("Screening case was not found"));}
    private ScreeningCase requirePermitted(String agency,String id,String actor,boolean administrator){ScreeningCase value=requireCase(agency,id);if(!administrator&&assignments.findByAgencyIdAndCaseIdAndActiveTrueOrderByRoleAsc(agency,id).stream().noneMatch(a->a.getEmployeeNo().equalsIgnoreCase(actor)))throw new AccessDeniedException("Only assigned screening staff may access this case");return value;}
    private void requireAssigned(String agency,String id,String actor,ScreeningAssignment.Role role){if(!assignments.existsByAgencyIdAndCaseIdAndEmployeeNoAndRoleAndActiveTrue(agency,id,actor,role))throw new AccessDeniedException("The authenticated employee is not the assigned "+role.name().toLowerCase(Locale.ROOT));}
    private void requireIndependent(ScreeningCase value,String actor){if(actor.equalsIgnoreCase(value.getSubmittedBy()))throw new AccessDeniedException("A validator cannot finalize their own screening recommendation");}
    private static void requireEditable(ScreeningCase value){if(value.getStatus()!=ScreeningCase.Status.DRAFT&&value.getStatus()!=ScreeningCase.Status.RETURNED)throw new ApplicationConflictException("Only draft or returned screening can be changed");}
    private static boolean isFinal(ScreeningCase value){return value.getStatus()==ScreeningCase.Status.QUALIFIED||value.getStatus()==ScreeningCase.Status.DISQUALIFIED;}
    private static void requireVersion(long actual,long expected){if(actual!=expected)throw new OptimisticConflictException("Expected recordVersion "+expected+" but current version is "+actual);}
    private static <E extends Enum<E>> E parse(Class<E> type,String value,String field){try{return Enum.valueOf(type,value.trim().toUpperCase(Locale.ROOT));}catch(Exception e){throw new IllegalArgumentException(field+" is invalid");}}
    private static PageRequest page(int page,int size){return PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)));}
    private String applicationSnapshot(PositionApplication a){Map<String,Object> snapshot=new LinkedHashMap<>();snapshot.put("applicationId",a.getId());snapshot.put("applicationVersion",a.getApplicationVersion());snapshot.put("vacancy",a.getVacancySnapshot());snapshot.put("qualification",a.getQualificationSnapshot());snapshot.put("competency",a.getCompetencySnapshot());snapshot.put("profile",a.getProfileSnapshot());snapshot.put("submittedAt",a.getSubmittedAt());return json(snapshot);}
    private String json(Object value){try{return json.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalStateException("Screening snapshot could not be serialized",e);}}
    private CaseResponse response(ScreeningCase value){List<ScreeningAssignment> assigned=assignments.findByAgencyIdAndCaseIdAndActiveTrueOrderByRoleAsc(value.getAgencyId(),value.getId());Map<String,List<ScreeningEvidenceLink>> linked=evidenceLinks.findByAgencyIdAndCaseId(value.getAgencyId(),value.getId()).stream().collect(Collectors.groupingBy(ScreeningEvidenceLink::getFindingId));List<Finding> findingResponses=findings.findByAgencyIdAndCaseIdOrderByDisplayOrderAsc(value.getAgencyId(),value.getId()).stream().map(f->new Finding(f.getId(),f.getCriterionId(),f.getCriterionCode(),f.getCriterionLabel(),f.isMandatory(),f.isDisqualifying(),f.isAllowsNotApplicable(),f.isRequiresRemarks(),f.isRequiresEvidence(),f.getDisplayOrder(),f.getResult()==null?null:f.getResult().name(),f.getRemarks(),f.isHumanConfirmed(),f.getVersion(),linked.getOrDefault(f.getId(),List.of()).stream().map(e->new EvidenceResponse(e.getId(),e.getType().name(),e.getReferenceId(),e.getLabel(),e.getStaffDeclaration())).toList())).toList();Decision decision=decisions.findByAgencyIdAndCaseId(value.getAgencyId(),value.getId()).map(d->new Decision(d.getOutcome().name(),d.getReasonCode(),d.getInternalExplanation(),d.getApplicantSafeReason(),d.getRecommendedBy(),d.getValidatedBy(),d.getDecidedAt(),d.isAdministratorOverride(),d.getOverrideReason())).orElse(null);return new CaseResponse(value.getId(),value.getApplication().getId(),value.getApplication().getApplicantId(),value.getPublicationId(),value.getPolicyId(),value.getPolicyDefinitionVersion(),value.getCaseRevision(),value.getSupersedesId(),value.isCurrent(),value.getStatus().name(),value.getRecommendation()==null?null:value.getRecommendation().name(),value.getVersion(),value.getApplicationSnapshot(),value.getPolicySnapshot(),assigned.stream().map(a->new Assignment(a.getEmployeeNo(),a.getRole().name(),a.isActive())).toList(),findingResponses,decision);}
}
