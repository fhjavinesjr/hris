package com.payroll.dtos;

import java.util.List;

/** Body for POST /api/payroll-computation/employee-config/bulk-save */
public class PayrollEmployeeConfigSaveRequest {

    private String salaryPeriodKey;
    private List<PayrollEmployeeConfigDTO> configs;

    public String getSalaryPeriodKey() { return salaryPeriodKey; }
    public void setSalaryPeriodKey(String salaryPeriodKey) { this.salaryPeriodKey = salaryPeriodKey; }
    public List<PayrollEmployeeConfigDTO> getConfigs() { return configs; }
    public void setConfigs(List<PayrollEmployeeConfigDTO> configs) { this.configs = configs; }
}
