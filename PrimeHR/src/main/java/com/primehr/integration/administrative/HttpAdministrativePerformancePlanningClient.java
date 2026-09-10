package com.primehr.integration.administrative;

import com.primehr.config.PrimeHrProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class HttpAdministrativePerformancePlanningClient implements AdministrativePerformancePlanningClient {
    private final RestClient rest;
    public HttpAdministrativePerformancePlanningClient(PrimeHrProperties p){var c=p.administrative();HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofMillis(c.connectTimeoutMillis())).build();JdkClientHttpRequestFactory f=new JdkClientHttpRequestFactory(client);f.setReadTimeout(Duration.ofMillis(c.readTimeoutMillis()));rest=RestClient.builder().baseUrl(c.baseUrl()).requestFactory(f).build();}
    @Override public OrganizationTarget organization(String type,Long id,String token){return get("/api/integration/v1/primehr/performance/organization-targets/{type}/{id}",token,OrganizationTarget.class,type,id);}
    @Override public PersonnelMembership membership(Long employeeId,String token){return get("/api/integration/v1/primehr/performance/personnel-membership/{id}",token,PersonnelMembership.class,employeeId);}
    @Override public ApprovalRoute approvalRoute(Long unit,String code,String token){try{ApprovalRoute value=rest.get().uri(b->b.path("/api/integration/v1/primehr/performance/approval-routes").queryParam("businessUnitId",unit).queryParam("requestCode",code).build()).header(HttpHeaders.AUTHORIZATION,token).retrieve().body(ApprovalRoute.class);if(value==null)throw new OrganizationDirectoryDependencyException("Administrative returned no approval route",null);return value;}catch(RestClientException e){throw new OrganizationDirectoryDependencyException("Administrative performance approval route is unavailable",e);}}
    private <T>T get(String path,String token,Class<T> type,Object...vars){try{T value=rest.get().uri(path,vars).header(HttpHeaders.AUTHORIZATION,token).retrieve().body(type);if(value==null)throw new OrganizationDirectoryDependencyException("Administrative returned no performance source",null);return value;}catch(RestClientException e){throw new OrganizationDirectoryDependencyException("Administrative performance source is unavailable",e);}}
}
