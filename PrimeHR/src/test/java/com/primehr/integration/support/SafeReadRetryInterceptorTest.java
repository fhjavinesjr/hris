package com.primehr.integration.support;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SafeReadRetryInterceptorTest {
    @Test
    void retriesTransientSafeReadAndClosesDiscardedResponse() throws Exception {
        SafeReadRetryInterceptor interceptor = new SafeReadRetryInterceptor(2, Duration.ZERO);
        HttpRequest request = request(HttpMethod.GET);
        ClientHttpResponse unavailable = response(HttpStatus.SERVICE_UNAVAILABLE);
        ClientHttpResponse success = response(HttpStatus.OK);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (ignored, body) ->
                attempts.getAndIncrement() == 0 ? unavailable : success;

        assertThat(interceptor.intercept(request, new byte[0], execution)).isSameAs(success);
        assertThat(attempts).hasValue(2);
        verify(unavailable).close();
    }

    @Test
    void retriesIoFailureForSafeRead() throws Exception {
        SafeReadRetryInterceptor interceptor = new SafeReadRetryInterceptor(2, Duration.ZERO);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpResponse success = response(HttpStatus.OK);
        ClientHttpRequestExecution execution = (ignored, body) -> {
            if (attempts.getAndIncrement() == 0) throw new IOException("timeout");
            return success;
        };

        assertThat(interceptor.intercept(request(HttpMethod.GET), new byte[0], execution))
                .isSameAs(success);
        assertThat(attempts).hasValue(2);
    }

    @Test
    void neverRetriesMutationOrNonTransientClientFailure() throws Exception {
        SafeReadRetryInterceptor interceptor = new SafeReadRetryInterceptor(3, Duration.ZERO);
        AtomicInteger postAttempts = new AtomicInteger();
        ClientHttpResponse unavailable = response(HttpStatus.SERVICE_UNAVAILABLE);
        interceptor.intercept(request(HttpMethod.POST), new byte[0], (ignored, body) -> {
            postAttempts.incrementAndGet();
            return unavailable;
        });
        assertThat(postAttempts).hasValue(1);

        AtomicInteger unauthorizedAttempts = new AtomicInteger();
        ClientHttpResponse unauthorized = response(HttpStatus.UNAUTHORIZED);
        interceptor.intercept(request(HttpMethod.GET), new byte[0], (ignored, body) -> {
            unauthorizedAttempts.incrementAndGet();
            return unauthorized;
        });
        assertThat(unauthorizedAttempts).hasValue(1);
    }

    private static HttpRequest request(HttpMethod method) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getMethod()).thenReturn(method);
        return request;
    }

    private static ClientHttpResponse response(HttpStatus status) throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(status);
        return response;
    }
}
