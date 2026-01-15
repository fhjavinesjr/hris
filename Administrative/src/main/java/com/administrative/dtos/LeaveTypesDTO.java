package com.administrative.dtos;

import java.io.Serializable;

public class LeaveTypesDTO implements Serializable {

    private Long leaveTypesId;
    private String code;
    private String name;

    public LeaveTypesDTO() {

    }

    public LeaveTypesDTO(Long leaveTypesId, String code, String name) {
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