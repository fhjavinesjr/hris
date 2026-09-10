package com.humanresource.integration.primehr;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PerformanceParticipantIntegrationControllerTest {
    @Test
    void authorizesAgencyWideAccessBeforeReturningAnExactParticipant() {
        AssessmentSubjectIntegrationService subjects = mock(AssessmentSubjectIntegrationService.class);
        PrimeHrPerformanceParticipantAuthorization authorization = mock(PrimeHrPerformanceParticipantAuthorization.class);
        AssessmentSubjectResponse expected = mock(AssessmentSubjectResponse.class);
        when(subjects.getByEmployeeNo("001")).thenReturn(expected);
        var controller = new PerformanceParticipantIntegrationController(subjects, authorization);

        var result = controller.byEmployeeNo("Bearer service-token", "001");

        assertThat(result).isSameAs(expected);
        var order = inOrder(authorization, subjects);
        order.verify(authorization).requireAgencyWide("Bearer service-token");
        order.verify(subjects).getByEmployeeNo("001");
    }
}
