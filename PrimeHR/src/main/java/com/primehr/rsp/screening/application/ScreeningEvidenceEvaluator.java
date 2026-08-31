package com.primehr.rsp.screening.application;
import com.primehr.rsp.screening.api.ScreeningPolicyDtos.*; import com.primehr.rsp.screening.domain.ScreeningCriterion; import org.springframework.stereotype.Component; import java.math.BigDecimal; import java.util.*;
@Component public class ScreeningEvidenceEvaluator {
 public Evaluation evaluate(ScreeningCriterion c,EvidenceFacts raw){EvidenceFacts f=raw==null?new EvidenceFacts(Set.of(),Map.of(),Map.of(),Set.of()):raw;Set<String> present=f.presentKeys()==null?Set.of():f.presentKeys();Map<String,BigDecimal> nums=f.numericValues()==null?Map.of():f.numericValues();Map<String,Long> days=f.durationDays()==null?Map.of():f.durationDays();Set<String> declarations=f.declarations()==null?Set.of():f.declarations();String key=c.getSourceKey();return switch(c.getEvaluationMode()){
  case MANUAL_REVIEW -> result(c,"NEEDS_REVIEW","Human review is required; free text is not interpreted automatically.",true);
  case PRESENCE -> present.contains(key)?result(c,"MET","Required immutable evidence is present.",true):result(c,"NOT_MET","Required immutable evidence is absent.",true);
  case DECLARATION -> declarations.contains(key)?result(c,"MET","The configured declaration is present.",true):result(c,"NOT_MET","The configured declaration is absent.",true);
  case NUMERIC_THRESHOLD -> nums.get(key)==null?result(c,"NEEDS_REVIEW","No comparable structured numeric evidence is available.",true):nums.get(key).compareTo(c.getThresholdValue())>=0?result(c,"MET","Structured value meets the configured threshold.",true):result(c,"NOT_MET","Structured value is below the configured threshold.",true);
  case DATE_OR_DURATION -> days.get(key)==null?result(c,"NEEDS_REVIEW","No comparable structured duration evidence is available.",true):c.getThresholdValue()==null?result(c,"NEEDS_REVIEW","No duration threshold is configured.",true):BigDecimal.valueOf(days.get(key)).compareTo(c.getThresholdValue())>=0?result(c,"MET","Structured duration meets the configured threshold.",true):result(c,"NOT_MET","Structured duration is below the configured threshold.",true);
 };}
 private Evaluation result(ScreeningCriterion c,String result,String explanation,boolean confirmation){return new Evaluation(c.getCode(),result,explanation,confirmation);}
}
