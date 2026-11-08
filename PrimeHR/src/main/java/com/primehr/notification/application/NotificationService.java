package com.primehr.notification.application;

import com.primehr.notification.api.NotificationDtos.MarkAllReadResponse;
import com.primehr.notification.api.NotificationDtos.NotificationResponse;
import com.primehr.notification.api.NotificationDtos.TargetResolutionResponse;
import com.primehr.notification.domain.PrimeHrNotification.EventCode;
import com.primehr.notification.domain.PrimeHrNotification.SourceDomain;
import com.primehr.shared.api.PageResponse;

import java.time.Instant;

public interface NotificationService {
    PublishResult publish(PublishCommand command, String correlationId);
    int closeActions(CloseCommand command, String correlationId);
    PageResponse<NotificationResponse> feed(String agencyId, String recipientEmployeeNo,
                                            boolean unreadOnly, boolean includeDismissed,
                                            int page, int size);
    long unreadCount(String agencyId, String recipientEmployeeNo);
    NotificationResponse markRead(String agencyId, String recipientEmployeeNo, String id,
                                  long recordVersion, String correlationId);
    MarkAllReadResponse markAllRead(String agencyId, String recipientEmployeeNo, String correlationId);
    NotificationResponse dismiss(String agencyId, String recipientEmployeeNo, String id,
                                 long recordVersion, String correlationId);
    TargetResolutionResponse resolveTarget(String agencyId, String recipientEmployeeNo, String id,
                                           String correlationId);

    record PublishCommand(String agencyId, String recipientEmployeeNo, SourceDomain sourceDomain,
                          EventCode eventCode, String sourceAggregateType, String sourceAggregateId,
                          long sourceRecordVersion, String eventKey, Instant dueAt) { }
    record PublishResult(NotificationResponse notification, boolean idempotentReplay) { }
    record CloseCommand(String agencyId, SourceDomain sourceDomain, String sourceAggregateType,
                        String sourceAggregateId, long sourceRecordVersion) { }
}
