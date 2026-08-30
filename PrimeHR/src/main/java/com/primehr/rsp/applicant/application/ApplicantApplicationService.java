package com.primehr.rsp.applicant.application;

import com.primehr.rsp.applicant.api.ApplicationDtos.*;
import com.primehr.shared.api.PageResponse;

import java.util.List;

public interface ApplicantApplicationService {
    PageResponse<Application> applicantApplications(String applicantId, int page, int size);
    Application applicantApplication(String applicantId, String applicationId);
    Application create(String applicantId, Create request, String correlationId);
    Application save(String applicantId, String applicationId, Save request, String correlationId);
    Application submit(String applicantId, String applicationId, Submit request, String correlationId);
    Application withdraw(String applicantId, String applicationId, Withdraw request, String correlationId);
    List<Communication> applicantCommunications(String applicantId, String applicationId);

    PageResponse<Application> staffApplications(String agencyId, int page, int size);
    Application staffApplication(String agencyId, String applicationId);
    List<Communication> staffCommunications(String agencyId, String applicationId);
    Communication sendStaffMessage(String agencyId, String applicationId, StaffMessage request,
                                   String actor, String correlationId);
    ApplicantFoundationService.DocumentContent staffDocument(String agencyId, String applicationId,
                                                              String evidenceId, String actor);
}
