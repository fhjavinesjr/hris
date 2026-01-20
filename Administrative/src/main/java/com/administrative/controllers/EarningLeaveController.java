package com.administrative.controllers;

import com.administrative.dtos.EarningLeaveDTO;
import com.administrative.services.EarningLeaveService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EarningLeaveController {

    private final EarningLeaveService earningLeaveService;

    public EarningLeaveController(EarningLeaveService earningLeaveService) {
        this.earningLeaveService = earningLeaveService;
    }

    @PostMapping("/earningLeave/create")
    public ResponseEntity<MetadataResponse> createEarningLeave(@RequestBody List<EarningLeaveDTO> earningLeaveDTOList) throws Exception {
        List<EarningLeaveDTO> earningLeaveDTOs = earningLeaveService.createEarningLeave(earningLeaveDTOList);
        if(earningLeaveDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update EarningLeave"));
        }

        return ResponseEntity.ok(new MetadataResponse(earningLeaveDTOs.get(0).getEffectivityDate().toString(), "Successful to update EarningLeave"));
    }

    @GetMapping("/earningLeave/get-all")
    public ResponseEntity<List<EarningLeaveDTO>> getAllEarningLeave() throws Exception {
        List<EarningLeaveDTO> earningLeaveDTOList = earningLeaveService.getAllEarningLeave();
        return ResponseEntity.ok(earningLeaveDTOList);
    }

    @GetMapping("/earningLeave/getBy/{effectivityDate}")
    public ResponseEntity<List<EarningLeaveDTO>> getEarningLeaveByEffectivityDate(@PathVariable("effectivityDate")  @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        List<EarningLeaveDTO> earningLeaveDTOList = earningLeaveService.getEarningLeaveByEffectivityDate(effectivityDate);
        return ResponseEntity.ok(earningLeaveDTOList);
    }

    @PutMapping("/earningLeave/update")
    public ResponseEntity<MetadataResponse> updateEarningLeave(@RequestBody List<EarningLeaveDTO> earningLeaveDTOList) throws Exception {
        List<EarningLeaveDTO> earningLeaveDTOs = earningLeaveService.updateEarningLeave(earningLeaveDTOList);
        if(earningLeaveDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update earningLeave"));
        }

        return ResponseEntity.ok(new MetadataResponse(earningLeaveDTOs.get(0).getEffectivityDate().toString(), "Successful to update earningLeave"));
    }

    @DeleteMapping("/earningLeave/delete/{effectivityDate}")
    public ResponseEntity<MetadataResponse> deleteEarningLeave(@PathVariable("effectivityDate") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        Boolean boolDel = earningLeaveService.deleteEarningLeave(effectivityDate);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete earningLeave"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful to earningLeave"));
    }

}