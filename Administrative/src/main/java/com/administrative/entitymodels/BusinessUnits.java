package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "businessunits")
public class BusinessUnits implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "businessUnitsId")
    private Long businessUnitsId;

    @Column(name = "businessUnitsCode")
    private String businessUnitsCode;

    @Column(name = "businessUnitsName")
    private String businessUnitsName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "areasId") // This is the foreign key in the DB
    private Areas areas;

    public BusinessUnits() {

    }

    public BusinessUnits(Long businessUnitsId, String businessUnitsCode, String businessUnitsName, Areas areas) {
        this.businessUnitsId = businessUnitsId;
        this.businessUnitsCode = businessUnitsCode;
        this.businessUnitsName = businessUnitsName;
        this.areas = areas;
    }

    public Long getBusinessUnitsId() {
        return businessUnitsId;
    }

    public void setBusinessUnitsId(Long businessUnitsId) {
        this.businessUnitsId = businessUnitsId;
    }

    public String getBusinessUnitsCode() {
        return businessUnitsCode;
    }

    public void setBusinessUnitsCode(String businessUnitsCode) {
        this.businessUnitsCode = businessUnitsCode;
    }

    public String getBusinessUnitsName() {
        return businessUnitsName;
    }

    public void setBusinessUnitsName(String businessUnitsName) {
        this.businessUnitsName = businessUnitsName;
    }

    public Areas getAreas() {
        return areas;
    }

    public void setAreas(Areas areas) {
        this.areas = areas;
    }
}