package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;import jakarta.persistence.*;
@Entity@Table(name="spms_commitment",uniqueConstraints=@UniqueConstraint(name="uk_spms_commitment_assignment",columnNames={"agency_id","assignment_id"}))
public class PerformanceCommitment extends RspAuditedEntity{
 @Column(name="assignment_id",nullable=false,length=36)private String assignmentId;@Column(name="current_version_id",length=36)private String currentVersionId;
 protected PerformanceCommitment(){}public PerformanceCommitment(String a,String assignment){super(a);assignmentId=requiredText(assignment,"assignmentId");}public void current(String id){currentVersionId=requiredText(id,"currentVersionId");}public String getAssignmentId(){return assignmentId;}public String getCurrentVersionId(){return currentVersionId;}
}
