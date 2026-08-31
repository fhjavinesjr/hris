package com.primehr.rsp.screening.domain;
import com.primehr.rsp.domain.RspAuditedEntity; import jakarta.persistence.*; import java.time.Instant; import java.util.Objects;
@Entity @Table(name="rsp_screening_assignment",uniqueConstraints=@UniqueConstraint(name="uk_rsp_screening_assignment",columnNames={"agency_id","case_id","employee_no","process_role"}))
public class ScreeningAssignment extends RspAuditedEntity {
 public enum Role { SCREENER, VALIDATOR }
 @Column(name="case_id",nullable=false,length=36) private String caseId; @Column(name="employee_no",nullable=false,length=100) private String employeeNo; @Enumerated(EnumType.STRING) @Column(name="process_role",nullable=false,length=20) private Role role; @Column(nullable=false) private boolean active; @Column(name="assigned_by",nullable=false,length=100) private String assignedBy; @Column(name="assigned_at",nullable=false) private Instant assignedAt;
 protected ScreeningAssignment(){} public ScreeningAssignment(String agency,String caseId,String employee,Role role,String actor,Instant at){super(agency);this.caseId=requiredText(caseId,"caseId");employeeNo=requiredText(employee,"employeeNo");this.role=Objects.requireNonNull(role);active=true;assignedBy=requiredText(actor,"actor");assignedAt=Objects.requireNonNull(at);}
 public void deactivate(){active=false;}
 public void activate(String actor,Instant at){active=true;assignedBy=requiredText(actor,"actor");assignedAt=Objects.requireNonNull(at);}
 public String getCaseId(){return caseId;} public String getEmployeeNo(){return employeeNo;} public Role getRole(){return role;} public boolean isActive(){return active;} public String getAssignedBy(){return assignedBy;} public Instant getAssignedAt(){return assignedAt;}
}
