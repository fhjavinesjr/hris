package com.primehr.integration.support;

import com.primehr.shared.web.CorrelationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorrelationPropagationInterceptorTest {
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesCurrentCorrelationAndPreservesExplicitHeader() throws Exception {
        CorrelationPropagationInterceptor interceptor = new CorrelationPropagationInterceptor();
        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(headers);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        ClientHttpRequestExecution execution = (ignored, body) -> response;
        MDC.put(CorrelationContext.MDC_KEY, "trace-456");

        interceptor.intercept(request, new byte[0], execution);
        assertThat(headers.getFirst(CorrelationContext.HEADER)).isEqualTo("trace-456");

        headers.set(CorrelationContext.HEADER, "handoff-trace");
        interceptor.intercept(request, new byte[0], execution);
        assertThat(headers.getFirst(CorrelationContext.HEADER)).isEqualTo("handoff-trace");
    }
}
