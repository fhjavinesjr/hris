package com.administrative.dtos;

import java.io.Serializable;

public class OfficialEngagementDTO implements Serializable {

    private Long officialEngagementId;
    private String code;
    private String name;

    public OfficialEngagementDTO() {

    }

    public OfficialEngagementDTO(Long officialEngagementId, String code, String name) {
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