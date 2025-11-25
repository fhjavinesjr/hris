package com.administrative.controllers;

import com.administrative.dtos.NatureOfAppointmentDTO;
import com.administrative.services.NatureOfAppointmentService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NatureOfAppointmentController {

    private final NatureOfAppointmentService natureOfAppointmentService;

    public NatureOfAppointmentController(NatureOfAppointmentService natureOfAppointmentService) {
        this.natureOfAppointmentService = natureOfAppointmentService;
    }

    @PostMapping("/natureOfAppointment/create")
    public ResponseEntity<MetadataResponse> createNatureOfAppointment(@RequestBody NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception {
        natureOfAppointmentDTO = natureOfAppointmentService.createNatureOfAppointment(natureOfAppointmentDTO);
        if(natureOfAppointmentDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update NatureOfAppointment"));
        }

        return ResponseEntity.ok(new MetadataResponse(natureOfAppointmentDTO.getNatureOfAppointmentId(), "Successful to update NatureOfAppointment"));
    }

    @GetMapping("/natureOfAppointment/get-all")
    public ResponseEntity<List<NatureOfAppointmentDTO>> getAllNatureOfAppointment() throws Exception {
        List<NatureOfAppointmentDTO> natureOfAppointmentDTOList = natureOfAppointmentService.getAllNatureOfAppointment();
        return ResponseEntity.ok(natureOfAppointmentDTOList);
    }

    @PutMapping("/natureOfAppointment/update/{natureOfAppointmentId}")
    public ResponseEntity<MetadataResponse> updateNatureOfAppointment(@PathVariable("natureOfAppointmentId") Long natureOfAppointmentId, @RequestBody NatureOfAppointmentDTO natureOfAppointmentDTO) throws Exception {
        natureOfAppointmentDTO = natureOfAppointmentService.updateNatureOfAppointment(natureOfAppointmentId, natureOfAppointmentDTO);
        if(natureOfAppointmentDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update natureOfAppointment"));
        }

        return ResponseEntity.ok(new MetadataResponse(natureOfAppointmentDTO.getNatureOfAppointmentId(), "Successful to update natureOfAppointment"));
    }

    @DeleteMapping("/natureOfAppointment/delete/{natureOfAppointmentId}")
    public ResponseEntity<MetadataResponse> deleteNatureOfAppointment(@PathVariable("natureOfAppointmentId") Long natureOfAppointmentId) throws Exception {
        Boolean boolDel = natureOfAppointmentService.deleteNatureOfAppointment(natureOfAppointmentId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete natureOfAppointment"));
        }

        return ResponseEntity.ok(new MetadataResponse(natureOfAppointmentId, "Successful to natureOfAppointment"));
    }

}