package com.humanresource.onboarding;

import java.util.List;

public final class AppointmentReportData {
    private AppointmentReportData() {}

    public record LegalDocument(
            String agencyName, String agencyAddress, String appointeeName, String appointeeAddress,
            String position, String office, String governmentIdType, String governmentIdNumber,
            String governmentIdDate, String issueDate, String oathDate, String assumptionDate,
            String venue, String administeringName, String administeringPosition,
            String certifyingName, String certifyingPosition, String attestingName,
            String attestingPosition, String templateCode, String templateVersion,
            String sourceFingerprint, String generated) {}

    public record OnboardingCompletion(
            String agencyName, String agencyAddress, String intakeId, String handoffId,
            String selectionId, String applicationId, String employeeReference,
            String appointmentReference, String templateReference, String completedAt,
            String sourceFingerprint, String generated, List<CompletionRow> rows) {}

    public static final class CompletionRow {
        private final int number;
        private final String code;
        private final String label;
        private final String status;
        private final String evidenceReference;
        private final String completedBy;
        private final String verifiedBy;

        public CompletionRow(int number, String code, String label, String status,
                             String evidenceReference, String completedBy, String verifiedBy) {
            this.number = number;
            this.code = code;
            this.label = label;
            this.status = status;
            this.evidenceReference = evidenceReference;
            this.completedBy = completedBy;
            this.verifiedBy = verifiedBy;
        }

        public int getNumber() { return number; }
        public String getCode() { return code; }
        public String getLabel() { return label; }
        public String getStatus() { return status; }
        public String getEvidenceReference() { return evidenceReference; }
        public String getCompletedBy() { return completedBy; }
        public String getVerifiedBy() { return verifiedBy; }
    }
}
