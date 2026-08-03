package com.primehr.competency.api;

import com.primehr.competency.application.CompetencyQueryService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/primehr/v1")
public class CompetencyQueryController {

    private final CompetencyQueryService service;
    private final AgencyScopeResolver agencyScopeResolver;

    public CompetencyQueryController(CompetencyQueryService service, AgencyScopeResolver agencyScopeResolver) {
        this.service = service;
        this.agencyScopeResolver = agencyScopeResolver;
    }

    @GetMapping("/competency-categories")
    public List<CompetencyCategoryResponse> listCategories(
            Authentication authentication,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return service.listCategories(agencyScopeResolver.resolveAgencyId(authentication), active, asOf);
    }

    @GetMapping("/competencies")
    public PageResponse<CompetencySummaryResponse> listCompetencies(
            Authentication authentication,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.listCompetencies(agencyScopeResolver.resolveAgencyId(authentication), categoryId,
                active, search, asOf, page, size);
    }

    @GetMapping("/competencies/{competencyId}")
    public CompetencyDetailResponse getCompetency(
            Authentication authentication,
            @PathVariable("competencyId") String competencyId,
            @RequestParam(name = "includeInactive", defaultValue = "false") boolean includeInactive,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return service.getCompetency(agencyScopeResolver.resolveAgencyId(authentication), competencyId,
                includeInactive, asOf);
    }

    @GetMapping("/proficiency-scales")
    public List<ProficiencyScaleResponse> listScales(
            Authentication authentication,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf) {
        return service.listScales(agencyScopeResolver.resolveAgencyId(authentication), active, asOf);
    }
}
