package com.humanresource.onboarding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDateTime;

@Component
class HttpAdministrativeAppointmentSourceClient implements AdministrativeAppointmentSourceClient {
    private final RestTemplate rest;private final String base;
    HttpAdministrativeAppointmentSourceClient(@Value("${primehr.administrative.base-url}")String base,@Value("${primehr.administrative.connect-timeout-millis:3000}")int connect,@Value("${primehr.administrative.read-timeout-millis:5000}")int read){var f=new org.springframework.http.client.SimpleClientHttpRequestFactory();f.setConnectTimeout(connect);f.setReadTimeout(read);rest=new RestTemplate(f);this.base=base;}
    public Source resolve(String token,long plantilla,long unit,long nature,LocalDateTime assumption){String url=UriComponentsBuilder.fromHttpUrl(base+"/api/integration/v1/hrm/appointment-sources/"+plantilla).queryParam("businessUnitId",unit).queryParam("natureOfAppointmentId",nature).queryParam("assumptionDate",assumption).toUriString();try{HttpHeaders h=new HttpHeaders();h.set(HttpHeaders.AUTHORIZATION,token);Source value=rest.exchange(url,HttpMethod.GET,new HttpEntity<>(h),Source.class).getBody();if(value==null)throw new IllegalStateException("Administrative returned no appointment source");return value;}catch(RestClientException e){throw new org.springframework.web.server.ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Authoritative Administrative appointment source is unavailable",e);}}
}
