package com.primehr.learningdevelopment.completion.infrastructure;
import com.primehr.learningdevelopment.completion.domain.LdCompletionOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LdCompletionOutputRepository extends JpaRepository<LdCompletionOutput,String>{
    Optional<LdCompletionOutput> findByIdAndAgencyId(String id,String agency);
    List<LdCompletionOutput> findByAgencyIdAndCompletionCaseIdOrderByCreatedAtAsc(String agency,String completionCaseId);
}
