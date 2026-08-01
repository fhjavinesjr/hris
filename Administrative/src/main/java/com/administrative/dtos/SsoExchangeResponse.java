package com.administrative.dtos;

import java.util.List;

public record SsoExchangeResponse(
        String token,
        String employeeNo,
        String employeeRole,
        String target,
        PermissionRulesetDTO permission,
        List<SystemConfigDTO> systemConfig
) {}
