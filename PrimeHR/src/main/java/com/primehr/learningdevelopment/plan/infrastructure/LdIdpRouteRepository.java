package com.primehr.learningdevelopment.plan.infrastructure;
import com.primehr.learningdevelopment.plan.domain.LdIdpRoute;import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface LdIdpRouteRepository extends JpaRepository<LdIdpRoute,String>{Optional<LdIdpRoute>findFirstByAgencyIdAndPlanIdOrderByRouteRevisionDesc(String agency,String plan);}
