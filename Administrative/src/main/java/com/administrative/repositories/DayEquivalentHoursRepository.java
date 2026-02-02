package com.administrative.repositories;

import com.administrative.entitymodels.DayEquivalentHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DayEquivalentHoursRepository extends JpaRepository<DayEquivalentHours, Long> {

    void deleteByEffectivityDate(LocalDateTime effectivityDate);

    List<DayEquivalentHours> findByEffectivityDate(LocalDateTime effectivityDate);

}
