package com.primehr.shared.exception;

public class IllegalLifecycleTransitionException extends RuntimeException {
    public IllegalLifecycleTransitionException(String message) {
        super(message);
    }
}
