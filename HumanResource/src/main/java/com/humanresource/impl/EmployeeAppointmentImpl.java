package com.humanresource.impl;

import com.humanresource.dtos.EmployeeAppointmentDTO;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.services.EmployeeAppointmentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeAppointmentImpl implements EmployeeAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeAppointmentImpl.class);
    private final EmployeeAppointmentRepository employeeAppointmentRepository;

    public EmployeeAppointmentImpl(EmployeeAppointmentRepository employeeAppointmentRepository) {
        this.employeeAppointmentRepository = employeeAppointmentRepository;
    }

    @Transactional
    @Override
    public EmployeeAppointmentDTO createEmployeeAppointment(EmployeeAppointmentDTO employeeAppointmentDTO) throws Exception {
        try {
            if(employeeAppointmentDTO.getEmployeeAppointmentId() != null
                    && employeeAppointmentDTO.getEmployeeAppointmentId() > 0) {
                //Modify previous active to inactive
                EmployeeAppointment employeeAppointmentModifyActive = employeeAppointmentRepository.findTop1ByEmployeeIdOrderByAssumptionToDutyDateDesc(employeeAppointmentDTO.getEmployeeId());
                employeeAppointmentModifyActive.setActiveAppointment(employeeAppointmentDTO.getActiveAppointment());
                employeeAppointmentRepository.save(employeeAppointmentModifyActive);
            }

            //Add new employee appointment with active true
            EmployeeAppointment employeeAppointment = new EmployeeAppointment(employeeAppointmentDTO.getEmployeeId()
                    ,employeeAppointmentDTO.getAppointmentIssuedDate()
                    ,employeeAppointmentDTO.getAssumptionToDutyDate()
                    ,employeeAppointmentDTO.getNatureOfAppointmentId()
                    ,employeeAppointmentDTO.getPlantillaId()
                    ,employeeAppointmentDTO.getJobPositionId()
                    ,employeeAppointmentDTO.getSalaryGrade()
                    ,employeeAppointmentDTO.getSalaryStep()
                    ,employeeAppointmentDTO.getSalaryPerAnnum()
                    ,employeeAppointmentDTO.getSalaryPerMonth()
                    ,employeeAppointmentDTO.getSalaryPerDay()
                    ,employeeAppointmentDTO.getDetails()
                    ,employeeAppointmentDTO.getActiveAppointment());

            employeeAppointmentRepository.save(employeeAppointment);

            return employeeAppointmentDTO;
        } catch(Exception e) {
            log.error("Error in creating EmployeeAppointment: ", e);
            return null;
        }
    }

    @Override
    public List<EmployeeAppointmentDTO> getAllEmployeeAppointment() throws Exception {
        List<EmployeeAppointment> employeeAppointmentList = employeeAppointmentRepository.findAll();
        List<EmployeeAppointmentDTO> employeeAppointmentDTOList = new ArrayList<>();

        for(EmployeeAppointment employeeAppointment : employeeAppointmentList) {
            EmployeeAppointmentDTO employeeAppointmentDTO = new EmployeeAppointmentDTO();
            employeeAppointmentDTO.setEmployeeAppointmentId(employeeAppointment.getEmployeeAppointmentId());
            employeeAppointmentDTO.setEmployeeId(employeeAppointment.getEmployeeId());
            employeeAppointmentDTO.setAppointmentIssuedDate(employeeAppointment.getAppointmentIssuedDate());
            employeeAppointmentDTO.setAssumptionToDutyDate(employeeAppointment.getAssumptionToDutyDate());
            employeeAppointmentDTO.setNatureOfAppointmentId(employeeAppointment.getNatureOfAppointmentId());
            employeeAppointmentDTO.setPlantillaId(employeeAppointment.getPlantillaId());
            employeeAppointmentDTO.setJobPositionId(employeeAppointment.getJobPositionId());
            employeeAppointmentDTO.setSalaryGrade(employeeAppointment.getSalaryGrade());
            employeeAppointmentDTO.setSalaryStep(employeeAppointment.getSalaryStep());
            employeeAppointmentDTO.setSalaryPerAnnum(employeeAppointment.getSalaryPerAnnum());
            employeeAppointmentDTO.setSalaryPerMonth(employeeAppointment.getSalaryPerMonth());
            employeeAppointmentDTO.setSalaryPerDay(employeeAppointment.getSalaryPerDay());
            employeeAppointmentDTO.setDetails(employeeAppointment.getDetails());
            employeeAppointmentDTO.setActiveAppointment(employeeAppointment.getActiveAppointment());

            employeeAppointmentDTOList.add(employeeAppointmentDTO);
        }

        return employeeAppointmentDTOList;
    }

    @Override
    public List<EmployeeAppointmentDTO> getAllEmployeeAppointmentByEmployeeId(Long employeeId) throws Exception {
        List<EmployeeAppointment> employeeAppointmentList = employeeAppointmentRepository.findByEmployeeId(employeeId);
        List<EmployeeAppointmentDTO> employeeAppointmentDTOList = new ArrayList<>();

        for(EmployeeAppointment employeeAppointment : employeeAppointmentList) {
            EmployeeAppointmentDTO employeeAppointmentDTO = new EmployeeAppointmentDTO();
            employeeAppointmentDTO.setEmployeeAppointmentId(employeeAppointment.getEmployeeAppointmentId());
            employeeAppointmentDTO.setEmployeeId(employeeAppointment.getEmployeeId());
            employeeAppointmentDTO.setAppointmentIssuedDate(employeeAppointment.getAppointmentIssuedDate());
            employeeAppointmentDTO.setAssumptionToDutyDate(employeeAppointment.getAssumptionToDutyDate());
            employeeAppointmentDTO.setNatureOfAppointmentId(employeeAppointment.getNatureOfAppointmentId());
            employeeAppointmentDTO.setPlantillaId(employeeAppointment.getPlantillaId());
            employeeAppointmentDTO.setJobPositionId(employeeAppointment.getJobPositionId());
            employeeAppointmentDTO.setSalaryGrade(employeeAppointment.getSalaryGrade());
            employeeAppointmentDTO.setSalaryStep(employeeAppointment.getSalaryStep());
            employeeAppointmentDTO.setSalaryPerAnnum(employeeAppointment.getSalaryPerAnnum());
            employeeAppointmentDTO.setSalaryPerMonth(employeeAppointment.getSalaryPerMonth());
            employeeAppointmentDTO.setSalaryPerDay(employeeAppointment.getSalaryPerDay());
            employeeAppointmentDTO.setDetails(employeeAppointment.getDetails());
            employeeAppointmentDTO.setActiveAppointment(employeeAppointment.getActiveAppointment());

            employeeAppointmentDTOList.add(employeeAppointmentDTO);
        }

        return employeeAppointmentDTOList;
    }

    @Override
    public EmployeeAppointmentDTO getLatestEmployeeAppointmentByEmployeeId(Long employeeId) throws Exception {
        EmployeeAppointment employeeAppointment = employeeAppointmentRepository.findTop1ByEmployeeIdOrderByAssumptionToDutyDateDesc(employeeId);
        EmployeeAppointmentDTO employeeAppointmentDTO = new EmployeeAppointmentDTO();
        if(employeeAppointment != null) {
            employeeAppointmentDTO.setEmployeeId(employeeAppointment.getEmployeeId());
            employeeAppointmentDTO.setAppointmentIssuedDate(employeeAppointment.getAppointmentIssuedDate());
            employeeAppointmentDTO.setAssumptionToDutyDate(employeeAppointment.getAssumptionToDutyDate());
            employeeAppointmentDTO.setNatureOfAppointmentId(employeeAppointment.getNatureOfAppointmentId());
            employeeAppointmentDTO.setPlantillaId(employeeAppointment.getPlantillaId());
            employeeAppointmentDTO.setJobPositionId(employeeAppointment.getJobPositionId());
            employeeAppointmentDTO.setSalaryGrade(employeeAppointment.getSalaryGrade());
            employeeAppointmentDTO.setSalaryStep(employeeAppointment.getSalaryStep());
            employeeAppointmentDTO.setSalaryPerAnnum(employeeAppointment.getSalaryPerAnnum());
            employeeAppointmentDTO.setSalaryPerMonth(employeeAppointment.getSalaryPerMonth());
            employeeAppointmentDTO.setSalaryPerDay(employeeAppointment.getSalaryPerDay());
            employeeAppointmentDTO.setDetails(employeeAppointment.getDetails());
            employeeAppointmentDTO.setActiveAppointment(employeeAppointment.getActiveAppointment());

            return employeeAppointmentDTO;
        }

        return null;
    }

    @Override
    public EmployeeAppointmentDTO getEmployeeAppointmentById(Long employeeAppointmentId) throws Exception {
        return null;
    }

    @Transactional
    @Override
    public EmployeeAppointmentDTO updateEmployeeAppointment(Long employeeAppointmentId, EmployeeAppointmentDTO employeeAppointmentDTO) throws Exception {
        try {
            EmployeeAppointment employeeAppointment = employeeAppointmentRepository.findById(employeeAppointmentId).orElseThrow(() -> new RuntimeException("EmployeeAppointment not found"));
            if(employeeAppointment != null) {
                employeeAppointment.setEmployeeId(employeeAppointmentDTO.getEmployeeId());
                employeeAppointment.setAppointmentIssuedDate(employeeAppointmentDTO.getAppointmentIssuedDate());
                employeeAppointment.setAssumptionToDutyDate(employeeAppointmentDTO.getAssumptionToDutyDate());
                employeeAppointment.setNatureOfAppointmentId(employeeAppointmentDTO.getNatureOfAppointmentId());
                employeeAppointment.setPlantillaId(employeeAppointmentDTO.getPlantillaId());
                employeeAppointment.setJobPositionId(employeeAppointmentDTO.getJobPositionId());
                employeeAppointment.setSalaryGrade(employeeAppointmentDTO.getSalaryGrade());
                employeeAppointment.setSalaryStep(employeeAppointmentDTO.getSalaryStep());
                employeeAppointment.setSalaryPerAnnum(employeeAppointmentDTO.getSalaryPerAnnum());
                employeeAppointment.setSalaryPerMonth(employeeAppointmentDTO.getSalaryPerMonth());
                employeeAppointment.setSalaryPerDay(employeeAppointmentDTO.getSalaryPerDay());
                employeeAppointment.setDetails(employeeAppointmentDTO.getDetails());
                employeeAppointment.setActiveAppointment(employeeAppointmentDTO.getActiveAppointment());

                employeeAppointmentRepository.save(employeeAppointment);

                return employeeAppointmentDTO;
            }
        } catch(Exception e) {
            log.error("Error failed fetching EmployeeAppointment: {}", e.getMessage());
        }

        return null;
    }

    @Transactional
    @Override
    public Boolean deleteEmployeeAppointment(Long employeeAppointmentId) throws Exception {
        try {
            employeeAppointmentRepository.deleteById(employeeAppointmentId);

            return true;
        } catch(Exception e) {
            log.error("Error failed delete EmployeeAppointment: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public List<EmployeeAppointmentDTO> getByJobPositionId(Long jobPositionId) throws Exception {
        List<EmployeeAppointment> employeeAppointmentList = employeeAppointmentRepository.findByJobPositionId(jobPositionId);

        List<EmployeeAppointmentDTO> employeeAppointmentDTOList = new ArrayList<>();

        for(EmployeeAppointment employeeAppointment : employeeAppointmentList) {
            EmployeeAppointmentDTO employeeAppointmentDTO = new EmployeeAppointmentDTO();
            employeeAppointmentDTO.setEmployeeAppointmentId(employeeAppointment.getEmployeeAppointmentId());
            employeeAppointmentDTO.setEmployeeId(employeeAppointment.getEmployeeId());
            employeeAppointmentDTO.setAppointmentIssuedDate(employeeAppointment.getAppointmentIssuedDate());
            employeeAppointmentDTO.setAssumptionToDutyDate(employeeAppointment.getAssumptionToDutyDate());
            employeeAppointmentDTO.setNatureOfAppointmentId(employeeAppointment.getNatureOfAppointmentId());
            employeeAppointmentDTO.setPlantillaId(employeeAppointment.getPlantillaId());
            employeeAppointmentDTO.setJobPositionId(employeeAppointment.getJobPositionId());
            employeeAppointmentDTO.setSalaryGrade(employeeAppointment.getSalaryGrade());
            employeeAppointmentDTO.setSalaryStep(employeeAppointment.getSalaryStep());
            employeeAppointmentDTO.setSalaryPerAnnum(employeeAppointment.getSalaryPerAnnum());
            employeeAppointmentDTO.setSalaryPerMonth(employeeAppointment.getSalaryPerMonth());
            employeeAppointmentDTO.setSalaryPerDay(employeeAppointment.getSalaryPerDay());
            employeeAppointmentDTO.setDetails(employeeAppointment.getDetails());
            employeeAppointmentDTO.setActiveAppointment(employeeAppointment.getActiveAppointment());

            employeeAppointmentDTOList.add(employeeAppointmentDTO);
        }

        return employeeAppointmentDTOList;
    }
}