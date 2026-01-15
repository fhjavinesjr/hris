package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "officialengagement")
public class OfficialEngagement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "officialEngagementId")
    private Long officialEngagementId;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "name", length = 100, unique = true, nullable = false)
    private String name;

    public OfficialEngagement() {

    }

    public OfficialEngagement(Long officialEngagementId, String code, String name) {
        this.officialEngagementId = officialEngagementId;
        this.code = code;
        this.name = name;
    }

    public Long getOfficialEngagementId() {
        return officialEngagementId;
    }

    public void setOfficialEngagementId(Long officialEngagementId) {
        this.officialEngagementId = officialEngagementId;
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