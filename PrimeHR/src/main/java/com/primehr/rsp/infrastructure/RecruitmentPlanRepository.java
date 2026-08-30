package com.primehr.rsp.infrastructure;
import com.primehr.rsp.domain.*;import org.springframework.data.domain.*;import org.springframework.data.jpa.repository.JpaRepository;import java.util.Optional;
public interface RecruitmentPlanRepository extends JpaRepository<RecruitmentPlan,String>{Optional<RecruitmentPlan> findByIdAndAgencyId(String id,String agency);boolean existsByAgencyIdAndCodeIgnoreCase(String agency,String code);Page<RecruitmentPlan> findByAgencyId(String agency,Pageable pageable);}
