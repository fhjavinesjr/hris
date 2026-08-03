package com.primehr.integration.administrative;

public class AuthorizationDependencyException extends RuntimeException {
    public AuthorizationDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
