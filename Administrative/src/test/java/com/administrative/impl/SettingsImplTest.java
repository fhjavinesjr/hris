package com.administrative.impl;

import com.administrative.dtos.SettingsDTO;
import com.administrative.entitymodels.Settings;
import com.administrative.repositories.SettingsRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SettingsImplTest {

    @Test
    void rejectsInvalidAgencyZipBeforeSaving() {
        SettingsRepository repository = mock(SettingsRepository.class);
        SettingsDTO request = new SettingsDTO();
        request.setZipCode("HRIS Client");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new SettingsImpl(repository).createSettings(request));

        assertEquals("ZIP Code must be four digits.", error.getMessage());
        verify(repository, never()).save(any(Settings.class));
    }
}
