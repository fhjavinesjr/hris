package com.administrative.services;

import com.administrative.dtos.SettingsDTO;

import java.util.List;

public interface SettingsService {

    SettingsDTO createSettings(SettingsDTO SettingsDTO) throws Exception;

    List<SettingsDTO> getAllSettings() throws Exception;

    SettingsDTO getSettingsById(Long settingsId) throws Exception;

    SettingsDTO updateSettings(Long settingsId, SettingsDTO SettingsDTO) throws Exception;

    Boolean deleteSettings(Long settingsId) throws Exception;

}
