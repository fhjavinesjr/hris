package com.primehr.learningdevelopment.completion.infrastructure;
import com.primehr.learningdevelopment.completion.domain.LdSessionAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LdSessionAttendanceRepository extends JpaRepository<LdSessionAttendance,String>{
    Optional<LdSessionAttendance> findByIdAndAgencyId(String id,String agency);
    Optional<LdSessionAttendance> findByAgencyIdAndCompletionCaseIdAndSessionId(String agency,String completionCaseId,String sessionId);
    List<LdSessionAttendance> findByAgencyIdAndCompletionCaseIdOrderByStartsAtAsc(String agency,String completionCaseId);
}
