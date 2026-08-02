package com.administrative.impl;

import com.administrative.dtos.PayrollSettingsDTO;
import com.administrative.entitymodels.PayrollSettings;
import com.administrative.repositories.PayrollSettingsRepository;
import com.administrative.services.PayrollSettingsService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollSettingsImpl implements PayrollSettingsService {

    private static final Logger log = LoggerFactory.getLogger(PayrollSettingsImpl.class);
    private final PayrollSettingsRepository repository;

    public PayrollSettingsImpl(PayrollSettingsRepository repository) {
        this.repository = repository;
    }

    /**
     * Seeds default payroll constants for ZCMC on first startup.
     * cutoffDays=22 and peraProrationDivisor=22 are the standard PH government values.
     */
    @PostConstruct
    @Transactional
    public void seedDefaults() {
        if (repository.count() == 0) {
            PayrollSettings defaults = new PayrollSettings(
                    null,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    22,   // cutoffDays
                    22,   // peraProrationDivisor
                    false, // autoComputeHazardPay - disabled by default
                    PayrollSettings.DEFAULT_REGULAR_DAY_MULTIPLIER,
                    PayrollSettings.DEFAULT_REGULAR_OVERTIME_MULTIPLIER
            );
            repository.save(defaults);
            log.info("PayrollSettings: seeded defaults (cutoffDays=22, peraProrationDivisor=22, regularDayMultiplier=1.0, regularOvertimeMultiplier=1.25, autoComputeHazardPay=false)");
        }
    }

    @Transactional
    @Override
    public PayrollSettingsDTO createPayrollSettings(PayrollSettingsDTO dto) throws Exception {
        try {
            PayrollSettings entity = new PayrollSettings(
                    null,
                    dto.getEffectivityDate(),
                    dto.getCutoffDays(),
                    dto.getPeraProrationDivisor(),
                    dto.getAutoComputeHazardPay(),
                    positiveOrDefault(dto.getRegularDayMultiplier(), PayrollSettings.DEFAULT_REGULAR_DAY_MULTIPLIER),
                    positiveOrDefault(dto.getRegularOvertimeMultiplier(), PayrollSettings.DEFAULT_REGULAR_OVERTIME_MULTIPLIER)
            );
            entity = repository.save(entity);
            dto.setPayrollSettingsId(entity.getPayrollSettingsId());
            dto.setRegularDayMultiplier(entity.getRegularDayMultiplier());
            dto.setRegularOvertimeMultiplier(entity.getRegularOvertimeMultiplier());
            return dto;
        } catch (Exception e) {
            log.error("Error creating PayrollSettings: ", e);
            return null;
        }
    }

    @Override
    public List<PayrollSettingsDTO> getAllPayrollSettings() throws Exception {
        return repository.findAll().stream()
                .map(e -> new PayrollSettingsDTO(
                        e.getPayrollSettingsId(),
                        e.getEffectivityDate(),
                        e.getCutoffDays(),
                        e.getPeraProrationDivisor(),
                        e.getAutoComputeHazardPay(),
                        valueOrDefault(e.getRegularDayMultiplier(), PayrollSettings.DEFAULT_REGULAR_DAY_MULTIPLIER),
                        valueOrDefault(e.getRegularOvertimeMultiplier(), PayrollSettings.DEFAULT_REGULAR_OVERTIME_MULTIPLIER)))
                .collect(Collectors.toList());
    }

    @Override
    public PayrollSettingsDTO getCurrent() throws Exception {
        return repository.findFirstByOrderByEffectivityDateDesc()
                .map(e -> new PayrollSettingsDTO(
                        e.getPayrollSettingsId(),
                        e.getEffectivityDate(),
                        e.getCutoffDays(),
                        e.getPeraProrationDivisor(),
                        e.getAutoComputeHazardPay(),
                        valueOrDefault(e.getRegularDayMultiplier(), PayrollSettings.DEFAULT_REGULAR_DAY_MULTIPLIER),
                        valueOrDefault(e.getRegularOvertimeMultiplier(), PayrollSettings.DEFAULT_REGULAR_OVERTIME_MULTIPLIER)))
                .orElse(new PayrollSettingsDTO(null, null, 22, 22, false,
                        PayrollSettings.DEFAULT_REGULAR_DAY_MULTIPLIER,
                        PayrollSettings.DEFAULT_REGULAR_OVERTIME_MULTIPLIER));
    }

    @Transactional
    @Override
    public PayrollSettingsDTO updatePayrollSettings(Long id, PayrollSettingsDTO dto) throws Exception {
        try {
            return repository.findById(id).map(entity -> {
                entity.setEffectivityDate(dto.getEffectivityDate());
                entity.setCutoffDays(dto.getCutoffDays());
                entity.setPeraProrationDivisor(dto.getPeraProrationDivisor());
                if (dto.getAutoComputeHazardPay() != null) {
                    entity.setAutoComputeHazardPay(dto.getAutoComputeHazardPay());
                }
                entity.setRegularDayMultiplier(positiveOrDefault(
                        dto.getRegularDayMultiplier(),
                        valueOrDefault(entity.getRegularDayMultiplier(), PayrollSettings.DEFAULT_REGULAR_DAY_MULTIPLIER)));
                entity.setRegularOvertimeMultiplier(positiveOrDefault(
                        dto.getRegularOvertimeMultiplier(),
                        valueOrDefault(entity.getRegularOvertimeMultiplier(), PayrollSettings.DEFAULT_REGULAR_OVERTIME_MULTIPLIER)));
                repository.save(entity);
                dto.setPayrollSettingsId(entity.getPayrollSettingsId());
                dto.setRegularDayMultiplier(entity.getRegularDayMultiplier());
                dto.setRegularOvertimeMultiplier(entity.getRegularOvertimeMultiplier());
                return dto;
            }).orElse(null);
        } catch (Exception e) {
            log.error("Error updating PayrollSettings id={}: ", id, e);
            return null;
        }
    }

    @Transactional
    @Override
    public Boolean deletePayrollSettings(Long id) throws Exception {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("Error deleting PayrollSettings id={}: ", id, e);
            return false;
        }
    }

    @Transactional
    @Override
    public Boolean updateHazardAutoCompute(Boolean autoCompute) throws Exception {
        try {
            // Update the most recent (current) payroll settings record
            return repository.findFirstByOrderByEffectivityDateDesc().map(entity -> {
                entity.setAutoComputeHazardPay(autoCompute);
                repository.save(entity);
                log.info("Updated autoComputeHazardPay to: {}", autoCompute);
                return true;
            }).orElse(false);
        } catch (Exception e) {
            log.error("Error updating hazard auto-compute: ", e);
            return false;
        }
    }

    private BigDecimal positiveOrDefault(BigDecimal value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Payroll multipliers must be greater than zero");
        }
        return value;
    }

    private BigDecimal valueOrDefault(BigDecimal value, BigDecimal defaultValue) {
        return value != null ? value : defaultValue;
    }
}
