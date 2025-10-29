package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "job_position")
public class JobPosition implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jobPositionId")
    private Long jobPositionId;

    @Column(name = "jobPositionName", length = 100, unique = true, nullable = false)
    private String jobPositionName;

    @Column(name = "salaryGrade", length = 50)
    private Long salaryGrade;

    public JobPosition() {

    }

    public JobPosition(String jobPositionName, Long salaryGrade) {
        this.jobPositionName = jobPositionName;
        this.salaryGrade = salaryGrade;
    }

    public JobPosition(Long jobPositionId, String jobPositionName, Long salaryGrade) {
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