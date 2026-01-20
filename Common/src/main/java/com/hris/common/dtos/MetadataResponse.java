package com.hris.common.dtos;

public class MetadataResponse {

    private Long metaId;
    private String identifier;
    private String message;

    public MetadataResponse() {

    }

    public MetadataResponse(String message) {
        this.message = message;
    }

    public MetadataResponse(String identifier, String message) {
        this.identifier = identifier;
        this.message = message;
    }

    public MetadataResponse(Long metaId, String message) {
        this.message = message;
        this.metaId = metaId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Long getMetaId() {
        return metaId;
    }

    public void setMetaId(Long metaId) {
        this.metaId = metaId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}