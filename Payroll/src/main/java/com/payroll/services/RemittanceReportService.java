package com.payroll.services;

import java.io.OutputStream;

public interface RemittanceReportService {

    void generateGsisRemittancePdf(String salaryPeriodKey,
                                   String salaryDate,
                                   String currentCompany,
                                   String officeCode,
                                   String preparedBy,
                                   String certifiedBy,
                                   String preparedByEmployeeNo,
                                   String certifiedByEmployeeNo,
                                   OutputStream out) throws Exception;

    void generatePagibigRemittancePdf(String salaryPeriodKey,
                                      String periodCovered,
                                      String currentCompany,
                                      String employerOfficeId,
                                      String companyAddress,
                                      String employerPagIbigNo,
                                      String certifiedBy,
                                      String certifiedByEmployeeNo,
                                      OutputStream out) throws Exception;

    void generatePhilhealthRemittancePdf(String salaryPeriodKey,
                                         String salaryDate,
                                         String currentCompany,
                                         String companyPhilhealthNo,
                                         String preparedBy,
                                         String preparedByPos,
                                         String certifiedBy,
                                         String certifiedByPos,
                                         OutputStream out) throws Exception;
}
