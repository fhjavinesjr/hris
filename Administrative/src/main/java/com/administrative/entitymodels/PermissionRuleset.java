package com.administrative.entitymodels;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "permission_ruleset")
public class PermissionRuleset implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permissionId")
    private Long permissionId;

    @Column(name = "permissionName", length = 150, unique = true, nullable = false)
    private String permissionName;

    @Column(name = "isAdministrator")
    private Boolean isAdministrator = false;

    /**
     * JSON string holding the full module-permission map.
     * Structure: { "moduleKey": { "canAccess": bool, "canAdd": bool, "canEdit": bool, "canDelete": bool }, ... }
     */
    @Lob
    @Column(name = "permissionData")
    private String permissionData;

    /**
     * JSON string holding Employee Portal visibility for the top-level systems.
     * Structure: { "administrative": bool, "hrManagement": bool,
     *              "timeKeeping": bool, "payroll": bool }
     */
    @Lob
    @Column(name = "portalModuleAccess")
    private String portalModuleAccess;

    public PermissionRuleset() {}

    public PermissionRuleset(String permissionName, Boolean isAdministrator, String permissionData) {
        this.permissionName = permissionName;
        this.isAdministrator = isAdministrator;
        this.permissionData = permissionData;
    }

    public PermissionRuleset(String permissionName, Boolean isAdministrator, String permissionData, String portalModuleAccess) {
        this(permissionName, isAdministrator, permissionData);
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
