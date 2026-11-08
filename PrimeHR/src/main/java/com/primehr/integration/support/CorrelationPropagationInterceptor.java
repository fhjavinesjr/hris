package com.primehr.integration.support;

import com.primehr.shared.web.CorrelationContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class CorrelationPropagationInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        if (!request.getHeaders().containsKey(CorrelationContext.HEADER)) {
            request.getHeaders().set(CorrelationContext.HEADER, CorrelationContext.currentOrGenerate());
        }
        return execution.execute(request, body);
    }
}
