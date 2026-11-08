package com.primehr.succession.infrastructure;
import com.primehr.succession.domain.SuccessionCareerFamily; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface SuccessionCareerFamilyRepository extends JpaRepository<SuccessionCareerFamily,String>{Optional<SuccessionCareerFamily> findByIdAndAgencyId(String id,String agencyId);boolean existsByAgencyIdAndNormalizedCode(String agencyId,String normalizedCode);}
