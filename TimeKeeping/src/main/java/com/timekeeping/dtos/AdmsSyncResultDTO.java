package com.timekeeping.dtos;

public class AdmsSyncResultDTO {
    private boolean enabled;
    private int recordsRead;
    private int imported;
    private int unmapped;
    private int invalid;
    private int duplicatesSkipped;
    private int dtrEmployeesReviewed;
    private int dtrSegmentsCreated;
    private int dtrPunchesProcessed;
    private int dtrPendingPunches;
    private int dtrConflicts;
    private int dtrDuplicates;
    private String message;

    public AdmsSyncResultDTO() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRecordsRead() {
        return recordsRead;
    }

    public void setRecordsRead(int recordsRead) {
        this.recordsRead = recordsRead;
    }

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getUnmapped() {
        return unmapped;
    }

    public void setUnmapped(int unmapped) {
        this.unmapped = unmapped;
    }

    public int getInvalid() {
        return invalid;
    }

    public void setInvalid(int invalid) {
        this.invalid = invalid;
    }

    public int getDuplicatesSkipped() {
        return duplicatesSkipped;
    }

    public void setDuplicatesSkipped(int duplicatesSkipped) {
        this.duplicatesSkipped = duplicatesSkipped;
    }

    public int getDtrEmployeesReviewed() {
        return dtrEmployeesReviewed;
    }

    public void setDtrEmployeesReviewed(int dtrEmployeesReviewed) {
        this.dtrEmployeesReviewed = dtrEmployeesReviewed;
    }

    public int getDtrSegmentsCreated() {
        return dtrSegmentsCreated;
    }

    public void setDtrSegmentsCreated(int dtrSegmentsCreated) {
        this.dtrSegmentsCreated = dtrSegmentsCreated;
    }

    public int getDtrPunchesProcessed() {
        return dtrPunchesProcessed;
    }

    public void setDtrPunchesProcessed(int dtrPunchesProcessed) {
        this.dtrPunchesProcessed = dtrPunchesProcessed;
    }

    public int getDtrPendingPunches() {
        return dtrPendingPunches;
    }

    public void setDtrPendingPunches(int dtrPendingPunches) {
        this.dtrPendingPunches = dtrPendingPunches;
    }

    public int getDtrConflicts() {
        return dtrConflicts;
    }

    public void setDtrConflicts(int dtrConflicts) {
        this.dtrConflicts = dtrConflicts;
    }

    public int getDtrDuplicates() {
        return dtrDuplicates;
    }

    public void setDtrDuplicates(int dtrDuplicates) {
        this.dtrDuplicates = dtrDuplicates;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
