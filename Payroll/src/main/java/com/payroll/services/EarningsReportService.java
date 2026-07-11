package com.payroll.services;

import java.io.OutputStream;

public interface EarningsReportService {
    void generateEarningsReportPdf(String salaryPeriodKey,
                                   String category,
                                   String earningTypeId,
                                   String earningTypeName,
                                   String earningTypeCode,
                                   Boolean hazardPay,
                                   String currentCompany,
                                   String reportPeriodLabel,
                                   OutputStream out) throws Exception;
}
