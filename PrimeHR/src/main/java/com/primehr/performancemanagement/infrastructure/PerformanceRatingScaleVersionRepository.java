package com.primehr.performancemanagement.infrastructure;
import com.primehr.performancemanagement.domain.PerformanceRatingScaleVersion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;
public interface PerformanceRatingScaleVersionRepository extends JpaRepository<PerformanceRatingScaleVersion,String>{
 Optional<PerformanceRatingScaleVersion> findByIdAndAgencyId(String id,String agencyId);
 List<PerformanceRatingScaleVersion> findByAgencyIdOrderByCreatedAtDesc(String agencyId);
 List<PerformanceRatingScaleVersion> findByAgencyIdAndRatingScaleIdOrderByDefinitionVersionDesc(String agencyId,String ratingScaleId);
 @Query("select v from PerformanceRatingScaleVersion v where v.agencyId=:agency and v.ratingScaleId=:scale and v.status='PUBLISHED' and v.effectiveFrom<=:to and (v.effectiveTo is null or v.effectiveTo>=:from)")
 List<PerformanceRatingScaleVersion> overlapping(@Param("agency")String agency,@Param("scale")String scale,@Param("from")LocalDate from,@Param("to")LocalDate to);
}
