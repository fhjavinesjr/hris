package com.administrative.dtos;

public class PermissionRulesetDTO {

    private Long permissionId;
    private String permissionName;
    private Boolean isAdministrator;
    private String permissionData; // JSON string of module permissions
    private String portalModuleAccess; // JSON string of top-level systems shown in Employee Portal

    public PermissionRulesetDTO() {}

    public PermissionRulesetDTO(Long permissionId, String permissionName, Boolean isAdministrator, String permissionData) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.isAdministrator = isAdministrator;
        this.permissionData = permissionData;
    }

    public PermissionRulesetDTO(Long permissionId, String permissionName, Boolean isAdministrator,
                                String permissionData, String portalModuleAccess) {
        this(permissionId, permissionName, isAdministrator, permissionData);
        this.portalModuleAccess = portalModuleAccess;
    }

    public Long getPermissionId() { return permissionId; }
    public void setPermissionId(Long permissionId) { this.permissionId = permissionId; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    public Boolean getIsAdministrator() { return isAdministrator; }
    public void setIsAdministrator(Boolean isAdministrator) { this.isAdministrator = isAdministrator; }

    public String getPermissionData() { return permissionData; }
    public void setPermissionData(String permissionData) { this.permissionData = permissionData; }

    public String getPortalModuleAccess() { return portalModuleAccess; }
    public void setPortalModuleAccess(String portalModuleAccess) { this.portalModuleAccess = portalModuleAccess; }
}
