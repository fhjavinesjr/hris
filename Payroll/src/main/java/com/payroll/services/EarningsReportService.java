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
                                   String preparedBy,
                                   String preparedByEmployeeNo,
                                   String certifiedBy,
                                   String certifiedByEmployeeNo,
                                   String approvedBy,
                                   String approvedByEmployeeNo,
                                   OutputStream out) throws Exception;
}
