package com.administrative.dtos;

import java.io.Serializable;

public class CivilStatusDTO implements Serializable {

    private Long civilStatusId;
    private String code;
    private String name;

    public CivilStatusDTO() {

    }

    public CivilStatusDTO(Long civilStatusId, String code, String name) {
        this.civilStatusId = civilStatusId;
        this.code = code;
        this.name = name;
    }

    public Long getCivilStatusId() {
        return civilStatusId;
    }

    public void setCivilStatusId(Long civilStatusId) {
        this.civilStatusId = civilStatusId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}