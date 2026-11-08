package com.primehr.performancemanagement.report.application;

import java.math.BigDecimal;
import java.util.List;

public final class PerformanceFormalReportData {
    private PerformanceFormalReportData() { }

    public record Header(String title, String formType, String lifecycleState, String agencyName,
            String agencyAddress, String employeeNo, String employeeName, String positionName,
            String organizationName, String period, String supervisor, String approver,
            String sourceReference, String metadataReference, String generated) { }

    public record Form(Header header, BigDecimal finalScore, String ratingLabel, List<FormRow> rows) { }
    public record FormRow(Integer rowNumber, String section, String output, String successIndicator,
            String target, String accomplishment, String dimensionRatings, BigDecimal weight,
            BigDecimal score, String remarks) {
        public Integer getRowNumber(){return rowNumber;} public String getSection(){return section;}
        public String getOutput(){return output;} public String getSuccessIndicator(){return successIndicator;}
        public String getTarget(){return target;} public String getAccomplishment(){return accomplishment;}
        public String getDimensionRatings(){return dimensionRatings;} public BigDecimal getWeight(){return weight;}
        public BigDecimal getScore(){return score;} public String getRemarks(){return remarks;}
    }

    public record Monitoring(Header header, List<MonitoringRow> rows) { }
    public record MonitoringRow(Integer rowNumber, String reportingDate, String output,
            String accomplishment, String actual, String progress, String issues, String support,
            String employeeRemarks, String status) {
        public Integer getRowNumber(){return rowNumber;} public String getReportingDate(){return reportingDate;}
        public String getOutput(){return output;} public String getAccomplishment(){return accomplishment;}
        public String getActual(){return actual;} public String getProgress(){return progress;}
        public String getIssues(){return issues;} public String getSupport(){return support;}
        public String getEmployeeRemarks(){return employeeRemarks;} public String getStatus(){return status;}
    }

    public record Coaching(Header header, String agenda, String goal, String observedIssue,
            String agreedAction, String resourcesSupport, String actionDueDate, String nextMeeting,
            String employeeVisibleFeedback, String employeeResponse, List<ActionRow> rows) { }
    public record ActionRow(Integer rowNumber, String description, String accountableEmployeeNo,
            String dueDate, String status, String progressNote, String verification) {
        public Integer getRowNumber(){return rowNumber;} public String getDescription(){return description;}
        public String getAccountableEmployeeNo(){return accountableEmployeeNo;} public String getDueDate(){return dueDate;}
        public String getStatus(){return status;} public String getProgressNote(){return progressNote;}
        public String getVerification(){return verification;}
    }

    public record Intervention(Header header, String reportPurpose, String action, String objectives,
            String supportPlan, String accountableEmployeeNo, String dateRange, String employeeResponse,
            String closure, List<CheckpointRow> rows) { }
    public record CheckpointRow(Integer rowNumber, String dueDate, String objective, String status,
            String progressNote, String employeeVisibleFeedback) {
        public Integer getRowNumber(){return rowNumber;} public String getDueDate(){return dueDate;}
        public String getObjective(){return objective;} public String getStatus(){return status;}
        public String getProgressNote(){return progressNote;} public String getEmployeeVisibleFeedback(){return employeeVisibleFeedback;}
    }
}
