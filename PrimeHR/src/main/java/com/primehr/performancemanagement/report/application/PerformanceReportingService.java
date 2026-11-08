package com.primehr.performancemanagement.report.application;

import static com.primehr.performancemanagement.report.application.PerformanceReportingData.*;

public interface PerformanceReportingService {
    RegisterPage register(String agencyId, RegisterQuery query, String actor, String correlationId);
    Analytics analytics(String agencyId, RegisterQuery query, String actor, String correlationId);
    OutcomePage outcomes(String agencyId, OutcomeQuery query, String actor, String correlationId);
}
