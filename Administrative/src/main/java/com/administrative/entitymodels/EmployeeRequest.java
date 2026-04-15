package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "employee_request")
public class EmployeeRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeRequestId")
    private Long employeeRequestId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "max", nullable = false)
    private Integer max;

    public EmployeeRequest() {

    }

    public EmployeeRequest(Long employeeRequestId, String code, String name, Integer max) {
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
