package com.administrative.impl;

import com.administrative.dtos.PayrollSettingsDTO;
import com.administrative.entitymodels.PayrollSettings;
import com.administrative.repositories.PayrollSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayrollSettingsImplTest {

    @Mock
    private PayrollSettingsRepository repository;

    @Test
    void createUsesBackwardCompatibleMultiplierDefaultsWhenFieldsAreOmitted() throws Exception {
        PayrollSettingsDTO request = new PayrollSettingsDTO(
                null, LocalDateTime.of(2026, 8, 1, 0, 0), 22, 22, false);
        when(repository.save(any(PayrollSettings.class))).thenAnswer(invocation -> {
            PayrollSettings saved = invocation.getArgument(0);
            saved.setPayrollSettingsId(1L);
            return saved;
        });

        PayrollSettingsDTO result = new PayrollSettingsImpl(repository).createPayrollSettings(request);

        ArgumentCaptor<PayrollSettings> captor = ArgumentCaptor.forClass(PayrollSettings.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getRegularDayMultiplier()).isEqualByComparingTo("1.0000");
        assertThat(captor.getValue().getRegularOvertimeMultiplier()).isEqualByComparingTo("1.2500");
        assertThat(result.getRegularDayMultiplier()).isEqualByComparingTo("1.0000");
        assertThat(result.getRegularOvertimeMultiplier()).isEqualByComparingTo("1.2500");
    }

    @Test
    void getCurrentBackfillsMultipliersForRowsCreatedBeforeTheColumnsExisted() throws Exception {
        PayrollSettings legacy = new PayrollSettings();
        legacy.setPayrollSettingsId(7L);
        legacy.setEffectivityDate(LocalDateTime.of(2025, 1, 1, 0, 0));
        legacy.setCutoffDays(22);
        legacy.setPeraProrationDivisor(22);
        legacy.setAutoComputeHazardPay(false);
        when(repository.findFirstByOrderByEffectivityDateDesc()).thenReturn(Optional.of(legacy));

        PayrollSettingsDTO result = new PayrollSettingsImpl(repository).getCurrent();

        assertThat(result.getRegularDayMultiplier()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(result.getRegularOvertimeMultiplier()).isEqualByComparingTo("1.2500");
    }
}
