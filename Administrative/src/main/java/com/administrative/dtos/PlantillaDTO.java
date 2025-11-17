package com.administrative.dtos;

import java.io.Serializable;

public class PlantillaDTO implements Serializable {

    private Long plantillaId;
    private String plantillaName;
    private Long jobPositionId;

    public PlantillaDTO() {

    }

    public PlantillaDTO(Long plantillaId, String plantillaName, Long jobPositionId) {
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