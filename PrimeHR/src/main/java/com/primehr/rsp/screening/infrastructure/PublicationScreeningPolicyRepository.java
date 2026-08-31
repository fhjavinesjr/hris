package com.primehr.rsp.screening.infrastructure;
import com.primehr.rsp.screening.domain.PublicationScreeningPolicy; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PublicationScreeningPolicyRepository extends JpaRepository<PublicationScreeningPolicy,String>{Optional<PublicationScreeningPolicy> findByAgencyIdAndPublicationId(String agency,String publicationId);boolean existsByAgencyIdAndPublicationId(String agency,String publicationId);}
