package com.humanresource.impl;

import com.humanresource.dtos.StaffOvertimeParticipantDTO;
import com.humanresource.dtos.StaffOvertimeRequestDTO;
import com.humanresource.entitymodels.OvertimeRequest;
import com.humanresource.integration.administrative.StaffOvertimeAuthorizationClient;
import com.humanresource.repositories.OvertimeRequestRepository;
import com.humanresource.repositories.ReportHeaderSettingsRepository;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OvertimeRequestStaffTest {
    @Test
    void createsOneRecommendedGroupWithIndividualParticipantRows() throws Exception {
        OvertimeRequestRepository repository = mock(OvertimeRequestRepository.class);
        StaffOvertimeAuthorizationClient authorization = mock(StaffOvertimeAuthorizationClient.class);
        Map<Long, OvertimeRequest> store = new HashMap<>();
        AtomicLong sequence = new AtomicLong();
        when(repository.save(any(OvertimeRequest.class))).thenAnswer(invocation -> {
            OvertimeRequest row = invocation.getArgument(0);
            if (row.getOvertimeRequestId() == null) row.setOvertimeRequestId(sequence.incrementAndGet());
            store.put(row.getOvertimeRequestId(), row);
            return row;
        });
        when(repository.findById(any(Long.class))).thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(authorization.requireAuthorizedUnit(any(), any(), any(), any())).thenReturn(
                new StaffOvertimeAuthorizationClient.SupervisedUnit(
                        10L, "HR", "HEAD", null, null, List.of(2L, 3L)));

        OvertimeRequestImpl service = new OvertimeRequestImpl(
                repository, mock(ReportHeaderSettingsRepository.class), mock(DataSource.class), authorization);
        StaffOvertimeRequestDTO command = new StaffOvertimeRequestDTO();
        command.setBusinessUnitId(10L);
        command.setDateFiled(LocalDate.of(2026, 9, 18));
        command.setDateTimeFrom(LocalDateTime.of(2026, 9, 20, 17, 0));
        command.setDateTimeTo(LocalDateTime.of(2026, 9, 20, 19, 0));
        command.setWorkType("REGULAR_OVERTIME");
        command.setAuthorityReference("OO-2026-1");
        command.setPurpose("Month-end work");
        command.setExpectedOutput("Validated report");
        command.setParticipants(new ArrayList<>(List.of(participant(2L), participant(3L))));

        var result = service.createStaffRequest(command, 1L, "Bearer token");

        assertThat(store).hasSize(2);
        assertThat(store.values()).extracting(OvertimeRequest::getGroupRequestId).containsOnly(result.getGroupRequestId());
        assertThat(store.values()).extracting(OvertimeRequest::getRecommendationStatus).containsOnly("Recommended");
        assertThat(store.values()).extracting(OvertimeRequest::getEmployeeId).containsExactlyInAnyOrder(2L, 3L);
        assertThat(result.getParticipantEmployeeIds()).containsExactly(2L, 3L);
    }

    private StaffOvertimeParticipantDTO participant(Long employeeId) {
        StaffOvertimeParticipantDTO value = new StaffOvertimeParticipantDTO();
        value.setEmployeeId(employeeId);
        value.setBreakMinutes(0);
        return value;
    }
}
