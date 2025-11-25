package com.administrative.repositories;

import com.administrative.entitymodels.Plantilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlantillaRepository extends JpaRepository<Plantilla, Long> {

    List<Plantilla> findByJobPositionId(Long jobPositionId);

}
