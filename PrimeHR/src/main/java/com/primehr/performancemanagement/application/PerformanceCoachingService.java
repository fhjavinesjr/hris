package com.primehr.performancemanagement.application;
import com.primehr.integration.administrative.PermissionDataScope;import com.primehr.performancemanagement.api.PerformanceCoachingDtos.*;import java.util.*;
public interface PerformanceCoachingService{
 List<SessionResponse>sessions(String agency,String caseId,String actor,PermissionDataScope scope);
 SessionResponse createSession(String agency,String caseId,SessionInput input,String actor,PermissionDataScope scope,String correlation);
 SessionResponse editSession(String agency,String id,SessionEdit input,String actor,PermissionDataScope scope,String correlation);
 SessionResponse transitionSession(String agency,String id,SessionAction input,String actor,PermissionDataScope scope,String action,String correlation);
 SessionResponse correctSession(String agency,String id,SessionInput input,String actor,PermissionDataScope scope,String correlation);
 ActionItemResponse addActionItem(String agency,String sessionId,ActionItemInput input,String actor,PermissionDataScope scope,String correlation);
 ActionItemResponse updateActionItem(String agency,String id,ActionItemEdit input,String actor,PermissionDataScope scope,String correlation);
 ActionItemResponse decideActionItem(String agency,String id,SessionAction input,String actor,PermissionDataScope scope,boolean verify,String correlation);
 ReviewResponse getReview(String agency,String caseId,String actor,PermissionDataScope scope);
 ReviewResponse createReview(String agency,String caseId,ReviewInput input,String actor,PermissionDataScope scope,String correlation);
 ReviewResponse editReview(String agency,String id,ReviewEdit input,String actor,PermissionDataScope scope,String correlation);
 ReviewResponse transitionReview(String agency,String id,ReviewAction input,String actor,PermissionDataScope scope,String action,String correlation);
}
