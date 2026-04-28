package com.humanresource.dtos;

import java.io.Serializable;
import java.util.List;

/**
 * Response returned after running the Leave Process for a period.
 */
public class LeaveProcessResultDTO implements Serializable {

    private int totalProcessed;
    private int totalSkipped;
    private List<String> skippedReasons;
    private List<LeaveInformationDTO> processed;

    public LeaveProcessResultDTO() {
    }

    public LeaveProcessResultDTO(int totalProcessed, int totalSkipped,
                                  List<String> skippedReasons, List<LeaveInformationDTO> processed) {
        this.totalProcessed = totalProcessed;
        this.totalSkipped = totalSkipped;
        this.skippedReasons = skippedReasons;
        this.processed = processed;
    }

    public int getTotalProcessed() { return totalProcessed; }
    public void setTotalProcessed(int totalProcessed) { this.totalProcessed = totalProcessed; }

    public int getTotalSkipped() { return totalSkipped; }
    public void setTotalSkipped(int totalSkipped) { this.totalSkipped = totalSkipped; }

    public List<String> getSkippedReasons() { return skippedReasons; }
    public void setSkippedReasons(List<String> skippedReasons) { this.skippedReasons = skippedReasons; }

    public List<LeaveInformationDTO> getProcessed() { return processed; }
    public void setProcessed(List<LeaveInformationDTO> processed) { this.processed = processed; }
}
