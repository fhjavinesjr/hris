package com.primehr.notification.application;

import com.primehr.notification.domain.PrimeHrNotification;
import com.primehr.performancemanagement.domain.PerformanceCommitmentRoute;
import com.primehr.performancemanagement.domain.PerformanceCommitmentVersion;
import com.primehr.performancemanagement.infrastructure.PerformanceCommitmentRouteRepository;
import com.primehr.performancemanagement.infrastructure.PerformanceCommitmentRouteStepRepository;
import com.primehr.performancemanagement.infrastructure.PerformanceCommitmentVersionRepository;
import org.springframework.stereotype.Component;

@Component
public class PerformanceNotificationSourceValidator implements NotificationSourceValidator {
    private static final String TYPE = "SPMS_COMMITMENT_VERSION";
    private final PerformanceCommitmentVersionRepository versions;
    private final PerformanceCommitmentRouteRepository routes;
    private final PerformanceCommitmentRouteStepRepository steps;

    public PerformanceNotificationSourceValidator(PerformanceCommitmentVersionRepository versions,
                                                  PerformanceCommitmentRouteRepository routes,
                                                  PerformanceCommitmentRouteStepRepository steps) {
        this.versions = versions;
        this.routes = routes;
        this.steps = steps;
    }

    @Override
    public boolean supports(PrimeHrNotification value) {
        return value.getSourceDomain() == PrimeHrNotification.SourceDomain.PERFORMANCE
                && TYPE.equals(value.getSourceAggregateType());
    }

    @Override
    public Validation validate(PrimeHrNotification notice) {
        var found = versions.findByIdAndAgencyId(notice.getSourceAggregateId(), notice.getAgencyId());
        if (found.isEmpty()) return Validation.unavailable(notice.getSourceRecordVersion());
        PerformanceCommitmentVersion value = found.get();
        boolean available = switch (notice.getEventCode()) {
            case ACTION_ASSIGNED, DUE_REMINDER -> routes.findFirstByAgencyIdAndCommitmentVersionIdOrderByRouteRevisionDesc(
                    notice.getAgencyId(), value.getId()).filter(route -> route.getStatus() == PerformanceCommitmentRoute.Status.ACTIVE)
                    .flatMap(route -> steps.findByAgencyIdAndRouteIdAndLevel(notice.getAgencyId(), route.getId(),
                            route.getCurrentLevel())).filter(step -> step.getEmployeeNo().equalsIgnoreCase(
                            notice.getRecipientEmployeeNo())).isPresent();
            case RECORD_RETURNED -> value.getStatus() == PerformanceCommitmentVersion.Status.RETURNED
                    && value.getOwnerEmployeeNo().equalsIgnoreCase(notice.getRecipientEmployeeNo());
            case OUTCOME_AVAILABLE -> (value.getStatus() == PerformanceCommitmentVersion.Status.APPROVED
                    || value.getStatus() == PerformanceCommitmentVersion.Status.REJECTED)
                    && value.getOwnerEmployeeNo().equalsIgnoreCase(notice.getRecipientEmployeeNo());
        };
        return available ? Validation.available(value.getVersion()) : Validation.unavailable(value.getVersion());
    }
}
