package com.administrative.repositories;

import com.administrative.entitymodels.DayEquivalentMinutes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DayEquivalentMinutesRepository extends JpaRepository<DayEquivalentMinutes, Long> {

    void deleteByEffectivityDate(LocalDateTime effectivityDate);

    List<DayEquivalentMinutes> findByEffectivityDate(LocalDateTime effectivityDate);

}
