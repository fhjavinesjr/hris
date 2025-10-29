package com.administrative.dtos;

import java.io.Serializable;

public class JobPositionDTO implements Serializable {

    private Long jobPositionId;
    private String jobPositionName;
    private Long salaryGrade;

    public JobPositionDTO() {

    }

    public JobPositionDTO(Long jobPositionId, String jobPositionName, Long salaryGrade) {
        this.jobPositionId = jobPositionId;
        this.jobPositionName = jobPositionName;
        this.salaryGrade = salaryGrade;
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
}
