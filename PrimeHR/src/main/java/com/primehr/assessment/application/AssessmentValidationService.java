package com.primehr.assessment.application;
import com.primehr.assessment.api.AssessmentValidationDtos.*;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.shared.api.PageResponse;
import java.time.LocalDate;
public interface AssessmentValidationService {
 PageResponse<ValidationListItem> pending(String agencyId,int page,int size);
 ValidationCaseResponse get(String agencyId,String caseId);
 ValidationResultResponse validate(String agencyId,String caseId,ValidateCaseRequest request,String actor,boolean administrator,String correlationId);
 PersonProfileResponse latest(String agencyId,String employeeNo,LocalDate asOf,String actor,PermissionDataScope scope);
 PageResponse<PersonProfileResponse> history(String agencyId,String employeeNo,int page,int size,String actor,PermissionDataScope scope);
 PersonProfileResponse version(String agencyId,String profileId,String actor,PermissionDataScope scope);
}
