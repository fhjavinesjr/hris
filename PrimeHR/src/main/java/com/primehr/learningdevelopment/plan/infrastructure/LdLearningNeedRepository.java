package com.primehr.learningdevelopment.plan.infrastructure;
import com.primehr.learningdevelopment.plan.domain.LdLearningNeed;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface LdLearningNeedRepository extends JpaRepository<LdLearningNeed,String>{Optional<LdLearningNeed>findByIdAndAgencyId(String id,String agency);List<LdLearningNeed>findByAgencyIdOrderByCreatedAtDesc(String agency);List<LdLearningNeed>findByAgencyIdAndEmployeeNoIgnoreCaseOrderByCreatedAtDesc(String agency,String employeeNo);}
