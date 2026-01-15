package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "leave_types")
public class LeaveTypes implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leaveTypesId")
    private Long leaveTypesId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    public LeaveTypes() {

    }

    public LeaveTypes(Long leaveTypesId, String code, String name) {
        this.leaveTypesId = leaveTypesId;
        this.code = code;
        this.name = name;
    }

    public Long getLeaveTypesId() {
        return leaveTypesId;
    }

    public void setLeaveTypesId(Long leaveTypesId) {
        this.leaveTypesId = leaveTypesId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}