package com.primehr.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = CorrelationContext.normalizeOrGenerate(
                request.getHeader(CorrelationContext.HEADER));
        HttpServletRequest wrapped = new CorrelationRequestWrapper(request, correlationId);
        wrapped.setAttribute(CorrelationContext.REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(CorrelationContext.HEADER, correlationId);
        MDC.put(CorrelationContext.MDC_KEY, correlationId);
        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            MDC.remove(CorrelationContext.MDC_KEY);
        }
    }

    private static final class CorrelationRequestWrapper extends HttpServletRequestWrapper {
        private final String correlationId;

        private CorrelationRequestWrapper(HttpServletRequest request, String correlationId) {
            super(request);
            this.correlationId = correlationId;
        }

        @Override
        public String getHeader(String name) {
            return CorrelationContext.HEADER.equalsIgnoreCase(name)
                    ? correlationId
                    : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return CorrelationContext.HEADER.equalsIgnoreCase(name)
                    ? Collections.enumeration(Collections.singleton(correlationId))
                    : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> existing = super.getHeaderNames();
            if (existing != null) existing.asIterator().forEachRemaining(names::add);
            names.add(CorrelationContext.HEADER);
            return Collections.enumeration(names);
        }
    }
}
