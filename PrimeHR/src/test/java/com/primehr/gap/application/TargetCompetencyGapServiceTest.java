package com.primehr.gap.application;

import com.primehr.assessment.domain.PersonCompetencyProfile;
import com.primehr.assessment.domain.PersonCompetencyResult;
import com.primehr.assessment.infrastructure.PersonCompetencyProfileRepository;
import com.primehr.assessment.infrastructure.PersonCompetencyResultRepository;
import com.primehr.competency.domain.Competency;
import com.primehr.competency.domain.ProficiencyLevel;
import com.primehr.competency.domain.ProficiencyScale;
import com.primehr.gap.domain.GapClassification;
import com.primehr.positionprofile.domain.*;
import com.primehr.positionprofile.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetCompetencyGapServiceTest {
    @Test
    void calculatesExactTargetPositionGapWithoutUsingCurrentAppointmentTarget() {
        var profiles=mock(PositionProfileRepository.class);
        var requirements=mock(PositionProfileRequirementRepository.class);
        var people=mock(PersonCompetencyProfileRepository.class);
        var results=mock(PersonCompetencyResultRepository.class);
        var service=new TargetCompetencyGapServiceImpl(profiles,requirements,people,results);
        var profile=mock(PositionProfile.class);var person=mock(PersonCompetencyProfile.class);
        var requirement=mock(PositionProfileRequirement.class);var competency=mock(Competency.class);
        var scale=mock(ProficiencyScale.class);var required=mock(ProficiencyLevel.class);
        var result=mock(PersonCompetencyResult.class);var attained=mock(ProficiencyLevel.class);
        when(profiles.findAll(
                org.mockito.ArgumentMatchers
                        .<org.springframework.data.jpa.domain.Specification<PositionProfile>>any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(profile)));
        when(profile.getId()).thenReturn("target-profile");when(profile.getDefinitionVersion()).thenReturn(3);
        when(profile.getContentRevision()).thenReturn(7L);when(profile.getSourceFingerprint()).thenReturn("target-fp");
        when(people.findEffective(eq("A"),eq("EMP-1"),any(LocalDate.class),any(Pageable.class)))
                .thenReturn(List.of(person));
        when(person.getId()).thenReturn("person-profile");when(person.getProfileVersion()).thenReturn(2);
        when(person.getValidFrom()).thenReturn(LocalDate.of(2026,1,1));
        when(results.findByPersonProfileIdOrderByCompetencyCode("person-profile")).thenReturn(List.of(result));
        when(requirements.findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc("target-profile","A"))
                .thenReturn(List.of(requirement));
        when(requirement.getId()).thenReturn("requirement");when(requirement.getCompetency()).thenReturn(competency);
        when(requirement.getRequiredProficiencyLevel()).thenReturn(required);
        when(requirement.getClassification()).thenReturn(RequirementClassification.MANDATORY);
        when(requirement.getDisplayOrder()).thenReturn(1);
        when(competency.getId()).thenReturn("competency");when(competency.getCode()).thenReturn("C1");
        when(competency.getName()).thenReturn("Competency");when(competency.getDefinitionVersion()).thenReturn(4);
        when(competency.getProficiencyScale()).thenReturn(scale);when(scale.getId()).thenReturn("scale");
        when(scale.getDefinitionVersion()).thenReturn(1);when(required.getId()).thenReturn("required");
        when(required.getCode()).thenReturn("L3");when(required.getLabel()).thenReturn("Level 3");
        when(required.getLevelOrder()).thenReturn(3);when(result.getCompetency()).thenReturn(competency);
        when(result.getAttainedLevel()).thenReturn(attained);when(attained.getId()).thenReturn("attained");
        when(attained.getCode()).thenReturn("L2");when(attained.getLabel()).thenReturn("Level 2");
        when(attained.getLevelOrder()).thenReturn(2);when(attained.getScale()).thenReturn(scale);

        var out=service.calculate("A",PositionTargetType.PLANTILLA,20L,30L,"EMP-1",LocalDate.of(2026,11,4));
        assertThat(out.positionProfileId()).isEqualTo("target-profile");
        assertThat(out.personProfileId()).isEqualTo("person-profile");
        assertThat(out.items()).singleElement().satisfies(item->{
            assertThat(item.gap()).isEqualTo(1);
            assertThat(item.classification()).isEqualTo(GapClassification.BELOW);
        });
    }
}
