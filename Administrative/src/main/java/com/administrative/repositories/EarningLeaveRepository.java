package com.administrative.repositories;

import com.administrative.entitymodels.EarningLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EarningLeaveRepository extends JpaRepository<EarningLeave, Long> {

    void deleteByEffectivityDate(LocalDateTime effectivityDate);

    List<EarningLeave> findByEffectivityDate(LocalDateTime effectivityDate);

}
