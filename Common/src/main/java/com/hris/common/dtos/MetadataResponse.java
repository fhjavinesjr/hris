package com.hris.common.dtos;

public class MetadataResponse {

    private Long metaId;
    private String message;

    public MetadataResponse() {

    }

    public MetadataResponse(String message) {
        this.message = message;
    }

    public MetadataResponse(Long metaId, String message) {
        this.metaId = metaId;
        this.message = message;
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