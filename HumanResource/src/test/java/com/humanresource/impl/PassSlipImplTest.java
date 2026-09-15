package com.humanresource.impl;

import com.humanresource.dtos.PassSlipDTO;
import com.humanresource.entitymodels.PassSlip;
import com.humanresource.repositories.PassSlipRepository;
import com.humanresource.repositories.ReportHeaderSettingsRepository;
import com.humanresource.services.DateConflictChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PassSlipImplTest {
    private PassSlipRepository repository;
    private PassSlipImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PassSlipRepository.class);
        service = new PassSlipImpl(
                repository,
                mock(DateConflictChecker.class),
                mock(ReportHeaderSettingsRepository.class),
                mock(DataSource.class));
        when(repository.save(any(PassSlip.class))).thenAnswer(invocation -> {
            PassSlip value = invocation.getArgument(0);
            value.setPassSlipId(1L);
            return value;
        });
    }

    @Test
    void employeeCreateAlwaysStartsPendingAndIgnoresClientApprovalFields() throws Exception {
        PassSlipDTO request = validRequest();
        request.setStatus("Approved");
        request.setApprovedById(99L);

        service.create(request);

        ArgumentCaptor<PassSlip> captor = ArgumentCaptor.forClass(PassSlip.class);
        org.mockito.Mockito.verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("Pending");
        assertThat(captor.getValue().getApprovedById()).isNull();
    }

    @Test
    void overrideCreateMayPersistAnApprovedRecord() throws Exception {
        PassSlipDTO request = validRequest();
        request.setStatus("Approved");
        request.setApprovedById(99L);

        PassSlipDTO result = service.createOverride(request);

        assertThat(result.getStatus()).isEqualTo("Approved");
        assertThat(result.getApprovedById()).isEqualTo(99L);
        assertThat(result.getApprovedAt()).isNotNull();
    }

    @Test
    void rejectsMissingReturnTimeAndInvalidTimeOrder() {
        PassSlipDTO request = validRequest();
        request.setArrivalTime(null);
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("return times");

        request.setArrivalTime(LocalTime.of(9, 0));
        request.setDepartureTime(LocalTime.of(10, 0));
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("later than departure");
    }

    @Test
    void rejectsUnknownPurposeAndBlankDetails() {
        PassSlipDTO unknownPurpose = validRequest();
        unknownPurpose.setPurpose("Errand");
        assertThatThrownBy(() -> service.create(unknownPurpose))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Personal or Official");

        PassSlipDTO blankDetails = validRequest();
        blankDetails.setDetails("  ");
        assertThatThrownBy(() -> service.create(blankDetails))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("details");
    }

    private PassSlipDTO validRequest() {
        PassSlipDTO dto = new PassSlipDTO();
        dto.setEmployeeId(1L);
        dto.setDateFiled(LocalDate.of(2026, 9, 15));
        dto.setPassSlipDate(LocalDate.of(2026, 9, 16));
        dto.setPurpose("Personal");
        dto.setDepartureTime(LocalTime.of(10, 0));
        dto.setArrivalTime(LocalTime.of(11, 0));
        dto.setDetails("Personal appointment");
        return dto;
    }
}
