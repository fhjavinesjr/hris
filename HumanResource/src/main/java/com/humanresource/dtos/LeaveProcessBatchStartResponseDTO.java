package com.humanresource.dtos;

import java.io.Serializable;

public class LeaveProcessBatchStartResponseDTO implements Serializable {

    private String jobId;
    private String message;

    public LeaveProcessBatchStartResponseDTO() {
    }

    public LeaveProcessBatchStartResponseDTO(String jobId, String message) {
        this.jobId = jobId;
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
