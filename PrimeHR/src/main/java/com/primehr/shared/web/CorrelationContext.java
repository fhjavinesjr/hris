package com.primehr.shared.web;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

public final class CorrelationContext {
    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    public static final String REQUEST_ATTRIBUTE = CorrelationContext.class.getName() + ".value";
    private static final int MAXIMUM_LENGTH = 100;
    private static final Pattern ALLOWED = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,99}");

    private CorrelationContext() {
    }

    public static String normalizeOrGenerate(String candidate) {
        if (candidate != null) {
            String trimmed = candidate.trim();
            if (trimmed.length() <= MAXIMUM_LENGTH && ALLOWED.matcher(trimmed).matches()) {
                return trimmed;
            }
        }
        return UUID.randomUUID().toString();
    }

    public static String currentOrGenerate() {
        return normalizeOrGenerate(MDC.get(MDC_KEY));
    }
}
