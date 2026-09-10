package com.primehr.performancemanagement.application;

import com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.*;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @Transactional
public class PerformanceSuccessIndicatorServiceImpl implements PerformanceSuccessIndicatorService {
    private static final BigDecimal HUNDRED=new BigDecimal("100");
    private static final BigDecimal QUANTUM=new BigDecimal("0.000001");
    private final PerformanceSuccessIndicatorRepository indicators;
    private final PerformanceSuccessIndicatorVersionRepository versions;
    private final PerformanceIndicatorDimensionRepository dimensions;
    private final PerformanceIndicatorDimensionLevelRepository levels;
    private final PerformancePolicyVersionRepository policies;
    private final PerformanceRatingScaleVersionRepository scales;
    private final PerformanceRatingBandRepository bands;
    private final PrimeHrAuditService audit;

    public PerformanceSuccessIndicatorServiceImpl(PerformanceSuccessIndicatorRepository i,
            PerformanceSuccessIndicatorVersionRepository v,PerformanceIndicatorDimensionRepository d,
            PerformanceIndicatorDimensionLevelRepository l,PerformancePolicyVersionRepository p,
            PerformanceRatingScaleVersionRepository s,PerformanceRatingBandRepository b,PrimeHrAuditService a){
        indicators=i;versions=v;dimensions=d;levels=l;policies=p;scales=s;bands=b;audit=a;
    }
    @Override @Transactional(readOnly=true) public List<IndicatorResponse> list(String a){return versions.findByAgencyIdOrderByCreatedAtDesc(a).stream().map(v->response(a,v)).toList();}
    @Override @Transactional(readOnly=true) public IndicatorResponse get(String a,String id){return response(a,version(a,id));}
    @Override public IndicatorResponse create(String a,IndicatorInput i,String c){
        if(i.recordVersion()!=null)throw new IllegalArgumentException("recordVersion must not be supplied when creating");
        bindings(a,i.policyVersionId(),i.ratingScaleVersionId(),false);
        String code=i.code().trim().toUpperCase(Locale.ROOT);
        if(indicators.existsByAgencyIdAndNormalizedCode(a,code))throw new IllegalArgumentException("Success indicator code already exists");
        PerformanceSuccessIndicator root=indicators.saveAndFlush(new PerformanceSuccessIndicator(a,i.code()));
        PerformanceSuccessIndicatorVersion v=versions.saveAndFlush(make(a,root.getId(),1,null,i));
        return audited(a,"CREATE_SUCCESS_INDICATOR_DRAFT",v,null,c,null);
    }
    @Override public IndicatorResponse update(String a,String id,IndicatorInput i,String c){
        PerformanceSuccessIndicatorVersion v=version(a,id);check(v.getVersion(),i.recordVersion());
        if(!indicator(a,v.getSuccessIndicatorId()).getCode().equalsIgnoreCase(i.code()))throw new IllegalArgumentException("Success indicator code is stable");
        bindings(a,i.policyVersionId(),i.ratingScaleVersionId(),false);IndicatorResponse before=response(a,v);
        v.update(i.policyVersionId(),i.ratingScaleVersionId(),i.title(),i.description(),i.outputDescription(),
            i.keyResultArea(),i.performanceIndicator(),i.successStatement(),i.measureType(),i.unitOfMeasure(),
            i.direction(),i.targetValue(),i.targetFrom(),i.targetTo(),i.requiresEvidence(),i.evidenceRequirements());
        return audited(a,"UPDATE_SUCCESS_INDICATOR_DRAFT",versions.saveAndFlush(v),before,c,null);
    }
    @Override public IndicatorResponse dimensions(String a,String id,DimensionsInput i,String c){
        PerformanceSuccessIndicatorVersion v=version(a,id);check(v.getVersion(),i.recordVersion());validateStructure(a,v,i.dimensions());
        IndicatorResponse before=response(a,v);for(PerformanceIndicatorDimension d:dimensionRows(a,id))levels.deleteByAgencyIdAndDimensionId(a,d.getId());
        dimensions.deleteByAgencyIdAndIndicatorVersionId(a,id);dimensions.flush();
        for(DimensionInput in:i.dimensions().stream().sorted(Comparator.comparingInt(DimensionInput::displayOrder)).toList()){
            PerformanceIndicatorDimension d=dimensions.saveAndFlush(new PerformanceIndicatorDimension(a,id,in.type(),in.code(),in.label(),in.weightPercent(),in.displayOrder()));
            levels.saveAll(in.levels().stream().map(x->new PerformanceIndicatorDimensionLevel(a,d.getId(),x.ratingBandId(),x.operator(),x.thresholdFrom(),x.thresholdTo(),x.manualCriteria())).toList());
        }
        v.structureChanged();return audited(a,"REPLACE_SUCCESS_INDICATOR_DIMENSIONS",versions.saveAndFlush(v),before,c,null);
    }
    @Override public IndicatorResponse revision(String a,String id,Transition i,String c){
        PerformanceSuccessIndicatorVersion old=version(a,id);check(old.getVersion(),i.recordVersion());
        if(old.getStatus()!=PerformanceSuccessIndicatorVersion.Status.PUBLISHED)throw new IllegalArgumentException("Only a published success indicator can have a revision");
        int n=versions.findByAgencyIdAndSuccessIndicatorIdOrderByDefinitionVersionDesc(a,old.getSuccessIndicatorId()).stream().mapToInt(PerformanceSuccessIndicatorVersion::getDefinitionVersion).max().orElse(0)+1;
        PerformanceSuccessIndicatorVersion v=versions.saveAndFlush(new PerformanceSuccessIndicatorVersion(a,old.getSuccessIndicatorId(),old.getPolicyVersionId(),old.getRatingScaleVersionId(),n,old.getId(),old.getTitle(),old.getDescription(),old.getOutputDescription(),old.getKeyResultArea(),old.getPerformanceIndicator(),old.getSuccessStatement(),old.getMeasureType(),old.getUnitOfMeasure(),old.getDirection(),old.getTargetValue(),old.getTargetFrom(),old.getTargetTo(),old.isRequiresEvidence(),old.getEvidenceRequirements()));
        copyStructure(a,old.getId(),v.getId());v.structureChanged();return audited(a,"CREATE_SUCCESS_INDICATOR_REVISION",versions.saveAndFlush(v),null,c,i.reason());
    }
    @Override public IndicatorResponse publish(String a,String id,Transition i,String c){
        PerformanceSuccessIndicatorVersion v=version(a,id);check(v.getVersion(),i.recordVersion());
        Binding binding=bindings(a,v.getPolicyVersionId(),v.getRatingScaleVersionId(),true);
        validateEffectiveInterval(i.effectiveFrom(),i.effectiveTo(),binding.policy(),binding.scale());validateExisting(a,v);
        IndicatorResponse before=response(a,v);Instant now=Instant.now();String actor=audit.currentActor();
        if(v.getSupersedesId()!=null){PerformanceSuccessIndicatorVersion prior=version(a,v.getSupersedesId());prior.supersede(i.effectiveFrom(),actor,now);versions.saveAndFlush(prior);}
        v.publish(i.effectiveFrom(),i.effectiveTo(),actor,now);
        return audited(a,"PUBLISH_SUCCESS_INDICATOR",versions.saveAndFlush(v),before,c,i.reason());
    }
    @Override public IndicatorResponse retire(String a,String id,Transition i,String c){PerformanceSuccessIndicatorVersion v=version(a,id);check(v.getVersion(),i.recordVersion());IndicatorResponse before=response(a,v);v.retire(audit.currentActor(),i.reason(),Instant.now());return audited(a,"RETIRE_SUCCESS_INDICATOR",versions.saveAndFlush(v),before,c,i.reason());}
    @Override @Transactional(readOnly=true) public PreviewResponse preview(String a,String id,PreviewInput i){
        PerformanceSuccessIndicatorVersion v=version(a,id);PerformanceRatingScaleVersion scale=scale(a,v.getRatingScaleVersionId());
        Map<String,DimensionActual> actuals=new HashMap<>();for(DimensionActual x:i.actuals())if(actuals.put(normalize(x.dimensionCode()),x)!=null)throw new IllegalArgumentException("Dimension actuals must be unique");
        List<DimensionResult> results=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;
        for(PerformanceIndicatorDimension d:dimensionRows(a,id)){
            DimensionActual actual=Optional.ofNullable(actuals.get(d.getCode())).orElseThrow(()->new IllegalArgumentException("Missing dimension actual: "+d.getCode()));
            PerformanceIndicatorDimensionLevel matched=match(a,d,actual);PerformanceRatingBand band=band(a,matched.getRatingBandId());
            BigDecimal contribution=band.getNumericScore().multiply(d.getWeightPercent()).divide(HUNDRED,8,RoundingMode.HALF_UP);
            total=total.add(contribution);results.add(new DimensionResult(d.getType().name(),d.getCode(),band.getNumericScore(),d.getWeightPercent(),contribution,band.getId()));
        }
        return new PreviewResponse(total.setScale(scale.getRoundingScale(),scale.getRoundingMode()),results,"sum(dimensionScore * dimensionWeight / 100)");
    }
    private PerformanceIndicatorDimensionLevel match(String a,PerformanceIndicatorDimension d,DimensionActual x){
        List<PerformanceIndicatorDimensionLevel> matches=levelRows(a,d.getId()).stream().filter(l->{
            if(l.getOperator()==PerformanceIndicatorDimensionLevel.Operator.MANUAL_CRITERIA)return x.manualBandId()!=null&&x.manualBandId().equals(l.getRatingBandId());
            if(x.actualValue()==null)return false;int c=x.actualValue().compareTo(l.getThresholdFrom());
            return switch(l.getOperator()){case LT->c<0;case LTE->c<=0;case EQ->c==0;case GTE->c>=0;case GT->c>0;case BETWEEN->c>=0&&x.actualValue().compareTo(l.getThresholdTo())<=0;default->false;};
        }).toList();
        if(matches.size()!=1)throw new IllegalArgumentException("Dimension actual must match exactly one rubric level: "+d.getCode());return matches.get(0);
    }
    private void validateStructure(String a,PerformanceSuccessIndicatorVersion v,List<DimensionInput> rows){
        if(rows==null||rows.isEmpty())throw new IllegalArgumentException("At least one dimension is required");
        if(rows.stream().map(DimensionInput::weightPercent).reduce(BigDecimal.ZERO,BigDecimal::add).compareTo(HUNDRED)!=0)throw new IllegalArgumentException("Dimension weights must total 100");
        Set<String> codes=new HashSet<>();Set<Integer> orders=new HashSet<>();
        Set<String> bandIds=bands.findByAgencyIdAndRatingScaleVersionIdOrderByDisplayOrder(a,v.getRatingScaleVersionId()).stream().map(PerformanceRatingBand::getId).collect(Collectors.toSet());
        for(DimensionInput d:rows){
            String code=normalize(d.code());if(!codes.add(code)||!orders.add(d.displayOrder()))throw new IllegalArgumentException("Dimension codes and display orders must be unique");
            if(d.type()!=PerformanceIndicatorDimension.Type.AGENCY_DEFINED&&!code.equals(d.type().name()))throw new IllegalArgumentException("Controlled dimensions must use their canonical code");
            Set<String> given=d.levels().stream().map(LevelInput::ratingBandId).collect(Collectors.toSet());
            if(given.size()!=d.levels().size()||!given.equals(bandIds))throw new IllegalArgumentException("Every dimension must define exactly one level for every rating band");
            validateRubric(d);
        }
    }
    private void validateRubric(DimensionInput d){
        boolean manual=d.levels().stream().allMatch(x->x.operator()==PerformanceIndicatorDimensionLevel.Operator.MANUAL_CRITERIA);
        boolean automatic=d.levels().stream().noneMatch(x->x.operator()==PerformanceIndicatorDimensionLevel.Operator.MANUAL_CRITERIA);
        if(!manual&&!automatic)throw new IllegalArgumentException("A dimension cannot mix manual and threshold rubric levels");
        if(manual)return;
        record Range(BigDecimal low,BigDecimal high){}
        List<Range> ranges=d.levels().stream().map(x->{
            if(x.thresholdFrom()!=null&&x.thresholdFrom().scale()>6||x.thresholdTo()!=null&&x.thresholdTo().scale()>6)throw new IllegalArgumentException("Threshold precision cannot exceed 6 decimals");
            return switch(x.operator()){
                case LT->new Range(null,x.thresholdFrom().subtract(QUANTUM)); case LTE->new Range(null,x.thresholdFrom());
                case EQ->new Range(x.thresholdFrom(),x.thresholdFrom()); case GTE->new Range(x.thresholdFrom(),null);
                case GT->new Range(x.thresholdFrom().add(QUANTUM),null); case BETWEEN->new Range(x.thresholdFrom(),x.thresholdTo());
                default->throw new IllegalArgumentException("Invalid threshold operator");};
        }).sorted(Comparator.comparing(Range::low,Comparator.nullsFirst(Comparator.naturalOrder()))).toList();
        if(ranges.get(0).low()!=null||ranges.get(ranges.size()-1).high()!=null)throw new IllegalArgumentException("Threshold rubric must be exhaustive");
        for(int n=1;n<ranges.size();n++){Range prior=ranges.get(n-1),next=ranges.get(n);if(prior.high()==null||next.low()==null||next.low().compareTo(prior.high().add(QUANTUM))!=0)throw new IllegalArgumentException("Threshold rubric must be exhaustive and non-overlapping");}
    }
    private void validateExisting(String a,PerformanceSuccessIndicatorVersion v){List<DimensionInput> rows=dimensionRows(a,v.getId()).stream().map(d->new DimensionInput(d.getType(),d.getCode(),d.getLabel(),d.getWeightPercent(),d.getDisplayOrder(),levelRows(a,d.getId()).stream().map(l->new LevelInput(l.getRatingBandId(),l.getOperator(),l.getThresholdFrom(),l.getThresholdTo(),l.getManualCriteria())).toList())).toList();validateStructure(a,v,rows);}
    private void copyStructure(String a,String from,String to){for(PerformanceIndicatorDimension d:dimensionRows(a,from)){PerformanceIndicatorDimension copy=dimensions.saveAndFlush(new PerformanceIndicatorDimension(a,to,d.getType(),d.getCode(),d.getLabel(),d.getWeightPercent(),d.getDisplayOrder()));levels.saveAll(levelRows(a,d.getId()).stream().map(l->new PerformanceIndicatorDimensionLevel(a,copy.getId(),l.getRatingBandId(),l.getOperator(),l.getThresholdFrom(),l.getThresholdTo(),l.getManualCriteria())).toList());}}
    private IndicatorResponse audited(String a,String action,PerformanceSuccessIndicatorVersion v,IndicatorResponse before,String c,String reason){IndicatorResponse after=response(a,v);audit.record(a,action,"SPMS_SUCCESS_INDICATOR_VERSION",v.getId(),v.getDefinitionVersion(),v.getVersion(),before,after,reason,c);return after;}
    private IndicatorResponse response(String a,PerformanceSuccessIndicatorVersion v){List<DimensionResponse> ds=dimensionRows(a,v.getId()).stream().map(d->new DimensionResponse(d.getId(),d.getType().name(),d.getCode(),d.getLabel(),d.getWeightPercent(),d.getDisplayOrder(),levelRows(a,d.getId()).stream().map(l->new LevelResponse(l.getId(),l.getRatingBandId(),l.getOperator().name(),l.getThresholdFrom(),l.getThresholdTo(),l.getManualCriteria())).toList())).toList();return new IndicatorResponse(v.getId(),v.getSuccessIndicatorId(),indicator(a,v.getSuccessIndicatorId()).getCode(),v.getTitle(),v.getDescription(),v.getOutputDescription(),v.getKeyResultArea(),v.getPerformanceIndicator(),v.getSuccessStatement(),v.getPolicyVersionId(),v.getRatingScaleVersionId(),v.getMeasureType().name(),v.getUnitOfMeasure(),v.getDirection().name(),v.getTargetValue(),v.getTargetFrom(),v.getTargetTo(),v.isRequiresEvidence(),v.getEvidenceRequirements(),v.getDefinitionVersion(),v.getSupersedesId(),v.getStatus().name(),v.getEffectiveFrom(),v.getEffectiveTo(),v.getVersion(),ds);}
    private PerformanceSuccessIndicatorVersion make(String a,String root,int n,String prior,IndicatorInput i){return new PerformanceSuccessIndicatorVersion(a,root,i.policyVersionId(),i.ratingScaleVersionId(),n,prior,i.title(),i.description(),i.outputDescription(),i.keyResultArea(),i.performanceIndicator(),i.successStatement(),i.measureType(),i.unitOfMeasure(),i.direction(),i.targetValue(),i.targetFrom(),i.targetTo(),i.requiresEvidence(),i.evidenceRequirements());}
    private Binding bindings(String a,String policyId,String scaleId,boolean published){PerformancePolicyVersion p=policies.findByIdAndAgencyId(policyId,a).orElseThrow(()->new ResourceNotFoundException("Performance policy version not found"));PerformanceRatingScaleVersion s=scale(a,scaleId);if(!s.getPolicyVersionId().equals(p.getId()))throw new IllegalArgumentException("Success indicator policy and rating scale must reference the same exact policy version");if(published&&(p.getStatus()!=PerformancePolicyVersion.Status.PUBLISHED||s.getStatus()!=PerformanceRatingScaleVersion.Status.PUBLISHED))throw new IllegalArgumentException("Published policy and rating scale versions are required");return new Binding(p,s);}
    private void validateEffectiveInterval(LocalDate from,LocalDate to,PerformancePolicyVersion policy,PerformanceRatingScaleVersion scale){if(from==null)throw new IllegalArgumentException("effectiveFrom is required");if(to!=null&&to.isBefore(from))throw new IllegalArgumentException("effectiveTo cannot precede effectiveFrom");if(!contains(policy.getEffectiveFrom(),policy.getEffectiveTo(),from,to)||!contains(scale.getEffectiveFrom(),scale.getEffectiveTo(),from,to))throw new IllegalArgumentException("Indicator effectivity must be contained by its exact policy and rating-scale versions");}
    private static boolean contains(LocalDate outerFrom,LocalDate outerTo,LocalDate innerFrom,LocalDate innerTo){return outerFrom!=null&&!innerFrom.isBefore(outerFrom)&&(outerTo==null||(innerTo!=null&&!innerTo.isAfter(outerTo)));}
    private record Binding(PerformancePolicyVersion policy,PerformanceRatingScaleVersion scale){}
    private List<PerformanceIndicatorDimension> dimensionRows(String a,String v){return dimensions.findByAgencyIdAndIndicatorVersionIdOrderByDisplayOrder(a,v);} private List<PerformanceIndicatorDimensionLevel> levelRows(String a,String d){return levels.findByAgencyIdAndDimensionId(a,d);} private PerformanceRatingBand band(String a,String id){return bands.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Rating band not found"));} private PerformanceRatingScaleVersion scale(String a,String id){return scales.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Rating scale version not found"));} private PerformanceSuccessIndicatorVersion version(String a,String id){return versions.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Success indicator version not found"));} private PerformanceSuccessIndicator indicator(String a,String id){return indicators.findByIdAndAgencyId(id,a).orElseThrow(()->new ResourceNotFoundException("Success indicator not found"));}
    private static String normalize(String value){return value==null?null:value.trim().toUpperCase(Locale.ROOT);} private static void check(long actual,Long expected){if(expected==null||actual!=expected)throw new OptimisticConflictException("Expected recordVersion "+expected+" but current version is "+actual);}
}
