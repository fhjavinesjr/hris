package com.primehr.operations.infrastructure;
import com.primehr.operations.domain.ReconciliationRun;import org.springframework.data.jpa.repository.JpaRepository;import java.util.List;
public interface ReconciliationRunRepository extends JpaRepository<ReconciliationRun,String>{List<ReconciliationRun>findByAgencyIdOrderByStartedAtDesc(String agency);}
