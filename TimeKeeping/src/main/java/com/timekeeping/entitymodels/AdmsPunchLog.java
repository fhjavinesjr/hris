package com.timekeeping.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Read-only copy of one ZKTeco/iClock checkinout row inside the HRIS database.
 * The row is first imported safely, then interpreted into the HRIS DTR.
 */
@Entity
@Table(
        name = "adms_punch_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_adms_punch_log_checkout_id",
                columnNames = "adms_checkout_id"
        )
)
public class AdmsPunchLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adms_punch_log_id")
    private Long admsPunchLogId;

    // Kept for backward compatibility. The value is checkinout.id.
    @Column(name = "adms_checkout_id", nullable = false)
    private Long admsCheckoutId;

    @Column(name = "adms_user_id", length = 100)
    private String admsUserId;

    @Column(name = "adms_badge_number", length = 100)
    private String admsBadgeNumber;

    @Column(name = "employee_id", length = 100)
    private String employeeId;

    @Column(name = "check_time")
    private LocalDateTime checkTime;

    @Column(name = "check_type")
    private Integer checkType;

    @Column(name = "device_serial_no", length = 100)
    private String deviceSerialNo;

    @Column(name = "import_status", nullable = false, length = 30)
    private String importStatus;

    @Column(name = "import_message", length = 500)
    private String importMessage;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    @Column(name = "dtr_processed", nullable = false)
    private Boolean dtrProcessed = false;

    @Column(name = "dtr_processing_status", length = 30)
    private String dtrProcessingStatus;

    @Column(name = "dtr_processing_message", length = 500)
    private String dtrProcessingMessage;

    @Column(name = "dtr_processed_at")
    private LocalDateTime dtrProcessedAt;

    @Column(name = "dtr_daily_id")
    private Long dtrDailyId;

    @Column(name = "dtr_segment_id")
    private Long dtrSegmentId;

    public Long getAdmsPunchLogId() {
        return admsPunchLogId;
    }

    public void setAdmsPunchLogId(Long admsPunchLogId) {
        this.admsPunchLogId = admsPunchLogId;
    }

    public Long getAdmsCheckoutId() {
        return admsCheckoutId;
    }

    public void setAdmsCheckoutId(Long admsCheckoutId) {
        this.admsCheckoutId = admsCheckoutId;
    }

    public String getAdmsUserId() {
        return admsUserId;
    }

    public void setAdmsUserId(String admsUserId) {
        this.admsUserId = admsUserId;
    }

    public String getAdmsBadgeNumber() {
        return admsBadgeNumber;
    }

    public void setAdmsBadgeNumber(String admsBadgeNumber) {
        this.admsBadgeNumber = admsBadgeNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public Integer getCheckType() {
        return checkType;
    }

    public void setCheckType(Integer checkType) {
        this.checkType = checkType;
    }

    public String getDeviceSerialNo() {
        return deviceSerialNo;
    }

    public void setDeviceSerialNo(String deviceSerialNo) {
        this.deviceSerialNo = deviceSerialNo;
    }

    public String getImportStatus() {
        return importStatus;
    }

    public void setImportStatus(String importStatus) {
        this.importStatus = importStatus;
    }

    public String getImportMessage() {
        return importMessage;
    }

    public void setImportMessage(String importMessage) {
        this.importMessage = importMessage;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public Boolean getDtrProcessed() {
        return dtrProcessed;
    }

    public void setDtrProcessed(Boolean dtrProcessed) {
        this.dtrProcessed = dtrProcessed;
    }

    public String getDtrProcessingStatus() {
        return dtrProcessingStatus;
    }

    public void setDtrProcessingStatus(String dtrProcessingStatus) {
        this.dtrProcessingStatus = dtrProcessingStatus;
    }

    public String getDtrProcessingMessage() {
        return dtrProcessingMessage;
    }

    public void setDtrProcessingMessage(String dtrProcessingMessage) {
        this.dtrProcessingMessage = dtrProcessingMessage;
    }

    public LocalDateTime getDtrProcessedAt() {
        return dtrProcessedAt;
    }

    public void setDtrProcessedAt(LocalDateTime dtrProcessedAt) {
        this.dtrProcessedAt = dtrProcessedAt;
    }

    public Long getDtrDailyId() {
        return dtrDailyId;
    }

    public void setDtrDailyId(Long dtrDailyId) {
        this.dtrDailyId = dtrDailyId;
    }

    public Long getDtrSegmentId() {
        return dtrSegmentId;
    }

    public void setDtrSegmentId(Long dtrSegmentId) {
        this.dtrSegmentId = dtrSegmentId;
    }
}
