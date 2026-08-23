package com.administrative.services;

import com.administrative.dtos.SystemConfigDTO;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {

    List<SystemConfigDTO> getAllConfigs() throws Exception;

    Map<String, String> getPublicRuntimeConfig();

    SystemConfigDTO getByKey(String configKey) throws Exception;

    SystemConfigDTO updateConfig(String configKey, SystemConfigDTO dto) throws Exception;
}
