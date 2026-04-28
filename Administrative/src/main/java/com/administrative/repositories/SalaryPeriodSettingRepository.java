package com.administrative.repositories;

import com.administrative.entitymodels.SalaryPeriodSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalaryPeriodSettingRepository extends JpaRepository<SalaryPeriodSetting, Long> {

    List<SalaryPeriodSetting> findAllByOrderBySalaryTypeAscNthOrderAsc();

    List<SalaryPeriodSetting> findByPeriodContextInAndIsActiveTrueOrderBySalaryTypeAscNthOrderAsc(
            List<String> contexts);
}
