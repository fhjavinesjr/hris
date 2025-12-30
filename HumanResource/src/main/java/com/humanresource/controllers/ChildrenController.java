package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.ChildrenDTO;
import com.humanresource.services.ChildrenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChildrenController {

    private final ChildrenService childrenService;

    public ChildrenController(ChildrenService childrenService) {
        this.childrenService = childrenService;
    }

    @PostMapping("/create/children")
    public ResponseEntity<MetadataResponse> createChildren(@RequestBody ChildrenDTO childrenDTO) throws Exception {
        childrenDTO = childrenService.createChildren(childrenDTO);
        if(childrenDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create children"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(childrenDTO.getChildrenId(), "Successful to create children"));
    }

    @GetMapping("/fetch/children/{childrenId}")
    public ResponseEntity<ChildrenDTO> getChildrenByChildrenId(@PathVariable("childrenId") Long childrenId) throws Exception {
        ChildrenDTO dto = childrenService.getChildrenByChildrenId(childrenId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/children/by/{personalDataId}")
    public ResponseEntity<List<ChildrenDTO>> getChildrenByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<ChildrenDTO> dtoList = childrenService.getChildrenByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/children/update/{childrenId}")
    public Boolean updateChildren(@PathVariable("childrenId") Long childrenId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return childrenService.updateChildren(childrenId, updates);
    }

    @DeleteMapping("/children/delete/{childrenId}")
    public Boolean deleteChildren(@PathVariable("childrenId") Long childrenId) throws Exception {
        return childrenService.deleteChildren(childrenId);
    }

}
