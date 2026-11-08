package com.primehr.notification.api;

import com.primehr.notification.api.NotificationDtos.MarkAllReadResponse;
import com.primehr.notification.api.NotificationDtos.MarkRequest;
import com.primehr.notification.api.NotificationDtos.NotificationResponse;
import com.primehr.notification.api.NotificationDtos.UnreadCountResponse;
import com.primehr.notification.api.NotificationDtos.TargetResolutionResponse;
import com.primehr.notification.application.NotificationService;
import com.primehr.security.AgencyScopeResolver;
import com.primehr.shared.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/primehr/v1/notifications/me")
public class NotificationController {
    private final NotificationService service;
    private final AgencyScopeResolver agency;

    public NotificationController(NotificationService service, AgencyScopeResolver agency) {
        this.service = service;
        this.agency = agency;
    }

    @GetMapping
    public PageResponse<NotificationResponse> feed(Authentication authentication,
                                                   @RequestParam(defaultValue = "false") boolean unreadOnly,
                                                   @RequestParam(defaultValue = "false") boolean includeDismissed,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.feed(agency.resolveAgencyId(authentication), actor(authentication),
                unreadOnly, includeDismissed, page, size);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(Authentication authentication) {
        return new UnreadCountResponse(service.unreadCount(
                agency.resolveAgencyId(authentication), actor(authentication)));
    }

    @PutMapping("/{id}/read")
    public NotificationResponse markRead(Authentication authentication,
                                         @PathVariable String id,
                                         @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                         @Valid @RequestBody MarkRequest request) {
        return service.markRead(agency.resolveAgencyId(authentication), actor(authentication), id,
                request.recordVersion(), correlationId);
    }

    @PutMapping("/read-all")
    public MarkAllReadResponse markAllRead(Authentication authentication,
                                           @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return service.markAllRead(agency.resolveAgencyId(authentication), actor(authentication), correlationId);
    }

    @PutMapping("/{id}/dismiss")
    public NotificationResponse dismiss(Authentication authentication,
                                        @PathVariable String id,
                                        @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                                        @Valid @RequestBody MarkRequest request) {
        return service.dismiss(agency.resolveAgencyId(authentication), actor(authentication), id,
                request.recordVersion(), correlationId);
    }

    @GetMapping("/{id}/target")
    public TargetResolutionResponse resolveTarget(Authentication authentication,
                                                  @PathVariable String id,
                                                  @RequestHeader(value = "X-Correlation-Id", required = false)
                                                  String correlationId) {
        return service.resolveTarget(agency.resolveAgencyId(authentication), actor(authentication), id,
                correlationId);
    }

    private static String actor(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Authenticated employee identity is required");
        }
        return authentication.getName();
    }
}
