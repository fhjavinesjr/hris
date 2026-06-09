package com.administrative.dtos;

public class PermissionRulesetDTO {

    private Long permissionId;
    private String permissionName;
    private Boolean isAdministrator;
    private String permissionData; // JSON string of module permissions

    public PermissionRulesetDTO() {}

    public PermissionRulesetDTO(Long permissionId, String permissionName, Boolean isAdministrator, String permissionData) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.isAdministrator = isAdministrator;
        this.permissionData = permissionData;
    }

    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    public Boolean getIsAdministrator() { return isAdministrator; }
    public void setIsAdministrator(Boolean isAdministrator) { this.isAdministrator = isAdministrator; }

    public String getPermissionData() { return permissionData; }
    public void setPermissionData(String permissionData) { this.permissionData = permissionData; }
}
