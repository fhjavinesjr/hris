package com.primehr.integration.support;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

public class SafeReadRetryInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SafeReadRetryInterceptor.class);
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(429, 502, 503, 504);
    private final int maximumAttempts;
    private final Duration delay;
    private final String dependency;

    public SafeReadRetryInterceptor(int maximumAttempts, Duration delay) {
        this("External dependency", maximumAttempts, delay);
    }

    public SafeReadRetryInterceptor(String dependency, int maximumAttempts, Duration delay) {
        this.dependency = dependency == null || dependency.isBlank()
                ? "External dependency" : dependency;
        this.maximumAttempts = Math.max(1, Math.min(3, maximumAttempts));
        this.delay = delay == null || delay.isNegative() ? Duration.ZERO : delay;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (!isSafeRead(request.getMethod())) return execution.execute(request, body);
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= maximumAttempts; attempt++) {
            try {
                ClientHttpResponse response = execution.execute(request, body);
                if (attempt == maximumAttempts || !RETRYABLE_STATUSES.contains(response.getStatusCode().value())) {
                    return response;
                }
                log.warn("{} safe read retry {}/{} after downstream HTTP {}",
                        dependency, attempt, maximumAttempts, response.getStatusCode().value());
                response.close();
            } catch (IOException exception) {
                lastFailure = exception;
                if (attempt == maximumAttempts) throw exception;
                log.warn("{} safe read retry {}/{} after transport failure",
                        dependency, attempt, maximumAttempts);
            }
            pause();
        }
        throw lastFailure == null ? new IOException("Safe-read retry exhausted") : lastFailure;
    }

    private static boolean isSafeRead(HttpMethod method) {
        return HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method);
    }

    private void pause() throws IOException {
        if (delay.isZero()) return;
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Safe-read retry interrupted", exception);
        }
    }
}
