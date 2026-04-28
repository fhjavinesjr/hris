package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.TimeCorrectionDTO;
import com.humanresource.services.TimeCorrectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TimeCorrectionController {

    private final TimeCorrectionService service;

    public TimeCorrectionController(TimeCorrectionService service) {
        this.service = service;
    }

    @PostMapping("/time-correction/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody TimeCorrectionDTO dto) throws Exception {
        TimeCorrectionDTO created = service.create(dto);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Time Correction"));
        }
        return ResponseEntity.ok(new MetadataResponse(created.getTimeCorrectionId(), "Time Correction created successfully"));
    }

    @GetMapping("/time-correction/get-all")
    public ResponseEntity<List<TimeCorrectionDTO>> getAll() throws Exception {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/time-correction/get-all/{employeeId}")
    public ResponseEntity<List<TimeCorrectionDTO>> getAllByEmployeeId(@PathVariable Long employeeId) throws Exception {
        return ResponseEntity.ok(service.getAllByEmployeeId(employeeId));
    }

    @GetMapping("/time-correction/get-pending")
    public ResponseEntity<List<TimeCorrectionDTO>> getPending() throws Exception {
        return ResponseEntity.ok(service.getPendingAll());
    }

    @PutMapping("/time-correction/approve/{id}")
    public ResponseEntity<MetadataResponse> approve(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        TimeCorrectionDTO result = service.approve(id, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to approve Time Correction"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Time Correction approved"));
    }

    @PutMapping("/time-correction/disapprove/{id}")
    public ResponseEntity<MetadataResponse> disapprove(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body) throws Exception {
        Long approvedById = body.get("approvedById") != null ? Long.valueOf(body.get("approvedById").toString()) : null;
        String remarks = body.get("remarks") != null ? body.get("remarks").toString() : "";
        TimeCorrectionDTO result = service.disapprove(id, approvedById, remarks);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to disapprove Time Correction"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Time Correction disapproved"));
    }

    @PutMapping("/time-correction/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long id,
                                                    @RequestBody TimeCorrectionDTO dto) throws Exception {
        TimeCorrectionDTO result = service.update(id, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MetadataResponse("Time Correction not found"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Time Correction updated"));
    }

    @DeleteMapping("/time-correction/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean result = service.delete(id);
        if (!result) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MetadataResponse("Time Correction not found"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Time Correction deleted"));
    }
}
