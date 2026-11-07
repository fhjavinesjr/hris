package com.administrative.impl;

import com.administrative.dtos.PrimeHrReportSourceDtos.*;
import com.administrative.entitymodels.*;
import com.administrative.repositories.*;
import com.administrative.services.PrimeHrReportSourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@Transactional(readOnly=true)
public class PrimeHrReportSourceServiceImpl implements PrimeHrReportSourceService {
    private final SettingsRepository settings;private final PlantillaRepository plantilla;private final JobPositionRepository positions;
    public PrimeHrReportSourceServiceImpl(SettingsRepository settings,PlantillaRepository plantilla,JobPositionRepository positions){this.settings=settings;this.plantilla=plantilla;this.positions=positions;}
    @Override public Branding branding(){Settings value=settings.findFirstByOrderBySettingsIdDesc().orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Administrative agency settings are required for formal reports"));String name=required(value.getCompanyName(),"Company name");String address=join(value.getAddress(),value.getCity(),value.getZipCode());String fp=sha(name,address,hash(value.getLeftHeaderLogo()),hash(value.getMainLogo()),hash(value.getRightHeaderLogo()),String.valueOf(value.getSettingsId()));return new Branding(name,address,fp);}
    @Override public Position position(Long plantillaId){Plantilla item=plantilla.findById(plantillaId).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"The report Plantilla source is unavailable"));JobPosition job=positions.findById(item.getJobPositionId()).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"The report Job Position source is unavailable"));return new Position(item.getPlantillaId(),required(item.getPlantillaName(),"Plantilla name"),job.getJobPositionId(),required(job.getJobPositionName(),"Job Position name"),sha(String.valueOf(item.getPlantillaId()),item.getPlantillaName(),String.valueOf(job.getJobPositionId()),job.getJobPositionName()));}
    private static String join(String... values){return java.util.Arrays.stream(values).filter(v->v!=null&&!v.isBlank()).map(String::trim).collect(java.util.stream.Collectors.joining(", "));}private static String required(String value,String field){if(value==null||value.isBlank())throw new ResponseStatusException(HttpStatus.CONFLICT,field+" is required for formal reports");return value.trim();}private static String hash(byte[] bytes){return bytes==null?"":sha(bytes);}private static String sha(String... values){return sha(String.join("\u001f",values).getBytes(StandardCharsets.UTF_8));}private static String sha(byte[] bytes){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}catch(Exception e){throw new IllegalStateException(e);}}
}
