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
                    false // autoComputeHazardPay - disabled by default
            );
            repository.save(defaults);
            log.info("PayrollSettings: seeded defaults (cutoffDays=22, peraProrationDivisor=22, autoComputeHazardPay=false)");
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
                    dto.getAutoComputeHazardPay()
            );
            entity = repository.save(entity);
            dto.setPayrollSettingsId(entity.getPayrollSettingsId());
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
                        e.getAutoComputeHazardPay()))
                .collect(Collectors.toList());
    }

    @Override
    public PayrollSettingsDTO getCurrent() throws Exception {
        return repository.findLatest()
                .map(e -> new PayrollSettingsDTO(
                        e.getPayrollSettingsId(),
                        e.getEffectivityDate(),
                        e.getCutoffDays(),
                        e.getPeraProrationDivisor(),
                        e.getAutoComputeHazardPay()))
                .orElse(new PayrollSettingsDTO(null, null, 22, 22, false));
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
                repository.save(entity);
                dto.setPayrollSettingsId(entity.getPayrollSettingsId());
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
            return repository.findLatest().map(entity -> {
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
}
