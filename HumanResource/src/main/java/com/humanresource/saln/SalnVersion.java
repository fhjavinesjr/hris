package com.humanresource.saln;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="saln_version", uniqueConstraints=@UniqueConstraint(name="uq_saln_version", columnNames={"saln_id","version_no"}))
public class SalnVersion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="saln_version_id") private Long id;
    @Column(name="saln_id", nullable=false) private Long salnId;
    @Column(name="version_no", nullable=false) private int versionNo;
    @Enumerated(EnumType.STRING) @Column(name="status", nullable=false, length=24) private SalnTypes.Status status;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR) @Column(name="snapshot_json", nullable=false) private String snapshotJson;
    @Column(name="submitted_at", nullable=false) private LocalDateTime submittedAt;
    @Column(name="submitted_by", nullable=false, length=100) private String submittedBy;
    protected SalnVersion() {}
    public SalnVersion(Long salnId,int versionNo,SalnTypes.Status status,String snapshotJson,LocalDateTime submittedAt,String submittedBy){this.salnId=salnId;this.versionNo=versionNo;this.status=status;this.snapshotJson=snapshotJson;this.submittedAt=submittedAt;this.submittedBy=submittedBy;}
    public Long getId(){return id;} public Long getSalnId(){return salnId;} public int getVersionNo(){return versionNo;} public SalnTypes.Status getStatus(){return status;} public String getSnapshotJson(){return snapshotJson;} public LocalDateTime getSubmittedAt(){return submittedAt;} public String getSubmittedBy(){return submittedBy;}
}
