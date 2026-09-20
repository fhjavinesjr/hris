package com.timekeeping.impl;

import com.timekeeping.repositories.AdmsPunchLogRepository;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import com.timekeeping.repositories.WorkScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmsDtrProcessingServicePassSlipTest {
    private AdmsPunchLogRepository punchLogs;
    private DTRDailyRepository dailies;
    private JdbcTemplate jdbc;
    private AdmsDtrProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        punchLogs = mock(AdmsPunchLogRepository.class);
        dailies = mock(DTRDailyRepository.class);
        jdbc = mock(JdbcTemplate.class);
        service = new AdmsDtrProcessingServiceImpl(
                punchLogs,
                mock(WorkScheduleRepository.class),
                dailies,
                mock(DTRSegmentRepository.class),
                jdbc
        );
        ReflectionTestUtils.setField(service, "dtrProcessingEnabled", true);

        when(jdbc.queryForList(anyString(), any(), any(), any())).thenReturn(List.of());
        when(punchLogs.findByImportStatusAndEmployeeIdAndCheckTimeBetweenOrderByCheckTimeAscAdmsPunchLogIdAsc(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
    }

    @Test
    void searchDoesNotSuppressDtrForAnApprovedPassSlip() {
        service.processEmployeePunches(
                "34",
                LocalDateTime.of(2026, 7, 8, 0, 0),
                LocalDateTime.of(2026, 7, 8, 23, 59, 59)
        );

        verify(jdbc, never()).queryForList(
                org.mockito.ArgumentMatchers.contains("FROM pass_slip"),
                any(), any(), any()
        );
        verify(dailies, never()).delete(any());
    }
}
