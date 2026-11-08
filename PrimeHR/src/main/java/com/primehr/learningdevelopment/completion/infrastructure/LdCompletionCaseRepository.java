package com.primehr.learningdevelopment.completion.infrastructure;
import com.primehr.learningdevelopment.completion.domain.LdCompletionCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LdCompletionCaseRepository extends JpaRepository<LdCompletionCase,String>{
    Optional<LdCompletionCase> findByIdAndAgencyId(String id,String agency);
    Optional<LdCompletionCase> findByAgencyIdAndEnrollmentId(String agency,String enrollmentId);
    List<LdCompletionCase> findByAgencyIdOrderByCreatedAtDesc(String agency);
    List<LdCompletionCase> findByAgencyIdAndEmployeeNoOrderByCreatedAtDesc(String agency,String employeeNo);
}
