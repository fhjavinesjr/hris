package com.primehr.performancemanagement;

import com.primehr.performancemanagement.api.PerformanceManagementDtos.*;
import com.primehr.performancemanagement.api.PerformanceRatingScaleDtos.*;
import com.primehr.performancemanagement.application.*;
import com.primehr.performancemanagement.domain.PerformancePolicyVersion;
import com.primehr.performancemanagement.domain.PerformanceRatingScaleVersion.*;
import com.primehr.performancemanagement.infrastructure.PerformanceRatingBandRepository;
import com.primehr.shared.audit.PrimeHrAuditEventRepository;
import com.primehr.shared.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class PerformanceRatingScaleIntegrationTest {
 @Autowired PerformancePolicyService policyService;
 @Autowired PerformanceRatingScaleService scales;
 @Autowired PerformanceRatingBandRepository bands;
 @Autowired PrimeHrAuditEventRepository auditEvents;

 @Test void closedVocabularyBandsPreviewAndAuditAreDeterministic() {
  String agency="RATING-PREVIEW"; PolicyResponse policy=publishedPolicy(agency,"POLICY-A");
  ScaleResponse draft=scales.create(agency,input("FIVE",policy.id(),null),"create-1");
  ScaleResponse configured=scales.bands(agency,draft.id(),new BandsInput(draft.recordVersion(),validBands()),"bands-1");
  long rowsBefore=bands.countByAgencyIdAndRatingScaleVersionId(agency,draft.id()); long auditBefore=auditEvents.count();
  PreviewResponse preview=scales.preview(agency,draft.id(),new PreviewInput(new BigDecimal("3.504")));
  assertThat(preview.roundedScore()).isEqualByComparingTo("3.50");
  assertThat(preview.matchedBandCode()).isEqualTo("VERY-SATISFACTORY");
  assertThat(preview.numericScore()).isEqualByComparingTo("4");
  assertThat(bands.countByAgencyIdAndRatingScaleVersionId(agency,draft.id())).isEqualTo(rowsBefore);
  assertThat(auditEvents.count()).isEqualTo(auditBefore);
  assertThat(configured.recordVersion()).isGreaterThan(draft.recordVersion());
 }

 @Test void gapsOverlapsPrecisionAndStaleWritesFailClosed() {
  String agency="RATING-VALIDATION"; PolicyResponse policy=publishedPolicy(agency,"POLICY-B");
  ScaleResponse draft=scales.create(agency,input("FIVE",policy.id(),null),null);
  List<BandInput> gap=List.of(new BandInput("LOW",BigDecimal.ONE,"Low",new BigDecimal("1.00"),new BigDecimal("2.00"),1,null),new BandInput("HIGH",new BigDecimal("5"),"High",new BigDecimal("2.02"),new BigDecimal("5.00"),2,null));
  assertThatThrownBy(()->scales.bands(agency,draft.id(),new BandsInput(draft.recordVersion(),gap),null)).hasMessageContaining("gap-free");
  assertThatThrownBy(()->scales.bands(agency,draft.id(),new BandsInput(draft.recordVersion(),List.of(new BandInput("ALL",BigDecimal.ONE,"All",new BigDecimal("1.000"),new BigDecimal("5.00"),1,null))),null)).hasMessageContaining("precision");
  ScaleResponse configured=scales.bands(agency,draft.id(),new BandsInput(draft.recordVersion(),validBands()),null);
  assertThatThrownBy(()->scales.update(agency,draft.id(),input("FIVE",policy.id(),draft.recordVersion()),null)).isInstanceOf(OptimisticConflictException.class);
  assertThat(configured.bands()).hasSize(5);
 }

 @Test void publishMakesVersionImmutableAndRevisionCopiesBands() {
  String agency="RATING-LIFECYCLE"; PolicyResponse policy=publishedPolicy(agency,"POLICY-C");
  ScaleResponse draft=scales.create(agency,input("FIVE",policy.id(),null),null);
  ScaleResponse configured=scales.bands(agency,draft.id(),new BandsInput(draft.recordVersion(),validBands()),null);
  ScaleResponse published=scales.publish(agency,draft.id(),new PublishTransition(configured.recordVersion(),LocalDate.of(2026,1,1),null,"approved"),null);
  assertThat(published.status()).isEqualTo("PUBLISHED");
  assertThatThrownBy(()->scales.bands(agency,published.id(),new BandsInput(published.recordVersion(),validBands()),null)).isInstanceOf(IllegalLifecycleTransitionException.class);
  ScaleResponse revision=scales.revision(agency,published.id(),new Transition(published.recordVersion(),"annual review"),null);
  assertThat(revision.definitionVersion()).isEqualTo(2); assertThat(revision.supersedesId()).isEqualTo(published.id()); assertThat(revision.bands()).hasSize(5);
  ScaleResponse next=scales.publish(agency,revision.id(),new PublishTransition(revision.recordVersion(),LocalDate.of(2027,1,1),null,"2027 scale"),null);
  assertThat(next.status()).isEqualTo("PUBLISHED"); assertThat(scales.get(agency,published.id()).effectiveTo()).isEqualTo(LocalDate.of(2026,12,31));
 }

 @Test void manualRubricVocabularyMustBeConsistentAndCannotUseNumericPreview() {
  String agency="RATING-MANUAL"; PolicyResponse policy=publishedPolicy(agency,"POLICY-D");
  ScaleInput inconsistent=new ScaleInput("BAD","Bad",null,"Basis",policy.id(),BigDecimal.ONE,new BigDecimal("5"),2,RoundingMode.HALF_UP,MeasureType.MANUAL_RUBRIC,Direction.HIGHER_IS_BETTER,DimensionScoreSource.MANUAL_RUBRIC,null);
  assertThatThrownBy(()->scales.create(agency,inconsistent,null)).hasMessageContaining("selected together");
  ScaleInput manual=new ScaleInput("MANUAL","Manual",null,"Basis",policy.id(),BigDecimal.ONE,new BigDecimal("5"),2,RoundingMode.HALF_EVEN,MeasureType.MANUAL_RUBRIC,Direction.MANUAL_RUBRIC,DimensionScoreSource.MANUAL_RUBRIC,null);
  ScaleResponse draft=scales.create(agency,manual,null); draft=scales.bands(agency,draft.id(),new BandsInput(draft.recordVersion(),validBands()),null);
  ScaleResponse finalDraft=draft; assertThatThrownBy(()->scales.preview(agency,finalDraft.id(),new PreviewInput(new BigDecimal("3.5")))).hasMessageContaining("unavailable");
 }

 private PolicyResponse publishedPolicy(String agency,String code){PolicyResponse draft=policyService.create(agency,new PolicyInput(code,"Agency policy",null,"CSC and agency policy","SPMS",PerformancePolicyVersion.Frequency.ANNUAL,false,false,false,false,false,null),null);return policyService.publish(agency,draft.id(),new PolicyTransition(draft.recordVersion(),LocalDate.of(2026,1,1),null,"approved"),null);}
 private ScaleInput input(String code,String policy,Long version){return new ScaleInput(code,"Five point scale",null,"CSC and agency policy",policy,BigDecimal.ONE,new BigDecimal("5.00"),2,RoundingMode.HALF_UP,MeasureType.PERCENTAGE,Direction.HIGHER_IS_BETTER,DimensionScoreSource.THRESHOLD_BAND,version);}
 private List<BandInput> validBands(){return List.of(new BandInput("POOR",BigDecimal.ONE,"Poor",new BigDecimal("1.00"),new BigDecimal("1.99"),1,null),new BandInput("UNSATISFACTORY",new BigDecimal("2"),"Unsatisfactory",new BigDecimal("2.00"),new BigDecimal("2.99"),2,null),new BandInput("SATISFACTORY",new BigDecimal("3"),"Satisfactory",new BigDecimal("3.00"),new BigDecimal("3.49"),3,null),new BandInput("VERY-SATISFACTORY",new BigDecimal("4"),"Very Satisfactory",new BigDecimal("3.50"),new BigDecimal("4.49"),4,null),new BandInput("OUTSTANDING",new BigDecimal("5"),"Outstanding",new BigDecimal("4.50"),new BigDecimal("5.00"),5,null));}
}
