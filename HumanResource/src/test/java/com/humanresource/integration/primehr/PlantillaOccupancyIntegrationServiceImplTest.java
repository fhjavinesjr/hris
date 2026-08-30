package com.humanresource.integration.primehr;

import com.humanresource.entitymodels.EmployeeAppointment;
import com.humanresource.repositories.EmployeeAppointmentRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlantillaOccupancyIntegrationServiceImplTest {
    private final EmployeeAppointmentRepository repository = mock(EmployeeAppointmentRepository.class);
    private final PlantillaOccupancyIntegrationServiceImpl service =
            new PlantillaOccupancyIntegrationServiceImpl(repository);

    @Test
    void returnsOnlyTheExactActiveAppointmentSnapshot() {
        EmployeeAppointment appointment = mock(EmployeeAppointment.class);
        when(appointment.getEmployeeAppointmentId()).thenReturn(10L);
        when(appointment.getEmployeeId()).thenReturn(20L);
        when(appointment.getAssumptionToDutyDate()).thenReturn(LocalDateTime.of(2026, 7, 1, 8, 0));
        when(appointment.getActiveAppointment()).thenReturn(true);
        when(repository.findTop1ByPlantillaIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(3L))
                .thenReturn(Optional.of(appointment));

        PlantillaOccupancyResponse response = service.get(3L);

        assertThat(response.occupied()).isTrue();
        assertThat(response.activeAppointmentId()).isEqualTo(10L);
        assertThat(response.sourceFingerprint()).hasSize(64);
        assertThat(PlantillaOccupancyResponse.class.getRecordComponents())
                .extracting(component -> component.getName().toLowerCase())
                .noneMatch(name -> name.matches(".*(password|biometric|salary|address|contact|email|pds).*"));
    }

    @Test
    void reportsVacantWithoutInventingEmployeeFacts() {
        when(repository.findTop1ByPlantillaIdAndActiveAppointmentTrueOrderByAssumptionToDutyDateDescEmployeeAppointmentIdDesc(3L))
                .thenReturn(Optional.empty());
        PlantillaOccupancyResponse response = service.get(3L);
        assertThat(response.occupied()).isFalse();
        assertThat(response.activeAppointmentId()).isNull();
    }

    @Test
    void rejectsInvalidPlantillaBeforeRepositoryAccess() {
        assertThatThrownBy(() -> service.get(0L)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository);
    }
}
