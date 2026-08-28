package com.humanresource.integration.primehr;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AssessmentSubjectIntegrationControllerTest {
    @Test
    void directUnauthorizedCallFailsBeforeEmployeeDataIsRead() {
        AssessmentSubjectIntegrationService service = mock(AssessmentSubjectIntegrationService.class);
        PrimeHrSubjectAuthorization authorization = mock(PrimeHrSubjectAuthorization.class);
        doThrow(new AccessDeniedException("denied")).when(authorization).requireAgencyWideAccess("Bearer denied");
        AssessmentSubjectIntegrationController controller =
                new AssessmentSubjectIntegrationController(service, authorization);

        assertThatThrownBy(() -> controller.list("Bearer denied", null, 0, 20, true))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(service);
    }
}
