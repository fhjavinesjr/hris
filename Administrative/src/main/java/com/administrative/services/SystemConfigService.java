package com.administrative.services;

import com.administrative.dtos.SystemConfigDTO;

import java.util.List;

public interface SystemConfigService {

    List<SystemConfigDTO> getAllConfigs() throws Exception;

    SystemConfigDTO getByKey(String configKey) throws Exception;

    SystemConfigDTO updateConfig(String configKey, SystemConfigDTO dto) throws Exception;
}
