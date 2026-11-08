package com.primehr.notification.application;

import com.primehr.evidence.domain.EvidenceAssessmentCase;
import com.primehr.evidence.domain.EvidenceCorrectiveAction;
import com.primehr.evidence.infrastructure.EvidenceAssessmentCaseRepository;
import com.primehr.evidence.infrastructure.EvidenceCorrectiveActionRepository;
import com.primehr.notification.domain.PrimeHrNotification;
import org.springframework.stereotype.Component;

@Component
public class EvidenceNotificationSourceValidator implements NotificationSourceValidator {
    private final EvidenceAssessmentCaseRepository cases;
    private final EvidenceCorrectiveActionRepository actions;

    public EvidenceNotificationSourceValidator(EvidenceAssessmentCaseRepository cases,
                                               EvidenceCorrectiveActionRepository actions) {
        this.cases = cases;
        this.actions = actions;
    }

    @Override
    public boolean supports(PrimeHrNotification value) {
        return value.getSourceDomain() == PrimeHrNotification.SourceDomain.EVIDENCE
                && ("EVIDENCE_ASSESSMENT_CASE".equals(value.getSourceAggregateType())
                || "EVIDENCE_CORRECTIVE_ACTION".equals(value.getSourceAggregateType()));
    }

    @Override
    public Validation validate(PrimeHrNotification notice) {
        return "EVIDENCE_ASSESSMENT_CASE".equals(notice.getSourceAggregateType())
                ? validateCase(notice) : validateAction(notice);
    }

    private Validation validateCase(PrimeHrNotification notice) {
        var found = cases.findByIdAndAgencyId(notice.getSourceAggregateId(), notice.getAgencyId());
        if (found.isEmpty()) return Validation.unavailable(notice.getSourceRecordVersion());
        var value = found.get();
        boolean submitted = value.getStatus() == EvidenceAssessmentCase.Status.SUBMITTED
                || value.getStatus() == EvidenceAssessmentCase.Status.RESUBMITTED;
        boolean available = switch (notice.getEventCode()) {
            case ACTION_ASSIGNED, DUE_REMINDER -> submitted && !value.isValidatorRecused()
                    && value.getValidatorEmployeeNo().equalsIgnoreCase(notice.getRecipientEmployeeNo());
            case RECORD_RETURNED -> value.getStatus() == EvidenceAssessmentCase.Status.RETURNED
                    && value.getOwnerEmployeeNo().equalsIgnoreCase(notice.getRecipientEmployeeNo());
            case OUTCOME_AVAILABLE -> value.getStatus() == EvidenceAssessmentCase.Status.FINALIZED
                    && value.getOwnerEmployeeNo().equalsIgnoreCase(notice.getRecipientEmployeeNo());
        };
        return available ? Validation.available(value.getVersion()) : Validation.unavailable(value.getVersion());
    }

    private Validation validateAction(PrimeHrNotification notice) {
        var found = actions.findByIdAndAgencyId(notice.getSourceAggregateId(), notice.getAgencyId());
        if (found.isEmpty()) return Validation.unavailable(notice.getSourceRecordVersion());
        EvidenceCorrectiveAction value = found.get();
        var parent = cases.findByIdAndAgencyId(value.getAssessmentCaseId(), notice.getAgencyId()).orElse(null);
        boolean responsible = value.getResponsibleEmployeeNo().equalsIgnoreCase(notice.getRecipientEmployeeNo());
        boolean available = switch (notice.getEventCode()) {
            case ACTION_ASSIGNED, DUE_REMINDER -> switch (value.getStatus()) {
                case PLANNED, IN_PROGRESS, RETURNED -> responsible;
                case SUBMITTED_FOR_VERIFICATION -> parent != null && parent.getValidatorEmployeeNo()
                        .equalsIgnoreCase(notice.getRecipientEmployeeNo()) && !parent.isValidatorRecused();
                default -> false;
            };
            case RECORD_RETURNED -> value.getStatus() == EvidenceCorrectiveAction.Status.RETURNED && responsible;
            case OUTCOME_AVAILABLE -> (value.getStatus() == EvidenceCorrectiveAction.Status.VERIFIED
                    || value.getStatus() == EvidenceCorrectiveAction.Status.CANCELLED) && responsible;
        };
        return available ? Validation.available(value.getVersion()) : Validation.unavailable(value.getVersion());
    }
}
