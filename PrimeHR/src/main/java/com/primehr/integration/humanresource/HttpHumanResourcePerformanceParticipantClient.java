package com.primehr.integration.humanresource;

import com.primehr.config.PrimeHrProperties;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class HttpHumanResourcePerformanceParticipantClient implements HumanResourcePerformanceParticipantClient {
    private final RestClient rest;
    public HttpHumanResourcePerformanceParticipantClient(PrimeHrProperties p){var c=p.humanResource();HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofMillis(c.connectTimeoutMillis())).build();JdkClientHttpRequestFactory f=new JdkClientHttpRequestFactory(client);f.setReadTimeout(Duration.ofMillis(c.readTimeoutMillis()));rest=RestClient.builder().baseUrl(c.baseUrl()).requestFactory(f).build();}
    @Override public HumanResourceAssessmentSubject get(Long id,String token){return read("/api/integration/v1/primehr/performance-participants/{value}",id,token);}
    @Override public HumanResourceAssessmentSubject getByEmployeeNo(String no,String token){return read("/api/integration/v1/primehr/performance-participants/by-employee-no/{value}",no,token);}
    private HumanResourceAssessmentSubject read(String path,Object value,String token){try{HumanResourceAssessmentSubject result=rest.get().uri(path,value).header(HttpHeaders.AUTHORIZATION,token).retrieve().body(HumanResourceAssessmentSubject.class);if(result==null||!result.eligible())throw new HumanResourceDependencyException("HRM returned an invalid performance participant",null);return result;}catch(RestClientException e){throw new HumanResourceDependencyException("HRM performance participant is unavailable",e);}}
}
