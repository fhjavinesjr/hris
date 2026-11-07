package com.primehr.operations.infrastructure;
import com.primehr.operations.domain.LegalHold;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface LegalHoldRepository extends JpaRepository<LegalHold,String>{List<LegalHold>findByAgencyIdOrderByPlacedAtDesc(String agency);Optional<LegalHold>findByIdAndAgencyId(String id,String agency);boolean existsByAgencyIdAndRecordTypeAndRecordIdAndActiveTrue(String agency,String type,String id);}
