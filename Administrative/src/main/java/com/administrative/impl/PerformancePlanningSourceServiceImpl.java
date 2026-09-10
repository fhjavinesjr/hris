package com.administrative.impl;

import com.administrative.dtos.PerformancePlanningSourceDtos.*;
import com.administrative.entitymodels.*;
import com.administrative.repositories.*;
import com.administrative.services.PerformancePlanningSourceService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class PerformancePlanningSourceServiceImpl implements PerformancePlanningSourceService {
    private final AreasRepository areas; private final BusinessUnitsRepository units;
    private final ManagePersonnelRepository personnel; private final EmployeeRequestRepository requests;
    private final ApprovalWorkflowRepository workflows;
    public PerformancePlanningSourceServiceImpl(AreasRepository a,BusinessUnitsRepository u,
            ManagePersonnelRepository p,EmployeeRequestRepository r,ApprovalWorkflowRepository w){
        areas=a;units=u;personnel=p;requests=r;workflows=w;
    }
    @Override public OrganizationPage organizations(OrganizationType type,String search,int page,int size){
        validatePage(page,size);String term=search==null?"":search.trim().toLowerCase(Locale.ROOT);
        Instant now=Instant.now();
        if(type==OrganizationType.AREA){List<Areas> values=areas.findAll(Sort.by("areasName").ascending().and(Sort.by("areasId"))).stream()
                .filter(v->term.isEmpty()||value(v.getAreasName()).toLowerCase(Locale.ROOT).contains(term)).toList();
            return page(values,page,size,v->area(v,now));}
        List<BusinessUnits> values=units.findAll(Sort.by("businessUnitsName").ascending().and(Sort.by("businessUnitsId"))).stream()
                .filter(v->term.isEmpty()||value(v.getBusinessUnitsCode()).toLowerCase(Locale.ROOT).contains(term)||value(v.getBusinessUnitsName()).toLowerCase(Locale.ROOT).contains(term)).toList();
        return page(values,page,size,v->unit(v,now));
    }
    @Override public OrganizationTarget organization(OrganizationType type,Long id){positive(id,"id");Instant now=Instant.now();
        return type==OrganizationType.AREA?area(areas.findById(id).orElseThrow(()->missing("Area")),now):unit(units.findById(id).orElseThrow(()->missing("Business Unit")),now);}
    @Override public PersonnelMembership membership(Long employeeId){positive(employeeId,"employeeId");List<ManagePersonnel> matches=personnel.findByEmployeeIdOrderById(employeeId);
        if(matches.isEmpty())throw missing("Personnel membership");if(matches.size()!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee has duplicate Administrative personnel memberships");
        ManagePersonnel row=matches.get(0);BusinessUnits unit=units.findById(row.getBusinessUnitId()).orElseThrow(()->missing("Business Unit"));Areas area=areas.findById(row.getAreaId()).orElseThrow(()->missing("Area"));
        if(unit.getAreas()==null||!Objects.equals(unit.getAreas().getAreasId(),area.getAreasId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Personnel Business Unit does not belong to its Area");
        Instant now=Instant.now();String raw=join(employeeId,unit.getBusinessUnitsId(),unit.getBusinessUnitsCode(),unit.getBusinessUnitsName(),area.getAreasId(),area.getAreasName(),row.isHead(),row.isCoApprover(),row.getBase());
        return new PersonnelMembership(employeeId,unit.getBusinessUnitsId(),unit.getBusinessUnitsCode(),unit.getBusinessUnitsName(),area.getAreasId(),area.getAreasName(),row.isHead(),row.isCoApprover(),row.getBase(),fingerprint(raw),now);
    }
    @Override public ApprovalRoute approvalRoute(Long businessUnitId,String requestCode){positive(businessUnitId,"businessUnitId");String code=required(requestCode,"requestCode").toUpperCase(Locale.ROOT);
        if(!Set.of("PERFORMANCE_OFFICE_COMMITMENT","PERFORMANCE_INDIVIDUAL_COMMITMENT").contains(code))throw new IllegalArgumentException("Unsupported performance approval request code");
        BusinessUnits unit=units.findById(businessUnitId).orElseThrow(()->missing("Business Unit"));EmployeeRequest request=requests.findByCodeIgnoreCase(code).orElseThrow(()->missing("Employee Request"));
        List<ApprovalWorkflow> rows=workflows.findByBusinessUnitIdAndEmployeeRequestIdOrderByApprovalLevelAscApprovalWorkflowIdAsc(businessUnitId,request.getEmployeeRequestId());
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Performance approval route is not configured");
        Set<Integer> levels=new HashSet<>();Set<Long> actors=new HashSet<>();int expected=1;for(ApprovalWorkflow row:rows){if(!Objects.equals(row.getAreaId(),unit.getAreas().getAreasId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Approval route Area does not match its Business Unit");if(row.getApprovalLevel()==null||row.getApprovalLevel()!=expected++||!levels.add(row.getApprovalLevel()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Approval route levels must be unique and contiguous from one");if(row.getEmployeeId()==null||row.getEmployeeId()<1||!actors.add(row.getEmployeeId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Approval route actors must be positive and unique");}
        if(request.getMax()==null||request.getMax()<rows.size())throw new ResponseStatusException(HttpStatus.CONFLICT,"Approval route exceeds the configured maximum");
        List<ApprovalRouteStep> steps=rows.stream().map(v->new ApprovalRouteStep(v.getApprovalWorkflowId(),v.getApprovalLevel(),v.getEmployeeId())).toList();Instant now=Instant.now();String raw=join(request.getEmployeeRequestId(),code,request.getMax(),businessUnitId,unit.getAreas().getAreasId(),steps);
        return new ApprovalRoute(request.getEmployeeRequestId(),code,request.getMax(),businessUnitId,unit.getAreas().getAreasId(),steps,fingerprint(raw),now);
    }
    private OrganizationTarget area(Areas v,Instant now){String raw=join("AREA",v.getAreasId(),v.getAreasName());return new OrganizationTarget(OrganizationType.AREA,v.getAreasId(),null,v.getAreasName(),v.getAreasId(),v.getAreasName(),fingerprint(raw),now);}
    private OrganizationTarget unit(BusinessUnits v,Instant now){if(v.getAreas()==null)throw new ResponseStatusException(HttpStatus.CONFLICT,"Business Unit has no Area");String raw=join("BUSINESS_UNIT",v.getBusinessUnitsId(),v.getBusinessUnitsCode(),v.getBusinessUnitsName(),v.getAreas().getAreasId(),v.getAreas().getAreasName());return new OrganizationTarget(OrganizationType.BUSINESS_UNIT,v.getBusinessUnitsId(),v.getBusinessUnitsCode(),v.getBusinessUnitsName(),v.getAreas().getAreasId(),v.getAreas().getAreasName(),fingerprint(raw),now);}
    private static <T> OrganizationPage page(List<T> all,int page,int size,java.util.function.Function<T,OrganizationTarget> mapper){int from=Math.min(page*size,all.size()),to=Math.min(from+size,all.size());List<OrganizationTarget> content=all.subList(from,to).stream().map(mapper).toList();int totalPages=(all.size()+size-1)/size;return new OrganizationPage(content,page,size,all.size(),totalPages,page==0,page>=Math.max(0,totalPages-1));}
    private static String fingerprint(String raw){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private static String join(Object...v){return Arrays.stream(v).map(PerformancePlanningSourceServiceImpl::value).collect(java.util.stream.Collectors.joining("|"));}
    private static String value(Object v){return v==null?"":v.toString().trim();}private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
    private static void positive(Long v,String n){if(v==null||v<1)throw new IllegalArgumentException(n+" must be positive");}private static void validatePage(int p,int s){if(p<0)throw new IllegalArgumentException("page cannot be negative");if(s<1||s>100)throw new IllegalArgumentException("size must be between 1 and 100");}
    private static ResponseStatusException missing(String what){return new ResponseStatusException(HttpStatus.NOT_FOUND,what+" was not found");}
}
