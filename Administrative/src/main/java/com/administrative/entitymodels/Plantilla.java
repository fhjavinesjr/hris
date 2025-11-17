package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "plantilla")
public class Plantilla implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plantillaId")
    private Long plantillaId;

    @Column(name = "plantillaName", length = 100, unique = true, nullable = false)
    private String plantillaName;

    @Column(name = "jobPositionId", length = 50)
    private Long jobPositionId;

    public Plantilla() {

    }

    public Plantilla(Long plantillaId, String plantillaName, Long jobPositionId) {
        this.plantillaId = plantillaId;
        this.plantillaName = plantillaName;
        this.jobPositionId = jobPositionId;
    }

    public Long getPlantillaId() {
        return plantillaId;
    }

    public void setPlantillaId(Long plantillaId) {
        this.plantillaId = plantillaId;
    }

    public String getPlantillaName() {
        return plantillaName;
    }

    public void setPlantillaName(String plantillaName) {
        this.plantillaName = plantillaName;
    }

    public Long getJobPositionId() {
        return jobPositionId;
    }

    public void setJobPositionId(Long jobPositionId) {
        this.jobPositionId = jobPositionId;
    }
}