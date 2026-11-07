package com.primehr.notification;

import com.primehr.notification.application.NotificationProjectionService;
import com.primehr.notification.application.NotificationService;
import com.primehr.notification.domain.PrimeHrNotification;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationProjectionServiceTest {
    @Test
    void replacementClosesPriorActionsBeforePublishingTheNewAssignment() {
        NotificationService notifications = mock(NotificationService.class);
        var service = new NotificationProjectionService(notifications);
        var projection = new NotificationProjectionService.Projection("A", "emp-2",
                PrimeHrNotification.SourceDomain.PERFORMANCE, "SPMS_COMMITMENT_VERSION", "case-1", 4, null);

        service.replaceAssignment(projection, "correlation");

        var close = new NotificationService.CloseCommand("A", PrimeHrNotification.SourceDomain.PERFORMANCE,
                "SPMS_COMMITMENT_VERSION", "case-1", 4);
        var command = ArgumentCaptor.forClass(NotificationService.PublishCommand.class);
        var ordered = inOrder(notifications);
        ordered.verify(notifications).closeActions(close, "correlation");
        ordered.verify(notifications).publish(command.capture(), eq("correlation"));
        assertThat(command.getValue().eventCode()).isEqualTo(PrimeHrNotification.EventCode.ACTION_ASSIGNED);
        assertThat(command.getValue().eventKey())
                .startsWith("projection:performance:spms_commitment_version:case-1:4:action_assigned:")
                .doesNotContain("EMP-2", "emp-2");
    }

    @Test
    void outcomeClosesActionAndPublishesInformationOnly() {
        NotificationService notifications = mock(NotificationService.class);
        var service = new NotificationProjectionService(notifications);
        var projection = new NotificationProjectionService.Projection("A", "EMP-1",
                PrimeHrNotification.SourceDomain.EVIDENCE, "EVIDENCE_ASSESSMENT_CASE", "e-1", 7, null);

        service.outcome(projection, null);

        var command = ArgumentCaptor.forClass(NotificationService.PublishCommand.class);
        verify(notifications).closeActions(any(), isNull());
        verify(notifications).publish(command.capture(), isNull());
        assertThat(command.getValue().eventCode()).isEqualTo(PrimeHrNotification.EventCode.OUTCOME_AVAILABLE);
    }
}
