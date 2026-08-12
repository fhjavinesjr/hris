package com.timekeeping.controllers;

import com.timekeeping.services.DtrTransactionDeleteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dtr-daily")
public class DtrTransactionDeleteController {

    private final DtrTransactionDeleteService deleteService;

    public DtrTransactionDeleteController(DtrTransactionDeleteService deleteService) {
        this.deleteService = deleteService;
    }

    @DeleteMapping("/segment/{segmentId}")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long segmentId) {
        deleteService.deleteSegmentAndEmptyDaily(segmentId);
        return ResponseEntity.noContent().build();
    }
}
