package com.humanresource.entitymodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "settings")
public class ReportHeaderSettings {

    @Id
    @Column(name = "settingsId")
    private Long settingsId;

    @Lob
    @Column(name = "leftHeaderLogo")
    private byte[] leftHeaderLogo;

    @Lob
    @Column(name = "rightHeaderLogo")
    private byte[] rightHeaderLogo;

    protected ReportHeaderSettings() {
    }

    public Long getSettingsId() {
        return settingsId;
    }

    public byte[] getLeftHeaderLogo() {
        return leftHeaderLogo;
    }

    public byte[] getRightHeaderLogo() {
        return rightHeaderLogo;
    }
}
