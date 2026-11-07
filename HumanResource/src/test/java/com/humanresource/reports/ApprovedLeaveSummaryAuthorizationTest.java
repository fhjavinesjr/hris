package com.humanresource.reports;

import com.humanresource.controllers.ApprovedLeaveSummaryController;
import com.humanresource.onboarding.HrmPermissionGuard;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ApprovedLeaveSummaryAuthorizationTest {
    @Test
    void deniesPdfBeforeReadingLeaveDataWhenAgencyWidePermissionIsAbsent() {
        ApprovedLeaveSummaryService service = mock(ApprovedLeaveSummaryService.class);
        HrmPermissionGuard guard = mock(HrmPermissionGuard.class);
        doThrow(new AccessDeniedException("denied")).when(guard)
                .require("Bearer test", "hrm.reports.leaveSummary", HrmPermissionGuard.Action.ACCESS);
        ApprovedLeaveSummaryController controller = new ApprovedLeaveSummaryController(service, guard);
        assertThatThrownBy(() -> controller.print("Bearer test", LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), 1L, null, null, "ALL"))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(service);
    }
}
