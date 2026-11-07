package com.primehr.succession.infrastructure;
import com.primehr.succession.domain.SuccessionCareerPathLink; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface SuccessionCareerPathLinkRepository extends JpaRepository<SuccessionCareerPathLink,String>{List<SuccessionCareerPathLink> findByAgencyIdAndFamilyVersionIdOrderByDisplayOrder(String agencyId,String familyVersionId);void deleteByAgencyIdAndFamilyVersionId(String agencyId,String familyVersionId);}
