package com.humanresource.saln;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SalnCorrectionRepository extends JpaRepository<SalnCorrection,Long>{List<SalnCorrection> findBySalnIdOrderByRequestedAtDesc(Long salnId);List<SalnCorrection> findBySalnIdAndResolvedFalse(Long salnId);}
