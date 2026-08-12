package com.timekeeping.impl;

import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import com.timekeeping.repositories.WorkScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortableJdbcQueryTest {

    @Mock
    private DTRDailyRepository dtrDailyRepository;
    @Mock
    private DTRSegmentRepository dtrSegmentRepository;
    @Mock
    private WorkScheduleRepository workScheduleRepository;
    @Mock
    private JdbcTemplate jdbc;
    @Mock
    private DataSource dataSource;

    @Test
    void bulkDtrQueryNormalizesCrossModuleIdsAndUnionBooleanType() {
        DTRDailyServiceImpl service = new DTRDailyServiceImpl(
                dtrDailyRepository,
                dtrSegmentRepository,
                workScheduleRepository,
                jdbc,
                dataSource
        );
        when(jdbc.query(
                anyString(),
                any(Object[].class),
                org.mockito.ArgumentMatchers.<RowMapper<Map<String, Object>>>any()
        )).thenReturn(List.of());

        service.getBulkDtrSummary(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(
                sqlCaptor.capture(),
                any(Object[].class),
                org.mockito.ArgumentMatchers.<RowMapper<Map<String, Object>>>any()
        );
        String sql = sqlCaptor.getValue().toLowerCase();
        assertThat(sql)
                .contains("cast(e.employeeid as varchar(64))")
                .contains("cast(ws.isdayoff as varchar(5))")
                .doesNotContain("cast(d.employee_id as int)");
    }

    @Test
    void signatoryQueryUsesPortableWindowFunctionInsteadOfOuterApply() {
        WorkScheduleImpl service = new WorkScheduleImpl(
                workScheduleRepository,
                jdbc,
                dataSource
        );
        when(jdbc.query(
                anyString(),
                org.mockito.ArgumentMatchers.<ResultSetExtractor<Map<String, String>>>any(),
                any(Object[].class)
        )).thenReturn(Map.of());

        service.getWorkScheduleSignatoryInfo("123");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(
                sqlCaptor.capture(),
                org.mockito.ArgumentMatchers.<ResultSetExtractor<Map<String, String>>>any(),
                any(Object[].class)
        );
        String sql = sqlCaptor.getValue().toLowerCase();
        assertThat(sql)
                .contains("row_number() over")
                .contains("coalesce(")
                .contains("on ea.employeeid = e.employeeid")
                .contains("where e.employeeid = ?")
                .doesNotContain("outer apply")
                .doesNotContain("top ")
                .doesNotContain("try_cast")
                .doesNotContain("isnull(");
    }
}
