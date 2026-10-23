package com.payroll.controllers;

import com.payroll.dtos.Bir1604COfficerReviewDTO;
import com.payroll.dtos.Bir1604COfficerReviewRequest;
import com.payroll.impl.Bir1604COfficerReviewService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/annualization/1604c-review")
public class Bir1604COfficerReviewController {
    private final Bir1604COfficerReviewService service;

    public Bir1604COfficerReviewController(Bir1604COfficerReviewService service) {
        this.service = service;
    }

    @GetMapping
    public Bir1604COfficerReviewDTO review(@RequestParam int year,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return service.review(year, authorizationHeader);
    }

    @PutMapping
    public Bir1604COfficerReviewDTO save(@RequestParam int year,
            @RequestBody Bir1604COfficerReviewRequest request,
            Authentication authentication,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank())
            throw new AccessDeniedException("Officer identity is required.");
        return service.save(year, request, authentication.getName(), authorizationHeader);
    }
}
