package com.payroll.services;

import java.io.OutputStream;

public interface GeneralPayrollReportService {
    void generateGeneralPayrollPdf(String salaryPeriodKey,
                                   String currentCompany,
                                   String preparedBy,
                                   String approvedBy,
                                   String cashierBy,
                                   OutputStream out) throws Exception;
}
