package com.primehr.rsp.handoff.infrastructure;
import com.primehr.rsp.handoff.domain.AppointmentHandoffAttempt;import org.springframework.data.jpa.repository.JpaRepository;import java.util.List;
public interface AppointmentHandoffAttemptRepository extends JpaRepository<AppointmentHandoffAttempt,String>{long countByAgencyIdAndHandoffId(String agency,String handoff);List<AppointmentHandoffAttempt>findByAgencyIdAndHandoffIdOrderByAttemptNumberAsc(String agency,String handoff);}
