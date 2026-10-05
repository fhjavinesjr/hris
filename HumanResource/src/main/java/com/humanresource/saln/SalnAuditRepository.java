package com.humanresource.saln;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SalnAuditRepository extends JpaRepository<SalnAudit,Long>{List<SalnAudit> findBySalnIdOrderByPerformedAtDesc(Long salnId);}
