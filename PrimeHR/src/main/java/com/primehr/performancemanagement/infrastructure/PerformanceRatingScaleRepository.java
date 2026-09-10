package com.primehr.performancemanagement.infrastructure;
import com.primehr.performancemanagement.domain.PerformanceRatingScale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PerformanceRatingScaleRepository extends JpaRepository<PerformanceRatingScale,String>{
 Optional<PerformanceRatingScale> findByIdAndAgencyId(String id,String agencyId);
 boolean existsByAgencyIdAndNormalizedCode(String agencyId,String normalizedCode);
}
