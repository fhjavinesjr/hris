package com.administrative.entitymodels;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "holiday")
public class Holiday implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holidayId")
    private Long holidayId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
    @Column(name = "holidayDate", nullable = false)
    private LocalDate holidayDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyyy")
    @Column(name = "observedDate")
    private LocalDate observedDate;

    @Column(name = "holidayType", length = 50, nullable = false)
    private String holidayType;

    @Column(name = "holidayScope", length = 50, nullable = false)
    private String holidayScope;

    @Column(name = "localityCode", length = 50)
    private String localityCode;

    @Column(name = "sourceReference", length = 150)
    private String sourceReference;

    @Column(name = "withPay", nullable = false)
    private Boolean withPay = true;

    @Column(name = "isWorkingHoliday", nullable = false)
    private Boolean isWorkingHoliday = false;

    @Column(name = "isActive", nullable = false)
    private Boolean isActive = true;

    public Holiday() {
    }

    public Holiday(Long holidayId, String code, String name, LocalDate holidayDate, LocalDate observedDate,
                   String holidayType, String holidayScope, String localityCode, String sourceReference,
                   Boolean withPay, Boolean isWorkingHoliday, Boolean isActive) {
        this.holidayId = holidayId;
        this.code = code;
        this.name = name;
        this.holidayDate = holidayDate;
        this.observedDate = observedDate;
        this.holidayType = holidayType;
        this.holidayScope = holidayScope;
        this.localityCode = localityCode;
        this.sourceReference = sourceReference;
        this.withPay = withPay;
        this.isWorkingHoliday = isWorkingHoliday;
        this.isActive = isActive;
    }

    public Long getHolidayId() {
        return holidayId;
    }

    public void setHolidayId(Long holidayId) {
        this.holidayId = holidayId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public LocalDate getObservedDate() {
        return observedDate;
    }

    public void setObservedDate(LocalDate observedDate) {
        this.observedDate = observedDate;
    }

    public String getHolidayType() {
        return holidayType;
    }

    public void setHolidayType(String holidayType) {
        this.holidayType = holidayType;
    }

    public String getHolidayScope() {
        return holidayScope;
    }

    public void setHolidayScope(String holidayScope) {
        this.holidayScope = holidayScope;
    }

    public String getLocalityCode() {
        return localityCode;
    }

    public void setLocalityCode(String localityCode) {
        this.localityCode = localityCode;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public Boolean getWithPay() {
        return withPay;
    }

    public void setWithPay(Boolean withPay) {
        this.withPay = withPay;
    }

    public Boolean getIsWorkingHoliday() {
        return isWorkingHoliday;
    }

    public void setIsWorkingHoliday(Boolean isWorkingHoliday) {
        this.isWorkingHoliday = isWorkingHoliday;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
