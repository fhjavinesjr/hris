package com.administrative.entitymodels;

import jakarta.persistence.*;

@Entity
@Table(name = "manage_personnel")
public class ManagePersonnel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Long businessUnitId;

    @Column(nullable = false)
    private Long areaId;

    private boolean head;
    private boolean coApprover;
    private String otherStatus;
    private String base;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Long getBusinessUnitId() { return businessUnitId; }
    public void setBusinessUnitId(Long businessUnitId) { this.businessUnitId = businessUnitId; }

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public boolean isHead() { return head; }
    public void setHead(boolean head) { this.head = head; }

    public boolean isCoApprover() { return coApprover; }
    public void setCoApprover(boolean coApprover) { this.coApprover = coApprover; }

    public String getOtherStatus() { return otherStatus; }
    public void setOtherStatus(String otherStatus) { this.otherStatus = otherStatus; }

    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }
}
