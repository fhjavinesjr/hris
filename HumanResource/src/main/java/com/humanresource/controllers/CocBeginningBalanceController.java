package com.humanresource.controllers;

import com.hris.common.dtos.MetadataResponse;
import com.humanresource.dtos.CocBeginningBalanceDTO;
import com.humanresource.services.CocBeginningBalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CocBeginningBalanceController {

    private final CocBeginningBalanceService cocBeginningBalanceService;

    public CocBeginningBalanceController(CocBeginningBalanceService cocBeginningBalanceService) {
        this.cocBeginningBalanceService = cocBeginningBalanceService;
    }

    @PostMapping("/coc-beginning-balance/save/{employeeId}")
    public ResponseEntity<MetadataResponse> save(
            @PathVariable Long employeeId,
            @RequestBody CocBeginningBalanceDTO dto) throws Exception {
        CocBeginningBalanceDTO result = cocBeginningBalanceService.save(employeeId, dto);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to save COC beginning balance"));
        }
        return ResponseEntity.ok(new MetadataResponse(result.getCocBeginningBalanceId(), "Successfully saved COC beginning balance"));
    }

    @GetMapping("/coc-beginning-balance/get/{employeeId}")
    public ResponseEntity<CocBeginningBalanceDTO> getByEmployeeId(
            @PathVariable Long employeeId) throws Exception {
        CocBeginningBalanceDTO dto = cocBeginningBalanceService.getByEmployeeId(employeeId);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/coc-beginning-balance/delete/{id}")
    public ResponseEntity<MetadataResponse> deleteById(@PathVariable Long id) throws Exception {
        Boolean deleted = cocBeginningBalanceService.deleteById(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete COC beginning balance"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Successfully deleted COC beginning balance"));
    }
}
