package com.administrative.services;

import com.administrative.dtos.EffectiveFeaturePermissionResponse;

public interface EffectiveAuthorizationService {
    EffectiveFeaturePermissionResponse resolve(String employeeNo, String role, String featureKey);
}
