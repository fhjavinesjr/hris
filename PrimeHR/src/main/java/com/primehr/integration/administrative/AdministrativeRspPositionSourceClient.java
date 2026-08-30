package com.primehr.integration.administrative;

import com.primehr.config.PrimeHrProperties;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.net.http.HttpClient;
import java.time.*;

@Component
public class AdministrativeRspPositionSourceClient {
    private final RestClient rest;
    public AdministrativeRspPositionSourceClient(PrimeHrProperties p){var c=p.administrative();HttpClient h=HttpClient.newBuilder().connectTimeout(Duration.ofMillis(c.connectTimeoutMillis())).build();var f=new JdkClientHttpRequestFactory(h);f.setReadTimeout(Duration.ofMillis(c.readTimeoutMillis()));rest=RestClient.builder().baseUrl(c.baseUrl()).requestFactory(f).build();}
    public AdministrativeRspPositionSource get(Long plantillaId,Long businessUnitId,LocalDate asOf,String token){try{
        AdministrativeRspPositionSource s=rest.get().uri(u->u.path("/api/integration/v1/primehr/rsp/position-sources/{id}").queryParam("businessUnitId",businessUnitId).queryParam("asOf",asOf).build(plantillaId)).header(HttpHeaders.AUTHORIZATION,token).retrieve()
                .onStatus(x->x.value()==401||x.value()==403,(a,b)->{throw new AccessDeniedException("Access denied");})
                .onStatus(HttpStatusCode::isError,(a,b)->{throw new PositionTargetDependencyException("Administrative RSP source is unavailable",null);}).body(AdministrativeRspPositionSource.class);
        if(s==null||!plantillaId.equals(s.plantillaId())||s.qualificationStandardId()==null||s.qualificationStandardVersion()<1||blank(s.sourceFingerprint()))throw new PositionTargetDependencyException("Administrative returned an invalid RSP source",null);return s;
    }catch(AccessDeniedException|PositionTargetDependencyException e){throw e;}catch(RestClientException e){throw new PositionTargetDependencyException("Administrative RSP source is unavailable",e);}}
    private static boolean blank(String v){return v==null||v.isBlank();}
}
