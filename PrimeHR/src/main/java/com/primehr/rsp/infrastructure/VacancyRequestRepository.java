package com.primehr.rsp.infrastructure;

import com.primehr.rsp.domain.VacancyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VacancyRequestRepository extends JpaRepository<VacancyRequest,String> {
    List<VacancyRequest> findByPlanIdAndAgencyIdOrderByCreatedAtAsc(String planId,String agency);
    Optional<VacancyRequest> findByIdAndAgencyId(String id,String agency);
    boolean existsByPlanIdAndPlantillaId(String planId,Long plantillaId);

    @Query("""
            select case when count(v) > 0 then true else false end
              from VacancyRequest v
              join v.plan p
             where v.agencyId = :agencyId
               and v.plantillaId = :plantillaId
               and v.active = true
               and p.status <> com.primehr.rsp.domain.RecruitmentPlanStatus.ARCHIVED
               and p.periodStart <= :periodEnd
               and p.periodEnd >= :periodStart
               and (:excludeVacancyId is null or v.id <> :excludeVacancyId)
            """)
    boolean existsActiveForOverlappingPeriod(@Param("agencyId") String agencyId,
                                             @Param("plantillaId") Long plantillaId,
                                             @Param("periodStart") LocalDate periodStart,
                                             @Param("periodEnd") LocalDate periodEnd,
                                             @Param("excludeVacancyId") String excludeVacancyId);
}
