package com.humanresource.dtos;

public record EmployeeSecurityStatusDTO(boolean usingDefaultPassword, boolean roleAssigned, String role) {
}
