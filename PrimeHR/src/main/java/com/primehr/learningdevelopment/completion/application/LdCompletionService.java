package com.primehr.learningdevelopment.completion.application;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.learningdevelopment.completion.api.LdCompletionDtos.*;
import com.primehr.learningdevelopment.completion.domain.LdCompletionEvidence;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface LdCompletionService {
    List<CompletionCaseResponse> cases(String agency,String actor,PermissionDataScope scope);
    List<CompletionCaseResponse> mine(String agency,String actor);
    CompletionCaseResponse get(String agency,String id,String actor,PermissionDataScope scope);
    CompletionCaseResponse getMine(String agency,String id,String actor);
    CompletionCaseResponse open(String agency,String enrollmentId,OpenCaseInput input,String actor,PermissionDataScope scope,String correlation);
    CompletionCaseResponse recordAttendance(String agency,String id,String sessionId,AttendanceInput input,String actor,PermissionDataScope scope,String correlation);
    CompletionCaseResponse addOutput(String agency,String id,OutputInput input,String actor,PermissionDataScope scope,String correlation);
    CompletionCaseResponse decideOutput(String agency,String id,String outputId,OutputDecision input,boolean waive,String actor,PermissionDataScope scope,String correlation);
    EvidenceResponse upload(String agency,String id,LdCompletionEvidence.Type type,String outputId,String certificateNumber,String certificateIssuer,LocalDate certificateIssueDate,MultipartFile file,String actor,PermissionDataScope scope,boolean own,String correlation);
    EvidenceContent download(String agency,String id,String evidenceId,String actor,PermissionDataScope scope,boolean own,String correlation);
    CompletionCaseResponse submit(String agency,String id,CaseTransition input,String actor,PermissionDataScope scope,String correlation);
    CompletionCaseResponse returnForChanges(String agency,String id,CaseTransition input,String actor,PermissionDataScope scope,String correlation);
    CompletionCaseResponse finalizeCase(String agency,String id,CompletionDecision input,String actor,PermissionDataScope scope,String correlation);
}
