package com.primehr.rsp.screening.application;

import com.primehr.integration.administrative.*;
import com.primehr.integration.humanresource.*;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ScreeningAssignmentEligibilityServiceTest {
    private HumanResourceEmployeeDirectoryClient employees;
    private AdministrativeAuthorizationClient authorization;
    private ScreeningAssignmentEligibilityService service;

    @BeforeEach void setUp() {
        employees = mock(HumanResourceEmployeeDirectoryClient.class);
        authorization = mock(AdministrativeAuthorizationClient.class);
        service = new ScreeningAssignmentEligibilityService(employees, authorization);
        when(employees.require("SCR-1", "Bearer test")).thenReturn(new HumanResourceEmployeeDirectoryEntry(1L, "SCR-1", "Screen", "User", null, "SCREENER"));
        when(employees.require("VAL-1", "Bearer test")).thenReturn(new HumanResourceEmployeeDirectoryEntry(2L, "VAL-1", "Validate", "User", null, "VALIDATOR"));
    }

    @Test void acceptsDistinctAgencyWideEmployeesWithExactActionPermissions() {
        when(authorization.resolveEmployee(anyString(), eq("SCR-1"), eq("SCREENER"), anyString())).thenReturn(permission(false, true, true, true, false));
        when(authorization.resolveEmployee(anyString(), eq("VAL-1"), eq("VALIDATOR"), anyString())).thenReturn(permission(false, true, false, false, true));
        service.validate("SCR-1", "VAL-1", "Bearer test");
        verify(authorization, times(2)).resolveEmployee(anyString(), anyString(), anyString(), eq("Bearer test"));
    }

    @Test void rejectsSameEmployeeForBothSegregatedDuties() {
        assertThatThrownBy(() -> service.validate("SCR-1", "scr-1", "Bearer test"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("different employees");
        verifyNoInteractions(employees, authorization);
    }

    @Test void rejectsScreenerWithoutEditAndSubmitPermissions() {
        when(authorization.resolveEmployee(anyString(), eq("SCR-1"), anyString(), anyString())).thenReturn(permission(false, true, true, false, false));
        assertThatThrownBy(() -> service.validate("SCR-1", "VAL-1", "Bearer test"))
                .isInstanceOf(AccessDeniedException.class).hasMessageContaining("screener");
    }

    @Test void rejectsValidatorWithoutApprovePermission() {
        when(authorization.resolveEmployee(anyString(), eq("SCR-1"), anyString(), anyString())).thenReturn(permission(false, true, true, true, false));
        when(authorization.resolveEmployee(anyString(), eq("VAL-1"), anyString(), anyString())).thenReturn(permission(false, true, false, false, false));
        assertThatThrownBy(() -> service.validate("SCR-1", "VAL-1", "Bearer test"))
                .isInstanceOf(AccessDeniedException.class).hasMessageContaining("validator");
    }

    private EffectiveFeaturePermission permission(boolean admin, boolean access, boolean edit, boolean submit, boolean approve) {
        return new EffectiveFeaturePermission("primehr.rsp-application-screening", admin, access, false, edit, false, false, submit, approve, false, false, false, PermissionDataScope.AGENCY_WIDE);
    }
}
