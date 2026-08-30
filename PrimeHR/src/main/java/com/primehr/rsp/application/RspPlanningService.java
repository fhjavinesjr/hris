package com.primehr.rsp.application;

import com.primehr.rsp.api.RspPlanningDtos.CreatePlan;
import com.primehr.rsp.api.RspPlanningDtos.PlanResponse;
import com.primehr.rsp.api.RspPlanningDtos.Readiness;
import com.primehr.rsp.api.RspPlanningDtos.SaveVacancy;
import com.primehr.rsp.api.RspPlanningDtos.Transition;
import com.primehr.rsp.api.RspPlanningDtos.UpdatePlan;
import com.primehr.shared.api.PageResponse;

import java.time.LocalDate;

public interface RspPlanningService {
    PageResponse<PlanResponse> list(String agencyId, int page, int size);
    PlanResponse get(String agencyId, String id);
    PlanResponse create(String agencyId, CreatePlan request, String correlationId);
    PlanResponse update(String agencyId, String id, UpdatePlan request, String correlationId);
    PlanResponse archive(String agencyId, String id, Transition request, String correlationId);
    PlanResponse submitPlan(String agencyId, String id, Transition request, String token, String correlationId);
    PlanResponse returnPlan(String agencyId, String id, Transition request, String correlationId);
    PlanResponse approvePlan(String agencyId, String id, Transition request, String token,
                             boolean administrator, String correlationId);
    PlanResponse addVacancy(String agencyId, String planId, SaveVacancy request,
                            String token, String correlationId);
    PlanResponse updateVacancy(String agencyId, String id, SaveVacancy request,
                               String token, String correlationId);
    PlanResponse archiveVacancy(String agencyId, String id, Transition request, String correlationId);
    PlanResponse submitVacancy(String agencyId, String id, Transition request,
                               String token, String correlationId);
    PlanResponse returnVacancy(String agencyId, String id, Transition request, String correlationId);
    PlanResponse authorizeVacancy(String agencyId, String id, Transition request, String token,
                                  boolean administrator, String correlationId);
    PlanResponse declineVacancy(String agencyId, String id, Transition request,
                                boolean administrator, String correlationId);
    PlanResponse cancelVacancy(String agencyId, String id, Transition request, String correlationId);
    Readiness vacancyReadiness(String agencyId, String id, String token);
    Readiness readiness(String agencyId, Long plantillaId, Long businessUnitId,
                        LocalDate asOf, String token);
}
