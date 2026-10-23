package com.humanresource.saln;

public final class SalnTypes {
    private SalnTypes() {}

    public enum FilingType { ASSUMPTION, ANNUAL, SEPARATION }
    public enum Status { DRAFT, SUBMITTED, UNDER_REVIEW, FOR_CORRECTION, RESUBMITTED, COMPLIANT, LOCKED, VOIDED }
    public enum SourceType { ONLINE, PAPER, LEGACY_MIGRATION, ADMIN_ENCODED }
    public enum FilingMode { JOINT, SEPARATE, NOT_APPLICABLE }
    public enum OwnerType { DECLARANT, SPOUSE, CHILD }
    public enum RequirementStatus { NOT_FILED, DRAFT, FILED, OVERDUE, WAIVED }
}
