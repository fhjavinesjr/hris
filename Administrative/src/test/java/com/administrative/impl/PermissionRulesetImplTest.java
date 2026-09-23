package com.administrative.impl;

import com.administrative.entitymodels.PermissionRuleset;
import com.administrative.repositories.PermissionRulesetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionRulesetImplTest {

    private final PermissionRulesetRepository repository = mock(PermissionRulesetRepository.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final PermissionRulesetImpl service = new PermissionRulesetImpl(repository, jdbcTemplate);

    @Test
    void blankRoleHasNoRuleset() {
        assertTrue(service.resolveByRole("  ").isEmpty());
    }

    @Test
    void resolvesRoleNameWithoutRolePrefix() {
        PermissionRuleset ruleset = new PermissionRuleset("EMPLOYEE", false, "{}", "{}");
        when(repository.findByPermissionNameIgnoreCase("EMPLOYEE")).thenReturn(Optional.of(ruleset));

        assertEquals("EMPLOYEE", service.resolveByRole("ROLE_EMPLOYEE").orElseThrow().getPermissionName());
    }

    @Test
    void legacyNullTextHasNoRuleset() {
        assertTrue(service.resolveByRole(" null ").isEmpty());
    }

    @Test
    void resolvesCurrentEmployeeRoleFromTheDatabaseInsteadOfTheJwt() {
        PermissionRuleset ruleset = new PermissionRuleset("EMPLOYEE", false, "{}", "{}");
        ruleset.setPermissionId(2L);
        when(jdbcTemplate.query(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<String>>any(),
                org.mockito.ArgumentMatchers.eq("EMP-007")))
                .thenReturn(List.of("2"));
        when(repository.findById(2L)).thenReturn(Optional.of(ruleset));

        assertEquals("EMPLOYEE", service.resolveForEmployee("EMP-007").orElseThrow().getPermissionName());
    }

    @Test
    void currentEmployeeLegacyRoleNameIsNotAConfiguredDropdownAssignment() {
        when(jdbcTemplate.query(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<String>>any(),
                org.mockito.ArgumentMatchers.eq("EMP-007")))
                .thenReturn(List.of("ADMIN"));

        assertTrue(service.resolveForEmployee("EMP-007").isEmpty());
    }
}
