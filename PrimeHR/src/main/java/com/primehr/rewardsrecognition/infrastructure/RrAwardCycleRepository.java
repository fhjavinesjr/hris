package com.primehr.rewardsrecognition.infrastructure;
import com.primehr.rewardsrecognition.domain.RrAwardCycle;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface RrAwardCycleRepository extends JpaRepository<RrAwardCycle,String>{Optional<RrAwardCycle>findByIdAndAgencyId(String id,String agency);List<RrAwardCycle>findByAgencyIdOrderByCreatedAtDesc(String agency);boolean existsByAgencyIdAndNormalizedCode(String agency,String code);}
