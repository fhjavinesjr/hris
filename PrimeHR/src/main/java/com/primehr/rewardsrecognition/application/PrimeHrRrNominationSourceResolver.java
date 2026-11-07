package com.primehr.rewardsrecognition.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.assessment.application.AssessmentValidationService;
import com.primehr.integration.administrative.PermissionDataScope;
import com.primehr.learningdevelopment.evaluation.application.LdEvaluationService;
import com.primehr.performancemanagement.report.application.PerformanceReportingData.OutcomeQuery;
import com.primehr.performancemanagement.report.application.PerformanceReportingService;
import com.primehr.shared.exception.OptimisticConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class PrimeHrRrNominationSourceResolver implements RrNominationSourceResolver {
    private final AssessmentValidationService competency;
    private final PerformanceReportingService performance;
    private final LdEvaluationService learning;
    private final ObjectMapper json;
    public PrimeHrRrNominationSourceResolver(AssessmentValidationService competency,PerformanceReportingService performance,LdEvaluationService learning,ObjectMapper json){this.competency=competency;this.performance=performance;this.learning=learning;this.json=json;}
    @Override public Snapshot resolve(String agency,Request r,String actor,String correlation){if(r==null||r.type()==null||r.id()==null||r.id().isBlank())throw new IllegalArgumentException("Complete nomination source identity is required");if(r.type()!=com.primehr.rewardsrecognition.domain.RrNominationSource.Type.COMPETENCY_PROFILE&&(r.fingerprint()==null||r.fingerprint().isBlank()))throw new IllegalArgumentException("sourceFingerprint is required for outcome sources");return switch(r.type()){case COMPETENCY_PROFILE->competency(agency,r,actor);case PERFORMANCE_OUTCOME->performance(agency,r,actor,correlation);case LD_COMPLETION->learning(agency,r,actor,correlation);};}
    private Snapshot competency(String agency,Request r,String actor){var p=competency.version(agency,r.id(),actor,PermissionDataScope.AGENCY_WIDE);String fp=sha(p);if(p.profileVersion()!=r.version())throw new OptimisticConflictException("Competency profile source changed; reload the exact source");return new Snapshot(r.type(),p.id(),p.profileVersion(),fp,"Competency profile v"+p.profileVersion(),p.subjectEmployeeNo(),null,p.validatedAt());}
    private Snapshot performance(String agency,Request r,String actor,String correlation){Instant changed=requiredChanged(r);var page=performance.outcomes(agency,new OutcomeQuery(changed.minusSeconds(1),changed.plusSeconds(1),0,500),actor,correlation);var o=page.content().stream().filter(x->x.outcomeId().equals(r.id())).findFirst().orElseThrow(()->new ResourceNotFoundException("Exact performance outcome source not found"));if(o.recordVersion()!=r.version()||!o.outcomeFingerprint().equals(r.fingerprint())||!o.changedAt().equals(changed))throw new OptimisticConflictException("Performance outcome source changed; reload the exact source");return new Snapshot(r.type(),o.outcomeId(),o.recordVersion(),o.outcomeFingerprint(),"Final performance outcome "+o.ratingLabel(),o.employeeNo(),o.businessUnitId(),o.changedAt());}
    private Snapshot learning(String agency,Request r,String actor,String correlation){Instant changed=requiredChanged(r);var page=learning.completionFeed(agency,changed.minusSeconds(1),changed.plusSeconds(1),0,500,actor,correlation);var o=page.content().stream().filter(x->x.completionId().equals(r.id())).findFirst().orElseThrow(()->new ResourceNotFoundException("Exact L&D completion source not found"));if(o.recordVersion()!=r.version()||!o.outcomeFingerprint().equals(r.fingerprint())||!o.changedAt().equals(changed))throw new OptimisticConflictException("L&D completion source changed; reload the exact source");return new Snapshot(r.type(),o.completionId(),o.recordVersion(),o.outcomeFingerprint(),"L&D completion - "+o.offeringTitle(),o.employeeNo(),null,o.changedAt());}
    private static Instant requiredChanged(Request r){if(r.changedAt()==null)throw new IllegalArgumentException("sourceChangedAt is required for outcome sources");return r.changedAt();}
    private String sha(Object value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsString(value).getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("Source fingerprint could not be calculated",e);}}
}
