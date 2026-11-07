package com.timekeeping.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MraReportAccessTest {
    @Test
    void grantsOnlyTheSelectedReportPermissionOrBuiltInAdministrator() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:mra_access;DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE permission_ruleset (permissionId BIGINT PRIMARY KEY, "
                + "permissionName VARCHAR(100), isAdministrator BOOLEAN, permissionData VARCHAR(2000))");
        TimekeepingReportAccess access = new TimekeepingReportAccess(jdbc, new ObjectMapper());
        var officer = new UsernamePasswordAuthenticationToken("officer", null,
                List.of(new SimpleGrantedAuthority("2")));
        assertThatThrownBy(() -> access.requireAccess(officer, "tk.reports.mra"))
                .isInstanceOf(AccessDeniedException.class);
        jdbc.update("INSERT INTO permission_ruleset VALUES (?, ?, ?, ?)",
                2L, "DTR Officer", false, "{\"tk.dtr\":{\"canAccess\":true}}");
        assertThatThrownBy(() -> access.requireAccess(officer, "tk.reports.mra"))
                .isInstanceOf(AccessDeniedException.class);
        jdbc.update("UPDATE permission_ruleset SET isAdministrator = ? WHERE permissionId = ?", true, 2L);
        assertThatThrownBy(() -> access.requireAccess(officer, "tk.reports.mra"))
                .isInstanceOf(AccessDeniedException.class);
        jdbc.update("UPDATE permission_ruleset SET permissionData = ? WHERE permissionId = ?",
                "{\"tk.reports.mra\":{\"canAccess\":true}}", 2L);
        assertThatCode(() -> access.requireAccess(officer, "tk.reports.mra")).doesNotThrowAnyException();
        assertThatThrownBy(() -> access.requireAccess(officer, "tk.reports.overtime"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> access.requireAccess(officer, "tk.reports.incompleteDtr"))
                .isInstanceOf(AccessDeniedException.class);
        jdbc.update("UPDATE permission_ruleset SET permissionData = ? WHERE permissionId = ?",
                "{\"tk.reports.incompleteDtr\":{\"canAccess\":true}}", 2L);
        assertThatCode(() -> access.requireAccess(officer, "tk.reports.incompleteDtr"))
                .doesNotThrowAnyException();
        var admin = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("1")));
        assertThatCode(() -> access.requireAccess(admin, "tk.reports.nightDifferential"))
                .doesNotThrowAnyException();
    }
}
