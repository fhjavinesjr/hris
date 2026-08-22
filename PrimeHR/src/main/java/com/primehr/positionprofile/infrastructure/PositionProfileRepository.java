package com.primehr.positionprofile.infrastructure;

import com.primehr.positionprofile.domain.PositionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PositionProfileRepository extends JpaRepository<PositionProfile, String>,
        JpaSpecificationExecutor<PositionProfile> {
    Optional<PositionProfile> findByIdAndAgencyId(String id, String agencyId);

    boolean existsByAgencyIdAndTargetKey(String agencyId, String targetKey);

    boolean existsByAgencyIdAndTargetKeyAndStatus(String agencyId, String targetKey, String status);

    boolean existsByAgencyIdAndTargetKeyAndStatusIn(String agencyId, String targetKey,
                                                     Collection<String> statuses);

    List<PositionProfile> findByAgencyIdAndTargetKeyOrderByDefinitionVersionAsc(String agencyId, String targetKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from PositionProfile profile " +
            "where profile.agencyId = :agencyId and profile.targetKey = :targetKey " +
            "order by profile.definitionVersion asc")
    List<PositionProfile> findChainForUpdate(@Param("agencyId") String agencyId,
                                             @Param("targetKey") String targetKey);
}
