package com.timekeeping.dtos;

public class AdmsDtrProcessResultDTO {
    private int employeesReviewed;
    private int segmentsCreated;
    private int punchesProcessed;
    private int pendingPunches;
    private int conflicts;
    private int duplicates;

    public int getEmployeesReviewed() {
        return employeesReviewed;
    }

    public void setEmployeesReviewed(int employeesReviewed) {
        this.employeesReviewed = employeesReviewed;
    }

    public int getSegmentsCreated() {
        return segmentsCreated;
    }

    public void setSegmentsCreated(int segmentsCreated) {
        this.segmentsCreated = segmentsCreated;
    }

    public int getPunchesProcessed() {
        return punchesProcessed;
    }

    public void setPunchesProcessed(int punchesProcessed) {
        this.punchesProcessed = punchesProcessed;
    }

    public int getPendingPunches() {
        return pendingPunches;
    }

    public void setPendingPunches(int pendingPunches) {
        this.pendingPunches = pendingPunches;
    }

    public int getConflicts() {
        return conflicts;
    }

    public void setConflicts(int conflicts) {
        this.conflicts = conflicts;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }
}
