package com.administrative.controllers;

import com.administrative.dtos.PagIbigContributionDTO;
import com.administrative.services.PagIbigContributionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PagIbigContributionController {

    private final PagIbigContributionService pagIbigContributionService;

    public PagIbigContributionController(PagIbigContributionService pagIbigContributionService) {
        this.pagIbigContributionService = pagIbigContributionService;
    }

    @PostMapping("/pagibigContribution/create")
    public ResponseEntity<MetadataResponse> create(@RequestBody PagIbigContributionDTO dto) throws Exception {
        dto = pagIbigContributionService.createPagIbigContribution(dto);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create PagIbig Contribution"));
        }
        return ResponseEntity.ok(new MetadataResponse(dto.getPagIbigContributionId(), "Pag-IBIG Contribution created successfully"));
    }

    @GetMapping("/pagibigContribution/get-all")
    public ResponseEntity<List<PagIbigContributionDTO>> getAll() throws Exception {
        return ResponseEntity.ok(pagIbigContributionService.getAllPagIbigContribution());
    }

    @GetMapping("/pagibigContribution/get-current")
    public ResponseEntity<PagIbigContributionDTO> getCurrent() throws Exception {
        return ResponseEntity.ok(pagIbigContributionService.getCurrent());
    }

    @PutMapping("/pagibigContribution/update/{id}")
    public ResponseEntity<MetadataResponse> update(@PathVariable Long id,
                                                   @RequestBody PagIbigContributionDTO dto) throws Exception {
        dto = pagIbigContributionService.updatePagIbigContribution(id, dto);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update PagIbig Contribution"));
        }
        return ResponseEntity.ok(new MetadataResponse(dto.getPagIbigContributionId(), "Pag-IBIG Contribution updated successfully"));
    }

    @DeleteMapping("/pagibigContribution/delete/{id}")
    public ResponseEntity<MetadataResponse> delete(@PathVariable Long id) throws Exception {
        Boolean deleted = pagIbigContributionService.deletePagIbigContribution(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete PagIbig Contribution"));
        }
        return ResponseEntity.ok(new MetadataResponse(id, "Pag-IBIG Contribution deleted successfully"));
    }
}
