package com.primehr.security;

public interface CompetencyReadPermissionResolver {

    boolean canReadCompetencies(String employeeNo, String role);
}
