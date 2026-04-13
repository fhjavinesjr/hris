package com.administrative.repositories;

import com.administrative.entitymodels.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findAllByOrderByHolidayDateAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndHolidayIdNot(String code, Long holidayId);
}
