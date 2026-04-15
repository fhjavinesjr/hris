package com.administrative.dtos;

import java.io.Serializable;

public class EmployeeRequestDTO implements Serializable {

    private Long employeeRequestId;
    private String code;
    private String name;
    private Integer max;

    public EmployeeRequestDTO() {

    }

    public EmployeeRequestDTO(Long employeeRequestId, String code, String name, Integer max) {
        this.employeeRequestId = employeeRequestId;
        this.code = code;
        this.name = name;
        this.max = max;
    }

    public Long getEmployeeRequestId() {
        return employeeRequestId;
    }

    public void setEmployeeRequestId(Long employeeRequestId) {
        this.employeeRequestId = employeeRequestId;
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

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }
}
