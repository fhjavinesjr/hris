package com.administrative.controllers;

import com.administrative.dtos.GenderDTO;
import com.administrative.services.GenderService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GenderController {

    private final GenderService genderService;

    public GenderController(GenderService genderService) {
        this.genderService = genderService;
    }

    @PostMapping("/gender/create")
    public ResponseEntity<MetadataResponse> createGender(@RequestBody GenderDTO genderDTO) throws Exception {
        genderDTO = genderService.createGender(genderDTO);
        if(genderDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update Gender"));
        }

        return ResponseEntity.ok(new MetadataResponse(genderDTO.getGenderId(), "Successful to update Gender"));
    }

    @GetMapping("/gender/get-all")
    public ResponseEntity<List<GenderDTO>> getAllGender() throws Exception {
        List<GenderDTO> genderDTOList = genderService.getAllGender();
        return ResponseEntity.ok(genderDTOList);
    }

    @PutMapping("/gender/update/{genderId}")
    public ResponseEntity<MetadataResponse> updateGender(@PathVariable("genderId") Long genderId, @RequestBody GenderDTO genderDTO) throws Exception {
        genderDTO = genderService.updateGender(genderId, genderDTO);
        if(genderDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update gender"));
        }

        return ResponseEntity.ok(new MetadataResponse(genderDTO.getGenderId(), "Successful to update gender"));
    }

    @DeleteMapping("/gender/delete/{genderId}")
    public ResponseEntity<MetadataResponse> deleteGender(@PathVariable("genderId") Long genderId) throws Exception {
        Boolean boolDel = genderService.deleteGender(genderId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete gender"));
        }

        return ResponseEntity.ok(new MetadataResponse(genderId, "Successful to gender"));
    }

}