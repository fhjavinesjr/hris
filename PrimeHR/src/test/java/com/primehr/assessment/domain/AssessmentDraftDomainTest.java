package com.primehr.assessment.domain;

import com.primehr.positionprofile.domain.*;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssessmentDraftDomainTest {
    @Test
    void phaseThreePointOneObjectsRemainDraftOnly() {
        AssessmentCycle cycle = new AssessmentCycle("AGENCY", "FY26", "FY 2026", null,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        PositionProfile profile = profile(PositionTargetType.JOB_POSITION, 14L, null);
        AssessmentTool tool = new AssessmentTool("AGENCY", cycle, profile, "Annual", null);
        AssessmentToolMethod method = new AssessmentToolMethod("AGENCY", tool,
                AssessmentMethod.SELF_ASSESSMENT, true);

        assertThat(cycle.getStatus()).isEqualTo(AssessmentCycleStatus.DRAFT);
        assertThat(tool.getStatus()).isEqualTo(AssessmentToolStatus.DRAFT);
        assertThat(method.isActive()).isTrue();
        assertThat(AssessmentCaseStatus.values()).contains(AssessmentCaseStatus.DRAFT);
    }

    @Test
    void subjectMustMatchExactPositionProfileTargetIncludingPlantillaPrecedence() {
        AssessmentCycle cycle = new AssessmentCycle("AGENCY", "FY26", "FY 2026", null, null, null);
        AssessmentTool tool = new AssessmentTool("AGENCY", cycle,
                profile(PositionTargetType.PLANTILLA, 14L, 3L), "Annual", null);
        AssessmentSubjectSnapshot wrong = subject(1L, 14L, 4L);

        assertThatThrownBy(() -> new AssessmentCase("AGENCY", tool, wrong))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Plantilla");
    }

    @Test
    void explicitSelfAndNonSelfAssessorIdentityRulesAreEnforced() {
        AssessmentCycle cycle = new AssessmentCycle("AGENCY", "FY26", "FY 2026", null, null, null);
        AssessmentTool tool = new AssessmentTool("AGENCY", cycle,
                profile(PositionTargetType.JOB_POSITION, 14L, null), "Annual", null);
        AssessmentCase assessmentCase = new AssessmentCase("AGENCY", tool, subject(1L, 14L, null));

        assertThatThrownBy(() -> new AssessorAssignment("AGENCY", assessmentCase,
                AssessmentMethod.SELF_ASSESSMENT, subject(2L, 14L, null), null))
                .hasMessageContaining("subject");
        assertThatThrownBy(() -> new AssessorAssignment("AGENCY", assessmentCase,
                AssessmentMethod.IMMEDIATE_SUPERVISOR, subject(1L, 14L, null), "Explicit HR assignment"))
                .hasMessageContaining("Only SELF");
        assertThat(new AssessorAssignment("AGENCY", assessmentCase, AssessmentMethod.IMMEDIATE_SUPERVISOR,
                subject(2L, 14L, null), "Explicit HR assignment").getAssignmentReason())
                .isEqualTo("Explicit HR assignment");
    }

    private static PositionProfile profile(PositionTargetType type, Long job, Long plantilla) {
        PositionProfile profile = mock(PositionProfile.class);
        when(profile.getAgencyId()).thenReturn("AGENCY"); when(profile.getStatus()).thenReturn(PositionProfileStatus.ACTIVE);
        when(profile.isActive()).thenReturn(true); when(profile.getDefinitionVersion()).thenReturn(1);
        when(profile.getContentRevision()).thenReturn(1L); when(profile.getTargetKey()).thenReturn(
                type == PositionTargetType.PLANTILLA ? "PLANTILLA:" + plantilla : "JOB_POSITION:" + job);
        when(profile.getName()).thenReturn("Profile"); when(profile.getSourceFingerprint()).thenReturn("f".repeat(64));
        when(profile.getTargetType()).thenReturn(type); when(profile.getJobPositionId()).thenReturn(job);
        when(profile.getPlantillaId()).thenReturn(plantilla);
        return profile;
    }

    private static AssessmentSubjectSnapshot subject(Long id, Long job, Long plantilla) {
        return new AssessmentSubjectSnapshot(id, "E-" + id, "Employee " + id, 100L + id,
                LocalDateTime.of(2026, 1, 1, 8, 0), job, plantilla, "s".repeat(64), null, Instant.now());
    }
}
