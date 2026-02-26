package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "areas")
public class Areas implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "areasId")
    private Long areasId;

    @Column(name = "areasName")
    private String areasName;

    @Column(name = "areasDescription")
    private String areasDescription;

    public Areas() {

    }

    public Areas(Long areasId, String areasName, String areasDescription) {
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