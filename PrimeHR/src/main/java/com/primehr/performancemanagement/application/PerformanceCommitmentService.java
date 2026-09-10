package com.primehr.performancemanagement.application;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.performancemanagement.api.CommitmentFilter;
import com.primehr.performancemanagement.api.PerformanceCommitmentDtos.*;
import com.primehr.performancemanagement.api.PerformanceCommitmentWorkflowDtos.AmendmentCommand;
import java.util.List;
public interface PerformanceCommitmentService {
 String formTypeForAssignment(String agency,String id); String formTypeForVersion(String agency,String id);
 List<CommitmentResponse> list(String agency,String actor,PermissionDataScope scope,CommitmentFilter filter);
 CommitmentResponse get(String agency,String id,String actor,PermissionDataScope scope);
 CommitmentResponse generate(String agency,String assignment,String key,String actor,String correlation);
 CommitmentResponse generate(String agency,String assignment,String key,String actor,PermissionDataScope scope,String correlation);
 CommitmentResponse replaceTargets(String agency,String id,TargetReplacement input,String token,String actor,PermissionDataScope scope,String correlation);
 CommitmentResponse replaceCascades(String agency,String id,CascadeReplacement input,String actor,PermissionDataScope scope,String correlation);
 CommitmentResponse amend(String agency,String id,AmendmentCommand input,String actor,PermissionDataScope scope,String correlation);
 Readiness readiness(String agency,String id,String token,String actor,PermissionDataScope scope);
}
