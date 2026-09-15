package com.humanresource.impl;

import com.humanresource.dtos.EmployeeAppointmentDTO;
import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import com.humanresource.services.EmployeeAppointmentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            if (employeeAppointmentDTO.getAppointmentIssuedDate()
                    .isAfter(employeeAppointmentDTO.getAssumptionToDutyDate())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Appointment issued date cannot be after assumption to duty date");
            }

            if (Boolean.TRUE.equals(employeeAppointmentDTO.getActiveAppointment())) {
                List<EmployeeAppointment> plantillaOccupants = employeeAppointmentRepository
                        .findActiveByPlantillaForUpdate(employeeAppointmentDTO.getPlantillaId());
                boolean occupiedByAnotherEmployee = plantillaOccupants.stream()
                        .anyMatch(appointment -> !employeeAppointmentDTO.getEmployeeId()
                                .equals(appointment.getEmployeeId()));
                if (occupiedByAnotherEmployee) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Plantilla is already occupied");
                }

                List<EmployeeAppointment> activeAppointments = employeeAppointmentRepository
                        .findActiveByEmployeeForUpdate(employeeAppointmentDTO.getEmployeeId());
                for (EmployeeAppointment activeAppointment : activeAppointments) {
                    if (!employeeAppointmentDTO.getAssumptionToDutyDate()
                            .isAfter(activeAppointment.getAssumptionToDutyDate())) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "New appointment must be later than the active appointment");
                    }
                    activeAppointment.setActiveAppointment(false);
                }

                // A flush is required here. Hibernate normally executes inserts before
                // updates, which would violate the filtered one-active-appointment index.
                employeeAppointmentRepository.saveAllAndFlush(activeAppointments);
            }

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

            employeeAppointment = employeeAppointmentRepository.saveAndFlush(employeeAppointment);
            employeeAppointmentDTO.setEmployeeAppointmentId(employeeAppointment.getEmployeeAppointmentId());

            return employeeAppointmentDTO;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (DataIntegrityViolationException exception) {
            log.warn("Appointment creation conflicted with an existing record for employee {}",
                    employeeAppointmentDTO.getEmployeeId());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The employee or Plantilla already has an appointment with conflicting details",
                    exception);
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
            EmployeeAppointment employeeAppointment = employeeAppointmentRepository
                    .findByIdForUpdate(employeeAppointmentId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Employee appointment was not found"));

            if (!employeeAppointment.getEmployeeId().equals(employeeAppointmentDTO.getEmployeeId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "An appointment cannot be moved to another employee");
            }

            if (employeeAppointmentDTO.getAppointmentIssuedDate()
                    .isAfter(employeeAppointmentDTO.getAssumptionToDutyDate())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Appointment issued date cannot be after assumption to duty date");
            }

            if (Boolean.TRUE.equals(employeeAppointmentDTO.getActiveAppointment())) {
                boolean plantillaOccupied = employeeAppointmentRepository
                        .findActiveByPlantillaForUpdate(employeeAppointmentDTO.getPlantillaId())
                        .stream()
                        .anyMatch(appointment -> !employeeAppointmentId
                                .equals(appointment.getEmployeeAppointmentId()));
                if (plantillaOccupied) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Plantilla is already occupied");
                }

                boolean anotherActiveAppointment = employeeAppointmentRepository
                        .findActiveByEmployeeForUpdate(employeeAppointmentDTO.getEmployeeId())
                        .stream()
                        .anyMatch(appointment -> !employeeAppointmentId
                                .equals(appointment.getEmployeeAppointmentId()));
                if (anotherActiveAppointment) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "The employee already has another active appointment");
                }

                boolean notLatestAppointment = employeeAppointmentRepository
                        .findByEmployeeId(employeeAppointmentDTO.getEmployeeId())
                        .stream()
                        .filter(appointment -> !employeeAppointmentId
                                .equals(appointment.getEmployeeAppointmentId()))
                        .anyMatch(appointment -> !employeeAppointmentDTO.getAssumptionToDutyDate()
                                .isAfter(appointment.getAssumptionToDutyDate()));
                if (notLatestAppointment) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "The active appointment must be later than the employee's service history");
                }
            }

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

            employeeAppointmentRepository.saveAndFlush(employeeAppointment);
            employeeAppointmentDTO.setEmployeeAppointmentId(employeeAppointmentId);
            return employeeAppointmentDTO;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (DataIntegrityViolationException exception) {
            log.warn("Appointment update conflicted with an existing record for employee {}",
                    employeeAppointmentDTO.getEmployeeId());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The employee or Plantilla already has an appointment with conflicting details",
                    exception);
        }
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

    @Override
    public boolean isPlantillaTaken(Long plantillaId) throws Exception {
        return employeeAppointmentRepository.existsByPlantillaIdAndActiveAppointmentTrue(plantillaId);
    }

    @Transactional
    @Override
    public Boolean deactivateAppointment(Long employeeAppointmentId) throws Exception {
        try {
            EmployeeAppointment appointment = employeeAppointmentRepository.findById(employeeAppointmentId)
                    .orElseThrow(() -> new RuntimeException("EmployeeAppointment not found with id: " + employeeAppointmentId));
            appointment.setActiveAppointment(false);
            employeeAppointmentRepository.save(appointment);
            return true;
        } catch (Exception e) {
            log.error("Error deactivating EmployeeAppointment {}: {}", employeeAppointmentId, e.getMessage());
            return false;
        }
    }
}
