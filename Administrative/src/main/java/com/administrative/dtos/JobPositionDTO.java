package com.administrative.dtos;

import java.io.Serializable;

public class JobPositionDTO implements Serializable {

    private Long jobPositionId;
    private String jobPositionName;
    private Long salaryGrade;
    private Long salaryStep;

    public JobPositionDTO() {

    }

    public JobPositionDTO(String jobPositionName, Long salaryGrade, Long salaryStep) {
        this.jobPositionName = jobPositionName;
        this.salaryGrade = salaryGrade;
        this.salaryStep = salaryStep;
    }

    public Long getJobPositionId() {
        return jobPositionId;
    }

    public void setJobPositionId(Long jobPositionId) {
        this.jobPositionId = jobPositionId;
    }

    public String getJobPositionName() {
        return jobPositionName;
    }

    public void setJobPositionName(String jobPositionName) {
        this.jobPositionName = jobPositionName;
    }

    public Long getSalaryGrade() {
        return salaryGrade;
    }

    public void setSalaryGrade(Long salaryGrade) {
        this.salaryGrade = salaryGrade;
    }

    public Long getSalaryStep() {
        return salaryStep;
    }

    public void setSalaryStep(Long salaryStep) {
        this.salaryStep = salaryStep;
    }
}