package com.administrative.controllers;

import com.administrative.dtos.WTAXContributionDTO;
import com.administrative.services.WTAXContributionService;
import com.hris.common.dtos.MetadataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class WTAXContributionController {

    private final WTAXContributionService wTAXContributionService;

    public WTAXContributionController(WTAXContributionService wTAXContributionService) {
        this.wTAXContributionService = wTAXContributionService;
    }

    @PostMapping("/wTAXContribution/create")
    public ResponseEntity<MetadataResponse> createWTAXContribution(@RequestBody WTAXContributionDTO wTAXContributionDTO) throws Exception {
        validateTaxYear(wTAXContributionDTO);
        wTAXContributionDTO = wTAXContributionService.createWTAXContribution(wTAXContributionDTO);
        if(wTAXContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to create WTAXContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(wTAXContributionDTO.getwTaxContributionId(), "Successful to create WTAXContribution"));
    }

    @GetMapping("/wTAXContribution/get-all")
    public ResponseEntity<List<WTAXContributionDTO>> getAllWTAXContribution() throws Exception {
        List<WTAXContributionDTO> wTAXContributionDTOList = wTAXContributionService.getAllWTAXContribution();
        return ResponseEntity.ok(wTAXContributionDTOList);
    }

    @PutMapping("/wTAXContribution/update/{wTAXContributionId}")
    public ResponseEntity<MetadataResponse> updateWTAXContribution(@PathVariable("wTAXContributionId") Long wTAXContributionId, @RequestBody WTAXContributionDTO wTAXContributionDTO) throws Exception {
        validateTaxYear(wTAXContributionDTO);
        wTAXContributionDTO = wTAXContributionService.updateWTAXContribution(wTAXContributionId, wTAXContributionDTO);
        if(wTAXContributionDTO == null) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to update wTAXContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(wTAXContributionDTO.getwTaxContributionId(), "Successful to update wTAXContribution"));
    }

    @DeleteMapping("/wTAXContribution/delete/{wTAXContributionId}")
    public ResponseEntity<MetadataResponse> deleteWTAXContribution(@PathVariable("wTAXContributionId") Long wTAXContributionId) throws Exception {
        Boolean boolDel = wTAXContributionService.deleteWTAXContribution(wTAXContributionId);
        if(!boolDel) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MetadataResponse("Failed to delete wTAXContribution"));
        }

        return ResponseEntity.ok(new MetadataResponse(wTAXContributionId, "Successful to wTAXContribution"));
    }

    /**
     * Returns all withholding tax brackets grouped by salaryType.
     * Used by the Payroll batch service to load tax tables for computation.
     *
     * Format per bracket: { incomeFrom, incomeTo, baseTax, excessRate }
     * excessRate is normalised to decimal (e.g. stored "15" → returned 0.15).
     */
    @GetMapping("/wh-tax/brackets")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getTaxBrackets() throws Exception {
        List<WTAXContributionDTO> all = wTAXContributionService.getAllWTAXContribution();
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (WTAXContributionDTO dto : all) {
            if ("Annual".equalsIgnoreCase(dto.getSalaryType())) continue;
            String salaryType = dto.getSalaryType() != null ? dto.getSalaryType() : "UNKNOWN";

            double baseTax = 0.0;
            try { baseTax = Double.parseDouble(dto.getFixedAmount()); } catch (Exception ignored) {}

            double excessRate = 0.0;
            try {
                excessRate = Double.parseDouble(dto.getPercentageOverBase());
                // Normalise: if stored as percentage (e.g. 15.0) convert to decimal (0.15)
                if (excessRate > 1.0) excessRate = excessRate / 100.0;
            } catch (Exception ignored) {}

            Double incomeFrom = null;
            try { incomeFrom = Double.parseDouble(dto.getIncomeFrom()); } catch (Exception ignored) {}

            Double incomeTo = null;
            try { incomeTo = Double.parseDouble(dto.getIncomeTo()); } catch (Exception ignored) {}

            Map<String, Object> bracket = new LinkedHashMap<>();
            bracket.put("incomeFrom", incomeFrom);
            bracket.put("incomeTo", incomeTo);   // null = and-above bracket
            bracket.put("baseTax", baseTax);
            bracket.put("excessRate", excessRate);

            result.computeIfAbsent(salaryType, k -> new ArrayList<>()).add(bracket);
        }

        return ResponseEntity.ok(result);
    }

    /** Annual brackets are isolated by tax year so historical reconciliation stays reproducible. */
    @GetMapping("/wh-tax/annual-brackets")
    public ResponseEntity<List<Map<String, Object>>> getAnnualTaxBrackets(
            @RequestParam Integer year) throws Exception {
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("A valid four-digit tax year is required.");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (WTAXContributionDTO dto : wTAXContributionService.getAllWTAXContribution()) {
            if (!"Annual".equalsIgnoreCase(dto.getSalaryType()) || !year.equals(dto.getTaxYear())) {
                continue;
            }
            Map<String, Object> bracket = new LinkedHashMap<>();
            bracket.put("incomeFrom", decimal(dto.getIncomeFrom(), BigDecimal.ZERO));
            bracket.put("incomeTo", decimal(dto.getIncomeTo(), null));
            bracket.put("baseTax", decimal(dto.getFixedAmount(), BigDecimal.ZERO));
            BigDecimal rate = decimal(dto.getPercentageOverBase(), BigDecimal.ZERO);
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                rate = rate.movePointLeft(2);
            }
            bracket.put("excessRate", rate);
            result.add(bracket);
        }
        result.sort((left, right) -> ((BigDecimal) left.get("incomeFrom"))
                .compareTo((BigDecimal) right.get("incomeFrom")));
        return ResponseEntity.ok(result);
    }

    private BigDecimal decimal(String value, BigDecimal fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private void validateTaxYear(WTAXContributionDTO dto) {
        if ("Annual".equalsIgnoreCase(dto.getSalaryType())) {
            if (dto.getTaxYear() == null || dto.getTaxYear() < 1900 || dto.getTaxYear() > 9999) {
                throw new IllegalArgumentException("A valid tax year is required for Annual brackets.");
            }
        } else {
            dto.setTaxYear(null);
        }
    }

}
