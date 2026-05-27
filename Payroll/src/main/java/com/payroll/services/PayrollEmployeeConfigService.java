package com.payroll.services;

import com.payroll.dtos.PayrollEmployeeConfigDTO;

import java.util.List;

public interface PayrollEmployeeConfigService {

    /**
     * Returns the pre-setup employee config list for a given salary period and payroll group.
     * Employees are fetched from the HumanResource service and merged with any saved config.
     * For employees with no saved config for this period, the latest previous period's config
     * is used as the default (hybrid persistence).
     *
     * @param salaryPeriodKey  e.g. "2026-6-1"
     * @param employeeGroup    "regular" or "contractual"
     * @param authHeader       JWT Authorization header, forwarded to HumanResource service
     */
    List<PayrollEmployeeConfigDTO> getConfigForSetup(String salaryPeriodKey,
                                                      String employeeGroup,
                                                      String authHeader);

    /**
     * Saves (upsert) per-employee configs for a salary period.
     */
    void bulkSave(String salaryPeriodKey, List<PayrollEmployeeConfigDTO> configs);
}
