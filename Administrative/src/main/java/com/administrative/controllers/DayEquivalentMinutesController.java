package com.administrative.controllers;

import com.administrative.dtos.DayEquivalentMinutesDTO;
import com.administrative.services.DayEquivalentMinutesService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DayEquivalentMinutesController {

    private final DayEquivalentMinutesService dayEquivalentMinutesService;

    public DayEquivalentMinutesController(DayEquivalentMinutesService dayEquivalentMinutesService) {
        this.dayEquivalentMinutesService = dayEquivalentMinutesService;
    }

    @PostMapping("/dayEquivalentMinutes/create")
    public ResponseEntity<MetadataResponse> createDayEquivalentMinutes(@RequestBody List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception {
        List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOs = dayEquivalentMinutesService.createDayEquivalentMinutes(dayEquivalentMinutesDTOList);
        if(dayEquivalentMinutesDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update DayEquivalentMinutes"));
        }

        return ResponseEntity.ok(new MetadataResponse(dayEquivalentMinutesDTOs.get(0).getEffectivityDate().toString(), "Successful to create DayEquivalentMinutes"));
    }

    @GetMapping("/dayEquivalentMinutes/get-all")
    public ResponseEntity<List<DayEquivalentMinutesDTO>> getAllDayEquivalentMinutes() throws Exception {
        List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList = dayEquivalentMinutesService.getAllDayEquivalentMinutes();
        return ResponseEntity.ok(dayEquivalentMinutesDTOList);
    }

    @GetMapping("/dayEquivalentMinutes/getBy/{effectivityDate}")
    public ResponseEntity<List<DayEquivalentMinutesDTO>> getDayEquivalentMinutesByEffectivityDate(@PathVariable("effectivityDate")  @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList = dayEquivalentMinutesService.getDayEquivalentMinutesByEffectivityDate(effectivityDate);
        return ResponseEntity.ok(dayEquivalentMinutesDTOList);
    }

    @PutMapping("/dayEquivalentMinutes/update")
    public ResponseEntity<MetadataResponse> updateDayEquivalentMinutes(@RequestBody List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception {
        List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOs = dayEquivalentMinutesService.updateDayEquivalentMinutes(dayEquivalentMinutesDTOList);
        if(dayEquivalentMinutesDTOs == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update dayEquivalentMinutes"));
        }

        return ResponseEntity.ok(new MetadataResponse(dayEquivalentMinutesDTOs.get(0).getEffectivityDate().toString(), "Successful to update dayEquivalentMinutes"));
    }

    @DeleteMapping("/dayEquivalentMinutes/delete/{effectivityDate}")
    public ResponseEntity<MetadataResponse> deleteDayEquivalentMinutes(@PathVariable("effectivityDate") @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm:ss") LocalDateTime effectivityDate) throws Exception {
        Boolean boolDel = dayEquivalentMinutesService.deleteDayEquivalentMinutes(effectivityDate);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete dayEquivalentMinutes"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful to dayEquivalentMinutes"));
    }

    @DeleteMapping("/dayEquivalentMinutes/deleteById")
    public ResponseEntity<MetadataResponse> deleteDayEquivalentMinutesById(@RequestBody List<DayEquivalentMinutesDTO> dayEquivalentMinutesDTOList) throws Exception {
        Boolean boolDel = dayEquivalentMinutesService.deleteDayEquivalentMinutesById(dayEquivalentMinutesDTOList);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete dayEquivalentMinutes"));
        }

        return ResponseEntity.ok(new MetadataResponse(0L, "Successful delete of dayEquivalentMinutes by Id"));
    }

}