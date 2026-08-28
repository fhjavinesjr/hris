package com.humanresource.integration.primehr;

import com.humanresource.repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AssessmentSubjectIntegrationServiceImplTest {
    private final EmployeeRepository repository = mock(EmployeeRepository.class);
    private final AssessmentSubjectIntegrationServiceImpl service = new AssessmentSubjectIntegrationServiceImpl(repository);

    @Test
    void returnsOnlyMinimalCurrentAppointmentSnapshotWithDeterministicFingerprint() {
        AssessmentSubjectRow row = new AssessmentSubjectRow(1L, "001", "Ferdinand", "Javines", null,
                LocalDateTime.of(2026, 8, 1, 9, 0), 10L, LocalDateTime.of(2026, 7, 1, 8, 0), 14, 3);
        when(repository.findPrimeHrAssessmentSubject(eq(1L), any())).thenReturn(Optional.of(row));

        AssessmentSubjectResponse result = service.get(1L);

        assertThat(result.displayName()).isEqualTo("Ferdinand Javines");
        assertThat(result.employeeNo()).isEqualTo("001");
        assertThat(result.appointmentId()).isEqualTo(10L);
        assertThat(result.jobPositionId()).isEqualTo(14L);
        assertThat(result.plantillaId()).isEqualTo(3L);
        assertThat(result.sourceFingerprint()).hasSize(64);
        assertThat(AssessmentSubjectResponse.class.getRecordComponents())
                .extracting(component -> component.getName().toLowerCase())
                .noneMatch(name -> name.matches(".*(password|biometric|salary|address|contact|email|pds).*"));
    }

    @Test
    void rejectsInactiveHistoryAndInvalidPaginationBeforeRepositoryAccess() {
        assertThatThrownBy(() -> service.list(null, 0, 20, false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("active");
        assertThatThrownBy(() -> service.list(null, -1, 20, true)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void listUsesProviderNeutralPageableProjection() {
        AssessmentSubjectRow row = new AssessmentSubjectRow(1L, "001", "Ferdinand", "Javines", null,
                null, 10L, LocalDateTime.of(2026, 7, 1, 8, 0), 14, null);
        when(repository.findPrimeHrAssessmentSubjects(eq("fer"), any(), any()))
                .thenReturn(new PageImpl<>(java.util.List.of(row), PageRequest.of(0, 10), 1));
        assertThat(service.list(" FER ", 0, 10, true).content()).singleElement()
                .extracting(AssessmentSubjectResponse::employeeNo).isEqualTo("001");
    }
}
