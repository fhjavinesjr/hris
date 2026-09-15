package com.humanresource.onboarding;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class Phase5eSecurityContractTest {
    @Test void legacyEmployeeCreationBootstrapAndAppointmentMutationsArePermissionProtected() throws Exception {
        String security=Files.readString(Path.of("src/main/java/com/humanresource/configs/HumanResourceSecurityConfig.java"));
        assertTrue(security.contains("\"/api/employee/register\", \"/api/hris/installAuth\").hasAuthority(\"1\")"));
        assertFalse(security.contains("\"/api/employeeAppointment/create\").hasAuthority(\"1\")"));
        String controller=Files.readString(Path.of("src/main/java/com/humanresource/controllers/EmployeeAppointmentController.java"));
        assertTrue(controller.contains("requireAction(token, EMPLOYMENT_RECORD, HrmPermissionGuard.Action.ADD)"));
        assertTrue(controller.contains("requireAction(token, EMPLOYMENT_RECORD, HrmPermissionGuard.Action.EDIT)"));
        assertTrue(controller.contains("requireAction(token, EMPLOYMENT_RECORD, HrmPermissionGuard.Action.DELETE)"));
        assertTrue(controller.contains("requireAction(token, EMPLOYMENT_RECORD, HrmPermissionGuard.Action.ACCESS)"));
        assertFalse(controller.contains("require(token, \"hrm.appointment-report\""));
        String filter=Files.readString(Path.of("../Common/src/main/java/com/hris/common/utilities/JwtFilter.java"));
        assertFalse(filter.contains("startsWith(\"/api/employee/register\")"));
        assertFalse(filter.contains("startsWith(\"/api/hris/installAuth\")"));
        assertTrue(filter.contains("startsWith(\"/api/employee/activate\")"));
    }

    @Test void phase5e3OpenApiIncludesEveryAuthorizedBackendBoundary() throws Exception {
        String api=Files.readString(Path.of("../contracts/openapi/humanresource-primehr-integration-v1.yaml"));
        for(String operation:new String[]{"createOnboardingTemplate","publishOnboardingTemplate","listAppointmentIntakes",
                "beginAppointmentIntakeReview","returnAppointmentIntake","resolveAppointmentIntakeIdentity",
                "updateOnboardingChecklistItem","approveAppointmentIntake","createAuthoritativeAppointment",
                "completeOnboarding","activateEmployeeAccount"})assertTrue(api.contains("operationId: "+operation),operation);
    }
}
