package com.humanresource.reports;

import com.humanresource.entitymodels.LeaveApplication;
import com.humanresource.repositories.LeaveApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApprovedLeaveSummaryServiceTest {
    @Test
    void paidFilterUsesApprovedFilingTreatmentIncludingLegacyNull() {
        LeaveApplication paid = leave("Vacation Leave", true);
        LeaveApplication unpaid = leave("Sick Leave", false);
        LeaveApplication legacy = leave("Vacation Leave", null);
        assertThat(ApprovedLeaveSummaryService.matches(paid, "Vacation Leave", "WITH_PAY")).isTrue();
        assertThat(ApprovedLeaveSummaryService.matches(legacy, "Vacation Leave", "WITH_PAY")).isTrue();
        assertThat(ApprovedLeaveSummaryService.matches(unpaid, "", "WITHOUT_PAY")).isTrue();
        assertThat(ApprovedLeaveSummaryService.matches(unpaid, "", "WITH_PAY")).isFalse();
        assertThat(ApprovedLeaveSummaryService.matches(paid, "Sick Leave", "ALL")).isFalse();
    }

    @Test
    void readsOnlyFinalApprovedOverlappingFilingsAndPrintsPdf() throws Exception {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:approved_leave_summary_service;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE areas (areasId BIGINT PRIMARY KEY, areasName VARCHAR(100))");
        jdbc.execute("CREATE TABLE businessunits (businessUnitsId BIGINT PRIMARY KEY, areasId BIGINT, businessUnitsName VARCHAR(100))");
        jdbc.execute("CREATE TABLE manage_personnel (employeeId BIGINT, areaId BIGINT, businessUnitId BIGINT)");
        jdbc.execute("CREATE TABLE settings (settingsId BIGINT PRIMARY KEY, companyName VARCHAR(100))");
        jdbc.execute("CREATE TABLE employee (employeeId BIGINT PRIMARY KEY, employeeNo VARCHAR(100), firstname VARCHAR(100), lastname VARCHAR(100))");
        jdbc.update("INSERT INTO areas VALUES (?, ?)", 10L, "Hospital");
        jdbc.update("INSERT INTO businessunits VALUES (?, ?, ?)", 20L, 10L, "Ward A");
        jdbc.update("INSERT INTO manage_personnel VALUES (?, ?, ?)", 1L, 10L, 20L);
        jdbc.update("INSERT INTO settings VALUES (?, ?)", 1L, "Sample Agency");
        jdbc.update("INSERT INTO employee VALUES (?, ?, ?, ?)", 1L, "0001", "Sample", "Employee");
        LeaveApplicationRepository repository = mock(LeaveApplicationRepository.class);
        LeaveApplication filing = leave("Vacation Leave", true);
        filing.setEmployeeId(1L);
        filing.setStartDate(LocalDate.of(2026, 9, 30));
        filing.setEndDate(LocalDate.of(2026, 10, 2));
        filing.setNoOfDays(3.0);
        when(repository.findByStatusAndApprovedStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                "Approved", "Approved", LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1)))
                .thenReturn(List.of(filing));
        ApprovedLeaveSummaryService service = new ApprovedLeaveSummaryService(repository, jdbc);
        byte[] pdf = service.generate(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                10L, 20L, "Vacation Leave", "WITH_PAY");
        assertThat(pdf).startsWith("%PDF".getBytes());
        assertThatThrownBy(() -> service.generate(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                10L, 999L, "", "ALL")).isInstanceOf(IllegalArgumentException.class);
    }

    private static LeaveApplication leave(String type, Boolean paid) {
        LeaveApplication value = new LeaveApplication();
        value.setLeaveType(type);
        value.setWithPay(paid);
        return value;
    }
}
