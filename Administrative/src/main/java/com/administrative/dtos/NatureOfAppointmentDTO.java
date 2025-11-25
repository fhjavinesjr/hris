package com.administrative.dtos;

import java.io.Serializable;

public class NatureOfAppointmentDTO implements Serializable {

    private Long natureOfAppointmentId;
    private String code;
    private String nature;

    public NatureOfAppointmentDTO() {

    }

    public NatureOfAppointmentDTO(Long natureOfAppointmentId, String code, String nature) {
        this.natureOfAppointmentId = natureOfAppointmentId;
        this.code = code;
        this.nature = nature;
    }

    public Long getNatureOfAppointmentId() {
        return natureOfAppointmentId;
    }

    public void setNatureOfAppointmentId(Long natureOfAppointmentId) {
        this.natureOfAppointmentId = natureOfAppointmentId;
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