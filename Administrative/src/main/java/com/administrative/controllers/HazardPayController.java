package com.administrative.controllers;

import com.administrative.dtos.HazardPayDTO;
import com.administrative.services.HazardPayService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HazardPayController {

    private final HazardPayService hazardPayService;

    public HazardPayController(HazardPayService hazardPayService) {
        this.hazardPayService = hazardPayService;
    }

    @PostMapping("/hazardPay/create")
    public ResponseEntity<MetadataResponse> createHazardPay(@RequestBody List<HazardPayDTO> hazardPayDTOList) throws Exception {
        List<HazardPayDTO> hazardPayDTOs = hazardPayService.createHazardPay(hazardPayDTOList);
        if(hazardPayDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update HazardPay"));
        }

        return ResponseEntity.ok(new MetadataResponse(hazardPayDTOs.get(0).getEffectivityDate().toString(), "Successful to update HazardPay"));
    }

    @GetMapping("/hazardPay/get-all")
    public ResponseEntity<List<HazardPayDTO>> getAllHazardPay() throws Exception {
        List<HazardPayDTO> hazardPayDTOList = hazardPayService.getAllHazardPay();
        return ResponseEntity.ok(hazardPayDTOList);
    }

    @GetMapping("/hazardPay/getBy/{effectivityDate}")
    public ResponseEntity<List<HazardPayDTO>> getHazardPayByEffectivityDate(@PathVariable("effectivityDate")  @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        List<HazardPayDTO> hazardPayDTOList = hazardPayService.getHazardPayByEffectivityDate(effectivityDate);
        return ResponseEntity.ok(hazardPayDTOList);
    }

    @PutMapping("/hazardPay/update")
    public ResponseEntity<MetadataResponse> updateHazardPay(@RequestBody List<HazardPayDTO> hazardPayDTOList) throws Exception {
        List<HazardPayDTO> hazardPayDTOs = hazardPayService.updateHazardPay(hazardPayDTOList);
        if(hazardPayDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update hazardPay"));
        }

        return ResponseEntity.ok(new MetadataResponse(hazardPayDTOs.get(0).getEffectivityDate().toString(), "Successful to update hazardPay"));
    }

    @DeleteMapping("/hazardPay/delete/{effectivityDate}")
    public ResponseEntity<MetadataResponse> deleteHazardPay(@PathVariable("effectivityDate") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        Boolean boolDel = hazardPayService.deleteHazardPay(effectivityDate);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete hazardPay"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful to hazardPay"));
    }

    @DeleteMapping("/hazardPay/deleteById")
    public ResponseEntity<MetadataResponse> deleteHazardPayById(@RequestBody List<HazardPayDTO> hazardPayDTOList) throws Exception {
        Boolean boolDel = hazardPayService.deleteHazardPayById(hazardPayDTOList);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete hazardPay"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful delete of hazardPay by Id"));
    }

}