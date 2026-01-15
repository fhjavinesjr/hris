package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "civilstatus")
public class CivilStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "civilStatusId")
    private Long civilStatusId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    public CivilStatus() {

    }

    public CivilStatus(Long civilStatusId, String code, String name) {
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