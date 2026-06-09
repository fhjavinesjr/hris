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
    @Column(name = "permissionData", columnDefinition = "NVARCHAR(MAX)")
    private String permissionData;

    public PermissionRuleset() {}

    public PermissionRuleset(String permissionName, Boolean isAdministrator, String permissionData) {
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
