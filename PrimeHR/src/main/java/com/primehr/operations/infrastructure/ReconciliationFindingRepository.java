package com.primehr.operations.infrastructure;
import com.primehr.operations.domain.ReconciliationFinding;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface ReconciliationFindingRepository extends JpaRepository<ReconciliationFinding,String>{List<ReconciliationFinding>findByAgencyIdAndRunIdOrderByCreatedAtAsc(String agency,String run);Optional<ReconciliationFinding>findByIdAndAgencyId(String id,String agency);}
