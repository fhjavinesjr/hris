package com.primehr.rsp.report;

import com.primehr.rsp.report.RspProcessReportData.*;

public interface RspProcessReportService {
    RegisterPage register(String agency, RegisterQuery query, String actor, String authorization, String correlationId);
    byte[] registerPdf(String agency, RegisterQuery query, String actor, String authorization, String correlationId);
    Analytics analytics(String agency, RegisterQuery query, String actor, String authorization, String correlationId);
    byte[] analyticsPdf(String agency, RegisterQuery query, String actor, String authorization, String correlationId);
}
