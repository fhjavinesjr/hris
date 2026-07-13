package com.payroll.services;

import java.io.OutputStream;

public interface GeneralPayrollReportService {
    void generateGeneralPayrollPdf(String salaryPeriodKey,
                                   String payrollGroup,
                                   String currentCompany,
                                   String preparedBy,
                                   String approvedBy,
                                   String cashierBy,
                                   String preparedByEmployeeNo,
                                   String approvedByEmployeeNo,
                                   String cashierByEmployeeNo,
                                   OutputStream out) throws Exception;
}
