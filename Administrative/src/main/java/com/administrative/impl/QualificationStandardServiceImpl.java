package com.administrative.impl;

import com.administrative.dtos.QualificationStandardDtos.*;
import com.administrative.entitymodels.*;
import com.administrative.repositories.*;
import com.administrative.services.QualificationStandardService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

@Service @Transactional
public class QualificationStandardServiceImpl implements QualificationStandardService {
    private final QualificationStandardRepository standards; private final JobPositionRepository positions;
    public QualificationStandardServiceImpl(QualificationStandardRepository s,JobPositionRepository p){standards=s;positions=p;}
    @Transactional(readOnly=true) public List<Response> list(Long job){requirePosition(job);return standards.findByJobPositionIdOrderByDefinitionVersionDesc(job).stream().map(this::response).toList();}
    @Transactional(readOnly=true) public Response get(Long id){return response(require(id));}
    public Response create(Save r,String actor){requirePosition(r.jobPositionId());if(r.recordVersion()!=null)throw new IllegalArgumentException("recordVersion must not be supplied");
        if(!standards.findByJobPositionIdOrderByDefinitionVersionDesc(r.jobPositionId()).isEmpty())throw new IllegalArgumentException("Create a successor version for an existing Job Position standard");
        return response(standards.saveAndFlush(QualificationStandard.draft(r.jobPositionId(),1,null,r.education(),r.training(),r.experience(),r.eligibility(),r.licenseRequirement(),r.sourceBasis(),r.effectiveFrom(),r.effectiveTo(),actor)));}
    public Response update(Long id,Save r,String actor){QualificationStandard q=require(id);version(q,r.recordVersion());if(!q.getJobPositionId().equals(r.jobPositionId()))throw new IllegalArgumentException("jobPositionId cannot change");
        q.update(r.education(),r.training(),r.experience(),r.eligibility(),r.licenseRequirement(),r.sourceBasis(),r.effectiveFrom(),r.effectiveTo(),actor);return response(standards.saveAndFlush(q));}
    public Response archive(Long id,Transition r,String actor){QualificationStandard q=require(id);version(q,r.recordVersion());q.archive(actor);return response(standards.saveAndFlush(q));}
    public Response successor(Long id,Transition r,String actor){QualificationStandard source=require(id);version(source,r.recordVersion());
        if(source.getStatus()!=QualificationStandardStatus.ACTIVE)throw new IllegalStateException("Only ACTIVE standards can have a successor");
        if(standards.existsByJobPositionIdAndStatus(source.getJobPositionId(),QualificationStandardStatus.DRAFT))throw new IllegalArgumentException("An unfinished successor already exists");
        LocalDate from=r.effectiveFrom();if(from==null)throw new IllegalArgumentException("effectiveFrom is required");
        return response(standards.saveAndFlush(QualificationStandard.draft(source.getJobPositionId(),source.getDefinitionVersion()+1,source.getId(),source.getEducation(),source.getTraining(),source.getExperience(),source.getEligibility(),source.getLicenseRequirement(),source.getSourceBasis(),from,null,actor)));}
    public Response publish(Long id,Transition r,String actor){QualificationStandard q=require(id);version(q,r.recordVersion());if(q.getEffectiveFrom()==null)throw new IllegalArgumentException("effectiveFrom is required before publication");
        List<QualificationStandard> active=standards.findByJobPositionIdAndStatusOrderByDefinitionVersionDesc(q.getJobPositionId(),QualificationStandardStatus.ACTIVE);
        for(QualificationStandard old:active){
            boolean overlapsSuccessor = old.getEffectiveTo()==null || !old.getEffectiveTo().isBefore(q.getEffectiveFrom());
            if(!overlapsSuccessor)continue;
            if(old.getEffectiveFrom()==null||!q.getEffectiveFrom().isAfter(old.getEffectiveFrom()))throw new IllegalArgumentException("A successor must start after the active standard");
            old.close(q.getEffectiveFrom().minusDays(1),actor);standards.save(old);
        }
        q.publish(actor,Instant.now());return response(standards.saveAndFlush(q));}
    @Transactional(readOnly=true) public Response effective(Long job,LocalDate date){requirePosition(job);LocalDate asOf=date==null?LocalDate.now():date;
        return standards.findByJobPositionIdAndStatusOrderByDefinitionVersionDesc(job,QualificationStandardStatus.ACTIVE).stream().filter(q->(q.getEffectiveFrom()==null||!q.getEffectiveFrom().isAfter(asOf))&&(q.getEffectiveTo()==null||!q.getEffectiveTo().isBefore(asOf))).findFirst().map(this::response).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No effective Qualification Standard was found"));}
    private QualificationStandard require(Long id){return standards.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Qualification Standard was not found"));}
    private JobPosition requirePosition(Long id){if(id==null||id<1)throw new IllegalArgumentException("jobPositionId must be positive");return positions.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Job Position was not found"));}
    private static void version(QualificationStandard q,Long expected){if(expected==null||expected!=q.getRecordVersion())throw new ResponseStatusException(HttpStatus.CONFLICT,"Qualification Standard changed; reload and retry");}
    private Response response(QualificationStandard q){JobPosition p=requirePosition(q.getJobPositionId());Instant fetched=Instant.now();String raw=String.join("|",q.getId().toString(),String.valueOf(q.getDefinitionVersion()),q.getStatus().name(),q.getEducation(),q.getTraining(),q.getExperience(),q.getEligibility(),String.valueOf(q.getLicenseRequirement()),String.valueOf(q.getEffectiveFrom()),String.valueOf(q.getEffectiveTo()));
        return new Response(q.getId(),q.getJobPositionId(),p.getJobPositionName(),q.getDefinitionVersion(),q.getSupersedesId(),q.getStatus(),q.getEducation(),q.getTraining(),q.getExperience(),q.getEligibility(),q.getLicenseRequirement(),q.getSourceBasis(),q.getEffectiveFrom(),q.getEffectiveTo(),q.getPublishedBy(),q.getPublishedAt(),q.getRecordVersion(),sha(raw),fetched);}
    private static String sha(String s){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
