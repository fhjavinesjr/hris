package com.primehr.rsp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "rsp_vacancy_publication_channel")
public class VacancyPublicationChannel extends RspAuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publication_id", nullable = false)
    private VacancyPublication publication;

    @Column(name = "channel_name", nullable = false, length = 200)
    private String channelName;

    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(name = "reference", length = 1000)
    private String reference;

    @Column(nullable = false)
    private boolean active;

    protected VacancyPublicationChannel() {
    }

    public VacancyPublicationChannel(String agencyId, VacancyPublication publication, String channelName,
                                     LocalDate publicationDate, String reference) {
        super(agencyId);
        this.publication = publication;
        this.channelName = text(channelName, "channelName");
        this.publicationDate = java.util.Objects.requireNonNull(publicationDate, "publicationDate");
        this.reference = optional(reference);
        this.active = true;
    }

    public void archive() {
        publication.requireEditable();
        active = false;
    }

    public VacancyPublication getPublication() { return publication; }
    public String getChannelName() { return channelName; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public String getReference() { return reference; }
    public boolean isActive() { return active; }
}
