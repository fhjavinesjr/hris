package com.administrative.repositories;

import com.administrative.entitymodels.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    List<SystemConfig> findByCategory(String category);

    boolean existsByConfigKey(String configKey);
}
