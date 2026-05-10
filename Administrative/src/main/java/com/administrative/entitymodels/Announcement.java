package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "announcements")
public class Announcement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcementId")
    private Long announcementId;

    @Column(name = "effectivityDate", nullable = false)
    private String effectivityDate;

    @Column(name = "effectiveUntil", nullable = false)
    private String effectiveUntil;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    public Announcement() {}

    public Announcement(Long announcementId, String effectivityDate, String effectiveUntil, String title, String content) {
        this.announcementId = announcementId;
        this.effectivityDate = effectivityDate;
        this.effectiveUntil = effectiveUntil;
        this.title = title;
        this.content = content;
    }

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
