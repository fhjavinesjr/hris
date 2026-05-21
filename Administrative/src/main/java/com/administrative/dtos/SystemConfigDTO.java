package com.administrative.dtos;

import java.io.Serializable;

public class SystemConfigDTO implements Serializable {

    private String configKey;
    private String configValue;
    private String description;
    private String category;
    private Boolean editable;

    public SystemConfigDTO() {
    }

    public SystemConfigDTO(String configKey, String configValue, String description, String category, Boolean editable) {
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
