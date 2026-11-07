package com.administrative.impl;

import com.administrative.dtos.MinimumWageRateDTO;
import com.administrative.entitymodels.MinimumWageRate;
import com.administrative.repositories.MinimumWageRateRepository;
import com.administrative.services.MinimumWageRateService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class MinimumWageRateServiceImpl implements MinimumWageRateService {
    private final MinimumWageRateRepository repository;

    public MinimumWageRateServiceImpl(MinimumWageRateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MinimumWageRateDTO> getAll() {
        return repository.findAllByOrderByRegionCodeAscEffectiveFromDesc().stream()
                .map(this::toDto).toList();
    }

    @Override
    @Transactional
    public MinimumWageRateDTO create(MinimumWageRateDTO request) {
        MinimumWageRate target = new MinimumWageRate();
        apply(target, request);
        validateNoOverlap(target, null);
        return toDto(repository.save(target));
    }

    @Override
    @Transactional
    public MinimumWageRateDTO update(Long id, MinimumWageRateDTO request) {
        MinimumWageRate target = repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Minimum wage rate was not found"));
        apply(target, request);
        validateNoOverlap(target, id);
        return toDto(repository.save(target));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Minimum wage rate was not found");
        }
        repository.deleteById(id);
    }

    private void apply(MinimumWageRate target, MinimumWageRateDTO request) {
        if (request == null) throw bad("Minimum wage rate details are required");
        target.setRegionCode(required(request.regionCode(), "Region code", 30).toUpperCase());
        target.setRegionName(required(request.regionName(), "Region name", 150));
        if (request.effectiveFrom() == null) throw bad("Effective-from date is required");
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw bad("Effective-to date cannot be before the effective-from date");
        }
        if (request.dailyRate() == null || request.dailyRate().signum() <= 0) {
            throw bad("Daily statutory minimum wage must be greater than zero");
        }
        if (request.monthlyRate() == null || request.monthlyRate().signum() <= 0) {
            throw bad("Monthly statutory minimum wage must be greater than zero");
        }
        target.setEffectiveFrom(request.effectiveFrom());
        target.setEffectiveTo(request.effectiveTo());
        target.setDailyRate(request.dailyRate().setScale(2, RoundingMode.HALF_UP));
        target.setMonthlyRate(request.monthlyRate().setScale(2, RoundingMode.HALF_UP));
        target.setLegalBasis(required(request.legalBasis(), "Legal basis", 200));
        target.setRemarks(optional(request.remarks(), 500));
    }

    private void validateNoOverlap(MinimumWageRate candidate, Long excludedId) {
        for (MinimumWageRate existing : repository
                .findByRegionCodeIgnoreCaseOrderByEffectiveFromAsc(candidate.getRegionCode())) {
            if (existing.getId().equals(excludedId)) continue;
            if (overlaps(candidate.getEffectiveFrom(), candidate.getEffectiveTo(),
                    existing.getEffectiveFrom(), existing.getEffectiveTo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A minimum wage rate already covers part of this date range for the region");
            }
        }
    }

    private boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return (endB == null || !startA.isAfter(endB)) && (endA == null || !startB.isAfter(endA));
    }

    private String required(String value, String label, int max) {
        if (value == null || value.isBlank()) throw bad(label + " is required");
        String clean = value.trim();
        if (clean.length() > max) throw bad(label + " cannot exceed " + max + " characters");
        return clean;
    }

    private String optional(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String clean = value.trim();
        if (clean.length() > max) throw bad("Remarks cannot exceed " + max + " characters");
        return clean;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private MinimumWageRateDTO toDto(MinimumWageRate value) {
        return new MinimumWageRateDTO(value.getId(), value.getRegionCode(), value.getRegionName(),
                value.getEffectiveFrom(), value.getEffectiveTo(), value.getDailyRate(),
                value.getMonthlyRate(), value.getLegalBasis(), value.getRemarks());
    }
}
