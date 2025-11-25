package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "natureofappointment")
public class NatureOfAppointment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "natureofappointmentId")
    private Long natureOfAppointmentId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "nature", length = 100, unique = true, nullable = false)
    private String nature;

    public NatureOfAppointment() {

    }

    public NatureOfAppointment(Long natureOfAppointmentId, String code, String nature) {
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