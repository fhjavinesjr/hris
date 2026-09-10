package com.primehr.performancemanagement.application;

import com.primehr.performancemanagement.api.PerformanceRatingScaleDtos.*;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class PerformanceRatingScaleServiceImpl implements PerformanceRatingScaleService {
    private final PerformanceRatingScaleRepository scales;
    private final PerformanceRatingScaleVersionRepository versions;
    private final PerformanceRatingBandRepository bands;
    private final PerformancePolicyVersionRepository policies;
    private final PrimeHrAuditService audit;

    public PerformanceRatingScaleServiceImpl(PerformanceRatingScaleRepository scales,
            PerformanceRatingScaleVersionRepository versions, PerformanceRatingBandRepository bands,
            PerformancePolicyVersionRepository policies, PrimeHrAuditService audit) {
        this.scales=scales; this.versions=versions; this.bands=bands; this.policies=policies; this.audit=audit;
    }

    @Override @Transactional(readOnly=true)
    public List<ScaleResponse> list(String agencyId) { return versions.findByAgencyIdOrderByCreatedAtDesc(agencyId).stream().map(v->response(agencyId,v)).toList(); }
    @Override @Transactional(readOnly=true)
    public ScaleResponse get(String agencyId,String id) { return response(agencyId,version(agencyId,id)); }

    @Override
    public ScaleResponse create(String agencyId,ScaleInput input,String correlationId) {
        if(input.recordVersion()!=null) throw new IllegalArgumentException("recordVersion must not be supplied when creating");
        requirePolicy(agencyId,input.policyVersionId());
        String normalized=input.code().trim().toUpperCase(Locale.ROOT);
        if(scales.existsByAgencyIdAndNormalizedCode(agencyId,normalized)) throw new IllegalArgumentException("Rating scale code already exists");
        PerformanceRatingScale scale=scales.saveAndFlush(new PerformanceRatingScale(agencyId,input.code()));
        PerformanceRatingScaleVersion value=versions.saveAndFlush(newVersion(agencyId,scale.getId(),1,null,input));
        ScaleResponse after=response(agencyId,value);
        audit.record(agencyId,"CREATE_RATING_SCALE_DRAFT","SPMS_RATING_SCALE_VERSION",value.getId(),1,value.getVersion(),null,after,null,correlationId);
        return after;
    }

    @Override
    public ScaleResponse update(String agencyId,String id,ScaleInput input,String correlationId) {
        PerformanceRatingScaleVersion value=version(agencyId,id); requireVersion(value.getVersion(),input.recordVersion());
        if(!scale(agencyId,value.getRatingScaleId()).getCode().equalsIgnoreCase(input.code())) throw new IllegalArgumentException("Rating scale code is stable; create a separate scale for a new code");
        requirePolicy(agencyId,input.policyVersionId()); ScaleResponse before=response(agencyId,value);
        value.update(input.policyVersionId(),input.title(),input.description(),input.legalBasis(),input.minimumScore(),input.maximumScore(),input.roundingScale(),input.roundingMode(),input.measureType(),input.direction(),input.dimensionScoreSource());
        value=versions.saveAndFlush(value); ScaleResponse after=response(agencyId,value);
        audit.record(agencyId,"UPDATE_RATING_SCALE_DRAFT","SPMS_RATING_SCALE_VERSION",id,value.getDefinitionVersion(),value.getVersion(),before,after,null,correlationId);
        return after;
    }

    @Override
    public ScaleResponse bands(String agencyId,String id,BandsInput input,String correlationId) {
        PerformanceRatingScaleVersion value=version(agencyId,id); requireVersion(value.getVersion(),input.recordVersion()); validateBands(value,input.bands());
        ScaleResponse before=response(agencyId,value); bands.deleteByAgencyIdAndRatingScaleVersionId(agencyId,id); bands.flush();
        bands.saveAll(input.bands().stream().map(b->new PerformanceRatingBand(agencyId,id,b.code(),b.numericScore(),b.label(),b.lowerBound(),b.upperBound(),b.displayOrder(),b.guidance())).toList());
        value.bandsReplaced(); value=versions.saveAndFlush(value); ScaleResponse after=response(agencyId,value);
        audit.record(agencyId,"REPLACE_RATING_BANDS","SPMS_RATING_SCALE_VERSION",id,value.getDefinitionVersion(),value.getVersion(),before,after,null,correlationId);
        return after;
    }

    @Override
    public ScaleResponse revision(String agencyId,String id,Transition input,String correlationId) {
        PerformanceRatingScaleVersion prior=version(agencyId,id); requireVersion(prior.getVersion(),input.recordVersion());
        if(prior.getStatus()!=PerformanceRatingScaleVersion.Status.PUBLISHED) throw new IllegalArgumentException("Only a published rating scale can have a revision");
        int number=versions.findByAgencyIdAndRatingScaleIdOrderByDefinitionVersionDesc(agencyId,prior.getRatingScaleId()).stream().mapToInt(PerformanceRatingScaleVersion::getDefinitionVersion).max().orElse(0)+1;
        PerformanceRatingScaleVersion value=versions.saveAndFlush(new PerformanceRatingScaleVersion(agencyId,prior.getRatingScaleId(),prior.getPolicyVersionId(),number,prior.getId(),prior.getTitle(),prior.getDescription(),prior.getLegalBasis(),prior.getMinimumScore(),prior.getMaximumScore(),prior.getRoundingScale(),prior.getRoundingMode(),prior.getMeasureType(),prior.getDirection(),prior.getDimensionScoreSource()));
        String revisionId=value.getId();
        bands.saveAll(bandEntities(agencyId,prior.getId()).stream().map(b->new PerformanceRatingBand(agencyId,revisionId,b.getCode(),b.getNumericScore(),b.getLabel(),b.getLowerBound(),b.getUpperBound(),b.getDisplayOrder(),b.getGuidance())).toList());
        value.bandsReplaced(); value=versions.saveAndFlush(value); ScaleResponse after=response(agencyId,value);
        audit.record(agencyId,"CREATE_RATING_SCALE_REVISION","SPMS_RATING_SCALE_VERSION",value.getId(),number,value.getVersion(),null,after,input.reason(),correlationId);
        return after;
    }

    @Override
    public ScaleResponse publish(String agencyId,String id,PublishTransition input,String correlationId) {
        PerformanceRatingScaleVersion value=version(agencyId,id); requireVersion(value.getVersion(),input.recordVersion());
        PerformancePolicyVersion policy=requirePolicy(agencyId,value.getPolicyVersionId());
        if(policy.getStatus()!=PerformancePolicyVersion.Status.PUBLISHED) throw new IllegalArgumentException("Rating scales may only bind a published performance policy version");
        validateBands(value,bandEntities(agencyId,id).stream().map(this::input).toList());
        List<PerformanceRatingScaleVersion> overlaps=versions.overlapping(agencyId,value.getRatingScaleId(),input.effectiveFrom(),input.effectiveTo()==null?LocalDate.of(9999,12,31):input.effectiveTo());
        PerformanceRatingScaleVersion prior=value.getSupersedesId()==null?null:version(agencyId,value.getSupersedesId());
        if(prior!=null&&!input.effectiveFrom().isAfter(prior.getEffectiveFrom())) throw new IllegalArgumentException("A revision must become effective after its predecessor");
        if(overlaps.stream().anyMatch(candidate->prior==null||!candidate.getId().equals(prior.getId()))) throw new IllegalArgumentException("A published rating scale version already overlaps the requested interval");
        String actor=audit.currentActor(); Instant now=Instant.now(); ScaleResponse before=response(agencyId,value);
        value.publish(input.effectiveFrom(),input.effectiveTo(),actor,now); value=versions.saveAndFlush(value);
        if(prior!=null){prior.supersede(input.effectiveFrom(),actor,now);versions.saveAndFlush(prior);}
        ScaleResponse after=response(agencyId,value); audit.record(agencyId,"PUBLISH_RATING_SCALE","SPMS_RATING_SCALE_VERSION",id,value.getDefinitionVersion(),value.getVersion(),before,after,input.reason(),correlationId); return after;
    }

    @Override
    public ScaleResponse retire(String agencyId,String id,Transition input,String correlationId) {
        PerformanceRatingScaleVersion value=version(agencyId,id); requireVersion(value.getVersion(),input.recordVersion()); ScaleResponse before=response(agencyId,value);
        value.retire(audit.currentActor(),input.reason(),Instant.now()); value=versions.saveAndFlush(value); ScaleResponse after=response(agencyId,value);
        audit.record(agencyId,"RETIRE_RATING_SCALE","SPMS_RATING_SCALE_VERSION",id,value.getDefinitionVersion(),value.getVersion(),before,after,input.reason(),correlationId); return after;
    }

    @Override @Transactional(readOnly=true)
    public PreviewResponse preview(String agencyId,String id,PreviewInput input) {
        PerformanceRatingScaleVersion value=version(agencyId,id);
        if(value.getDimensionScoreSource()==PerformanceRatingScaleVersion.DimensionScoreSource.MANUAL_RUBRIC) throw new IllegalArgumentException("Numeric preview is unavailable for a manual rubric scale");
        BigDecimal rounded=input.sampleValue().setScale(value.getRoundingScale(),value.getRoundingMode());
        PerformanceRatingBand match=bandEntities(agencyId,id).stream().filter(b->rounded.compareTo(b.getLowerBound())>=0&&rounded.compareTo(b.getUpperBound())<=0).findFirst().orElseThrow(()->new IllegalArgumentException("Rounded sample is outside the configured rating bands"));
        BigDecimal unit=BigDecimal.ONE.scaleByPowerOfTen(-value.getRoundingScale());
        return new PreviewResponse(input.sampleValue(),rounded,unit,match.getCode(),match.getLabel(),match.getNumericScore(),"roundedSample -> inclusiveBand -> numericScore");
    }

    private void validateBands(PerformanceRatingScaleVersion scale,List<BandInput> inputs) {
        if(inputs==null||inputs.isEmpty()) throw new IllegalArgumentException("At least one rating band is required");
        List<BandInput> ordered=inputs.stream().sorted(Comparator.comparingInt(BandInput::displayOrder)).toList(); Set<String> codes=new HashSet<>(); BigDecimal unit=BigDecimal.ONE.scaleByPowerOfTen(-scale.getRoundingScale());
        for(int index=0;index<ordered.size();index++){BandInput band=ordered.get(index);
            if(band.displayOrder()!=index+1) throw new IllegalArgumentException("Band displayOrder must be consecutive from 1");
            if(!codes.add(band.code().trim().toUpperCase(Locale.ROOT))) throw new IllegalArgumentException("Band codes must be unique");
            if(band.lowerBound().scale()>scale.getRoundingScale()||band.upperBound().scale()>scale.getRoundingScale()) throw new IllegalArgumentException("Band bounds cannot exceed the rating scale precision");
            if(band.upperBound().compareTo(band.lowerBound())<0) throw new IllegalArgumentException("Band upperBound cannot be less than lowerBound");
            if(band.numericScore().compareTo(scale.getMinimumScore())<0||band.numericScore().compareTo(scale.getMaximumScore())>0) throw new IllegalArgumentException("Band numericScore must be inside the scale score range");
            if(index==0&&band.lowerBound().compareTo(scale.getMinimumScore())!=0) throw new IllegalArgumentException("Rating bands must begin at minimumScore");
            if(index>0&&band.lowerBound().compareTo(ordered.get(index-1).upperBound().add(unit))!=0) throw new IllegalArgumentException("Rating bands must be non-overlapping and gap-free at the configured precision");
        }
        if(ordered.get(ordered.size()-1).upperBound().compareTo(scale.getMaximumScore())!=0) throw new IllegalArgumentException("Rating bands must end at maximumScore");
    }

    private BandInput input(PerformanceRatingBand b){return new BandInput(b.getCode(),b.getNumericScore(),b.getLabel(),b.getLowerBound(),b.getUpperBound(),b.getDisplayOrder(),b.getGuidance());}
    private ScaleResponse response(String agencyId,PerformanceRatingScaleVersion v){PerformanceRatingScale scale=scale(agencyId,v.getRatingScaleId());List<BandResponse> rows=bandEntities(agencyId,v.getId()).stream().map(b->new BandResponse(b.getId(),b.getCode(),b.getNumericScore(),b.getLabel(),b.getLowerBound(),b.getUpperBound(),b.getDisplayOrder(),b.getGuidance())).toList();return new ScaleResponse(v.getId(),scale.getId(),scale.getCode(),v.getTitle(),v.getDescription(),v.getLegalBasis(),v.getPolicyVersionId(),v.getDefinitionVersion(),v.getSupersedesId(),v.getStatus().name(),v.getMinimumScore(),v.getMaximumScore(),v.getRoundingScale(),v.getRoundingMode().name(),v.getMeasureType().name(),v.getDirection().name(),v.getAggregation().name(),v.getMissingValuePolicy().name(),v.getDimensionScoreSource().name(),v.getEffectiveFrom(),v.getEffectiveTo(),v.getPublishedBy(),v.getPublishedAt(),v.getRetiredBy(),v.getRetiredAt(),v.getRetirementReason(),v.getVersion(),rows);}
    private PerformanceRatingScaleVersion newVersion(String agencyId,String scaleId,int number,String prior,ScaleInput input){return new PerformanceRatingScaleVersion(agencyId,scaleId,input.policyVersionId(),number,prior,input.title(),input.description(),input.legalBasis(),input.minimumScore(),input.maximumScore(),input.roundingScale(),input.roundingMode(),input.measureType(),input.direction(),input.dimensionScoreSource());}
    private List<PerformanceRatingBand> bandEntities(String agencyId,String id){return bands.findByAgencyIdAndRatingScaleVersionIdOrderByDisplayOrder(agencyId,id);}
    private PerformanceRatingScaleVersion version(String agencyId,String id){return versions.findByIdAndAgencyId(id,agencyId).orElseThrow(()->new ResourceNotFoundException("Rating scale version not found"));}
    private PerformanceRatingScale scale(String agencyId,String id){return scales.findByIdAndAgencyId(id,agencyId).orElseThrow(()->new ResourceNotFoundException("Rating scale not found"));}
    private PerformancePolicyVersion requirePolicy(String agencyId,String id){return policies.findByIdAndAgencyId(id,agencyId).orElseThrow(()->new ResourceNotFoundException("Performance policy version not found"));}
    private static void requireVersion(long actual,Long expected){if(expected==null||actual!=expected)throw new OptimisticConflictException("Expected recordVersion "+expected+" but current version is "+actual);}
}
