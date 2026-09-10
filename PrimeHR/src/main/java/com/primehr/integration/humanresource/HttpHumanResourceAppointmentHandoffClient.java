package com.primehr.integration.humanresource;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.primehr.config.PrimeHrProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
class HttpHumanResourceAppointmentHandoffClient implements HumanResourceAppointmentHandoffClient {
    private static final String PATH="/api/integration/v1/primehr/appointment-handoffs";
    private final RestClient rest;private final String endpoint;private final String secret;private final String issuer;private final String audience;private final long tokenSeconds;
    HttpHumanResourceAppointmentHandoffClient(PrimeHrProperties properties,
        @Value("${primehr.handoff.jwt-secret:}")String secret,@Value("${primehr.handoff.issuer:primehr}")String issuer,
        @Value("${primehr.handoff.audience:humanresource}")String audience,@Value("${primehr.handoff.token-seconds:60}")long tokenSeconds){
        var c=properties.humanResource();HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofMillis(c.connectTimeoutMillis())).build();JdkClientHttpRequestFactory factory=new JdkClientHttpRequestFactory(client);factory.setReadTimeout(Duration.ofMillis(c.readTimeoutMillis()));rest=RestClient.builder().baseUrl(c.baseUrl()).requestFactory(factory).build();endpoint=c.baseUrl()+PATH;this.secret=secret;this.issuer=issuer;this.audience=audience;this.tokenSeconds=Math.max(15,tokenSeconds);
    }
    public DeliveryResult deliver(DeliveryRequest request){try{Receipt receipt=rest.post().uri(PATH).header(HttpHeaders.AUTHORIZATION,bearer(request.agencyId())).header("Idempotency-Key",request.handoffId()).header("X-Correlation-Id",safe(request.correlationId())).body(request).retrieve().body(Receipt.class);return receipt==null?failure("EMPTY_RESPONSE",null,"HumanResource returned no receipt"):new DeliveryResult("ACKNOWLEDGED",202,null,receipt);}catch(HttpStatusCodeException e){return httpFailure(e);}catch(RestClientException|IllegalStateException e){return failure("UNAVAILABLE",null,safeDiagnostic(e));}}
    public DeliveryResult reconcile(String agency,String handoff,String correlation){try{Receipt receipt=rest.get().uri(PATH+"/{id}",handoff).header(HttpHeaders.AUTHORIZATION,bearer(agency)).header("X-Correlation-Id",safe(correlation)).retrieve().body(Receipt.class);return receipt==null?failure("NOT_FOUND",404,"HumanResource receipt was not found"):new DeliveryResult("RECONCILED",200,null,receipt);}catch(HttpStatusCodeException e){return httpFailure(e);}catch(RestClientException|IllegalStateException e){return failure("UNAVAILABLE",null,safeDiagnostic(e));}}
    private DeliveryResult httpFailure(HttpStatusCodeException e){int status=e.getStatusCode().value();String category=status==401||status==403?"UNAUTHORIZED":status==409?"CONFLICT":status>=500?"UNAVAILABLE":"REJECTED";return failure(category,status,"HumanResource rejected the handoff (HTTP "+status+")");}
    private DeliveryResult failure(String category,Integer status,String diagnostic){return new DeliveryResult(category,status,diagnostic,null);}
    private String bearer(String agency){if(secret==null||secret.length()<32)throw new IllegalStateException("PrimeHR handoff service secret is not configured");Instant now=Instant.now();String token=JWT.create().withIssuer(issuer).withAudience(audience).withSubject("primehr-service").withClaim("scope",List.of("appointment-handoff.write")).withClaim("agencyId",agency).withIssuedAt(Date.from(now)).withExpiresAt(Date.from(now.plusSeconds(tokenSeconds))).sign(Algorithm.HMAC256(secret));return "Bearer "+token;}
    private String safe(String value){return value==null?"":value;}
    private String safeDiagnostic(Exception e){String value=e.getMessage();if(value==null||value.isBlank())value=e.getClass().getSimpleName();return value.length()>500?value.substring(0,500):value;}
    String endpointIdentity(){return endpoint;}
}
