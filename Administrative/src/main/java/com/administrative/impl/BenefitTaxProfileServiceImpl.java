package com.administrative.impl;

import com.administrative.dtos.BenefitTaxProfileDTO;
import com.administrative.entitymodels.BenefitTaxProfile;
import com.administrative.repositories.BenefitTaxProfileRepository;
import com.administrative.services.BenefitTaxProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BenefitTaxProfileServiceImpl implements BenefitTaxProfileService {
    private static final Set<String> EXEMPT_ITEMS = Set.of("29", "30", "31", "32", "33", "34", "35", "37");
    private static final Set<String> TAXABLE_ITEMS = Set.of("39", "40", "41", "42", "43", "44A", "44B", "45", "46", "47", "48", "49", "50", "51A", "51B");
    private static final Set<String> PERIODS = Set.of("ANNUAL", "MONTHLY", "PER_EVENT");
    private final BenefitTaxProfileRepository repository;

    public BenefitTaxProfileServiceImpl(BenefitTaxProfileRepository repository) {
        this.repository = repository;
    }

    @Override @Transactional(readOnly = true)
    public List<BenefitTaxProfileDTO> list(int year) {
        if (year < 2018 || year > 9999) throw bad("Select a valid tax year");
        return repository.findByTaxYearOrderByLabelAsc(year).stream().map(this::dto).toList();
    }

    @Override @Transactional
    public BenefitTaxProfileDTO save(Long id, BenefitTaxProfileDTO request) {
        if (request == null) throw bad("Benefit tax profile is required");
        BenefitTaxProfile target = id == null ? new BenefitTaxProfile() : repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Benefit tax profile was not found"));
        if (request.taxYear() == null || request.taxYear() < 2018 || request.taxYear() > 9999) throw bad("Select a valid tax year");
        String code = required(request.profileCode(), "Profile code", 50).toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z0-9_-]+")) throw bad("Profile code may contain letters, numbers, dash and underscore only");
        if (repository.existsByProfileCodeIgnoreCaseAndTaxYearAndIdNot(code, request.taxYear(), id == null ? -1L : id)) {
            throw bad("A profile with this code and tax year already exists");
        }
        BigDecimal ceiling = request.guidanceCeiling();
        if (ceiling != null && (ceiling.signum() < 0 || ceiling.scale() > 2)) throw bad("Guidance ceiling must be a non-negative peso amount");
        String period = optional(request.ceilingPeriod(), 30);
        if (ceiling != null && (period == null || !PERIODS.contains(period))) throw bad("Select an annual, monthly or per-event ceiling period");
        String exempt = optional(request.exemptBirItem(), 4);
        String taxable = optional(request.taxableBirItem(), 4);
        if (exempt != null && !EXEMPT_ITEMS.contains(exempt)) throw bad("Select a valid non-taxable BIR item");
        if (taxable != null && !TAXABLE_ITEMS.contains(taxable)) throw bad("Select a valid taxable BIR item");
        String url = optional(request.sourceUrl(), 500);
        if (url != null && !url.startsWith("https://")) throw bad("Legal source must use HTTPS");
        target.setProfileCode(code);
        target.setTaxYear(request.taxYear());
        target.setLabel(required(request.label(), "Profile label", 120));
        target.setGuidanceCeiling(ceiling);
        target.setCeilingPeriod(period);
        target.setSharedPoolCode(optional(request.sharedPoolCode(), 50));
        target.setExemptBirItem(exempt);
        target.setTaxableBirItem(taxable);
        target.setLegalBasis(required(request.legalBasis(), "Legal basis", 200));
        target.setSourceUrl(url);
        return dto(repository.save(target));
    }

    private BenefitTaxProfileDTO dto(BenefitTaxProfile source) {
        return new BenefitTaxProfileDTO(source.getId(), source.getProfileCode(), source.getTaxYear(),
                source.getLabel(), source.getGuidanceCeiling(), source.getCeilingPeriod(),
                source.getSharedPoolCode(), source.getExemptBirItem(), source.getTaxableBirItem(),
                source.getLegalBasis(), source.getSourceUrl());
    }

    private String required(String value, String label, int max) {
        String normalized = optional(value, max);
        if (normalized == null) throw bad(label + " is required");
        return normalized;
    }

    private String optional(String value, int max) {
        if (value == null || value.isBlank()) return null;
        String result = value.trim();
        if (result.length() > max) throw bad("Value is too long");
        return result;
    }

    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
