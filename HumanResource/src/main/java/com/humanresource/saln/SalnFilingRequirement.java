package com.humanresource.saln;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="saln_filing_requirement", uniqueConstraints=@UniqueConstraint(name="uq_saln_requirement", columnNames={"employee_id","filing_type","reference_date"}), indexes=@Index(name="ix_saln_requirement_due",columnList="status,due_date"))
public class SalnFilingRequirement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="requirement_id") private Long id;
    @Column(name="employee_id", nullable=false) private Long employeeId;
    @Column(name="employee_no", nullable=false, length=100) private String employeeNo;
    @Enumerated(EnumType.STRING) @Column(name="filing_type", nullable=false, length=20) private SalnTypes.FilingType filingType;
    @Column(name="reference_date", nullable=false) private LocalDate referenceDate;
    @Column(name="saln_year", nullable=false) private int salnYear;
    @Column(name="due_date", nullable=false) private LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(name="status", nullable=false, length=20) private SalnTypes.RequirementStatus status=SalnTypes.RequirementStatus.NOT_FILED;
    @Column(name="generated_by", nullable=false, length=100) private String generatedBy;
    @Column(name="generated_at", nullable=false) private LocalDateTime generatedAt;
    @Column(name="linked_saln_id") private Long linkedSalnId;
    protected SalnFilingRequirement() {}
    public SalnFilingRequirement(Long employeeId,String employeeNo,SalnTypes.FilingType filingType,LocalDate referenceDate,int salnYear,LocalDate dueDate,String generatedBy){this.employeeId=employeeId;this.employeeNo=employeeNo;this.filingType=filingType;this.referenceDate=referenceDate;this.salnYear=salnYear;this.dueDate=dueDate;this.generatedBy=generatedBy;this.generatedAt=LocalDateTime.now();}
    public void link(Long salnId,SalnTypes.RequirementStatus status){linkedSalnId=salnId;this.status=status;}
    public Long getId(){return id;} public Long getEmployeeId(){return employeeId;} public String getEmployeeNo(){return employeeNo;} public SalnTypes.FilingType getFilingType(){return filingType;} public LocalDate getReferenceDate(){return referenceDate;} public int getSalnYear(){return salnYear;} public LocalDate getDueDate(){return dueDate;} public SalnTypes.RequirementStatus getStatus(){return status;} public String getGeneratedBy(){return generatedBy;} public LocalDateTime getGeneratedAt(){return generatedAt;} public Long getLinkedSalnId(){return linkedSalnId;}
}
