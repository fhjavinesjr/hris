package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "gender")
public class Gender implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genderId")
    private Long genderId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    public Gender() {

    }

    public Gender(Long genderId, String code, String name) {
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