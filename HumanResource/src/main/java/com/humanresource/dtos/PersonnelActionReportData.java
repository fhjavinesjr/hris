package com.humanresource.dtos;

public class PersonnelActionReportData {

    private final String companyName;
    private final String companyAddress;
    private final String employeeName;
    private final String natureOfAction;
    private final String effectiveDate;
    private final String fromPosition;
    private final String toPosition;
    private final String fromSalary;
    private final String toSalary;
    private final String fromSection;
    private final String toSection;
    private final String fromDivision;
    private final String toDivision;
    private final String remarks;
    private final byte[] leftHeaderLogo;
    private final byte[] rightHeaderLogo;

    public PersonnelActionReportData(
            String companyName,
            String companyAddress,
            String employeeName,
            String natureOfAction,
            String effectiveDate,
            String fromPosition,
            String toPosition,
            String fromSalary,
            String toSalary,
            String fromSection,
            String toSection,
            String fromDivision,
            String toDivision,
            String remarks,
            byte[] leftHeaderLogo,
            byte[] rightHeaderLogo) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.employeeName = employeeName;
        this.natureOfAction = natureOfAction;
        this.effectiveDate = effectiveDate;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.fromSalary = fromSalary;
        this.toSalary = toSalary;
        this.fromSection = fromSection;
        this.toSection = toSection;
        this.fromDivision = fromDivision;
        this.toDivision = toDivision;
        this.remarks = remarks;
        this.leftHeaderLogo = leftHeaderLogo;
        this.rightHeaderLogo = rightHeaderLogo;
    }

    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getEmployeeName() { return employeeName; }
    public String getNatureOfAction() { return natureOfAction; }
    public String getEffectiveDate() { return effectiveDate; }
    public String getFromPosition() { return fromPosition; }
    public String getToPosition() { return toPosition; }
    public String getFromSalary() { return fromSalary; }
    public String getToSalary() { return toSalary; }
    public String getFromSection() { return fromSection; }
    public String getToSection() { return toSection; }
    public String getFromDivision() { return fromDivision; }
    public String getToDivision() { return toDivision; }
    public String getRemarks() { return remarks; }
    public byte[] getLeftHeaderLogo() { return leftHeaderLogo; }
    public byte[] getRightHeaderLogo() { return rightHeaderLogo; }
}
