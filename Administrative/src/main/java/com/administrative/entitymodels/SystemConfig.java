package com.administrative.entitymodels;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "system_config")
public class SystemConfig implements Serializable {

    @Id
    @Column(name = "configKey", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "configValue", nullable = false, length = 500)
    private String configValue;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "editable", nullable = false)
    private Boolean editable = true;

    public SystemConfig() {
    }

    public SystemConfig(String configKey, String configValue, String description, String category, Boolean editable) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
        this.category = category;
        this.editable = editable;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
}
