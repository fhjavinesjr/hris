package com.primehr.assessment.infrastructure;

import com.primehr.assessment.domain.AssessmentTool;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssessmentToolRepository extends JpaRepository<AssessmentTool, String> {
    Optional<AssessmentTool> findByAgencyIdAndId(String agencyId, String id);
    List<AssessmentTool> findByAgencyIdAndCycleId(String agencyId, String cycleId, Sort sort);
    boolean existsByCycleIdAndNameIgnoreCase(String cycleId, String name);
    boolean existsByCycleId(String cycleId);
    boolean existsByCycleIdAndStatus(String cycleId, String status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AssessmentTool tool set tool.version = tool.version + 1 " +
            "where tool.agencyId = :agencyId and tool.id = :id and tool.version = :expected")
    int bumpVersion(@Param("agencyId") String agencyId, @Param("id") String id,
                    @Param("expected") long expected);
}
