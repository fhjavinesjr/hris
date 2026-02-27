package com.administrative.dtos;

import java.io.Serializable;

public class BusinessUnitsDTO implements Serializable {

    private Long businessUnitsId;
    private String businessUnitsCode;
    private String businessUnitsName;
    private Long areasId;

    private AreasDTO areasDTO;

    public BusinessUnitsDTO() {

    }

    public BusinessUnitsDTO(Long businessUnitsId, String businessUnitsCode, String businessUnitsName, Long areasId, AreasDTO areasDTO) {
        this.businessUnitsId = businessUnitsId;
        this.businessUnitsCode = businessUnitsCode;
        this.businessUnitsName = businessUnitsName;
        this.areasId = areasId;
        this.areasDTO = areasDTO;
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

    public Long getAreasId() {
        return areasId;
    }

    public void setAreasId(Long areasId) {
        this.areasId = areasId;
    }

    public AreasDTO getAreasDTO() {
        return areasDTO;
    }

    public void setAreasDTO(AreasDTO areasDTO) {
        this.areasDTO = areasDTO;
    }
}