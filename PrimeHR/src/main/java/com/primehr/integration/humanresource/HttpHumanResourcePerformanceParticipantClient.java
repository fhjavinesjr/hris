package com.primehr.integration.humanresource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpHumanResourcePerformanceParticipantClient implements HumanResourcePerformanceParticipantClient {
    private final RestClient rest;
    public HttpHumanResourcePerformanceParticipantClient(
            @Qualifier("humanResourceRestClient") RestClient rest) {
        this.rest = rest;
    }
    @Override public HumanResourceAssessmentSubject get(Long id,String token){return read("/api/integration/v1/primehr/performance-participants/{value}",id,token);}
    @Override public HumanResourceAssessmentSubject getByEmployeeNo(String no,String token){return read("/api/integration/v1/primehr/performance-participants/by-employee-no/{value}",no,token);}
    private HumanResourceAssessmentSubject read(String path,Object value,String token){try{HumanResourceAssessmentSubject result=rest.get().uri(path,value).header(HttpHeaders.AUTHORIZATION,token).retrieve().body(HumanResourceAssessmentSubject.class);if(result==null||!result.eligible())throw new HumanResourceDependencyException("HRM returned an invalid performance participant",null);return result;}catch(RestClientException e){throw new HumanResourceDependencyException("HRM performance participant is unavailable",e);}}
}
