package com.administrative.dtos;

import java.io.Serializable;

public class NatureOfSeparationDTO implements Serializable {

    private Long natureOfSeparationId;
    private String code;
    private String nature;

    public NatureOfSeparationDTO() {

    }

    public NatureOfSeparationDTO(Long natureOfSeparationId, String code, String nature) {
        this.natureOfSeparationId = natureOfSeparationId;
        this.code = code;
        this.nature = nature;
    }

    public Long getNatureOfSeparationId() {
        return natureOfSeparationId;
    }

    public void setNatureOfSeparationId(Long natureOfSeparationId) {
        this.natureOfSeparationId = natureOfSeparationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }
}