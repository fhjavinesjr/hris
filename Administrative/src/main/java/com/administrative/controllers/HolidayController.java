package com.administrative.controllers;

import com.administrative.dtos.HolidayDTO;
import com.administrative.services.HolidayService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @PostMapping("/holiday/create")
    public ResponseEntity<MetadataResponse> createHoliday(@RequestBody HolidayDTO holidayDTO) throws Exception {
        holidayDTO = holidayService.createHoliday(holidayDTO);
        if (holidayDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create Holiday"));
        }

        return ResponseEntity.ok(new MetadataResponse(holidayDTO.getHolidayId(), "Successful to create Holiday"));
    }

    @GetMapping("/holiday/get-all")
    public ResponseEntity<List<HolidayDTO>> getAllHoliday() throws Exception {
        List<HolidayDTO> holidayDTOList = holidayService.getAllHoliday();
        return ResponseEntity.ok(holidayDTOList);
    }

    @PutMapping("/holiday/update/{holidayId}")
    public ResponseEntity<MetadataResponse> updateHoliday(@PathVariable("holidayId") Long holidayId,
                                                          @RequestBody HolidayDTO holidayDTO) throws Exception {
        holidayDTO = holidayService.updateHoliday(holidayId, holidayDTO);
        if (holidayDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Holiday"));
        }

        return ResponseEntity.ok(new MetadataResponse(holidayId, "Successful to update Holiday"));
    }

    @DeleteMapping("/holiday/delete/{holidayId}")
    public ResponseEntity<MetadataResponse> deleteHoliday(@PathVariable("holidayId") Long holidayId) throws Exception {
        Boolean boolDel = holidayService.deleteHoliday(holidayId);
        if (!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete Holiday"));
        }

        return ResponseEntity.ok(new MetadataResponse(holidayId, "Successful to delete Holiday"));
    }
}
