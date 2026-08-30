package com.primehr.integration.humanresource;

import com.primehr.config.PrimeHrProperties;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class HumanResourcePlantillaOccupancyClient {
    private final RestClient rest;
    public HumanResourcePlantillaOccupancyClient(PrimeHrProperties p){var c=p.humanResource();HttpClient h=HttpClient.newBuilder().connectTimeout(Duration.ofMillis(c.connectTimeoutMillis())).build();var f=new JdkClientHttpRequestFactory(h);f.setReadTimeout(Duration.ofMillis(c.readTimeoutMillis()));rest=RestClient.builder().baseUrl(c.baseUrl()).requestFactory(f).build();}
    public HumanResourcePlantillaOccupancy get(Long id,String token){try{HumanResourcePlantillaOccupancy o=rest.get().uri("/api/integration/v1/primehr/plantilla-occupancy/{id}",id).header(HttpHeaders.AUTHORIZATION,token).retrieve()
            .onStatus(x->x.value()==401||x.value()==403,(a,b)->{throw new AccessDeniedException("Access denied");})
            .onStatus(HttpStatusCode::isError,(a,b)->{throw new HumanResourceDependencyException("HumanResource Plantilla occupancy is unavailable",null);}).body(HumanResourcePlantillaOccupancy.class);
        if(o==null||!id.equals(o.plantillaId())||o.sourceFingerprint()==null||o.fetchedAt()==null||o.occupied()&&(o.activeAppointmentId()==null||o.assumptionToDutyDate()==null))throw new HumanResourceDependencyException("HumanResource returned invalid Plantilla occupancy",null);return o;
    }catch(AccessDeniedException|HumanResourceDependencyException e){throw e;}catch(RestClientException e){throw new HumanResourceDependencyException("HumanResource Plantilla occupancy is unavailable",e);}}
}
