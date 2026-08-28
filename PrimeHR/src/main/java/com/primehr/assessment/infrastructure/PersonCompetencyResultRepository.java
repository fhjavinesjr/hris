package com.primehr.assessment.infrastructure;
import com.primehr.assessment.domain.PersonCompetencyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PersonCompetencyResultRepository extends JpaRepository<PersonCompetencyResult,String>{
 List<PersonCompetencyResult> findByPersonProfileIdOrderByCompetencyCode(String profileId);
}
