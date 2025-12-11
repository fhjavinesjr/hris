package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "natureofseparation")
public class NatureOfSeparation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "natureOfSeparationId")
    private Long natureOfSeparationId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "nature", length = 100, unique = true, nullable = false)
    private String nature;

    public NatureOfSeparation() {

    }

    public NatureOfSeparation(Long natureOfSeparationId, String code, String nature) {
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