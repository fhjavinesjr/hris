package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.EmployeeAppointmentDTO;
import com.humanresource.services.EmployeeAppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeAppointmentController {

    private final EmployeeAppointmentService employeeAppointmentService;

    public EmployeeAppointmentController(EmployeeAppointmentService employeeAppointmentService) {
        this.employeeAppointmentService = employeeAppointmentService;
    }

    @PostMapping("/employeeAppointment/create")
    public ResponseEntity<MetadataResponse> createEmployeeAppointment(@RequestBody EmployeeAppointmentDTO employeeAppointmentDTO) throws Exception {
        employeeAppointmentDTO = employeeAppointmentService.createEmployeeAppointment(employeeAppointmentDTO);
        if(employeeAppointmentDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update EmployeeAppointment"));
        }

        return ResponseEntity.ok(new MetadataResponse(employeeAppointmentDTO.getEmployeeAppointmentId(), "Successful to update EmployeeAppointment"));
    }

    @GetMapping("/employeeAppointment/get-all")
    public ResponseEntity<List<EmployeeAppointmentDTO>> getAllEmployeeAppointment() throws Exception {
        List<EmployeeAppointmentDTO> employeeAppointmentDTOList = employeeAppointmentService.getAllEmployeeAppointment();
        return ResponseEntity.ok(employeeAppointmentDTOList);
    }

    @PutMapping("/employeeAppointment/update/{employeeAppointmentId}")
    public ResponseEntity<MetadataResponse> updateEmployeeAppointment(@PathVariable("employeeAppointmentId") Long employeeAppointmentId, @RequestBody EmployeeAppointmentDTO employeeAppointmentDTO) throws Exception {
        employeeAppointmentDTO = employeeAppointmentService.updateEmployeeAppointment(employeeAppointmentId, employeeAppointmentDTO);
        if(employeeAppointmentDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update employeeAppointment"));
        }

        return ResponseEntity.ok(new MetadataResponse(employeeAppointmentDTO.getEmployeeAppointmentId(), "Successful to update employeeAppointment"));
    }

    @DeleteMapping("/employeeAppointment/delete/{employeeAppointmentId}")
    public ResponseEntity<MetadataResponse> deleteEmployeeAppointment(@PathVariable("employeeAppointmentId") Long employeeAppointmentId) throws Exception {
        Boolean boolDel = employeeAppointmentService.deleteEmployeeAppointment(employeeAppointmentId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete employeeAppointment"));
        }

        return ResponseEntity.ok(new MetadataResponse(employeeAppointmentId, "Successful to employeeAppointment"));
    }

    @GetMapping("/employeeAppointment/by-job-position/{jobPositionId}")
    public ResponseEntity<List<EmployeeAppointmentDTO>> getByJobPosition(@PathVariable Long jobPositionId) throws Exception {

        List<EmployeeAppointmentDTO> list = employeeAppointmentService.getByJobPositionId(jobPositionId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/employeeAppointment/getLatestEmployeeAppointmentByEmployeeId/{employeeId}")
    public ResponseEntity<List<EmployeeAppointmentDTO>> getLatestEmployeeAppointmentByEmployeeId() throws Exception {
        List<EmployeeAppointmentDTO> employeeAppointmentDTOList = employeeAppointmentService.getAllEmployeeAppointment();
        return ResponseEntity.ok(employeeAppointmentDTOList);
    }

}