package com.primehr.operations.infrastructure;
import com.primehr.operations.domain.RetentionPolicyVersion;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface RetentionPolicyVersionRepository extends JpaRepository<RetentionPolicyVersion,String>{List<RetentionPolicyVersion>findByAgencyIdOrderByCodeAscPolicyVersionDesc(String agency);boolean existsByAgencyIdAndCodeAndPolicyVersion(String agency,String code,int version);}
