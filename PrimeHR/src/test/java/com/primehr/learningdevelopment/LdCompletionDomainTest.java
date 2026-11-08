package com.primehr.learningdevelopment;

import com.primehr.learningdevelopment.completion.domain.LdCompletionCase;
import com.primehr.learningdevelopment.completion.domain.LdCompletionEvidence;
import com.primehr.learningdevelopment.completion.domain.LdCompletionOutput;
import com.primehr.learningdevelopment.completion.domain.LdSessionAttendance;
import com.primehr.learningdevelopment.delivery.domain.LdOfferingSession;
import com.primehr.learningdevelopment.participation.domain.LdEnrollment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LdCompletionDomainTest {

    @Test
    void attendanceCapsCreditedHoursAndEnforcesAbsentSemantics() {
        LdOfferingSession session = session();
        LdSessionAttendance attendance = new LdSessionAttendance("AGENCY", "CASE-1", session);

        attendance.record(LdSessionAttendance.Status.PARTIAL, new BigDecimal("6"),
                new BigDecimal("5.50"), "Partial attendance", "RECORDER", Instant.parse("2026-05-11T00:00:00Z"));

        assertThat(attendance.getStatus()).isEqualTo(LdSessionAttendance.Status.PARTIAL);
        assertThat(attendance.getCreditedHours()).isEqualByComparingTo("5.50");
        assertThatThrownBy(() -> attendance.record(LdSessionAttendance.Status.PRESENT,
                new BigDecimal("9"), new BigDecimal("8"), null, "RECORDER", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("session limits");
        assertThatThrownBy(() -> attendance.record(LdSessionAttendance.Status.ABSENT,
                BigDecimal.ONE, BigDecimal.ZERO, null, "RECORDER", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Absent");
    }

    @Test
    void outputsRequireSubmissionBeforeVerificationAndWaiversRemainExplicit() {
        LdCompletionOutput output = new LdCompletionOutput("AGENCY", "CASE-1", "OUTPUT-1",
                "Action Plan", "Submit an action plan");

        assertThatThrownBy(() -> output.verify("VERIFIER", Instant.now()))
                .isInstanceOf(IllegalStateException.class);
        output.submitted("EMP-1", Instant.parse("2026-05-11T00:00:00Z"));
        output.verify("VERIFIER", Instant.parse("2026-05-12T00:00:00Z"));
        assertThat(output.getStatus()).isEqualTo(LdCompletionOutput.Status.VERIFIED);
        assertThatThrownBy(() -> output.waive("Late waiver", "VERIFIER", Instant.now()))
                .isInstanceOf(IllegalStateException.class);

        LdCompletionOutput waived = new LdCompletionOutput("AGENCY", "CASE-1", "OUTPUT-2",
                "Reflection", "Submit a reflection");
        waived.waive("Not required for this modality", "VERIFIER", Instant.now());
        assertThat(waived.getStatus()).isEqualTo(LdCompletionOutput.Status.WAIVED);
        assertThat(waived.getDecisionReason()).isNotBlank();
    }

    @Test
    void completionRequiresIndependentVerifierAndBecomesImmutable() {
        LdCompletionCase completion = completionCase();
        Instant submittedAt = Instant.parse("2026-05-11T00:00:00Z");
        completion.submit("SUBMITTER", submittedAt);

        assertThatThrownBy(() -> completion.finalizeDecision(LdCompletionCase.Decision.COMPLETED,
                new BigDecimal("8"), LocalDate.of(2026, 5, 10), "Completed", "SUBMITTER",
                Instant.now(), "fingerprint"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Submitter cannot verify");

        completion.finalizeDecision(LdCompletionCase.Decision.COMPLETED, new BigDecimal("8"),
                LocalDate.of(2026, 5, 10), "Requirements verified", "VERIFIER",
                Instant.parse("2026-05-12T00:00:00Z"), "completion-fingerprint");

        assertThat(completion.getStatus()).isEqualTo(LdCompletionCase.Status.COMPLETED);
        assertThat(completion.getCreditedHours()).isEqualByComparingTo("8.00");
        assertThatThrownBy(completion::touch).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void evidenceSeparatesOutputAndCertificateMetadata() {
        assertThatThrownBy(() -> new LdCompletionEvidence("AGENCY", "CASE-1", null,
                LdCompletionEvidence.Type.OUTPUT, "output.pdf", "filesystem", "key", "application/pdf",
                10, "checksum", null, null, null, "EMP-1", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("outputId");

        LdCompletionEvidence certificate = new LdCompletionEvidence("AGENCY", "CASE-1", null,
                LdCompletionEvidence.Type.CERTIFICATE, "certificate.pdf", "filesystem", "key",
                "application/pdf", 10, "checksum", "CERT-1", "CSC",
                LocalDate.of(2026, 5, 10), "EMP-1", Instant.now());
        assertThat(certificate.getCertificateNumber()).isEqualTo("CERT-1");
    }

    private LdCompletionCase completionCase() {
        LdEnrollment enrollment = mock(LdEnrollment.class);
        when(enrollment.getStatus()).thenReturn(LdEnrollment.Status.CONFIRMED);
        when(enrollment.getId()).thenReturn("ENROLLMENT-1");
        when(enrollment.getOfferingId()).thenReturn("OFFERING-1");
        when(enrollment.getOfferingVersion()).thenReturn(1);
        when(enrollment.getOfferingFingerprint()).thenReturn("offering-fingerprint");
        when(enrollment.getEmployeeId()).thenReturn(1L);
        when(enrollment.getEmployeeNo()).thenReturn("EMP-1");
        when(enrollment.getEmployeeName()).thenReturn("Employee One");
        when(enrollment.getAppointmentId()).thenReturn(11L);
        when(enrollment.getEmployeeFingerprint()).thenReturn("employee-fingerprint");
        return new LdCompletionCase("AGENCY", enrollment, "ORDER-1", "order-fingerprint",
                new BigDecimal("8"));
    }

    private LdOfferingSession session() {
        LdOfferingSession session = mock(LdOfferingSession.class);
        when(session.getId()).thenReturn("SESSION-1");
        when(session.getTitle()).thenReturn("Workshop");
        when(session.getStartsAt()).thenReturn(OffsetDateTime.parse("2026-05-10T08:00:00+08:00"));
        when(session.getEndsAt()).thenReturn(OffsetDateTime.parse("2026-05-10T17:00:00+08:00"));
        when(session.getCreditedHours()).thenReturn(new BigDecimal("8"));
        return session;
    }
}
