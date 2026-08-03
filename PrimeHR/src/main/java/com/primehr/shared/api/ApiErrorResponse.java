package com.primehr.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String path,
        List<String> details
) {
    public ApiErrorResponse {
        details = details == null ? List.of() : List.copyOf(details);
    }
}
