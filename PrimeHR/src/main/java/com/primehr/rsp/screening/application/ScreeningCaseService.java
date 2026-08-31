package com.primehr.rsp.screening.application;
import com.primehr.rsp.screening.api.ScreeningCaseDtos.*; import com.primehr.shared.api.PageResponse;
public interface ScreeningCaseService {
 PageResponse<CaseResponse> list(String agency,String actor,boolean administrator,int page,int size);
 CaseResponse get(String agency,String id,String actor,boolean administrator);
 CaseResponse open(String agency,String applicationId,OpenCase request,String actor,String correlationId);
 CaseResponse successor(String agency,String id,Successor request,String actor,String correlationId);
 CaseResponse assignments(String agency,String id,Assignments request,String actor,String correlationId);
 CaseResponse finding(String agency,String id,String criterionId,SaveFinding request,String actor,String correlationId);
 CaseResponse submit(String agency,String id,Submit request,String actor,String correlationId);
 CaseResponse returnCase(String agency,String id,Transition request,String actor,String correlationId);
 CaseResponse finalizeCase(String agency,String id,Finalize request,String actor,String correlationId);
 CaseResponse overrideCase(String agency,String id,AdminOverride request,String actor,String correlationId);
 PageResponse<CaseResponse> history(String agency,String id,String actor,boolean administrator,int page,int size);
}
