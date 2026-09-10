package com.primehr.integration.administrative;

import java.util.Set;

public interface AdministrativeOrganizationScopeClient {
    Set<Long> businessUnitIdsForArea(Long areaId, String authorization);
}
