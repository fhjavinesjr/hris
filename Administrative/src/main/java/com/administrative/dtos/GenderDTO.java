package com.administrative.dtos;

import java.io.Serializable;

public class GenderDTO implements Serializable {

    private Long genderId;
    private String code;
    private String name;

    public GenderDTO() {

    }

    public GenderDTO(Long genderId, String code, String name) {
        this.genderId = genderId;
        this.code = code;
        this.name = name;
    }

    public Long getGenderId() {
        return genderId;
    }

    public void setGenderId(Long genderId) {
        this.genderId = genderId;
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