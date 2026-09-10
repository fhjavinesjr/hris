package com.primehr.performancemanagement.application;

import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.performancemanagement.api.PerformanceCommitmentWorkflowDtos.*;

public interface PerformanceCommitmentWorkflowService {
    WorkflowResponse get(String agency,String id,String actor,PermissionDataScope scope);
    WorkflowResponse submit(String agency,String id,Command command,String token,String actor,PermissionDataScope scope,String correlation);
    WorkflowResponse withdraw(String agency,String id,Command command,String actor,PermissionDataScope scope,String correlation);
    WorkflowResponse recommend(String agency,String id,Command command,String token,String actor,String correlation);
    WorkflowResponse returnForChanges(String agency,String id,Command command,String token,String actor,String correlation);
    WorkflowResponse approve(String agency,String id,Command command,String token,String actor,String correlation);
    WorkflowResponse reject(String agency,String id,Command command,String token,String actor,String correlation);
    WorkflowResponse voidRecord(String agency,String id,Command command,String actor,String correlation);
    WorkflowResponse rebase(String agency,String id,Command command,String token,String actor,String correlation);
}
