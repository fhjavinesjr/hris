package com.payroll.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.payroll.dtos.EarningAllowanceDTO;
import com.payroll.services.EarningAllowanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EarningAllowanceController {

    private final EarningAllowanceService earningAllowanceService;

    public EarningAllowanceController(EarningAllowanceService earningAllowanceService) {
        this.earningAllowanceService = earningAllowanceService;
    }

    @PostMapping("/earningAllowance/create-bulk")
    public ResponseEntity<MetadataResponse> createBulk(@RequestBody List<EarningAllowanceDTO> dtoList) throws Exception {
        List<EarningAllowanceDTO> saved = earningAllowanceService.createBulkEarningAllowance(dtoList);
        if (saved == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create EarningAllowance records"));
        }
        return ResponseEntity.ok(new MetadataResponse((long) saved.size(), "Successfully created " + saved.size() + " EarningAllowance record(s)"));
    }

    @GetMapping("/earningAllowance/get-all")
    public ResponseEntity<List<EarningAllowanceDTO>> getAll() throws Exception {
        return ResponseEntity.ok(earningAllowanceService.getAllEarningAllowance());
    }

    @PutMapping("/earningAllowance/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable("id") Long id, @RequestBody EarningAllowanceDTO dto) throws Exception {
        EarningAllowanceDTO updated = earningAllowanceService.updateEarningAllowance(id, dto);
        return ResponseEntity.ok(new MetadataResponse(updated.getId(), "Successfully updated EarningAllowance"));
    }

    @DeleteMapping("/earningAllowance/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable("id") Long id) throws Exception {
        Boolean deleted = earningAllowanceService.deleteEarningAllowance(id);
        if (!deleted) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete EarningAllowance"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Successfully deleted EarningAllowance"));
    }
}
