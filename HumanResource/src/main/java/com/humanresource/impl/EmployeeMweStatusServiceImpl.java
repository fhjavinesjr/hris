package com.humanresource.impl;

import com.humanresource.dtos.EmployeeMweStatusDTO;
import com.humanresource.entitymodels.Employee;
import com.humanresource.entitymodels.EmployeeMweStatus;
import com.humanresource.repositories.EmployeeMweStatusRepository;
import com.humanresource.repositories.EmployeeRepository;
import com.humanresource.services.EmployeeMweStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmployeeMweStatusServiceImpl implements EmployeeMweStatusService {
    private final EmployeeMweStatusRepository repository;
    private final EmployeeRepository employeeRepository;

    public EmployeeMweStatusServiceImpl(EmployeeMweStatusRepository repository,
                                        EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeMweStatusDTO> getAll(String employeeNo) {
        if (employeeNo != null && !employeeNo.isBlank()) {
            Employee employee = requireEmployee(employeeNo);
            return repository.findByEmployeeIdOrderByEffectiveFromDesc(employee.getEmployeeId()).stream()
                    .map(value -> toDto(value, employee.getEmployeeNo())).toList();
        }
        Map<Long, Employee> employees = employeeRepository.findAll().stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, Function.identity()));
        return repository.findAllByOrderByEmployeeIdAscEffectiveFromDesc().stream()
                .map(value -> toDto(value, employees.containsKey(value.getEmployeeId())
                        ? employees.get(value.getEmployeeId()).getEmployeeNo() : null))
                .toList();
    }

    @Override
    @Transactional
    public EmployeeMweStatusDTO create(EmployeeMweStatusDTO request, String actor) {
        Employee employee = requireRequestEmployee(request);
        EmployeeMweStatus target = new EmployeeMweStatus();
        target.setEmployeeId(employee.getEmployeeId());
        apply(target, request);
        validateNoOverlap(target, null);
        LocalDateTime now = LocalDateTime.now();
        target.setCreatedBy(actor); target.setCreatedAt(now);
        target.setUpdatedBy(actor); target.setUpdatedAt(now);
        return toDto(repository.save(target), employee.getEmployeeNo());
    }

    @Override
    @Transactional
    public EmployeeMweStatusDTO update(Long id, EmployeeMweStatusDTO request, String actor) {
        EmployeeMweStatus target = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee MWE status was not found"));
        Employee employee = employeeRepository.findById(target.getEmployeeId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.CONFLICT, "The linked employee no longer exists"));
        if (request.employeeNo() != null
                && !employee.getEmployeeNo().equalsIgnoreCase(request.employeeNo().trim())) {
            throw bad("Employee cannot be changed on an existing MWE status record");
        }
        apply(target, request);
        validateNoOverlap(target, id);
        target.setUpdatedBy(actor); target.setUpdatedAt(LocalDateTime.now());
        return toDto(repository.save(target), employee.getEmployeeNo());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Employee MWE status was not found");
        repository.deleteById(id);
    }

    private Employee requireRequestEmployee(EmployeeMweStatusDTO request) {
        if (request == null || request.employeeNo() == null || request.employeeNo().isBlank()) {
            throw bad("Employee number is required");
        }
        return requireEmployee(request.employeeNo());
    }

    private Employee requireEmployee(String employeeNo) {
        return employeeRepository.findByEmployeeNo(employeeNo.trim()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee was not found"));
    }

    private void apply(EmployeeMweStatus target, EmployeeMweStatusDTO request) {
        if (request == null) throw bad("Employee MWE status details are required");
        if (request.effectiveFrom() == null) throw bad("Effective-from date is required");
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw bad("Effective-to date cannot be before the effective-from date");
        }
        if (request.minimumWageEarner() == null) {
            throw bad("Select whether the employee is a minimum-wage earner");
        }
        String regionCode = clean(request.regionCode(), 30, "Region code");
        if (Boolean.TRUE.equals(request.minimumWageEarner()) && regionCode == null) {
            throw bad("Region code is required for a minimum-wage earner");
        }
        target.setEffectiveFrom(request.effectiveFrom());
        target.setEffectiveTo(request.effectiveTo());
        target.setMinimumWageEarner(request.minimumWageEarner());
        target.setRegionCode(regionCode == null ? null : regionCode.toUpperCase(Locale.ROOT));
        target.setRemarks(clean(request.remarks(), 500, "Remarks"));
    }

    private void validateNoOverlap(EmployeeMweStatus candidate, Long excludedId) {
        for (EmployeeMweStatus existing : repository
                .findByEmployeeIdOrderByEffectiveFromDesc(candidate.getEmployeeId())) {
            if (existing.getId().equals(excludedId)) continue;
            if (overlaps(candidate.getEffectiveFrom(), candidate.getEffectiveTo(),
                    existing.getEffectiveFrom(), existing.getEffectiveTo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "An MWE status already covers part of this employee date range");
            }
        }
    }

    private boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return (endB == null || !startA.isAfter(endB)) && (endA == null || !startB.isAfter(endA));
    }

    private String clean(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String clean = value.trim();
        if (clean.length() > max) throw bad(label + " cannot exceed " + max + " characters");
        return clean;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private EmployeeMweStatusDTO toDto(EmployeeMweStatus value, String employeeNo) {
        return new EmployeeMweStatusDTO(value.getId(), employeeNo, value.getEffectiveFrom(),
                value.getEffectiveTo(), value.getMinimumWageEarner(), value.getRegionCode(),
                value.getRemarks(), value.getCreatedBy(), value.getCreatedAt(),
                value.getUpdatedBy(), value.getUpdatedAt());
    }
}
