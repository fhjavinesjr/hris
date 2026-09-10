package com.humanresource.integration.primehr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
public class PrimeHrPerformanceParticipantAuthorization {
    public static final String FEATURE_KEY="primehr.performance-plan-assignment";
    private final RestTemplate rest;private final String url;
    public PrimeHrPerformanceParticipantAuthorization(@Value("${primehr.administrative.base-url:}")String base,
            @Value("${primehr.administrative.connect-timeout-millis:3000}")int connect,
            @Value("${primehr.administrative.read-timeout-millis:5000}")int read){String normalized=base==null?"":base.trim().replaceAll("/+$","");if(normalized.isEmpty()){rest=null;url=null;return;}SimpleClientHttpRequestFactory f=new SimpleClientHttpRequestFactory();f.setConnectTimeout(Math.max(250,connect));f.setReadTimeout(Math.max(250,read));rest=new RestTemplate(f);url=normalized+"/api/authorization/effective?featureKey="+FEATURE_KEY;}
    public void requireAgencyWide(String token){if(token==null||!token.regionMatches(true,0,"Bearer ",0,7))throw new AccessDeniedException("A bearer token is required");if(rest==null)throw unavailable(null);try{HttpHeaders h=new HttpHeaders();h.set(HttpHeaders.AUTHORIZATION,token);AdministrativePermissionResponse p=rest.exchange(url,HttpMethod.GET,new HttpEntity<>(h),AdministrativePermissionResponse.class).getBody();if(p==null||!FEATURE_KEY.equals(p.featureKey()))throw unavailable(null);if(!p.administrator()&&(!p.canAccess()||!"AGENCY_WIDE".equals(p.dataScope())))throw new AccessDeniedException("Performance plan assignment access is not permitted");}catch(HttpClientErrorException.Unauthorized|HttpClientErrorException.Forbidden e){throw new AccessDeniedException("Access denied",e);}catch(AccessDeniedException|ResponseStatusException e){throw e;}catch(RestClientException e){throw unavailable(e);}}
    private static ResponseStatusException unavailable(Throwable cause){return new ResponseStatusException(SERVICE_UNAVAILABLE,"Administrative authorization is unavailable",cause);}
}
