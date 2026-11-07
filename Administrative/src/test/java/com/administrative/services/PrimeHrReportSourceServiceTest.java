package com.administrative.services;

import com.administrative.entitymodels.*;
import com.administrative.impl.PrimeHrReportSourceServiceImpl;
import com.administrative.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrimeHrReportSourceServiceTest {
    @Test void returnsAuthoritativeBrandingAndPositionWithStableFingerprints() {
        SettingsRepository settings = mock(SettingsRepository.class);
        PlantillaRepository plantilla = mock(PlantillaRepository.class);
        JobPositionRepository positions = mock(JobPositionRepository.class);
        Settings value = new Settings(); value.setSettingsId(7L); value.setCompanyName("ISOFT Test Agency");
        value.setAddress("Main Road"); value.setCity("Quezon City"); value.setZipCode("1100");
        value.setMainLogo(new byte[]{1, 2, 3});
        when(settings.findTopByOrderBySettingsIdDesc()).thenReturn(Optional.of(value));
        when(plantilla.findById(11L)).thenReturn(Optional.of(new Plantilla(11L, "Administrative Officer V", 22L)));
        when(positions.findById(22L)).thenReturn(Optional.of(new JobPosition(22L, "Administrative Officer V", 18L)));
        PrimeHrReportSourceService service = new PrimeHrReportSourceServiceImpl(settings, plantilla, positions);

        var branding = service.branding(); var position = service.position(11L);
        assertThat(branding.companyName()).isEqualTo("ISOFT Test Agency");
        assertThat(branding.address()).isEqualTo("Main Road, Quezon City, 1100");
        assertThat(branding.sourceFingerprint()).matches("[0-9a-f]{64}");
        assertThat(position.plantillaId()).isEqualTo(11L); assertThat(position.jobPositionId()).isEqualTo(22L);
        assertThat(position.sourceFingerprint()).matches("[0-9a-f]{64}");
    }

    @Test void failsClosedWhenRequiredAdministrativeSourcesAreMissing() {
        SettingsRepository settings = mock(SettingsRepository.class);
        PlantillaRepository plantilla = mock(PlantillaRepository.class);
        JobPositionRepository positions = mock(JobPositionRepository.class);
        PrimeHrReportSourceService service = new PrimeHrReportSourceServiceImpl(settings, plantilla, positions);
        when(settings.findTopByOrderBySettingsIdDesc()).thenReturn(Optional.empty());
        when(plantilla.findById(11L)).thenReturn(Optional.empty());
        assertThatThrownBy(service::branding).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.position(11L)).isInstanceOf(ResponseStatusException.class);
    }
}
