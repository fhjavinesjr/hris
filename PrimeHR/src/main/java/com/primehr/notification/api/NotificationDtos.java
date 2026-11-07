package com.primehr.notification.api;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class NotificationDtos {
    private NotificationDtos() { }

    public record NotificationResponse(
            String id,
            long recordVersion,
            String sourceDomain,
            String eventCode,
            String kind,
            String sourceAggregateType,
            String sourceAggregateId,
            long sourceRecordVersion,
            String routeKey,
            String title,
            String message,
            Instant dueAt,
            String actionState,
            Instant readAt,
            Instant dismissedAt,
            Instant createdAt
    ) { }

    public record UnreadCountResponse(long unreadCount) { }
    public record MarkRequest(@NotNull Long recordVersion) { }
    public record MarkAllReadResponse(int updated, Instant readAt) { }
    public record TargetResolutionResponse(boolean available, String routeKey,
                                           String sourceAggregateType, String sourceAggregateId,
                                           String actionState) { }
}
