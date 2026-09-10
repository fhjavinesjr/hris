package com.primehr.performancemanagement;

import com.primehr.performancemanagement.api.PerformanceManagementDtos.*;
import com.primehr.performancemanagement.api.PerformanceRatingScaleDtos.*;
import com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.*;
import com.primehr.performancemanagement.application.*;
import com.primehr.performancemanagement.domain.*;
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
class PerformanceSuccessIndicatorIntegrationTest {
    @Autowired PerformancePolicyService policies; @Autowired PerformanceRatingScaleService scales;
    @Autowired PerformanceSuccessIndicatorService indicators;

    @Test void exactBindingsWeightsRubricsEffectivityPreviewAndLifecycleAreGoverned(){
        String agency="INDICATOR-A";PolicyResponse policy=policy(agency);ScaleResponse scale=scale(agency,policy);
        IndicatorResponse draft=indicators.create(agency,input(policy.id(),scale.id(),null),null);
        assertThat(draft.requiresEvidence()).isTrue();assertThat(draft.description()).isEqualTo("Definition notes");
        assertThatThrownBy(()->indicators.dimensions(agency,draft.id(),new DimensionsInput(draft.recordVersion(),List.of(dimension(scale,new BigDecimal("99"),"QUALITY"))),null)).hasMessageContaining("total 100");
        DimensionInput gap=dimension(scale,new BigDecimal("100"),"QUALITY");
        List<LevelInput> broken=new ArrayList<>(gap.levels());broken.set(1,new LevelInput(scale.bands().get(1).id(),PerformanceIndicatorDimensionLevel.Operator.BETWEEN,new BigDecimal("61"),new BigDecimal("69.99"),null));
        assertThatThrownBy(()->indicators.dimensions(agency,draft.id(),new DimensionsInput(draft.recordVersion(),List.of(new DimensionInput(PerformanceIndicatorDimension.Type.QUALITY,"QUALITY","Quality",new BigDecimal("100"),1,broken))),null)).hasMessageContaining("exhaustive and non-overlapping");
        IndicatorResponse configured=indicators.dimensions(agency,draft.id(),new DimensionsInput(draft.recordVersion(),List.of(dimension(scale,new BigDecimal("100"),"QUALITY"))),null);
        com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.PreviewResponse preview=indicators.preview(agency,draft.id(),new com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.PreviewInput(List.of(new DimensionActual("QUALITY",new BigDecimal("85"),null))));
        assertThat(preview.indicatorScore()).isEqualByComparingTo("4.00");assertThat(preview.dimensions().get(0).code()).isEqualTo("QUALITY");
        assertThatThrownBy(()->indicators.publish(agency,draft.id(),new com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.Transition(configured.recordVersion(),"approved",null,null),null)).hasMessageContaining("effectiveFrom");
        IndicatorResponse published=indicators.publish(agency,draft.id(),new com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.Transition(configured.recordVersion(),"approved",LocalDate.of(2026,1,1),null),null);
        assertThat(published.effectiveFrom()).isEqualTo(LocalDate.of(2026,1,1));
        assertThatThrownBy(()->indicators.update(agency,published.id(),input(policy.id(),scale.id(),published.recordVersion()),null)).isInstanceOf(com.primehr.shared.exception.IllegalLifecycleTransitionException.class);
        IndicatorResponse revision=indicators.revision(agency,published.id(),new com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.Transition(published.recordVersion(),"revision"),null);
        assertThat(revision.dimensions()).singleElement().satisfies(d->assertThat(d.code()).isEqualTo("QUALITY"));
        indicators.publish(agency,revision.id(),new com.primehr.performancemanagement.api.PerformanceSuccessIndicatorDtos.Transition(revision.recordVersion(),"successor",LocalDate.of(2027,1,1),null),null);
        assertThat(indicators.get(agency,published.id()).effectiveTo()).isEqualTo(LocalDate.of(2026,12,31));
    }
    private PolicyResponse policy(String a){PolicyResponse d=policies.create(a,new PolicyInput("P","Policy",null,"Basis","SPMS",PerformancePolicyVersion.Frequency.ANNUAL,false,false,false,false,false,null),null);return policies.publish(a,d.id(),new PolicyTransition(d.recordVersion(),LocalDate.of(2026,1,1),null,"approved"),null);}
    private ScaleResponse scale(String a,PolicyResponse p){ScaleInput i=new ScaleInput("S","Scale",null,"Basis",p.id(),BigDecimal.ONE,new BigDecimal("5"),2,RoundingMode.HALF_UP,PerformanceRatingScaleVersion.MeasureType.PERCENTAGE,PerformanceRatingScaleVersion.Direction.HIGHER_IS_BETTER,PerformanceRatingScaleVersion.DimensionScoreSource.THRESHOLD_BAND,null);ScaleResponse d=scales.create(a,i,null);List<BandInput>b=List.of(new BandInput("B1",BigDecimal.ONE,"One",BigDecimal.ONE,new BigDecimal("1.99"),1,null),new BandInput("B2",new BigDecimal("2"),"Two",new BigDecimal("2"),new BigDecimal("2.99"),2,null),new BandInput("B3",new BigDecimal("3"),"Three",new BigDecimal("3"),new BigDecimal("3.99"),3,null),new BandInput("B4",new BigDecimal("4"),"Four",new BigDecimal("4"),new BigDecimal("4.99"),4,null),new BandInput("B5",new BigDecimal("5"),"Five",new BigDecimal("5"),new BigDecimal("5"),5,null));d=scales.bands(a,d.id(),new BandsInput(d.recordVersion(),b),null);return scales.publish(a,d.id(),new PublishTransition(d.recordVersion(),LocalDate.of(2026,1,1),null,"approved"),null);}
    private IndicatorInput input(String p,String s,Long v){return new IndicatorInput("SI","Title","Output","KRA","KPI","Success",p,s,PerformanceRatingScaleVersion.MeasureType.PERCENTAGE,"percent",PerformanceRatingScaleVersion.Direction.HIGHER_IS_BETTER,new BigDecimal("100"),null,null,"MOV","Definition notes",true,v);}
    private DimensionInput dimension(ScaleResponse s,BigDecimal w,String code){List<LevelInput>l=List.of(new LevelInput(s.bands().get(0).id(),PerformanceIndicatorDimensionLevel.Operator.LT,new BigDecimal("60"),null,null),new LevelInput(s.bands().get(1).id(),PerformanceIndicatorDimensionLevel.Operator.BETWEEN,new BigDecimal("60"),new BigDecimal("69.999999"),null),new LevelInput(s.bands().get(2).id(),PerformanceIndicatorDimensionLevel.Operator.BETWEEN,new BigDecimal("70"),new BigDecimal("79.999999"),null),new LevelInput(s.bands().get(3).id(),PerformanceIndicatorDimensionLevel.Operator.BETWEEN,new BigDecimal("80"),new BigDecimal("89.999999"),null),new LevelInput(s.bands().get(4).id(),PerformanceIndicatorDimensionLevel.Operator.GTE,new BigDecimal("90"),null,null));return new DimensionInput(PerformanceIndicatorDimension.Type.QUALITY,code,"Quality",w,1,l);}
}
