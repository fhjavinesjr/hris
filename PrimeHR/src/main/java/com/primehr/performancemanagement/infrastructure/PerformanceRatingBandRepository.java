package com.primehr.performancemanagement.infrastructure;
import com.primehr.performancemanagement.domain.PerformanceRatingBand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PerformanceRatingBandRepository extends JpaRepository<PerformanceRatingBand,String>{
 List<PerformanceRatingBand> findByAgencyIdAndRatingScaleVersionIdOrderByDisplayOrder(String agencyId,String versionId);
 void deleteByAgencyIdAndRatingScaleVersionId(String agencyId,String versionId);
 long countByAgencyIdAndRatingScaleVersionId(String agencyId,String versionId);
 Optional<PerformanceRatingBand> findByIdAndAgencyId(String id,String agencyId);
}
