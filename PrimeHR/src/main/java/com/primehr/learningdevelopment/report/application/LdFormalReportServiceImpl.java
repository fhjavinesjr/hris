package com.primehr.learningdevelopment.report.application;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.learningdevelopment.completion.domain.*;
import com.primehr.learningdevelopment.completion.infrastructure.*;
import com.primehr.learningdevelopment.delivery.domain.*;
import com.primehr.learningdevelopment.delivery.infrastructure.*;
import com.primehr.learningdevelopment.evaluation.domain.*;
import com.primehr.learningdevelopment.evaluation.infrastructure.*;
import com.primehr.learningdevelopment.participation.domain.*;
import com.primehr.learningdevelopment.participation.infrastructure.*;
import com.primehr.learningdevelopment.plan.domain.*;
import com.primehr.learningdevelopment.plan.infrastructure.*;
import com.primehr.learningdevelopment.report.application.LdFormalReportData.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.ApplicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class LdFormalReportServiceImpl implements LdFormalReportService {
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("MMM d, uuuu",Locale.ENGLISH);
    private final LdIndividualDevelopmentPlanRepository idps; private final LdIndividualDevelopmentPlanItemRepository idpItems;
    private final LdIdpRouteRepository idpRoutes; private final LdIdpRouteStepRepository idpSteps;
    private final LdTrainingOrderSourceRepository orders; private final LdTrainingOrderParticipantRepository participants;
    private final LdCompletionCaseRepository completions; private final LdCompletionOutputRepository outputs;
    private final LdEvaluationTaskRepository tasks; private final LdEvaluationResultRepository results;
    private final LdLearningActionPlanRepository actionPlans; private final LdOfferingRepository offerings;
    private final LdProgramRepository programs; private final LdFormalReportRenderer renderer; private final PrimeHrAuditService audit;

    public LdFormalReportServiceImpl(LdIndividualDevelopmentPlanRepository idps,LdIndividualDevelopmentPlanItemRepository idpItems,
            LdIdpRouteRepository idpRoutes,LdIdpRouteStepRepository idpSteps,LdTrainingOrderSourceRepository orders,
            LdTrainingOrderParticipantRepository participants,LdCompletionCaseRepository completions,
            LdCompletionOutputRepository outputs,LdEvaluationTaskRepository tasks,LdEvaluationResultRepository results,
            LdLearningActionPlanRepository actionPlans,LdOfferingRepository offerings,LdProgramRepository programs,
            LdFormalReportRenderer renderer,PrimeHrAuditService audit){this.idps=idps;this.idpItems=idpItems;this.idpRoutes=idpRoutes;this.idpSteps=idpSteps;this.orders=orders;this.participants=participants;this.completions=completions;this.outputs=outputs;this.tasks=tasks;this.results=results;this.actionPlans=actionPlans;this.offerings=offerings;this.programs=programs;this.renderer=renderer;this.audit=audit;}

    @Override public byte[] individualPlan(String agency,String id,String actor,PermissionDataScope scope,String correlation){
        LdIndividualDevelopmentPlan plan=idps.findByIdAndAgencyId(id,agency).orElseThrow(()->notFound("Individual Development Plan was not found"));
        if(plan.getStatus()!=LdIndividualDevelopmentPlan.Status.APPROVED)throw conflict("An Individual Development Plan report requires APPROVED status");
        if(scope==PermissionDataScope.OWN_RECORDS&&!plan.getEmployeeNo().equalsIgnoreCase(actor))deny();
        if(scope==PermissionDataScope.ASSIGNED_RECORDS&&!assignedIdp(agency,plan.getId(),actor))deny();
        List<Row> rows=new ArrayList<>();int n=1;for(LdIndividualDevelopmentPlanItem item:idpItems.findByAgencyIdAndPlanIdOrderByDisplayOrderAsc(agency,id))rows.add(new Row(n++,item.getInterventionCategory(),item.getDevelopmentObjective(),join("Current: "+item.getCurrentState(),"Target: "+item.getTargetState(),"Measure: "+item.getSuccessMeasure(),"Employee: "+item.getEmployeeCommitment(),"Supervisor: "+item.getSupervisorCommitment()),item.getPriority()+" / "+date(item.getTargetDate())));
        Document document=new Document(new Header("INDIVIDUAL DEVELOPMENT PLAN",plan.getOverallGoal(),plan.getStatus().name(),person(plan.getEmployeeName(),plan.getEmployeeNo()),join(plan.getAreaName(),plan.getBusinessUnitName()),String.valueOf(plan.getPlanningYear()),"IDP "+plan.getId()+" version "+plan.getPlanVersion()+"; employee source "+plan.getEmployeeFingerprint(),generated(actor)),rows);
        return rendered(agency,"GENERATE_LD_IDP_REPORT",id,plan.getPlanVersion(),plan.getVersion(),document,correlation);
    }

    @Override public byte[] trainingOrder(String agency,String offeringId,String actor,PermissionDataScope scope,String correlation){
        LdTrainingOrderSource order=orders.findByAgencyIdAndOfferingId(agency,offeringId).orElseThrow(()->notFound("Approved Training Order was not found"));
        if(scope==PermissionDataScope.OWN_RECORDS)throw new AccessDeniedException("Training Order reports require Assigned Records or Agency Wide scope");
        LdOffering offering=offering(agency,offeringId);if(scope==PermissionDataScope.ASSIGNED_RECORDS&&!program(agency,offering.getProgramId()).getAccountableEmployeeNo().equalsIgnoreCase(actor))deny();
        List<Row> rows=new ArrayList<>();int n=1;for(LdTrainingOrderParticipant item:participants.findByAgencyIdAndTrainingOrderIdOrderByDisplayOrderAsc(agency,order.getId()))rows.add(new Row(n++,"PARTICIPANT",person(item.getEmployeeName(),item.getEmployeeNo()),"Appointment "+item.getAppointmentId(),item.getEnrollmentStatus()));
        Document document=new Document(new Header("TRAINING ORDER",order.getOrderNumber()+" — "+order.getSubject(),"APPROVED",order.getApprovedBy(),offering.getTitle(),offering.getCode()+" version "+offering.getOfferingVersion(),join(order.getAttendanceDirective(),order.getDressCode(),order.getNotes(),"Snapshot "+order.getSnapshotFingerprint()),generated(actor)),rows);
        return rendered(agency,"GENERATE_LD_TRAINING_ORDER",order.getId(),order.getOfferingVersion(),order.getVersion(),document,correlation);
    }

    @Override public byte[] completionPacket(String agency,String completionId,String actor,PermissionDataScope scope,String correlation){
        LdCompletionCase completion=completion(agency,completionId);if(completion.getStatus()!=LdCompletionCase.Status.COMPLETED)throw conflict("A completion packet requires COMPLETED status");accessCompletion(agency,completion,actor,scope);
        List<Row> rows=new ArrayList<>();int n=1;rows.add(new Row(n++,"COMPLETION","Credited hours",number(completion.getCreditedHours()),date(completion.getCompletionDate())));for(LdCompletionOutput output:outputs.findByAgencyIdAndCompletionCaseIdOrderByCreatedAtAsc(agency,completionId))rows.add(new Row(n++,"OUTPUT",output.getTitle(),output.getDescription(),output.getStatus().name()));
        actionPlans.findByAgencyIdAndCompletionCaseId(agency,completionId).ifPresent(plan->rows.add(new Row(rows.size()+1,"FOLLOW-THROUGH",plan.getApplicationObjective(),join("Measure: "+plan.getSuccessMeasure(),"Outcome: "+safe(plan.getFollowUpOutcome())),plan.getStatus().name()+" / "+date(plan.getTargetDate()))));
        Document document=new Document(new Header("EMPLOYEE L&D COMPLETION RECORD",completion.getEmployeeName(),completion.getStatus().name(),person(completion.getEmployeeName(),completion.getEmployeeNo()),offering(agency,completion.getOfferingId()).getTitle(),date(completion.getCompletionDate()),"Completion "+completion.getId()+"; "+completion.getCompletionFingerprint(),generated(actor)),rows);
        return rendered(agency,"GENERATE_LD_COMPLETION_PACKET",completionId,completion.getContentRevision(),completion.getVersion(),document,correlation);
    }

    @Override public byte[] evaluationSummary(String agency,String completionId,String actor,PermissionDataScope scope,String correlation){
        LdCompletionCase completion=completion(agency,completionId);if(scope==PermissionDataScope.OWN_RECORDS)throw new AccessDeniedException("Evaluation summaries require Assigned Records or Agency Wide scope");accessCompletion(agency,completion,actor,scope);
        List<Row> rows=new ArrayList<>();int n=1;for(LdEvaluationTask task:tasks.findByAgencyIdAndCompletionCaseIdOrderByDueDateAsc(agency,completionId)){LdEvaluationResult result=task.getCurrentResultId()==null?null:results.findByIdAndAgencyId(task.getCurrentResultId(),agency).orElse(null);String detail=result==null?task.getInstrumentGuidance():join("Score: "+number(result.getScore()),"Passed: "+result.getPassed(),result.getNarrative(),"Evidence: "+safe(result.getEvidenceReference()));rows.add(new Row(n++,task.getLevelType().name(),task.getEvaluatorRole()+" / "+task.getAssignedEvaluatorNo(),detail,task.getStatus()+" / due "+date(task.getDueDate())));}
        LdOffering offering=offering(agency,completion.getOfferingId());Document document=new Document(new Header("L&D EVALUATION AND FOLLOW-THROUGH SUMMARY",offering.getTitle(),completion.getStatus().name(),person(completion.getEmployeeName(),completion.getEmployeeNo()),offering.getCode(),date(completion.getCompletionDate()),"Framework "+offering.getEvaluationFrameworkVersionId()+"; "+offering.getEvaluationFingerprint(),generated(actor)),rows);
        return rendered(agency,"GENERATE_LD_EVALUATION_SUMMARY",completionId,completion.getContentRevision(),completion.getVersion(),document,correlation);
    }

    private boolean assignedIdp(String agency,String id,String actor){return idpRoutes.findFirstByAgencyIdAndPlanIdOrderByRouteRevisionDesc(agency,id).map(route->idpSteps.findByAgencyIdAndRouteIdOrderByLevelAsc(agency,route.getId()).stream().anyMatch(step->step.getEmployeeNo().equalsIgnoreCase(actor))).orElse(false);}
    private void accessCompletion(String agency,LdCompletionCase completion,String actor,PermissionDataScope scope){if(scope==PermissionDataScope.AGENCY_WIDE)return;if(scope==PermissionDataScope.OWN_RECORDS&&completion.getEmployeeNo().equalsIgnoreCase(actor))return;LdOffering offering=offering(agency,completion.getOfferingId());if(scope==PermissionDataScope.ASSIGNED_RECORDS&&program(agency,offering.getProgramId()).getAccountableEmployeeNo().equalsIgnoreCase(actor))return;deny();}
    private byte[] rendered(String agency,String action,String id,int business,long record,Document document,String correlation){byte[] pdf=renderer.render(document);audit.record(agency,action,"LD_FORMAL_REPORT",id,business,record,null,Map.of("pdfSha256",sha(pdf),"templateVersion","PHASE-7E-v1"),null,correlation);return pdf;}
    private LdCompletionCase completion(String agency,String id){return completions.findByIdAndAgencyId(id,agency).orElseThrow(()->notFound("L&D completion case was not found"));}private LdOffering offering(String agency,String id){return offerings.findByIdAndAgencyId(id,agency).orElseThrow(()->notFound("L&D offering was not found"));}private LdProgram program(String agency,String id){return programs.findByIdAndAgencyId(id,agency).orElseThrow(()->notFound("L&D program was not found"));}
    private static String generated(String actor){return "Generated "+Instant.now()+" by "+actor;}private static String person(String name,String no){return join(name,no);}private static String date(LocalDate value){return value==null?"":DATE.format(value);}private static String safe(String value){return value==null?"":value;}private static String number(BigDecimal value){return value==null?"":value.stripTrailingZeros().toPlainString();}private static String join(String...values){return Arrays.stream(values).filter(v->v!=null&&!v.isBlank()).reduce((a,b)->a+" | "+b).orElse("");}private static void deny(){throw new AccessDeniedException("L&D report is outside the permitted record scope");}private static ApplicationConflictException conflict(String value){return new ApplicationConflictException(value);}private static ResourceNotFoundException notFound(String value){return new ResourceNotFoundException(value);}private static String sha(byte[] value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));}catch(Exception e){throw new IllegalStateException(e);}}
}
