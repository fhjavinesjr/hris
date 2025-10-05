package com.timekeeping.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.common.utilities.JwtUtil;
import com.hris.common.utilities.UseUtils;
import com.timekeeping.dtos.EmployeeDTO;
import com.timekeeping.entitymodels.Employee;
import com.timekeeping.repositories.EmployeeRepository;
import com.timekeeping.services.EmployeeService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    // List of fields that should NOT be updated
    // Blacklisted
    private static final List<String> NOT_ALLOWED_FIELDS = Arrays.asList("employeeId", "employeeNo", "employeePassword", "createdAt");

    private final EmployeeRepository employeeRepository;

    private final ObjectMapper objectMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, ObjectMapper objectMapper, @Qualifier("timekeepingPasswordEncoder") PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String loginEmployee(String employeeNo, String employeePassword) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
                .orElseThrow(() -> new RuntimeException("Employee No not found"));
        if (passwordEncoder.matches(employeePassword, employee.getEmployeePassword())) {
            return jwtUtil.generateToken(employee.getEmployeeNo(), employee.getRole());
        }

        throw new RuntimeException("Invalid credentials");
    }

    @Transactional
    @Override
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) throws Exception {
        Employee employee = new Employee(employeeDTO.getEmployeeNo(), employeeDTO.getEmployeePassword(),
                employeeDTO.getRole(), employeeDTO.getFirstname(), employeeDTO.getLastname(),
                employeeDTO.getSuffix(), employeeDTO.getEmail(), employeeDTO.getPosition(),
                employeeDTO.getShortJobDesc(), UseUtils.getLocalDateTimeNow(), UseUtils.getLocalDateTimeNow());

        //hashing algorithm the password
        employee.setEmployeePassword(passwordEncoder.encode(employee.getEmployeePassword()));

        employeeRepository.save(employee);

        employeeDTO.setEmployeePassword("");
        employeeDTO.setCreatedAt(UseUtils.getLocalDateTimeNow());
        employeeDTO.setUpdatedAt(UseUtils.getLocalDateTimeNow());

        return employeeDTO;
    }

    @Transactional
    @Override
    public EmployeeDTO updateEmployee(Long employeeId, Map<String, Object> updates) throws Exception {
        EmployeeDTO employeeDTO = getEmployeeById(employeeId);

        Employee existingEmployee = new Employee(employeeDTO.getEmployeeNo(), employeeDTO.getEmployeePassword(),
                employeeDTO.getRole(), employeeDTO.getFirstname(), employeeDTO.getLastname(),
                employeeDTO.getSuffix(), employeeDTO.getEmail(), employeeDTO.getPosition(),
                employeeDTO.getShortJobDesc(), employeeDTO.getCreatedAt(), employeeDTO.getUpdatedAt());
        existingEmployee.setEmployeeId(employeeId);

        // Remove disallowed fields before updating
        Map<String, Object> filteredUpdates = updates.entrySet().stream()
                .filter(entrySet -> !NOT_ALLOWED_FIELDS.contains(entrySet.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        objectMapper.updateValue(existingEmployee, filteredUpdates);

        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        return buildEmployeeDTO(updatedEmployee);
    }

    public EmployeeDTO getEmployeeById(Long employeeId) throws Exception {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new RuntimeException("Employee not found"));

        return buildEmployeeDTO(employee);
    }

    @Override
    public EmployeeDTO getEmployeeDisplayById(Long employeeId) throws Exception {
        EmployeeDTO employeeDTO = getEmployeeById(employeeId);

        EmployeeDTO employeeDisplay = null;
        if(employeeDTO != null) {
            employeeDisplay = new EmployeeDTO(employeeDTO.getEmployeeNo(), employeeDTO.getFirstname(),
                    employeeDTO.getLastname(), employeeDTO.getSuffix(), employeeDTO.getEmail(),
                    employeeDTO.getPosition(), employeeDTO.getShortJobDesc(), employeeDTO.getCreatedAt(),
                    employeeDTO.getRole(), employeeDTO.getUpdatedAt());
        }

        return employeeDisplay;
    }

    @Override
    public List<EmployeeDTO> getAllEmployeeNoAndName() throws Exception {
        List<Employee> employees = employeeRepository.findAll();

        List<EmployeeDTO> employeeDisplays = new ArrayList<>();
        for(Employee employee : employees) {
            EmployeeDTO employeeDisplay = new EmployeeDTO(employee.getEmployeeNo(), employee.getFirstname(),
                    employee.getLastname(), employee.getSuffix(), employee.getRole());

            employeeDisplays.add(employeeDisplay);
        }

        return employeeDisplays;
    }

    private EmployeeDTO buildEmployeeDTO(Employee employee) {
        return new EmployeeDTO(employee.getEmployeeNo(), employee.getEmployeePassword(), employee.getFirstname(),
                employee.getLastname(), employee.getSuffix(), employee.getEmail(),
                employee.getPosition(), employee.getShortJobDesc(),
                employee.getCreatedAt(), employee.getUpdatedAt(), employee.getRole());
    }

}