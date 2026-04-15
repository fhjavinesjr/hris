package com.administrative.impl;

import com.administrative.dtos.EmployeeRequestDTO;
import com.administrative.entitymodels.EmployeeRequest;
import com.administrative.repositories.EmployeeRequestRepository;
import com.administrative.services.EmployeeRequestService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeRequestImpl implements EmployeeRequestService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeRequestImpl.class);
    private final EmployeeRequestRepository employeeRequestRepository;

    public EmployeeRequestImpl(EmployeeRequestRepository employeeRequestRepository) {
        this.employeeRequestRepository = employeeRequestRepository;
    }

    @Transactional
    @Override
    public EmployeeRequestDTO createEmployeeRequest(EmployeeRequestDTO employeeRequestDTO) throws Exception {
        try {
            EmployeeRequest employeeRequest = new EmployeeRequest(
                    employeeRequestDTO.getEmployeeRequestId(),
                    employeeRequestDTO.getCode(),
                    employeeRequestDTO.getName(),
                    employeeRequestDTO.getMax()
            );
            employeeRequestRepository.save(employeeRequest);

            return employeeRequestDTO;
        } catch (Exception e) {
            log.error("Error in creating EmployeeRequest: ", e);
            return null;
        }
    }

    @Override
    public List<EmployeeRequestDTO> getAllEmployeeRequests() throws Exception {
        List<EmployeeRequest> employeeRequestList = employeeRequestRepository.findAll();
        List<EmployeeRequestDTO> employeeRequestDTOList = new ArrayList<>();

        for (EmployeeRequest employeeRequest : employeeRequestList) {
            EmployeeRequestDTO dto = new EmployeeRequestDTO();
            dto.setEmployeeRequestId(employeeRequest.getEmployeeRequestId());
            dto.setCode(employeeRequest.getCode());
            dto.setName(employeeRequest.getName());
            dto.setMax(employeeRequest.getMax());

            employeeRequestDTOList.add(dto);
        }

        return employeeRequestDTOList;
    }

    @Override
    public EmployeeRequestDTO getEmployeeRequestById(Long employeeRequestId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public EmployeeRequestDTO updateEmployeeRequest(Long employeeRequestId, EmployeeRequestDTO employeeRequestDTO) throws Exception {
        try {
            EmployeeRequest employeeRequest = employeeRequestRepository.findById(employeeRequestId)
                    .orElseThrow(() -> new RuntimeException("EmployeeRequest not found"));

            employeeRequest.setCode(employeeRequestDTO.getCode());
            employeeRequest.setName(employeeRequestDTO.getName());
            employeeRequest.setMax(employeeRequestDTO.getMax());

            employeeRequestRepository.save(employeeRequest);

            return employeeRequestDTO;
        } catch (Exception e) {
            log.error("Error failed updating EmployeeRequest: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteEmployeeRequest(Long employeeRequestId) throws Exception {
        try {
            employeeRequestRepository.deleteById(employeeRequestId);

            return true;
        } catch (Exception e) {
            log.error("Error failed deleting EmployeeRequest: {}", e.getMessage());
        }

        return false;
    }
}
