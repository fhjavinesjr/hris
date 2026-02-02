package com.administrative.repositories;

import com.administrative.entitymodels.HazardPay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HazardPayRepository extends JpaRepository<HazardPay, Long> {

    void deleteByEffectivityDate(LocalDateTime effectivityDate);

    List<HazardPay> findByEffectivityDate(LocalDateTime effectivityDate);

}
