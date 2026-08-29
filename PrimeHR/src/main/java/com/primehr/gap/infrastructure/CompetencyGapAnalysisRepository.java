package com.primehr.gap.infrastructure;

import com.primehr.gap.domain.CompetencyGapAnalysis;
import com.primehr.gap.domain.GapClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompetencyGapAnalysisRepository extends JpaRepository<CompetencyGapAnalysis, String> {
    Optional<CompetencyGapAnalysis> findByIdAndAgencyId(String id, String agencyId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select analysis from CompetencyGapAnalysis analysis where analysis.id=:id and analysis.agencyId=:agencyId")
    Optional<CompetencyGapAnalysis> findByIdAndAgencyIdForUpdate(@Param("id") String id,
                                                                 @Param("agencyId") String agencyId);
    Optional<CompetencyGapAnalysis> findByAgencyIdAndRequestKey(String agencyId, String requestKey);
    Optional<CompetencyGapAnalysis> findByAgencyIdAndSubjectEmployeeIdAndAnalysisDateAndPositionProfileIdAndPersonProfileIdAndPrioritySchemeId(
            String agencyId, Long subjectEmployeeId, java.time.LocalDate analysisDate,
            String positionProfileId, String personProfileId, String prioritySchemeId);
    Optional<CompetencyGapAnalysis> findFirstByAgencyIdAndSubjectEmployeeNoIgnoreCaseOrderByAnalysisDateDescGeneratedAtDesc(
            String agencyId, String employeeNo);

    @Query("select distinct analysis from CompetencyGapAnalysis analysis " +
            "left join CompetencyGapItem item on item.analysis=analysis " +
            "where analysis.agencyId=:agencyId " +
            "and (:employeeNo is null or upper(analysis.subjectEmployeeNo)=upper(:employeeNo)) " +
            "and (:classification is null or item.gapClassification=:classification) " +
            "and (:priorityCode is null or upper(item.priorityCode)=upper(:priorityCode))")
    Page<CompetencyGapAnalysis> search(@Param("agencyId") String agencyId,
                                       @Param("employeeNo") String employeeNo,
                                       @Param("classification") String classification,
                                       @Param("priorityCode") String priorityCode,
                                       Pageable pageable);
}
