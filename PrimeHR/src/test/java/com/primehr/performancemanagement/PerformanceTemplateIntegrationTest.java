package com.primehr.performancemanagement;

import com.primehr.performancemanagement.api.PerformanceTemplateDtos.*;
import com.primehr.performancemanagement.application.PerformanceTemplateService;
import com.primehr.performancemanagement.domain.*;
import com.primehr.performancemanagement.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class PerformanceTemplateIntegrationTest {
    @Autowired PerformanceTemplateService templates; @Autowired PerformancePolicyRepository policyRoots;
    @Autowired PerformancePolicyVersionRepository policyVersions; @Autowired PerformanceRatingScaleRepository scaleRoots;
    @Autowired PerformanceRatingScaleVersionRepository scaleVersions; @Autowired PerformanceRatingBandRepository bands;
    @Autowired PerformanceSuccessIndicatorRepository indicatorRoots; @Autowired PerformanceSuccessIndicatorVersionRepository indicatorVersions;

    @Test void metadataWeightsExactVersionsReadinessFormulaEffectivityAndImmutabilityAreGoverned(){
        String a="TEMPLATE-A";PerformancePolicyVersion policy=policy(a);PerformanceRatingScaleVersion scale=scale(a,policy);PerformanceSuccessIndicatorVersion indicator=indicator(a,policy,scale);
        TemplateResponse draft=templates.create(a,new TemplateInput("IPCR","IPCR",null,PerformanceTemplateVersion.FormType.IPCR,policy.getId(),scale.getId(),"Individual Performance Commitment and Review","CSC SPMS Guidelines",null),null);
        assertThat(draft.formLabel()).contains("Individual Performance");assertThat(templates.readiness(a,draft.id()).ready()).isFalse();
        assertThatThrownBy(()->templates.structure(a,draft.id(),new StructureInput(draft.recordVersion(),List.of(section(indicator,new BigDecimal("90")))),null)).hasMessageContaining("Section weights");
        TemplateResponse configured=templates.structure(a,draft.id(),new StructureInput(draft.recordVersion(),List.of(section(indicator,new BigDecimal("100")))),null);
        assertThat(configured.sections().get(0).code()).isEqualTo("CORE-OUTPUTS");assertThat(configured.sections().get(0).items().get(0).required()).isTrue();
        String item=configured.sections().get(0).items().get(0).id();
        assertThatThrownBy(()->templates.preview(a,draft.id(),new PreviewInput(List.of()))).hasMessageContaining("ERROR policy");
        PreviewResponse preview=templates.preview(a,draft.id(),new PreviewInput(List.of(new ItemScore(item,new BigDecimal("4.25")))));
        assertThat(preview.overallScore()).isEqualByComparingTo("4.25");assertThat(preview.ratingLabel()).isEqualTo("High");
        assertThatThrownBy(()->templates.publish(a,draft.id(),new Transition(configured.recordVersion(),"approved",LocalDate.of(2025,12,31),null),null)).hasMessageContaining("effectivity");
        TemplateResponse published=templates.publish(a,draft.id(),new Transition(configured.recordVersion(),"approved",LocalDate.of(2026,1,1),null),null);
        assertThatThrownBy(()->templates.structure(a,published.id(),new StructureInput(published.recordVersion(),List.of(section(indicator,new BigDecimal("100")))),null)).isInstanceOf(com.primehr.shared.exception.IllegalLifecycleTransitionException.class);
        TemplateResponse revision=templates.revision(a,published.id(),new Transition(published.recordVersion(),"revision"),null);
        assertThat(revision.sections()).singleElement().satisfies(s->{assertThat(s.code()).isEqualTo("CORE-OUTPUTS");assertThat(s.items().get(0).evidenceOverride()).isEqualTo("Certified MOV");});
        templates.publish(a,revision.id(),new Transition(revision.recordVersion(),"successor",LocalDate.of(2027,1,1),null),null);
        assertThat(templates.get(a,published.id()).effectiveTo()).isEqualTo(LocalDate.of(2026,12,31));
    }
    private SectionInput section(PerformanceSuccessIndicatorVersion indicator,BigDecimal weight){return new SectionInput(PerformanceTemplateSection.Type.CORE,"CORE-OUTPUTS","Core Outputs","Major final outputs",weight,1,List.of(new ItemInput(indicator.getId(),"Approved KPI",new BigDecimal("100"),true,"Certified MOV",1)));}
    private PerformancePolicyVersion policy(String a){PerformancePolicy r=policyRoots.saveAndFlush(new PerformancePolicy(a,"P"));PerformancePolicyVersion v=policyVersions.saveAndFlush(new PerformancePolicyVersion(a,r.getId(),1,null,"Policy",null,"Basis","SPMS",PerformancePolicyVersion.Frequency.ANNUAL,false,false,false,false,false));v.publish(LocalDate.of(2026,1,1),null,"tester",Instant.now());return policyVersions.saveAndFlush(v);}
    private PerformanceRatingScaleVersion scale(String a,PerformancePolicyVersion p){PerformanceRatingScale r=scaleRoots.saveAndFlush(new PerformanceRatingScale(a,"S"));PerformanceRatingScaleVersion v=scaleVersions.saveAndFlush(new PerformanceRatingScaleVersion(a,r.getId(),p.getId(),1,null,"Scale",null,"Basis",BigDecimal.ONE,new BigDecimal("5"),2,RoundingMode.HALF_UP,PerformanceRatingScaleVersion.MeasureType.NUMBER,PerformanceRatingScaleVersion.Direction.HIGHER_IS_BETTER,PerformanceRatingScaleVersion.DimensionScoreSource.THRESHOLD_BAND));bands.saveAll(List.of(new PerformanceRatingBand(a,v.getId(),"LOW",BigDecimal.ONE,"Low",BigDecimal.ONE,new BigDecimal("2.99"),1,null),new PerformanceRatingBand(a,v.getId(),"HIGH",new BigDecimal("5"),"High",new BigDecimal("3"),new BigDecimal("5"),2,null)));v.bandsReplaced();v.publish(LocalDate.of(2026,1,1),null,"tester",Instant.now());return scaleVersions.saveAndFlush(v);}
    private PerformanceSuccessIndicatorVersion indicator(String a,PerformancePolicyVersion p,PerformanceRatingScaleVersion s){PerformanceSuccessIndicator r=indicatorRoots.saveAndFlush(new PerformanceSuccessIndicator(a,"I"));PerformanceSuccessIndicatorVersion v=indicatorVersions.saveAndFlush(new PerformanceSuccessIndicatorVersion(a,r.getId(),p.getId(),s.getId(),1,null,"Indicator","Definition","Output","KRA","KPI","Success",PerformanceRatingScaleVersion.MeasureType.NUMBER,"count",PerformanceRatingScaleVersion.Direction.HIGHER_IS_BETTER,BigDecimal.TEN,null,null,true,"MOV"));v.publish(LocalDate.of(2026,1,1),null,"tester",Instant.now());return indicatorVersions.saveAndFlush(v);}
}
