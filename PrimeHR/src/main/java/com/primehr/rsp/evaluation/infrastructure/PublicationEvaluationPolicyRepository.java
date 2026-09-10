package com.primehr.rsp.evaluation.infrastructure;
import com.primehr.rsp.evaluation.domain.PublicationEvaluationPolicy; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface PublicationEvaluationPolicyRepository extends JpaRepository<PublicationEvaluationPolicy,String>{Optional<PublicationEvaluationPolicy> findByAgencyIdAndPublicationId(String agency,String publicationId);boolean existsByAgencyIdAndPublicationId(String agency,String publicationId);}
