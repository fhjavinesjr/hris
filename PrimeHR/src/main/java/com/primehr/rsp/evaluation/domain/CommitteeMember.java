package com.primehr.rsp.evaluation.domain;
import com.primehr.rsp.domain.RspAuditedEntity; import jakarta.persistence.*; import java.time.*; import java.util.*;
@Entity @Table(name="prime_committee_member",uniqueConstraints=@UniqueConstraint(name="uk_prime_committee_member",columnNames={"agency_id","committee_id","employee_no"}))
public class CommitteeMember extends RspAuditedEntity {
 public enum Role { CHAIRPERSON,VICE_CHAIRPERSON,MEMBER,SECRETARIAT,ALTERNATE,OBSERVER }
 @Column(name="committee_id",nullable=false,length=36) private String committeeId; @Column(name="employee_no",nullable=false,length=100) private String employeeNo; @Enumerated(EnumType.STRING) @Column(name="member_role",nullable=false,length=30) private Role role; @Column(length=300) private String representation; @Column(name="effective_from",nullable=false) private LocalDate effectiveFrom; @Column(name="effective_to") private LocalDate effectiveTo;
 protected CommitteeMember(){}
 public CommitteeMember(String agency,String committee,String employee,Role role,String representation,LocalDate from,LocalDate to){super(agency);committeeId=requiredText(committee,"committeeId");employeeNo=requiredText(employee,"employeeNo");this.role=Objects.requireNonNull(role);this.representation=optionalText(representation);effectiveFrom=Objects.requireNonNull(from,"effectiveFrom");effectiveTo=to;if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("Member effectiveTo cannot precede effectiveFrom");}
 public boolean effectiveOn(LocalDate date){return !date.isBefore(effectiveFrom)&&(effectiveTo==null||!date.isAfter(effectiveTo));} public boolean voting(){return role==Role.CHAIRPERSON||role==Role.VICE_CHAIRPERSON||role==Role.MEMBER;}
 public String getCommitteeId(){return committeeId;} public String getEmployeeNo(){return employeeNo;} public Role getRole(){return role;} public String getRepresentation(){return representation;} public LocalDate getEffectiveFrom(){return effectiveFrom;} public LocalDate getEffectiveTo(){return effectiveTo;}
}
