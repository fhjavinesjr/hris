package com.administrative.dtos;

import java.io.Serializable;

public class AreasDTO implements Serializable {

    private Long areasId;
    private String areasName;
    private String areasDescription;

    public AreasDTO() {

    }

    public AreasDTO(Long areasId, String areasName, String areasDescription) {
        this.areasId = areasId;
        this.areasName = areasName;
        this.areasDescription = areasDescription;
    }

    public Long getAreasId() {
        return areasId;
    }

    public void setAreasId(Long areasId) {
        this.areasId = areasId;
    }

    public String getAreasName() {
        return areasName;
    }

    public void setAreasName(String areasName) {
        this.areasName = areasName;
    }

    public String getAreasDescription() {
        return areasDescription;
    }

    public void setAreasDescription(String areasDescription) {
        this.areasDescription = areasDescription;
    }
}