package com.humanresource.onboarding;

import com.humanresource.integration.primehr.AdministrativePermissionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
public class HrmPermissionGuard {
    public enum Action{ACCESS,ADD,EDIT,SUBMIT,APPROVE,FINALIZE,PUBLISH}
    private final RestTemplate rest;private final String base;
    public HrmPermissionGuard(@Value("${primehr.administrative.base-url}")String base,@Value("${primehr.administrative.connect-timeout-millis:3000}")int connect,@Value("${primehr.administrative.read-timeout-millis:5000}")int read){var f=new org.springframework.http.client.SimpleClientHttpRequestFactory();f.setConnectTimeout(connect);f.setReadTimeout(read);rest=new RestTemplate(f);this.base=base;}
    public void require(String token,String feature,Action action){if(token==null||!token.startsWith("Bearer "))throw new AccessDeniedException("Bearer authentication is required");try{HttpHeaders h=new HttpHeaders();h.set(HttpHeaders.AUTHORIZATION,token);var p=rest.exchange(base+"/api/authorization/effective?featureKey="+feature,HttpMethod.GET,new HttpEntity<>(h),AdministrativePermissionResponse.class).getBody();if(p==null||!feature.equals(p.featureKey())||(!p.administrator()&&!allowed(p,action)))throw new AccessDeniedException("The requested HRM action is not permitted");}catch(AccessDeniedException e){throw e;}catch(RestClientResponseException e){if(e.getStatusCode().value()==401||e.getStatusCode().value()==403)throw new AccessDeniedException("Administrative authorization denied the request");throw unavailable(e);}catch(RestClientException e){throw unavailable(e);}}
    private boolean allowed(AdministrativePermissionResponse p,Action a){if(!p.canAccess()||!"AGENCY_WIDE".equals(p.dataScope()))return false;return switch(a){case ACCESS->true;case ADD->p.canAdd();case EDIT->p.canEdit();case SUBMIT->p.canSubmit();case APPROVE->p.canApprove();case FINALIZE->p.canFinalize();case PUBLISH->p.canPublish();};}
    private ResponseStatusException unavailable(Throwable e){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Administrative authorization is unavailable",e);}
}
