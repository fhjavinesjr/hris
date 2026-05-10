package com.administrative.dtos;

import java.io.Serializable;

public class AnnouncementDTO implements Serializable {

    private Long announcementId;
    private String effectivityDate;
    private String effectiveUntil;
    private String title;
    private String content;

    public AnnouncementDTO() {}

    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }

    public String getEffectivityDate() { return effectivityDate; }
    public void setEffectivityDate(String effectivityDate) { this.effectivityDate = effectivityDate; }

    public String getEffectiveUntil() { return effectiveUntil; }
    public void setEffectiveUntil(String effectiveUntil) { this.effectiveUntil = effectiveUntil; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
