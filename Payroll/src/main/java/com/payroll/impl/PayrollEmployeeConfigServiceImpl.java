package com.payroll.impl;

import com.hris.common.config.SystemConfigRuntimeResolver;
import com.payroll.dtos.EmployeePayrollInfoDTO;
import com.payroll.dtos.PayrollEmployeeConfigDTO;
import com.payroll.entitymodels.PayrollEmployeeConfig;
import com.payroll.repositories.PayrollEmployeeConfigRepository;
import com.payroll.services.PayrollEmployeeConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollEmployeeConfigServiceImpl implements PayrollEmployeeConfigService {

    private static final Logger log = LoggerFactory.getLogger(PayrollEmployeeConfigServiceImpl.class);

    private final PayrollEmployeeConfigRepository configRepo;
    private final RestTemplate restTemplate;
    private final SystemConfigRuntimeResolver systemConfigResolver;

    @Value("${hris.services.hrmanagement.url:http://localhost:8085}")
    private String hrServiceUrl;

    public PayrollEmployeeConfigServiceImpl(PayrollEmployeeConfigRepository configRepo,
                                             RestTemplate restTemplate,
                                             SystemConfigRuntimeResolver systemConfigResolver) {
        this.configRepo = configRepo;
        this.restTemplate = restTemplate;
        this.systemConfigResolver = systemConfigResolver;
    }

    @Override
    public List<PayrollEmployeeConfigDTO> getConfigForSetup(String salaryPeriodKey,
                                                             String employeeGroup,
                                                             String authHeader) {
        // 1. Fetch all employees from HumanResource
        List<EmployeePayrollInfoDTO> hrEmployees = fetchEmployeesFromHr(authHeader);

        // 2. Filter by group (regular = not excluded from payroll, contractual = excluded)
        boolean wantContractual = "contractual".equalsIgnoreCase(employeeGroup);
        List<EmployeePayrollInfoDTO> filtered = hrEmployees.stream()
                .filter(e -> wantContractual
                        ? Boolean.TRUE.equals(e.getIsExcludedFromPayroll())
                        : !Boolean.TRUE.equals(e.getIsExcludedFromPayroll()))
                .collect(Collectors.toList());

        // 3. Load existing configs for this period
        Map<String, PayrollEmployeeConfig> existingConfig =
                configRepo.findBySalaryPeriodKey(salaryPeriodKey).stream()
                        .collect(Collectors.toMap(PayrollEmployeeConfig::getEmployeeNo, c -> c));

        // 4. Load previous-period defaults for new employees (hybrid persistence)
        Set<String> noConfigYet = filtered.stream()
                .map(EmployeePayrollInfoDTO::getEmployeeNo)
                .filter(no -> !existingConfig.containsKey(no))
                .collect(Collectors.toSet());

        Map<String, PayrollEmployeeConfig> previousDefaults = Collections.emptyMap();
        if (!noConfigYet.isEmpty()) {
            previousDefaults = configRepo.findLatestBeforePeriod(salaryPeriodKey).stream()
                    .filter(c -> noConfigYet.contains(c.getEmployeeNo()))
                    .collect(Collectors.toMap(PayrollEmployeeConfig::getEmployeeNo, c -> c,
                            (a, b) -> a)); // keep first in case of duplicates
        }

        // 5. Build response DTOs, merging HR data + saved/default config
        List<PayrollEmployeeConfigDTO> result = new ArrayList<>();
        for (EmployeePayrollInfoDTO emp : filtered) {
            PayrollEmployeeConfigDTO dto = new PayrollEmployeeConfigDTO();
            dto.setEmployeeNo(emp.getEmployeeNo());
            dto.setEmployeeName(emp.getFullName());
            dto.setDepartment(emp.getDepartment());
            dto.setSalaryGrade(emp.getSalaryGrade());
            dto.setSalaryStep(emp.getSalaryStep());
            dto.setSalaryPeriodKey(salaryPeriodKey);

            PayrollEmployeeConfig saved = existingConfig.get(emp.getEmployeeNo());
            if (saved != null) {
                dto.setIsExcludedFromPayroll(saved.getIsExcludedFromPayroll());
                dto.setNoHazardPay(saved.getNoHazardPay());
                dto.setDisplayToLastPage(saved.getDisplayToLastPage());
            } else {
                // Use previous period as default (hybrid persistence)
                PayrollEmployeeConfig prev = previousDefaults.get(emp.getEmployeeNo());
                if (prev != null) {
                    dto.setIsExcludedFromPayroll(prev.getIsExcludedFromPayroll());
                    dto.setNoHazardPay(prev.getNoHazardPay());
                    dto.setDisplayToLastPage(prev.getDisplayToLastPage());
                } else {
                    // First-time defaults: all false
                    dto.setIsExcludedFromPayroll(false);
                    dto.setNoHazardPay(false);
                    dto.setDisplayToLastPage(false);
                }
            }

            result.add(dto);
        }

        // Sort alphabetically by name
        result.sort(Comparator.comparing(d -> d.getEmployeeName() != null ? d.getEmployeeName() : ""));
        return result;
    }

    @Override
    @Transactional
    public void bulkSave(String salaryPeriodKey, List<PayrollEmployeeConfigDTO> configs) {
        if (configs == null || configs.isEmpty()) return;

        // Load existing records for this period for upsert
        Map<String, PayrollEmployeeConfig> existing =
                configRepo.findBySalaryPeriodKey(salaryPeriodKey).stream()
                        .collect(Collectors.toMap(PayrollEmployeeConfig::getEmployeeNo, c -> c));

        List<PayrollEmployeeConfig> toSave = new ArrayList<>();
        for (PayrollEmployeeConfigDTO dto : configs) {
            PayrollEmployeeConfig entity = existing.getOrDefault(
                    dto.getEmployeeNo(), new PayrollEmployeeConfig());

            entity.setEmployeeNo(dto.getEmployeeNo());
            entity.setEmployeeName(dto.getEmployeeName());
            entity.setDepartment(dto.getDepartment());
            entity.setSalaryPeriodKey(salaryPeriodKey);
            entity.setIsExcludedFromPayroll(Boolean.TRUE.equals(dto.getIsExcludedFromPayroll()));
            entity.setNoHazardPay(Boolean.TRUE.equals(dto.getNoHazardPay()));
            entity.setDisplayToLastPage(Boolean.TRUE.equals(dto.getDisplayToLastPage()));
            entity.setUpdatedAt(LocalDateTime.now());

            toSave.add(entity);
        }

        configRepo.saveAll(toSave);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private List<EmployeePayrollInfoDTO> fetchEmployeesFromHr(String authHeader) {
        String baseUrl = systemConfigResolver.resolveApiUrl(
                SystemConfigRuntimeResolver.API_HRM, hrServiceUrl);
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(authHeader)) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }
        try {
            ResponseEntity<List<EmployeePayrollInfoDTO>> resp = restTemplate.exchange(
                    baseUrl + "/api/employee/payroll-info/bulk",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<EmployeePayrollInfoDTO>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception ex) {
            log.error("PayrollEmployeeConfig: HRM employee lookup failed at {}: {}",
                    baseUrl, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Unable to load employees from the HR Management URL in SystemConfig",
                    ex);
        }
    }

}
