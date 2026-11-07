package com.primehr.learningdevelopment.completion.infrastructure;
import com.primehr.learningdevelopment.completion.domain.LdCompletionEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LdCompletionEvidenceRepository extends JpaRepository<LdCompletionEvidence,String>{
    Optional<LdCompletionEvidence> findByIdAndAgencyId(String id,String agency);
    List<LdCompletionEvidence> findByAgencyIdAndCompletionCaseIdOrderByUploadedAtAsc(String agency,String completionCaseId);
    boolean existsByAgencyIdAndCompletionCaseIdAndOutputId(String agency,String completionCaseId,String outputId);
}
