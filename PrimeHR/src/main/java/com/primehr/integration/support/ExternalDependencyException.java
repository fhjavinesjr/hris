package com.primehr.integration.support;

public class ExternalDependencyException extends RuntimeException {
    private final String dependency;
    private final String code;
    private final Integer downstreamStatus;

    public ExternalDependencyException(String dependency, String code, Integer downstreamStatus,
                                       String message, Throwable cause) {
        super(message, cause);
        this.dependency = dependency;
        this.code = code;
        this.downstreamStatus = downstreamStatus;
    }

    public String dependency() {
        return dependency;
    }

    public String code() {
        return code;
    }

    public Integer downstreamStatus() {
        return downstreamStatus;
    }
}
