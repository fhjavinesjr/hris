package com.primehr.succession.infrastructure;
import com.primehr.succession.domain.SuccessionCriticalityFramework; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface SuccessionCriticalityFrameworkRepository extends JpaRepository<SuccessionCriticalityFramework,String>{Optional<SuccessionCriticalityFramework> findByIdAndAgencyId(String id,String agencyId);boolean existsByAgencyIdAndNormalizedCode(String agencyId,String normalizedCode);}
