package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.LearningAndDevelopmentDTO;
import com.humanresource.services.LearningAndDevelopmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LearningAndDevelopmentController {

    private final LearningAndDevelopmentService learningAndDevelopmentService;

    public LearningAndDevelopmentController(LearningAndDevelopmentService learningAndDevelopmentService) {
        this.learningAndDevelopmentService = learningAndDevelopmentService;
    }

    @PostMapping("/create/learningAndDevelopment")
    public ResponseEntity<MetadataResponse> createLearningAndDevelopment(@RequestBody LearningAndDevelopmentDTO learningAndDevelopmentDTO) throws Exception {
        learningAndDevelopmentDTO = learningAndDevelopmentService.createLearningAndDevelopment(learningAndDevelopmentDTO);
        if(learningAndDevelopmentDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create learningAndDevelopment"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new MetadataResponse(learningAndDevelopmentDTO.getLearningAndDevelopmentId(), "Successful to create learning and development"));
    }

    @GetMapping("/fetch/learningAndDevelopment/{learningAndDevelopmentId}")
    public ResponseEntity<LearningAndDevelopmentDTO> getLearningAndDevelopmentByLearningAndDevelopmentId(@PathVariable("learningAndDevelopmentId") Long learningAndDevelopmentId) throws Exception {
        LearningAndDevelopmentDTO dto = learningAndDevelopmentService.getLearningAndDevelopmentByLearningAndDevelopmentId(learningAndDevelopmentId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/fetch/learningAndDevelopment/by/{personalDataId}")
    public ResponseEntity<List<LearningAndDevelopmentDTO>> getLearningAndDevelopmentByPersonalDataId(@PathVariable("personalDataId") Long personalDataId) throws Exception {
        List<LearningAndDevelopmentDTO> dtoList = learningAndDevelopmentService.getLearningAndDevelopmentByPersonalDataId(personalDataId);
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/learningAndDevelopment/update/{learningAndDevelopmentId}")
    public Boolean updateLearningAndDevelopment(@PathVariable("learningAndDevelopmentId") Long learningAndDevelopmentId, @Valid @RequestBody Map<String, Object> updates) throws Exception {
        return learningAndDevelopmentService.updateLearningAndDevelopment(learningAndDevelopmentId, updates);
    }

    @DeleteMapping("/learningAndDevelopment/delete/{learningAndDevelopmentId}")
    public Boolean deleteLearningAndDevelopment(@PathVariable("learningAndDevelopmentId") Long learningAndDevelopmentId) throws Exception {
        return learningAndDevelopmentService.deleteLearningAndDevelopment(learningAndDevelopmentId);
    }
}