package com.administrative.controllers;

import com.administrative.dtos.LeaveTypesDTO;
import com.administrative.services.LeaveTypesService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaveTypesController {

    private final LeaveTypesService leaveTypesService;

    public LeaveTypesController(LeaveTypesService leaveTypesService) {
        this.leaveTypesService = leaveTypesService;
    }

    @PostMapping("/leaveTypes/create")
    public ResponseEntity<MetadataResponse> createLeaveTypes(@RequestBody LeaveTypesDTO leaveTypesDTO) throws Exception {
        leaveTypesDTO = leaveTypesService.createLeaveTypes(leaveTypesDTO);
        if(leaveTypesDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update LeaveTypes"));
        }

        return ResponseEntity.ok(new MetadataResponse(leaveTypesDTO.getLeaveTypesId(), "Successful to update LeaveTypes"));
    }

    @GetMapping("/leaveTypes/get-all")
    public ResponseEntity<List<LeaveTypesDTO>> getAllLeaveTypes() throws Exception {
        List<LeaveTypesDTO> leaveTypesDTOList = leaveTypesService.getAllLeaveTypes();
        return ResponseEntity.ok(leaveTypesDTOList);
    }

    @PutMapping("/leaveTypes/update/{leaveTypesId}")
    public ResponseEntity<MetadataResponse> updateLeaveTypes(@PathVariable("leaveTypesId") Long leaveTypesId, @RequestBody LeaveTypesDTO leaveTypesDTO) throws Exception {
        leaveTypesDTO = leaveTypesService.updateLeaveTypes(leaveTypesId, leaveTypesDTO);
        if(leaveTypesDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update leaveTypes"));
        }

        return ResponseEntity.ok(new MetadataResponse(leaveTypesDTO.getLeaveTypesId(), "Successful to update leaveTypes"));
    }

    @DeleteMapping("/leaveTypes/delete/{leaveTypesId}")
    public ResponseEntity<MetadataResponse> deleteLeaveTypes(@PathVariable("leaveTypesId") Long leaveTypesId) throws Exception {
        Boolean boolDel = leaveTypesService.deleteLeaveTypes(leaveTypesId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete leaveTypes"));
        }

        return ResponseEntity.ok(new MetadataResponse(leaveTypesId, "Successful to leaveTypes"));
    }

}