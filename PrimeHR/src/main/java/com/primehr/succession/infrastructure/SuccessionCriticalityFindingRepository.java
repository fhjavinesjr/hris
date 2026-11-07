package com.primehr.succession.infrastructure;
import com.primehr.succession.domain.SuccessionCriticalityFinding; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface SuccessionCriticalityFindingRepository extends JpaRepository<SuccessionCriticalityFinding,String>{List<SuccessionCriticalityFinding> findByAgencyIdAndDesignationIdOrderByDisplayOrder(String agencyId,String designationId);void deleteByAgencyIdAndDesignationId(String agencyId,String designationId);}
