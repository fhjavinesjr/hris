package com.primehr.rewardsrecognition.infrastructure;
import com.primehr.rewardsrecognition.domain.RrFulfillmentEvent;import org.springframework.data.jpa.repository.JpaRepository;import java.util.List;
public interface RrFulfillmentEventRepository extends JpaRepository<RrFulfillmentEvent,String>{List<RrFulfillmentEvent>findByAgencyIdAndAuthorizationIdOrderByRecordedAt(String agencyId,String authorizationId);}
