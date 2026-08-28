package com.primehr.positionprofile.infrastructure;

import com.primehr.positionprofile.domain.PositionProfileRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionProfileRequirementRepository extends JpaRepository<PositionProfileRequirement, String> {
    List<PositionProfileRequirement> findByProfileIdAndAgencyIdOrderByDisplayOrderAscIdAsc(
            String profileId, String agencyId);

    Optional<PositionProfileRequirement> findByIdAndProfileIdAndAgencyId(
            String id, String profileId, String agencyId);

    boolean existsByProfileIdAndCompetencyId(String profileId, String competencyId);
    List<PositionProfileRequirement> findByProfileIdAndAgencyIdAndActiveTrueOrderByDisplayOrderAscIdAsc(
            String profileId, String agencyId);
}
