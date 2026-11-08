package com.primehr.learningdevelopment.plan.infrastructure;
import com.primehr.learningdevelopment.plan.domain.LdIdpRouteStep;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface LdIdpRouteStepRepository extends JpaRepository<LdIdpRouteStep,String>{List<LdIdpRouteStep>findByAgencyIdAndRouteIdOrderByLevelAsc(String agency,String route);Optional<LdIdpRouteStep>findByAgencyIdAndRouteIdAndLevel(String agency,String route,int level);}
