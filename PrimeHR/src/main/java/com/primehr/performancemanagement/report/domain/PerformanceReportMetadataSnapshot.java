package com.primehr.performancemanagement.report.domain;

import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "spms_report_metadata_snapshot", uniqueConstraints = @UniqueConstraint(
        name = "uk_spms_report_snapshot_source",
        columnNames = {"agency_id", "report_kind", "source_type", "source_id", "source_business_revision"}))
public class PerformanceReportMetadataSnapshot extends RspAuditedEntity {
    public enum ReportKind { OPCR, DPCR, IPCR, MONITORING, COACHING, DEVELOPMENT_PLAN, INTERVENTION }
    public enum SourceType { COMMITMENT_VERSION, FINAL_RATING, MONITORING_CASE, COACHING_SESSION, INTERVENTION }

    @Enumerated(EnumType.STRING) @Column(name = "report_kind", length = 30, nullable = false, updatable = false)
    private ReportKind reportKind;
    @Enumerated(EnumType.STRING) @Column(name = "source_type", length = 30, nullable = false, updatable = false)
    private SourceType sourceType;
    @Column(name = "source_id", length = 36, nullable = false, updatable = false) private String sourceId;
    @Column(name = "source_business_revision", nullable = false, updatable = false) private int sourceBusinessRevision;
    @Column(name = "source_record_version", nullable = false, updatable = false) private long sourceRecordVersion;
    @Column(name = "source_fingerprint", length = 64, nullable = false, updatable = false) private String sourceFingerprint;
    @Column(name = "template_code", length = 100, nullable = false, updatable = false) private String templateCode;
    @Column(name = "template_version", nullable = false, updatable = false) private int templateVersion;
    @Column(name = "employee_id", nullable = false, updatable = false) private Long employeeId;
    @Column(name = "employee_no", length = 100, nullable = false, updatable = false) private String employeeNo;
    @Nationalized @Column(name = "employee_name", length = 300, nullable = false, updatable = false) private String employeeName;
    @Nationalized @Column(name = "position_name", length = 200, nullable = false, updatable = false) private String positionName;
    @Nationalized @Column(name = "plantilla_name", length = 200, updatable = false) private String plantillaName;
    @Column(name = "area_id", updatable = false) private Long areaId;
    @Nationalized @Column(name = "area_name", length = 200, updatable = false) private String areaName;
    @Column(name = "business_unit_id", nullable = false, updatable = false) private Long businessUnitId;
    @Nationalized @Column(name = "business_unit_name", length = 200, nullable = false, updatable = false) private String businessUnitName;
    @Column(name = "supervisor_employee_no", length = 100, updatable = false) private String supervisorEmployeeNo;
    @Nationalized @Column(name = "supervisor_name", length = 300, updatable = false) private String supervisorName;
    @Nationalized @Column(name = "supervisor_position_name", length = 200, updatable = false) private String supervisorPositionName;
    @Column(name = "approver_employee_no", length = 100, updatable = false) private String approverEmployeeNo;
    @Nationalized @Column(name = "approver_name", length = 300, updatable = false) private String approverName;
    @Nationalized @Column(name = "approver_position_name", length = 200, updatable = false) private String approverPositionName;
    @Column(name = "period_start", nullable = false, updatable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false, updatable = false) private LocalDate periodEnd;
    @Nationalized @Column(name = "agency_name", length = 300, nullable = false, updatable = false) private String agencyName;
    @Nationalized @Column(name = "agency_address", length = 500, updatable = false) private String agencyAddress;
    @Column(name = "branding_fingerprint", length = 64, nullable = false, updatable = false) private String brandingFingerprint;
    @Column(name = "metadata_fingerprint", length = 64, nullable = false, updatable = false) private String metadataFingerprint;
    @Column(name = "captured_by", length = 100, nullable = false, updatable = false) private String capturedBy;
    @Column(name = "captured_at", nullable = false, updatable = false) private Instant capturedAt;

    protected PerformanceReportMetadataSnapshot() { }

    public PerformanceReportMetadataSnapshot(String agencyId, ReportKind reportKind, SourceType sourceType,
            String sourceId, int sourceBusinessRevision, long sourceRecordVersion, String sourceFingerprint,
            String templateCode, int templateVersion, Long employeeId, String employeeNo, String employeeName,
            String positionName, String plantillaName, Long areaId, String areaName, Long businessUnitId,
            String businessUnitName, String supervisorEmployeeNo, String supervisorName,
            String supervisorPositionName, String approverEmployeeNo, String approverName,
            String approverPositionName, LocalDate periodStart, LocalDate periodEnd, String agencyName,
            String agencyAddress, String brandingFingerprint, String metadataFingerprint,
            String capturedBy, Instant capturedAt) {
        super(agencyId);
        this.reportKind = java.util.Objects.requireNonNull(reportKind, "reportKind");
        this.sourceType = java.util.Objects.requireNonNull(sourceType, "sourceType");
        this.sourceId = requiredText(sourceId, "sourceId");
        if (sourceBusinessRevision < 1) throw new IllegalArgumentException("sourceBusinessRevision must be positive");
        if (sourceRecordVersion < 0) throw new IllegalArgumentException("sourceRecordVersion cannot be negative");
        this.sourceBusinessRevision = sourceBusinessRevision;
        this.sourceRecordVersion = sourceRecordVersion;
        this.sourceFingerprint = fingerprint(sourceFingerprint, "sourceFingerprint");
        this.templateCode = requiredText(templateCode, "templateCode");
        if (templateVersion < 1) throw new IllegalArgumentException("templateVersion must be positive");
        this.templateVersion = templateVersion;
        this.employeeId = java.util.Objects.requireNonNull(employeeId, "employeeId");
        this.employeeNo = requiredText(employeeNo, "employeeNo");
        this.employeeName = requiredText(employeeName, "employeeName");
        this.positionName = requiredText(positionName, "positionName");
        this.plantillaName = optionalText(plantillaName);
        this.areaId = areaId;
        this.areaName = optionalText(areaName);
        this.businessUnitId = java.util.Objects.requireNonNull(businessUnitId, "businessUnitId");
        this.businessUnitName = requiredText(businessUnitName, "businessUnitName");
        this.supervisorEmployeeNo = optionalText(supervisorEmployeeNo);
        this.supervisorName = optionalText(supervisorName);
        this.supervisorPositionName = optionalText(supervisorPositionName);
        this.approverEmployeeNo = optionalText(approverEmployeeNo);
        this.approverName = optionalText(approverName);
        this.approverPositionName = optionalText(approverPositionName);
        this.periodStart = java.util.Objects.requireNonNull(periodStart, "periodStart");
        this.periodEnd = java.util.Objects.requireNonNull(periodEnd, "periodEnd");
        if (periodEnd.isBefore(periodStart)) throw new IllegalArgumentException("periodEnd cannot be before periodStart");
        this.agencyName = requiredText(agencyName, "agencyName");
        this.agencyAddress = optionalText(agencyAddress);
        this.brandingFingerprint = fingerprint(brandingFingerprint, "brandingFingerprint");
        this.metadataFingerprint = fingerprint(metadataFingerprint, "metadataFingerprint");
        this.capturedBy = requiredText(capturedBy, "capturedBy");
        this.capturedAt = java.util.Objects.requireNonNull(capturedAt, "capturedAt");
    }

    private static String fingerprint(String value, String field) {
        String normalized = requiredText(value, field);
        if (!normalized.matches("[0-9a-fA-F]{64}")) throw new IllegalArgumentException(field + " must be a SHA-256 fingerprint");
        return normalized.toLowerCase(java.util.Locale.ROOT);
    }

    public ReportKind getReportKind(){return reportKind;} public SourceType getSourceType(){return sourceType;}
    public String getSourceId(){return sourceId;} public int getSourceBusinessRevision(){return sourceBusinessRevision;}
    public long getSourceRecordVersion(){return sourceRecordVersion;} public String getSourceFingerprint(){return sourceFingerprint;}
    public String getTemplateCode(){return templateCode;} public int getTemplateVersion(){return templateVersion;}
    public Long getEmployeeId(){return employeeId;} public String getEmployeeNo(){return employeeNo;}
    public String getEmployeeName(){return employeeName;} public String getPositionName(){return positionName;}
    public String getPlantillaName(){return plantillaName;} public Long getAreaId(){return areaId;}
    public String getAreaName(){return areaName;} public Long getBusinessUnitId(){return businessUnitId;}
    public String getBusinessUnitName(){return businessUnitName;} public String getSupervisorEmployeeNo(){return supervisorEmployeeNo;}
    public String getSupervisorName(){return supervisorName;} public String getSupervisorPositionName(){return supervisorPositionName;}
    public String getApproverEmployeeNo(){return approverEmployeeNo;} public String getApproverName(){return approverName;}
    public String getApproverPositionName(){return approverPositionName;} public LocalDate getPeriodStart(){return periodStart;}
    public LocalDate getPeriodEnd(){return periodEnd;} public String getAgencyName(){return agencyName;}
    public String getAgencyAddress(){return agencyAddress;} public String getBrandingFingerprint(){return brandingFingerprint;}
    public String getMetadataFingerprint(){return metadataFingerprint;} public String getCapturedBy(){return capturedBy;}
    public Instant getCapturedAt(){return capturedAt;}
}
