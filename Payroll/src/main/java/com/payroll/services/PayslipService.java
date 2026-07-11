package com.payroll.services;

import com.payroll.dtos.PayslipDTO;

import java.io.OutputStream;
import java.util.List;

public interface PayslipService {

    PayslipDTO getPayslip(String employeeNo, String salaryPeriodKey, boolean releasedOnly);

    List<String> getSalaryPeriods(String employeeNo);

    void generatePayslipPdf(String employeeNo, String salaryPeriodKey, boolean releasedOnly, OutputStream outputStream) throws Exception;
}
