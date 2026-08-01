package com.administrative.services;

import com.administrative.dtos.SsoExchangeResponse;
import com.administrative.dtos.SsoLaunchResponse;

public interface SsoService {
    SsoLaunchResponse launch(String employeeNo, String employeeRole, String target);
    SsoExchangeResponse exchange(String code, String target) throws Exception;
}
