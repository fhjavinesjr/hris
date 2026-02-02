package com.administrative.controllers;

import com.administrative.dtos.DayEquivalentHoursDTO;
import com.administrative.services.DayEquivalentHoursService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DayEquivalentHoursController {

    private final DayEquivalentHoursService dayEquivalentHoursService;

    public DayEquivalentHoursController(DayEquivalentHoursService dayEquivalentHoursService) {
        this.dayEquivalentHoursService = dayEquivalentHoursService;
    }

    @PostMapping("/dayEquivalentHours/create")
    public ResponseEntity<MetadataResponse> createDayEquivalentHours(@RequestBody List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception {
        List<DayEquivalentHoursDTO> dayEquivalentHoursDTOs = dayEquivalentHoursService.createDayEquivalentHours(dayEquivalentHoursDTOList);
        if(dayEquivalentHoursDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update DayEquivalentHours"));
        }

        return ResponseEntity.ok(new MetadataResponse(dayEquivalentHoursDTOs.get(0).getEffectivityDate().toString(), "Successful to create DayEquivalentHours"));
    }

    @GetMapping("/dayEquivalentHours/get-all")
    public ResponseEntity<List<DayEquivalentHoursDTO>> getAllDayEquivalentHours() throws Exception {
        List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList = dayEquivalentHoursService.getAllDayEquivalentHours();
        return ResponseEntity.ok(dayEquivalentHoursDTOList);
    }

    @GetMapping("/dayEquivalentHours/getBy/{effectivityDate}")
    public ResponseEntity<List<DayEquivalentHoursDTO>> getDayEquivalentHoursByEffectivityDate(@PathVariable("effectivityDate")  @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList = dayEquivalentHoursService.getDayEquivalentHoursByEffectivityDate(effectivityDate);
        return ResponseEntity.ok(dayEquivalentHoursDTOList);
    }

    @PutMapping("/dayEquivalentHours/update")
    public ResponseEntity<MetadataResponse> updateDayEquivalentHours(@RequestBody List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception {
        List<DayEquivalentHoursDTO> dayEquivalentHoursDTOs = dayEquivalentHoursService.updateDayEquivalentHours(dayEquivalentHoursDTOList);
        if(dayEquivalentHoursDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update dayEquivalentHours"));
        }

        return ResponseEntity.ok(new MetadataResponse(dayEquivalentHoursDTOs.get(0).getEffectivityDate().toString(), "Successful to update dayEquivalentHours"));
    }

    @DeleteMapping("/dayEquivalentHours/delete/{effectivityDate}")
    public ResponseEntity<MetadataResponse> deleteDayEquivalentHours(@PathVariable("effectivityDate") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        Boolean boolDel = dayEquivalentHoursService.deleteDayEquivalentHours(effectivityDate);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete dayEquivalentHours"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful to dayEquivalentHours"));
    }

    @DeleteMapping("/dayEquivalentHours/deleteById")
    public ResponseEntity<MetadataResponse> deleteDayEquivalentHoursById(@RequestBody List<DayEquivalentHoursDTO> dayEquivalentHoursDTOList) throws Exception {
        Boolean boolDel = dayEquivalentHoursService.deleteDayEquivalentHoursById(dayEquivalentHoursDTOList);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete dayEquivalentHours"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful delete of dayEquivalentHours by Id"));
    }

}