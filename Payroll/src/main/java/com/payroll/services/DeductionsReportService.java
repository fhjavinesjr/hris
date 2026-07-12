package com.payroll.services;

import java.io.OutputStream;

public interface DeductionsReportService {

    void generateDeductionsReportPdf(String salaryPeriodKey,
                                     String deductionTypeId,
                                     String deductionTypeName,
                                     String deductionTypeCode,
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
