package com.payroll.controllers;

import com.payroll.dtos.TaxRemittance1601CMonthDTO;
import com.payroll.dtos.TaxRemittance1601CRequest;
import com.payroll.dtos.TaxRemittance1601CReviewDTO;
import com.payroll.dtos.TaxRemittance1601CVoidRequest;
import com.payroll.impl.TaxRemittance1601CService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/annualization/1601c-remittances")
public class TaxRemittance1601CController {
    private final TaxRemittance1601CService service;

    public TaxRemittance1601CController(TaxRemittance1601CService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<TaxRemittance1601CReviewDTO> review(
            @RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return ResponseEntity.ok(service.review(year, authorizationHeader));
    }

    @PutMapping("/{month}")
    public ResponseEntity<TaxRemittance1601CMonthDTO> save(
            @RequestParam int year,
            @PathVariable int month,
            @RequestBody TaxRemittance1601CRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        if (authentication == null || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Officer identity is required.");
        }
        return ResponseEntity.ok(service.save(year, month, request,
                authentication.getName(), authorizationHeader));
    }

    @PostMapping("/{month}/void")
    public ResponseEntity<TaxRemittance1601CMonthDTO> voidRecord(
            @RequestParam int year,
            @PathVariable int month,
            @RequestBody TaxRemittance1601CVoidRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        if (authentication == null || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Officer identity is required.");
        }
        return ResponseEntity.ok(service.voidRecord(year, month, request,
                authentication.getName(), authorizationHeader));
    }
}
