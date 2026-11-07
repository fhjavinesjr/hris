package com.primehr.learningdevelopment.completion.domain;

import com.primehr.learningdevelopment.participation.domain.LdEnrollment;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;

@Entity
@Table(name = "ld_completion_case", uniqueConstraints =
        @UniqueConstraint(name = "uk_ld_completion_enrollment", columnNames = {"agency_id", "enrollment_id"}))
public class LdCompletionCase extends RspAuditedEntity {
    public enum Status { DRAFT, FOR_VERIFICATION, COMPLETED, NOT_COMPLETED }
    public enum Decision { COMPLETED, NOT_COMPLETED }

    @Column(name = "enrollment_id", nullable = false, length = 36) private String enrollmentId;
    @Column(name = "training_order_id", nullable = false, length = 36) private String trainingOrderId;
    @Column(name = "training_order_fingerprint", nullable = false, length = 64) private String trainingOrderFingerprint;
    @Column(name = "offering_id", nullable = false, length = 36) private String offeringId;
    @Column(name = "offering_version", nullable = false) private int offeringVersion;
    @Column(name = "offering_fingerprint", nullable = false, length = 64) private String offeringFingerprint;
    @Column(name = "employee_id", nullable = false) private Long employeeId;
    @Column(name = "employee_no", nullable = false, length = 100) private String employeeNo;
    @Column(name = "employee_name", nullable = false, length = 300) private String employeeName;
    @Column(name = "appointment_id", nullable = false) private Long appointmentId;
    @Column(name = "employee_fingerprint", nullable = false, length = 64) private String employeeFingerprint;
    @Column(name = "planned_hours", nullable = false, precision = 9, scale = 2) private BigDecimal plannedHours;
    @Column(name = "credited_hours", nullable = false, precision = 9, scale = 2) private BigDecimal creditedHours;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private Status status;
    @Column(name = "content_revision", nullable = false) private int contentRevision;
    @Column(name = "submitted_by", length = 100) private String submittedBy;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "verified_by", length = 100) private String verifiedBy;
    @Column(name = "verified_at") private Instant verifiedAt;
    @Column(name = "completion_date") private LocalDate completionDate;
    @Column(name = "decision_reason", length = 3000) private String decisionReason;
    @Column(name = "completion_fingerprint", length = 64) private String completionFingerprint;

    protected LdCompletionCase() {}

    public LdCompletionCase(String agency, LdEnrollment enrollment, String trainingOrderId,
            String trainingOrderFingerprint, BigDecimal plannedHours) {
        super(agency);
        if (enrollment == null || enrollment.getStatus() != LdEnrollment.Status.CONFIRMED) {
            throw new IllegalArgumentException("A confirmed enrollment is required");
        }
        this.enrollmentId = requiredText(enrollment.getId(), "enrollmentId");
        this.trainingOrderId = requiredText(trainingOrderId, "trainingOrderId");
        this.trainingOrderFingerprint = requiredText(trainingOrderFingerprint, "trainingOrderFingerprint");
        offeringId = requiredText(enrollment.getOfferingId(), "offeringId");
        offeringVersion = enrollment.getOfferingVersion();
        offeringFingerprint = requiredText(enrollment.getOfferingFingerprint(), "offeringFingerprint");
        employeeId = positive(enrollment.getEmployeeId(), "employeeId");
        employeeNo = requiredText(enrollment.getEmployeeNo(), "employeeNo");
        employeeName = requiredText(enrollment.getEmployeeName(), "employeeName");
        appointmentId = positive(enrollment.getAppointmentId(), "appointmentId");
        employeeFingerprint = requiredText(enrollment.getEmployeeFingerprint(), "employeeFingerprint");
        if (plannedHours == null || plannedHours.signum() <= 0) throw new IllegalArgumentException("plannedHours must be positive");
        this.plannedHours = plannedHours.setScale(2, RoundingMode.HALF_UP);
        creditedHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        status = Status.DRAFT;
        contentRevision = 1;
    }

    public void touch() { editable(); contentRevision++; }
    public void submit(String actor, Instant at) {
        editable(); status = Status.FOR_VERIFICATION; submittedBy = requiredText(actor, "actor"); submittedAt = required(at, "submittedAt");
    }
    public void returnForChanges(String reason) {
        if (status != Status.FOR_VERIFICATION) throw new IllegalStateException("Only a case for verification may be returned");
        status = Status.DRAFT; decisionReason = requiredText(reason, "reason"); contentRevision++;
    }
    public void finalizeDecision(Decision decision, BigDecimal credited, LocalDate date, String reason,
            String actor, Instant at, String fingerprint) {
        if (status != Status.FOR_VERIFICATION) throw new IllegalStateException("Only a case for verification may be finalized");
        if (submittedBy != null && submittedBy.equalsIgnoreCase(actor)) throw new IllegalArgumentException("Submitter cannot verify the same completion case");
        if (decision == null) throw new IllegalArgumentException("decision is required");
        if (decision == Decision.COMPLETED) {
            if (credited == null || credited.signum() <= 0 || credited.compareTo(plannedHours) > 0) {
                throw new IllegalArgumentException("Credited hours must be positive and cannot exceed planned hours");
            }
            creditedHours = credited.setScale(2, RoundingMode.HALF_UP);
            status = Status.COMPLETED;
        } else {
            creditedHours = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            status = Status.NOT_COMPLETED;
        }
        completionDate = required(date, "completionDate");
        decisionReason = requiredText(reason, "reason");
        verifiedBy = requiredText(actor, "actor");
        verifiedAt = required(at, "verifiedAt");
        completionFingerprint = requiredText(fingerprint, "completionFingerprint");
    }

    private void editable() { if (status != Status.DRAFT) throw new IllegalStateException("Only a DRAFT completion case may be changed"); }
    private static Long positive(Long value, String name) { if (value == null || value < 1) throw new IllegalArgumentException(name + " is required"); return value; }
    private static <T> T required(T value, String name) { if (value == null) throw new IllegalArgumentException(name + " is required"); return value; }

    public String getEnrollmentId(){return enrollmentId;} public String getTrainingOrderId(){return trainingOrderId;}
    public String getTrainingOrderFingerprint(){return trainingOrderFingerprint;} public String getOfferingId(){return offeringId;}
    public int getOfferingVersion(){return offeringVersion;} public String getOfferingFingerprint(){return offeringFingerprint;}
    public Long getEmployeeId(){return employeeId;} public String getEmployeeNo(){return employeeNo;}
    public String getEmployeeName(){return employeeName;} public Long getAppointmentId(){return appointmentId;}
    public String getEmployeeFingerprint(){return employeeFingerprint;} public BigDecimal getPlannedHours(){return plannedHours;}
    public BigDecimal getCreditedHours(){return creditedHours;} public Status getStatus(){return status;}
    public int getContentRevision(){return contentRevision;} public String getSubmittedBy(){return submittedBy;}
    public Instant getSubmittedAt(){return submittedAt;} public String getVerifiedBy(){return verifiedBy;}
    public Instant getVerifiedAt(){return verifiedAt;} public LocalDate getCompletionDate(){return completionDate;}
    public String getDecisionReason(){return decisionReason;} public String getCompletionFingerprint(){return completionFingerprint;}
}
