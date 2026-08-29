package com.primehr.learning.referral.application;

import com.primehr.gap.domain.*;
import com.primehr.gap.infrastructure.*;
import com.primehr.learning.referral.api.LdReferralDtos.*;
import com.primehr.learning.referral.domain.*;
import com.primehr.learning.referral.infrastructure.*;
import com.primehr.shared.api.PageResponse;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service @Transactional
public class LdReferralServiceImpl implements LdReferralService {
    private final LdReferralRepository referrals; private final LdReferralItemRepository referralItems;
    private final CompetencyGapAnalysisRepository analyses; private final CompetencyGapItemRepository gapItems;
    private final PrimeHrAuditService audit;
    public LdReferralServiceImpl(LdReferralRepository referrals, LdReferralItemRepository referralItems,
            CompetencyGapAnalysisRepository analyses, CompetencyGapItemRepository gapItems, PrimeHrAuditService audit) {
        this.referrals=referrals; this.referralItems=referralItems; this.analyses=analyses; this.gapItems=gapItems; this.audit=audit;
    }

    @Override @Transactional(readOnly=true)
    public PageResponse<SummaryResponse> list(String agency, String employeeNo, LdReferralStatus status, int page, int size) {
        validatePage(page,size); Pageable pageable=PageRequest.of(page,size,Sort.by(Sort.Order.desc("createdAt")));
        String employee=normalize(employeeNo); Page<LdReferral> result;
        if (employee!=null && status!=null) result=referrals.findByAgencyIdAndSubjectEmployeeNoContainingIgnoreCaseAndStatus(agency,employee,status,pageable);
        else if (employee!=null) result=referrals.findByAgencyIdAndSubjectEmployeeNoContainingIgnoreCase(agency,employee,pageable);
        else if (status!=null) result=referrals.findByAgencyIdAndStatus(agency,status,pageable);
        else result=referrals.findByAgencyId(agency,pageable);
        return PageResponse.from(result,this::summary);
    }
    @Override @Transactional(readOnly=true) public Response get(String agency,String id){return response(referral(agency,id));}
    @Override public Response create(String agency,CreateRequest request,String correlation){
        CompetencyGapAnalysis analysis=lockAnalysis(agency,request.analysisId());
        LdReferral entity=referrals.saveAndFlush(new LdReferral(agency,analysis,request.developmentNeed(),request.recommendedIntervention(),request.targetCompletionDate(),request.referralReason(),request.remarks()));
        Response after=response(entity); audit.record(agency,"CREATE_DRAFT","LD_REFERRAL",entity.getId(),1,entity.getVersion(),null,after,null,correlation); return after;
    }
    @Override public Response update(String agency,String id,UpdateRequest request,String correlation){
        LdReferral entity=referral(agency,id); requireVersion(entity,request.recordVersion()); Response before=response(entity);
        entity.update(request.developmentNeed(),request.recommendedIntervention(),request.targetCompletionDate(),request.referralReason(),request.remarks());
        entity=referrals.saveAndFlush(entity); Response after=response(entity); audit.record(agency,"UPDATE_DRAFT","LD_REFERRAL",id,1,entity.getVersion(),before,after,null,correlation); return after;
    }
    @Override public Response addItems(String agency,String id,AddItemsRequest request,String correlation){
        LdReferral entity=referral(agency,id); requireVersion(entity,request.recordVersion()); entity.requireDraft();
        lockAnalysis(agency,entity.getAnalysis().getId()); Response before=response(entity);
        for(String gapId:new LinkedHashSet<>(request.gapItemIds())){
            CompetencyGapItem gap=gapItems.findById(gapId).filter(item->item.getAnalysis().getId().equals(entity.getAnalysis().getId()))
                    .orElseThrow(()->new ResourceNotFoundException("Gap item not found for the referral analysis"));
            if(gap.getGapClassification()!=GapClassification.BELOW && gap.getGapClassification()!=GapClassification.NOT_ASSESSED)
                throw new IllegalArgumentException("Only BELOW or NOT_ASSESSED gap items may be referred");
            Optional<LdReferralItem> existing=referralItems.findByReferralIdAndGapItemId(id,gapId);
            if(existing.isPresent()) { if(existing.get().isActive()) throw new OptimisticConflictException("The gap item is already selected"); existing.get().restore(); referralItems.save(existing.get()); continue; }
            if(referralItems.existsActiveClaim(agency,entity.getAnalysis().getId(),gapId))
                throw new OptimisticConflictException("The gap item already belongs to an active referral");
            referralItems.save(new LdReferralItem(agency,entity,gap));
        }
        referralItems.flush(); Response after=response(entity); audit.record(agency,"ADD_REFERRAL_ITEMS","LD_REFERRAL",id,1,entity.getVersion(),before,after,null,correlation); return after;
    }
    @Override public Response archiveItem(String agency,String id,String itemId,ItemTransitionRequest request,String correlation){
        LdReferral entity=referral(agency,id); entity.requireDraft(); LdReferralItem item=referralItems.findByIdAndReferralId(itemId,id)
                .orElseThrow(()->new ResourceNotFoundException("Referral item not found")); requireVersion(item,request.recordVersion());
        Response before=response(entity); item.archive(); referralItems.saveAndFlush(item); Response after=response(entity);
        audit.record(agency,"ARCHIVE_REFERRAL_ITEM","LD_REFERRAL",id,1,entity.getVersion(),before,after,null,correlation); return after;
    }
    @Override public Response submit(String agency,String id,TransitionRequest request,String correlation){
        LdReferral entity=referral(agency,id); requireVersion(entity,request.recordVersion()); entity.requireDraft();
        lockAnalysis(agency,entity.getAnalysis().getId()); List<LdReferralItem> selected=activeItems(entity);
        if(selected.isEmpty()) throw new IllegalArgumentException("At least one active actionable gap item is required");
        for(LdReferralItem item:selected) if(referralItems.countActiveClaims(agency,entity.getAnalysis().getId(),item.getGapItem().getId())>1)
            throw new OptimisticConflictException("A selected gap item belongs to another active referral");
        Response before=response(entity); entity.submit(audit.currentActor(),Instant.now()); entity=referrals.saveAndFlush(entity); Response after=response(entity);
        audit.record(agency,"SUBMIT_REFERRAL","LD_REFERRAL",id,1,entity.getVersion(),before,after,request.reason(),correlation); return after;
    }
    @Override public Response archive(String agency,String id,TransitionRequest request,String correlation){
        LdReferral entity=referral(agency,id); requireVersion(entity,request.recordVersion()); Response before=response(entity);
        entity.archiveDraft(); entity=referrals.saveAndFlush(entity); Response after=response(entity);
        audit.record(agency,"ARCHIVE_DRAFT","LD_REFERRAL",id,1,entity.getVersion(),before,after,request.reason(),correlation); return after;
    }
    private CompetencyGapAnalysis lockAnalysis(String agency,String id){return analyses.findByIdAndAgencyIdForUpdate(id,agency).orElseThrow(()->new ResourceNotFoundException("Competency gap analysis not found"));}
    private LdReferral referral(String agency,String id){return referrals.findByIdAndAgencyId(id,agency).orElseThrow(()->new ResourceNotFoundException("L&D referral not found"));}
    private List<LdReferralItem> items(LdReferral entity){return referralItems.findByReferralIdOrderByDisplayOrderAscCompetencyCodeAsc(entity.getId());}
    private List<LdReferralItem> activeItems(LdReferral entity){return items(entity).stream().filter(LdReferralItem::isActive).toList();}
    private SummaryResponse summary(LdReferral r){return new SummaryResponse(r.getId(),r.getAnalysis().getId(),r.getSubjectEmployeeId(),r.getSubjectEmployeeNo(),r.getSubjectDisplayName(),r.getAnalysisDate(),r.getPositionName(),r.getStatus(),r.getTargetCompletionDate(),activeItems(r).size(),r.getVersion(),r.getCreatedBy(),r.getCreatedAt(),r.getReferredBy(),r.getReferredAt());}
    private Response response(LdReferral r){return new Response(r.getId(),r.getAnalysis().getId(),r.getSubjectEmployeeId(),r.getSubjectEmployeeNo(),r.getSubjectDisplayName(),r.getAnalysisDate(),r.getPositionName(),r.getStatus(),r.getDevelopmentNeed(),r.getRecommendedIntervention(),r.getTargetCompletionDate(),r.getReferralReason(),r.getRemarks(),r.getVersion(),r.getCreatedBy(),r.getCreatedAt(),r.getUpdatedBy(),r.getUpdatedAt(),r.getReferredBy(),r.getReferredAt(),items(r).stream().map(this::item).toList());}
    private ItemResponse item(LdReferralItem i){return new ItemResponse(i.getId(),i.getGapItem().getId(),i.getCompetencyCode(),i.getCompetencyName(),i.getGapClassification(),i.getNotAssessedReason(),i.getGapValue(),i.getPriorityCode(),i.getPriorityLabel(),i.getPriorityRank(),i.getDisplayOrder(),i.isActive(),i.getVersion());}
    private static void requireVersion(LdReferral r,long v){if(r.getVersion()!=v)throw new OptimisticConflictException("Expected recordVersion "+v+" but current version is "+r.getVersion());}
    private static void requireVersion(LdReferralItem r,long v){if(r.getVersion()!=v)throw new OptimisticConflictException("Expected item recordVersion "+v+" but current version is "+r.getVersion());}
    private static String normalize(String v){return v==null||v.isBlank()?null:v.trim();}
    private static void validatePage(int p,int s){if(p<0||s<1||s>100)throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");}
}
