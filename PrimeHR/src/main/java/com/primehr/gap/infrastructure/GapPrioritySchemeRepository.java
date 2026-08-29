package com.primehr.gap.infrastructure;

import com.primehr.gap.domain.GapPriorityScheme;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GapPrioritySchemeRepository extends JpaRepository<GapPriorityScheme, String> {
    Optional<GapPriorityScheme> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndCodeIgnoreCase(String agencyId, String code);
    boolean existsByAgencyIdAndCodeIgnoreCaseAndStatus(String agencyId, String code, String status);
    Page<GapPriorityScheme> findByAgencyId(String agencyId, Pageable pageable);
    Page<GapPriorityScheme> findByAgencyIdAndStatus(String agencyId, String status, Pageable pageable);
    List<GapPriorityScheme> findByAgencyIdAndStatusOrderByEffectiveFromAscDefinitionVersionAsc(
            String agencyId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select scheme from GapPriorityScheme scheme where scheme.agencyId=:agencyId " +
            "and upper(scheme.code)=upper(:code) order by scheme.definitionVersion asc")
    List<GapPriorityScheme> findChainForUpdate(@Param("agencyId") String agencyId, @Param("code") String code);

    @Query("select scheme from GapPriorityScheme scheme where scheme.agencyId=:agencyId " +
            "and scheme.status='ACTIVE' and scheme.active=true and scheme.effectiveFrom<=:asOf " +
            "and (scheme.effectiveTo is null or scheme.effectiveTo>=:asOf) " +
            "order by scheme.definitionVersion desc, scheme.publishedAt desc")
    List<GapPriorityScheme> findEffective(@Param("agencyId") String agencyId,
                                          @Param("asOf") LocalDate asOf, Pageable pageable);
}
