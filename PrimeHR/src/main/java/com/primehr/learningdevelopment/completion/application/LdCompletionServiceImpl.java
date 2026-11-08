package com.primehr.learningdevelopment.completion.application;

import com.primehr.config.PrimeHrProperties;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.learningdevelopment.completion.api.LdCompletionDtos.*;
import com.primehr.learningdevelopment.completion.domain.*;
import com.primehr.learningdevelopment.completion.infrastructure.*;
import com.primehr.learningdevelopment.delivery.domain.*;
import com.primehr.learningdevelopment.delivery.infrastructure.*;
import com.primehr.learningdevelopment.participation.domain.*;
import com.primehr.learningdevelopment.participation.infrastructure.*;
import com.primehr.rsp.applicant.storage.DocumentStorage;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class LdCompletionServiceImpl implements LdCompletionService {
    private final LdCompletionCaseRepository cases;
    private final LdSessionAttendanceRepository attendance;
    private final LdCompletionOutputRepository outputs;
    private final LdCompletionEvidenceRepository evidence;
    private final LdEnrollmentRepository enrollments;
    private final LdTrainingOrderSourceRepository orders;
    private final LdTrainingOrderParticipantRepository orderParticipants;
    private final LdOfferingSessionRepository sessions;
    private final LdOfferingRepository offerings;
    private final LdProgramRepository programs;
    private final DocumentStorage storage;
    private final PrimeHrProperties properties;
    private final PrimeHrAuditService audit;

    public LdCompletionServiceImpl(LdCompletionCaseRepository cases,LdSessionAttendanceRepository attendance,
            LdCompletionOutputRepository outputs,LdCompletionEvidenceRepository evidence,
            LdEnrollmentRepository enrollments,LdTrainingOrderSourceRepository orders,
            LdTrainingOrderParticipantRepository orderParticipants,LdOfferingSessionRepository sessions,
            LdOfferingRepository offerings,LdProgramRepository programs,DocumentStorage storage,
            PrimeHrProperties properties,PrimeHrAuditService audit) {
        this.cases=cases;this.attendance=attendance;this.outputs=outputs;this.evidence=evidence;
        this.enrollments=enrollments;this.orders=orders;this.orderParticipants=orderParticipants;
        this.sessions=sessions;this.offerings=offerings;this.programs=programs;this.storage=storage;
        this.properties=properties;this.audit=audit;
    }

    @Override @Transactional(readOnly=true)
    public List<CompletionCaseResponse> cases(String agency,String actor,PermissionDataScope scope){
        return cases.findByAgencyIdOrderByCreatedAtDesc(agency).stream().filter(x->visible(agency,x,actor,scope)).map(x->response(agency,x)).toList();
    }
    @Override @Transactional(readOnly=true)
    public List<CompletionCaseResponse> mine(String agency,String actor){return cases.findByAgencyIdAndEmployeeNoOrderByCreatedAtDesc(agency,actor).stream().map(x->response(agency,x)).toList();}
    @Override @Transactional(readOnly=true)
    public CompletionCaseResponse get(String agency,String id,String actor,PermissionDataScope scope){LdCompletionCase value=value(agency,id);access(agency,value,actor,scope);return response(agency,value);}
    @Override @Transactional(readOnly=true)
    public CompletionCaseResponse getMine(String agency,String id,String actor){LdCompletionCase value=value(agency,id);own(value,actor);return response(agency,value);}

    @Override
    public CompletionCaseResponse open(String agency,String enrollmentId,OpenCaseInput input,String actor,PermissionDataScope scope,String correlation){
        if(scope==PermissionDataScope.OWN_RECORDS)throw new AccessDeniedException("Completion case creation requires Assigned Records or Agency Wide scope");
        LdEnrollment enrollment=enrollment(agency,enrollmentId);check(enrollment.getVersion(),input.enrollmentRecordVersion());
        if(enrollment.getStatus()!=LdEnrollment.Status.CONFIRMED)throw new IllegalStateException("Only a confirmed enrollment may enter delivery completion");
        cases.findByAgencyIdAndEnrollmentId(agency,enrollmentId).ifPresent(x->{throw new OptimisticConflictException("Completion case already exists for this enrollment");});
        LdTrainingOrderParticipant participant=orderParticipants.findByAgencyIdAndEnrollmentId(agency,enrollmentId)
                .orElseThrow(()->new IllegalStateException("Enrollment is not part of an approved Training Order roster"));
        LdTrainingOrderSource order=orders.findById(participant.getTrainingOrderId()).filter(x->x.getAgencyId().equals(agency))
                .orElseThrow(()->new ResourceNotFoundException("Training Order source not found"));
        LdOffering offering=offering(agency,enrollment.getOfferingId());staffAccess(agency,offering,actor,scope);
        List<LdOfferingSession> rows=sessionRows(agency,offering.getId());
        BigDecimal planned=rows.stream().map(LdOfferingSession::getCreditedHours).reduce(BigDecimal.ZERO,BigDecimal::add);
        LdCompletionCase created=cases.saveAndFlush(new LdCompletionCase(agency,enrollment,order.getId(),order.getSnapshotFingerprint(),planned));
        for(LdOfferingSession session:rows)attendance.save(new LdSessionAttendance(agency,created.getId(),session));
        attendance.flush();CompletionCaseResponse after=response(agency,created);
        record(agency,"OPEN_LD_COMPLETION_CASE","LD_COMPLETION_CASE",created.getId(),created.getContentRevision(),created.getVersion(),null,after,null,correlation);return after;
    }

    @Override
    public CompletionCaseResponse recordAttendance(String agency,String id,String sessionId,AttendanceInput input,String actor,PermissionDataScope scope,String correlation){
        LdCompletionCase value=value(agency,id);staffAccess(agency,offering(agency,value.getOfferingId()),actor,scope);editable(value);LdSessionAttendance row=attendance.findByAgencyIdAndCompletionCaseIdAndSessionId(agency,id,sessionId).orElseThrow(()->new ResourceNotFoundException("Completion session not found"));check(row.getVersion(),input.recordVersion());CompletionCaseResponse before=response(agency,value);row.record(input.status(),input.attendedHours(),input.creditedHours(),input.remarks(),actor,Instant.now());attendance.save(row);value.touch();value=cases.saveAndFlush(value);return changed(agency,value,"RECORD_LD_SESSION_ATTENDANCE",before,input.remarks(),correlation);
    }

    @Override
    public CompletionCaseResponse addOutput(String agency,String id,OutputInput input,String actor,PermissionDataScope scope,String correlation){
        LdCompletionCase value=value(agency,id);staffAccess(agency,offering(agency,value.getOfferingId()),actor,scope);editable(value);check(value.getVersion(),input.caseRecordVersion());CompletionCaseResponse before=response(agency,value);outputs.saveAndFlush(new LdCompletionOutput(agency,id,input.code(),input.title(),input.description()));value.touch();value=cases.saveAndFlush(value);return changed(agency,value,"ADD_LD_REQUIRED_OUTPUT",before,null,correlation);
    }

    @Override
    public CompletionCaseResponse decideOutput(String agency,String id,String outputId,OutputDecision input,boolean waive,String actor,PermissionDataScope scope,String correlation){
        LdCompletionCase value=value(agency,id);staffAccess(agency,offering(agency,value.getOfferingId()),actor,scope);editable(value);check(value.getVersion(),input.caseRecordVersion());LdCompletionOutput output=output(agency,id,outputId);check(output.getVersion(),input.outputRecordVersion());CompletionCaseResponse before=response(agency,value);if(waive)output.waive(requiredReason(input.reason()),actor,Instant.now());else{if(!evidence.existsByAgencyIdAndCompletionCaseIdAndOutputId(agency,id,outputId))throw new IllegalStateException("Output evidence is required before verification");output.verify(actor,Instant.now());}outputs.save(output);value.touch();value=cases.saveAndFlush(value);return changed(agency,value,waive?"WAIVE_LD_REQUIRED_OUTPUT":"VERIFY_LD_REQUIRED_OUTPUT",before,input.reason(),correlation);
    }

    @Override
    public EvidenceResponse upload(String agency,String id,LdCompletionEvidence.Type type,String outputId,String certificateNumber,String certificateIssuer,LocalDate certificateIssueDate,MultipartFile file,String actor,PermissionDataScope scope,boolean own,String correlation){
        LdCompletionCase value=value(agency,id);if(own)own(value,actor);else staffAccess(agency,offering(agency,value.getOfferingId()),actor,scope);editable(value);
        LdCompletionOutput output=null;if(type==LdCompletionEvidence.Type.OUTPUT){output=output(agency,id,outputId);if(output.getStatus()==LdCompletionOutput.Status.VERIFIED||output.getStatus()==LdCompletionOutput.Status.WAIVED)throw new IllegalStateException("Resolved output cannot receive new evidence");}
        if(!properties.storage().enabled())throw new IllegalStateException("Document uploads are disabled");if(file==null||file.isEmpty()||file.getSize()>properties.storage().maximumBytes())throw new IllegalArgumentException("Evidence size is invalid");String media=Objects.requireNonNullElse(file.getContentType(),"");if(!properties.storage().allowedMediaTypes().contains(media))throw new IllegalArgumentException("Evidence media type is not allowed");byte[] bytes;try{bytes=file.getBytes();}catch(IOException ex){throw new IllegalArgumentException("Evidence could not be read",ex);}verifySignature(media,bytes);String filename=safeFilename(file.getOriginalFilename());String objectKey=agency+"/learning-development/completions/"+id+"/"+UUID.randomUUID().toString().replace("-","");storage.put(objectKey,bytes,media);
        try{CompletionCaseResponse before=response(agency,value);LdCompletionEvidence saved=evidence.saveAndFlush(new LdCompletionEvidence(agency,id,outputId,type,filename,storage.provider(),objectKey,media,bytes.length,sha256(bytes),certificateNumber,certificateIssuer,certificateIssueDate,actor,Instant.now()));if(output!=null){output.submitted(actor,Instant.now());outputs.save(output);}value.touch();value=cases.saveAndFlush(value);EvidenceResponse after=evidenceResponse(saved);record(agency,"UPLOAD_LD_COMPLETION_EVIDENCE","LD_COMPLETION_EVIDENCE",saved.getId(),value.getContentRevision(),saved.getVersion(),before,after,null,correlation);return after;}catch(RuntimeException ex){storage.delete(objectKey);throw ex;}
    }

    @Override
    public EvidenceContent download(String agency,String id,String evidenceId,String actor,PermissionDataScope scope,boolean own,String correlation){LdCompletionCase value=value(agency,id);if(own)own(value,actor);else staffAccess(agency,offering(agency,value.getOfferingId()),actor,scope);LdCompletionEvidence stored=evidence.findByIdAndAgencyId(evidenceId,agency).filter(x->x.getCompletionCaseId().equals(id)).orElseThrow(()->new ResourceNotFoundException("Completion evidence not found"));record(agency,"DOWNLOAD_LD_COMPLETION_EVIDENCE","LD_COMPLETION_EVIDENCE",stored.getId(),value.getContentRevision(),stored.getVersion(),null,Map.of("completionCaseId",id,"checksum",stored.getChecksum()),null,correlation);return new EvidenceContent(stored.getOriginalFilename(),stored.getMediaType(),stored.getByteSize(),storage.get(stored.getStorageObjectKey()));}

    @Override
    public CompletionCaseResponse submit(String agency,String id,CaseTransition input,String actor,PermissionDataScope scope,String correlation){LdCompletionCase value=value(agency,id);staffAccess(agency,offering(agency,value.getOfferingId()),actor,scope);check(value.getVersion(),input.recordVersion());List<LdSessionAttendance> rows=attendance.findByAgencyIdAndCompletionCaseIdOrderByStartsAtAsc(agency,id);if(rows.isEmpty()||rows.stream().anyMatch(x->x.getStatus()==LdSessionAttendance.Status.UNRECORDED))throw new IllegalStateException("Every scheduled session requires an attendance record");List<LdCompletionOutput> required=outputs.findByAgencyIdAndCompletionCaseIdOrderByCreatedAtAsc(agency,id);if(required.stream().anyMatch(x->x.getStatus()!=LdCompletionOutput.Status.VERIFIED&&x.getStatus()!=LdCompletionOutput.Status.WAIVED))throw new IllegalStateException("Every required output must be verified or waived");CompletionCaseResponse before=response(agency,value);value.submit(actor,Instant.now());value=cases.saveAndFlush(value);return changed(agency,value,"SUBMIT_LD_COMPLETION_FOR_VERIFICATION",before,input.reason(),correlation);}

    @Override
    public CompletionCaseResponse returnForChanges(String agency,String id,CaseTransition input,String actor,PermissionDataScope scope,String correlation){if(scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("Completion verification requires Agency Wide scope");LdCompletionCase value=value(agency,id);check(value.getVersion(),input.recordVersion());CompletionCaseResponse before=response(agency,value);value.returnForChanges(requiredReason(input.reason()));value=cases.saveAndFlush(value);return changed(agency,value,"RETURN_LD_COMPLETION_FOR_CHANGES",before,input.reason(),correlation);}

    @Override
    public CompletionCaseResponse finalizeCase(String agency,String id,CompletionDecision input,String actor,PermissionDataScope scope,String correlation){if(scope!=PermissionDataScope.AGENCY_WIDE)throw new AccessDeniedException("Completion finalization requires Agency Wide scope");LdCompletionCase value=value(agency,id);check(value.getVersion(),input.recordVersion());List<LdSessionAttendance> rows=attendance.findByAgencyIdAndCompletionCaseIdOrderByStartsAtAsc(agency,id);BigDecimal credited=rows.stream().map(LdSessionAttendance::getCreditedHours).reduce(BigDecimal.ZERO,BigDecimal::add).setScale(2,RoundingMode.HALF_UP);List<LdCompletionEvidence> files=evidence.findByAgencyIdAndCompletionCaseIdOrderByUploadedAtAsc(agency,id);if(input.decision()==LdCompletionCase.Decision.COMPLETED&&files.isEmpty())throw new IllegalStateException("Verified completion requires secured certificate or supporting evidence");LocalDate lastSession=rows.stream().map(x->x.getEndsAt().toLocalDate()).max(LocalDate::compareTo).orElseThrow();LocalDate today=LocalDate.now(ZoneId.of("Asia/Manila"));if(input.completionDate().isBefore(lastSession)||input.completionDate().isAfter(today))throw new IllegalArgumentException("Completion date must be on or after the final session and cannot be in the future");CompletionCaseResponse before=response(agency,value);String fingerprint=completionFingerprint(value,rows,outputs.findByAgencyIdAndCompletionCaseIdOrderByCreatedAtAsc(agency,id),files,input);value.finalizeDecision(input.decision(),credited,input.completionDate(),input.reason(),actor,Instant.now(),fingerprint);value=cases.saveAndFlush(value);return changed(agency,value,"FINALIZE_LD_COMPLETION_"+input.decision(),before,input.reason(),correlation);}

    private boolean visible(String agency,LdCompletionCase value,String actor,PermissionDataScope scope){if(scope==PermissionDataScope.AGENCY_WIDE)return true;if(scope==PermissionDataScope.OWN_RECORDS)return value.getEmployeeNo().equalsIgnoreCase(actor);LdOffering offering=offering(agency,value.getOfferingId());LdProgram program=program(agency,offering.getProgramId());return program.getAccountableEmployeeNo().equalsIgnoreCase(actor);}
    private void access(String agency,LdCompletionCase value,String actor,PermissionDataScope scope){if(!visible(agency,value,actor,scope))throw new AccessDeniedException("Completion case is outside the permitted data scope");}
    private void staffAccess(String agency,LdOffering offering,String actor,PermissionDataScope scope){if(scope==PermissionDataScope.AGENCY_WIDE)return;if(scope==PermissionDataScope.ASSIGNED_RECORDS&&program(agency,offering.getProgramId()).getAccountableEmployeeNo().equalsIgnoreCase(actor))return;throw new AccessDeniedException("Completion delivery record is outside the permitted data scope");}
    private void own(LdCompletionCase value,String actor){if(!value.getEmployeeNo().equalsIgnoreCase(actor))throw new AccessDeniedException("Only the participant may access this completion record");}
    private void editable(LdCompletionCase value){if(value.getStatus()!=LdCompletionCase.Status.DRAFT)throw new IllegalStateException("Only a DRAFT completion case may be changed");}
    private LdCompletionCase value(String agency,String id){return cases.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("L&D completion case not found"));}
    private LdEnrollment enrollment(String agency,String id){return enrollments.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("L&D enrollment not found"));}
    private LdOffering offering(String agency,String id){return offerings.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("L&D offering not found"));}
    private LdProgram program(String agency,String id){return programs.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("L&D program not found"));}
    private LdCompletionOutput output(String agency,String caseId,String id){return outputs.findByIdAndAgencyId(id,agency).filter(x->x.getCompletionCaseId().equals(caseId)).orElseThrow(()->new ResourceNotFoundException("Required output not found"));}
    private List<LdOfferingSession> sessionRows(String agency,String offering){List<LdOfferingSession> rows=sessions.findByAgencyIdAndOfferingIdOrderByDisplayOrderAsc(agency,offering);if(rows.isEmpty())throw new IllegalStateException("Training Order offering has no sessions");return rows;}
    private CompletionCaseResponse changed(String agency,LdCompletionCase value,String action,CompletionCaseResponse before,String reason,String correlation){CompletionCaseResponse after=response(agency,value);record(agency,action,"LD_COMPLETION_CASE",value.getId(),value.getContentRevision(),value.getVersion(),before,after,reason,correlation);return after;}
    private CompletionCaseResponse response(String agency,LdCompletionCase x){return new CompletionCaseResponse(x.getId(),x.getVersion(),x.getEnrollmentId(),x.getTrainingOrderId(),x.getOfferingId(),x.getOfferingVersion(),x.getEmployeeId(),x.getEmployeeNo(),x.getEmployeeName(),x.getAppointmentId(),x.getPlannedHours(),x.getCreditedHours(),x.getStatus().name(),x.getContentRevision(),x.getSubmittedBy(),x.getSubmittedAt(),x.getVerifiedBy(),x.getVerifiedAt(),x.getCompletionDate(),x.getDecisionReason(),x.getCompletionFingerprint(),attendance.findByAgencyIdAndCompletionCaseIdOrderByStartsAtAsc(agency,x.getId()).stream().map(this::attendanceResponse).toList(),outputs.findByAgencyIdAndCompletionCaseIdOrderByCreatedAtAsc(agency,x.getId()).stream().map(this::outputResponse).toList(),evidence.findByAgencyIdAndCompletionCaseIdOrderByUploadedAtAsc(agency,x.getId()).stream().map(this::evidenceResponse).toList());}
    private AttendanceResponse attendanceResponse(LdSessionAttendance x){return new AttendanceResponse(x.getId(),x.getVersion(),x.getSessionId(),x.getSessionTitle(),x.getStartsAt(),x.getEndsAt(),x.getPlannedHours(),x.getStatus().name(),x.getAttendedHours(),x.getCreditedHours(),x.getRemarks(),x.getRecordedBy(),x.getRecordedAt());}
    private OutputResponse outputResponse(LdCompletionOutput x){return new OutputResponse(x.getId(),x.getVersion(),x.getCode(),x.getTitle(),x.getDescription(),x.getStatus().name(),x.getSubmittedBy(),x.getSubmittedAt(),x.getVerifiedBy(),x.getVerifiedAt(),x.getDecisionReason());}
    private EvidenceResponse evidenceResponse(LdCompletionEvidence x){return new EvidenceResponse(x.getId(),x.getVersion(),x.getOutputId(),x.getEvidenceType().name(),x.getOriginalFilename(),x.getMediaType(),x.getByteSize(),x.getChecksum(),x.getCertificateNumber(),x.getCertificateIssuer(),x.getCertificateIssueDate(),x.getUploadedBy(),x.getUploadedAt());}
    private String completionFingerprint(LdCompletionCase c,List<LdSessionAttendance> rows,List<LdCompletionOutput> out,List<LdCompletionEvidence> files,CompletionDecision decision){return sha256((c.getTrainingOrderFingerprint()+"|"+c.getEmployeeFingerprint()+"|"+decision.decision()+"|"+decision.completionDate()+"|"+rows.stream().map(x->x.getSessionId()+":"+x.getStatus()+":"+x.getCreditedHours()).toList()+"|"+out.stream().map(x->x.getId()+":"+x.getStatus()).toList()+"|"+files.stream().map(LdCompletionEvidence::getChecksum).sorted().toList()).getBytes(StandardCharsets.UTF_8));}
    private void record(String agency,String action,String type,String id,Integer business,long version,Object before,Object after,String reason,String correlation){audit.record(agency,action,type,id,business,version,before,after,reason,correlation);}
    private static void check(long actual,Long expected){if(expected==null||actual!=expected)throw new OptimisticConflictException("Record changed; reload and retry");}
    private static String requiredReason(String reason){if(reason==null||reason.isBlank())throw new IllegalArgumentException("reason is required");return reason.trim();}
    private static String safeFilename(String value){String name=value==null?"evidence":Path.of(value).getFileName().toString();return name.replaceAll("[^A-Za-z0-9._ -]","_");}
    private static String sha256(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}}
    private static void verifySignature(String media,byte[] b){boolean ok=switch(media){case"application/pdf"->b.length>4&&b[0]=='%'&&b[1]=='P'&&b[2]=='D'&&b[3]=='F';case"image/png"->b.length>8&&(b[0]&255)==137&&b[1]=='P'&&b[2]=='N'&&b[3]=='G';case"image/jpeg"->b.length>3&&(b[0]&255)==255&&(b[1]&255)==216&&(b[2]&255)==255;default->false;};if(!ok)throw new IllegalArgumentException("Evidence content does not match its media type");}
}
