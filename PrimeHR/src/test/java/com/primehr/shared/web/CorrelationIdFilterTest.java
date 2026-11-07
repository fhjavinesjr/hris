package com.primehr.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void preservesValidCorrelationIdAcrossRequestResponseAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        request.addHeader(CorrelationContext.HEADER, "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestHeader = new AtomicReference<>();
        AtomicReference<String> mdcValue = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) -> {
            requestHeader.set(((HttpServletRequest) servletRequest)
                    .getHeader(CorrelationContext.HEADER));
            mdcValue.set(MDC.get(CorrelationContext.MDC_KEY));
        };

        filter.doFilter(request, response, chain);

        assertThat(requestHeader.get()).isEqualTo("trace-123");
        assertThat(mdcValue.get()).isEqualTo("trace-123");
        assertThat(response.getHeader(CorrelationContext.HEADER)).isEqualTo("trace-123");
        assertThat(MDC.get(CorrelationContext.MDC_KEY)).isNull();
    }

    @Test
    void replacesUnsafeCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        request.addHeader(CorrelationContext.HEADER, "unsafe value with spaces");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> { });

        assertThat(response.getHeader(CorrelationContext.HEADER))
                .matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,99}")
                .isNotEqualTo("unsafe value with spaces");
    }
}
