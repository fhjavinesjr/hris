package com.primehr.gap.application;

import com.primehr.assessment.domain.PersonCompetencyResult;
import com.primehr.assessment.infrastructure.PersonCompetencyProfileRepository;
import com.primehr.assessment.infrastructure.PersonCompetencyResultRepository;
import com.primehr.gap.domain.GapClassification;
import com.primehr.gap.domain.NotAssessedReason;
import com.primehr.positionprofile.domain.PositionProfile;
import com.primehr.positionprofile.domain.PositionTargetType;
import com.primehr.positionprofile.infrastructure.PositionProfileRepository;
import com.primehr.positionprofile.infrastructure.PositionProfileRequirementRepository;
import com.primehr.positionprofile.infrastructure.PositionProfileSpecifications;
import com.primehr.shared.exception.PublicationConflictException;
import com.primehr.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TargetCompetencyGapServiceImpl implements TargetCompetencyGapService {
    private final PositionProfileRepository profiles;
    private final PositionProfileRequirementRepository requirements;
    private final PersonCompetencyProfileRepository personProfiles;
    private final PersonCompetencyResultRepository personResults;

    public TargetCompetencyGapServiceImpl(PositionProfileRepository profiles,
                                          PositionProfileRequirementRepository requirements,
                                          PersonCompetencyProfileRepository personProfiles,
                                          PersonCompetencyResultRepository personResults) {
        this.profiles = profiles;
        this.requirements = requirements;
        this.personProfiles = personProfiles;
        this.personResults = personResults;
    }

    @Override
    public TargetGapResult calculate(String agencyId, PositionTargetType targetType, Long jobPositionId,
                                     Long plantillaId, String employeeNo, LocalDate asOf) {
        var found = profiles.findAll(PositionProfileSpecifications.effective(agencyId, targetType,
                Objects.requireNonNull(jobPositionId, "jobPositionId"), plantillaId,
                Objects.requireNonNull(asOf, "asOf")), PageRequest.of(0, 2,
                Sort.by("definitionVersion").descending()));
        if (found.getTotalElements() > 1) throw new PublicationConflictException(
                "More than one effective approved target position profile was found");
        PositionProfile profile = found.stream().findFirst().orElseThrow(() -> new ResourceNotFoundException(
                "No effective approved competency profile was found for the target position"));
        var person = personProfiles.findEffective(agencyId, required(employeeNo, "employeeNo"), asOf,
                PageRequest.of(0, 2)).stream().findFirst().orElseThrow(() -> new ResourceNotFoundException(
                "No valid person competency profile was found for the candidate"));
        List<PersonCompetencyResult> results = personResults.findByPersonProfileIdOrderByCompetencyCode(person.getId());
        Map<String, PersonCompetencyResult> byVersion = results.stream().collect(Collectors.toMap(
                r -> r.getCompetency().getId(), Function.identity()));
        Map<String, List<PersonCompetencyResult>> byCode = results.stream().collect(Collectors.groupingBy(
                r -> r.getCompetency().getCode().toUpperCase(Locale.ROOT)));
        var items = requirements.findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(
                profile.getId(), agencyId).stream().map(requirement -> {
            PersonCompetencyResult result = byVersion.get(requirement.getCompetency().getId());
            GapClassification classification;
            NotAssessedReason reason = null;
            Integer gap = null;
            if (result == null || result.getAttainedLevel() == null) {
                classification = GapClassification.NOT_ASSESSED;
                reason = result == null && byCode.containsKey(requirement.getCompetency().getCode().toUpperCase(Locale.ROOT))
                        ? NotAssessedReason.VERSION_NOT_COMPARABLE : NotAssessedReason.NO_RESULT;
                result = null;
            } else if (!requirement.getCompetency().getProficiencyScale().getId()
                    .equals(result.getAttainedLevel().getScale().getId())) {
                classification = GapClassification.NOT_ASSESSED;
                reason = NotAssessedReason.VERSION_NOT_COMPARABLE;
                result = null;
            } else {
                gap = requirement.getRequiredProficiencyLevel().getLevelOrder()
                        - result.getAttainedLevel().getLevelOrder();
                classification = gap > 0 ? GapClassification.BELOW
                        : gap == 0 ? GapClassification.MEETS : GapClassification.EXCEEDS;
            }
            return new TargetGapItem(requirement.getId(), requirement.getCompetency().getId(),
                    requirement.getCompetency().getCode(), requirement.getCompetency().getName(),
                    requirement.getCompetency().getDefinitionVersion(),
                    requirement.getCompetency().getProficiencyScale().getId(),
                    requirement.getCompetency().getProficiencyScale().getDefinitionVersion(),
                    requirement.getRequiredProficiencyLevel().getId(),
                    requirement.getRequiredProficiencyLevel().getCode(),
                    requirement.getRequiredProficiencyLevel().getLabel(),
                    requirement.getRequiredProficiencyLevel().getLevelOrder(),
                    result == null ? null : result.getAttainedLevel().getId(),
                    result == null ? null : result.getAttainedLevel().getCode(),
                    result == null ? null : result.getAttainedLevel().getLabel(),
                    result == null ? null : result.getAttainedLevel().getLevelOrder(), gap, classification, reason,
                    requirement.getClassification().name(), requirement.getCriticalityCode(),
                    requirement.getDisplayOrder());
        }).toList();
        if (items.isEmpty()) throw new PublicationConflictException(
                "The effective target position profile has no active competency requirements");
        return new TargetGapResult(profile.getId(), profile.getDefinitionVersion(), profile.getContentRevision(),
                profile.getSourceFingerprint(), person.getId(), person.getProfileVersion(), person.getValidFrom(),
                person.getValidTo(), Instant.now(), items);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
