package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.EmployeeDTO;
import com.humanresource.dtos.PersonalDataDTO;
import com.humanresource.services.PersonalDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PersonalDataController {

    private final PersonalDataService personalDataService;

    public PersonalDataController(PersonalDataService personalDataService) {
        this.personalDataService = personalDataService;
    }

    @PostMapping("/create/personal-data")
    public ResponseEntity<MetadataResponse> createPersonalData(@RequestBody PersonalDataDTO personalDataDTO) throws Exception {
        personalDataDTO = personalDataService.createPersonalData(personalDataDTO);
        if(personalDataDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create work schedule"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(personalDataDTO.getEmployeeId(), "Successful to create personal data"));
    }

    @GetMapping("/fetch/personal-data/{employeeId}")
    public ResponseEntity<PersonalDataDTO> getPersonalDataByEmployeeId(@PathVariable("employeeId") Long employeeId) throws Exception {
        PersonalDataDTO dto = personalDataService.getPersonalDataByEmployeeId(employeeId);
        return ResponseEntity.ok(dto);
    }

}
