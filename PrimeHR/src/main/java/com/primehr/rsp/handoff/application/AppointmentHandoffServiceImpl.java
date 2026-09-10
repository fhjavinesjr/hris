package com.primehr.rsp.handoff.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.config.PrimeHrProperties;
import com.primehr.integration.humanresource.HumanResourceAppointmentHandoffClient;
import com.primehr.integration.humanresource.HumanResourceDependencyException;
import com.primehr.rsp.applicant.domain.*;
import com.primehr.rsp.applicant.infrastructure.*;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.rsp.handoff.api.AppointmentHandoffDtos.*;
import com.primehr.rsp.handoff.domain.*;
import com.primehr.rsp.handoff.infrastructure.*;
import com.primehr.rsp.selection.domain.*;
import com.primehr.rsp.selection.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;

@Service @Transactional
public class AppointmentHandoffServiceImpl implements AppointmentHandoffService {
    private final AppointmentHandoffRepository handoffs;private final AppointmentHandoffAttemptRepository attempts;
    private final SelectionCaseRepository selections;private final SelectionCandidateDecisionRepository candidates;private final OfferResponseRepository offers;
    private final PositionApplicationRepository applications;private final ApplicantProfileRepository profiles;private final ApplicantAccountRepository accounts;
    private final HumanResourceAppointmentHandoffClient client;private final PrimeHrAuditService audit;private final ObjectMapper json;private final String endpoint;
    public AppointmentHandoffServiceImpl(AppointmentHandoffRepository h,AppointmentHandoffAttemptRepository at,SelectionCaseRepository s,SelectionCandidateDecisionRepository c,OfferResponseRepository o,PositionApplicationRepository ap,ApplicantProfileRepository p,ApplicantAccountRepository ac,HumanResourceAppointmentHandoffClient cl,PrimeHrAuditService au,ObjectMapper j,PrimeHrProperties properties){handoffs=h;attempts=at;selections=s;candidates=c;offers=o;applications=ap;profiles=p;accounts=ac;client=cl;audit=au;json=j;endpoint=properties.humanResource().baseUrl()+"/api/integration/v1/primehr/appointment-handoffs";}

    @Override public Response create(String agency,String selectionId,String actor,String correlation){
        SelectionCase selection=selections.findByIdAndAgencyId(selectionId,agency).orElseThrow(()->new ResourceNotFoundException("Selection case was not found"));
        if(selection.getStatus()!=SelectionCase.Status.FINALIZED||selection.getOutcome()!=SelectionCase.Outcome.SELECTED)throw new ApplicationConflictException("Appointment handoff requires a finalized SELECTED case");
        if(handoffs.findByAgencyIdAndCurrentSelectionKey(agency,selectionId).isPresent())throw new ApplicationConflictException("A current appointment handoff already exists for this selection");
        SelectionCandidateDecision selected=candidates.findByAgencyIdAndSelectionCaseIdAndSelectedTrue(agency,selectionId).orElseThrow(()->new ApplicationConflictException("The selected candidate record was not found"));
        OfferResponse offer=offers.findByAgencyIdAndSelectionCaseId(agency,selectionId).orElseThrow(()->new ApplicationConflictException("The selection offer was not found"));
        if(offer.getStatus()!=OfferResponse.Status.ACCEPTED)throw new ApplicationConflictException("An accepted offer is required for appointment handoff");
        PositionApplication application=applications.findByIdAndAgencyId(selected.getApplicationId(),agency).orElseThrow(()->new ApplicationConflictException("The selected application was not found"));
        if(application.getStatus()!=PositionApplication.Status.QUALIFIED)throw new ApplicationConflictException("The selected application is no longer qualified");
        ApplicantProfile profile=profiles.findByAgencyIdAndApplicantId(agency,selected.getApplicantId()).orElseThrow(()->new ApplicationConflictException("The selected applicant profile was not found"));
        ApplicantAccount account=accounts.findByIdAndAgencyId(selected.getApplicantId(),agency).orElseThrow(()->new ApplicationConflictException("The selected applicant account was not found"));
        String id=UUID.randomUUID().toString();int revision=Math.toIntExact(handoffs.countByAgencyIdAndSelectionCaseId(agency,selectionId)+1);
        String payload=payload(id,agency,selection,selected,offer,application,profile,account,correlation);String fingerprint=sha256(payload);
        AppointmentHandoff value=handoffs.saveAndFlush(new AppointmentHandoff(id,agency,selectionId,selected.getApplicationId(),selected.getApplicantId(),revision,payload,fingerprint,correlation));
        audit.record(agency,"CREATE_APPOINTMENT_HANDOFF","RSP_APPOINTMENT_HANDOFF",id,revision,value.getVersion(),null,response(value),null,correlation);return response(value);
    }

    @Override public Response deliver(String agency,String id,long version,String actor,String action){
        AppointmentHandoff value=require(agency,id);requireVersion(value,version);Instant now=Instant.now();
        if("reconcile".equals(action))return reconcile(value,actor,now);
        if("submit".equals(action)){if(value.getStatus()!=AppointmentHandoff.Status.DRAFT)throw new ApplicationConflictException("Only a draft handoff may be submitted");value.ready(now);}
        else if("retry".equals(action)){if(value.getStatus()!=AppointmentHandoff.Status.RETRYABLE_FAILURE)throw new ApplicationConflictException("Only a failed handoff may be retried");}
        else throw new ApplicationConflictException("Unsupported appointment handoff action");
        value.sent(now);HumanResourceAppointmentHandoffClient.DeliveryResult result=client.deliver(request(value,actor));
        recordAttempt(value,result,now);if(result.acknowledged()){var receipt=result.receipt();validateReceipt(value,receipt);value.acknowledge(receipt.receiptId(),receipt.state(),receipt.recordVersion(),now);}else value.deliveryFailed(result.diagnostic()==null?result.category():result.diagnostic());
        value=handoffs.saveAndFlush(value);audit.record(agency,"DELIVER_APPOINTMENT_HANDOFF","RSP_APPOINTMENT_HANDOFF",id,value.getHandoffRevision(),value.getVersion(),null,response(value),result.category(),value.getCorrelationId());return response(value);
    }

    private Response reconcile(AppointmentHandoff value,String actor,Instant now){
        if(value.getStatus()!=AppointmentHandoff.Status.RETRYABLE_FAILURE&&value.getStatus()!=AppointmentHandoff.Status.ACKNOWLEDGED)throw new ApplicationConflictException("Only a failed or acknowledged handoff may be reconciled");
        var result=client.reconcile(value.getAgencyId(),value.getId(),value.getCorrelationId());recordAttempt(value,result,now);
        if(result.acknowledged()){var receipt=result.receipt();validateReceipt(value,receipt);if(value.getStatus()==AppointmentHandoff.Status.RETRYABLE_FAILURE)value.acknowledge(receipt.receiptId(),receipt.state(),receipt.recordVersion(),now);else value.reconcileReceipt(receipt.state(),receipt.recordVersion());if("COMPLETED".equals(receipt.state()))value.close(now);}
        value=handoffs.saveAndFlush(value);audit.record(value.getAgencyId(),"RECONCILE_APPOINTMENT_HANDOFF","RSP_APPOINTMENT_HANDOFF",value.getId(),value.getHandoffRevision(),value.getVersion(),null,response(value),result.category(),value.getCorrelationId());return response(value);
    }

    @Override @Transactional(readOnly=true)public Response get(String agency,String id){return response(require(agency,id));}
    private AppointmentHandoff require(String agency,String id){return handoffs.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("Appointment handoff was not found"));}
    private void requireVersion(AppointmentHandoff value,long version){if(value.getVersion()!=version)throw new OptimisticConflictException("Appointment handoff record version changed");}
    private HumanResourceAppointmentHandoffClient.DeliveryRequest request(AppointmentHandoff v,String actor){try{return new HumanResourceAppointmentHandoffClient.DeliveryRequest(v.getSchemaVersion(),v.getId(),v.getAgencyId(),v.getSelectionCaseId(),v.getApplicationId(),v.getApplicantId(),v.getPayloadFingerprint(),json.readTree(v.getPayloadSnapshot()),v.getCorrelationId(),actor);}catch(JsonProcessingException e){throw new IllegalStateException("Stored handoff payload is invalid",e);}}
    private void validateReceipt(AppointmentHandoff v,HumanResourceAppointmentHandoffClient.Receipt r){if(!v.getId().equals(r.handoffId())||!v.getAgencyId().equals(r.agencyId())||!v.getSelectionCaseId().equals(r.selectionId())||!v.getPayloadFingerprint().equals(r.sourceFingerprint()))throw new HumanResourceDependencyException("HumanResource returned a mismatched handoff receipt",null);}
    private void recordAttempt(AppointmentHandoff v,HumanResourceAppointmentHandoffClient.DeliveryResult r,Instant at){int number=Math.toIntExact(attempts.countByAgencyIdAndHandoffId(v.getAgencyId(),v.getId())+1);attempts.save(new AppointmentHandoffAttempt(v.getAgencyId(),v.getId(),number,at,endpoint,r.category(),r.httpStatus(),r.diagnostic(),v.getPayloadFingerprint()));}
    private String payload(String id,String agency,SelectionCase s,SelectionCandidateDecision c,OfferResponse o,PositionApplication a,ApplicantProfile p,ApplicantAccount account,String correlation){VacancyPublication publication=a.getPublication();Map<String,Object> root=new LinkedHashMap<>();root.put("schemaVersion",1);root.put("handoffId",id);root.put("agencyId",agency);root.put("correlationId",correlation);root.put("selection",Map.of("id",s.getId(),"recordVersion",s.getVersion(),"sourceFingerprint",s.getSourceFingerprint(),"status",s.getStatus().name(),"outcome",s.getOutcome().name(),"proceedingId",s.getProceedingId(),"comparativeEvaluationId",s.getComparativeEvaluationId(),"meetingId",s.getMeetingId(),"finalizedBy",s.getFinalizedBy(),"finalizedAt",s.getFinalizedAt().toString()));root.put("offer",Map.of("status",o.getStatus().name(),"respondedAt",o.getRespondedAt().toString(),"responseDeadline",o.getResponseDeadline().toString()));Map<String,Object> applicant=new LinkedHashMap<>();applicant.put("applicantId",c.getApplicantId());applicant.put("applicationId",c.getApplicationId());applicant.put("givenName",p.getGivenName());applicant.put("middleName",p.getMiddleName());applicant.put("familyName",p.getFamilyName());applicant.put("suffix",p.getSuffix());applicant.put("email",account.getEmail());applicant.put("mobileNumber",p.getMobileNumber());root.put("applicant",applicant);Map<String,Object> position=new LinkedHashMap<>();position.put("publicationId",publication.getId());position.put("vacancyRequestId",publication.getVacancyRequest().getId());position.put("plantillaId",publication.getPlantillaId());position.put("plantillaName",publication.getPlantillaName());position.put("jobPositionId",publication.getJobPositionId());position.put("jobPositionName",publication.getJobPositionName());position.put("businessUnitId",publication.getBusinessUnitId());position.put("businessUnitCode",publication.getBusinessUnitCode());position.put("businessUnitName",publication.getBusinessUnitName());position.put("administrativeFingerprint",publication.getAdministrativeFingerprint());position.put("hrmFingerprint",publication.getHrmFingerprint());root.put("position",position);Map<String,Object> consent=new LinkedHashMap<>();consent.put("privacyNoticeId",a.getPrivacyNoticeId());consent.put("privacyNoticeVersion",a.getPrivacyNoticeVersion());consent.put("profileDeclarationAccepted",p.isDeclarationAccepted());consent.put("applicationSubmittedAt",a.getSubmittedAt()==null?null:a.getSubmittedAt().toString());root.put("consent",consent);return write(root);}
    private String write(Object value){try{return json.writeValueAsString(value);}catch(JsonProcessingException e){throw new IllegalStateException("Appointment handoff payload could not be serialized",e);}}
    private String sha256(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private Response response(AppointmentHandoff v){List<AttemptResponse> history=attempts.findByAgencyIdAndHandoffIdOrderByAttemptNumberAsc(v.getAgencyId(),v.getId()).stream().map(a->new AttemptResponse(a.getAttemptNumber(),a.getAttemptedAt(),a.getEndpointIdentity(),a.getResultCategory(),a.getHttpStatus(),a.getSafeDiagnostic(),a.getPayloadFingerprint())).toList();return new Response(v.getId(),v.getSelectionCaseId(),v.getApplicationId(),v.getApplicantId(),v.getSchemaVersion(),v.getHandoffRevision(),v.getStatus().name(),v.getPayloadFingerprint(),v.getCorrelationId(),v.getReceiptId(),v.getReceiptState(),v.getReceiptRecordVersion(),v.getReadyAt(),v.getSentAt(),v.getAcknowledgedAt(),v.getCancelledAt(),v.getClosedAt(),v.getLastFailure(),v.getVersion(),history);}
}
